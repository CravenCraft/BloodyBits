package com.cravencraft.bloodybits.network.messages;

import com.cravencraft.bloodybits.BloodyBitsMod;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record EntityMessage(int entityId, int entityOwnerid) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<EntityMessage> TYPE = new CustomPacketPayload.Type<>(BloodyBitsMod.id("entity_message"));

    public static final StreamCodec<FriendlyByteBuf, EntityMessage> STREAM_CODEC = StreamCodec.ofMember(EntityMessage::encode, EntityMessage::decode);

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeInt(this.entityId);
        buffer.writeInt(this.entityOwnerid);
    }

    public static EntityMessage decode(FriendlyByteBuf buffer) {
        return new EntityMessage(buffer.readInt(), buffer.readInt());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(EntityMessage message, IPayloadContext context) {
        context.enqueueWork(() -> {
            // Originally empty in 1.20.1 implementation
        });
    }
}
