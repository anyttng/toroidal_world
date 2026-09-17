package com.toroidalworld.compat.mekanism;

import com.toroidalworld.core.WorldFold;

public interface RadiationBoxFrame {
    void toroidal$setFold(WorldFold fold);

    int toroidal$minX();

    int toroidal$minZ();

    int toroidal$maxX();

    int toroidal$maxZ();
}
