package com.cravencraft.bloodybits.network.messages;

import com.cravencraft.bloodybits.BloodyBitsMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record EntityMessage(int entityId, int entityOwnerId) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<EntityMessage> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(BloodyBitsMod.MODID, "entity_message"));

    public static final StreamCodec<ByteBuf, EntityMessage> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            EntityMessage::entityId,
            ByteBufCodecs.VAR_INT,
            EntityMessage::entityOwnerId,
            EntityMessage::new
    );

    @Override
    public CustomPacketPayload.@NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleToClient(final EntityMessage message, final IPayloadContext contextSupplier) {
        contextSupplier.enqueueWork(() -> {

        });
    }

    public static void handleToServer(final EntityMessage message, final IPayloadContext contextSupplier) {
        contextSupplier.enqueueWork(() -> {

        });
    }
}
