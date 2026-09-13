package com.toroidalworld.engine.net;

import com.toroidalworld.platform.Platforms;

import net.minecraft.server.level.ServerPlayer;

public final class PositionRowsSync {
    private static volatile PositionRows current = PositionRows.EMPTY;

    static void apply(PositionRows rows) {
        current = rows;
        SpawnBufferFold.declare(rows.entities());
    }

    public static void sendTo(ServerPlayer player) {
        Platforms.get().sendBlockEntityPositions(player, current.blockEntities());
    }

    private PositionRowsSync() {
    }
}
