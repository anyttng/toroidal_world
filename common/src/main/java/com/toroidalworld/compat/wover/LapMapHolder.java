package com.toroidalworld.compat.wover;

import com.toroidalworld.core.WorldFold;

public interface LapMapHolder<T> {
    LapMap<T> toroidal$lapMap(WorldFold fold);
}
