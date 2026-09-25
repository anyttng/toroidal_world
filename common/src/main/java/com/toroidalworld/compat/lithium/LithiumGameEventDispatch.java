package com.toroidalworld.compat.lithium;

import java.lang.reflect.Field;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import com.toroidalworld.compat.ModSymbol;

public final class LithiumGameEventDispatch {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final String GATE_LABEL = "[lithium-compat] gate game_events_dispatch";
    private static final String CLASS_SUFFIX = ".class";
    private static final String MIXIN_ROOT = "net.caffeinemc.mods.lithium.mixin.";
    private static final String MIXIN_PLUGIN_CLASS = MIXIN_ROOT + "LithiumMixinPlugin";
    private static final String CONFIG_FIELD = "CONFIG";
    private static final String EFFECTIVE_OPTION_METHOD = "getEffectiveOptionForMixin";
    private static final String IS_ENABLED_METHOD = "isEnabled";
    private static final String MIXIN_RULE =
            LithiumInjectionTargets.GAME_EVENT_DISPATCHER_MIXIN.substring(MIXIN_ROOT.length());
    private static final String HANDLER_OWNER = LithiumInjectionTargets.GAME_EVENT_DISPATCHER_MIXIN.replace('.', '/');

    static final ModSymbol NULL_DISPATCHER_HANDLER = new ModSymbol(HANDLER_OWNER,
            LithiumInjectionTargets.NULL_DISPATCHER_HANDLER,
            "(Lnet/minecraft/world/level/gameevent/GameEventListenerRegistry;Lnet/minecraft/core/Holder;"
                    + "Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/level/gameevent/GameEvent$Context;"
                    + "Lnet/minecraft/world/level/gameevent/GameEventListenerRegistry$ListenerVisitor;III)Z");

    // A game type is spelled per loader on this line, so the handler is looked up in both spellings.
    static final ModSymbol NULL_DISPATCHER_HANDLER_INTERMEDIARY = new ModSymbol(HANDLER_OWNER,
            LithiumInjectionTargets.NULL_DISPATCHER_HANDLER,
            "(Lnet/minecraft/class_5713;Lnet/minecraft/class_6880;Lnet/minecraft/class_243;"
                    + "Lnet/minecraft/class_5712$class_7397;Lnet/minecraft/class_5713$class_7721;III)Z");

    private static final boolean APPLIES = readApplies(LithiumGameEventDispatch.class.getClassLoader());

    public static boolean applies() {
        return APPLIES;
    }

    static boolean readApplies(ClassLoader classLoader) {
        if (classLoader.getResource(HANDLER_OWNER + CLASS_SUFFIX) == null) {
            LOGGER.info("{} lithium_present=false", GATE_LABEL);
            return false;
        }

        if (!NULL_DISPATCHER_HANDLER.carriedBy(classLoader)
                && !NULL_DISPATCHER_HANDLER_INTERMEDIARY.carriedBy(classLoader)) {
            LOGGER.warn("{} lithium_present=true symbol_present=false symbol={}", GATE_LABEL,
                    NULL_DISPATCHER_HANDLER);
            return false;
        }

        try {
            boolean enabled = optionEnabled(classLoader);
            LOGGER.info("{} lithium_present=true symbol_present=true option_enabled={}", GATE_LABEL, enabled);
            return enabled;
        } catch (ReflectiveOperationException | RuntimeException | LinkageError unreadable) {
            // A read failure applies the mixin: an absent handler then fails its apply in the log, while standing down leaves the seam deaf and silent.
            LOGGER.warn("{} lithium_present=true symbol_present=true option_enabled=unreadable, applying",
                    GATE_LABEL, unreadable);
            return true;
        }
    }

    private static boolean optionEnabled(ClassLoader classLoader) throws ReflectiveOperationException {
        Class<?> plugin = Class.forName(MIXIN_PLUGIN_CLASS, true, classLoader);
        Field configField = plugin.getDeclaredField(CONFIG_FIELD);
        configField.setAccessible(true);
        Object config = configField.get(null);
        if (config == null) {
            throw new IllegalStateException(MIXIN_PLUGIN_CLASS + "." + CONFIG_FIELD + " is not loaded yet");
        }

        Object option = config.getClass().getMethod(EFFECTIVE_OPTION_METHOD, String.class).invoke(config, MIXIN_RULE);
        return option != null && (Boolean) option.getClass().getMethod(IS_ENABLED_METHOD).invoke(option);
    }

    private LithiumGameEventDispatch() {
    }
}
