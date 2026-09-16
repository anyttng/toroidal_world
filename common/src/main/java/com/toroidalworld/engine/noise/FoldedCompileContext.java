package com.toroidalworld.engine.noise;

import com.toroidalworld.accessors.CoastLiftCache;
import com.toroidalworld.core.WorldFold;

import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.densityfunction.DensityFunction;
import net.minecraft.world.level.levelgen.synth.Noise;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public record FoldedCompileContext(DensityFunction.CompileContext vanilla, WorldFold fold, CoastLiftCache coastLift,
        double xDivisor, double zDivisor) implements DensityFunction.CompileContext {
    public FoldedCompileContext(DensityFunction.CompileContext vanilla, WorldFold fold, CoastLiftCache coastLift) {
        this(vanilla, fold, coastLift, NoiseConstants.UNDIVIDED, NoiseConstants.UNDIVIDED);
    }

    public FoldedCompileContext withDivisors(double xCellWidth, double zCellWidth) {
        return new FoldedCompileContext(this.vanilla, this.fold, this.coastLift, xCellWidth, zCellWidth);
    }

    @Override
    public Noise createNoiseSampler(Holder<NormalNoise> parameters) {
        return this.vanilla.createNoiseSampler(parameters);
    }

    @Override
    public RandomSource createRandom(Identifier seed) {
        return this.vanilla.createRandom(seed);
    }

    @Override
    @Deprecated
    @SuppressWarnings("deprecation")
    public RandomSource createEndIslandRandom() {
        return this.vanilla.createEndIslandRandom();
    }
}
