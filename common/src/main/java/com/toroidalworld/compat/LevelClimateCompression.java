package com.toroidalworld.compat;

import org.jspecify.annotations.Nullable;

import com.toroidalworld.core.WorldFold;
import com.toroidalworld.engine.noise.ClimateScaleCompression;
import com.toroidalworld.engine.noise.GenerationTransformerContext;
import com.toroidalworld.shape.climate.ClimateCompression;
import com.toroidalworld.shape.noise.DensityNoises;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;

public final class LevelClimateCompression {
    public static DensityFunction temperatureOf(@Nullable ServerLevel level) {
        return temperatureOf(routerTemperatureOf(level), overworldTemperatureOf(level));
    }

    static DensityFunction temperatureOf(DensityFunction own, DensityFunction overworld) {
        return ClimateCompression.climateNoiseOf(own) != null ? own : overworld;
    }

    static DensityFunction overworldTemperatureOf(@Nullable ServerLevel level) {
        return routerTemperatureOf(level != null ? level.getServer().overworld() : null);
    }

    static DensityFunction routerTemperatureOf(@Nullable ServerLevel level) {
        if (level != null && level.getChunkSource().getGenerator() instanceof NoiseBasedChunkGenerator noise) {
            return noise.generatorSettings().value().noiseRouter().temperature();
        }

        return DensityFunctions.zero();
    }

    public static double factor(WorldFold fold, DensityFunction temperature) {
        DensityNoises.ScaledNoise climate = ClimateCompression.climateNoiseOf(temperature);
        return climate == null
                ? ClimateScaleCompression.NO_COMPRESSION
                : ClimateCompression.factor(fold, climate.noise(), climate.xzScale(),
                        GenerationTransformerContext.verticalShare(climate.xzScale(), climate.yScale()));
    }

    private LevelClimateCompression() {
    }
}
