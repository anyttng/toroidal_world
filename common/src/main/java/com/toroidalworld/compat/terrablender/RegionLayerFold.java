package com.toroidalworld.compat.terrablender;

import org.jspecify.annotations.Nullable;

import com.toroidalworld.core.WorldFold;
import com.toroidalworld.core.WrapDomain;

import net.minecraft.core.Direction;
import net.minecraft.core.QuartPos;

public record RegionLayerFold(WorldFold transformer, int topDepth, LayerAxis x, LayerAxis z) {
    public static RegionLayerFold of(WorldFold transformer, int topDepth) {
        return new RegionLayerFold(transformer, topDepth,
                LayerAxis.of(transformer.blockDomain(Direction.Axis.X), topDepth),
                LayerAxis.of(transformer.blockDomain(Direction.Axis.Z), topDepth));
    }

    static RegionLayerFold resolve(@Nullable RegionLayerFold cached, WorldFold transformer, int topDepth) {
        return cached != null && cached.transformer == transformer && cached.topDepth == topDepth
                ? cached
                : of(transformer, topDepth);
    }

    public int apply(Direction.Axis axis, int depth, int coord) {
        LayerAxis layerAxis = axis == Direction.Axis.X ? this.x : this.z;
        return depth == this.topDepth
                ? layerAxis.toVirtual(coord)
                : layerAxis.fold(coord, this.topDepth - depth);
    }

    public record LayerAxis(boolean loops, int lap, int origin, int interior, int strip, int stripCells,
            int cell, int virtualLap) {
        static final LayerAxis UNBOUNDED = new LayerAxis(false, 0, 0, 0, 0, 0, 1, 0);

        static LayerAxis of(WrapDomain blocks, int topDepth) {
            if (!blocks.loops()) {
                return UNBOUNDED;
            }

            int cell = 1 << topDepth;
            int lap = QuartPos.fromBlock(blocks.domainLength);
            int min = QuartPos.fromBlock(blocks.lowerBound);
            int origin = Math.ceilDiv(min, cell) * cell;
            int interior = Math.max(0, Math.floorDiv(min + lap, cell) * cell - origin);
            int strip = lap - interior;
            int stripCells = strip == 0 ? 0 : Math.max(interior == 0 ? 1 : 0, (strip + cell / 2) / cell);
            int virtualLap = interior + stripCells * cell;
            return new LayerAxis(true, lap, origin, interior, strip, stripCells, cell, virtualLap);
        }

        int toVirtual(int quart) {
            if (!this.loops) {
                return quart;
            }

            int offset = Math.floorMod(quart - this.origin, this.lap);
            if (this.strip == 0) {
                return this.origin + offset;
            }

            int stretchedStart = this.stripCells > 0 ? this.interior : this.interior - this.cell;
            if (offset < stretchedStart) {
                return this.origin + offset;
            }

            long rawSpan = this.lap - stretchedStart;
            long virtualSpan = this.virtualLap - stretchedStart;
            return this.origin + stretchedStart + (int) ((offset - stretchedStart) * virtualSpan / rawSpan);
        }

        int fold(int coord, int shift) {
            if (!this.loops) {
                return coord;
            }

            int start = this.origin >> shift;
            return start + Math.floorMod(coord - start, this.virtualLap >> shift);
        }
    }
}
