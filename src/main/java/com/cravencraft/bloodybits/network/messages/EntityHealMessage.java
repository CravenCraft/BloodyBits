package com.cravencraft.bloodybits.network.messages;

import com.cravencraft.bloodybits.client.model.EntityInjuries;
import com.cravencraft.bloodybits.config.ClientConfig;
import com.cravencraft.bloodybits.utils.BloodyBitsUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class EntityHealMessage {
    public int entityId;
    public float healAmount;

    public EntityHealMessage(int entityId, float healAmount) {
        this.healAmount = healAmount;
        this.entityId = entityId;
    }

    public static void encode(EntityHealMessage message, FriendlyByteBuf buffer) {
        buffer.writeInt(message.entityId);
        buffer.writeFloat(message.healAmount);
    }

    public static EntityHealMessage decode(FriendlyByteBuf buffer) {
        return new EntityHealMessage(buffer.readInt(), buffer.readFloat());
    }

    public static void handle(EntityHealMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            if (ClientConfig.showEntityDamage() && Minecraft.getInstance().level != null && context.getDirection().getReceptionSide().isClient()) {
                if (Minecraft.getInstance().level.getEntity(message.entityId) instanceof LivingEntity livingEntity) {
                    healEntityInjuries(livingEntity, message);
                }
            }
        });
        context.setPacketHandled(true);
    }

    private static void healEntityInjuries(LivingEntity livingEntity, EntityHealMessage message) {

        if (livingEntity.isAlive()) {

            int entityId = livingEntity.getId();
            if (livingEntity.getHealth() < livingEntity.getMaxHealth()) {

                // Will get the entity injuries either by retrieving them from a list based on the entity's ID.
                if (BloodyBitsUtils.INJURED_ENTITIES.containsKey(entityId)) {
                    EntityInjuries entityInjuries = BloodyBitsUtils.INJURED_ENTITIES.get(entityId);

                    double entityHealPercentage = message.healAmount / livingEntity.getMaxHealth();

                    entityInjuries.addHealAmount(entityHealPercentage);
                }
            }
            else {
                BloodyBitsUtils.INJURED_ENTITIES.remove(entityId);
            }
        }
    }
}
