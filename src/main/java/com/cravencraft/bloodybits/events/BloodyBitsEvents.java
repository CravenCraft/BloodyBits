package com.cravencraft.bloodybits.events;

import com.cravencraft.bloodybits.BloodyBitsMod;
import com.cravencraft.bloodybits.client.particle.mist.BloodMistParticleOptions;
import com.cravencraft.bloodybits.config.CommonConfig;
import com.cravencraft.bloodybits.client.particle.spray.BloodSprayParticleOptions;
import com.cravencraft.bloodybits.registries.ParticleRegistry;
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
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = BloodyBitsMod.MODID)
public class BloodyBitsEvents {

    private static final String doesNotBleed = "does_not_bleed";
    private static int maxDamage = 50;



    // TODO: Comment out or remove before final build.
    @SubscribeEvent
    public static void rightClickBlockMakeBloodSpray(PlayerInteractEvent.RightClickBlock event) {

        if (event.getEntity().level() instanceof ServerLevel serverLevel) {
            if (event.getItemStack().getItem() instanceof SwordItem swordItem) {
                var server = serverLevel.getServer();
                var center = event.getPos().getCenter();
                var bloodColor = ParticleRegistry.DEFAULT_BLOOD_COLOR;
                var sprayVector = new Vec3(0,1,0);

                server.getPlayerList().getPlayers().forEach(player -> (serverLevel)
                        .sendParticles(
                                player,
                                new BloodSprayParticleOptions(bloodColor, sprayVector, 0.5F),
                                true,
                                center.x,
                                center.y + 1.0F,
                                center.z,
                                1,
                                0,
                                0,
                                0,
                                0.2
                        )
                );
            }
        }
    }

    /**
     * Looks for all the players on a given server and creates blood sprays if the damage event is
     * close enough to any of the players.
     */
    @SubscribeEvent
    public static void bloodOnEntityDamage(LivingDamageEvent event) {
        if (event.getEntity().level() instanceof ServerLevel serverLevel) {

            if (!event.getSource().isCreativePlayer()) return;

            createBloodParticles(serverLevel, event.getEntity(), event.getSource(), event.getAmount());

            if (event.getSource().getEntity() instanceof ServerPlayer player) {
                sendPlayerDamageType(player, event.getSource().type());
            }
        }
    }

    // TODO: Need to find the best way to pass in that the entity dies due to a projectile so the mist spray can be larger.
    @SubscribeEvent
    public static void bloodMistOnEntityDeath(LivingDeathEvent event) {
//        if (event.getEntity().level() instanceof ServerLevel serverLevel) {
//            createBloodParticles(serverLevel, event.getEntity(), event.getSource(), 0);
//        }
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

        var bloodColor = BloodyBitsUtils.getEntityBloodColor(entity);

        if (bloodColor == null) return;

        if (damageAmount <= 0 || damageAmount == Float.MAX_VALUE) return;

        damageAmount = Math.max(1, Math.min(maxDamage, damageAmount));

        var spatterSize = Mth.clamp(damageAmount / maxDamage, 0.0f, 1.0f);

        AABB aabb = entity.isMultipartEntity() ?
                entity.getParts()[entity.getRandom().nextInt(entity.getParts().length)].getBoundingBox() :
                entity.getBoundingBox();
        Vec3 vec = aabb.getCenter();

        int count = serverLevel.random.nextIntBetweenInclusive(1, (int) damageAmount);
        double bbShove = Math.max(aabb.getXsize() * 0.5 - 0.5, 0);
        double scale = (aabb.getXsize() + 2) / 3f;
        var server = serverLevel.getServer();

        var damageSourceEntity = damageSource.getEntity();
        var damageSourcePosition = (damageSourceEntity != null) ? damageSourceEntity.position() : entity.position();
        var entityPosition = entity.position();

        var min = 0.1;
        var max = 1.9;

        Vec3 sprayDirection = damageSourcePosition
                .subtract(entityPosition)
                .normalize();

        // If the damage source creates a blood mist, then the blood spray should come out the opposite side.
        if (CommonConfig.bloodMistDamageSources().contains(damageSource.type().msgId())) {
            var mistDirection = damageSourcePosition
                    .subtract(entityPosition)
                    .normalize()
                    .multiply(BloodyBitsUtils.getRandomVectorVariance(min, max));

            sprayDirection = entityPosition
                    .subtract(damageSourcePosition)
                    .normalize();

            createBloodMist(serverLevel, aabb, mistDirection, bloodColor);
        }

//        for (int i = 0; i < count; i++) {
            sprayDirection = sprayDirection.multiply(BloodyBitsUtils.getRandomVectorVariance(min, max));
            createBloodSpray(serverLevel, aabb, sprayDirection, bloodColor, spatterSize);
//        }
    }

    private static void createBloodSpray(ServerLevel serverLevel, AABB entitySize, Vec3 sprayVector,
                                         String bloodColor, float spatterSize) {
        var server = serverLevel.getServer();
        var entityCenter = entitySize.getCenter();

        server.getPlayerList().getPlayers().forEach(player -> (serverLevel)
                .sendParticles(
                        player,
                        new BloodSprayParticleOptions(bloodColor, sprayVector, spatterSize),
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

    private static void createBloodMist(ServerLevel serverLevel, AABB entitySize, Vec3 mistVector, String bloodColor) {
        var server = serverLevel.getServer();
        var entityCenter = entitySize.getCenter();

        server.getPlayerList().getPlayers().forEach(player -> (serverLevel)
                .sendParticles(
                        player,
                        new BloodMistParticleOptions(bloodColor, mistVector, 1.0f),
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
