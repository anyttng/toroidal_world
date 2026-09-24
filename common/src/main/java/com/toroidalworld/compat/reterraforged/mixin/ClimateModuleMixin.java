package com.toroidalworld.compat.reterraforged.mixin;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.toroidalworld.compat.reterraforged.ReTerraForgedInjectionTargets;
import com.toroidalworld.compat.reterraforged.RtfLap;

import net.minecraft.core.Direction;
import raccoonman.reterraforged.world.worldgen.cell.Cell;
import raccoonman.reterraforged.world.worldgen.cell.climate.ClimateModule;
import raccoonman.reterraforged.world.worldgen.noise.module.Noise;

@Mixin(value = ClimateModule.class, remap = false)
public abstract class ClimateModuleMixin {
    private static final String APPLY = "apply(Lraccoonman/reterraforged/world/worldgen/cell/Cell;FFFFZ)V";

    private static final String BIOME_FREQUENCY =
            "Lraccoonman/reterraforged/world/worldgen/cell/climate/ClimateModule;biomeFreq:F";

    private static final int X_READ = 0;

    private static final int Z_READ = 1;

    private static final int CENTER_X_READ = 2;

    private static final int CENTER_Z_READ = 3;

    @Shadow
    private float biomeFreq;

    @Shadow
    @Final
    private Noise warpX;

    @Shadow
    @Final
    private Noise warpZ;

    @WrapMethod(method = APPLY)
    private void toroidal$closeApply(Cell cell, float x, float z, float originalX, float originalZ, boolean mask,
            Operation<Void> original) {
        RtfLap.Frame frame = RtfLap.boundFrame();
        if (frame == null) {
            original.call(cell, x, z, originalX, originalZ, mask);
            return;
        }

        try (RtfLap.Frame.Scope lattice = frame.octave(this.biomeFreq)) {
            original.call(cell, frame.shift(Direction.Axis.X, x), frame.shift(Direction.Axis.Z, z), originalX,
                    originalZ, mask);
        }
    }

    @ModifyExpressionValue(method = APPLY, at = {
            @At(value = "FIELD", target = BIOME_FREQUENCY, opcode = Opcodes.GETFIELD, ordinal = X_READ),
            @At(value = "FIELD", target = BIOME_FREQUENCY, opcode = Opcodes.GETFIELD, ordinal = CENTER_X_READ)})
    private float toroidal$snapX(float frequency) {
        RtfLap.Frame frame = RtfLap.boundFrame();
        return frame == null ? frequency : frame.snappedFrequency(Direction.Axis.X, frequency);
    }

    @ModifyExpressionValue(method = APPLY, at = {
            @At(value = "FIELD", target = BIOME_FREQUENCY, opcode = Opcodes.GETFIELD, ordinal = Z_READ),
            @At(value = "FIELD", target = BIOME_FREQUENCY, opcode = Opcodes.GETFIELD, ordinal = CENTER_Z_READ)})
    private float toroidal$snapZ(float frequency) {
        RtfLap.Frame frame = RtfLap.boundFrame();
        return frame == null ? frequency : frame.snappedFrequency(Direction.Axis.Z, frequency);
    }

    // Past the warps every climate noise is read at a lattice position, a biome cell's point or a region centre times
    // the biome frequency, so its lap is the lattice's: a cell reached across the seam P cells on reads what it reads
    // from the near side.
    @WrapOperation(method = APPLY, at = @At(value = "INVOKE", target = ReTerraForgedInjectionTargets.NOISE_COMPUTE))
    private float toroidal$latticeLap(Noise noise, float x, float z, int seed, Operation<Float> original) {
        RtfLap.Frame frame = RtfLap.boundFrame();
        if (frame == null || noise == this.warpX || noise == this.warpZ) {
            return original.call(noise, x, z, seed);
        }

        try (RtfLap.Frame.Scope scaled = frame.scale(frame.snappedFrequency(Direction.Axis.X, this.biomeFreq),
                frame.snappedFrequency(Direction.Axis.Z, this.biomeFreq))) {
            return original.call(noise, x, z, seed);
        }
    }
}
