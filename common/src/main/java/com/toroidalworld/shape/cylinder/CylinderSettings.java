package com.toroidalworld.shape.cylinder;

import java.util.List;

import com.toroidalworld.api.v1.option.GenerationOptions;
import com.toroidalworld.api.v1.option.WorldOption;
import com.toroidalworld.api.v1.shape.LoopSpans;
import com.toroidalworld.core.NetherScales;
import com.toroidalworld.core.WorldLoopSizes;
import com.toroidalworld.shape.climate.ClimateScale;
import com.toroidalworld.shape.climate.CompactBiomes;

import net.minecraft.core.Direction;

public record CylinderSettings(LoopSpans overworld, int netherScale, LoopSpans end,
        GenerationOptions generationOptions) {
    private static final Direction.Axis DEFAULT_AXIS = Direction.Axis.X;

    public static final List<WorldOption<?>> OFFERED_OPTIONS = List.of(CompactBiomes.OPTION);

    private static final GenerationOptions DEFAULT_GENERATION_OPTIONS =
            GenerationOptions.DEFAULT.with(CompactBiomes.OPTION, ClimateScale.OFF);

    public static final CylinderSettings DEFAULT = new CylinderSettings(
            LoopSpans.ofWidth(DEFAULT_AXIS, WorldLoopSizes.DEFAULT_CHUNK_WIDTH),
            NetherScales.DEFAULT,
            LoopSpans.ofWidth(DEFAULT_AXIS, WorldLoopSizes.END_DEFAULT_CHUNK_WIDTH),
            DEFAULT_GENERATION_OPTIONS);

    public CylinderSettings {
        if (!isCylinder(overworld)) {
            throw new IllegalArgumentException("A cylinder loops on exactly one axis, got " + overworld);
        }

        if (!isCylinder(end) || !end.loops(loopedAxis(overworld))) {
            throw new IllegalArgumentException("The End loops on the overworld axis " + loopedAxis(overworld)
                    + ", got " + end);
        }
    }

    public Direction.Axis axis() {
        return loopedAxis(overworld);
    }

    public int chunkWidth() {
        return overworld.chunkWidth(axis());
    }

    public int endChunkWidth() {
        return end.chunkWidth(axis());
    }

    public static boolean isCylinder(LoopSpans spans) {
        return spans.loops(Direction.Axis.X) != spans.loops(Direction.Axis.Z);
    }

    public static Direction.Axis loopedAxis(LoopSpans spans) {
        return spans.loops(Direction.Axis.X) ? Direction.Axis.X : Direction.Axis.Z;
    }
}
