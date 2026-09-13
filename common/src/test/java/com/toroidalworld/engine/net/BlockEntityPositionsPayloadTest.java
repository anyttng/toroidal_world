package com.toroidalworld.engine.net;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.toroidalworld.engine.net.TagPositions.Nesting;
import com.toroidalworld.engine.net.TagPositions.PositionShape;
import com.toroidalworld.engine.net.TagPositions.TagPosition;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import net.minecraft.resources.Identifier;

class BlockEntityPositionsPayloadTest {
    private static final Identifier HOLDER_ID = Identifier.fromNamespaceAndPath("cject", "holder");
    private static final Identifier RELAY_ID = Identifier.fromNamespaceAndPath("pack", "relay");

    private static final List<TagPosition> HOLDER_POSITIONS = List.of(
            new TagPosition("Goal", PositionShape.PACKED_LONG),
            new TagPosition("Partner", PositionShape.BLOCK_POS),
            new TagPosition(Nesting.COMPOUND, "Printer", "Anchor", PositionShape.BLOCK_POS),
            new TagPosition(Nesting.COMPOUND, "Printer", "Cursor", PositionShape.VEC3_LIST),
            new TagPosition(Nesting.EACH_OF_LIST, "FlyingBlocks", "Target", PositionShape.BLOCK_POS));

    private static final List<TagPosition> RELAY_POSITIONS =
            List.of(new TagPosition(Nesting.EACH_OF_LIST, "Queue", "Destination", PositionShape.VEC3_LIST));

    @Test
    void everyAddressFormCrossesTheWireAndDrainsTheBuffer() {
        ByteBuf buffer = Unpooled.buffer();
        BlockEntityPositionsPayload.STREAM_CODEC.encode(buffer,
                new BlockEntityPositionsPayload(Map.of(HOLDER_ID, HOLDER_POSITIONS, RELAY_ID, RELAY_POSITIONS)));

        BlockEntityPositionsPayload decoded = BlockEntityPositionsPayload.STREAM_CODEC.decode(buffer);

        assertEquals(Set.of(HOLDER_ID, RELAY_ID), decoded.blockEntities().keySet());
        assertEquals(Set.copyOf(HOLDER_POSITIONS), Set.copyOf(decoded.blockEntities().get(HOLDER_ID)));
        assertEquals(Set.copyOf(RELAY_POSITIONS), Set.copyOf(decoded.blockEntities().get(RELAY_ID)));
        assertEquals(0, buffer.readableBytes());
    }

    @Test
    void noRowsCrossTheWireAsNoRows() {
        ByteBuf buffer = Unpooled.buffer();
        BlockEntityPositionsPayload.STREAM_CODEC.encode(buffer, new BlockEntityPositionsPayload(Map.of()));

        assertEquals(Map.of(), BlockEntityPositionsPayload.STREAM_CODEC.decode(buffer).blockEntities());
    }
}
