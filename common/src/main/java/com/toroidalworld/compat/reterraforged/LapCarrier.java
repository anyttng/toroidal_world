package com.toroidalworld.compat.reterraforged;

import org.jspecify.annotations.Nullable;

import com.toroidalworld.core.WorldFold;

public interface LapCarrier {
    @Nullable WorldFold toroidal$fold();

    void toroidal$carryFold(WorldFold fold);

    double toroidal$climateCompression();
}
