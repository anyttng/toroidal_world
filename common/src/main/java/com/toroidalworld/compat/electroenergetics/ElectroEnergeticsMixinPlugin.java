package com.toroidalworld.compat.electroenergetics;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import com.toroidalworld.compat.ModPresence;
import com.toroidalworld.compat.ModPresenceGatePlugin;
import com.toroidalworld.compat.ModSymbol;

public class ElectroEnergeticsMixinPlugin extends ModPresenceGatePlugin {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final ModSymbol SECTION_INDEX = new ModSymbol(
            "com/george_vi/electroenergetics/simulation/infrastructure/WireSimulationState",
            "getConnectionsInSection", "(J)Ljava/util/Map;");

    private static final ModSymbol DETACHED_NODE_TICK = new ModSymbol(
            "com/george_vi/electroenergetics/simulation/infrastructure/detached_nodes/DetachedNodeEntity",
            "tick", "()V");

    private static final ModSymbol CHANGE_LENGTH_PACKET = new ModSymbol(
            "com/george_vi/electroenergetics/content/wire_spool/ChangeLengthWirePacket",
            "handle", "(Lnet/minecraft/server/level/ServerPlayer;)V");

    private static final ModSymbol LINEMANS_STICK = new ModSymbol(
            "com/george_vi/electroenergetics/content/linemans_stick/LinemansStickWireInteractionBehaviour",
            "interactWire", "(Lcom/george_vi/electroenergetics/foundation/nodes/NodeConnectionPoint;"
                    + "Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;"
                    + "Lnet/minecraft/world/item/ItemStack;)V");

    private static final ModPresence ELECTRO_ENERGETICS = ModPresence.of(LOGGER,
            "com/george_vi/electroenergetics/CreateElectroEnergetics.class",
            "[electroenergetics-compat] gate electroenergetics_present",
            SECTION_INDEX, DETACHED_NODE_TICK, CHANGE_LENGTH_PACKET, LINEMANS_STICK);

    public ElectroEnergeticsMixinPlugin() {
        super(ELECTRO_ENERGETICS);
    }
}
