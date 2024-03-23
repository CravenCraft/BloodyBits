package com.cravencraft.bloodybits.entities;

import com.cravencraft.bloodybits.BloodyBitsMod;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

public class BloodSprayEntity extends AbstractArrow {

    public int stretchLimit;
    public int xMinVal;
    public int xMaxVal;
    public Direction entityDirection;

    public BloodSprayEntity(EntityType<BloodSprayEntity> entityType, Level level) {
        super(entityType, level);
    }

//    protected BloodSprayEntity(EntityType<BloodSprayEntity> entityType, double x, double y, double z, Level level) {
//        super(entityType, x, y, z, level);
//    }

    public BloodSprayEntity(EntityType<BloodSprayEntity> entityType, LivingEntity shooter, Level level, float damageAmount) {
        super(entityType, shooter, level);
        this.stretchLimit = (int) damageAmount;
        BloodyBitsMod.LOGGER.info("HURT DIR: {}", shooter.getHurtDir());
        BloodyBitsMod.LOGGER.info("STRETCH LIMIT SOURCE: {}", this.stretchLimit);
//        shooter.getLastDamageSource().
    }

//    @Override
//    public void shoot(double pX, double pY, double pZ, float pVelocity, float pInaccuracy) {
//        super.shoot(pX, pY, pZ, pVelocity, pInaccuracy);
//        this.
//        BloodyBitsMod.LOGGER.info("VELOCITY: {}", pVelocity);
////        this.life = 0;
//    }

    @Override
    protected ItemStack getPickupItem() {
        return null;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.inGround) {
            if (this.xMinVal < this.xMaxVal) {
                this.xMinVal += 1;
            }
        }
        else {
            if (this.xMinVal > -this.stretchLimit) {
                BloodyBitsMod.LOGGER.info("MIN VAL SUB {} - {}", xMinVal, -stretchLimit);
                this.xMinVal -= 1;
            }

            if (this.xMaxVal < this.stretchLimit) {
                BloodyBitsMod.LOGGER.info("MAX VAL ADD {} - {}", xMaxVal, stretchLimit);
                this.xMaxVal += 1;
            }
        }
    }

    /**
     * TODO:
     *       - We probably want to set the direction as a field & then the render class can modify how the entity
     *       is rendered based on the direction hit.
     *
     *
     * Is called once on block hit. Can get the direction the entity has hit the block on, which will dictate how
     * it is expanded.
     *
     * @param result
     */
    @Override
    protected void onHitBlock(BlockHitResult result) {
        this.lastState = this.level().getBlockState(result.getBlockPos());
        this.entityDirection = result.getDirection();
//        BloodyBitsMod.LOGGER.info("\nBLOCK HIT POS: {}\nLOCATION: {}", result.getBlockPos(), result.getLocation());
        BloodyBitsMod.LOGGER.info("DIRECTION: {}", result.getDirection());
        super.onHitBlock(result);
        Vec3 vec3 = result.getLocation().subtract(this.getX(), this.getY(), this.getZ());
        this.setDeltaMovement(vec3);
        Vec3 vec31 = vec3.normalize().scale((double)0.05F);
        this.setPosRaw(this.getX() - vec31.x, this.getY() - vec31.y, this.getZ() - vec31.z);
        this.playSound(this.getHitGroundSoundEvent(), 1.0F, 1.2F / (this.random.nextFloat() * 0.2F + 0.9F));
        this.inGround = true;
//        this.shakeTime = 7;
        this.setCritArrow(false);
        this.setPierceLevel((byte)0);
        this.setSoundEvent(SoundEvents.ARROW_HIT);
        this.setShotFromCrossbow(false);
        this.resetPiercedEntities();
    }

    @Override
    protected void onHitEntity(EntityHitResult pResult) {

    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
