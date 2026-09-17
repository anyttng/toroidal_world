package com.toroidalworld.compat.ftbchunks.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.blaze3d.platform.Window;
import com.toroidalworld.compat.ftbchunks.FtbChunksFold;

import dev.ftb.mods.ftbchunks.client.gui.LargeMapScreen;
import net.minecraft.client.Minecraft;

@Mixin(value = LargeMapScreen.class, remap = false)
public abstract class LargeMapScreenMixin {
    @ModifyReturnValue(method = "determineMinZoom", at = @At("RETURN"))
    private int toroidal$floorZoomToWorld(int original) {
        Window window = Minecraft.getInstance().getWindow();
        return Math.max(original, FtbChunksFold.zoomFloor(window.getWidth(), window.getHeight()));
    }
}
