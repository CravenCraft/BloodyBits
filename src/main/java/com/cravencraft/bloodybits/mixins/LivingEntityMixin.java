package com.cravencraft.bloodybits.mixins;

import com.cravencraft.bloodybits.config.CommonConfig;
import com.cravencraft.bloodybits.entity.custom.BloodChunkEntity;
import com.cravencraft.bloodybits.entity.custom.BloodSprayEntity;
import com.cravencraft.bloodybits.network.BloodyBitsPacketHandler;
import com.cravencraft.bloodybits.network.messages.EntityMessage;
import com.cravencraft.bloodybits.registries.EntityRegistry;
import com.cravencraft.bloodybits.sounds.BloodyBitsSounds;
import com.cravencraft.bloodybits.utils.BloodyBitsUtils;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.PacketDistributor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
    private LivingEntity self;

    @Shadow public int deathTime;

    public LivingEntityMixin(EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Redirect(method = "hurt", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/common/ForgeHooks;onLivingAttack(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/damagesource/DamageSource;F)Z"))
    private boolean getSelf(LivingEntity entity, DamageSource src, float amount) {
        this.self = entity;
        return net.minecraftforge.common.ForgeHooks.onLivingAttack(entity, src, amount);
    }

    @Inject(method = "tickDeath", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;broadcastEntityEvent(Lnet/minecraft/world/entity/Entity;B)V"))
    private void addBloodChunksToDeath(CallbackInfo ci) {
        if (CommonConfig.showBloodChunks()) {
            this.createBloodChunk();
        }
    }

    @Inject(method = "makePoofParticles", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)V"), cancellable = true)
    private void removeDeathPoof(CallbackInfo ci) {
        if (CommonConfig.showBloodChunks() && this.deathTime >= 18) {
            ci.cancel();
        }
    }

    private void createBloodChunk() {
        int maxChunks = (int) Math.min(20, this.getBoundingBox().getSize() * 10);
        for (int i=0; i < maxChunks; i++) {
            // TODO: Setup Common Config for blood chunks as well.
            if (BloodyBitsUtils.BLOOD_CHUNK_ENTITIES.size() >= CommonConfig.maxChunks()) {
                BloodyBitsUtils.BLOOD_CHUNK_ENTITIES.get(0).discard();
                BloodyBitsUtils.BLOOD_CHUNK_ENTITIES.remove(0);
            }

            if (BloodyBitsUtils.BLOOD_SPRAY_ENTITIES.size() >= CommonConfig.maxSpatters()) {
                BloodyBitsUtils.BLOOD_SPRAY_ENTITIES.get(0).discard();
                BloodyBitsUtils.BLOOD_SPRAY_ENTITIES.remove(0);
            }
            BloodChunkEntity bloodChunkEntity = new BloodChunkEntity(EntityRegistry.BLOOD_CHUNK.get(), this.self, this.level(), 0);
            BloodyBitsUtils.BLOOD_CHUNK_ENTITIES.add(bloodChunkEntity);

            BloodSprayEntity bloodSprayEntity = new BloodSprayEntity(EntityRegistry.BLOOD_SPRAY.get(), this.self, this.level());
            BloodyBitsUtils.BLOOD_SPRAY_ENTITIES.add(bloodSprayEntity);

            double xAngle = BloodyBitsUtils.getRandomAngle(0.5);
            double yAngle = BloodyBitsUtils.getRandomAngle(0.5);
            double zAngle = BloodyBitsUtils.getRandomAngle(0.5);

            bloodChunkEntity.setDeltaMovement(xAngle, yAngle, zAngle);
            bloodSprayEntity.setDeltaMovement(xAngle, yAngle, zAngle);

            this.level().addFreshEntity(bloodChunkEntity);
            this.level().addFreshEntity(bloodSprayEntity);

            BloodyBitsPacketHandler.INSTANCE.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> bloodChunkEntity),
                    new EntityMessage(bloodChunkEntity.getId(), this.getId()));

            BloodyBitsPacketHandler.INSTANCE.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> bloodSprayEntity),
                    new EntityMessage(bloodSprayEntity.getId(), this.getId()));
        }
        this.playSound(BloodyBitsSounds.BODY_EXPLOSION.get(), 1.0F, 1.0F / (this.random.nextFloat() * 0.2F + 0.9F));
    }
}