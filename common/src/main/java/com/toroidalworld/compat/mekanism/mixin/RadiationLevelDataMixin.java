package com.toroidalworld.compat.mekanism.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.toroidalworld.compat.mekanism.RadiationIndexFrame;
import com.toroidalworld.compat.mekanism.RadiationLevelFrame;
import com.toroidalworld.core.WorldFold;

import mekanism.common.lib.collection.IndexedCuboidMap;
import mekanism.common.lib.radiation.RadiationLevelData;
import mekanism.common.lib.radiation.RadiationSource;

import net.minecraft.core.BlockPos;

@Mixin(value = RadiationLevelData.class, remap = false)
public class RadiationLevelDataMixin implements RadiationLevelFrame {
    @Shadow
    @Final
    private IndexedCuboidMap<RadiationSource> sources;

    @Override
    public void toroidal$bind(WorldFold fold) {
        ((RadiationIndexFrame) this.sources).toroidal$bind(fold);
    }

    @WrapOperation(method = "getRadiationLevelAndMaxMagnitude",
            at = @At(value = "INVOKE", target = "Lmekanism/common/lib/radiation/RadiationUtil;computeExposure("
                    + "Lmekanism/common/lib/radiation/RadiationSource;Lnet/minecraft/core/BlockPos;)D"))
    private double toroidal$exposureFromNearestCopy(RadiationSource source, BlockPos checkPos,
            Operation<Double> original) {
        WorldFold fold = ((RadiationIndexFrame) this.sources).toroidal$fold();
        return original.call(source, fold.nearestCopy(source.getPosition(), checkPos));
    }
}
