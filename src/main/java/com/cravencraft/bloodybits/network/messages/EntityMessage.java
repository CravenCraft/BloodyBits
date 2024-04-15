package com.cravencraft.bloodybits.network.messages;

import com.cravencraft.bloodybits.entity.custom.BloodChunkEntity;
import com.cravencraft.bloodybits.entity.custom.BloodSprayEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class EntityMessage {

    public int entityId;
    public int entityOwner;

    public EntityMessage(int entityId, int entityOwner) {
        this.entityId = entityId;
        this.entityOwner = entityOwner;
    }

    public static void encode(EntityMessage message, FriendlyByteBuf buffer) {
        buffer.writeInt(message.entityId).writeInt(message.entityOwner);
    }

    public static EntityMessage decode(FriendlyByteBuf buffer) {
        return new EntityMessage(buffer.readInt(), buffer.readInt());
    }

    public static void handle(EntityMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            if (context.getDirection().getReceptionSide().isClient()) {
                Entity entity = Minecraft.getInstance().level.getEntity(message.entityId);
                if (entity instanceof BloodSprayEntity bloodSprayEntity) {
                    bloodSprayEntity.setOwner(Minecraft.getInstance().level.getEntity(message.entityOwner));
                }
                else if (entity instanceof BloodChunkEntity bloodChunkEntity) {
                    bloodChunkEntity.setOwner(Minecraft.getInstance().level.getEntity(message.entityOwner));
                }
            }
        });
        context.setPacketHandled(true);
    }
}
