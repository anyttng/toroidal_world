package com.toroidalworld.compat.electroenergetics;

import com.mojang.logging.LogUtils;
import com.toroidalworld.compat.ModPresence;

public final class ElectroEnergeticsMod {
    private static final ModPresence GATE = ModPresence.of(LogUtils.getLogger(),
            "com/george_vi/electroenergetics/CreateElectroEnergetics.class",
            "[electroenergetics-compat] gate electroenergetics_present");

    public static boolean present() {
        return GATE.present();
    }

    private ElectroEnergeticsMod() {
    }
}
