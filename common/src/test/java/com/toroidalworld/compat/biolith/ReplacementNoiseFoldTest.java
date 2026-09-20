package com.toroidalworld.compat.biolith;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.terraformersmc.biolith.impl.noise.OpenSimplexNoise2;
import com.toroidalworld.core.FlatShape;
import com.toroidalworld.core.WorldFold;
import com.toroidalworld.core.WorldFolds;
import com.toroidalworld.core.WorldLoopBounds;
import com.toroidalworld.engine.noise.GenerationTransformerContext;

import net.minecraft.core.Direction;
import net.minecraft.util.Mth;

class ReplacementNoiseFoldTest {
    private static final long WORLD_SEED = 0x0153EL;

    private static final double[] OVERWORLD_WEIGHTS = {1.0, 1.0 / 8.0, 1.0 / 16.0, 1.0 / 32.0};

    private static final double[] OVERWORLD_SCALE_QUARTS = {1024.0, 256.0, 64.0, 16.0};

    private static final double SUM_DIVISOR = 1.21875;

    private static final double NORMALIZE_GAIN = 0.5375;

    private static final double NORMALIZE_OFFSET = 0.5;

    private static final int SAMPLES_PER_AXIS = 96;

    private static final int PERIODICITY_SAMPLES = 512;

    private static final double SPREAD_RATIO_TOLERANCE = 0.25;

    private static final double HALF = 0.5;

    private static final double SPLIT_TOLERANCE = 0.15;

    private static final WorldFold TINY = torus(-16, 16);

    private static final WorldFold HUGE = torus(-256, 256);

    private static WorldFold torus(int chunkMin, int chunkMax) {
        return WorldFolds.of(FlatShape.torus(new WorldLoopBounds(chunkMin, chunkMax, chunkMin, chunkMax)));
    }

    private static ReplacementNoiseFold fold() {
        return new ReplacementNoiseFold(OVERWORLD_WEIGHTS);
    }

    private static double folded(ReplacementNoiseFold fold, WorldFold transformer, int quartX, int quartZ) {
        double[] sum = new double[1];
        GenerationTransformerContext.withTransformer(transformer,
                () -> sum[0] = fold.sum(WORLD_SEED, OVERWORLD_SCALE_QUARTS, quartX, quartZ));
        return sum[0];
    }

    private static double normalize(double sum) {
        return Mth.clamp(sum / SUM_DIVISOR * NORMALIZE_GAIN + NORMALIZE_OFFSET, 0.0, 1.0);
    }

    private static double biolithSum(OpenSimplexNoise2 noise, int quartX, int quartZ) {
        double sum = 0.0;
        for (int octave = 0; octave < OVERWORLD_WEIGHTS.length; octave++) {
            double scale = OVERWORLD_SCALE_QUARTS[octave];
            sum += OVERWORLD_WEIGHTS[octave] * noise.sample(quartX / scale, quartZ / scale);
        }

        return sum;
    }

    private record Spread(double mean, double deviation) {
        static Spread of(double[] values) {
            double total = 0.0;
            for (double value : values) {
                total += value;
            }

            double mean = total / values.length;
            double squares = 0.0;
            for (double value : values) {
                squares += (value - mean) * (value - mean);
            }

            return new Spread(mean, Math.sqrt(squares / values.length));
        }
    }

    private static Spread foldedSpread(WorldFold transformer) {
        ReplacementNoiseFold fold = fold();
        double[] values = new double[SAMPLES_PER_AXIS * SAMPLES_PER_AXIS];
        int lapQuarts = transformer.blockDomain(Direction.Axis.X).domainLength / 4;
        int minQuart = transformer.blockDomain(Direction.Axis.X).lowerBound / 4;
        int index = 0;
        for (int xStep = 0; xStep < SAMPLES_PER_AXIS; xStep++) {
            for (int zStep = 0; zStep < SAMPLES_PER_AXIS; zStep++) {
                int quartX = minQuart + xStep * lapQuarts / SAMPLES_PER_AXIS;
                int quartZ = minQuart + zStep * lapQuarts / SAMPLES_PER_AXIS;
                values[index++] = normalize(folded(fold, transformer, quartX, quartZ));
            }
        }

        return Spread.of(values);
    }

    private static double belowMedian(WorldFold transformer) {
        ReplacementNoiseFold fold = fold();
        int lapQuarts = transformer.blockDomain(Direction.Axis.X).domainLength / 4;
        int minQuart = transformer.blockDomain(Direction.Axis.X).lowerBound / 4;
        int below = 0;
        for (int xStep = 0; xStep < SAMPLES_PER_AXIS; xStep++) {
            for (int zStep = 0; zStep < SAMPLES_PER_AXIS; zStep++) {
                int quartX = minQuart + xStep * lapQuarts / SAMPLES_PER_AXIS;
                int quartZ = minQuart + zStep * lapQuarts / SAMPLES_PER_AXIS;
                if (normalize(folded(fold, transformer, quartX, quartZ)) < HALF) {
                    below++;
                }
            }
        }

        return (double) below / (SAMPLES_PER_AXIS * SAMPLES_PER_AXIS);
    }

    private static Spread biolithSpread(WorldFold transformer) {
        OpenSimplexNoise2 noise = new OpenSimplexNoise2(WORLD_SEED);
        double[] values = new double[SAMPLES_PER_AXIS * SAMPLES_PER_AXIS];
        int lapQuarts = transformer.blockDomain(Direction.Axis.X).domainLength / 4;
        int minQuart = transformer.blockDomain(Direction.Axis.X).lowerBound / 4;
        int index = 0;
        for (int xStep = 0; xStep < SAMPLES_PER_AXIS; xStep++) {
            for (int zStep = 0; zStep < SAMPLES_PER_AXIS; zStep++) {
                int quartX = minQuart + xStep * lapQuarts / SAMPLES_PER_AXIS;
                int quartZ = minQuart + zStep * lapQuarts / SAMPLES_PER_AXIS;
                values[index++] = normalize(biolithSum(noise, quartX, quartZ));
            }
        }

        return Spread.of(values);
    }

    @Nested
    class Periodicity {
        @Test
        void repeatsOneLapAway() {
            for (WorldFold transformer : new WorldFold[] {TINY, HUGE}) {
                ReplacementNoiseFold fold = fold();
                int lapQuarts = transformer.blockDomain(Direction.Axis.X).domainLength / 4;
                Random random = new Random(WORLD_SEED);
                for (int sample = 0; sample < PERIODICITY_SAMPLES; sample++) {
                    int quartX = random.nextInt(-lapQuarts, lapQuarts);
                    int quartZ = random.nextInt(-lapQuarts, lapQuarts);
                    assertEquals(folded(fold, transformer, quartX, quartZ),
                            folded(fold, transformer, quartX + lapQuarts, quartZ),
                            "X lap at quart " + quartX + ", " + quartZ);
                    assertEquals(folded(fold, transformer, quartX, quartZ),
                            folded(fold, transformer, quartX, quartZ + lapQuarts),
                            "Z lap at quart " + quartX + ", " + quartZ);
                }
            }
        }

        @Test
        void answersNotFoldedWithoutATransformer() {
            assertFalse(ReplacementNoiseFold.folded(fold().sum(WORLD_SEED, OVERWORLD_SCALE_QUARTS, 0, 0)));
        }

        @Test
        void variesAcrossTheLap() {
            assertTrue(foldedSpread(HUGE).deviation() > 0.01, "the folded field is flat across a huge world");
        }
    }

    @Nested
    class Distribution {
        @Test
        void spreadMatchesBiolithWhereBothHoldTwoPeriods() {
            Spread ours = foldedSpread(HUGE);
            Spread biolith = biolithSpread(HUGE);
            String reading = "ours sd=" + ours.deviation() + ", biolith sd=" + biolith.deviation();
            assertTrue(Math.abs(ours.deviation() - biolith.deviation())
                    <= SPREAD_RATIO_TOLERANCE * biolith.deviation(), reading);
        }

        @Test
        void splitsTheReplacementRangeOnAHugeWorld() {
            assertSplitHolds(HUGE);
        }

        @Test
        void splitsTheReplacementRangeOnATinyWorld() {
            assertSplitHolds(TINY);
        }

        private void assertSplitHolds(WorldFold transformer) {
            double below = belowMedian(transformer);
            assertTrue(Math.abs(below - HALF) <= SPLIT_TOLERANCE,
                    "a rate of one half covers " + below + " of the world");
        }
    }
}
