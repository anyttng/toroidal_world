package com.toroidalworld.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.toroidalworld.InjectionTargets;
import com.toroidalworld.engine.seam.SeamRange;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.entity.animal.nautilus.AbstractNautilus;

@Mixin(AbstractNautilus.class)
public class AbstractNautilusMixin {
    @WrapOperation(
            method = "checkRestriction",
            at = @At(value = "INVOKE", target = InjectionTargets.BLOCK_POS_CLOSER_THAN))
    private boolean toroidal$homeRestrictionThroughSeam(BlockPos home, Vec3i pos, double distance,
            Operation<Boolean> original) {
        return SeamRange.closerThan((AbstractNautilus) (Object) this, home, pos, distance);
    }
}
