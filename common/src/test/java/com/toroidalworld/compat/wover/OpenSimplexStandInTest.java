package com.toroidalworld.compat.wover;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;
import java.util.function.DoubleUnaryOperator;

import org.betterx.wover.math.api.noise.OpenSimplexNoise;
import org.junit.jupiter.api.Test;

import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.synth.ImprovedNoise;

class OpenSimplexStandInTest {
    private static final long SEED = 0x900L;

    private static final int FIELDS = 16;

    private static final int LINES = 32;

    private static final int LINE_SAMPLES = 20000;

    private static final double STEP = 0.01;

    private static final double LINE_SPACING = 3.7;

    private static final double ORIGIN_SPREAD = 1.0E4;

    private static final double TOLERANCE = 0.03;

    private static final double PERIOD_TOLERANCE = 1.0E-9;

    private static final double ROW_MIDDLE = 0.5;

    private static final double FLIP_STEP = 0.05;

    @Test
    void theStandInSpreadsAndCrossesZeroLikeWorldWeaversOpenSimplex() {
        Random random = new Random(SEED);
        Moments openSimplex = new Moments();
        Moments standIn = new Moments();
        for (int field = 0; field < FIELDS; field++) {
            OpenSimplexNoise reference = new OpenSimplexNoise(random.nextLong());
            OpenSimplexStandIn candidate = new OpenSimplexStandIn(random.nextLong());
            double originX = random.nextDouble() * ORIGIN_SPREAD;
            double originZ = random.nextDouble() * ORIGIN_SPREAD;
            for (int line = 0; line < LINES; line++) {
                double z = originZ + line * LINE_SPACING;
                openSimplex.line(x -> reference.eval(x, z), originX);
                standIn.line(x -> candidate.eval(x, z, OpenSimplexStandIn.UNBOUNDED, OpenSimplexStandIn.UNBOUNDED),
                        originX);
            }
        }

        assertEquals(1.0, standIn.deviation() / openSimplex.deviation(), TOLERANCE,
                "spread: stand-in " + standIn.deviation() + " against OpenSimplex " + openSimplex.deviation());
        assertEquals(1.0, standIn.crossingsPerUnit() / openSimplex.crossingsPerUnit(), TOLERANCE,
                "zero crossings per unit: stand-in " + standIn.crossingsPerUnit() + " against OpenSimplex "
                        + openSimplex.crossingsPerUnit());
    }

    @Test
    void theZeroSetCrossesALatticeRowNoMoreOftenThanALineBetweenRows() {
        Random random = new Random(SEED);
        long rowFlips = 0;
        long betweenFlips = 0;
        long pairs = 0;
        for (int field = 0; field < FIELDS; field++) {
            long seed = random.nextLong();
            OpenSimplexStandIn noise = new OpenSimplexStandIn(seed);
            double zo = new ImprovedNoise(new WorldgenRandom(new LegacyRandomSource(seed))).zo;
            for (int row = 0; row < LINES; row++) {
                double onRow = (row - zo) / OpenSimplexStandIn.RATE;
                double betweenRows = (row + ROW_MIDDLE - zo) / OpenSimplexStandIn.RATE;
                for (int sample = 0; sample < LINE_SAMPLES; sample++) {
                    double x = sample * FLIP_STEP;
                    pairs++;
                    rowFlips += flips(noise, x, onRow) ? 1 : 0;
                    betweenFlips += flips(noise, x, betweenRows) ? 1 : 0;
                }
            }
        }

        double ratio = (double) rowFlips / betweenFlips;
        assertTrue(ratio <= 1.0, "the zero set crosses a lattice row on "
                + rowFlips + " of " + pairs + " steps and a line between rows on " + betweenFlips + ": ratio " + ratio);
    }

    private static boolean flips(OpenSimplexStandIn noise, double x, double z) {
        double before = noise.eval(x, z - FLIP_STEP, OpenSimplexStandIn.UNBOUNDED, OpenSimplexStandIn.UNBOUNDED);
        double after = noise.eval(x, z + FLIP_STEP, OpenSimplexStandIn.UNBOUNDED, OpenSimplexStandIn.UNBOUNDED);
        return (before > 0.0) != (after > 0.0);
    }

    @Test
    void aClosedPeriodRepeatsOnBothAxes() {
        OpenSimplexStandIn noise = new OpenSimplexStandIn(SEED);
        double[] periods = {1.0, 7.0, 8.0, 21.0, 110.0};
        for (double xPeriod : periods) {
            for (double zPeriod : periods) {
                for (double x = -3.0; x < 3.0; x += 0.37) {
                    for (double z = -3.0; z < 3.0; z += 0.41) {
                        double here = noise.eval(x, z, xPeriod, zPeriod);
                        assertEquals(here, noise.eval(x + xPeriod, z, xPeriod, zPeriod), PERIOD_TOLERANCE,
                                "x period " + xPeriod + " at " + x + ", " + z);
                        assertEquals(here, noise.eval(x, z + zPeriod, xPeriod, zPeriod), PERIOD_TOLERANCE,
                                "z period " + zPeriod + " at " + x + ", " + z);
                    }
                }
            }
        }
    }

    @Test
    void aClosedPeriodNeverWidensTheFeature() {
        double[] periods = {0.5, 1.0, 2.5, 8.0, 110.0};
        for (double period : periods) {
            long cells = OpenSimplexStandIn.cells(period);
            assertTrue(cells >= period * OpenSimplexStandIn.RATE - PERIOD_TOLERANCE,
                    cells + " lattice cells over a period of " + period);
        }
    }

    private static final class Moments {
        private double squares;

        private long samples;

        private long crossings;

        private double length;

        void line(DoubleUnaryOperator field, double originX) {
            double previous = field.applyAsDouble(originX);
            for (int sample = 1; sample < LINE_SAMPLES; sample++) {
                double value = field.applyAsDouble(originX + sample * STEP);
                this.squares += value * value;
                this.samples++;
                if ((value > 0.0) != (previous > 0.0)) {
                    this.crossings++;
                }

                previous = value;
            }

            this.length += LINE_SAMPLES * STEP;
        }

        double deviation() {
            return Math.sqrt(this.squares / this.samples);
        }

        double crossingsPerUnit() {
            return this.crossings / this.length;
        }
    }
}
