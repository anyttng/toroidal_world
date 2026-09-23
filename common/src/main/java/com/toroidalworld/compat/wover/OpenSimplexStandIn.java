package com.toroidalworld.compat.wover;

import com.toroidalworld.engine.noise.PeriodicNoiseSampler;

import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.synth.ImprovedNoise;

public final class OpenSimplexStandIn {
    public static final double UNBOUNDED = 0.0;

    static final double AMPLITUDE = 1.252;

    static final double RATE = 1.065;

    private static final double BETWEEN_PLANES = 0.5;

    private static final long MIN_CELLS = 1L;

    private static final double CELL_EPSILON = 1.0E-9;

    private final ImprovedNoise noise;

    public OpenSimplexStandIn(long seed) {
        this.noise = new ImprovedNoise(new WorldgenRandom(new LegacyRandomSource(seed)));
    }

    public double eval(double x, double z, double xPeriod, double zPeriod) {
        long xCells = cells(xPeriod);
        long zCells = cells(zPeriod);
        return AMPLITUDE * PeriodicNoiseSampler.sampleLattice(this.noise.p,
                lattice(x, xPeriod, xCells) + this.noise.xo, Math.floor(this.noise.yo) + BETWEEN_PLANES,
                lattice(z, zPeriod, zCells) + this.noise.zo, xCells, zCells);
    }

    static long cells(double period) {
        return period == UNBOUNDED
                ? PeriodicNoiseSampler.UNBOUNDED_PERIOD
                : Math.max(MIN_CELLS, (long) Math.ceil(period * RATE - CELL_EPSILON));
    }

    private static double lattice(double coord, double period, long cells) {
        return period == UNBOUNDED ? coord * RATE : coord * (cells / period);
    }
}
