package com.toroidalworld.compat.wover;

import com.toroidalworld.compat.LevelClimateCompression;
import com.toroidalworld.core.ShapedChunkGenerator;
import com.toroidalworld.core.WorldFold;
import com.toroidalworld.engine.level.CurrentServer;
import com.toroidalworld.engine.noise.ClimateScaleCompression;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;

public final class FoldCompression {
    public static double of(WorldFold fold) {
        MinecraftServer server = CurrentServer.get();
        if (server != null) {
            for (ServerLevel level : server.getAllLevels()) {
                if (ShapedChunkGenerator.transformerOf(level.getChunkSource().getGenerator()) == fold) {
                    return LevelClimateCompression.factor(fold, LevelClimateCompression.temperatureOf(level));
                }
            }
        }

        return ClimateScaleCompression.NO_COMPRESSION;
    }

    private FoldCompression() {
    }
}
