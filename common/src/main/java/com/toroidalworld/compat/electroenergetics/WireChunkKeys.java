package com.toroidalworld.compat.electroenergetics;

import java.util.NoSuchElementException;

import org.jspecify.annotations.Nullable;

import com.toroidalworld.core.WorldFold;

import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public final class WireChunkKeys implements LongIterator {
    private static final int PLANE_Y = 0;

    private final LongIterator raw;
    private final WorldFold fold;
    private final @Nullable LongSet seen;
    private long next;
    private boolean ready;

    private WireChunkKeys(LongIterator raw, WorldFold fold, @Nullable LongSet seen) {
        this.raw = raw;
        this.fold = fold;
        this.seen = seen;
    }

    public static LongIterator of(LongIterator raw, WorldFold fold, int minX, int maxX, int minZ, int maxZ) {
        BoundingBox blocks = new BoundingBox(SectionPos.sectionToBlockCoord(minX), PLANE_Y,
                SectionPos.sectionToBlockCoord(minZ), SectionPos.sectionToBlockCoord(maxX) - 1, PLANE_Y,
                SectionPos.sectionToBlockCoord(maxZ) - 1);
        return new WireChunkKeys(raw, fold, fold.foldsOntoItself(blocks) ? new LongOpenHashSet() : null);
    }

    @Override
    public boolean hasNext() {
        while (!this.ready && this.raw.hasNext()) {
            long chunk = this.fold.foldChunkKey(this.raw.nextLong());
            if (this.seen == null || this.seen.add(chunk)) {
                this.next = chunk;
                this.ready = true;
            }
        }

        return this.ready;
    }

    @Override
    public long nextLong() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }

        this.ready = false;
        return this.next;
    }
}
