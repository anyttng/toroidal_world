package com.toroidalworld.accessors;

import org.jspecify.annotations.Nullable;

import com.toroidalworld.engine.noise.AquiferCells;
import com.toroidalworld.engine.noise.FoldedCompileContext;

public interface FoldedRandomState {
    @Nullable FoldedCompileContext toroidal$compileContext();

    @Nullable AquiferCells toroidal$aquiferCells();

    void toroidal$aquiferCells(AquiferCells cells);
}
