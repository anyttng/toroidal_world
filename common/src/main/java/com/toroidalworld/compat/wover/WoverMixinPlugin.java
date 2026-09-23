package com.toroidalworld.compat.wover;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import com.toroidalworld.compat.ModPresence;
import com.toroidalworld.compat.ModPresenceGatePlugin;
import com.toroidalworld.compat.ModSymbol;

public class WoverMixinPlugin extends ModPresenceGatePlugin {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final String NOISE_BIOME_NAME = "getNoiseBiome";

    private static final String NOISE_BIOME_DESCRIPTOR =
            "(IIILnet/minecraft/world/level/biome/Climate$Sampler;)Lnet/minecraft/core/Holder;";

    static final ModSymbol RAW_BIOME = new ModSymbol(
            "org/betterx/wover/generator/impl/map/hex/HexBiomeMap", "getRawBiome",
            "(DD)Lorg/betterx/wover/generator/api/biomesource/WoverBiomePicker$PickableBiome;");

    static final ModSymbol NETHER_NOISE_BIOME = new ModSymbol(
            "org/betterx/wover/generator/impl/biomesource/nether/WoverNetherBiomeSource", NOISE_BIOME_NAME,
            NOISE_BIOME_DESCRIPTOR);

    static final ModSymbol END_NOISE_BIOME = new ModSymbol(
            "org/betterx/wover/generator/impl/biomesource/end/WoverEndBiomeSource", NOISE_BIOME_NAME,
            NOISE_BIOME_DESCRIPTOR);

    private static final ModPresence WOVER = ModPresence.of(LOGGER,
            "org/betterx/wover/generator/impl/map/hex/HexBiomeMap.class",
            "[wover-compat] gate wover_present", RAW_BIOME, NETHER_NOISE_BIOME, END_NOISE_BIOME);

    public WoverMixinPlugin() {
        super(WOVER);
    }
}
