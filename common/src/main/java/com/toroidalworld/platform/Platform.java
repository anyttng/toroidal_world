package com.toroidalworld.platform;

import java.util.List;
import java.util.Map;
import java.util.function.IntFunction;

import com.toroidalworld.compat.MapCopies;
import com.toroidalworld.core.FlatShape;
import com.toroidalworld.engine.net.TagPositions;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.LevelStem;

public interface Platform {
    boolean isClient();

    String modVersion();

    String loaderName();

    String loaderVersion();

    void sendWorldShape(ServerPlayer player, ResourceKey<Level> dimension, FlatShape shape);

    void sendBlockEntityPositions(ServerPlayer player, Map<ResourceLocation, List<TagPositions.TagPosition>> blockEntities);

    IntFunction<RegistryFriendlyByteBuf> packetBuffers(ServerPlayer player);

    LevelStem withGenerator(LevelStem stem, ChunkGenerator generator);

    MapCopies mapCopies();
}
