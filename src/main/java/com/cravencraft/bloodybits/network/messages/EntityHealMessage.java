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
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record EntityHealMessage(int entityId, float healAmount) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<EntityHealMessage> TYPE = new CustomPacketPayload.Type<>(BloodyBitsMod.id("entity_heal_message"));

    public static final StreamCodec<FriendlyByteBuf, EntityHealMessage> STREAM_CODEC = StreamCodec.ofMember(EntityHealMessage::encode, EntityHealMessage::decode);

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeInt(this.entityId);
        buffer.writeFloat(this.healAmount);
    }

    public static EntityHealMessage decode(FriendlyByteBuf buffer) {
        return new EntityHealMessage(buffer.readInt(), buffer.readFloat());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(EntityHealMessage message, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (ClientConfig.showEntityDamage() && Minecraft.getInstance().level != null && context.flow().isClientbound()) {
                if (Minecraft.getInstance().level.getEntity(message.entityId()) instanceof LivingEntity livingEntity) {
                    healEntityInjuries(livingEntity, message);
                }
            }
        });
    }

    private static void healEntityInjuries(LivingEntity livingEntity, EntityHealMessage message) {
        if (livingEntity.isAlive()) {
            int entityId = livingEntity.getId();
            if (livingEntity.getHealth() < livingEntity.getMaxHealth()) {
                if (BloodyBitsUtils.INJURED_ENTITIES.containsKey(entityId)) {
                    EntityInjuries entityInjuries = BloodyBitsUtils.INJURED_ENTITIES.get(entityId);
                    double entityHealPercentage = message.healAmount() / livingEntity.getMaxHealth();
                    entityInjuries.addHealAmount(entityHealPercentage);
                }
            }
            else {
                BloodyBitsUtils.INJURED_ENTITIES.remove(entityId);
            }
        }
    }
}
