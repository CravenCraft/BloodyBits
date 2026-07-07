package com.cravencraft.bloodybits.network.messages;

import com.cravencraft.bloodybits.BloodyBitsMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

//TODO: Can probably just have isBurn here if I don't plan on adding other injury sources. If I do, then maybe enums or something?
public record EntityDamageMessage(int entityId, int damageAmount, boolean isBurn, boolean isBleed) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<EntityDamageMessage> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(BloodyBitsMod.MODID, "entity_damage_message"));

    public static final StreamCodec<ByteBuf, EntityDamageMessage> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            EntityDamageMessage::entityId,
            ByteBufCodecs.VAR_INT,
            EntityDamageMessage::damageAmount,
            ByteBufCodecs.BOOL,
            EntityDamageMessage::isBurn,
            ByteBufCodecs.BOOL,
            EntityDamageMessage::isBleed,
            EntityDamageMessage::new
    );

    @Override
    public CustomPacketPayload.@NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleToClient(final EntityDamageMessage message, final IPayloadContext contextSupplier) {
        contextSupplier.enqueueWork(() -> {

        });
    }

    public static void handleToServer(final EntityDamageMessage message, final IPayloadContext contextSupplier) {
        contextSupplier.enqueueWork(() -> {

        });
    }

//    public static void handle(EntityDamageMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
//        NetworkEvent.Context context = contextSupplier.get();
//        context.enqueueWork(() -> {
//            if (ClientConfig.showEntityDamage() && Minecraft.getInstance().level != null && context.getDirection().getReceptionSide().isClient()) {
//                if (Minecraft.getInstance().level.getEntity(message.entityId) instanceof LivingEntity livingEntity) {
//                    addEntityInjuries(livingEntity, message);
//                }
//            }
//        });
//        context.setPacketHandled(true);
//    }
//
//    private static void addEntityInjuries(LivingEntity livingEntity, EntityDamageMessage message) {
//        if (livingEntity.isAlive()) {
//            int entityId = livingEntity.getId();
//            String entityName = (livingEntity instanceof Player) ? "player" : livingEntity.getEncodeId();
//            entityName = (entityName == null) ? "" : entityName;
//            EntityInjuries entityInjuries;
//            String injuryType = (message.isBleed) ? "bleed" : "burn";
//
//            // Will get the entity injuries either by retrieving them from a list based on the entity's ID,
//            // or by creating a new one if it is not contained in the list.
//            if (BloodyBitsUtils.INJURED_ENTITIES.containsKey(entityId)) {
//                entityInjuries = BloodyBitsUtils.INJURED_ENTITIES.get(entityId);
//            }
//            else {
//                entityInjuries = new EntityInjuries(entityName);
//                BloodyBitsUtils.INJURED_ENTITIES.put(entityId, entityInjuries);
//            }
//
//            double entityDamagePercentage = message.damageAmount / livingEntity.getMaxHealth();
//            entityInjuries.addInjuryHits(injuryType, entityDamagePercentage);
//        }
//    }
}
