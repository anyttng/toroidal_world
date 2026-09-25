package com.toroidalworld.compat.betterend;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import com.toroidalworld.compat.ModPresence;
import com.toroidalworld.compat.ModPresenceGatePlugin;
import com.toroidalworld.compat.ModSymbol;
import com.toroidalworld.compat.wover.WoverInjectionTargets;

public class BetterEndMixinPlugin extends ModPresenceGatePlugin {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final String TERRAIN_GENERATOR = "org/betterx/betterend/world/generator/TerrainGenerator";

    private static final String ISLAND_LAYER = "org/betterx/betterend/world/generator/IslandLayer";

    private static final String ISLAND_LAYER_TYPE = "Lorg/betterx/betterend/world/generator/IslandLayer;";

    private static final String SPLIT_CONDITION = "org/betterx/betterend/world/surface/SplitNoiseCondition";

    static final ModSymbol LEVEL_INIT = new ModSymbol(TERRAIN_GENERATOR, "onServerLevelInit",
            "(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/level/dimension/LevelStem;J)V");

    static final ModSymbol FILL_DENSITY = new ModSymbol(TERRAIN_GENERATOR, "fillTerrainDensity", "([DIIIII)V");

    static final ModSymbol IS_LAND = new ModSymbol(TERRAIN_GENERATOR, "isLand", "(III)Ljava/lang/Boolean;");

    static final ModSymbol AVERAGE_DEPTH = new ModSymbol(TERRAIN_GENERATOR, "getAverageDepth", "(II)F");

    static final ModSymbol LOCKER = new ModSymbol(TERRAIN_GENERATOR, "LOCKER",
            "Ljava/util/concurrent/locks/ReentrantLock;");

    static final ModSymbol LARGE_ISLANDS = new ModSymbol(TERRAIN_GENERATOR, "largeIslands", ISLAND_LAYER_TYPE);

    static final ModSymbol MEDIUM_ISLANDS = new ModSymbol(TERRAIN_GENERATOR, "mediumIslands", ISLAND_LAYER_TYPE);

    static final ModSymbol SMALL_ISLANDS = new ModSymbol(TERRAIN_GENERATOR, "smallIslands", ISLAND_LAYER_TYPE);

    static final ModSymbol BOOL_CACHE = new ModSymbol(TERRAIN_GENERATOR, "TERRAIN_BOOL_CACHE_MAP", "Ljava/util/Map;");

    static final ModSymbol BOOL_CACHE_KEY = new ModSymbol(TERRAIN_GENERATOR, "POS", "Ljava/awt/Point;");

    static final ModSymbol ISLAND_SEED = new ModSymbol(ISLAND_LAYER, "getSeed", "(II)I");

    static final ModSymbol ISLAND = new ModSymbol(ISLAND_LAYER, "getIsland",
            "(Lnet/minecraft/core/BlockPos;)Lorg/betterx/bclib/sdf/SDF;");

    static final ModSymbol RELATIVE_DISTANCE = new ModSymbol(ISLAND_LAYER, "getRelativeDistance",
            "(Lorg/betterx/bclib/sdf/SDF;Lnet/minecraft/core/BlockPos;DDD)F");

    static final ModSymbol ISLAND_NOISE = new ModSymbol(ISLAND_LAYER, "noise",
            "Lorg/betterx/bclib/sdf/operator/SDFRadialNoiseMap;");

    static final ModSymbol ISLAND_DENSITY = new ModSymbol(ISLAND_LAYER, "density",
            "Lorg/betterx/betterend/noise/OpenSimplexNoise;");

    static final ModSymbol ISLAND_OPTIONS = new ModSymbol(ISLAND_LAYER, "options",
            "Lorg/betterx/betterend/world/generator/LayerOptions;");

    static final ModSymbol SPLIT_NUMBER = new ModSymbol(SPLIT_CONDITION, WoverInjectionTargets.NUMBER,
            WoverInjectionTargets.NUMBER_DESCRIPTOR);

    static final ModSymbol SPLIT_NOISE = new ModSymbol(SPLIT_CONDITION, "getNoise", "(III)D");

    static final ModSymbol SULPHURIC_NUMBER = new ModSymbol(
            "org/betterx/betterend/world/surface/SulphuricSurfaceNoiseCondition", WoverInjectionTargets.NUMBER,
            WoverInjectionTargets.NUMBER_DESCRIPTOR);

    static final ModSymbol UMBRA_DEPTH = new ModSymbol("org/betterx/betterend/world/surface/UmbraSurfaceNoiseCondition",
            "getDepth", "(III)I");

    static final ModSymbol NOISE_EVAL_2D = new ModSymbol("org/betterx/betterend/noise/OpenSimplexNoise", "eval",
            "(DD)D");

    static final ModSymbol[] SYMBOLS = {LEVEL_INIT, FILL_DENSITY, IS_LAND, AVERAGE_DEPTH, LOCKER, LARGE_ISLANDS,
            MEDIUM_ISLANDS, SMALL_ISLANDS, BOOL_CACHE, BOOL_CACHE_KEY, ISLAND_SEED, ISLAND, RELATIVE_DISTANCE,
            ISLAND_NOISE, ISLAND_DENSITY, ISLAND_OPTIONS, SPLIT_NUMBER, SPLIT_NOISE, SULPHURIC_NUMBER, UMBRA_DEPTH,
            NOISE_EVAL_2D};

    private static final ModPresence BETTER_END = ModPresence.of(LOGGER, TERRAIN_GENERATOR + ".class",
            "[betterend-compat] gate betterend_present", SYMBOLS);

    public BetterEndMixinPlugin() {
        super(BETTER_END);
    }
}
