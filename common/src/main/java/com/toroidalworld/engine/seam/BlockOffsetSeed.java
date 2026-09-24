package com.toroidalworld.engine.seam;

import org.jspecify.annotations.Nullable;

import com.toroidalworld.accessors.RegionLevelSource;
import com.toroidalworld.core.WorldFold;
import com.toroidalworld.core.WorldFolds;
import com.toroidalworld.core.WorldLoopAttachments;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;

public final class BlockOffsetSeed {
    public static BlockPos seedPosition(@Nullable BlockGetter getter, BlockPos pos) {
        return foldOf(getter).fold(pos);
    }

    private static WorldFold foldOf(@Nullable BlockGetter getter) {
        if (getter instanceof LevelReader reader) {
            return WorldLoopAttachments.transformerOfReader(reader);
        }

        if (getter instanceof RegionLevelSource region) {
            return WorldLoopAttachments.transformerOfReader(region.toroidal$regionLevel());
        }

        return WorldFolds.NOOP;
    }

    private BlockOffsetSeed() {
    }
}
