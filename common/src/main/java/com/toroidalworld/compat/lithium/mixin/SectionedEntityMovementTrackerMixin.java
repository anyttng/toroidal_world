package com.toroidalworld.compat.lithium.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.toroidalworld.core.WorldLoopAttachments;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;

import net.minecraft.server.level.ServerLevel;

@Mixin(targets = "net.caffeinemc.mods.lithium.common.tracking.entity.SectionedEntityMovementTracker")
public class SectionedEntityMovementTrackerMixin {
    @WrapOperation(
            method = "register(Lnet/minecraft/server/level/ServerLevel;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/core/SectionPos;asLong(III)J"))
    private long toroidal$subscribePhysicalSection(int x, int y, int z, Operation<Long> original,
            @Local(argsOnly = true) ServerLevel level) {
        return WorldLoopAttachments.transformerOf(level).foldSectionNode(original.call(x, y, z));
    }
}
