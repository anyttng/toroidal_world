package com.toroidalworld.compat.c2me;

import org.spongepowered.asm.mixin.Mixins;

import com.bawnorton.mixinsquared.MixinSquaredBootstrap;
import com.toroidalworld.MixinGatePlugin;

public class C2meMixinPlugin extends MixinGatePlugin {
    private static final String AQUIFER_MIXIN = "AquiferSeamMixin";

    private static final String[] NO_TICK_VD_MIXINS = {
            "PlayerNoTickLoaderMixin",
            "ServerAccessibleChunkSendingMixin"
    };

    @Override
    public void onLoad(String mixinPackage) {
        MixinSquaredBootstrap.init();
        Mixins.registerErrorHandlerClass(C2meMixinErrorHandler.class.getName());
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (mixinClassName.endsWith(AQUIFER_MIXIN)) {
            return C2meAquifer.optimizesAquifer();
        }

        for (String noTickVdMixin : NO_TICK_VD_MIXINS) {
            if (mixinClassName.endsWith(noTickVdMixin)) {
                return C2meNoTickVd.present();
            }
        }

        return C2meChunkSystem.present();
    }
}
