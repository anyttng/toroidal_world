package com.toroidalworld.compat.lithium;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import com.toroidalworld.compat.ModPresence;
import com.toroidalworld.compat.ModPresenceGatePlugin;
import com.toroidalworld.compat.ModSymbol;

public class LithiumMixinPlugin extends ModPresenceGatePlugin {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final String TRACKER_PACKAGE = "net/caffeinemc/mods/lithium/common/tracking/entity/";
    private static final String TRACKER = TRACKER_PACKAGE + "SectionedEntityMovementTracker";
    private static final String GET_ENTITIES = "getEntities";
    private static final String GET_ENTITIES_DESCRIPTOR = "(Lnet/minecraft/world/phys/AABB;)Ljava/util/List;";

    private static final ModSymbol TRACKER_REGISTER = new ModSymbol(TRACKER, "register",
            "(Lnet/minecraft/server/level/ServerLevel;)V");
    private static final ModSymbol INVENTORY_TRACKER_QUERY = new ModSymbol(
            TRACKER_PACKAGE + "SectionedInventoryEntityMovementTracker", GET_ENTITIES, GET_ENTITIES_DESCRIPTOR);
    private static final ModSymbol ITEM_TRACKER_QUERY = new ModSymbol(
            TRACKER_PACKAGE + "SectionedItemEntityMovementTracker", GET_ENTITIES, GET_ENTITIES_DESCRIPTOR);

    private static final ModPresence LITHIUM = ModPresence.of(LOGGER, TRACKER + ".class",
            "[lithium-compat] gate lithium_present", TRACKER_REGISTER, INVENTORY_TRACKER_QUERY, ITEM_TRACKER_QUERY);

    public LithiumMixinPlugin() {
        super(LITHIUM);
    }
}
