package com.toroidalworld.compat.distanthorizons;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import com.seibel.distanthorizons.api.interfaces.override.worldGenerator.IDhApiWorldGenerator;
import com.seibel.distanthorizons.api.interfaces.world.IDhApiLevelWrapper;

public final class DhGenerators {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final String DH_PACKAGE = "com.seibel.distanthorizons.";

    private static final String REFUSAL = "Toroidal World: Distant Horizons generates the LODs of %s itself, "
            + "because only its own generator runs this world's terrain; world generator %s was refused.";

    private static final Set<String> WARNED = ConcurrentHashMap.newKeySet();

    public static void requireOwn(IDhApiLevelWrapper level, IDhApiWorldGenerator generator) {
        if (level == null || generator == null || generator.getClass().getName().startsWith(DH_PACKAGE)
                || DhShapes.of(level) == null) {
            return;
        }

        String refusal = REFUSAL.formatted(level.getDhIdentifier(), generator.getClass().getName());
        if (WARNED.add(level.getDhIdentifier() + '|' + generator.getClass().getName())) {
            LOGGER.warn(refusal);
        }

        throw new IllegalArgumentException(refusal);
    }

    private DhGenerators() {
    }
}
