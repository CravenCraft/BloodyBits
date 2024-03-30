package com.cravencraft.bloodybits.entity.custom;

import com.cravencraft.bloodybits.BloodyBitsMod;
import com.cravencraft.bloodybits.config.CommonConfig;
import com.cravencraft.bloodybits.config.ConfigManager;
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
    public static final int BLOOD_SPATTER_AMOUNT = 20;
    private static final int BLOOD_SPATTER_TEXTURES = 3; // TODO: private?
    public static final int WALL_SLIDE_DOWN_AMOUNT = 20;
    public static final int MAX_DRIP_LENGTH = 50;
    public static final int DESPAWN_TIME = (1200); // TODO: Set this in the client config.

    public int currentLifeTime;
    public int randomTextureNumber;
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

    // TODO: Might be doubling since it's called both client and server side, which is causing the
    //       fade out to be slower than the discard since the fade out only happens client side.
    @Override
    protected void tickDespawn() {
        if (this.level().isClientSide()) {
            ++this.currentLifeTime;
            if (this.currentLifeTime >= CommonConfig.despawnTime()) {
                this.discard();
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
        BloodyBitsMod.LOGGER.info("DESPAWN TIME: {}", CommonConfig.despawnTime());
        this.hitBlockPos = result.getBlockPos();
        this.entityDirection = result.getDirection();
        this.hitPosition = this.position();
        this.lastState = this.level().getBlockState(result.getBlockPos());
        this.xHitAngle = this.getLookAngle().x;
        this.yHitAngle = -this.getLookAngle().y;
        this.zHitAngle = this.getLookAngle().z;

        // All of this is boilerplate from AbstractArrow except the setSoundEvent now playing a slime sound.
        this.lastState = this.level().getBlockState(result.getBlockPos());
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
        boolean nonExpandableBlockAdjacent;
        double expansionAmount;
        int wallSlideAmount;
        BlockState blockExpandingTo;

        if (this.entityDirection.equals(Direction.UP) || this.entityDirection.equals(Direction.DOWN)) {
            blockExpandingTo = this.level().getBlockState(BlockPos.containing(this.position().x - (this.yMin - 1.0F) * 0.1F, this.hitBlockPos.getY(), this.position().z + this.zMin * 0.1F));
            expansionAmount = (this.xHitAngle < 0) ? BLOOD_SPATTER_AMOUNT * this.xHitAngle : 0;
            wallSlideAmount = 0;
        }
        else if (this.entityDirection.equals(Direction.NORTH) || this.entityDirection.equals(Direction.SOUTH)) {
            blockExpandingTo = this.level().getBlockState(BlockPos.containing(this.position().x + this.zMin * 0.1F, this.position().y + (this.yMin - 1.0F) * 0.1F, this.hitBlockPos.getZ()));
            expansionAmount = (this.yHitAngle < 0) ? (BLOOD_SPATTER_AMOUNT * this.yHitAngle) : 0;
            wallSlideAmount = WALL_SLIDE_DOWN_AMOUNT;
        }
        else {
            blockExpandingTo = this.level().getBlockState(BlockPos.containing(this.hitBlockPos.getX(), this.position().y + (this.yMin - 1.0F) * 0.1F, this.position().z + this.zMin * 0.1F));
            expansionAmount = (this.yHitAngle < 0) ? (BLOOD_SPATTER_AMOUNT * this.yHitAngle) : 0;
            wallSlideAmount = WALL_SLIDE_DOWN_AMOUNT;
        }
        nonExpandableBlockAdjacent = nonExpandableBlocks(blockExpandingTo.getBlock().toString());

        if (this.yMin > expansionAmount && !nonExpandableBlockAdjacent) {
            this.yMin -= 1.0F;
        }

        if (!nonExpandableBlockAdjacent) {
            if (this.yMin > expansionAmount) {
                this.yMin -= 1.0F;
            }
            else if (this.yMin > expansionAmount - wallSlideAmount) {
                this.yMin -= 0.025F;
            }
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
            blockExpandingTo = this.level().getBlockState(BlockPos.containing(this.position().x - (this.zMin - 1.0F) * 0.1F, this.position().y + this.yMin * 0.1, this.hitBlockPos.getZ()));
            expansionAmount = (this.xHitAngle > 0) ? BLOOD_SPATTER_AMOUNT * -this.xHitAngle : 0;
        }
        else {
            blockExpandingTo = this.level().getBlockState(BlockPos.containing(this.hitBlockPos.getX(), this.position().y + this.yMin * 0.1F, this.position().z - (this.zMin - 1.0F) * 0.1F));
            expansionAmount = (this.zHitAngle < 0) ? BLOOD_SPATTER_AMOUNT * this.zHitAngle : 0;
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
            expansionAmount = (this.xHitAngle < 0) ? BLOOD_SPATTER_AMOUNT * -this.xHitAngle : 0;
        }
        else {
            blockExpandingTo = this.level().getBlockState(BlockPos.containing(this.hitBlockPos.getX(), this.position().y + this.yMax * 0.1F, this.position().z + (this.zMax + 1.0F) * 0.1F));
            expansionAmount = (this.zHitAngle > 0) ? BLOOD_SPATTER_AMOUNT * this.zHitAngle : 0;
        }
        nonExpandableBlockAdjacent = nonExpandableBlocks(blockExpandingTo.getBlock().toString());

        if (this.zMax < expansionAmount && !nonExpandableBlockAdjacent) {
            this.zMax += 1.0F;
        }
//        this.zMax += 0.1F;
    }

    public boolean isInGround() {
        return this.inGround;
    }
}
