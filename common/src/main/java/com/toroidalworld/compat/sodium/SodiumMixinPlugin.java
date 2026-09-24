package com.toroidalworld.compat.sodium;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import com.toroidalworld.compat.ModPresence;
import com.toroidalworld.compat.ModPresenceGatePlugin;
import com.toroidalworld.compat.ModSymbol;

public class SodiumMixinPlugin extends ModPresenceGatePlugin {
    private static final Logger LOGGER = LogUtils.getLogger();

    static final ModSymbol PREPARE_ON_SLICE = new ModSymbol(
            "net/caffeinemc/mods/sodium/client/render/chunk/compile/pipeline/BlockRenderer", "prepare",
            "(Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/ChunkBuildBuffers;"
                    + "Lnet/caffeinemc/mods/sodium/client/world/LevelSlice;"
                    + "Lnet/caffeinemc/mods/sodium/client/render/chunk/translucent_sorting/TranslucentGeometryCollector;)V");

    private static final ModPresence SODIUM = ModPresence.of(LOGGER,
            "net/caffeinemc/mods/sodium/client/render/chunk/compile/pipeline/BlockRenderer.class",
            "[sodium-compat] gate sodium_present", PREPARE_ON_SLICE);

    public SodiumMixinPlugin() {
        super(SODIUM);
    }
}
