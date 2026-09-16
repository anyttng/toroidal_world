package com.toroidalworld.platform;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.function.IntFunction;

import com.toroidalworld.ToroidalWorld;
import com.toroidalworld.compat.MapCopies;
import com.toroidalworld.core.FlatShape;
import com.toroidalworld.engine.net.BlockEntityPositionsPayload;
import com.toroidalworld.engine.net.TagPositions;
import com.toroidalworld.engine.net.WrappingSettingsPayload;

import io.netty.buffer.Unpooled;


import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.LevelStem;

public final class FabricPlatform implements Platform {
    private static final String LOADER_MOD_ID = "fabricloader";
    private static final String CLIENT_CONFIG_FILE = ToroidalWorld.MODID + "-client.properties";
    private static final String MAP_COPIES_KEY = "mapCopies";
    private static final List<String> CLIENT_CONFIG_DEFAULT = List.of(
            "# mapCopies: how a map mod's fullscreen map draws a looped world.",
            "#   REPEATED - fills the window with the world's copies.",
            "#   SINGLE - draws one copy and stops the view and the zoom at its edges.",
            "# Allowed values: REPEATED, SINGLE. Read once at game start.",
            MAP_COPIES_KEY + "=" + MapCopies.REPEATED.name());

    private volatile MapCopies mapCopies;

    @Override
    public boolean isClient() {
        return FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT;
    }

    @Override
    public String modVersion() {
        return versionOf(ToroidalWorld.MODID);
    }

    @Override
    public String loaderName() {
        return "fabric";
    }

    @Override
    public String loaderVersion() {
        return versionOf(LOADER_MOD_ID);
    }

    private static String versionOf(String modId) {
        return FabricLoader.getInstance().getModContainer(modId).orElseThrow()
                .getMetadata().getVersion().getFriendlyString();
    }

    @Override
    public void sendWorldShape(ServerPlayer player, ResourceKey<Level> dimension, FlatShape shape) {
        if (ServerPlayNetworking.canSend(player, WrappingSettingsPayload.TYPE)) {
            ServerPlayNetworking.send(player, new WrappingSettingsPayload(dimension, shape));
        }
    }

    @Override
    public void sendBlockEntityPositions(ServerPlayer player,
            Map<ResourceLocation, List<TagPositions.TagPosition>> blockEntities) {
        if (ServerPlayNetworking.canSend(player, BlockEntityPositionsPayload.TYPE)) {
            ServerPlayNetworking.send(player, new BlockEntityPositionsPayload(blockEntities));
        }
    }

    @Override
    public IntFunction<RegistryFriendlyByteBuf> packetBuffers(ServerPlayer player) {
        return capacity -> new RegistryFriendlyByteBuf(Unpooled.buffer(capacity), player.registryAccess());
    }

    @Override
    public LevelStem withGenerator(LevelStem stem, ChunkGenerator generator) {
        return new LevelStem(stem.type(), generator);
    }

    @Override
    public MapCopies mapCopies() {
        MapCopies resolved = mapCopies;
        if (resolved == null) {
            resolved = readMapCopies(FabricLoader.getInstance().getConfigDir().resolve(CLIENT_CONFIG_FILE));
            mapCopies = resolved;
        }

        return resolved;
    }

    private static MapCopies readMapCopies(Path file) {
        if (!Files.exists(file)) {
            writeDefaultClientConfig(file);
            return MapCopies.REPEATED;
        }

        Properties properties = new Properties();
        try (Reader reader = Files.newBufferedReader(file)) {
            properties.load(reader);
        } catch (IOException e) {
            ToroidalWorld.LOGGER.warn("[config] client_config_unreadable file={} error={}", file, e.toString());
            return MapCopies.REPEATED;
        }

        String value = properties.getProperty(MAP_COPIES_KEY, MapCopies.REPEATED.name()).strip();
        try {
            return MapCopies.valueOf(value.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            ToroidalWorld.LOGGER.warn("[config] map_copies_unknown value={} fallback={}", value, MapCopies.REPEATED);
            return MapCopies.REPEATED;
        }
    }

    private static void writeDefaultClientConfig(Path file) {
        try {
            Files.write(file, CLIENT_CONFIG_DEFAULT);
        } catch (IOException e) {
            ToroidalWorld.LOGGER.warn("[config] client_config_unwritable file={} error={}", file, e.toString());
        }
    }
}
