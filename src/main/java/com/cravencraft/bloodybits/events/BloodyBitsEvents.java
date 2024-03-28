package com.cravencraft.bloodybits.events;

import com.cravencraft.bloodybits.BloodyBitsMod;
import com.cravencraft.bloodybits.entity.custom.BloodSprayEntity;
import com.cravencraft.bloodybits.registries.EntityRegistry;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = BloodyBitsMod.MODID)
public class BloodyBitsEvents {
    @SubscribeEvent
    public static void particleOnEntityDamage(LivingAttackEvent event) {
        if (event.getSource().isCreativePlayer()) {
            //TODO: Probably do want this.
            if (!event.getEntity().level().isClientSide()) {
                for (int i = 0; i < event.getAmount(); i++) {
                    BloodSprayEntity bloodSprayEntity = new BloodSprayEntity(EntityRegistry.BLOOD_SPRAY.get(), event.getEntity(), event.getEntity().level(), event.getAmount());
                    BloodyBitsMod.LOGGER.info("LOOK ANGLE: {}\nDAMAGE AMOUNT: {}", event.getSource().getDirectEntity().getLookAngle().normalize(), event.getAmount());
//                bloodSprayEntity.stretchLimit = (int) event.getAmount();
//                bloodSprayEntity.xMinVal = (int) -event.getAmount();
//                bloodSprayEntity.setDeltaMovement(event.getSource().getDirectEntity().getLookAngle());
//                bloodSprayEntity.set
                    double xAngle = -event.getSource().getDirectEntity().getLookAngle().x;
                    double yAngle = -event.getSource().getDirectEntity().getLookAngle().y + Math.random();
                    double zAngle = -event.getSource().getDirectEntity().getLookAngle().z;
                    double adjustedDamage = event.getAmount() * 0.1;
                    // Ensure the angles are always going where they are expected to go.
                    xAngle = (xAngle > 0) ? (xAngle - Math.random()) : (xAngle + Math.random()) - adjustedDamage;
//                    yAngle = (yAngle > 0) ? (yAngle - Math.random()) : (yAngle + Math.random()) - adjustedDamage;
                    zAngle = (zAngle > 0) ? (zAngle - Math.random()) : (zAngle + Math.random()) - adjustedDamage;
                    BloodyBitsMod.LOGGER.info("X Y AND Z FORCES: {}, {}, {}", xAngle, yAngle, zAngle);
                    // TODO: 0.5 seems to be a good sweet spot for an average hit. What I can do in the future could be
                    //       to make that number be related to weapon damage. With base being something like maybe 0.25
                    //       and higher numbers bringing it up to maybe a 0.75 cap.
                    bloodSprayEntity.setDeltaMovement(xAngle * 0.5, yAngle * 0.5, zAngle * 0.5);

//                    bloodSprayEntity.setDeltaMovement(
//                            (event.getAmount() * 0.25) * Math.random(),
//                            (event.getAmount() * 0.25) * Math.random(),
//                            (event.getAmount() * 0.25) * Math.random());
                    BloodyBitsMod.LOGGER.info(" ENTITY DELTA MOVEMENT{}", bloodSprayEntity.getDeltaMovement());
                    event.getEntity().level().addFreshEntity(bloodSprayEntity);
                }

//                if (event.getEntity() instanceof Pillager pillager) {
//                    pillager.getBoundingBox();
//                }
//                event.getSource().getDirectEntity().getDirection();
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
//    private static void spawnParticles(Level level, Vec3 pos, Vec3 particleDirection) {
////        if (yTest > zTest) {
////            zTest += 0.1;
////        }
////        else {
////            yTest += 0.1;
////        }
////        zTest += 0.1;
////        BloodyBitsMod.LOGGER.info("Z TEST: {}", zTest);
////        BloodyBitsMod.LOGGER.info("Y TEST: {}", yTest);
//        BloodyBitsMod.LOGGER.info("VEC3 X {} Y {} Z {}", pos.x, pos.y, pos.z);
//        BloodyBitsMod.LOGGER.info("SPRAY ANGLE: {}", particleDirection);
//        for (int i = 0; i < 10; i++) {
//
//            double randomNumberRange = (Math.random() * pos.x) + pos.y;
//            level.addParticle(BloodyBitsParticles.BLOOD_PARTICLES.get(), pos.x, pos.y, pos.z,
//                    Math.random() + particleDirection.x, Math.random() + particleDirection.y, Math.random() + particleDirection.z);
//        }
//    }
}
