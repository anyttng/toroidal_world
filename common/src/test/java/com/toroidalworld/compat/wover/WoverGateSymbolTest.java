package com.toroidalworld.compat.wover;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.toroidalworld.compat.ModSymbol;

class WoverGateSymbolTest {
    @Test
    void theGateNamesSymbolsTheCompiledAgainstWorldWeaverCarries() {
        for (ModSymbol symbol : new ModSymbol[] {
                WoverMixinPlugin.RAW_BIOME, WoverMixinPlugin.SQUARE_RAW_BIOME, WoverMixinPlugin.SQUARE_CONSTRUCTOR,
                WoverMixinPlugin.STACK_BIOME, WoverMixinPlugin.STACK_CONSTRUCTOR, WoverMixinPlugin.NETHER_INIT_MAP,
                WoverMixinPlugin.END_INIT_MAP, WoverMixinPlugin.GENERATOR_INITIALIZE}) {
            assertTrue(symbol.carriedBy(WoverGateSymbolTest.class.getClassLoader()),
                    symbol + " is gone from the WorldWeaver this compat compiles against, so its gate would refuse "
                            + "a WorldWeaver that works");
        }
    }
}
