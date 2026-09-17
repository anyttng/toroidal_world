package com.toroidalworld.accessors;

import org.jspecify.annotations.Nullable;

import com.toroidalworld.engine.gen.AddedStructureStarts;

public interface AddedStartsHolder {
    @Nullable AddedStructureStarts toroidal$addedStarts();

    void toroidal$addedStarts(AddedStructureStarts starts);
}
