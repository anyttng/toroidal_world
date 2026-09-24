package com.toroidalworld.compat.reterraforged;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import com.toroidalworld.compat.ModPresence;
import com.toroidalworld.compat.ModPresenceGatePlugin;
import com.toroidalworld.compat.ModSymbol;

public class ReTerraForgedMixinPlugin extends ModPresenceGatePlugin {
    private static final Logger LOGGER = LogUtils.getLogger();

    static final ModSymbol LATTICE_HASH = new ModSymbol(
            "raccoonman/reterraforged/world/worldgen/noise/NoiseUtil", "hash2D", "(III)I");

    static final ModSymbol HEIGHTMAP_RIVERS = new ModSymbol(
            "raccoonman/reterraforged/world/worldgen/cell/heightmap/Heightmap", "applyRivers",
            "(Lraccoonman/reterraforged/world/worldgen/cell/Cell;FF"
                    + "Lraccoonman/reterraforged/world/worldgen/cell/rivermap/Rivermap;)V");

    static final ModSymbol UPLIFT_GRADIENT = new ModSymbol(
            "raccoonman/reterraforged/world/worldgen/cell/continent/uplift/UpliftContinentGenerator",
            "getSmoothVoronoiGradient", "(Lraccoonman/reterraforged/world/worldgen/cell/Cell;FF)F");

    private static final ModPresence RETERRAFORGED = ModPresence.of(LOGGER,
            "raccoonman/reterraforged/world/worldgen/cell/heightmap/Heightmap.class",
            "[reterraforged-compat] gate reterraforged_present", LATTICE_HASH, HEIGHTMAP_RIVERS, UPLIFT_GRADIENT);

    public ReTerraForgedMixinPlugin() {
        super(RETERRAFORGED);
    }
}
