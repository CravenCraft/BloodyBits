package com.cravencraft.bloodybits.network.messages;

import com.cravencraft.bloodybits.BloodyBitsMod;
import com.cravencraft.bloodybits.client.model.EntityInjuries;
import com.cravencraft.bloodybits.config.ClientConfig;
import com.cravencraft.bloodybits.utils.BloodyBitsUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record EntityDamageMessage(int entityId, float damageAmount, boolean isBleed, boolean isBurn) implements CustomPacketPayload {
    
    public static final CustomPacketPayload.Type<EntityDamageMessage> TYPE = new CustomPacketPayload.Type<>(BloodyBitsMod.id("entity_damage_message"));

    public static final StreamCodec<FriendlyByteBuf, EntityDamageMessage> STREAM_CODEC = StreamCodec.ofMember(EntityDamageMessage::encode, EntityDamageMessage::decode);

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeInt(this.entityId);
        buffer.writeFloat(this.damageAmount);
        buffer.writeBoolean(this.isBleed);
        buffer.writeBoolean(this.isBurn);
    }

    public static EntityDamageMessage decode(FriendlyByteBuf buffer) {
        return new EntityDamageMessage(buffer.readInt(), buffer.readFloat(), buffer.readBoolean(), buffer.readBoolean());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(EntityDamageMessage message, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (ClientConfig.showEntityDamage() && Minecraft.getInstance().level != null && context.flow().isClientbound()) {
                if (Minecraft.getInstance().level.getEntity(message.entityId()) instanceof LivingEntity livingEntity) {
                    addEntityInjuries(livingEntity, message);
                }
            }
        });
    }

    private static void addEntityInjuries(LivingEntity livingEntity, EntityDamageMessage message) {
        if (livingEntity.isAlive()) {
            int entityId = livingEntity.getId();
            String entityName = (livingEntity instanceof Player) ? "player" : livingEntity.getEncodeId();
            entityName = (entityName == null) ? "" : entityName;
            EntityInjuries entityInjuries;
            String injuryType = (message.isBleed()) ? "bleed" : "burn";

            if (BloodyBitsUtils.INJURED_ENTITIES.containsKey(entityId)) {
                entityInjuries = BloodyBitsUtils.INJURED_ENTITIES.get(entityId);
            }
            else {
                entityInjuries = new EntityInjuries(entityName);
                BloodyBitsUtils.INJURED_ENTITIES.put(entityId, entityInjuries);
            }

            double entityDamagePercentage = message.damageAmount() / livingEntity.getMaxHealth();
            entityInjuries.addInjuryHits(injuryType, entityDamagePercentage);
        }
    }
}
