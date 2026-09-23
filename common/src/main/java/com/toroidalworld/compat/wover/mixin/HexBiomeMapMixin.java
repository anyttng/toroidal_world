package com.toroidalworld.compat.wover.mixin;

import org.betterx.wover.generator.api.biomesource.WoverBiomePicker;
import org.betterx.wover.generator.impl.map.hex.HexBiomeMap;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.toroidalworld.compat.wover.FoldCompression;
import com.toroidalworld.compat.wover.HexLapMap;
import com.toroidalworld.core.WorldFold;
import com.toroidalworld.engine.noise.GenerationTransformerContext;

@Mixin(HexBiomeMap.class)
public class HexBiomeMapMixin {
    @Shadow
    @Final
    private WoverBiomePicker picker;

    @Shadow
    @Final
    private float scale;

    @Shadow
    @Final
    private int seed;

    @Unique
    private volatile @Nullable HexLapMap<WoverBiomePicker.PickableBiome> toroidal$lapMap;

    @Inject(method = "getRawBiome", at = @At("HEAD"), cancellable = true)
    private void toroidal$lapBiome(double x, double z,
            CallbackInfoReturnable<WoverBiomePicker.PickableBiome> cir) {
        WorldFold transformer = GenerationTransformerContext.context().wrappedTransformer();
        if (transformer == null) {
            return;
        }

        HexLapMap<WoverBiomePicker.PickableBiome> lapMap = this.toroidal$lapMap;
        if (lapMap == null || !lapMap.covers(transformer)) {
            lapMap = new HexLapMap<>(transformer, this.scale, FoldCompression.of(transformer), this.seed,
                    new HexLapMap.Picker<>(this.picker::getBiome, WoverBiomePicker.PickableBiome::getSubBiome));
            this.toroidal$lapMap = lapMap;
        }

        cir.setReturnValue(lapMap.biomeAt(x, z));
    }
}
