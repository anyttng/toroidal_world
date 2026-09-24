package com.toroidalworld.compat.wover;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jspecify.annotations.Nullable;

import com.toroidalworld.core.WorldFold;

import net.minecraft.core.Direction;
import net.minecraft.world.level.ChunkPos;

public abstract class LapMap<T> {
    interface ChunkSource<T> {
        LapChunk<T> chunk(int kx, int kz);
    }

    static final int CACHE_LIMIT = 127;

    private final WorldFold fold;

    private final double factor;

    private final Map<Long, LapChunk<T>> chunks = new ConcurrentHashMap<>();

    private volatile @Nullable ChunkSource<T> shared;

    LapMap(WorldFold fold, double factor) {
        this.fold = fold;
        this.factor = factor;
    }

    public boolean covers(WorldFold fold) {
        return this.fold == fold;
    }

    public double factor() {
        return this.factor;
    }

    public abstract T biomeAt(double blockX, double blockZ);

    abstract LapAxis axis(Direction.Axis axis);

    abstract LapChunk<T> buildChunk(int kx, int kz);

    void shareChunks(ChunkSource<T> source) {
        this.shared = source;
    }

    boolean bounded() {
        return axis(Direction.Axis.X).loops() && axis(Direction.Axis.Z).loops();
    }

    LapChunk<T> chunk(int kx, int kz) {
        ChunkSource<T> source = this.shared;
        if (source != null) {
            return source.chunk(kx, kz);
        }

        if (!bounded() && this.chunks.size() > CACHE_LIMIT) {
            this.chunks.clear();
        }

        return this.chunks.computeIfAbsent(ChunkPos.asLong(kx, kz), key -> buildChunk(kx, kz));
    }
}
