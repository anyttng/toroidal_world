package com.toroidalworld.engine.net;

import static com.toroidalworld.engine.net.PacketTranslatorFixture.CLIENT_BLOCK;
import static com.toroidalworld.engine.net.PacketTranslatorFixture.SERVER_BLOCK;
import static com.toroidalworld.engine.net.PacketTranslatorFixture.context;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;

import io.netty.buffer.Unpooled;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.decoration.painting.Painting;
import net.minecraft.world.entity.vehicle.boat.Boat;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.payload.AdvancedAddEntityPayload;

class SpawnBufferTranslationTest {
    private static final int ENTITY_ID = 21;
    private static final int MISSING_ENTITY_ID = 22;

    private static final String OFFSET_KEY = "From";
    private static final String BLOCK_POS_KEY = "block_pos";

    private static final byte TAIL_BYTE = 7;

    private static final Class<?> BLOCK_ATTACHED_TYPE = Painting.class;
    private static final Class<?> UNREGISTERED_TYPE = Boat.class;

    private static TranslationContext contextHolding(Class<?> entityClass) {
        return context(entityId -> false, entityId -> null,
                entityId -> entityId == ENTITY_ID ? entityClass : null);
    }

    private static ListTag doubleList(double x, double y, double z) {
        ListTag list = new ListTag();
        list.add(DoubleTag.valueOf(x));
        list.add(DoubleTag.valueOf(y));
        list.add(DoubleTag.valueOf(z));
        return list;
    }

    private static CompoundTag attachmentData(BlockPos attachment) {
        CompoundTag tag = new CompoundTag();
        tag.store(BLOCK_POS_KEY, BlockPos.CODEC, attachment);
        tag.put(OFFSET_KEY, doubleList(-1.0, 0.0, -1.0));
        return tag;
    }

    private static AdvancedAddEntityPayload payloadOf(int entityId, CompoundTag tag, boolean withTail) {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        buffer.writeNbt(tag);
        if (withTail) {
            buffer.writeByte(TAIL_BYTE);
        }

        byte[] bytes = new byte[buffer.readableBytes()];
        buffer.readBytes(bytes);
        return new AdvancedAddEntityPayload(entityId, bytes);
    }

    private static FriendlyByteBuf bufferOf(CustomPacketPayload payload) {
        return new FriendlyByteBuf(
                Unpooled.wrappedBuffer(((AdvancedAddEntityPayload) payload).customPayload()));
    }

    private static Vec3 vec3In(CompoundTag tag, String key) {
        ListTag list = tag.getListOrEmpty(key);
        return new Vec3(list.getDoubleOr(0, Double.NaN), list.getDoubleOr(1, Double.NaN),
                list.getDoubleOr(2, Double.NaN));
    }

    @Test
    void anAttachmentBlockAWorldAwayReachesTheViewerOnItsOwnCopy() {
        CustomPacketPayload seated = SpawnBufferTranslation.seated(
                payloadOf(ENTITY_ID, attachmentData(SERVER_BLOCK), false), contextHolding(BLOCK_ATTACHED_TYPE));

        CompoundTag tag = bufferOf(seated).readNbt();
        assertEquals(CLIENT_BLOCK, tag.read(BLOCK_POS_KEY, BlockPos.CODEC).orElseThrow());
    }

    @Test
    void anAttachmentBlockAlreadyInTheViewersFrameIsThePayloadBack() {
        AdvancedAddEntityPayload payload = payloadOf(ENTITY_ID, attachmentData(CLIENT_BLOCK), false);

        assertSame(payload, SpawnBufferTranslation.seated(payload, contextHolding(BLOCK_ATTACHED_TYPE)));
    }

    @Test
    void theEntityIdOpeningThePayloadIsCarriedThrough() {
        CustomPacketPayload seated = SpawnBufferTranslation.seated(
                payloadOf(ENTITY_ID, attachmentData(SERVER_BLOCK), false), contextHolding(BLOCK_ATTACHED_TYPE));

        assertEquals(ENTITY_ID, ((AdvancedAddEntityPayload) seated).entityId());
    }

    @Test
    void theOffsetKeysBesideThePositionAreLeftAsTheyAre() {
        CustomPacketPayload seated = SpawnBufferTranslation.seated(
                payloadOf(ENTITY_ID, attachmentData(SERVER_BLOCK), false), contextHolding(BLOCK_ATTACHED_TYPE));

        assertEquals(new Vec3(-1.0, 0.0, -1.0), vec3In(bufferOf(seated).readNbt(), OFFSET_KEY));
    }

    @Test
    void whateverTheEntityWroteAfterTheCompoundRidesAlongUntouched() {
        CustomPacketPayload seated = SpawnBufferTranslation.seated(
                payloadOf(ENTITY_ID, attachmentData(SERVER_BLOCK), true), contextHolding(BLOCK_ATTACHED_TYPE));

        FriendlyByteBuf buffer = bufferOf(seated);
        buffer.readNbt();
        assertEquals(TAIL_BYTE, buffer.readByte());
        assertEquals(0, buffer.readableBytes());
    }

    @Test
    void anEntityTypeWithNoRegistrationIsThePayloadBack() {
        AdvancedAddEntityPayload payload = payloadOf(ENTITY_ID, attachmentData(SERVER_BLOCK), false);

        assertSame(payload, SpawnBufferTranslation.seated(payload, contextHolding(UNREGISTERED_TYPE)));
    }

    @Test
    void anEntityTheServerNoLongerHoldsIsThePayloadBack() {
        AdvancedAddEntityPayload payload = payloadOf(MISSING_ENTITY_ID, attachmentData(SERVER_BLOCK), false);

        assertSame(payload, SpawnBufferTranslation.seated(payload, contextHolding(BLOCK_ATTACHED_TYPE)));
    }
}
