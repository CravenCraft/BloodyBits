package com.cravencraft.bloodybits.mixins;

import com.cravencraft.bloodybits.entity.custom.BloodSprayEntity;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(AbstractArrow.class)
public abstract class AbstractArrowMixin extends Projectile {

    @Shadow protected abstract float getWaterInertia();

    protected AbstractArrowMixin(EntityType<? extends AbstractArrow> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    /**
     *  Removes bubble particles from blood sprays in water. Does this by only allowing particles from arrow entities
     *  that have a water inertia greater than 0 (which is all entities except my new blood ones).
     */
    @Redirect(method = "tick",at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)V", ordinal = 1))
    private void removeBubbleParticles(Level instance, ParticleOptions pParticleData, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
        if (this.getWaterInertia() > 0) {
            this.level().addParticle(ParticleTypes.BUBBLE, x - xSpeed * 0.25D, y - ySpeed * 0.25D, z - zSpeed * 0.25D, xSpeed, ySpeed, zSpeed);
        }
    }

    /**
     * Removes rotation from the blood spray if it has entered the water.
     */
    @Redirect(method = "tick",at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/AbstractArrow;setYRot(F)V", ordinal = 2))
    private void stopRotation(AbstractArrow instance, float v) {
        if (!(instance instanceof BloodSprayEntity) || !this.isInWater()){
            this.setYRot((float)(Mth.atan2(instance.getDeltaMovement().x, instance.getDeltaMovement().z) * (double)(180F / (float)Math.PI)));
        }
    }

    /**
     * Slows the sinking of the blood spray in water.
     */
    @Redirect(method = "tick",at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/AbstractArrow;setDeltaMovement(DDD)V"))
    private void slowSinking(AbstractArrow instance, double x, double y, double z) {
        if (instance instanceof BloodSprayEntity && this.isInWater()){
            instance.setDeltaMovement(x, 0, z);
        }
        else {
            instance.setDeltaMovement(x, y, z);
        }
    }
}
