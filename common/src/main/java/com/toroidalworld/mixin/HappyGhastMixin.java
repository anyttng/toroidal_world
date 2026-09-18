package com.toroidalworld.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.toroidalworld.InjectionTargets;
import com.toroidalworld.engine.seam.SeamSteering;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import net.minecraft.world.entity.animal.happyghast.HappyGhast;
import net.minecraft.world.phys.Vec3;

@Mixin(HappyGhast.class)
public class HappyGhastMixin {
    @ModifyExpressionValue(
            method = "scanPlayerAboveGhast",
            at = @At(value = "INVOKE", target = InjectionTargets.ENTITY_POSITION))
    private Vec3 toroidal$riderPositionThroughSeam(Vec3 position) {
        return SeamSteering.nearestCopy((HappyGhast) (Object) this, position);
    }
}
