package com.toroidalworld.compat.reterraforged.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.toroidalworld.InjectionTargets;
import com.toroidalworld.compat.reterraforged.ReTerraForgedInjectionTargets;
import com.toroidalworld.compat.reterraforged.RtfEntry;

import net.minecraft.core.Direction;
import net.minecraft.world.level.levelgen.DensityFunction;
import raccoonman.reterraforged.world.worldgen.densityfunction.CellSampler;

@Mixin(CellSampler.class)
public abstract class CellSamplerMixin {
    @WrapOperation(method = {ReTerraForgedInjectionTargets.COMPUTE, ReTerraForgedInjectionTargets.COMPUTE_INTERMEDIARY},
            at = @At(value = "INVOKE", target = InjectionTargets.FUNCTION_CONTEXT_BLOCK_X))
    private int toroidal$foldX(DensityFunction.FunctionContext context, Operation<Integer> original) {
        return RtfEntry.foldBlock(RtfEntry.generationFold(), Direction.Axis.X, original.call(context));
    }

    @WrapOperation(method = {ReTerraForgedInjectionTargets.COMPUTE, ReTerraForgedInjectionTargets.COMPUTE_INTERMEDIARY},
            at = @At(value = "INVOKE", target = InjectionTargets.FUNCTION_CONTEXT_BLOCK_Z))
    private int toroidal$foldZ(DensityFunction.FunctionContext context, Operation<Integer> original) {
        return RtfEntry.foldBlock(RtfEntry.generationFold(), Direction.Axis.Z, original.call(context));
    }
}
