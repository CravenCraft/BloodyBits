package com.cravencraft.bloodybits.network.messages;

import com.cravencraft.bloodybits.BloodyBitsMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record EntityHealMessage(int entityId, int healAmount) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<EntityHealMessage> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(BloodyBitsMod.MODID, "entity_heal_message"));

    public static final StreamCodec<ByteBuf, EntityHealMessage> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            EntityHealMessage::entityId,
            ByteBufCodecs.VAR_INT,
            EntityHealMessage::healAmount,
            EntityHealMessage::new
    );

    @Override
    public CustomPacketPayload.@NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleToClient(final EntityHealMessage message, final IPayloadContext contextSupplier) {
        contextSupplier.enqueueWork(() -> {

        });
    }

    public static void handleToServer(final EntityHealMessage message, final IPayloadContext contextSupplier) {
        contextSupplier.enqueueWork(() -> {

        });
    }

//    public static void handle(EntityHealMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
//        NetworkEvent.Context context = contextSupplier.get();
//        context.enqueueWork(() -> {
//            if (ClientConfig.showEntityDamage() && Minecraft.getInstance().level != null && context.getDirection().getReceptionSide().isClient()) {
//                if (Minecraft.getInstance().level.getEntity(message.entityId) instanceof LivingEntity livingEntity) {
//                    healEntityInjuries(livingEntity, message);
//                }
//            }
//        });
//        context.setPacketHandled(true);
//    }
//
//    private static void healEntityInjuries(LivingEntity livingEntity, EntityHealMessage message) {
//
//        if (livingEntity.isAlive()) {
//
//            int entityId = livingEntity.getId();
//            if (livingEntity.getHealth() < livingEntity.getMaxHealth()) {
//
//                // Will get the entity injuries either by retrieving them from a list based on the entity's ID.
//                if (BloodyBitsUtils.INJURED_ENTITIES.containsKey(entityId)) {
//                    EntityInjuries entityInjuries = BloodyBitsUtils.INJURED_ENTITIES.get(entityId);
//
//                    double entityHealPercentage = message.healAmount / livingEntity.getMaxHealth();
//
//                    entityInjuries.addHealAmount(entityHealPercentage);
//                }
//            }
//            else {
//                BloodyBitsUtils.INJURED_ENTITIES.remove(entityId);
//            }
//        }
//    }
}
