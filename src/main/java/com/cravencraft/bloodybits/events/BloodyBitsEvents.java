package com.cravencraft.bloodybits.events;

import com.cravencraft.bloodybits.BloodyBitsMod;
import com.cravencraft.bloodybits.config.CommonConfig;
import com.cravencraft.bloodybits.entity.BloodSprayEntity;
import com.cravencraft.bloodybits.network.BloodyBitsPacketHandler;
import com.cravencraft.bloodybits.network.messages.EntityMessage;
import com.cravencraft.bloodybits.particle.BloodSprayParticle;
import com.cravencraft.bloodybits.registries.EntityRegistry;
import com.cravencraft.bloodybits.registries.ParticleRegistry;
import com.cravencraft.bloodybits.utils.BloodyBitsUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import net.neoforged.neoforge.event.level.ExplosionEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = BloodyBitsMod.MODID)
public class BloodyBitsEvents {

//    private static int currentTick = 0;

//    /**
//     * Just a simple method made to test blood sprays by right-clicking on blocks.
//     */
//    @SubscribeEvent
//    public static void testBloodSpray(PlayerInteractEvent.RightClickBlock event) {
//        if (!event.getEntity().level().isClientSide()) {
//            if (BloodyBitsUtils.BLOOD_SPRAY_ENTITIES.size() >= CommonConfig.maxSpatters()) {
//                BloodyBitsUtils.BLOOD_SPRAY_ENTITIES.get(0).discard();
//                BloodyBitsUtils.BLOOD_SPRAY_ENTITIES.remove(0);
//            }
//
//            BloodSprayEntity bloodSprayEntity = new BloodSprayEntity(EntityRegistry.BLOOD_SPRAY.get(), event.getEntity(), event.getEntity().level());
//            BloodyBitsUtils.BLOOD_SPRAY_ENTITIES.add(bloodSprayEntity);
//            BloodyBitsMod.LOGGER.info("Current Tick Count: {}", event.getEntity().tickCount);
//
//            bloodSprayEntity.setDeltaMovement(event.getEntity().getLookAngle());
//
//            if ((currentTick + 20) < event.getEntity().tickCount) {
//                currentTick = event.getEntity().tickCount;
//
//                event.getEntity().level().addFreshEntity(bloodSprayEntity);
//
//                BloodyBitsPacketHandler.INSTANCE.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> bloodSprayEntity),
//                        new EntityMessage(bloodSprayEntity.getId(), event.getEntity().getId()));
//            }
//        }
//    }

    /**
     * Looks for all the players on a given server and creates blood sprays if the damage event is
     * close enough to any of the players. Will break out of the loop the second a player is found,
     * which should optimize this somewhat.
     */
    @SubscribeEvent
    public static void bloodOnEntityDamage(LivingDamageEvent.Pre event) {
        var entity = event.getEntity();
        var source = event.getSource();
        var level = entity.level();

        if (level.isClientSide()) return;

        String entityName = (entity instanceof Player) ? "player" : entity.getEncodeId();
        entityName = (entityName == null) ? "" : entityName;

        if (!(source.getEntity() instanceof Player)) return;
//        try (var level = entity.level()) {
//            if (!level.isClientSide()) return;

        AABB aabb = entity.isMultipartEntity() ? entity.getParts()[entity.getRandom().nextInt(entity.getParts().length)].getBoundingBox() : entity.getBoundingBox();
        Vec3 vec = aabb.getCenter();
        float damage = event.getContainer().getNewDamage();
//        if (damage <= config.minDamage()) {
//            return;
//        }
        if (damage == Float.MAX_VALUE) {
            // kill command
            return;
        }

        damage = Math.min(damage, 2000);
        int count = (int) (damage)
                + level.random.nextIntBetweenInclusive(0, (int) (2 * (damage)));
//        double speed = config.scaledBaseSpeed() + count * config.scaledSpeedPerParticle();
        double bbShove = Math.max(aabb.getXsize() * 0.5 - 0.5, 0);
        double scale = (aabb.getXsize() + 2) / 3f;
//        level.addAlwaysVisibleParticle(ParticleRegistry.BLOOD_SPRAY_PARTICLE.get(), entity.getX(), entity.getY() + 3, entity.getZ(), 0, 0, 0);
        var server = level.getServer();

        if (server == null) return;

        server.getPlayerList().getPlayers().forEach(player -> ((ServerLevel) level)
                .sendParticles(player, ParticleRegistry.BLOOD_SPRAY_PARTICLE.get(), true, vec.x, vec.y + aabb.getYsize() * 0.5, vec.z, count, 0.05 + bbShove, 0.1, 0.05 + bbShove, 0.5));

//        }
//        catch (Exception e) {
//            BloodyBitsMod.LOGGER.error("Failed to load entity damage", e);
//        }

//        if (!event.getEntity().level().isClientSide() && !CommonConfig.blackListEntities().contains(entityName) && !CommonConfig.blackListDamageSources().contains(event.getSource().type().msgId())) {
//            int maxDamage = (int) Math.min(20, event.getOriginalDamage());
//            createBloodSpray(entity, event.getSource(), maxDamage, false);
//        }

        // TODO: This is just for damage textures, and is also client side only. So, need to add this
        //       to the client events class when I finally get around to polishing that feature.
        // For adding damage textures to the given entity. Ensure no blacklisted injury sources are added.
//            if (!ClientConfig.blackListInjurySources().contains(event.getSource().type().msgId())) {
//                boolean isBurn = ClientConfig.burnDamageSources().contains(event.getSource().type().msgId());
//
//                BloodyBitsPacketHandler.INSTANCE.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity),
//                        new EntityDamageMessage(entity.getId(), event.getAmount(), !isBurn, isBurn));
//            }
    }

    // TODO: Rework this. This is a cool feature to have. Just want it to be slightly less random. Set up a minimum and
    //       maximum interval that will change as the entity takes more damage. May need to get tick counts for that?

    /**
     *  Makes the entity bleed when damaged below a certain threshold. The entity will bleed more often the lower it is
     *  below that threshold.
     *  TODO: LivingEvent might only happen at certain times. See when it happens.
     */
//    @SubscribeEvent
//    public static void entityBleedWhenDamaged(LivingEvent event) {
//        if (CommonConfig.bleedWhenDamaged() && !event.getEntity().level().isClientSide() && !event.getEntity().isDeadOrDying()) {
//            LivingEntity entity = event.getEntity();
//            double remainingHealthPercentage = entity.getHealth() / entity.getMaxHealth();
//            String entityName = (entity instanceof Player) ? "player" : entity.getEncodeId();
//            entityName = (entityName == null) ? "" : entityName;
//
//            if (!CommonConfig.blackListEntities().contains(entityName) && remainingHealthPercentage <= 0.5) {
//
//                int mod = (int) (remainingHealthPercentage * 1000);
//                if (mod == 0 || entity.tickCount == 0) {
//                    return;
//                }
//
//                if (entity.tickCount % mod == 0) {
//                    createBloodSpray(entity, entity.damageSources().genericKill(), 1, true);
//                }
//            }
//        }
//    }

    /**
     * For when entities explode. Like a creeper.
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void creeperExplosionEvent(ExplosionEvent.Detonate event) {
        Entity entity = event.getExplosion().getDirectSourceEntity();

        if (entity instanceof LivingEntity livingEntity) {
            // TODO: This might produce an error since the damage source is probably whatever the explosion came from,
            //       and not the explosion itself.
            var damageSource = event.getExplosion().getIndirectSourceEntity();
            if (damageSource != null) {
                createBloodSpray(livingEntity, event.getExplosion().getIndirectSourceEntity().getLastDamageSource(), 15, false);
            }
        }
    }

    /**
     * Creates blood sprays for any entity that is damaged. Also adds each blood spray to a list BLOOD_SPRAY_ENTITIES
     * when it is created. This list maxes out at a configurable limit to ensure the blood spray entities impact the
     * game's performance as little as possible. When a new blood spray is created, if the limit of the list is passed,
     * the first index of the list is removed to ensure that the oldest sprays are removed first.
     */
    private static void createBloodSpray(LivingEntity entity, DamageSource damageSource, int damageAmount, boolean isBleedingDamage) {
        if (entity != null && damageSource != null) {
            String entityName = (entity instanceof Player) ? "player" : entity.getEncodeId();
            entityName = (entityName == null) ? "" : entityName;

            if (!entity.level().isClientSide() && !CommonConfig.blackListEntities().contains(entityName) && !CommonConfig.blackListDamageSources().contains(damageSource.type().msgId())) {
                for (int i = 0; i < damageAmount; i++) {
                    if (BloodyBitsUtils.BLOOD_SPRAY_ENTITIES.size() >= CommonConfig.maxSpatters()) {
                        BloodSprayEntity oldest = BloodyBitsUtils.CLIENT_SIDE_BLOOD_SPRAYS.get(0);
                        if (oldest != null) {
                            BloodyBitsUtils.BLOOD_SPRAY_ENTITIES.get(0).discard();
                            BloodyBitsUtils.BLOOD_SPRAY_ENTITIES.remove(0);
                        }
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

                    PacketDistributor.sendToServer(new EntityMessage(bloodSprayEntity.getId(), entity.getId()));
                }
            }
        }
    }
}
