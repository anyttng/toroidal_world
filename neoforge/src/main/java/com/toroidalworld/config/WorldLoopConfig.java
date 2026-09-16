package com.toroidalworld.config;

import java.util.Locale;

import com.toroidalworld.ToroidalWorld;
import com.toroidalworld.compat.MapCopies;

import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.TranslatableEnum;

public final class WorldLoopConfig {
    private static final String MAP_COPIES_KEY = "mapCopies";
    private static final String MAP_COPIES_TRANSLATION = ToroidalWorld.MODID + ".configuration." + MAP_COPIES_KEY;

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.EnumValue<MapCopiesChoice> MAP_COPIES = BUILDER
            .comment("How a map mod's fullscreen map draws a looped world: REPEATED fills the window with its copies, "
                    + "SINGLE draws one copy and stops the view and the zoom at its edges. Minimaps always repeat.")
            .translation(MAP_COPIES_TRANSLATION)
            .defineEnum(MAP_COPIES_KEY, MapCopiesChoice.REPEATED);

    public static final ModConfigSpec SPEC = BUILDER.build();

    public enum MapCopiesChoice implements TranslatableEnum {
        REPEATED(MapCopies.REPEATED),
        SINGLE(MapCopies.SINGLE);

        private final MapCopies copies;

        MapCopiesChoice(MapCopies copies) {
            this.copies = copies;
        }

        public MapCopies copies() {
            return copies;
        }

        @Override
        public Component getTranslatedName() {
            return Component.translatable(MAP_COPIES_TRANSLATION + "." + name().toLowerCase(Locale.ROOT));
        }
    }

    private WorldLoopConfig() {
    }
}
