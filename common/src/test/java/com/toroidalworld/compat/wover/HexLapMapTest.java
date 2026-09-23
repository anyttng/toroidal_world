package com.toroidalworld.compat.wover;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.toroidalworld.core.FlatShape;
import com.toroidalworld.core.WorldFold;
import com.toroidalworld.core.WorldFolds;
import com.toroidalworld.core.WorldLoopBounds;
import com.toroidalworld.core.WrapDomain;
import com.toroidalworld.engine.noise.ClimateScaleCompression;

import net.minecraft.core.Direction;

class HexLapMapTest {
    private static final float DEFAULT_SCALE = 256 / 8.0F;

    private static final double FACTOR = 4.0;

    private static final int SEED = 0x900;

    private static final int PALETTE = 16;

    private static final int MIN_STRIDE_BLOCKS = 3;

    private static final int SAMPLES_PER_LAP = 256;

    private static final int SEAM_STRIP_BLOCKS = 96;

    private static final HexLapMap.Picker<Integer> PICKER = new HexLapMap.Picker<>(
            random -> random.nextInt(PALETTE), (biome, random) -> biome);

    private static HexLapMap<Integer> map(WorldFold fold, float scale) {
        return map(fold, scale, ClimateScaleCompression.NO_COMPRESSION);
    }

    private static HexLapMap<Integer> map(WorldFold fold, float scale, double factor) {
        return new HexLapMap<>(fold, scale, factor, SEED, PICKER);
    }

    @Test
    void aTorusNarrowerThanOneBlockReadsTheSameBiomeOneLapAwayOnBothAxes() {
        assertPeriodic(WorldFolds.of(FlatShape.torus(new WorldLoopBounds(-8, 8, -8, 8))), DEFAULT_SCALE);
    }

    @Test
    void aTorusOfSeveralBlocksReadsTheSameBiomeOneLapAwayOnBothAxes() {
        assertPeriodic(WorldFolds.of(FlatShape.torus(new WorldLoopBounds(-128, 128, -128, 128))), DEFAULT_SCALE);
    }

    @Test
    void aTorusWhoseLapSplitsIntoUnevenBlocksStillRepeats() {
        assertPeriodic(WorldFolds.of(FlatShape.torus(new WorldLoopBounds(-75, 75, -41, 41))), DEFAULT_SCALE);
    }

    @Test
    void aCylinderRepeatsOnItsLoopedAxisAndKeepsTheOtherOpen() {
        WorldFold cylinder = WorldFolds.of(FlatShape.cylinder(WorldLoopBounds.ofWidth(Direction.Axis.X, 256)));
        HexLapMap<Integer> map = map(cylinder, DEFAULT_SCALE);
        int lap = cylinder.blockDomain(Direction.Axis.X).domainLength;
        for (int z = -4096; z < 4096; z += 37) {
            for (int x = -2 * lap; x < 2 * lap; x += 101) {
                assertEquals(map.biomeAt(x, z), map.biomeAt(x + lap, z), "x " + x + " z " + z);
            }
        }

        assertFalse(map.axis(Direction.Axis.Z).loops(), "the open axis was given a lap");
    }

    @Test
    void aLatticeKeepsWorldWeaversCellOrShrinksItOnlyToFitAWholeCount() {
        int[] chunkWidths = {1, 3, 16, 41, 75, 128, 256, 1875};
        float[] scales = {DEFAULT_SCALE, 4.0F, 128.0F};
        for (int width : chunkWidths) {
            WorldFold fold = WorldFolds.of(FlatShape.torus(WorldLoopBounds.ofWidth(width)));
            for (float scale : scales) {
                HexLapMap<Integer> map = map(fold, scale);
                assertCell(map.axis(Direction.Axis.X), scale / HexLapMap.RAD_INNER, 1);
                assertCell(map.axis(Direction.Axis.Z), scale, 2);
            }
        }
    }

    @Test
    void theCompactBiomesFactorShrinksTheCellOnBothAxes() {
        double[] factors = {2.0, 4.0, 1.56};
        WorldFold torus = WorldFolds.of(FlatShape.torus(WorldLoopBounds.ofWidth(256)));
        for (double factor : factors) {
            HexLapMap<Integer> map = map(torus, DEFAULT_SCALE, factor);
            assertCell(map.axis(Direction.Axis.X), DEFAULT_SCALE / factor / HexLapMap.RAD_INNER, 1);
            assertCell(map.axis(Direction.Axis.Z), DEFAULT_SCALE / factor, 2);
        }

        WorldFold cylinder = WorldFolds.of(FlatShape.cylinder(WorldLoopBounds.ofWidth(Direction.Axis.X, 256)));
        HexLapMap<Integer> compact = map(cylinder, DEFAULT_SCALE, FACTOR);
        assertEquals(DEFAULT_SCALE / FACTOR, compact.axis(Direction.Axis.Z).cellBlocks(), 1.0E-9,
                "the cylinder's open axis kept WorldWeaver's cell");
        assertCell(compact.axis(Direction.Axis.X), DEFAULT_SCALE / FACTOR / HexLapMap.RAD_INNER, 1);
        assertPeriodic(cylinder, DEFAULT_SCALE, FACTOR);
    }

    @Test
    void aLapHeldInOneBlockSeedsTwoLinesAlongEachLoopedAxis() {
        assertEquals(2, HexLapChunk.seedLines(7, true));
        assertEquals(2, HexLapChunk.seedLines(2, true));
        assertEquals(1, HexLapChunk.seedLines(1, true));
        assertEquals(4, HexLapChunk.seedLines(32, true));
        assertEquals(4, HexLapChunk.seedLines(32, false));
    }

    @Test
    void theSameFoldAndSeedLayTheSameMap() {
        WorldFold fold = WorldFolds.of(FlatShape.torus(new WorldLoopBounds(-40, 40, -40, 40)));
        HexLapMap<Integer> first = map(fold, DEFAULT_SCALE);
        HexLapMap<Integer> second = map(fold, DEFAULT_SCALE);
        Set<Integer> seen = new HashSet<>();
        for (int x = -640; x < 640; x += 11) {
            for (int z = -640; z < 640; z += 13) {
                int biome = first.biomeAt(x, z);
                seen.add(biome);
                assertEquals(biome, second.biomeAt(x, z), "x " + x + " z " + z);
            }
        }

        assertTrue(seen.size() > 1, "the map laid one biome over the whole lap: " + seen);
    }

    private static void assertCell(HexLapMap.LapAxis axis, double ownCellBlocks, int countStep) {
        double lap = axis.blocks().domainLength;
        double cell = axis.latticeBlocks();
        assertTrue(cell <= ownCellBlocks + 1.0E-9,
                "cell " + cell + " is wider than WorldWeaver's " + ownCellBlocks + " on a " + lap + "-block lap");
        int fewer = axis.cells() - countStep;
        assertTrue(fewer < 1 || lap / fewer > ownCellBlocks,
                axis.cells() + " cells on a " + lap + "-block lap: " + fewer + " would still fit " + ownCellBlocks);
    }

    private static void assertPeriodic(WorldFold fold, float scale) {
        assertPeriodic(fold, scale, ClimateScaleCompression.NO_COMPRESSION);
    }

    private static void assertPeriodic(WorldFold fold, float scale, double factor) {
        HexLapMap<Integer> map = map(fold, scale, factor);
        WrapDomain xDomain = fold.blockDomain(Direction.Axis.X);
        WrapDomain zDomain = fold.blockDomain(Direction.Axis.Z);
        int xLap = xDomain.domainLength;
        int zLap = zDomain.domainLength;
        int xStride = Math.max(MIN_STRIDE_BLOCKS, xLap / SAMPLES_PER_LAP);
        int zStride = Math.max(MIN_STRIDE_BLOCKS, zLap / SAMPLES_PER_LAP);
        Set<Integer> seen = new HashSet<>();
        for (int x = xDomain.lowerBound - SEAM_STRIP_BLOCKS; x < xDomain.upperBound; x += xStride) {
            for (int z = zDomain.lowerBound - SEAM_STRIP_BLOCKS; z < zDomain.upperBound; z += zStride) {
                int biome = map.biomeAt(x, z);
                seen.add(biome);
                assertEquals(biome, map.biomeAt(x + xLap, z), "x " + x + " z " + z + " one lap away on X");
                assertEquals(biome, map.biomeAt(x, z + zLap), "x " + x + " z " + z + " one lap away on Z");
            }
        }

        assertTrue(seen.size() > 1, "the map laid one biome over the whole lap: " + seen);
    }
}
