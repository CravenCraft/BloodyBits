package com.cravencraft.bloodybits.events;

import com.cravencraft.bloodybits.BloodyBitsMod;
import com.cravencraft.bloodybits.client.particle.mist.BloodMistParticleOptions;
import com.cravencraft.bloodybits.config.CommonConfig;
import com.cravencraft.bloodybits.client.particle.spray.BloodSprayParticleOptions;
import com.cravencraft.bloodybits.utils.BloodyBitsUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = BloodyBitsMod.MODID)
public class BloodyBitsEvents {

    /**
     * Looks for all the players on a given server and creates blood sprays if the damage event is
     * close enough to any of the players.
     */
    @SubscribeEvent
    public static void bloodOnEntityDamage(LivingDamageEvent event) {
        if (event.getEntity().level() instanceof ServerLevel serverLevel) {

            if (entityDamageSafetyChecks(event.getSource(), event.getAmount())) {
                createBloodParticles(serverLevel, event.getEntity(), event.getSource(), event.getAmount(), false);
            }

            if (event.getSource().getEntity() instanceof ServerPlayer player) {
                sendPlayerDamageType(player, event.getSource().type());
            }
        }
    }

    // TODO: Need to find the best way to pass in that the entity dies due to a projectile so the mist spray can be larger.
    @SubscribeEvent
    public static void bloodMistOnEntityDeath(LivingDeathEvent event) {
        if (event.getEntity().level() instanceof ServerLevel serverLevel) {
            var damageSource = event.getSource();
            var isBloodMistDamageSource = CommonConfig.bloodMistDamageSources().contains(damageSource.type().msgId());

            if (entityDeathSafetyChecks(damageSource) && isBloodMistDamageSource) {
                createBloodParticles(serverLevel, event.getEntity(), event.getSource(), 0, true);
            }
        }
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
        if (event.getLevel() instanceof ServerLevel serverLevel &&
                event.getExplosion().getDirectSourceEntity() instanceof Creeper creeper) {
            var explosionDamageSource = event.getExplosion().getDamageSource();
            var explosionDamageAmount = 25.0f;
            createBloodParticles(serverLevel, creeper, explosionDamageSource, explosionDamageAmount, true);
        }
    }

    private static boolean entityDamageSafetyChecks(DamageSource damageSource, float damageAmount) {
        if (!BloodyBitsMod.isCommonConfigLoaded) return false;
        if (CommonConfig.blackListDamageSources().contains(damageSource.type().msgId())) return false;
        return !(damageAmount <= 0) && damageAmount != Float.MAX_VALUE;
    }

    private static boolean entityDeathSafetyChecks(DamageSource damageSource) {
        if (!BloodyBitsMod.isCommonConfigLoaded) return false;
        return !CommonConfig.blackListDamageSources().contains(damageSource.type().msgId());
    }

    private static void createBloodParticles(ServerLevel serverLevel, LivingEntity entity,
                                             DamageSource damageSource, float damageAmount, boolean isKillingBlow) {

        var bloodColor = BloodyBitsUtils.getEntityBloodColor(entity);
        if (bloodColor == null) return;

        AABB entityAABB = entity.isMultipartEntity() ?
                entity.getParts()[entity.getRandom().nextInt(entity.getParts().length)].getBoundingBox() :
                entity.getBoundingBox();

//        Vec3 entityCenter = entityAABB.getCenter();
//        double bbShove = Math.max(entityAABB.getXsize() * 0.5 - 0.5, 0);
//        double scale = (entityAABB.getXsize() + 2) / 3f;

        var damageSourceEntity = damageSource.getEntity();
        var entityPosition = entity.position();
        var damageSourcePosition = (damageSourceEntity != null) ? damageSourceEntity.position() : entityPosition;

        Vec3 sprayDirection = entityPosition
                .subtract(damageSourcePosition)
                .normalize();

        // If the damage source creates a blood mist, then the blood spray should come out the opposite side.
        if (CommonConfig.bloodMistDamageSources().contains(damageSource.type().msgId())) {
            var minMistVariance = 0.1;
            var maxMistVariance = 1.9;
            var mistDirection = damageSourcePosition
                    .subtract(entityPosition)
                    .normalize()
                    .multiply(BloodyBitsUtils.getRandomVectorVariance(minMistVariance, maxMistVariance));

            sprayDirection = entityPosition
                    .subtract(damageSourcePosition)
                    .normalize();

            createBloodMist(serverLevel, entityAABB, mistDirection, bloodColor, isKillingBlow);
        }

        createBloodSpray(serverLevel, entityAABB, damageAmount, sprayDirection, bloodColor);
    }

    private static void createBloodSpray(ServerLevel serverLevel, AABB entitySize, float damageAmount,
                                         Vec3 sprayVector, String bloodColor) {
        var server = serverLevel.getServer();
        var entityCenter = entitySize.getCenter();

        var min = 0.1;
        var max = 1.0;

        var damageCap = CommonConfig.getBloodSprayDamageCap();
        if (damageAmount <= 0 || damageAmount == Float.MAX_VALUE) return;
        damageAmount = Math.max(1, Math.min(damageCap, damageAmount));

        var force = Mth.clamp(damageAmount / damageCap, 0.0f, 1.0f);
        var forceVector = new Vec3( 1 + force, 1 + force, 1 + force);

        int bloodSprayCount = (int) Math.max(1, damageAmount / ((float) damageCap / CommonConfig.getBloodSprayMaxPerHit()));
        for (int i = 0; i < bloodSprayCount; i++) {
            var randomVector = BloodyBitsUtils.getRandomSignVectorVariance(min, max);
            var modifiedSprayVector = sprayVector.add(randomVector).multiply(forceVector);

            server.getPlayerList().getPlayers().forEach(player -> (serverLevel)
                    .sendParticles(
                            player,
                            new BloodSprayParticleOptions(bloodColor, modifiedSprayVector, force),
                            true,
                            entityCenter.x,
                            entityCenter.y + entitySize.getYsize() * 0.5,
                            entityCenter.z,
                            1,
                            0,
                            0,
                            0,
                            0.2
                    )
            );
        }
    }

    private static void createBloodMist(ServerLevel serverLevel, AABB entitySize, Vec3 mistVector,
                                        String bloodColor, boolean isKillingBlow) {
        var server = serverLevel.getServer();
        var entityCenter = entitySize.getCenter();
        var scale = (isKillingBlow) ? 2.0F : 1.0F;

        server.getPlayerList().getPlayers().forEach(player -> (serverLevel)
                .sendParticles(
                        player,
                        new BloodMistParticleOptions(bloodColor, mistVector, scale),
                        true,
                        entityCenter.x,
                        entityCenter.y + entitySize.getYsize() * 0.5,
                        entityCenter.z,
                        1,
                        0.5,
                        0.5,
                        0.5,
                        0.1
                )
        );
    }

    private static void sendPlayerDamageType(Player player, DamageType damageType) {
        if (!CommonConfig.damageTypeDebug()) return;

        player.sendSystemMessage(Component.literal("Damage type id: " + damageType.msgId()));
    }
}
