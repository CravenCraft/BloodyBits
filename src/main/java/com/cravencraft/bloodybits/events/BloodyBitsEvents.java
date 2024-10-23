package com.cravencraft.bloodybits.events;

import com.cravencraft.bloodybits.BloodyBitsMod;
import com.cravencraft.bloodybits.client.renderer.entity.layers.InjuryLayer;
import com.cravencraft.bloodybits.config.ClientConfig;
import com.cravencraft.bloodybits.config.CommonConfig;
import com.cravencraft.bloodybits.entity.BloodSprayEntity;
import com.cravencraft.bloodybits.network.BloodyBitsPacketHandler;
import com.cravencraft.bloodybits.network.messages.EntityDamageMessage;
import com.cravencraft.bloodybits.network.messages.EntityHealMessage;
import com.cravencraft.bloodybits.network.messages.EntityMessage;
import com.cravencraft.bloodybits.registries.EntityRegistry;
import com.cravencraft.bloodybits.utils.BloodyBitsUtils;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

@Mod.EventBusSubscriber(modid = BloodyBitsMod.MODID)
public class BloodyBitsEvents {

    /**
     * TODO: Remove or comment out before building code for release.
     * Just a simple method made to test blood sprays by right clicking on blocks.
     */
    @SubscribeEvent
    public static void testBloodSpray(PlayerInteractEvent.RightClickBlock event) {
        if (!event.getEntity().level().isClientSide()) {
            if (BloodyBitsUtils.BLOOD_SPRAY_ENTITIES.size() >= CommonConfig.maxSpatters()) {
                BloodyBitsUtils.BLOOD_SPRAY_ENTITIES.get(0).discard();
                BloodyBitsUtils.BLOOD_SPRAY_ENTITIES.remove(0);
            }

            BloodSprayEntity bloodSprayEntity = new BloodSprayEntity(EntityRegistry.BLOOD_SPRAY.get(), event.getEntity(), event.getEntity().level());
            BloodyBitsUtils.BLOOD_SPRAY_ENTITIES.add(bloodSprayEntity);

            bloodSprayEntity.setDeltaMovement(event.getEntity().getLookAngle());
            event.getEntity().level().addFreshEntity(bloodSprayEntity);

            BloodyBitsPacketHandler.INSTANCE.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> bloodSprayEntity),
                    new EntityMessage(bloodSprayEntity.getId(), event.getEntity().getId()));
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void entityDeathEvent(LivingDeathEvent deathEvent) {
        if (!deathEvent.isCanceled() && deathEvent.getEntity() != null && deathEvent.getEntity().level().isClientSide()) {
            BloodyBitsUtils.INJURED_ENTITIES.remove(deathEvent.getEntity().getId());
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void entityHealEvent(LivingHealEvent event) {
        LivingEntity entity = event.getEntity();

        if (!event.isCanceled() && entity != null) {
            BloodyBitsPacketHandler.INSTANCE.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity),
                    new EntityHealMessage(entity.getId(), event.getAmount()));
        }
    }

    /**
     * Looks for all the players on a given server and creates blood sprays if the damage event is
     * close enough to any of the players. Will break out of the loop the second a player is found,
     * which should optimize this somewhat.
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void bloodOnEntityDamage(LivingDamageEvent event) {

        LivingEntity entity = event.getEntity();
        if (!event.isCanceled() && entity != null) {
            String entityName = (entity instanceof Player) ? "player" : entity.getEncodeId();
            entityName = (entityName == null) ? "" : entityName;

            if (!event.getEntity().level().isClientSide() && !CommonConfig.blackListEntities().contains(entityName) && !CommonConfig.blackListDamageSources().contains(event.getSource().type().msgId())) {
                int maxDamage = (int) Math.min(20, event.getAmount());
                createBloodSpray(entity, event.getSource(), maxDamage, false);
            }

            // For adding damage textures to the given entity.
            if (!ClientConfig.blackListInjurySources().contains(event.getSource().type().msgId())) {
                boolean isBurn = ClientConfig.burnDamageSources().contains(event.getSource().type().msgId());
                BloodyBitsMod.LOGGER.info("IS DAMAGE TYPE {} A BURN? {}", event.getSource().type().msgId(), isBurn);

                BloodyBitsPacketHandler.INSTANCE.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity),
                        new EntityDamageMessage(entity.getId(), event.getAmount(), !isBurn, isBurn));
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void testRenderEvent(RenderLivingEvent.Pre<?, ?> event) {

        if (ClientConfig.showEntityDamage() && !event.isCanceled()) {

            LivingEntity livingEntity = event.getEntity();
            if (livingEntity.isAlive() && livingEntity.getHealth() < livingEntity.getMaxHealth()) {
                String entityName = (livingEntity instanceof Player) ? "player" : livingEntity.getEncodeId();
                entityName = (entityName == null) ? "" : entityName;

                if (!BloodyBitsUtils.INJURY_LAYER_ENTITIES.contains(entityName)) {

                    BloodyBitsMod.LOGGER.info("ADDING NEW LAYER FOR {}", entityName);
                    event.getRenderer().addLayer(new InjuryLayer(event.getRenderer()));
                    BloodyBitsUtils.INJURY_LAYER_ENTITIES.add(entityName);
                    BloodyBitsMod.LOGGER.info("ADDED");
                }
            }
        }
    }

    @SubscribeEvent
    public static void entityBleedWhenDamaged(LivingEvent event) {
        if (CommonConfig.bleedWhenDamaged() && event.getEntity() != null && !event.getEntity().level().isClientSide() && !event.getEntity().isDeadOrDying()) {
            LivingEntity entity = event.getEntity();
            double remainingHealthPercentage = entity.getHealth() / entity.getMaxHealth();
            String entityName = (entity instanceof Player) ? "player" : entity.getEncodeId();
            entityName = (entityName == null) ? "" : entityName;

            if (!CommonConfig.blackListEntities().contains(entityName) && remainingHealthPercentage < 0.5) {
                double randomNumber = remainingHealthPercentage * Math.random();

                if (randomNumber < 0.001) {
                    createBloodSpray(entity, entity.damageSources().genericKill(), 1, true);
                }
            }
        }

    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void creeperExplosionEvent(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();

        if (entity != null && !event.isCanceled() && entity instanceof Creeper creeper) {

        }
    }

    private static void createBloodSpray(LivingEntity entity, DamageSource damageSource, int damageAmount, boolean isBleedingDamage) {
        if (entity != null && damageSource != null) {
            String entityName = (entity instanceof Player) ? "player" : entity.getEncodeId();
            entityName = (entityName == null) ? "" : entityName;

            if (!entity.level().isClientSide() && !CommonConfig.blackListEntities().contains(entityName) && !CommonConfig.blackListDamageSources().contains(damageSource.type().msgId())) {
                //TODO: Currently, creepers don't produce blood when exploding because it's not registered as a LivingAttackEvent on THEMSELF.
                //      So, maybe have an exception happen in a damage event?
                for (int i = 0; i < damageAmount; i++) {
                    if (BloodyBitsUtils.BLOOD_SPRAY_ENTITIES.size() >= CommonConfig.maxSpatters()) {
                        BloodyBitsUtils.BLOOD_SPRAY_ENTITIES.get(0).discard();
                        BloodyBitsUtils.BLOOD_SPRAY_ENTITIES.remove(0);
                    }

                    BloodSprayEntity bloodSprayEntity = new BloodSprayEntity(EntityRegistry.BLOOD_SPRAY.get(), entity, entity.level());
                    BloodyBitsUtils.BLOOD_SPRAY_ENTITIES.add(bloodSprayEntity);
                    Vec3 sourceAngle;
                    if (damageSource.getEntity() != null) {
                        sourceAngle = (damageSource.getDirectEntity() != null) ? damageSource.getDirectEntity().getLookAngle() : damageSource.getEntity().getLookAngle();
                    }
                    else {
                        sourceAngle = entity.getLookAngle();
                    }

                    double xAngle = sourceAngle.x;
                    double yAngle = (isBleedingDamage) ? -sourceAngle.y : -sourceAngle.y + Math.random();
                    double zAngle = sourceAngle.z;
                    double adjustedDamage = damageAmount * CommonConfig.bloodSprayDistance();

                    // Ensure the angles are always going where they are expected to go.
                    xAngle = (xAngle > 0) ? (xAngle - Math.random()) : (xAngle + Math.random());
                    zAngle = (zAngle > 0) ? (zAngle - Math.random()) : (zAngle + Math.random());

                    xAngle *= adjustedDamage;
                    zAngle *= adjustedDamage;

                    bloodSprayEntity.setDeltaMovement(xAngle, yAngle * 0.35, zAngle);
                    entity.level().addFreshEntity(bloodSprayEntity);

                    BloodyBitsPacketHandler.INSTANCE.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> bloodSprayEntity),
                            new EntityMessage(bloodSprayEntity.getId(), entity.getId()));
                }
            }
        }
    }
}
