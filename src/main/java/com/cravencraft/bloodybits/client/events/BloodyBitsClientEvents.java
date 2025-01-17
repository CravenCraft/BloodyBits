package com.cravencraft.bloodybits.client.events;

import com.cravencraft.bloodybits.client.renderer.entity.layers.InjuryLayer;
import com.cravencraft.bloodybits.config.ClientConfig;
import com.cravencraft.bloodybits.config.CommonConfig;
import com.cravencraft.bloodybits.network.BloodyBitsPacketHandler;
import com.cravencraft.bloodybits.network.messages.EntityHealMessage;
import com.cravencraft.bloodybits.utils.BloodyBitsUtils;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.network.PacketDistributor;

@OnlyIn(Dist.CLIENT)
public class BloodyBitsClientEvents {

    /**
     * Adds an injury layer to the entity. Will trigger once for any new entity the first time it is rendered.
     * Once the layer is added, it is then added to a list which will ensure that multiple layers aren't added for
     * entities.
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void addInjuryLayerToEntity(RenderLivingEvent.Pre<?, ?> event) {

        if (ClientConfig.showEntityDamage() && !event.isCanceled()) {

            LivingEntity livingEntity = event.getEntity();
            if (livingEntity.isAlive() && livingEntity.getHealth() < livingEntity.getMaxHealth()) {
                String entityName = (livingEntity instanceof Player) ? "player" : livingEntity.getEncodeId();
                entityName = (entityName == null) ? "" : entityName;

                if (!BloodyBitsUtils.INJURY_LAYER_ENTITIES.contains(entityName)) {
                    event.getRenderer().addLayer(new InjuryLayer(event.getRenderer()));
                    BloodyBitsUtils.INJURY_LAYER_ENTITIES.add(entityName);
                }
            }
        }
    }

    /**
     * Clears all injury related lists when the client player logs out (If they have entity damage enabled).
     * Allows the player to log out and back in if they modify their texture packs, and damage doesn't appear on entities.
     */
    @SubscribeEvent
    public static void clearInjuryTextureListsOnResourcePackReload(ClientPlayerNetworkEvent.LoggingOut event) {
        if (ClientConfig.showEntityDamage()) {
            BloodyBitsUtils.INJURY_LAYER_ENTITIES.clear();
            BloodyBitsUtils.INJURED_ENTITIES.clear();
        }
    }

    /**
     * When an entity heals a message will be sent client side with the entity's ID and the heal amount. This will
     * be used to potentially remove injuries from the entity if it is contained within the INJURED_ENTITIES list.
     * An entity will not be added to the list if the config is disabled, or injury textures do not exist for the given
     * entity.
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void entityHealEvent(LivingHealEvent event) {
        LivingEntity entity = event.getEntity();

        if (ClientConfig.showEntityDamage() && !event.isCanceled() && entity != null) {
            String entityName = (entity instanceof Player) ? "player" : entity.getEncodeId();
            entityName = (entityName == null) ? "" : entityName;

            if (!CommonConfig.blackListEntities().contains(entityName)) {
                BloodyBitsPacketHandler.INSTANCE.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity),
                        new EntityHealMessage(entity.getId(), event.getAmount()));
            }
        }
    }

    /**
     * Removes an entity from the INJURED_ENTITIES list upon death if that configuration is enabled client side.
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void entityDeathEvent(LivingDeathEvent deathEvent) {
        if (ClientConfig.showEntityDamage() && !deathEvent.isCanceled() && deathEvent.getEntity() != null && deathEvent.getEntity().level().isClientSide()) {
            BloodyBitsUtils.INJURED_ENTITIES.remove(deathEvent.getEntity().getId());
        }
    }
}
