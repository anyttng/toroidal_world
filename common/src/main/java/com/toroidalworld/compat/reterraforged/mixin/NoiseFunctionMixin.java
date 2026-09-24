package com.toroidalworld.compat.reterraforged.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.toroidalworld.compat.reterraforged.ReTerraForgedInjectionTargets;
import com.toroidalworld.compat.reterraforged.RtfEntry;
import com.toroidalworld.compat.reterraforged.RtfLap;
import com.toroidalworld.core.WorldFold;

import net.minecraft.core.Direction;
import raccoonman.reterraforged.world.worldgen.densityfunction.NoiseFunction;
import raccoonman.reterraforged.world.worldgen.noise.module.Noise;

@Mixin(NoiseFunction.class)
public abstract class NoiseFunctionMixin {
    @WrapOperation(method = {ReTerraForgedInjectionTargets.COMPUTE, ReTerraForgedInjectionTargets.COMPUTE_INTERMEDIARY},
            at = @At(value = "INVOKE", target = ReTerraForgedInjectionTargets.NOISE_COMPUTE, remap = false))
    private float toroidal$bindLap(Noise noise, float x, float z, int seed, Operation<Float> original) {
        WorldFold fold = RtfEntry.generationFold();
        if (fold == null) {
            return original.call(noise, x, z, seed);
        }

        try (RtfLap.Frame.Scope lap = RtfLap.frame().bind(fold)) {
            return original.call(noise, RtfEntry.foldBlock(fold, Direction.Axis.X, x),
                    RtfEntry.foldBlock(fold, Direction.Axis.Z, z), seed);
        }
    }
}
