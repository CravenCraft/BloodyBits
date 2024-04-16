package com.cravencraft.bloodybits.mixins;

import com.cravencraft.bloodybits.BloodyBitsMod;
import com.cravencraft.bloodybits.config.CommonConfig;
import com.cravencraft.bloodybits.entity.custom.BloodChunkEntity;
import com.cravencraft.bloodybits.entity.custom.BloodSprayEntity;
import com.cravencraft.bloodybits.network.BloodyBitsPacketHandler;
import com.cravencraft.bloodybits.network.messages.EntityMessage;
import com.cravencraft.bloodybits.registries.EntityRegistry;
import com.cravencraft.bloodybits.utils.BloodyBitsUtils;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Attackable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Objects;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {

    @Shadow public abstract float getHealth();

    public LivingEntityMixin(EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

//    @Nullable
//    @Override
//    public LivingEntity getLastAttacker() {
//        return null;
//    }
//
//    @Override
//    protected void defineSynchedData() {
//
//    }
//
//    @Override
//    public void readAdditionalSaveData(CompoundTag pCompound) {
//
//    }
//
//    @Override
//    protected void addAdditionalSaveData(CompoundTag pCompound) {
//
//    }

    @Redirect(method = "makePoofParticles", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)V"))
    private void replacePoofWithBlood(Level instance, ParticleOptions pParticleData, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
        if (this.getHealth() <= 0) {
            BloodyBitsMod.LOGGER.info("IN LIVING ENTITY MIXIN LESS THAN 0 HEALTH");
//            if (!instance.isClientSide()) {
//                for (Player player : Objects.requireNonNull(instance.getServer()).getPlayerList().getPlayers()) {
//                    if (this.distanceTo(player) < CommonConfig.distanceToPlayers()) {
                        this.createBloodChunk();
//                        break;
//                    }
//                }
//            }
        }
    }

    private void createBloodChunk() {
        String ownerName = (this.toString().contains("Player")) ? "player" : this.getEncodeId();
        if (CommonConfig.gasEntities().contains(ownerName)) {
            return;
        }

        for (int i=0; i < 10; i++) {
            // TODO: Setup Common Config for blood chunks as well.
            if (BloodyBitsUtils.BLOOD_CHUNK_ENTITIES.size() >= CommonConfig.maxSpatters()) {
                BloodyBitsUtils.BLOOD_CHUNK_ENTITIES.get(0).discard();
                BloodyBitsUtils.BLOOD_CHUNK_ENTITIES.remove(0);
            }

            if (BloodyBitsUtils.BLOOD_SPRAY_ENTITIES.size() >= CommonConfig.maxSpatters()) {
                BloodyBitsUtils.BLOOD_SPRAY_ENTITIES.get(0).discard();
                BloodyBitsUtils.BLOOD_SPRAY_ENTITIES.remove(0);
            }
            BloodChunkEntity bloodChunkEntity = new BloodChunkEntity(EntityRegistry.BLOOD_CHUNK.get(), this.position().x, this.position().y, this.position().z, this.level());
            BloodyBitsUtils.BLOOD_CHUNK_ENTITIES.add(bloodChunkEntity);

            BloodSprayEntity bloodSprayEntity = new BloodSprayEntity(EntityRegistry.BLOOD_SPRAY.get(), this.position().x, this.position().y, this.position().z, this.level());
            BloodyBitsUtils.BLOOD_SPRAY_ENTITIES.add(bloodSprayEntity);

            double xAngle = BloodyBitsUtils.getRandomAngle(0.5);
            double yAngle = BloodyBitsUtils.getRandomAngle(0.5);
            double zAngle = BloodyBitsUtils.getRandomAngle(0.5);

            bloodChunkEntity.setDeltaMovement(xAngle, yAngle, zAngle);
            bloodSprayEntity.setDeltaMovement(xAngle, yAngle, zAngle);

            this.level().addFreshEntity(bloodChunkEntity);
            this.level().addFreshEntity(bloodSprayEntity);
            BloodyBitsMod.LOGGER.info("SENDING INFO");
            BloodyBitsPacketHandler.INSTANCE.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> bloodChunkEntity),
                    new EntityMessage(bloodChunkEntity.getId(), this.getId()));

            BloodyBitsPacketHandler.INSTANCE.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> bloodSprayEntity),
                    new EntityMessage(bloodSprayEntity.getId(), this.getId()));
        }
    }
}