package com.toroidalworld;

import com.toroidalworld.client.settings.SettingsScreen;
import com.toroidalworld.engine.gen.LoopedChunkGenerator;
import com.toroidalworld.engine.gen.LoopedFlatChunkGenerator;
import com.toroidalworld.engine.gen.WorldLoopGenerators;
import com.toroidalworld.engine.gen.WorldLoopTicketTypes;
import com.toroidalworld.engine.net.AuxiliaryLightTranslation;
import com.toroidalworld.engine.net.BlockParticleTranslation;
import com.toroidalworld.engine.net.SpawnBufferTranslation;
import com.toroidalworld.engine.seam.circumnavigation.WorldLoopCriteria;
import com.toroidalworld.platform.NeoForgePlatform;
import com.toroidalworld.platform.Platforms;
import com.toroidalworld.settings.SettingsService;
import com.toroidalworld.shape.GenerationHookSetup;
import com.toroidalworld.shape.WorldOptionSetup;
import com.toroidalworld.shape.WorldShapeSetup;
import com.mojang.serialization.MapCodec;

import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class WorldLoop {
    private static final DeferredRegister<MapCodec<? extends ChunkGenerator>> CHUNK_GENERATORS =
            DeferredRegister.create(Registries.CHUNK_GENERATOR, ToroidalWorld.MODID);

    private static final DeferredRegister<TicketType> TICKET_TYPES =
            DeferredRegister.create(Registries.TICKET_TYPE, ToroidalWorld.MODID);

    private static final DeferredRegister<CriterionTrigger<?>> CRITERIA =
            DeferredRegister.create(Registries.TRIGGER_TYPE, ToroidalWorld.MODID);

    public static void init(IEventBus modEventBus, ModContainer modContainer) {
        Platforms.set(new NeoForgePlatform(modContainer));
        WorldOptionSetup.registerAll();
        WorldShapeSetup.registerAll();
        GenerationHookSetup.registerAll();

        CHUNK_GENERATORS.register(WorldLoopGenerators.TOROIDAL_ID, () -> LoopedChunkGenerator.CODEC);
        CHUNK_GENERATORS.register(WorldLoopGenerators.TOROIDAL_FLAT_ID, () -> LoopedFlatChunkGenerator.CODEC);
        CHUNK_GENERATORS.register(modEventBus);

        TICKET_TYPES.register(WorldLoopTicketTypes.SEAM_GENERATION_ID, () -> WorldLoopTicketTypes.SEAM_GENERATION);
        TICKET_TYPES.register(modEventBus);

        CRITERIA.register(WorldLoopCriteria.CIRCUMNAVIGATE_ID, () -> WorldLoopCriteria.CIRCUMNAVIGATE);
        CRITERIA.register(modEventBus);

        AuxiliaryLightTranslation.register();
        BlockParticleTranslation.register();
        SpawnBufferTranslation.register();
        if (Platforms.get().isClient()) {
            SettingsService.set(SettingsService.load(Platforms.get().configDir()));
            modContainer.registerExtensionPoint(IConfigScreenFactory.class,
                    (container, modListScreen) -> new SettingsScreen(modListScreen));
        }
    }

    private WorldLoop() {
    }
}
