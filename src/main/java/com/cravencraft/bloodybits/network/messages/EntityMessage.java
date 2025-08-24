package com.cravencraft.bloodybits.network.messages;

import com.cravencraft.bloodybits.BloodyBitsMod;
import com.cravencraft.bloodybits.entity.BloodSprayEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class EntityMessage {

    public int entityId;
    public int entityOwnerid;

    public EntityMessage(int entityId, int entityOwnerId) {
        this.entityId = entityId;
        this.entityOwnerid = entityOwnerId;
    }

    public static void encode(EntityMessage message, FriendlyByteBuf buffer) {
        buffer.writeInt(message.entityId).writeInt(message.entityOwnerid);
    }

    public static EntityMessage decode(FriendlyByteBuf buffer) {
        return new EntityMessage(buffer.readInt(), buffer.readInt());
    }

    public static void handle(EntityMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
//            if (Minecraft.getInstance().level != null && context.getDirection().getReceptionSide().isClient()) {
//                BloodyBitsMod.LOGGER.info("CREATING NEW ENTITY CLIENT SIDE?");
//                Entity entity = Minecraft.getInstance().level.getEntity(message.entityId);
//                BloodyBitsMod.LOGGER.info("ENTITY CLIENT SIDE CREATED.");
//                if (entity instanceof BloodSprayEntity bloodSprayEntity) {
//                    bloodSprayEntity.setOwner(Minecraft.getInstance().level.getEntity(message.entityOwnerid));
//                }
//            }
        });
        context.setPacketHandled(true);
    }
}
