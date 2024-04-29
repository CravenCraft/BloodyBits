package com.cravencraft.bloodybits.events;

import com.cravencraft.bloodybits.BloodyBitsMod;
import com.cravencraft.bloodybits.config.CommonConfig;
import com.cravencraft.bloodybits.entity.custom.BloodSprayEntity;
import com.cravencraft.bloodybits.network.BloodyBitsPacketHandler;
import com.cravencraft.bloodybits.network.messages.EntityMessage;
import com.cravencraft.bloodybits.registries.EntityRegistry;
import com.cravencraft.bloodybits.utils.BloodyBitsUtils;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

import java.util.Objects;

@Mod.EventBusSubscriber(modid = BloodyBitsMod.MODID)
public class BloodyBitsEvents {

    /**
     * Looks for all the players on a given server and creates blood sprays if the damage event is
     * close enough to any of the players. Will break out of the loop the second a player is found,
     * which should optimize this somewhat.
     */
    @SubscribeEvent
    public static void bloodOnEntityDamage(LivingAttackEvent event) {
        if (!event.getEntity().level().isClientSide()) {
            for (Player player : Objects.requireNonNull(event.getEntity().level().getServer()).getPlayerList().getPlayers()) {
                if (event.getEntity().distanceTo(player) < CommonConfig.distanceToPlayers()) {
                    createBloodSpray(event);
                    break;
                }
            }
        }
    }

    private static void createBloodSpray(LivingAttackEvent event) {
        int maxDamage = (int) Math.min(20, event.getAmount());
        //TODO: Currently, creepers don't produce blood when exploding because it's not registered as a LivingAttackEvent on THEMSELF.
        //      So, maybe have an exception happen in a damage event?
        for (int i = 0; i < maxDamage; i++) {
            if (BloodyBitsUtils.BLOOD_SPRAY_ENTITIES.size() >= CommonConfig.maxSpatters()) {
                BloodyBitsUtils.BLOOD_SPRAY_ENTITIES.get(0).discard();
                BloodyBitsUtils.BLOOD_SPRAY_ENTITIES.remove(0);
            }

            BloodSprayEntity bloodSprayEntity = new BloodSprayEntity(EntityRegistry.BLOOD_SPRAY.get(), event.getEntity(), event.getEntity().level());
            BloodyBitsUtils.BLOOD_SPRAY_ENTITIES.add(bloodSprayEntity);
            Vec3 sourceAngle;
            if (event.getSource().getEntity() != null) {
                sourceAngle = (event.getSource().getDirectEntity() != null) ? event.getSource().getDirectEntity().getLookAngle() : event.getSource().getEntity().getLookAngle();
            }
            else {
                sourceAngle = event.getEntity().getLookAngle();
            }

            double xAngle = sourceAngle.x;
            double yAngle = -sourceAngle.y + Math.random();
            double zAngle = sourceAngle.z;
            double adjustedDamage = maxDamage * 0.1;
            // Ensure the angles are always going where they are expected to go.
            xAngle = (xAngle > 0) ? (xAngle - Math.random()) : (xAngle + Math.random()) - adjustedDamage;
            zAngle = (zAngle > 0) ? (zAngle - Math.random()) : (zAngle + Math.random()) - adjustedDamage;
            // TODO: 0.5 seems to be a good sweet spot for an average hit. What I can do in the future could be
            //       to make that number be related to weapon damage. With base being something like maybe 0.25
            //       and higher numbers bringing it up to maybe a 0.75 cap.
            bloodSprayEntity.setDeltaMovement(xAngle * 0.25, yAngle * 0.35, zAngle * 0.25);
            event.getEntity().level().addFreshEntity(bloodSprayEntity);

            BloodyBitsPacketHandler.INSTANCE.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> bloodSprayEntity),
                    new EntityMessage(bloodSprayEntity.getId(), event.getEntity().getId()));
        }
    }
}