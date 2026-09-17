package com.toroidalworld.compat.mekanism;

import org.jspecify.annotations.Nullable;

import com.toroidalworld.core.WorldLoopAttachments;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public final class MekanismSeam {
    private static final long NO_POSITION = Long.MAX_VALUE;

    public static long fold(@Nullable Level level, long pos) {
        return pos == NO_POSITION ? pos : fold(level, BlockPos.of(pos)).asLong();
    }

    public static BlockPos fold(@Nullable Level level, BlockPos pos) {
        return WorldLoopAttachments.transformerOfReader(level).fold(pos);
    }

    public static long nearestCopy(@Nullable Level level, long anchor, long target) {
        if (anchor == NO_POSITION || target == NO_POSITION) {
            return target;
        }

        return nearestCopy(level, BlockPos.of(anchor), BlockPos.of(target)).asLong();
    }

    public static BlockPos nearestCopy(@Nullable Level level, BlockPos anchor, BlockPos target) {
        return WorldLoopAttachments.transformerOfReader(level).nearestCopy(anchor, target);
    }

    private MekanismSeam() {
    }
}
