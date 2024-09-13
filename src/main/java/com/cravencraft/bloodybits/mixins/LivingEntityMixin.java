package com.cravencraft.bloodybits.mixins;

import com.cravencraft.bloodybits.BloodyBitsMod;
import com.cravencraft.bloodybits.config.ClientConfig;
import com.cravencraft.bloodybits.config.CommonConfig;
import com.cravencraft.bloodybits.entity.custom.BloodChunkEntity;
import com.cravencraft.bloodybits.entity.custom.BloodSprayEntity;
import com.cravencraft.bloodybits.network.BloodyBitsPacketHandler;
import com.cravencraft.bloodybits.network.messages.EntityMessage;
import com.cravencraft.bloodybits.registries.EntityRegistry;
import com.cravencraft.bloodybits.sounds.BloodyBitsSounds;
import com.cravencraft.bloodybits.utils.BloodyBitsUtils;
import net.minecraft.sounds.SoundEvents;
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

import javax.annotation.Nullable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
    private LivingEntity self;

    @Shadow public int deathTime;

    @Shadow @Nullable private DamageSource lastDamageSource;

    public LivingEntityMixin(EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    /**
     * Mixin method solely created as a sneaky way to get the instance of the LivingEntity class. This method will always
     * be called before a living entity dies. So, this is a safe way to acquire it for what I want to use it for.
     */
    @Redirect(method = "hurt", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/common/ForgeHooks;onLivingAttack(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/damagesource/DamageSource;F)Z"), remap = false)
    private boolean getSelf(LivingEntity entity, DamageSource src, float amount) {
        this.self = entity;
        return net.minecraftforge.common.ForgeHooks.onLivingAttack(entity, src, amount);
    }

    /**
     * If the Common Config is set to allow blood chunks, then this mixin adds blood chunks at the final tick of the
     * entity's death.
     *
     * Also, has an extra check to the self entity value to ensure it's not null before trying to create the chunks.
     */
    @Inject(method = "tickDeath", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;broadcastEntityEvent(Lnet/minecraft/world/entity/Entity;B)V"))
    private void addBloodChunksToDeath(CallbackInfo ci) {
        if (this.self != null) {
            String entityId = (this.toString().contains("Player")) ? "player" : this.getEncodeId();
            if (CommonConfig.deathBloodExplosion() && !CommonConfig.blackListEntities().contains(entityId)) {
                assert this.lastDamageSource != null;
                if (!CommonConfig.blackListDamageSources().contains(this.lastDamageSource.type().msgId())) {
                    this.createBloodExplosion();
                }
            }
        }
    }

    /**
     * If the Common Config is set to allow blood chunks, then the entity's death poof particles are prevented from spawning.
     */
    @Inject(method = "makePoofParticles", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)V"), cancellable = true)
    private void removeDeathPoof(CallbackInfo ci) {
        if (CommonConfig.deathBloodExplosion() && this.deathTime >= 18) {
            ci.cancel();
        }
    }

    /**
     * Creates blood chunks and sprays on an entity's death.
     */
    private void createBloodExplosion() {
        int maxBloodChunks = (int) Math.min(20, this.getBoundingBox().getSize() * 10);
        String ownerName = (this.toString().contains("Player")) ? "player" : this.getEncodeId();
        for (int i=0; i < maxBloodChunks; i++) {
            if (BloodyBitsUtils.BLOOD_SPRAY_ENTITIES.size() >= CommonConfig.maxSpatters()) {
                BloodyBitsUtils.BLOOD_SPRAY_ENTITIES.get(0).discard();
                BloodyBitsUtils.BLOOD_SPRAY_ENTITIES.remove(0);
            }

            BloodSprayEntity bloodSprayEntity = new BloodSprayEntity(EntityRegistry.BLOOD_SPRAY.get(), this.self, this.level());
            BloodyBitsUtils.BLOOD_SPRAY_ENTITIES.add(bloodSprayEntity);

            double xAngle = BloodyBitsUtils.getRandomAngle(0.5);
            double yAngle = BloodyBitsUtils.getRandomAngle(0.5);
            double zAngle = BloodyBitsUtils.getRandomAngle(0.5);


            bloodSprayEntity.setDeltaMovement(xAngle, yAngle, zAngle);

            this.level().addFreshEntity(bloodSprayEntity);


            BloodyBitsPacketHandler.INSTANCE.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> bloodSprayEntity),
                    new EntityMessage(bloodSprayEntity.getId(), this.getId()));

            if (CommonConfig.showBloodChunks()) {
                if (BloodyBitsUtils.BLOOD_CHUNK_ENTITIES.size() >= CommonConfig.maxChunks()) {
                    BloodyBitsUtils.BLOOD_CHUNK_ENTITIES.get(0).discard();
                    BloodyBitsUtils.BLOOD_CHUNK_ENTITIES.remove(0);
                }

                BloodChunkEntity bloodChunkEntity = new BloodChunkEntity(EntityRegistry.BLOOD_CHUNK.get(), this.self, this.level(), 0);
                BloodyBitsUtils.BLOOD_CHUNK_ENTITIES.add(bloodChunkEntity);
                bloodChunkEntity.setDeltaMovement(xAngle, yAngle, zAngle);
                this.level().addFreshEntity(bloodChunkEntity);
                BloodyBitsPacketHandler.INSTANCE.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> bloodChunkEntity),
                        new EntityMessage(bloodChunkEntity.getId(), this.getId()));
            }
        }
        float volume = (float) CommonConfig.bloodExplosionVolume();
        if (CommonConfig.solidEntities().contains(ownerName)) {
            this.playSound(SoundEvents.BONE_BLOCK_BREAK, volume, 1.0F / (this.random.nextFloat() * 0.2F + 0.9F));
        }
        else {
            this.playSound(BloodyBitsSounds.BODY_EXPLOSION.get(), volume, 1.0F / (this.random.nextFloat() * 0.2F + 0.9F));
        }
    }
}