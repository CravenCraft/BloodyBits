package com.cravencraft.bloodybits.events;

import com.cravencraft.bloodybits.BloodyBitsMod;
import com.cravencraft.bloodybits.config.CommonConfig;
import com.cravencraft.bloodybits.entity.BloodSprayEntity;
import com.cravencraft.bloodybits.network.BloodyBitsPacketHandler;
import com.cravencraft.bloodybits.network.messages.EntityHealthMessage;
import com.cravencraft.bloodybits.network.messages.EntityMessage;
import com.cravencraft.bloodybits.registries.EntityRegistry;
import com.cravencraft.bloodybits.utils.BloodyBitsUtils;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

@Mod.EventBusSubscriber(modid = BloodyBitsMod.MODID)
public class BloodyBitsEvents {

    @SubscribeEvent
    public static void testBloodSpray(PlayerInteractEvent.RightClickBlock event) {
        BloodyBitsMod.LOGGER.info("CREATING BLOOD SPRAY");
        if (!event.getEntity().level().isClientSide()) {
            BloodyBitsMod.LOGGER.info("CREATING BLOOD SPRAY SERVER SIDE");
            if (BloodyBitsUtils.BLOOD_SPRAY_ENTITIES.size() >= CommonConfig.maxSpatters()) {
                BloodyBitsUtils.BLOOD_SPRAY_ENTITIES.get(0).discard();
                BloodyBitsUtils.BLOOD_SPRAY_ENTITIES.remove(0);
            }

            BloodSprayEntity bloodSprayEntity = new BloodSprayEntity(EntityRegistry.BLOOD_SPRAY.get(), event.getEntity(), event.getEntity().level());
            BloodyBitsUtils.BLOOD_SPRAY_ENTITIES.add(bloodSprayEntity);
//            Vec3 sourceAngle;
//            if (event.getSource().getEntity() != null) {
//                sourceAngle = (event.getSource().getDirectEntity() != null) ? event.getSource().getDirectEntity().getLookAngle() : event.getSource().getEntity().getLookAngle();
//            }
//            else {
//                sourceAngle = event.getEntity().getLookAngle();
//            }

//            double xAngle = sourceAngle.x;
//            double yAngle = -sourceAngle.y + Math.random();
//            double zAngle = sourceAngle.z;
//            double adjustedDamage = maxDamage * 0.1;

            // Ensure the angles are always going where they are expected to go.
//            xAngle = (xAngle > 0) ? (xAngle - Math.random()) : (xAngle + Math.random()) - adjustedDamage;
//            zAngle = (zAngle > 0) ? (zAngle - Math.random()) : (zAngle + Math.random()) - adjustedDamage;

            bloodSprayEntity.setDeltaMovement(event.getEntity().getLookAngle());
            event.getEntity().level().addFreshEntity(bloodSprayEntity);

            BloodyBitsPacketHandler.INSTANCE.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> bloodSprayEntity),
                    new EntityMessage(bloodSprayEntity.getId(), event.getEntity().getId()));
        }
    }

//    @SubscribeEvent(priority = EventPriority.LOWEST)
//    public static void sprayOnEntityDeath(LivingDeathEvent event) {
//        LivingEntity entity = event.getEntity();
//
//        if (entity != null && CommonConfig.deathBloodExplosion() && !CommonConfig.blackListEntities().contains(entity.getEncodeId())) {
//            BloodyBitsMod.LOGGER.info("BLOOD SPRAYING ON DEATH");
//            int maxSprays = (int) Math.min(30, entity.getMaxHealth());
//            float volume = (float) CommonConfig.bloodExplosionVolume();
//            RandomSource random = RandomSource.create();
//            String entityName = (entity instanceof Player) ? "player" : entity.getEncodeId();
//            entityName = (entityName == null) ? "" : entityName;
//
//            if (CommonConfig.solidEntities().contains(entityName)) {
//                entity.playSound(SoundEvents.BONE_BLOCK_BREAK, volume, 1.0F / (random.nextFloat() * 0.2F + 0.9F));
//            }
//            else {
//                entity.playSound(BloodyBitsSounds.BODY_EXPLOSION.get(), volume, 1.0F / (random.nextFloat() * 0.2F + 0.9F));
//            }
//
//            createBloodSprayTest(entity, event.getSource(), maxSprays);
//        }
//    }

    @SubscribeEvent
    public static void entityHealEvent(LivingHealEvent event) {
        BloodyBitsMod.LOGGER.info("ENTITY HEALING EVENT AMOUNT: {}", event.getAmount());
        LivingEntity entity = event.getEntity();

        if (entity != null) {
            BloodyBitsPacketHandler.INSTANCE.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity),
                    new EntityHealthMessage(false, entity.getId()));
        }
    }

    /**
     * Looks for all the players on a given server and creates blood sprays if the damage event is
     * close enough to any of the players. Will break out of the loop the second a player is found,
     * which should optimize this somewhat.
     */
    @SubscribeEvent
    public static void bloodOnEntityDamage(LivingDamageEvent event) {
        BloodyBitsMod.LOGGER.info("ENTITY DAMAGED: {}", event.getEntity().getEncodeId());
        LivingEntity entity = event.getEntity();
        if (entity != null) {
            BloodyBitsMod.LOGGER.info("ENTITY {} ID: {}", entity.getEncodeId(), event.getEntity().getId());
            String entityName = (entity instanceof Player) ? "player" : entity.getEncodeId();
            entityName = (entityName == null) ? "" : entityName;

            if (!event.getEntity().level().isClientSide() && !CommonConfig.blackListEntities().contains(entityName) && !CommonConfig.blackListDamageSources().contains(event.getSource().type().msgId())) {
                int maxDamage = (int) Math.min(20, event.getAmount());
                createBloodSprayTest(entity, event.getSource(), maxDamage);
            }

            // For adding damage textures to the given entity.
            BloodyBitsPacketHandler.INSTANCE.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity),
                    new EntityHealthMessage(true, entity.getId()));
        }
    }

    private static void createBloodSprayTest(LivingEntity entity, DamageSource damageSource, int maxSprays) {
        if (entity != null && damageSource != null) {
            BloodyBitsMod.LOGGER.info("ENTITY {} ID: {}", entity.getEncodeId(), entity.getId());
            String entityName = (entity instanceof Player) ? "player" : entity.getEncodeId();
            entityName = (entityName == null) ? "" : entityName;

            if (!entity.level().isClientSide() && !CommonConfig.blackListEntities().contains(entityName) && !CommonConfig.blackListDamageSources().contains(damageSource.type().msgId())) {
//                int maxDamage = (int) Math.min(20, event.getAmount());
                //TODO: Currently, creepers don't produce blood when exploding because it's not registered as a LivingAttackEvent on THEMSELF.
                //      So, maybe have an exception happen in a damage event?
                for (int i = 0; i < maxSprays; i++) {
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
                    double yAngle = -sourceAngle.y + Math.random();
                    double zAngle = sourceAngle.z;
                    double adjustedDamage = maxSprays * 0.1;

                    // Ensure the angles are always going where they are expected to go.
                    xAngle = (xAngle > 0) ? (xAngle - Math.random()) : (xAngle + Math.random()) - adjustedDamage;
                    zAngle = (zAngle > 0) ? (zAngle - Math.random()) : (zAngle + Math.random()) - adjustedDamage;

                    bloodSprayEntity.setDeltaMovement(xAngle * 0.25, yAngle * 0.35, zAngle * 0.25);
                    entity.level().addFreshEntity(bloodSprayEntity);

                    BloodyBitsPacketHandler.INSTANCE.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> bloodSprayEntity),
                            new EntityMessage(bloodSprayEntity.getId(), entity.getId()));
                }
            }
        }
    }

//    private static void createBloodSpray(LivingDamageEvent event) {
//        int maxDamage = (int) Math.min(20, event.getAmount());
//        //TODO: Currently, creepers don't produce blood when exploding because it's not registered as a LivingAttackEvent on THEMSELF.
//        //      So, maybe have an exception happen in a damage event?
//        for (int i = 0; i < maxDamage; i++) {
//            if (BloodyBitsUtils.BLOOD_SPRAY_ENTITIES.size() >= CommonConfig.maxSpatters()) {
//                BloodyBitsUtils.BLOOD_SPRAY_ENTITIES.get(0).discard();
//                BloodyBitsUtils.BLOOD_SPRAY_ENTITIES.remove(0);
//            }
//
//            BloodSprayEntity bloodSprayEntity = new BloodSprayEntity(EntityRegistry.BLOOD_SPRAY.get(), event.getEntity(), event.getEntity().level());
//            BloodyBitsUtils.BLOOD_SPRAY_ENTITIES.add(bloodSprayEntity);
//            Vec3 sourceAngle;
//            if (event.getSource().getEntity() != null) {
//                sourceAngle = (event.getSource().getDirectEntity() != null) ? event.getSource().getDirectEntity().getLookAngle() : event.getSource().getEntity().getLookAngle();
//            }
//            else {
//                sourceAngle = event.getEntity().getLookAngle();
//            }
//
//            double xAngle = sourceAngle.x;
//            double yAngle = -sourceAngle.y + Math.random();
//            double zAngle = sourceAngle.z;
//            double adjustedDamage = maxDamage * 0.1;
//
//            // Ensure the angles are always going where they are expected to go.
//            xAngle = (xAngle > 0) ? (xAngle - Math.random()) : (xAngle + Math.random()) - adjustedDamage;
//            zAngle = (zAngle > 0) ? (zAngle - Math.random()) : (zAngle + Math.random()) - adjustedDamage;
//
//            bloodSprayEntity.setDeltaMovement(xAngle * 0.25, yAngle * 0.35, zAngle * 0.25);
//            event.getEntity().level().addFreshEntity(bloodSprayEntity);
//
//            BloodyBitsPacketHandler.INSTANCE.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> bloodSprayEntity),
//                    new EntityMessage(bloodSprayEntity.getId(), event.getEntity().getId()));
//        }
//    }
}
