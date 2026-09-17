package com.toroidalworld.compat.terrablender;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;

import com.toroidalworld.compat.terrablender.RegionLayerFold.LayerAxis;
import com.toroidalworld.core.FlatShape;
import com.toroidalworld.core.WorldFold;
import com.toroidalworld.core.WorldFolds;
import com.toroidalworld.core.WorldLoopBounds;

import net.minecraft.core.Direction;

import terrablender.worldgen.noise.Area;
import terrablender.worldgen.noise.AreaContext;
import terrablender.worldgen.noise.ZoomLayer;

class RegionLayerFoldTest {
    private static final long SEED = 710L;
    private static final int REGIONS = 7;
    private static final int MAX_CACHE = 1024;
    private static final int CONTEXT_CACHE = 25;

    private static final long INITIAL_MODIFIER = 1L;
    private static final long FUZZY_MODIFIER = 2000L;
    private static final long FIXED_ZOOM_MODIFIER = 2001L;
    private static final long SIZE_ZOOM_MODIFIER = 1001L;
    private static final int FIXED_ZOOMS = 3;

    private static final int DEFAULT_REGION_SIZE = 3;
    private static final int[] REGION_SIZES = {2, 3, 4, 6};
    private static final int[] CHUNK_WIDTHS = {32, 48, 64, 128, 750, 2500};

    private static final int SAMPLE_LINES = 5;
    private static final int SAMPLES_PER_LINE = 400;

    private static final int SWEEP_MIN_CHUNKS = 16;
    private static final int SWEEP_MAX_CHUNKS = 3000;
    private static final double MAX_STRETCH = 2.0;

    private static WorldFold torus(int chunkWidth) {
        return WorldFolds.of(FlatShape.torus(WorldLoopBounds.ofWidth(chunkWidth)));
    }

    private static int topDepth(int regionSize) {
        return 1 + FIXED_ZOOMS + regionSize;
    }

    private interface LayerFold {
        int apply(Direction.Axis axis, int depth, int coord);
    }

    private static Area regionMap(int regionSize, @Nullable RegionLayerFold fold) {
        LayerFold layerFold = fold == null ? (axis, depth, coord) -> coord : fold::apply;
        AreaContext initialContext = new AreaContext(CONTEXT_CACHE, SEED, INITIAL_MODIFIER);
        Area area = new Area((x, z) -> {
            int foldedX = layerFold.apply(Direction.Axis.X, 0, x);
            int foldedZ = layerFold.apply(Direction.Axis.Z, 0, z);
            initialContext.initRandom(foldedX, foldedZ);
            return initialContext.nextRandom(REGIONS);
        }, CONTEXT_CACHE);

        area = zoom(area, ZoomLayer.FUZZY, FUZZY_MODIFIER, 1, layerFold);
        for (int zoom = 0; zoom < FIXED_ZOOMS; zoom++) {
            area = zoom(area, ZoomLayer.NORMAL, FIXED_ZOOM_MODIFIER + zoom, 2 + zoom, layerFold);
        }

        for (int zoom = 0; zoom < regionSize; zoom++) {
            area = zoom(area, ZoomLayer.NORMAL, SIZE_ZOOM_MODIFIER + zoom, 2 + FIXED_ZOOMS + zoom, layerFold);
        }

        return area;
    }

    private static Area zoom(Area parent, ZoomLayer layer, long modifier, int depth, LayerFold layerFold) {
        AreaContext context = new AreaContext(CONTEXT_CACHE, SEED, modifier);
        return new Area((x, z) -> {
            int foldedX = layerFold.apply(Direction.Axis.X, depth, x);
            int foldedZ = layerFold.apply(Direction.Axis.Z, depth, z);
            context.initRandom(foldedX, foldedZ);
            return layer.apply(context, parent, foldedX, foldedZ);
        }, MAX_CACHE);
    }

    @Test
    void foldedMapReadsTheSameRegionOneLapAwayOnBothAxes() {
        for (int regionSize : REGION_SIZES) {
            for (int chunkWidth : CHUNK_WIDTHS) {
                WorldFold torus = torus(chunkWidth);
                RegionLayerFold fold = RegionLayerFold.of(torus, topDepth(regionSize));
                Area map = regionMap(regionSize, fold);
                LayerAxis axis = fold.x();
                int stride = Math.max(1, axis.lap() / SAMPLES_PER_LINE);
                String world = chunkWidth + " chunks, region size " + regionSize;

                for (int line = 0; line < SAMPLE_LINES; line++) {
                    int cross = axis.origin() + line * axis.lap() / SAMPLE_LINES;
                    for (int along = axis.origin() - axis.lap(); along < axis.origin() + axis.lap(); along += stride) {
                        assertEquals(map.get(along, cross), map.get(along + axis.lap(), cross),
                                "X quart " + along + " in " + world);
                        assertEquals(map.get(cross, along), map.get(cross, along + axis.lap()),
                                "Z quart " + along + " in " + world);
                    }
                }
            }
        }
    }

    @Test
    void seamStripReadsTheSameRegionOneLapAwayAtEveryQuart() {
        for (int chunkWidth : CHUNK_WIDTHS) {
            RegionLayerFold fold = RegionLayerFold.of(torus(chunkWidth), topDepth(DEFAULT_REGION_SIZE));
            Area map = regionMap(DEFAULT_REGION_SIZE, fold);
            LayerAxis axis = fold.x();
            int seam = axis.origin() + axis.lap() - axis.strip();
            for (int along = seam - axis.cell(); along < seam + axis.strip() + axis.cell(); along++) {
                assertEquals(map.get(along, 0), map.get(along - axis.lap(), 0),
                        "quart " + along + " in " + chunkWidth + " chunks");
            }
        }
    }

    @Test
    void wholeCellsInsideTheWorldKeepTerraBlendersLayout() {
        for (int chunkWidth : CHUNK_WIDTHS) {
            RegionLayerFold fold = RegionLayerFold.of(torus(chunkWidth), topDepth(DEFAULT_REGION_SIZE));
            Area folded = regionMap(DEFAULT_REGION_SIZE, fold);
            Area unfolded = regionMap(DEFAULT_REGION_SIZE, null);
            LayerAxis axis = fold.x();
            int untouchedEnd = axis.origin() + axis.interior() - 2 * axis.cell();
            int stride = Math.max(1, axis.lap() / SAMPLES_PER_LINE);

            for (int x = axis.origin(); x < untouchedEnd; x += stride) {
                for (int z = axis.origin(); z < untouchedEnd; z += axis.cell()) {
                    assertEquals(unfolded.get(x, z), folded.get(x, z),
                            "quart " + x + ", " + z + " in " + chunkWidth + " chunks");
                }
            }
        }
    }

    @Test
    void unboundedAxisIsNeverFolded() {
        WorldFold cylinder = WorldFolds.of(FlatShape.cylinder(WorldLoopBounds.ofWidth(Direction.Axis.X, 64)));
        RegionLayerFold fold = RegionLayerFold.of(cylinder, topDepth(DEFAULT_REGION_SIZE));
        for (int depth = 0; depth <= topDepth(DEFAULT_REGION_SIZE); depth++) {
            for (int coord = -5000; coord < 5000; coord += 37) {
                assertEquals(coord, fold.apply(Direction.Axis.Z, depth, coord));
            }
        }
    }

    @Test
    void virtualLapIsWholeCellsAndStretchesTheSeamStripAtMostTwofold() {
        for (int regionSize : REGION_SIZES) {
            for (int chunkWidth = SWEEP_MIN_CHUNKS; chunkWidth <= SWEEP_MAX_CHUNKS; chunkWidth++) {
                LayerAxis axis = RegionLayerFold.of(torus(chunkWidth), topDepth(regionSize)).x();
                String world = chunkWidth + " chunks, region size " + regionSize;
                assertEquals(0, axis.virtualLap() % axis.cell(), world);
                assertEquals(0, axis.origin() % axis.cell(), world);
                if (axis.strip() == 0 || axis.interior() == 0) {
                    continue;
                }

                int stretchedStart = axis.stripCells() > 0 ? axis.interior() : axis.interior() - axis.cell();
                double ratio = (double) (axis.virtualLap() - stretchedStart) / (axis.lap() - stretchedStart);
                assertTrue(ratio <= MAX_STRETCH && ratio >= 1 / MAX_STRETCH, world + ": stretch " + ratio);
            }
        }
    }
}
