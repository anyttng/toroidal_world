package com.toroidalworld.shape;

import com.toroidalworld.shape.torus.CoastFieldLift;
import com.toroidalworld.shape.torus.NetherComplexStarts;

public final class GenerationHookSetup {

    public static void registerAll() {
        CoastFieldLift.register();
        NetherComplexStarts.register();
    }

    private GenerationHookSetup() {
    }
}
