package com.cravencraft.bloodybits.entity.custom;

import com.cravencraft.bloodybits.BloodyBitsMod;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

import java.util.ArrayList;

public class BloodSprayEntity extends AbstractArrow {
    public static final int BLOOD_SPATTER_AMOUNT = 20;
    public float xMin;
    public float xMax = 0; // TODO: I bet this needs to be at least 0.001 to not clip into blocks.
    public float yMin;
    public float yMax;
    public float zMin;
    public float zMax;
    public float yDrip;

    public double xHitAngle;
    public double yHitAngle;
    public double zHitAngle;
    public Direction entityDirection;
    public BlockPos hitBlockPos;
    public Vec3 hitPosition;

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
//        if (this.getOnPos().east())
//        BloodyBitsMod.LOGGER.info("EAST BLOCK POS: {}", this.getOnPos().east());
//        BloodyBitsMod.LOGGER.info("BLOCK AT ENTITY POS: {}", this.level().getBlockState(this.getOnPos()));

        if (this.inGround) {
            if (this.xMin < this.xMax) {
                this.xMin = this.xMax;
            }

            if (this.entityDirection != null) {
//                BloodyBitsMod.LOGGER.info("HIT BLOCK POS: {} ENTITY POS: {} HIT DIRECTION: {}", this.hitBlockPos, this.position(), this.entityDirection);
                setYMin();
                setYMax();
                setZMin();
                setZMax();
                setYDrip();
            }

            // Expands the blood spatter.
            // TODO: Make this part of the else statement actually. So it shrinks & drips down after the initial expansion
//            if (this.entityDirection != null && this.yMin > -16 && !this.entityDirection.equals(Direction.UP) && !this.entityDirection.equals(Direction.DOWN)) {
//                this.yMin -= 0.35F;
//            }

//            if (this.yMin > -4 && (this.position() < this.blockPosition())) {
//                this.yMin -= 0.25F;
//            }
//            if (this.yMax < 4) this.yMax += 0.25F;
//            if (this.zMin > -4) this.zMin -= 0.25F;
//            if (this.zMax < 4) this.zMax += 0.25F;
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
        this.hitBlockPos = result.getBlockPos();
        this.entityDirection = result.getDirection();
        this.hitPosition = this.position();
        BlockState blockState = this.level().getBlockState(result.getBlockPos());
//        blockstate.
        BlockPos blockPos = result.getBlockPos();
        BloodyBitsMod.LOGGER.info("BLOCK HIT: {}", blockState);
        BloodyBitsMod.LOGGER.info("BLOCK EAST {} WEST {} NORTH {} SOUTH {} ABOVE {} BELOW {}",
                this.level().getBlockState(blockPos.east()),
                this.level().getBlockState(blockPos.west()),
                this.level().getBlockState(blockPos.north()),
                this.level().getBlockState(blockPos.south()),
                this.level().getBlockState(blockPos.above()),
                this.level().getBlockState(blockPos.below()));
        BloodyBitsMod.LOGGER.info("BLOCK STATE COLLISION SHAPE: {}", blockState.getCollisionShape(this.level(), result.getBlockPos()));
        this.lastState = this.level().getBlockState(result.getBlockPos());
        BloodyBitsMod.LOGGER.info("BLOOD ANGLE ON HIT: {}", this.getLookAngle());
        this.xHitAngle = this.getLookAngle().x;
        this.yHitAngle = -this.getLookAngle().y;
        this.zHitAngle = this.getLookAngle().z;

        BloodyBitsMod.LOGGER.info("\nBLOCK HIT POS: {}\nLOCATION: {}", result.getBlockPos(), result.getLocation());
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

    private boolean nonExpandableBlocks(String blockName) {
        return blockName.contains("air") || blockName.contains("water") || blockName.contains("lava");
    }

    public void setYDrip() {
        boolean nonExpandableBlockAdjacent;
        boolean isSizeInBounds;

        if (this.yDrip > -BLOOD_SPATTER_AMOUNT) {
            if (!this.entityDirection.equals(Direction.UP) && !this.entityDirection.equals(Direction.DOWN)) {
                nonExpandableBlockAdjacent = nonExpandableBlocks(this.level().getBlockState(this.hitBlockPos.below()).getBlock().toString());
                isSizeInBounds = this.position().y - Math.abs(this.yDrip * 0.1) > this.hitBlockPos.getY();

                if (!nonExpandableBlockAdjacent || isSizeInBounds) {
                    this.yDrip -= 0.1F;
                }
            }
        }
    }

    public void setYMin() {
        boolean nonExpandableBlockAdjacent;
        double expansionAmount;
        BlockState blockExpandingTo;

        if (this.entityDirection.equals(Direction.UP) || this.entityDirection.equals(Direction.DOWN)) {
            blockExpandingTo = this.level().getBlockState(BlockPos.containing(this.position().x - (this.yMin - 1.0F) * 0.1F, this.hitBlockPos.getY(), this.position().z + this.zMin * 0.1F));
            expansionAmount = (this.xHitAngle < 0) ? BLOOD_SPATTER_AMOUNT * this.xHitAngle : 0;
        }
        else if (this.entityDirection.equals(Direction.NORTH) || this.entityDirection.equals(Direction.SOUTH)) {
            blockExpandingTo = this.level().getBlockState(BlockPos.containing(this.position().x + this.zMin * 0.1F, this.position().y + (this.yMin - 1.0F) * 0.1F, this.hitBlockPos.getZ()));
            expansionAmount = (this.yHitAngle < 0) ? BLOOD_SPATTER_AMOUNT * this.yHitAngle : 0;
        }
        else {
            blockExpandingTo = this.level().getBlockState(BlockPos.containing(this.hitBlockPos.getX(), this.position().y + (this.yMin - 1.0F) * 0.1F, this.position().z + this.zMin * 0.1F));
            expansionAmount = (this.yHitAngle < 0) ? BLOOD_SPATTER_AMOUNT * this.yHitAngle : 0;

        }
        nonExpandableBlockAdjacent = nonExpandableBlocks(blockExpandingTo.getBlock().toString());

        if (this.yMin > expansionAmount && !nonExpandableBlockAdjacent) {
            this.yMin -= 1.0F;
        }
    }

    public void setYMax() {
        boolean nonExpandableBlockAdjacent;
        double expansionAmount;
        BlockState blockExpandingTo;

        if (this.entityDirection.equals(Direction.UP) || this.entityDirection.equals(Direction.DOWN)) {
            blockExpandingTo = this.level().getBlockState(BlockPos.containing(this.position().x - (this.yMax + 1.0F) * 0.1F, this.hitBlockPos.getY(), this.position().z + this.zMax * 0.1F));
            expansionAmount = (this.xHitAngle > 0) ? BLOOD_SPATTER_AMOUNT * this.xHitAngle : 0;
        }
        else if (this.entityDirection.equals(Direction.NORTH) || this.entityDirection.equals(Direction.SOUTH)) {
            blockExpandingTo = this.level().getBlockState(BlockPos.containing(this.position().x + this.zMax * 0.1F, this.position().y + (this.yMax + 1.0F) * 0.1F, this.hitBlockPos.getZ()));
            expansionAmount = (this.yHitAngle > 0) ? BLOOD_SPATTER_AMOUNT * this.yHitAngle : 0;
        }
        else {
            blockExpandingTo = this.level().getBlockState(BlockPos.containing(this.hitBlockPos.getX(), this.position().y + (this.yMax + 1.0F) * 0.1F, this.position().z + this.zMax * 0.1F));
            expansionAmount = (this.yHitAngle > 0) ? BLOOD_SPATTER_AMOUNT * this.yHitAngle : 0;
        }
        nonExpandableBlockAdjacent = nonExpandableBlocks(blockExpandingTo.getBlock().toString());

        if (this.yMax < expansionAmount && !nonExpandableBlockAdjacent) {
            this.yMax += 1.0F;
        }
    }

    public void setZMin() {
        boolean nonExpandableBlockAdjacent;
        double expansionAmount;
        BlockState blockExpandingTo;

        if (this.entityDirection.equals(Direction.UP) || this.entityDirection.equals(Direction.DOWN)) {
            blockExpandingTo = this.level().getBlockState(BlockPos.containing(this.position().x + this.yMin * 0.1F, this.hitBlockPos.getY(), this.position().z + (this.zMin - 1.0F) * 0.1F));
            expansionAmount = (this.zHitAngle < 0) ? BLOOD_SPATTER_AMOUNT * this.zHitAngle : 0;
        }
        else if (this.entityDirection.equals(Direction.NORTH) || this.entityDirection.equals(Direction.SOUTH)) {
            // TODO: May have to account for going left and DOWN and UP. Just one more check.
            blockExpandingTo = this.level().getBlockState(BlockPos.containing(this.position().x + (this.zMin - 1.0F) * 0.1F, this.position().y + this.yMin * 0.1, this.hitBlockPos.getZ()));
            expansionAmount = (this.xHitAngle < 0) ? BLOOD_SPATTER_AMOUNT * this.xHitAngle : 0;
        }
        else {
            blockExpandingTo = this.level().getBlockState(BlockPos.containing(this.hitBlockPos.getX(), this.position().y + this.yMin * 0.1F, this.position().z + (this.zMin - 1.0F) * 0.1F));
            expansionAmount = (this.zHitAngle < 0) ? BLOOD_SPATTER_AMOUNT * this.xHitAngle : 0;
        }
        nonExpandableBlockAdjacent = nonExpandableBlocks(blockExpandingTo.getBlock().toString());

        if (this.zMin > expansionAmount && !nonExpandableBlockAdjacent) {
            this.zMin -= 1.0F;
        }
    }

    public void setZMax() {
        boolean nonExpandableBlockAdjacent;
        double expansionAmount;
        BlockState blockExpandingTo;

        if (this.entityDirection.equals(Direction.UP) || this.entityDirection.equals(Direction.DOWN)) {
            blockExpandingTo = this.level().getBlockState(BlockPos.containing(this.position().x + this.yMax * 0.1F, this.hitBlockPos.getY(), this.position().z + (this.zMax + 1.0F) * 0.1F));
            expansionAmount = (this.zHitAngle > 0) ? BLOOD_SPATTER_AMOUNT * this.zHitAngle : 0;
        }
        else if (this.entityDirection.equals(Direction.NORTH) || this.entityDirection.equals(Direction.SOUTH)) {
            blockExpandingTo = this.level().getBlockState(BlockPos.containing(this.position().x + (this.zMax + 1.0F) * 0.1F, this.position().y + this.yMax * 0.1F, this.hitBlockPos.getZ()));
            expansionAmount = (this.xHitAngle < 0) ? BLOOD_SPATTER_AMOUNT * this.xHitAngle : 0;
        }
        else {
            blockExpandingTo = this.level().getBlockState(BlockPos.containing(this.hitBlockPos.getX(), this.position().y + this.yMax * 0.1F, this.position().z + (this.zMax + 1.0F) * 0.1F));
            expansionAmount = (this.zHitAngle > 0) ? BLOOD_SPATTER_AMOUNT * this.zHitAngle : 0;
        }
        nonExpandableBlockAdjacent = nonExpandableBlocks(blockExpandingTo.getBlock().toString());

        if (this.zMax < expansionAmount && !nonExpandableBlockAdjacent) {
            this.zMax += 1.0F;
        }
    }

    public boolean isInGround() {
        return this.inGround;
    }
}
