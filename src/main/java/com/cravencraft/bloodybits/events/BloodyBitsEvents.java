package com.cravencraft.bloodybits.events;

import com.cravencraft.bloodybits.BloodyBitsMod;
import com.cravencraft.bloodybits.entity.custom.BloodSprayEntity;
import com.cravencraft.bloodybits.registries.EntityRegistry;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = BloodyBitsMod.MODID)
public class BloodyBitsEvents {
    @SubscribeEvent
    public static void particleOnEntityDamage(LivingAttackEvent event) {
        // TODO: We want to check damage type. There is a HUGE issue when calling a command to
        //       kill all entities in that it spawns countless blood sprays. Need to add the config
        //       too in order to limit the total number of blood spray entities that are able to spawn.
        if (!event.getEntity().level().isClientSide()) {
            for (int i = 0; i < event.getAmount(); i++) {
                BloodSprayEntity bloodSprayEntity = new BloodSprayEntity(EntityRegistry.BLOOD_SPRAY.get(), event.getEntity(), event.getEntity().level(), event.getAmount());

                Vec3 sourceAngle;
                if (event.getSource().getEntity() != null) {
                    sourceAngle = (event.getSource().getDirectEntity() != null) ? event.getSource().getDirectEntity().getLookAngle() : event.getSource().getEntity().getLookAngle();
                }
                else {
                    sourceAngle = event.getEntity().getLookAngle();
                }

                double xAngle = -sourceAngle.x;
                double yAngle = -sourceAngle.y + Math.random();
                double zAngle = -sourceAngle.z;
                double adjustedDamage = event.getAmount() * 0.1;
                // Ensure the angles are always going where they are expected to go.
                xAngle = (xAngle > 0) ? (xAngle - Math.random()) : (xAngle + Math.random()) - adjustedDamage;
//                    yAngle = (yAngle > 0) ? (yAngle - Math.random()) : (yAngle + Math.random()) - adjustedDamage;
                zAngle = (zAngle > 0) ? (zAngle - Math.random()) : (zAngle + Math.random()) - adjustedDamage;
//                BloodyBitsMod.LOGGER.info("X Y AND Z FORCES: {}, {}, {}", xAngle, yAngle, zAngle);
                // TODO: 0.5 seems to be a good sweet spot for an average hit. What I can do in the future could be
                //       to make that number be related to weapon damage. With base being something like maybe 0.25
                //       and higher numbers bringing it up to maybe a 0.75 cap.
                bloodSprayEntity.setDeltaMovement(xAngle * 0.5, yAngle * 0.5, zAngle * 0.5);

//                    bloodSprayEntity.setDeltaMovement(
//                            (event.getAmount() * 0.25) * Math.random(),
//                            (event.getAmount() * 0.25) * Math.random(),
//                            (event.getAmount() * 0.25) * Math.random());
//                BloodyBitsMod.LOGGER.info(" ENTITY DELTA MOVEMENT{}", bloodSprayEntity.getDeltaMovement());
                event.getEntity().level().addFreshEntity(bloodSprayEntity);
            }

        }
    }
}
