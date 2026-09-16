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

public final class LoaderlessPlatform implements Platform {
    private static final String NO_LOADER = "The vanilla test classpath has no loader";

    @Override
    public boolean isClient() {
        throw new UnsupportedOperationException(NO_LOADER);
    }

    @Override
    public String modVersion() {
        throw new UnsupportedOperationException(NO_LOADER);
    }

    @Override
    public String loaderName() {
        throw new UnsupportedOperationException(NO_LOADER);
    }

    @Override
    public String loaderVersion() {
        throw new UnsupportedOperationException(NO_LOADER);
    }

    @Override
    public void sendWorldShape(ServerPlayer player, ResourceKey<Level> dimension, FlatShape shape) {
        throw new UnsupportedOperationException(NO_LOADER);
    }

    @Override
    public void sendBlockEntityPositions(ServerPlayer player,
            Map<ResourceLocation, List<TagPositions.TagPosition>> blockEntities) {
        throw new UnsupportedOperationException(NO_LOADER);
    }

    @Override
    public IntFunction<RegistryFriendlyByteBuf> packetBuffers(ServerPlayer player) {
        throw new UnsupportedOperationException(NO_LOADER);
    }

    @Override
    public LevelStem withGenerator(LevelStem stem, ChunkGenerator generator) {
        return new LevelStem(stem.type(), generator);
    }

    @Override
    public MapCopies mapCopies() {
        throw new UnsupportedOperationException(NO_LOADER);
    }
}
