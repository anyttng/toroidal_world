package com.toroidalworld.compat.electroenergetics.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.toroidalworld.accessors.TransformerHolder;
import com.toroidalworld.compat.electroenergetics.WireChunkKeys;
import com.toroidalworld.core.WorldFold;
import com.toroidalworld.core.WorldFolds;

import it.unimi.dsi.fastutil.longs.LongIterator;
import net.minecraft.world.level.ChunkPos;

@Mixin(targets = "com.george_vi.electroenergetics.content.wire.WireSync$ChunkCuboidEntry", remap = false)
public abstract class ChunkCuboidEntryMixin implements TransformerHolder {
    @Shadow
    public int minX;

    @Shadow
    public int maxX;

    @Shadow
    public int minZ;

    @Shadow
    public int maxZ;

    @Unique
    private WorldFold toroidal$transformer = WorldFolds.NOOP;

    @Override
    public WorldFold toroidal$transformer() {
        return this.toroidal$transformer;
    }

    @Override
    public void toroidal$setTransformer(WorldFold transformer) {
        this.toroidal$transformer = transformer;
    }

    @WrapMethod(method = "includes(II)Z")
    private boolean toroidal$includesNearestCopy(int x, int z, Operation<Boolean> original) {
        ChunkPos centre = new ChunkPos(Math.floorDiv(this.minX + this.maxX - 1, 2),
                Math.floorDiv(this.minZ + this.maxZ - 1, 2));
        ChunkPos seated = this.toroidal$transformer.nearestCopy(centre, new ChunkPos(x, z));
        return original.call(seated.x, seated.z);
    }

    @ModifyReturnValue(method = "iterator()Lit/unimi/dsi/fastutil/longs/LongIterator;", at = @At("RETURN"))
    private LongIterator toroidal$foldedChunks(LongIterator raw) {
        return WireChunkKeys.of(raw, this.toroidal$transformer);
    }
}
