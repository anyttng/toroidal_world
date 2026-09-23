package com.toroidalworld.compat.wover;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Function;

import com.toroidalworld.core.WorldFold;
import com.toroidalworld.core.WrapDomain;

import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.WorldgenRandom;

public final class HexLapMap<T> {
    public record Picker<T>(Function<WorldgenRandom, T> pick, BiFunction<T, WorldgenRandom, T> subBiome) {
    }

    static final int CHUNK_SIDE = 32;

    static final float RAD_INNER = (float) Math.sqrt(3.0) * 0.5F;

    private static final float COEF = 0.25F * (float) Math.sqrt(3.0);

    private static final float COEF_HALF = COEF * 0.5F;

    private static final float HEXAGON_STRETCH = 1.1555F;

    private static final float HEXAGON_EDGE_WEIGHT = 0.25F;

    private static final float ROW_CORE = 0.3333F;

    private static final double ROW_OFFSET = 0.5;

    private static final double CELL_CENTRE = 0.5;

    private static final double WARP = 0.2;

    private static final int MAX_WARP_OCTAVES = 5;

    private static final int CRENEL_SHIFT = 2;

    private static final int CACHE_LIMIT = 127;

    private static final int SEED_X_FACTOR = 374761393;

    private static final int SEED_Z_FACTOR = 668265263;

    private static final int SEED_MIX_SHIFT = 13;

    private static final int SEED_MIX_FACTOR = 1274126177;

    private static final int SEED_FINAL_SHIFT = 16;

    private final WorldFold fold;

    private final int seed;

    private final Picker<T> picker;

    private final LapAxis x;

    private final LapAxis z;

    private final OpenSimplexStandIn[] warps;

    private final int warpOctaves;

    private final Map<Long, HexLapChunk<T>> chunks = new ConcurrentHashMap<>();

    public HexLapMap(WorldFold fold, float scale, int seed, Picker<T> picker) {
        this.fold = fold;
        this.seed = seed;
        this.picker = picker;
        this.x = LapAxis.of(fold.blockDomain(Direction.Axis.X), scale / RAD_INNER, false);
        this.z = LapAxis.of(fold.blockDomain(Direction.Axis.Z), scale, true);
        this.warps = new OpenSimplexStandIn[] {
                new OpenSimplexStandIn((long) seed << 1), new OpenSimplexStandIn(((long) seed << 1) | 1L)};
        this.warpOctaves = (int) Math.min(Math.ceil(Math.log(scale) / Math.log(2.0)), MAX_WARP_OCTAVES);
    }

    public boolean covers(WorldFold fold) {
        return this.fold == fold;
    }

    public T biomeAt(double blockX, double blockZ) {
        double px = this.x.toLattice(blockX);
        double pz = this.z.toLattice(blockZ);
        double dx = warp(px, pz, 0, this.x.period(), this.z.period()) * WARP;
        double dz = warp(pz, px, 1, this.z.period(), this.x.period()) * WARP;
        px += dx;
        pz += dz;

        int cellZ = Mth.floor(pz);
        boolean offset = (cellZ & 1) == 1;
        if (offset) {
            px += ROW_OFFSET;
        }

        int cellX = Mth.floor(px);
        float pointX = (float) (px - cellX - CELL_CENTRE);
        float pointZ = (float) (pz - cellZ - CELL_CENTRE);
        if (Math.abs(pointZ) < ROW_CORE || insideHexagon(pointZ * RAD_INNER, pointX)) {
            return cellBiome(cellX, cellZ);
        }

        cellX = pointX < 0.0F ? (offset ? cellX - 1 : cellX) : (offset ? cellX : cellX + 1);
        cellZ = pointZ < 0.0F ? cellZ - 1 : cellZ + 1;
        return cellBiome(cellX, cellZ);
    }

    LapAxis axis(Direction.Axis axis) {
        return axis == Direction.Axis.X ? this.x : this.z;
    }

    private double warp(double a, double b, int state, double aPeriod, double bPeriod) {
        double result = 0.0;
        for (int octave = 1; octave <= this.warpOctaves; octave++) {
            result += this.warps[state].eval(a * octave, b * octave, aPeriod * octave, bPeriod * octave) / octave;
            state ^= 1;
        }

        return result;
    }

    private T cellBiome(int cellX, int cellZ) {
        int gx = this.x.wrapCell(cellX);
        int gz = this.z.wrapCell(cellZ);
        int kx = this.x.chunkOf(gx);
        int kz = this.z.chunkOf(gz);
        int lx = gx - this.x.startOf(kx);
        int lz = gz - this.z.startOf(kz);
        if (((gz >> CRENEL_SHIFT) & 1) == 0 && this.x.isBorder(kx, lx)) {
            kx = this.x.next(kx);
            lx = 0;
        } else if (((gx >> CRENEL_SHIFT) & 1) == 0 && this.z.isBorder(kz, lz)) {
            kz = this.z.next(kz);
            lz = 0;
        }

        return chunk(kx, kz).get(lx, lz);
    }

    private HexLapChunk<T> chunk(int kx, int kz) {
        if (!(this.x.loops() && this.z.loops()) && this.chunks.size() > CACHE_LIMIT) {
            this.chunks.clear();
        }

        return this.chunks.computeIfAbsent(ChunkPos.asLong(kx, kz), key -> new HexLapChunk<>(
                this.x.sideOf(kx), this.z.sideOf(kz), this.x.wraps(), this.z.wraps(), this.z.startOf(kz),
                new WorldgenRandom(RandomSource.create(chunkSeed(this.seed, kx, kz))), this.picker));
    }

    private static int chunkSeed(int seed, int kx, int kz) {
        int hash = seed + kx * SEED_X_FACTOR + kz * SEED_Z_FACTOR;
        hash = (hash ^ hash >> SEED_MIX_SHIFT) * SEED_MIX_FACTOR;
        return hash ^ hash >> SEED_FINAL_SHIFT;
    }

    private static boolean insideHexagon(float x, float z) {
        double dx = Math.abs(x) / HEXAGON_STRETCH;
        double dz = Math.abs(z) / HEXAGON_STRETCH;
        return dz <= COEF && COEF * dx + HEXAGON_EDGE_WEIGHT * dz <= COEF_HALF;
    }

    record LapAxis(WrapDomain blocks, double cellBlocks, int cells, int chunks) {
        private static final double CELL_EPSILON = 1.0E-9;

        static LapAxis of(WrapDomain blocks, double cellBlocks, boolean evenCells) {
            if (!blocks.loops()) {
                return new LapAxis(blocks, cellBlocks, 0, 0);
            }

            int cells = Math.max(1, (int) Math.ceil(blocks.domainLength / cellBlocks - CELL_EPSILON));
            if (evenCells && (cells & 1) == 1) {
                cells++;
            }

            return new LapAxis(blocks, cellBlocks, cells, Math.ceilDiv(cells, CHUNK_SIDE));
        }

        boolean loops() {
            return this.blocks.loops();
        }

        boolean wraps() {
            return this.chunks == 1;
        }

        double toLattice(double block) {
            return loops()
                    ? (this.blocks.wrap(block) - this.blocks.lowerBound) * this.cells / this.blocks.domainLength
                    : block / this.cellBlocks;
        }

        double period() {
            return loops() ? this.cells : OpenSimplexStandIn.UNBOUNDED;
        }

        double latticeBlocks() {
            return loops() ? (double) this.blocks.domainLength / this.cells : this.cellBlocks;
        }

        int wrapCell(int cell) {
            return loops() ? Math.floorMod(cell, this.cells) : cell;
        }

        int chunkOf(int cell) {
            if (!loops()) {
                return Math.floorDiv(cell, CHUNK_SIDE);
            }

            int chunk = (int) ((long) cell * this.chunks / this.cells);
            while (startOf(chunk + 1) <= cell) {
                chunk++;
            }

            while (startOf(chunk) > cell) {
                chunk--;
            }

            return chunk;
        }

        int startOf(int chunk) {
            return loops() ? (int) ((long) this.cells * chunk / this.chunks) : chunk * CHUNK_SIDE;
        }

        int sideOf(int chunk) {
            return startOf(chunk + 1) - startOf(chunk);
        }

        boolean isBorder(int chunk, int local) {
            return !wraps() && local == sideOf(chunk) - 1;
        }

        int next(int chunk) {
            return loops() ? (chunk + 1) % this.chunks : chunk + 1;
        }
    }
}
