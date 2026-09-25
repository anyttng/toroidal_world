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
    private static final String CONSTRUCTOR = "<init>";
    private static final String TRACKER_CONSTRUCTOR_DESCRIPTOR =
            "(Lnet/caffeinemc/mods/lithium/common/util/tuples/WorldSectionBox;Ljava/lang/Class;)V";

    // A game type is spelled per loader on this line, so a target class is gated by a member naming none.
    private static final ModSymbol TRACKER_CONSTRUCTOR = new ModSymbol(TRACKER, CONSTRUCTOR,
            TRACKER_CONSTRUCTOR_DESCRIPTOR);
    private static final ModSymbol INVENTORY_TRACKER_CONSTRUCTOR = new ModSymbol(
            TRACKER_PACKAGE + "SectionedInventoryEntityMovementTracker", CONSTRUCTOR, TRACKER_CONSTRUCTOR_DESCRIPTOR);
    private static final ModSymbol ITEM_TRACKER_CONSTRUCTOR = new ModSymbol(
            TRACKER_PACKAGE + "SectionedItemEntityMovementTracker", CONSTRUCTOR, TRACKER_CONSTRUCTOR_DESCRIPTOR);

    private static final ModPresence LITHIUM = ModPresence.of(LOGGER, TRACKER + ".class",
            "[lithium-compat] gate lithium_present", TRACKER_CONSTRUCTOR, INVENTORY_TRACKER_CONSTRUCTOR,
            ITEM_TRACKER_CONSTRUCTOR);

    public LithiumMixinPlugin() {
        super(LITHIUM);
    }
}
