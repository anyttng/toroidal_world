package com.toroidalworld.mixin;

import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.toroidalworld.client.engine.PublishedShapes;
import com.toroidalworld.client.engine.SyncedTagFold;

import net.minecraft.client.multiplayer.ClientPacketListener;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {
    @Inject(method = "clearLevel", at = @At("RETURN"))
    private void toroidal$forgetWhatTheServerPublished(CallbackInfo ci) {
        PublishedShapes.clear();
        SyncedTagFold.declare(Map.of());
    }
}
