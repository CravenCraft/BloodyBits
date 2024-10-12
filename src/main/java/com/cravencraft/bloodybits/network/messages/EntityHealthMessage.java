package com.cravencraft.bloodybits.network.messages;

import com.cravencraft.bloodybits.client.model.EntityDamage;
import com.cravencraft.bloodybits.config.ClientConfig;
import com.cravencraft.bloodybits.utils.BloodyBitsUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class EntityHealthMessage {
    public boolean isDamageEvent;
    public int entityId;

    public EntityHealthMessage(boolean isDamageEvent, int entityId) {
        this.isDamageEvent = isDamageEvent;
        this.entityId = entityId;
    }

    public static void encode(EntityHealthMessage message, FriendlyByteBuf buffer) {
        buffer.writeBoolean(message.isDamageEvent);
        buffer.writeInt(message.entityId);
    }

    public static EntityHealthMessage decode(FriendlyByteBuf buffer) {
        return new EntityHealthMessage(buffer.readBoolean(), buffer.readInt());
    }

    public static void handle(EntityHealthMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            if (ClientConfig.showEntityDamage() && Minecraft.getInstance().level != null && context.getDirection().getReceptionSide().isClient()) {
                if (Minecraft.getInstance().level.getEntity(message.entityId) instanceof LivingEntity livingEntity) {
                    if ((message.isDamageEvent)) {
                        addDamageTexture(livingEntity);
                    } else {
                        removeDamageTexture(livingEntity);
                    }
                }
            }
        });
        context.setPacketHandled(true);
    }

    private static void removeDamageTexture(LivingEntity livingEntity) {
        if (livingEntity.isAlive()) {
//            BloodyBitsMod.LOGGER.info("INSIDE IF STATEMENT OF HEALING EVENT.");

            int entityId = livingEntity.getId();
            String entityName = (livingEntity instanceof Player) ? "player" : livingEntity.getEncodeId();
            entityName = (entityName == null) ? "" : entityName;

            if (BloodyBitsUtils.DAMAGED_ENTITIES.containsKey(entityId) && !BloodyBitsUtils.NO_INJURY_TEXTURE_ENTITIES.contains(entityName)) {
//                BloodyBitsMod.LOGGER.info("INSIDE SECOND IF STATEMENT OF HEALING EVENT");
                if (livingEntity.getHealth() >= livingEntity.getMaxHealth()) {
                    BloodyBitsUtils.DAMAGED_ENTITIES.remove(livingEntity.getId());
                }
                else {
//                    BloodyBitsMod.LOGGER.info("ENTITY DAMAGE SIZE PRIOR: {}", BloodyBitsUtils.DAMAGED_ENTITIES.get(entityId).getPaintedAppliedInjuryTextures().size());
                    EntityDamage entityDamage = BloodyBitsUtils.DAMAGED_ENTITIES.get(entityId);
                    entityDamage.removeInjuries( (livingEntity.getMaxHealth() - livingEntity.getHealth()) / livingEntity.getMaxHealth());
                    BloodyBitsUtils.DAMAGED_ENTITIES.put(entityId, entityDamage);
//                    BloodyBitsMod.LOGGER.info("ENTITY DAMAGE SIZE AFTER: {}", BloodyBitsUtils.DAMAGED_ENTITIES.get(entityId).getPaintedAppliedInjuryTextures().size());
                }
            }
        }
    }

    private static void addDamageTexture(LivingEntity livingEntity) {
//        BloodyBitsMod.LOGGER.info("ENTITY {} ID: {}", livingEntity.getEncodeId(), livingEntity.getId());

        if (livingEntity.isAlive() && livingEntity.getHealth() < livingEntity.getMaxHealth()) {
            int entityId = livingEntity.getId();
            String entityName = (livingEntity instanceof Player) ? "player" : livingEntity.getEncodeId();
            entityName = (entityName == null) ? "" : entityName;

            // Determines if the entity has any damage textures associated with it, then places that
            // entity within a blacklist or a map with its available textures. The blacklist is made
            // solely so this conditional is only ever accessed once per entity since this method is
            // executed every tick. Want to keep things as efficient as possible.
            if (!BloodyBitsUtils.DAMAGED_ENTITIES.containsKey(entityId) && !BloodyBitsUtils.NO_INJURY_TEXTURE_ENTITIES.contains(entityName)) {
                // Best to add this check to avoid unexpected crashes.
//                BloodyBitsMod.LOGGER.info("ENTITY NAME: {} ENTITY UUID: {}", entityName, entityId);
                EntityDamage entityDamage = new EntityDamage(entityName);
//                BloodyBitsMod.LOGGER.info("ENTITY DAMAGE AVAILABLE TEXTURES SIZE: {}", entityDamage.getAvailableInjuryTextures().size());
                if (entityDamage.getAvailableInjuryTextures().isEmpty()) {
                    BloodyBitsUtils.NO_INJURY_TEXTURE_ENTITIES.add(entityName);
                }
                else {
                    BloodyBitsUtils.DAMAGED_ENTITIES.put(entityId, new EntityDamage(entityName));
                }
            }

            // Will render a random assortment of injury textures on the given entity
            // if it is contained within the map.
            if (BloodyBitsUtils.DAMAGED_ENTITIES.containsKey(entityId)) {
                String damageType = (livingEntity.getLastDamageSource() != null) ? livingEntity.getLastDamageSource().type().msgId() : "generic";
                EntityDamage entityDamage = BloodyBitsUtils.DAMAGED_ENTITIES.get(entityId);
                entityDamage.addInjuries(damageType, (livingEntity.getMaxHealth() - livingEntity.getHealth()) / livingEntity.getMaxHealth());
            }
        }
    }
}
