package com.toroidalworld.accessors;

import org.jspecify.annotations.Nullable;

import com.toroidalworld.engine.noise.AquiferCells;

public interface AquiferCellsHolder {
    @Nullable AquiferCells toroidal$aquiferCells();

    void toroidal$aquiferCells(AquiferCells cells);
}
