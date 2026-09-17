package com.toroidalworld.compat.mekanism;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import com.toroidalworld.compat.ModPresence;
import com.toroidalworld.compat.ModPresenceGatePlugin;
import com.toroidalworld.compat.ModSymbol;

public class MekanismMixinPlugin extends ModPresenceGatePlugin {
    private static final Logger LOGGER = LogUtils.getLogger();

    static final ModSymbol TELEPORTER_CLOSEST_COORDS = new ModSymbol(
            "mekanism/common/content/teleporter/TeleporterFrequency", "getClosestCoords",
            "(Lnet/minecraft/core/GlobalPos;)Lnet/minecraft/core/GlobalPos;");

    private static final ModPresence MEKANISM = ModPresence.of(LOGGER, "mekanism/common/Mekanism.class",
            "[mek-compat] gate mekanism_present", TELEPORTER_CLOSEST_COORDS);

    public MekanismMixinPlugin() {
        super(MEKANISM);
    }
}
