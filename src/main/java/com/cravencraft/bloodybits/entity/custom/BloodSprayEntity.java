package com.cravencraft.bloodybits.entity.custom;

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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

public class BloodSprayEntity extends AbstractArrow {

//    public int stretchLimit;
    public float xMin;
    public float xMax = 0; // TODO: I bet this needs to be at least 0.001 to not clip into blocks.
    public float yMin;
    public float yMax;
    public float zMin;
    public float zMax;
    public Direction entityDirection;

    public BloodSprayEntity(EntityType<BloodSprayEntity> entityType, Level level) {
        super(entityType, level);
    }

    protected BloodSprayEntity(EntityType<BloodSprayEntity> entityType, double x, double y, double z, Level level) {
        super(entityType, x, y, z, level);
    }

    public BloodSprayEntity(EntityType<BloodSprayEntity> entityType, LivingEntity shooter, Level level, float damageAmount) {
        super(entityType, shooter, level);
//        entityType.
//        BloodyBitsMod.LOGGER.info("BOUNDING BOX: {}", this.getBoundingBox());

//        this.setBoundingBox();
//        this.stretchLimit = (int) damageAmount;
//        BloodyBitsMod.LOGGER.info("HURT DIR: {}", shooter.getHurtDir());
//        BloodyBitsMod.LOGGER.info("STRETCH LIMIT SOURCE: {}", this.stretchLimit);
    }

    @Override
    protected ItemStack getPickupItem() {
        return null;
    }

    //TODO: Making the note here as to not forget, but does not pertain to the tick() method.
    //      When an arrow hits an entity it sticks into it. We can look at that code to possibly
    //      Create our own method that sticks our blood splatter to an entity. However, we want to
    //      do this mainly when the blood is spawned and not when it actually hits an entity.
    @Override
    public void tick() {
        super.tick();

//        BloodyBitsMod.LOGGER.info("BOUNDING BOX: {}", this.getBoundingBox());
        if (this.inGround) {
//            if (this.xMin < this.xMax) {
//                this.xMin = this.xMax;
//            }
            // TODO: Uncomment when I figure out texturing.
//            if (this.yMin > -4) this.yMin -= 0.1F;
//            if (this.yMax < 4) this.yMax += 0.1F;
//            if (this.zMin > -4) this.zMin -= 0.1F;
//            if (this.zMax < 4) this.zMax += 0.1F;
        }
        else {
//            BloodyBitsMod.LOGGER.info("MOVEMENT INFO: " + this.getDeltaMovement());
            double velocity = this.getDeltaMovement().length();
//            BloodyBitsMod.LOGGER.info("VELOCITY: {}", velocity);
            float length = (float) (velocity * 10);
            this.xMin = -(length); // 6.5 here helps us reach 10 assuming we want the velocity modification cap to be around 1.5
//            BloodyBitsMod.LOGGER.info("MIN X: {}", this.xMin);

            // TODO: The larger the divisor the smaller the thickness.
            float widthAndHeight = (length > 10) ? (length - 10) / 4 : (10 - length) / 4;
//            BloodyBitsMod.LOGGER.info("WIDTH AND HEIGHT: {}", widthAndHeight);
            this.yMin = -(widthAndHeight / 2);
            this.yMax = (widthAndHeight / 2);
            this.zMin = -(widthAndHeight / 2);
            this.zMax = (widthAndHeight / 2);

//            if (this.xMin > -this.stretchLimit) {
//                BloodyBitsMod.LOGGER.info("X MIN VAL {}", xMin);
//                this.xMin -= 1;
//            }
//
//            if (this.xMax < this.stretchLimit) {
//                BloodyBitsMod.LOGGER.info("MAX VAL {}", xMax);
//                this.xMax += 1;
//            }
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
        BlockState blockState = this.level().getBlockState(result.getBlockPos());
        BloodyBitsMod.LOGGER.info("BLOCK STATE COLLISION SHAPE: {}", blockState.getCollisionShape(this.level(), result.getBlockPos()));
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

    public boolean isInGround() {
        return this.inGround;
    }
}
