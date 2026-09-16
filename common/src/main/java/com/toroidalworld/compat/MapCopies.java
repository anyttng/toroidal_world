package com.toroidalworld.compat;

import com.toroidalworld.platform.Platforms;

public enum MapCopies {
    REPEATED,
    SINGLE;

    public static MapCopies current() {
        return Platforms.get().mapCopies();
    }
}
