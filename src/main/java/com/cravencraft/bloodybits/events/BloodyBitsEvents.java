package com.cravencraft.bloodybits.events;

import com.cravencraft.bloodybits.BloodyBitsMod;
import com.cravencraft.bloodybits.client.particle.mist.BloodMistParticleOptions;
import com.cravencraft.bloodybits.config.CommonConfig;
import com.cravencraft.bloodybits.model.BloodType;
import com.cravencraft.bloodybits.client.particle.spray.BloodSprayParticleOptions;
import com.cravencraft.bloodybits.registries.BloodTypeRegistry;
import com.cravencraft.bloodybits.registries.ParticleRegistry;
import com.cravencraft.bloodybits.utils.BloodyBitsUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = BloodyBitsMod.MODID)
public class BloodyBitsEvents {

    private static final String doesNotBleed = "does_not_bleed";

    /**
     * Looks for all the players on a given server and creates blood sprays if the damage event is
     * close enough to any of the players.
     */
    @SubscribeEvent
    public static void bloodOnEntityDamage(LivingDamageEvent event) {

        // TODO: Just for testing. Remove before building
        if (!event.getSource().isCreativePlayer()) return;

        if (event.getEntity().level() instanceof ServerLevel serverLevel) {


//            var damageSourceEntity = event.getSource().getEntity();
//            var damageSourcePosition = (damageSourceEntity != null) ? damageSourceEntity.position() : event.getEntity().position();
//            var entityPosition = event.getEntity().position();
//            var difference = damageSourcePosition.subtract(entityPosition);

            createBloodParticles(serverLevel, event.getEntity(), event.getSource(), event.getAmount());
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
            createBloodParticles(serverLevel, creeper, explosionDamageSource, explosionDamageAmount);
        }
    }

    private static void createBloodParticles(ServerLevel serverLevel, LivingEntity entity,
                                             DamageSource damageSource, float damageAmount) {

        if (!BloodyBitsMod.isCommonConfigLoaded) return;

        if (CommonConfig.blackListDamageSources().contains(damageSource.type().msgId())) return;

        String bloodColor = ParticleRegistry.DEFAULT_BLOOD_COLOR;
        for (BloodType bloodType : BloodTypeRegistry.getBloodTypes()) {
            if (entity.getType().is(bloodType.entityTag())) {

                if (bloodType.entityTag().location().getPath().equals(doesNotBleed)) {
                    return;
                }
                else {
                    bloodColor = bloodType.color();
                    break;
                }
            }
        }

        AABB aabb = entity.isMultipartEntity() ?
                entity.getParts()[entity.getRandom().nextInt(entity.getParts().length)].getBoundingBox() :
                entity.getBoundingBox();
        Vec3 vec = aabb.getCenter();

        if (damageAmount == Float.MAX_VALUE) return;

        if (damageAmount <= 0) return;

        damageAmount = Math.max(1, Math.min(50, damageAmount));

        int count = serverLevel.random.nextIntBetweenInclusive(1, (int) damageAmount);
        double bbShove = Math.max(aabb.getXsize() * 0.5 - 0.5, 0);
        double scale = (aabb.getXsize() + 2) / 3f;
        var server = serverLevel.getServer();

        var damageSourceEntity = damageSource.getEntity();
        var damageSourcePosition = (damageSourceEntity != null) ? damageSourceEntity.position() : entity.position();
        var entityPosition = entity.position();
        var difference = damageSourcePosition.subtract(entityPosition).normalize();
        var testDifference = damageSourcePosition.subtract(entityPosition).normalize();
        var normalizedDifference = difference.normalize();

        String finalBloodColor = bloodColor;
        for (int i = 0; i < count; i++) {

            Vec3 sprayVector = new Vec3(
                    BloodyBitsUtils.applyRandomSign(serverLevel.random.nextIntBetweenInclusive(1, count) * 0.05f),
                    serverLevel.random.nextIntBetweenInclusive(1, count) * 0.05f,
                    BloodyBitsUtils.applyRandomSign(serverLevel.random.nextIntBetweenInclusive(1, count) * 0.05f)
            );

            server.getPlayerList().getPlayers().forEach(player -> (serverLevel)
                    .sendParticles(
                            player,
                            new BloodSprayParticleOptions(finalBloodColor, sprayVector, 1.0f),
                            true,
                            vec.x,
                            vec.y + aabb.getYsize() * 0.5,
                            vec.z,
                            1,
                            0.5,
                            0.5,
                            0.5,
                            0.2
                    )
            );
        }
//        Vec3 sprayVector = new Vec3(
//                BloodyBitsUtils.applyRandomSign(serverLevel.random.nextIntBetweenInclusive(1, count) * 0.05f),
//                serverLevel.random.nextIntBetweenInclusive(1, count) * 0.05f,
//                BloodyBitsUtils.applyRandomSign(serverLevel.random.nextIntBetweenInclusive(1, count) * 0.05f)
//        );
        server.getPlayerList().getPlayers().forEach(player -> (serverLevel)
                .sendParticles(
                        player,
                        new BloodMistParticleOptions(finalBloodColor, difference, 1.0f),
                        true,
                        vec.x,
                        vec.y + aabb.getYsize() * 0.5,
                        vec.z,
                        1,
                        0.5,
                        0.5,
                        0.5,
                        0.2
                )
        );
    }
}
