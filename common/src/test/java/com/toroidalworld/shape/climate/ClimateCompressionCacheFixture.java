package com.toroidalworld.shape.climate;

import org.jspecify.annotations.Nullable;

import com.toroidalworld.accessors.ClimateCompressionCache;
import com.toroidalworld.shape.climate.ClimateCompression.Resolved;

final class ClimateCompressionCacheFixture {
    static final class Storing implements ClimateCompressionCache {
        private @Nullable Resolved resolved;
        int stores;

        @Override
        public @Nullable Resolved toroidal$climateCompression() {
            return this.resolved;
        }

        @Override
        public void toroidal$climateCompression(Resolved resolved) {
            this.resolved = resolved;
            this.stores++;
        }
    }

    private ClimateCompressionCacheFixture() {
    }
}
