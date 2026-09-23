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

    private static final String RAW_BIOME_NAME = "getRawBiome";

    private static final String RAW_BIOME_DESCRIPTOR =
            "(DD)Lorg/betterx/wover/generator/api/biomesource/WoverBiomePicker$PickableBiome;";

    private static final String MAP_STACK = "org/betterx/wover/generator/impl/map/MapStack";

    private static final String SQUARE_MAP = "org/betterx/wover/generator/impl/map/square/SquareBiomeMap";

    private static final String CONSTRUCTOR = "<init>";

    static final ModSymbol RAW_BIOME = new ModSymbol(
            "org/betterx/wover/generator/impl/map/hex/HexBiomeMap", RAW_BIOME_NAME, RAW_BIOME_DESCRIPTOR);

    static final ModSymbol SQUARE_RAW_BIOME = new ModSymbol(SQUARE_MAP, RAW_BIOME_NAME, RAW_BIOME_DESCRIPTOR);

    static final ModSymbol SQUARE_CONSTRUCTOR = new ModSymbol(SQUARE_MAP, CONSTRUCTOR,
            "(JILorg/betterx/wover/generator/api/biomesource/WoverBiomePicker;)V");

    static final ModSymbol STACK_BIOME = new ModSymbol(MAP_STACK, "getBiome",
            "(DDD)Lorg/betterx/wover/generator/api/biomesource/WoverBiomePicker$PickableBiome;");

    static final ModSymbol STACK_CONSTRUCTOR = new ModSymbol(MAP_STACK, CONSTRUCTOR,
            "(JILorg/betterx/wover/generator/api/biomesource/WoverBiomePicker;II"
                    + "Lorg/betterx/wover/generator/api/map/MapBuilderFunction;)V");

    static final ModSymbol NETHER_NOISE_BIOME = new ModSymbol(
            "org/betterx/wover/generator/impl/biomesource/nether/WoverNetherBiomeSource", NOISE_BIOME_NAME,
            NOISE_BIOME_DESCRIPTOR);

    static final ModSymbol END_NOISE_BIOME = new ModSymbol(
            "org/betterx/wover/generator/impl/biomesource/end/WoverEndBiomeSource", NOISE_BIOME_NAME,
            NOISE_BIOME_DESCRIPTOR);

    static final ModSymbol REPLACE_GENERATOR = new ModSymbol(
            "org/betterx/wover/generator/impl/chunkgenerator/WoverChunkGeneratorImpl", "replaceGenerator",
            "(Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/resources/ResourceKey;"
                    + "Lnet/minecraft/core/RegistryAccess;Ljava/util/Set;Lnet/minecraft/world/level/chunk/ChunkGenerator;"
                    + "Lorg/betterx/wover/generator/impl/chunkgenerator/WoverChunkGeneratorImpl$StemGetter;"
                    + "Lorg/betterx/wover/generator/impl/chunkgenerator/WoverChunkGeneratorImpl$RegisterHelper;)"
                    + "Lnet/minecraft/core/Registry;");

    private static final ModPresence WOVER = ModPresence.of(LOGGER,
            "org/betterx/wover/generator/impl/map/hex/HexBiomeMap.class",
            "[wover-compat] gate wover_present", RAW_BIOME, SQUARE_RAW_BIOME, SQUARE_CONSTRUCTOR, STACK_BIOME,
            STACK_CONSTRUCTOR, NETHER_NOISE_BIOME, END_NOISE_BIOME, REPLACE_GENERATOR);

    public WoverMixinPlugin() {
        super(WOVER);
    }
}
