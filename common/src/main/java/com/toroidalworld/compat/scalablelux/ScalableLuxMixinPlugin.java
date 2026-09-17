package com.toroidalworld.compat.scalablelux;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import com.toroidalworld.compat.ModPresence;
import com.toroidalworld.compat.ModPresenceGatePlugin;
import com.toroidalworld.compat.ModSymbol;

public class ScalableLuxMixinPlugin extends ModPresenceGatePlugin {
    private static final Logger LOGGER = LogUtils.getLogger();

    static final ModSymbol ANY_CHUNK_NOW = new ModSymbol(
            "ca/spottedleaf/starlight/common/light/StarLightInterface", "getAnyChunkNow",
            "(II)Lnet/minecraft/world/level/chunk/ChunkAccess;");

    private static final ModPresence SCALABLELUX = ModPresence.of(LOGGER,
            "ca/spottedleaf/starlight/common/light/StarLightInterface.class",
            "[scalablelux-compat] gate scalablelux_present", ANY_CHUNK_NOW);

    public ScalableLuxMixinPlugin() {
        super(SCALABLELUX);
    }
}
