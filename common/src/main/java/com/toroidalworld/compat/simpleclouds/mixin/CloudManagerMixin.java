package com.toroidalworld.compat.simpleclouds.mixin;

import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.toroidalworld.core.WorldLoopAttachments;
import com.toroidalworld.engine.noise.GenerationTransformerContext;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

@Mixin(targets = "dev.nonamecrackers2.simpleclouds.common.world.CloudManager", remap = false)
public abstract class CloudManagerMixin {
    @Shadow
    @Final
    protected Level level;

    @WrapMethod(method = "getPrecipitationAt")
    private Pair<?, ?> toroidal$bindPrecipitationTransformer(BlockPos pos, Operation<Pair<?, ?>> original) {
        return GenerationTransformerContext.withTransformer(
                WorldLoopAttachments.noiseTransformerOf(this.level), () -> original.call(pos));
    }
}
