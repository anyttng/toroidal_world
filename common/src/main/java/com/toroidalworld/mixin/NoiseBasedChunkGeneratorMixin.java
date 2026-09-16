package com.toroidalworld.mixin;

import java.util.Set;

import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import com.toroidalworld.core.ShapedChunkGenerator;
import com.toroidalworld.engine.noise.GenerationTransformerContext;
import com.toroidalworld.engine.noise.TerrainCeiling;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;

import net.minecraft.core.Holder;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseChunk;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.densityfunction.DensityFunction;
import net.minecraft.world.level.levelgen.material.rule.MaterialRule;

@Mixin(NoiseBasedChunkGenerator.class)
public class NoiseBasedChunkGeneratorMixin {
    @Unique
    private volatile @Nullable DensityFunction toroidal$ceilingedFinalDensity;

    @ModifyExpressionValue(
            method = {"doFill", "iterateNoiseColumn"},
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/levelgen/NoiseRouter;finalDensity()"
                            + "Lnet/minecraft/world/level/levelgen/densityfunction/DensityFunction;"))
    private DensityFunction toroidal$fillUnderTheCeiling(DensityFunction finalDensity) {
        NoiseBasedChunkGenerator generator = (NoiseBasedChunkGenerator) (Object) this;
        if (ShapedChunkGenerator.wrappedTransformerOf(generator) == null) {
            return finalDensity;
        }

        DensityFunction ceilinged = this.toroidal$ceilingedFinalDensity;
        if (ceilinged == null) {
            ceilinged = TerrainCeiling.finalDensity(generator.generatorSettings().value().noiseRouter());
            this.toroidal$ceilingedFinalDensity = ceilinged;
        }

        return ceilinged;
    }

    @WrapMethod(method = "doFill")
    private void toroidal$bindWhileFilling(NoiseChunk noiseChunk, ChunkAccess chunk, Operation<Void> original) {
        GenerationTransformerContext.runWithTransformer(
                ShapedChunkGenerator.transformerOf((NoiseBasedChunkGenerator) (Object) this),
                () -> original.call(noiseChunk, chunk));
    }

    @WrapMethod(method = "buildSurface")
    private void toroidal$bindWhileBuildingSurface(ChunkAccess chunk, NoiseChunk noiseChunk, RandomState randomState,
            BiomeManager biomeManager, Set<Holder<Biome>> possibleBiomes, MaterialRule materialRule,
            Operation<Void> original) {
        GenerationTransformerContext.runWithTransformer(
                ShapedChunkGenerator.transformerOf((NoiseBasedChunkGenerator) (Object) this),
                () -> original.call(chunk, noiseChunk, randomState, biomeManager, possibleBiomes, materialRule));
    }

    @WrapMethod(method = "generateCarvers")
    private void toroidal$bindWhileCarving(ChunkAccess chunk, Blender blender, NoiseChunk noiseChunk,
            RandomState randomState, BiomeManager biomeManager, WorldGenRegion carverBiomeRegion,
            MaterialRule materialRule, Operation<Void> original) {
        GenerationTransformerContext.runWithTransformer(
                ShapedChunkGenerator.transformerOf((NoiseBasedChunkGenerator) (Object) this),
                () -> original.call(chunk, blender, noiseChunk, randomState, biomeManager, carverBiomeRegion,
                        materialRule));
    }

    @ModifyReturnValue(method = "getOrigin", at = @At("RETURN"))
    private ChunkPos toroidal$originInsideBounds(ChunkPos origin) {
        return ShapedChunkGenerator.transformerOf((NoiseBasedChunkGenerator) (Object) this).fold(origin);
    }
}
