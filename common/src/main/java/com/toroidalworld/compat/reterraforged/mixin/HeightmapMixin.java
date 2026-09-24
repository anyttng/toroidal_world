package com.toroidalworld.compat.reterraforged.mixin;

import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.toroidalworld.compat.reterraforged.LapCarrier;
import com.toroidalworld.compat.reterraforged.RtfLap;
import com.toroidalworld.core.WorldFold;

import net.minecraft.core.Direction;
import raccoonman.reterraforged.world.worldgen.cell.Cell;
import raccoonman.reterraforged.world.worldgen.cell.CellPopulator;
import raccoonman.reterraforged.world.worldgen.cell.heightmap.Heightmap;
import raccoonman.reterraforged.world.worldgen.cell.rivermap.Rivermap;

// The heightmap is the one surface every reader of ReTerraForged's field goes through, its tile workers included, so
// it binds the lap itself rather than trusting a thread that queued the work, and takes its position a whole lap over,
// so that every copy of a column enters the field as one number.
@Mixin(value = Heightmap.class, remap = false)
public abstract class HeightmapMixin implements LapCarrier {
    private static final int TERRAIN_POPULATOR_ORDINAL = 1;

    @Unique
    private volatile @Nullable WorldFold toroidal$fold;

    @Shadow
    @Final
    private float terrainFrequency;

    @Override
    public @Nullable WorldFold toroidal$fold() {
        return this.toroidal$fold;
    }

    @Override
    public void toroidal$carryFold(WorldFold fold) {
        this.toroidal$fold = fold;
    }

    @WrapMethod(method = "apply")
    private void toroidal$bindApply(Cell cell, float x, float z, boolean applyClimate, Operation<Void> original) {
        WorldFold fold = this.toroidal$fold;
        if (fold == null) {
            original.call(cell, x, z, applyClimate);
            return;
        }

        RtfLap.Frame frame = RtfLap.frame();
        try (RtfLap.Frame.Scope lap = frame.bind(fold)) {
            original.call(cell, frame.shift(Direction.Axis.X, x), frame.shift(Direction.Axis.Z, z), applyClimate);
        }
    }

    @WrapMethod(method = "applyTerrain")
    private void toroidal$bindTerrain(Cell cell, float x, float z, Operation<Void> original) {
        WorldFold fold = this.toroidal$fold;
        if (fold == null) {
            original.call(cell, x, z);
            return;
        }

        RtfLap.Frame frame = RtfLap.frame();
        try (RtfLap.Frame.Scope lap = frame.bind(fold)) {
            original.call(cell, frame.shift(Direction.Axis.X, x), frame.shift(Direction.Axis.Z, z));
        }
    }

    @WrapMethod(method = "applyRivers")
    private void toroidal$bindRivers(Cell cell, float x, float z, Rivermap rivermap, Operation<Void> original) {
        WorldFold fold = this.toroidal$fold;
        if (fold == null) {
            original.call(cell, x, z, rivermap);
            return;
        }

        RtfLap.Frame frame = RtfLap.frame();
        try (RtfLap.Frame.Scope lap = frame.bind(fold)) {
            original.call(cell, frame.shift(Direction.Axis.X, x), frame.shift(Direction.Axis.Z, z), rivermap);
        }
    }

    @WrapMethod(method = "applyClimate")
    private void toroidal$bindClimate(Cell cell, float x, float z, boolean applyClimate, Operation<Void> original) {
        WorldFold fold = this.toroidal$fold;
        if (fold == null) {
            original.call(cell, x, z, applyClimate);
            return;
        }

        RtfLap.Frame frame = RtfLap.frame();
        try (RtfLap.Frame.Scope lap = frame.bind(fold)) {
            original.call(cell, frame.shift(Direction.Axis.X, x), frame.shift(Direction.Axis.Z, z), applyClimate);
        }
    }

    // The terrain populators are handed the position times the global horizontal scale.
    @WrapOperation(method = "applyTerrain", at = @At(value = "INVOKE",
            target = "Lraccoonman/reterraforged/world/worldgen/cell/CellPopulator;apply"
                    + "(Lraccoonman/reterraforged/world/worldgen/cell/Cell;FF)V",
            ordinal = TERRAIN_POPULATOR_ORDINAL))
    private void toroidal$scaleTerrainLap(CellPopulator populator, Cell cell, float x, float z,
            Operation<Void> original) {
        RtfLap.Frame frame = RtfLap.boundFrame();
        if (frame == null) {
            original.call(populator, cell, x, z);
            return;
        }

        try (RtfLap.Frame.Scope scaled = frame.scale(this.terrainFrequency, this.terrainFrequency)) {
            original.call(populator, cell, x, z);
        }
    }
}
