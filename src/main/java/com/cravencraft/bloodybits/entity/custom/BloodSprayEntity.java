package com.cravencraft.bloodybits.entity.custom;

import com.cravencraft.bloodybits.BloodyBitsMod;
import com.cravencraft.bloodybits.config.CommonConfig;
import com.cravencraft.bloodybits.utils.BloodyBitsUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.*;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import org.joml.Random;

public class BloodSprayEntity extends AbstractArrow {

    private static final int BLOOD_SPATTER_TEXTURES = 3; // TODO: private?
    public static final int WALL_SLIDE_DOWN_AMOUNT = 20;
    public static final int MAX_DRIP_LENGTH = 50;

    public static final double BLOOD_SPATTER_AMOUNT = 0.3;
    public static final float SPATTER_SPEED = 1.5F;

    public int currentLifeTime;
    public int randomTextureNumber;

    public float yMinLimit;
    public float yMaxLimit;
    public float zMinLimit;
    public float zMaxLimit;
    public float xMin;
    public float xMax = 0; // TODO: I bet this needs to be at least 0.001 to not clip into blocks.
    public float yMin;
    public float yMax;
    public float zMin;
    public float zMax;
    public float drip;

    public double xHitAngle;
    public double yHitAngle;
    public double zHitAngle;

    public boolean shouldDrip;
    public Direction entityDirection;
    public BlockPos hitBlockPos;
    public Vec3 hitPosition;

    public BloodSprayEntity(EntityType<BloodSprayEntity> entityType, Level level) {
        super(entityType, level);
        this.randomTextureNumber = new Random().nextInt(BLOOD_SPATTER_TEXTURES);
    }

    protected BloodSprayEntity(EntityType<BloodSprayEntity> entityType, double x, double y, double z, Level level) {
        super(entityType, x, y, z, level);
    }

    public BloodSprayEntity(EntityType<BloodSprayEntity> entityType, LivingEntity shooter, Level level, float damageAmount) {
        super(entityType, shooter, level);
    }

    @Override
    protected void tickDespawn() {
        if (this.level().isClientSide()) {
            ++this.currentLifeTime;
            if (this.currentLifeTime >= CommonConfig.despawnTime()) {
                this.discard();
                BloodyBitsUtils.BLOOD_SPRAY_ENTITIES.remove(this);
            }
        }
    }

    /**
     * Do nothing when the player interacts with the entity.
     * TODO: Maybe later down the line we can add bloody footprints if the player steps on one in the UP direction?
     *
     * @param player
     */
    @Override
    public void playerTouch(@NotNull Player player) {}

    /**
     * Hopefully this is never called because of the above playerTouch override method.
     *
     * @return
     */
    @Override
    protected ItemStack getPickupItem() {
        return ItemStack.EMPTY;
    }

    @Override
    protected float getWaterInertia() {
        return -0.01F;
    }

    //TODO: Making the note here as to not forget, but does not pertain to the tick() method.
    //      When an arrow hits an entity it sticks into it. We can look at that code to possibly
    //      Create our own method that sticks our blood splatter to an entity. However, we want to
    //      do this mainly when the blood is spawned and not when it actually hits an entity.
    @Override
    public void tick() {
        super.tick();
        if (this.inGround) {
            if (this.xMin < this.xMax) {
                this.xMin = this.xMax;
            }

            if (!this.shouldFall()) {
                this.tickDespawn();
            }


            if (this.entityDirection != null) {
                setYMin();
                setYMax();
                setZMin();
                setZMax();
                setDrip();
            }
        }
        else if (!this.isInWater()) {
            this.currentLifeTime = 0;
            double velocity = this.getDeltaMovement().length();
            float length = (float) (velocity * 10);
            this.xMin = -(length);

            // TODO: The larger the divisor the smaller the thickness.
            float widthAndHeight = (length > 10) ? (length - 10) / 4 : (10 - length) / 4;
            this.yMin = -(widthAndHeight / 2);
            this.yMax = (widthAndHeight / 2);
            this.zMin = -(widthAndHeight / 2);
            this.zMax = (widthAndHeight / 2);
        }
        else {
            // TODO: Put all if statement logic in their one private methods to better organize.
            this.yMin -= 0.1F;
            this.yMax += 0.1F;
            this.xMin -= 0.01F;
            this.xMax += 0.01F;
            this.zMin -= 0.1F;
            this.zMax += 0.1F;

            // Rapidly decrease the life of the entity in water.
            this.currentLifeTime += (CommonConfig.despawnTime() / 50);
            this.tickDespawn();
        }
    }

    /**
     * TODO:
     *       - We probably want to set the direction as a field & then the render class can modify how the entity
     *         is rendered based on the direction hit.
     *
     *
     * Is called once on block hit. Can get the direction the entity has hit the block on, which will dictate how
     * it is expanded.
     *
     * @param result
     */
    @Override
    protected void onHitBlock(BlockHitResult result) {
        BloodyBitsMod.LOGGER.info("BLOCK HIT: {}", result.getBlockPos());
        this.hitBlockPos = result.getBlockPos();
        this.entityDirection = result.getDirection();
        this.hitPosition = result.getLocation();
        BloodyBitsMod.LOGGER.info("BLOCK HIT LOCATION: {}", result.getLocation());
        this.lastState = this.level().getBlockState(result.getBlockPos());
        this.xHitAngle = -this.getLookAngle().x;
        this.yHitAngle = -this.getLookAngle().y;
        this.zHitAngle = this.getLookAngle().z;

        BloodyBitsMod.LOGGER.info("HIT ANGLE: {}", this.getLookAngle());

        boolean isYNorm = (result.getDirection().equals(Direction.EAST) || result.getDirection().equals(Direction.WEST) || result.getDirection().equals(Direction.SOUTH) || result.getDirection().equals(Direction.NORTH));
        boolean isZNorm = (result.getDirection().equals(Direction.EAST) || result.getDirection().equals(Direction.WEST) || result.getDirection().equals(Direction.UP) || result.getDirection().equals(Direction.DOWN));
        double initialYMinVal;
        double initialZMinVal;
        double initialYMaxVal;
        double initialZMaxVal;

        if (isYNorm) {
            if (this.yHitAngle > 0) {
                initialYMinVal = hitPosition.y - BLOOD_SPATTER_AMOUNT;
                initialYMaxVal = hitPosition.y + BLOOD_SPATTER_AMOUNT + this.yHitAngle;
                BloodyBitsMod.LOGGER.info("Y MIN POSITIVE HIT ANGLE: {}", initialYMinVal);
                BloodyBitsMod.LOGGER.info("Y MAX POSITIVE HIT ANGLE: {}", initialYMaxVal);
            }
            else {
                initialYMinVal = hitPosition.y - BLOOD_SPATTER_AMOUNT + this.yHitAngle;
                initialYMaxVal = hitPosition.y + BLOOD_SPATTER_AMOUNT;
                BloodyBitsMod.LOGGER.info("Y MIN NEGATIVE HIT ANGLE: {}", initialYMinVal);
                BloodyBitsMod.LOGGER.info("Y MAX NEGATIVE HIT ANGLE: {}", initialYMaxVal);
            }
        }
        else {
            if (this.xHitAngle > 0) {
                initialYMinVal = hitPosition.x - BLOOD_SPATTER_AMOUNT;
                initialYMaxVal = hitPosition.x + BLOOD_SPATTER_AMOUNT + this.xHitAngle;
                BloodyBitsMod.LOGGER.info("X MIN POSITIVE HIT ANGLE: {}", initialYMinVal);
                BloodyBitsMod.LOGGER.info("X MAX POSITIVE HIT ANGLE: {}", initialYMaxVal);
            }
            else {
                initialYMinVal = hitPosition.x - BLOOD_SPATTER_AMOUNT + this.xHitAngle;
                initialYMaxVal = hitPosition.x + BLOOD_SPATTER_AMOUNT;
                BloodyBitsMod.LOGGER.info("X MIN NEGATIVE HIT ANGLE: {}", initialYMinVal);
                BloodyBitsMod.LOGGER.info("X MAX NEGATIVE HIT ANGLE: {}", initialYMaxVal);
            }
        }

        if (isZNorm) {
            if (this.zHitAngle > 0) {
                initialZMinVal = hitPosition.z - BLOOD_SPATTER_AMOUNT;
                initialZMaxVal = hitPosition.z + BLOOD_SPATTER_AMOUNT + this.zHitAngle;
                BloodyBitsMod.LOGGER.info("Z MIN POSITIVE HIT ANGLE: {}", initialZMinVal);
                BloodyBitsMod.LOGGER.info("Z MAX POSITIVE HIT ANGLE: {}", initialZMaxVal);
            }
            else {
                initialZMinVal = hitPosition.z - BLOOD_SPATTER_AMOUNT + this.zHitAngle;
                initialZMaxVal = hitPosition.z + BLOOD_SPATTER_AMOUNT;
                BloodyBitsMod.LOGGER.info("Z MIN NEGATIVE HIT ANGLE: {}", initialZMinVal);
                BloodyBitsMod.LOGGER.info("Z MAX NEGATIVE HIT ANGLE: {}", initialZMaxVal);
            }
        }
        else {
            //TODO: Looks like this can be broken up into a separate method? Maybe included in the determineSpatterExpansion. Include hitAngle
            if (this.xHitAngle > 0) {
                initialZMinVal = hitPosition.x - BLOOD_SPATTER_AMOUNT;
                initialZMaxVal = hitPosition.x + BLOOD_SPATTER_AMOUNT + this.xHitAngle;
                BloodyBitsMod.LOGGER.info("X MIN POSITIVE HIT ANGLE: {}", initialZMinVal);
                BloodyBitsMod.LOGGER.info("X MAX POSITIVE HIT ANGLE: {}", initialZMaxVal);
            }
            else {
                initialZMinVal = hitPosition.x - BLOOD_SPATTER_AMOUNT + this.xHitAngle;
                initialZMaxVal = hitPosition.x + BLOOD_SPATTER_AMOUNT;
                BloodyBitsMod.LOGGER.info("X MIN NEGATIVE HIT ANGLE: {}", initialZMinVal);
                BloodyBitsMod.LOGGER.info("X MAX NEGATIVE HIT ANGLE: {}", initialZMaxVal);
            }
        }

        if (this.entityDirection.equals(Direction.UP) || this.entityDirection.equals(Direction.DOWN)) {
            this.yMaxLimit = -(float) determineSpatterExpansion(initialYMinVal, true, false) * 10; // Y MIN
            this.yMinLimit = -(float) determineSpatterExpansion(initialYMaxVal, true, true) * 10; // Y MAX
        }
        else {
            this.yMinLimit = (float) determineSpatterExpansion(initialYMinVal, true, false) * 10; // Y MIN
            this.yMaxLimit = (float) determineSpatterExpansion(initialYMaxVal, true, true) * 10; // Y MAX
        }

        this.zMinLimit = (float) determineSpatterExpansion(initialZMinVal, false, false) * 10; // Z MIN
        this.zMaxLimit = (float) determineSpatterExpansion(initialZMaxVal, false, true) * 10; // Z MAX

        BloodyBitsMod.LOGGER.info("\nY MIN: {}\nY MAX: {}\nZ MIN: {}\nZ MAX: {}",yMinLimit, yMaxLimit, zMinLimit, zMaxLimit);

        // All of this is boilerplate from AbstractArrow except the setSoundEvent now playing a slime sound.
        BlockState blockstate = this.level().getBlockState(result.getBlockPos());
        blockstate.onProjectileHit(this.level(), blockstate, result, this);
        Vec3 vec3 = result.getLocation().subtract(this.getX(), this.getY(), this.getZ());
        this.setDeltaMovement(vec3);
        Vec3 vec31 = vec3.normalize().scale((double)0.05F);
        this.setPosRaw(this.getX() - vec31.x, this.getY() - vec31.y, this.getZ() - vec31.z);

        // Modified sound to be a deeper pitch of Slime Block sounds.
        this.setSoundEvent((Math.random() > 0.5) ? SoundEvents.SLIME_BLOCK_HIT : SoundEvents.SLIME_BLOCK_STEP);
        this.playSound(this.getHitGroundSoundEvent(), 0.75F, 1.8F / (this.random.nextFloat() * 0.2F + 0.9F));

        this.inGround = true;
        this.setCritArrow(false);
        this.setPierceLevel((byte)0);
        this.setShotFromCrossbow(false);
        this.resetPiercedEntities();
    }

    private double determineSpatterExpansion(double initialExpansionAmount, boolean isYAxis, boolean isMax)  {
        boolean isNonExpandable;
        double modifiedExpansionAmount;

        if (this.entityDirection.equals(Direction.EAST) || entityDirection.equals(Direction.WEST)) {
//            BloodyBitsMod.LOGGER.info("INITIAL EXPANSION AMOUNT: {}", initialExpansionAmount);

            if (isYAxis) {
                modifiedExpansionAmount = getMaxBlockBoundsExpAmount(this.hitBlockPos.getY(), initialExpansionAmount, isMax);
                isNonExpandable = nonExpandableBlocks(this.level().getBlockState(BlockPos.containing(this.hitBlockPos.getX(), modifiedExpansionAmount, hitPosition.z)).getBlock().toString());
                modifiedExpansionAmount = (isNonExpandable) ? getBlockBoundsExpAmount(this.hitBlockPos.getY(), isMax) : modifiedExpansionAmount;
                return modifiedExpansionAmount - this.hitPosition.y;
            }
            else {
                modifiedExpansionAmount = getMaxBlockBoundsExpAmount(this.hitBlockPos.getZ(), initialExpansionAmount, isMax);
                isNonExpandable = nonExpandableBlocks(this.level().getBlockState(BlockPos.containing(this.hitBlockPos.getX(), hitPosition.y, modifiedExpansionAmount)).getBlock().toString());
                modifiedExpansionAmount = (isNonExpandable) ? getBlockBoundsExpAmount(this.hitBlockPos.getZ(), isMax) : modifiedExpansionAmount;
                return modifiedExpansionAmount - this.hitPosition.z;
            }

//            BloodyBitsMod.LOGGER.info("IS BLOCK NON-EXPANDABLE: {} EXPANSION AMOUNT: {}", isNonExpandable, modifiedExpansionAmount);
        }
        else if (this.entityDirection.equals(Direction.NORTH) || entityDirection.equals(Direction.SOUTH)) {
//            BloodyBitsMod.LOGGER.info("INITIAL EXPANSION AMOUNT: {}", initialExpansionAmount);

            if (isYAxis) {
                modifiedExpansionAmount = getMaxBlockBoundsExpAmount(this.hitBlockPos.getY(), initialExpansionAmount, isMax);
                isNonExpandable = nonExpandableBlocks(this.level().getBlockState(BlockPos.containing(hitPosition.x, modifiedExpansionAmount, this.hitBlockPos.getZ())).getBlock().toString());
                modifiedExpansionAmount = (isNonExpandable) ? getBlockBoundsExpAmount(this.hitBlockPos.getY(), isMax) : modifiedExpansionAmount;
                return modifiedExpansionAmount - this.hitPosition.y;
            }
            else {
                modifiedExpansionAmount = getMaxBlockBoundsExpAmount(this.hitBlockPos.getX(), initialExpansionAmount, isMax);
                isNonExpandable = nonExpandableBlocks(this.level().getBlockState(BlockPos.containing(modifiedExpansionAmount, hitPosition.y, this.hitBlockPos.getZ())).getBlock().toString());
                modifiedExpansionAmount = (isNonExpandable) ? getBlockBoundsExpAmount(this.hitBlockPos.getX(), isMax) : modifiedExpansionAmount;
                return modifiedExpansionAmount - this.hitPosition.x;
//                BloodyBitsMod.LOGGER.info("REDUCING Z MIN INITIAL EXPANSION: {} MODIFIED EXPANSION: {}", initialExpansionAmount, modifiedExpansionAmount);
            }
//            BloodyBitsMod.LOGGER.info("IS BLOCK NON-EXPANDABLE: {} EXPANSION AMOUNT: {}", isNonExpandable, modifiedExpansionAmount);
        }
        else {
//            BloodyBitsMod.LOGGER.info("INITIAL EXPANSION AMOUNT: {}", initialExpansionAmount);

            if (isYAxis) {
                modifiedExpansionAmount = getMaxBlockBoundsExpAmount(this.hitBlockPos.getX(), initialExpansionAmount, isMax);
                BloodyBitsMod.LOGGER.info("Y MIN FOR X MIN FIRST EXPANSION: {}", modifiedExpansionAmount);
                isNonExpandable = nonExpandableBlocks(this.level().getBlockState(BlockPos.containing(modifiedExpansionAmount, this.hitBlockPos.getY(), hitPosition.z)).getBlock().toString());
                BloodyBitsMod.LOGGER.info("IS NON EXPANDABLE: {}", isNonExpandable);
                modifiedExpansionAmount = (isNonExpandable) ? getBlockBoundsExpAmount(this.hitBlockPos.getX(), isMax) : modifiedExpansionAmount;
                BloodyBitsMod.LOGGER.info("SECOND MODIFIED EXPANSION: {}", modifiedExpansionAmount);
                return modifiedExpansionAmount - this.hitPosition.x;
            }
            else {
                modifiedExpansionAmount = getMaxBlockBoundsExpAmount(this.hitBlockPos.getZ(), initialExpansionAmount, isMax);
                isNonExpandable = nonExpandableBlocks(this.level().getBlockState(BlockPos.containing(hitPosition.x, this.hitBlockPos.getY(), modifiedExpansionAmount)).getBlock().toString());
                modifiedExpansionAmount = (isNonExpandable) ? getBlockBoundsExpAmount(this.hitBlockPos.getZ(), isMax) : modifiedExpansionAmount;
                return modifiedExpansionAmount - this.hitPosition.z;
            }

//            modifiedExpansionAmount = (isNonExpandable) ? getBlockBoundsExpAmount(this.hitBlockPos.getX(), isMax) : modifiedExpansionAmount;
//            BloodyBitsMod.LOGGER.info("IS BLOCK NON-EXPANDABLE: {} EXPANSION AMOUNT: {}", isNonExpandable, modifiedExpansionAmount);
        }
//        return modifiedExpansionAmount;
    }

    private double getMaxBlockBoundsExpAmount(int blockPos, double expansionAmount, boolean isMax) {
        if (isMax) {
            return (expansionAmount > blockPos + 1) ? blockPos + 1 : expansionAmount;
        }
        else {
            return (expansionAmount < blockPos - 1) ? blockPos - 1 : expansionAmount;
        }
    }

    private double getBlockBoundsExpAmount(int blockPos, boolean isMax) {

        return (isMax) ? blockPos + 1 : blockPos;
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

    public void setDrip() {
        if (this.drip < MAX_DRIP_LENGTH) {
            if (this.currentLifeTime > 50 && this.entityDirection.equals(Direction.DOWN)) {
                if (this.shouldDrip) {
                    this.drip += 1.0F;
                }
                else {
                    double random = Math.random();
                    this.shouldDrip = random > 0.99;
                }
            }
        }
        else {
            this.drip = 0;
            this.shouldDrip = false;
        }
    }

    public void setYMin() {
        BlockState blockExpandingTo = this.level().getBlockState(this.blockPosition());

        if (this.entityDirection.equals(Direction.EAST) || this.entityDirection.equals(Direction.WEST)) {
            blockExpandingTo = this.level().getBlockState(BlockPos.containing(this.hitBlockPos.getX(), this.hitPosition.y + (this.yMin - 0.025F) * 0.1F, this.hitPosition.z + this.zMin * 0.1F));
        }
        else if (this.entityDirection.equals(Direction.NORTH) || this.entityDirection.equals(Direction.SOUTH)) {
            blockExpandingTo = this.level().getBlockState(BlockPos.containing(this.hitPosition.x + this.zMin * 0.1F, this.hitPosition.y + (this.yMin - 0.025F) * 0.1F, this.hitBlockPos.getZ()));
        }

        if (!this.entityDirection.equals(Direction.UP) && !this.entityDirection.equals(Direction.DOWN)) {
            if (this.yMin > this.yMinLimit) {
                this.yMin -= (this.yMin - SPATTER_SPEED < this.yMinLimit) ? this.yMin - this.yMinLimit : SPATTER_SPEED;
            }
            else if (!nonExpandableBlocks(blockExpandingTo.getBlock().toString())) {
                this.yMin -= (this.yMin - 0.025F < this.yMinLimit - WALL_SLIDE_DOWN_AMOUNT) ? this.yMin - (this.yMinLimit - WALL_SLIDE_DOWN_AMOUNT) : 0.025F;
            }
        }
        else if (this.yMin < this.yMinLimit) {
            this.yMin += (this.yMin + SPATTER_SPEED > this.yMinLimit) ? this.yMin + this.yMinLimit : SPATTER_SPEED;
        }
    }

    public void setYMax() {
        if (!this.entityDirection.equals(Direction.UP) && !this.entityDirection.equals(Direction.DOWN)) {
            if (this.yMax < this.yMaxLimit) {
                this.yMax += (this.yMax + SPATTER_SPEED > this.yMaxLimit) ? this.yMaxLimit - this.yMax : SPATTER_SPEED;
            }
        }
        else if (this.yMax < this.yMaxLimit) {
            this.yMax += (this.yMax + SPATTER_SPEED > this.yMax) ? this.yMax + this.yMaxLimit : SPATTER_SPEED;
        }
    }

    public void setZMin() {
        if (this.zMin > this.zMinLimit) {
            this.zMin -= (this.zMin - SPATTER_SPEED < this.zMinLimit) ? this.zMin - this.zMinLimit : SPATTER_SPEED;
        }
    }

    public void setZMax() {
        if (this.zMax < this.zMaxLimit) {
            this.zMax += (this.zMax + SPATTER_SPEED > this.zMaxLimit) ? this.zMaxLimit - this.zMax : SPATTER_SPEED;
        }
    }

    public boolean isInGround() {
        return this.inGround;
    }
}
