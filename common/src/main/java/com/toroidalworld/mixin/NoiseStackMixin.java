package com.toroidalworld.mixin;

import org.spongepowered.asm.mixin.Mixin;

import com.toroidalworld.core.WorldFold;
import com.toroidalworld.engine.noise.ContextScaledNoise;
import com.toroidalworld.engine.noise.GenerationTransformerContext;
import com.toroidalworld.engine.noise.NoiseConstants;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;

import net.minecraft.world.level.levelgen.synth.Noise;
import net.minecraft.world.level.levelgen.synth.NoiseStack;

@Mixin(value = NoiseStack.class, targets = {
        "net.minecraft.world.level.levelgen.synth.NoiseStack$Perlin",
        "net.minecraft.world.level.levelgen.synth.NoiseStack$SmearedPerlin"})
public class NoiseStackMixin {
    @WrapMethod(method = "get(DDD)F")
    private float toroidal$periodicValue(double x, double y, double z, Operation<Float> original) {
        WorldFold transformer = GenerationTransformerContext.context().wrappedTransformer();
        if (transformer == null) {
            return original.call(x, y, z);
        }

        return ContextScaledNoise.sample(transformer, (Noise) (Object) this, NoiseConstants.UNSCALED, x, y, z);
    }
}
