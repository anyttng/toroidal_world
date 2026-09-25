package com.toroidalworld.compat.electroenergetics.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.george_vi.electroenergetics.content.wire_spool.WireApplyingBehaviour;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNode;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.toroidalworld.InjectionTargets;
import com.toroidalworld.client.engine.ClientFrame;
import com.toroidalworld.compat.electroenergetics.WireNodes;

@Mixin(value = WireApplyingBehaviour.class, remap = false)
public abstract class SelectedNodeReadMixin {
    @ModifyExpressionValue(method = "tick", at = @At(value = "INVOKE", target = InjectionTargets.ITEM_STACK_GET))
    private static Object toroidal$seatSelectedNode(Object stored) {
        return stored instanceof InWorldNode node ? WireNodes.moved(node, ClientFrame::nearestToPlayer) : stored;
    }
}
