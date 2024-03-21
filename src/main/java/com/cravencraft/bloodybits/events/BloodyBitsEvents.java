package com.cravencraft.bloodybits.events;

import com.cravencraft.bloodybits.BloodyBitsMod;
import com.cravencraft.bloodybits.entities.BloodSprayEntity;
import com.cravencraft.bloodybits.particles.BloodyBitsParticles;
import com.cravencraft.bloodybits.registries.EntityRegistry;
import net.minecraft.world.entity.monster.Pillager;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = BloodyBitsMod.MODID)
public class BloodyBitsEvents {
//    private static double zTest = 0.0;
//    private static double yTest = 0.0;

    //TODO: Some math that might work:
    //      - Direct entity source location, it's looking angle (if a living entity. Just the position if not), and the attacked entity's current position.
    @SubscribeEvent
    public static void particleOnEntityDamage(LivingAttackEvent event) {
        if (event.getSource().isCreativePlayer()) {
            if (!event.getEntity().level().isClientSide()) {
                BloodSprayEntity bloodSprayEntity = new BloodSprayEntity(EntityRegistry.BLOOD_SPRAY.get(), event.getEntity(), event.getEntity().level());
                bloodSprayEntity.setDeltaMovement(0, 1, 0);
                event.getEntity().level().addFreshEntity(bloodSprayEntity);
                BloodyBitsMod.LOGGER.info("AFTER BLOOD SPRAY SHOULD BE RENDERED.");
//                BloodProjectileEntity bloodProjectile = new BloodProjectileEntity(event.getEntity().level());
//                event.getEntity().level().addFreshEntity(bloodProjectile);
//                bloodProjectile.setPos(event.getSource().getSourcePosition());
//                event.getEntity().horizontalCollision
//                event.getEntity().getLookAngle().normalize()
                if (event.getEntity() instanceof Pillager pillager) {
                    pillager.getBoundingBox();
//                    BloodyBitsMod.LOGGER.info("BOUNDING BOX: {}", pillager.getBoundingBox());
//                    BloodyBitsMod.LOGGER.info("UUID: {}", pillager.getUUID()); //TODO: Unique per entity. Could this be what I need for Molten Metals?
                }
//                BloodyBitsMod.LOGGER.info("DAMAGED ENTITY BOUNDING BOX: {}", event.getEntity().getBoundingBox());
//                BloodyBitsMod.LOGGER.info("DAMAGED ENTITY DIRECTION: {}", event.getEntity().getMotionDirection());
//                BloodyBitsMod.LOGGER.info("DAMAGED ENTITY DIRECTION 2D DATA VALUE: {}", event.getEntity().getDirection().get2DDataValue());
//                BloodyBitsMod.LOGGER.info("DIRECT SOURCE POSITION: {}", event.getSource().getSourcePosition());
//                BloodyBitsMod.LOGGER.info("DIRECT SOURCE DIRECTION: {}", event.getSource().getDirectEntity().getDirection());
//                BloodyBitsMod.LOGGER.info("DIRECT SOURCE LOOK ANGLE: {}", event.getSource().getDirectEntity().getLookAngle());
//                BloodyBitsMod.LOGGER.info("DIRECT SOURCE Y ROT: {}", event.getSource().getDirectEntity().getYRot());
                event.getSource().getDirectEntity().getDirection();
//                spawnParticles(event.getEntity().level(), event.getEntity().getEyePosition(), event.getSource().getDirectEntity().getLookAngle());
            }
        }
    }

    //TODO: Ensure this is Not Null. Will often be null when a source is not known.
    // Another check could be to ensure that it only shows when a player is looking.

    /**
     * TODO: Could add Direction as a simple way to determine which vector do add more force to.
     *       That is unless we find a better way to determine with the particleDirection parameter.
     *       Could just take the value with the greatest absolute value and add to it.
     *
     * @param level
     * @param pos               The base position of the entity that was attacked. This will be dead center at base.
     * @param particleDirection The base particle direction. Unmodified this will go exactly where the entity that attacked was looking.
     */
    private static void spawnParticles(Level level, Vec3 pos, Vec3 particleDirection) {
//        if (yTest > zTest) {
//            zTest += 0.1;
//        }
//        else {
//            yTest += 0.1;
//        }
//        zTest += 0.1;
//        BloodyBitsMod.LOGGER.info("Z TEST: {}", zTest);
//        BloodyBitsMod.LOGGER.info("Y TEST: {}", yTest);
        BloodyBitsMod.LOGGER.info("VEC3 X {} Y {} Z {}", pos.x, pos.y, pos.z);
        BloodyBitsMod.LOGGER.info("SPRAY ANGLE: {}", particleDirection);
        for (int i = 0; i < 10; i++) {

            double randomNumberRange = (Math.random() * pos.x) + pos.y;
            level.addParticle(BloodyBitsParticles.BLOOD_PARTICLES.get(), pos.x, pos.y, pos.z,
                    Math.random() + particleDirection.x, Math.random() + particleDirection.y, Math.random() + particleDirection.z);
        }
    }
}
