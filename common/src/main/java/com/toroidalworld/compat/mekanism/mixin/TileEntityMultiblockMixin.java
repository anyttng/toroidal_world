package com.toroidalworld.compat.mekanism.mixin;

import java.util.Set;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.toroidalworld.InjectionTargets;
import com.toroidalworld.compat.mekanism.MultiblockFrames;

import mekanism.common.lib.multiblock.MultiblockData;
import mekanism.common.tile.prefab.TileEntityMultiblock;

@Mixin(value = TileEntityMultiblock.class, remap = false)
public class TileEntityMultiblockMixin {
    @WrapOperation(method = "onNeighborChange", at = @At(value = "INVOKE", target = InjectionTargets.SET_CONTAINS))
    private boolean toroidal$internalInFrame(Set<?> internalLocations, Object neighborPos, Operation<Boolean> original,
            @Local MultiblockData multiblock) {
        return original.call(internalLocations, MultiblockFrames.seatOnto((Object) multiblock, neighborPos));
    }
}
