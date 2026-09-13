package com.toroidalworld.engine.net;

import java.util.List;
import java.util.Map;

import com.toroidalworld.ToroidalWorld;

import io.netty.buffer.ByteBuf;

import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record BlockEntityPositionsPayload(Map<ResourceLocation, List<TagPositions.TagPosition>> blockEntities)
        implements CustomPacketPayload {
    public static final Type<BlockEntityPositionsPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(ToroidalWorld.MODID, "block_entity_positions"));

    public static final StreamCodec<ByteBuf, BlockEntityPositionsPayload> STREAM_CODEC =
            ByteBufCodecs.fromCodec(PositionRows.SUBJECTS_CODEC)
                    .map(BlockEntityPositionsPayload::new, BlockEntityPositionsPayload::blockEntities);

    @Override
    public Type<BlockEntityPositionsPayload> type() {
        return TYPE;
    }
}
