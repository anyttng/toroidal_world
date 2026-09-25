package com.toroidalworld.engine.fold;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import com.toroidalworld.core.CoordinateConstants;
import com.toroidalworld.core.WorldFold;
import com.toroidalworld.core.WorldFold.Folded;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;

public final class SeamEntitySearch {
    public static AABB sectionReach(AABB box) {
        return box.inflate(CoordinateConstants.ENTITY_SECTION_SEARCH_GRACE, 0.0,
                CoordinateConstants.ENTITY_SECTION_SEARCH_GRACE);
    }

    public static boolean crossesSeam(WorldFold fold, AABB reach) {
        return fold.isWrapped() && fold.crossesBounds(reach);
    }

    public static <E extends Entity> List<E> collect(WorldFold fold, AABB box, Function<AABB, List<E>> walk) {
        AABB reach = sectionReach(box);
        if (!crossesSeam(fold, reach)) {
            return walk.apply(box);
        }

        Set<Entity> seen = Collections.newSetFromMap(new IdentityHashMap<>());
        List<E> found = new ArrayList<>();
        for (Folded<AABB> piece : fold.split(reach)) {
            for (E entity : walk.apply(piece.value())) {
                if (fold.boxesOverlap(box, entity.getBoundingBox()) && seen.add(entity)) {
                    found.add(entity);
                }
            }
        }

        return found;
    }

    private SeamEntitySearch() {
    }
}
