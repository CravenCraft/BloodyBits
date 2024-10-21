package com.cravencraft.bloodybits.network.messages;

import com.cravencraft.bloodybits.BloodyBitsMod;
import com.cravencraft.bloodybits.client.model.EntityInjuries;
import com.cravencraft.bloodybits.config.ClientConfig;
import com.cravencraft.bloodybits.utils.BloodyBitsUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class EntityDamageMessage {
    public int entityId;
    public float damageAmount;

    //TODO: Can probably just have isBurn here if I don't plan on adding other injury sources. If I do, then maybe enums or something?
    public boolean isBleed;
    public boolean isBurn;

    public EntityDamageMessage(int entityId, float damageAmount, boolean isBleed, boolean isBurn) {
        this.damageAmount = damageAmount;
        this.entityId = entityId;
        this.isBleed = isBleed;
        this.isBurn = isBurn;
    }

    public static void encode(EntityDamageMessage message, FriendlyByteBuf buffer) {
        buffer.writeInt(message.entityId);
        buffer.writeFloat(message.damageAmount);
        buffer.writeBoolean(message.isBleed);
        buffer.writeBoolean(message.isBurn);
    }

    public static EntityDamageMessage decode(FriendlyByteBuf buffer) {
        return new EntityDamageMessage(buffer.readInt(), buffer.readFloat(), buffer.readBoolean(), buffer.readBoolean());
    }

    public static void handle(EntityDamageMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            if (ClientConfig.showEntityDamage() && Minecraft.getInstance().level != null && context.getDirection().getReceptionSide().isClient()) {
                if (Minecraft.getInstance().level.getEntity(message.entityId) instanceof LivingEntity livingEntity) {
                    addEntityInjuries(livingEntity, message);
                }
            }
        });
        context.setPacketHandled(true);
    }

    private static void addEntityInjuries(LivingEntity livingEntity, EntityDamageMessage message) {
        BloodyBitsMod.LOGGER.info("BEFORE ADDING ENTITY INJURIES IF STATEMENT");
        if (livingEntity.isAlive()) {
            BloodyBitsMod.LOGGER.info("INSIDE IF STATEMENT");
            int entityId = livingEntity.getId();
            String entityName = (livingEntity instanceof Player) ? "player" : livingEntity.getEncodeId();
            entityName = (entityName == null) ? "" : entityName;
            EntityInjuries entityInjuries;
            String injuryType = (message.isBleed) ? "bleed" : "burn";

            // Will get the entity injuries either by retrieving them from a list based on the entity's ID,
            // or by creating a new one if it is not contained in the list.
            if (BloodyBitsUtils.INJURED_ENTITIES.containsKey(entityId)) {
                entityInjuries = BloodyBitsUtils.INJURED_ENTITIES.get(entityId);
            }
            else {
                BloodyBitsMod.LOGGER.info("Adding entity {} to INJURED ENTITIES", entityId);
                entityInjuries = new EntityInjuries(entityName);
                BloodyBitsUtils.INJURED_ENTITIES.put(entityId, entityInjuries);
            }

            double entityDamagePercentage = message.damageAmount / livingEntity.getMaxHealth();
            BloodyBitsMod.LOGGER.info("Entity damage %: {}", entityDamagePercentage);

            // Apply the appropriate hit size depending on the damage amount.
            if (entityDamagePercentage >= 0.15) {
                entityInjuries.addLargeHit(injuryType);
            }
            else if (entityDamagePercentage >= 0.05) {
                entityInjuries.addMediumHit(injuryType);
            }
            else {
                entityInjuries.addSmallHit(injuryType);
            }

        }
    }
}
