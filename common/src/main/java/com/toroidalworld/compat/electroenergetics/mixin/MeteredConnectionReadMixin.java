package com.toroidalworld.compat.electroenergetics.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.george_vi.electroenergetics.content.clamp_meter.ClampMeterItem;
import com.george_vi.electroenergetics.content.linemans_stick.LinemansStickItem;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNodeConnection;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.toroidalworld.client.engine.ClientFrame;
import com.toroidalworld.compat.electroenergetics.WireNodes;

@Mixin(value = {ClampMeterItem.class, LinemansStickItem.class}, remap = false)
public abstract class MeteredConnectionReadMixin {
    @ModifyExpressionValue(method = "onUseTick",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;getOrDefault(Lnet/minecraft/core/component/DataComponentType;Ljava/lang/Object;)Ljava/lang/Object;"))
    private Object toroidal$seatMeteredConnection(Object stored) {
        return stored instanceof InWorldNodeConnection connection
                ? WireNodes.moved(connection, ClientFrame::nearestToPlayer)
                : stored;
    }
}
