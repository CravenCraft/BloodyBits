package com.cravencraft.bloodybits.network.messages;

import com.cravencraft.bloodybits.BloodyBitsMod;
import com.cravencraft.bloodybits.entity.custom.BloodSprayEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class BloodySprayEntityMessage {

    public int entityId;
    public int entityOwner;

    public BloodySprayEntityMessage(int entityId, int entityOwner) {
        BloodyBitsMod.LOGGER.info("IN CONSTRUCTOR.");
        this.entityId = entityId;
        this.entityOwner = entityOwner;
    }

    public static void encode(BloodySprayEntityMessage message, FriendlyByteBuf buffer) {
        BloodyBitsMod.LOGGER.info("IN ENCODER: {} - {}", message.entityId, message.entityOwner);
        buffer.writeInt(message.entityId).writeInt(message.entityOwner);
    }

    public static BloodySprayEntityMessage decode(FriendlyByteBuf buffer) {
        BloodyBitsMod.LOGGER.info("IN DECODER.");
        return new BloodySprayEntityMessage(buffer.readInt(), buffer.readInt());
    }

    public static void handle(BloodySprayEntityMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        BloodyBitsMod.LOGGER.info("IN HANDLER");
        NetworkEvent.Context context = contextSupplier.get();
        BloodyBitsMod.LOGGER.info("CLIENT SIDE? {}", context.getDirection().getReceptionSide().isClient());
        context.enqueueWork(() -> {
            if (context.getDirection().getReceptionSide().isClient()) {
                BloodyBitsMod.LOGGER.info("IS INSTANCE OF THE ENTITY ON CLIENT SIDE: {} - {}", message.entityId, message.entityOwner);
                Entity entity = Minecraft.getInstance().level.getEntity(message.entityId);
                if (entity instanceof BloodSprayEntity bloodSprayEntity) {
                    bloodSprayEntity.setOwner(Minecraft.getInstance().level.getEntity(message.entityOwner));
                }
            }
        });
        context.setPacketHandled(true);
    }
}
