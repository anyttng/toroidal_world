package com.toroidalworld.compat.mekanism;

import com.toroidalworld.core.WorldFold;

public interface MultiblockBoundsFrame {
    WorldFold toroidal$fold();

    void toroidal$setFold(WorldFold fold);
}
