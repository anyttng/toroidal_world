package com.toroidalworld.compat.electroenergetics.mixin;

import java.util.Map;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import com.george_vi.electroenergetics.content.wire.WireSync;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.toroidalworld.InjectionTargets;
import com.toroidalworld.accessors.TransformerHolder;
import com.toroidalworld.compat.electroenergetics.WireSpan;
import com.toroidalworld.core.WorldLoopAttachments;

import net.minecraft.server.level.ServerLevel;

@Mixin(value = WireSync.class, remap = false)
public abstract class WireSyncMixin {
    @Shadow
    @Final
    private ServerLevel level;

    @WrapOperation(method = "handlePlayerEnterNewSection",
            at = @At(value = "INVOKE", target = InjectionTargets.MAP_PUT))
    private Object toroidal$bindBoxFold(Map<Object, Object> boxes, Object player, Object box,
            Operation<Object> original) {
        ((TransformerHolder) box).toroidal$setTransformer(WorldLoopAttachments.transformerOf(this.level));
        return original.call(boxes, player, box);
    }

    @ModifyExpressionValue(
            method = {"handleWireRemoved", "handleWireAdded", "handleNodeLabelRename", "handleNodeCreate",
                    "handleNodeRemove"},
            at = @At(value = "INVOKE", target = InjectionTargets.CHUNK_POS_AS_LONG))
    private long toroidal$canonicalChunk(long chunk) {
        return WireSpan.chunkKey(this.level, chunk);
    }

    @ModifyExpressionValue(
            method = {"handleCatenaryRemoved", "handleCatenaryAdded", "handleWireRepositioned", "handleNodeMoved",
                    "lambda$handlePlayerEnterNewSection$4"},
            at = @At(value = "INVOKE", target = InjectionTargets.CHUNK_POS_AS_LONG_BLOCK))
    private long toroidal$canonicalBlockChunk(long chunk) {
        return WireSpan.chunkKey(this.level, chunk);
    }
}
