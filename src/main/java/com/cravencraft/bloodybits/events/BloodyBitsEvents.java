package com.cravencraft.bloodybits.events;

import com.cravencraft.bloodybits.BloodyBitsMod;
import com.cravencraft.bloodybits.config.CommonConfig;
import com.cravencraft.bloodybits.model.BloodType;
import com.cravencraft.bloodybits.client.particle.spray.BloodSprayParticleOptions;
import com.cravencraft.bloodybits.registries.BloodTypeRegistry;
import com.cravencraft.bloodybits.registries.ParticleRegistry;
import com.cravencraft.bloodybits.utils.BloodyBitsUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.level.ExplosionEvent;

@EventBusSubscriber(modid = BloodyBitsMod.MODID)
public class BloodyBitsEvents {

    private static boolean isConfigLoaded;
    private static final String doesNotBleed = "does_not_bleed";

    @SubscribeEvent
    public static void isConfigLoaded(ModConfigEvent.Loading event) {
        isConfigLoaded = true;
    }

    @SubscribeEvent
    public static void isConfigReloading(ModConfigEvent.Reloading event) {
        isConfigLoaded = true;
    }

//    @SubscribeEvent
//    public static void testBlockTextureOverlay(PlayerInteractEvent.RightClickBlock event) {
//
//        if (!isConfigLoaded) return;
//
//        var level = event.getLevel();
//        var server =  level.getServer();
//
//        if (level instanceof ServerLevel serverLevel &&
//                server != null &&
//                event.getHand() == InteractionHand.MAIN_HAND &&
//                event.getItemStack().getItem() == Items.BLAZE_ROD) {
//            var player = event.getEntity();
//            var x = player.getX();
//            var y = player.getY() + 1.5;
//            var z = player.getZ();
//            var playerBlockPos = player.getOnPos();
//            var playerFacingDirection = player.getDirection();
//            var lookAngle = player.getLookAngle();
//
//           server.getPlayerList().getPlayers().forEach(serverPlayer -> (serverLevel)
//                    .sendParticles(
//                            serverPlayer,
//                            new BloodSprayParticleOptions(ParticleRegistry.DEFAULT_BLOOD_COLOR, lookAngle, 1.0f),
//                            true,
//                            x,
//                            y,
//                            z,
//                            1,
//                            lookAngle.x,
//                            lookAngle.y,
//                            lookAngle.z,
//                            0.0
//                    )
//            );
//        }
//
//    }

    /**
     * Looks for all the players on a given server and creates blood sprays if the damage event is
     * close enough to any of the players.
     */
    @SubscribeEvent
    public static void bloodOnEntityDamage(LivingDamageEvent.Post event) {

        if (!isConfigLoaded) return;

        if (event.getEntity().level() instanceof ServerLevel serverLevel) {
            createBloodParticles(serverLevel, event.getEntity(), event.getSource().type(), event.getNewDamage());
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
        if (!isConfigLoaded) return;

        if (event.getLevel() instanceof ServerLevel serverLevel &&
                event.getExplosion().getDirectSourceEntity() instanceof Creeper creeper) {

            var explosionDamageType = serverLevel.damageSources().source(DamageTypes.EXPLOSION).type();
            var explosionDamageAmount = 25.0f;
            createBloodParticles(serverLevel, creeper, explosionDamageType, explosionDamageAmount);
        }
    }

    private static void createBloodParticles(ServerLevel serverLevel, LivingEntity entity,
                                             DamageType damageType, float damageAmount) {

        if (CommonConfig.blackListDamageSources().contains(damageType.msgId())) return;

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
        damageAmount = Math.clamp(damageAmount, 1, 50);

        int count = serverLevel.random.nextIntBetweenInclusive(1, (int) damageAmount);
        double bbShove = Math.max(aabb.getXsize() * 0.5 - 0.5, 0);
        double scale = (aabb.getXsize() + 2) / 3f;
        var server = serverLevel.getServer();

        for (int i = 0; i < count; i++) {

            Vec3 sprayVector = new Vec3(
                    BloodyBitsUtils.applyRandomSign(serverLevel.random.nextIntBetweenInclusive(1, count) * 0.05f),
                    serverLevel.random.nextIntBetweenInclusive(1, count) * 0.05f,
                    BloodyBitsUtils.applyRandomSign(serverLevel.random.nextIntBetweenInclusive(1, count) * 0.05f)
            );

            String finalBloodColor = bloodColor;
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
    }
}
