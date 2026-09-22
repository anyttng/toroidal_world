package com.toroidalworld.compat.biolith;

import java.util.stream.IntStream;

import org.jspecify.annotations.Nullable;

import com.toroidalworld.core.WorldFold;
import com.toroidalworld.engine.noise.GenerationTransformerContext;
import com.toroidalworld.engine.noise.GenerationTransformerContext.Context;
import com.toroidalworld.engine.noise.PeriodicNoiseSampler;
import com.toroidalworld.shape.climate.ClimateCompression;

import it.unimi.dsi.fastutil.doubles.DoubleList;
import net.minecraft.core.QuartPos;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.synth.ImprovedNoise;
import net.minecraft.world.level.levelgen.synth.PerlinNoise;

public final class ReplacementNoiseFold {
    public static final double NOT_FOLDED = Double.NaN;

    private static final double OPEN_SIMPLEX_FLAT_AMPLITUDE = 1.971;

    private static final double OPEN_SIMPLEX_VOLUME_AMPLITUDE = 1.489;

    private static final double OPEN_SIMPLEX_ROW_RATE = 0.667;

    private static final double OPEN_SIMPLEX_COLUMN_RATE = 0.707;

    private static final int FLAT_QUART_Y = 0;

    private static final double NO_Y_SCALE = 0.0;

    private static final double NO_FUDGE = 0.0;

    private static final double BLOCKS_PER_QUART = 4.0;

    private final double[] weights;

    private final int[] horizontalScaleSlots;

    private final int[] verticalScaleSlots;

    private volatile @Nullable Octaves octaves;

    public ReplacementNoiseFold(double... weights) {
        this(weights, IntStream.range(0, weights.length).toArray(), IntStream.range(0, weights.length).toArray());
    }

    public ReplacementNoiseFold(double[] weights, int[] horizontalScaleSlots, int[] verticalScaleSlots) {
        this.weights = weights.clone();
        this.horizontalScaleSlots = horizontalScaleSlots.clone();
        this.verticalScaleSlots = verticalScaleSlots.clone();
    }

    public static boolean folded(double sum) {
        return !Double.isNaN(sum);
    }

    public double sum(long seed, double[] scaleQuarts, int quartX, int quartZ, DoubleList climateAmplitudes,
            double lowestFreqInputFactor) {
        return sum(seed, scaleQuarts, quartX, FLAT_QUART_Y, quartZ, OPEN_SIMPLEX_FLAT_AMPLITUDE, climateAmplitudes,
                lowestFreqInputFactor);
    }

    public double sum(long seed, double[] scaleQuarts, int quartX, int quartY, int quartZ, DoubleList climateAmplitudes,
            double lowestFreqInputFactor) {
        return sum(seed, scaleQuarts, quartX, quartY, quartZ, OPEN_SIMPLEX_VOLUME_AMPLITUDE, climateAmplitudes,
                lowestFreqInputFactor);
    }

    private double sum(long seed, double[] scaleQuarts, int quartX, int quartY, int quartZ, double amplitude,
            DoubleList climateAmplitudes, double lowestFreqInputFactor) {
        Context context = GenerationTransformerContext.context();
        WorldFold fold = context.wrappedTransformer();
        if (fold == null) {
            return NOT_FOLDED;
        }

        double factor = ClimateCompression.factor(fold, true, climateAmplitudes, lowestFreqInputFactor, 1.0, 0.0);
        Octaves resolved = octavesFor(seed);
        double x = QuartPos.toBlock(quartX);
        double y = QuartPos.toBlock(quartY);
        double z = QuartPos.toBlock(quartZ);
        double sum = 0.0;
        for (int octave = 0; octave < this.weights.length; octave++) {
            ImprovedNoise noise = resolved.noises()[octave];
            double horizontalQuarts = scaleQuarts[this.horizontalScaleSlots[octave]] * OPEN_SIMPLEX_ROW_RATE / factor;
            double scale = 1.0 / (horizontalQuarts * BLOCKS_PER_QUART);
            double verticalQuarts = scaleQuarts[this.verticalScaleSlots[octave]] * OPEN_SIMPLEX_COLUMN_RATE / factor;
            double verticalScale = 1.0 / (verticalQuarts * BLOCKS_PER_QUART);
            try (Context.ScaleScope _ = context.withScale(scale)) {
                sum += this.weights[octave] * PeriodicNoiseSampler.sample(noise.p, noise.xo, noise.yo, noise.zo,
                        fold, context, x, PerlinNoise.wrap(y * verticalScale), z, NO_Y_SCALE, NO_FUDGE);
            }
        }

        return sum * amplitude;
    }

    private Octaves octavesFor(long seed) {
        Octaves held = this.octaves;
        if (held != null && held.seed() == seed) {
            return held;
        }

        WorldgenRandom random = new WorldgenRandom(new LegacyRandomSource(seed));
        ImprovedNoise[] noises = new ImprovedNoise[this.weights.length];
        for (int octave = 0; octave < noises.length; octave++) {
            noises[octave] = new ImprovedNoise(random);
        }

        Octaves built = new Octaves(seed, noises);
        this.octaves = built;
        return built;
    }

    private record Octaves(long seed, ImprovedNoise[] noises) {
    }
}
