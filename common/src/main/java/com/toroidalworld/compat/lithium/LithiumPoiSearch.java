package com.toroidalworld.compat.lithium;

public final class LithiumPoiSearch {
    private static final boolean APPLIES = new LithiumOptionGate("[lithium-compat] gate ai_poi",
            LithiumInjectionTargets.POI_MANAGER_MIXIN, LithiumInjectionTargets.CLOSEST_BATCH,
            "(Ljava/util/function/Predicate;Ljava/util/function/Predicate;Lnet/minecraft/core/BlockPos;I"
                    + "Lnet/minecraft/world/entity/ai/village/poi/PoiManager$Occupancy;J)Ljava/util/Collection;")
            .read(LithiumPoiSearch.class.getClassLoader());

    public static boolean applies() {
        return APPLIES;
    }

    private LithiumPoiSearch() {
    }
}
