package com.toroidalworld.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import com.toroidalworld.core.WorldFold;
import com.toroidalworld.engine.noise.ContextScaledNoise;
import com.toroidalworld.engine.noise.GenerationTransformerContext;
import com.toroidalworld.engine.noise.NoiseConstants;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;

import net.minecraft.world.level.levelgen.material.MaterialSystem;
import net.minecraft.world.level.levelgen.synth.Noise;

@Mixin(MaterialSystem.class)
public class MaterialSystemMixin {
    @Unique
    private static final String toroidal$NOISE_GET = "Lnet/minecraft/world/level/levelgen/synth/Noise;get(DDD)F";

    @Shadow
    @Final
    private Noise badlandsPillarNoise;

    @Shadow
    @Final
    private Noise badlandsPillarRoofNoise;

    @Shadow
    @Final
    private Noise icebergPillarNoise;

    @Shadow
    @Final
    private Noise icebergPillarRoofNoise;

    @WrapOperation(method = "erodedBadlandsExtension", at = @At(value = "INVOKE", target = toroidal$NOISE_GET))
    private float toroidal$rawBadlandsNoise(Noise noise, double x, double y, double z, Operation<Float> original,
            @Local(argsOnly = true, ordinal = 0) int blockX, @Local(argsOnly = true, ordinal = 1) int blockZ) {
        return this.toroidal$rawCoordinateNoise(noise, x, y, z, blockX, blockZ, original);
    }

    @WrapOperation(method = "frozenOceanExtension", at = @At(value = "INVOKE", target = toroidal$NOISE_GET))
    private float toroidal$rawIcebergNoise(Noise noise, double x, double y, double z, Operation<Float> original,
            @Local(argsOnly = true, ordinal = 1) int blockX, @Local(argsOnly = true, ordinal = 2) int blockZ) {
        return this.toroidal$rawCoordinateNoise(noise, x, y, z, blockX, blockZ, original);
    }

    @Unique
    private float toroidal$rawCoordinateNoise(Noise noise, double x, double y, double z, int blockX, int blockZ,
            Operation<Float> original) {
        double scale = this.toroidal$scaleOf(noise);
        WorldFold transformer = GenerationTransformerContext.context().wrappedTransformer();
        if (scale == NoiseConstants.UNSCALED || transformer == null) {
            return original.call(noise, x, y, z);
        }

        return ContextScaledNoise.sample(transformer, noise, scale, blockX, y, blockZ);
    }

    @Unique
    private double toroidal$scaleOf(Noise noise) {
        if (noise == this.badlandsPillarNoise) {
            return NoiseConstants.BADLANDS_PILLAR_SCALE;
        }

        if (noise == this.badlandsPillarRoofNoise) {
            return NoiseConstants.BADLANDS_PILLAR_ROOF_SCALE;
        }

        if (noise == this.icebergPillarNoise) {
            return NoiseConstants.ICEBERG_PILLAR_SCALE;
        }

        if (noise == this.icebergPillarRoofNoise) {
            return NoiseConstants.ICEBERG_PILLAR_ROOF_SCALE;
        }

        return NoiseConstants.UNSCALED;
    }
}
