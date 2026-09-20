package com.toroidalworld.compat.biolith;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import com.toroidalworld.compat.ModPresence;
import com.toroidalworld.compat.ModPresenceGatePlugin;
import com.toroidalworld.compat.ModSymbol;

public class BiolithMixinPlugin extends ModPresenceGatePlugin {
    private static final Logger LOGGER = LogUtils.getLogger();

    static final ModSymbol LOCAL_NOISE = new ModSymbol(
            "com/terraformersmc/biolith/impl/biome/OverworldBiomePlacement", "getLocalNoise", "(III)D");

    private static final ModPresence BIOLITH = ModPresence.of(LOGGER,
            "com/terraformersmc/biolith/impl/biome/OverworldBiomePlacement.class",
            "[biolith-compat] gate biolith_present", LOCAL_NOISE);

    public BiolithMixinPlugin() {
        super(BIOLITH);
    }
}
