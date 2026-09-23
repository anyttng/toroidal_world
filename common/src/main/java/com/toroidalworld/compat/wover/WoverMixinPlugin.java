package com.toroidalworld.compat.wover;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import com.toroidalworld.compat.ModPresence;
import com.toroidalworld.compat.ModPresenceGatePlugin;
import com.toroidalworld.compat.ModSymbol;

public class WoverMixinPlugin extends ModPresenceGatePlugin {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final String INIT_MAP_NAME = "onInitMap";

    private static final String INIT_MAP_DESCRIPTOR = "(J)V";

    static final ModSymbol RAW_BIOME = new ModSymbol(
            "org/betterx/wover/generator/impl/map/hex/HexBiomeMap", "getRawBiome",
            "(DD)Lorg/betterx/wover/generator/api/biomesource/WoverBiomePicker$PickableBiome;");

    // A game type is spelled per loader on this line, so a target class is gated by a member naming none.
    static final ModSymbol NETHER_INIT_MAP = new ModSymbol(
            "org/betterx/wover/generator/impl/biomesource/nether/WoverNetherBiomeSource", INIT_MAP_NAME,
            INIT_MAP_DESCRIPTOR);

    static final ModSymbol END_INIT_MAP = new ModSymbol(
            "org/betterx/wover/generator/impl/biomesource/end/WoverEndBiomeSource", INIT_MAP_NAME,
            INIT_MAP_DESCRIPTOR);

    static final ModSymbol GENERATOR_INITIALIZE = new ModSymbol(
            "org/betterx/wover/generator/impl/chunkgenerator/WoverChunkGeneratorImpl", "initialize", "()V");

    private static final ModPresence WOVER = ModPresence.of(LOGGER,
            "org/betterx/wover/generator/impl/map/hex/HexBiomeMap.class",
            "[wover-compat] gate wover_present", RAW_BIOME, NETHER_INIT_MAP, END_INIT_MAP, GENERATOR_INITIALIZE);

    public WoverMixinPlugin() {
        super(WOVER);
    }
}
