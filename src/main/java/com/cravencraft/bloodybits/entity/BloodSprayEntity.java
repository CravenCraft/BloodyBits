package com.cravencraft.bloodybits.entity;

import com.cravencraft.bloodybits.BloodyBitsMod;
import com.cravencraft.bloodybits.config.ClientConfig;
import com.cravencraft.bloodybits.config.CommonConfig;
import com.cravencraft.bloodybits.utils.BloodyBitsUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.*;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class BloodSprayEntity extends Projectile {
    private static final EntityDataAccessor<Byte> ID_FLAGS = SynchedEntityData.defineId(net.minecraft.world.entity.projectile.AbstractArrow.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Byte> PIERCE_LEVEL = SynchedEntityData.defineId(net.minecraft.world.entity.projectile.AbstractArrow.class, EntityDataSerializers.BYTE);
    @Nullable
    public BlockState lastState;
    protected boolean inGround;
    protected int inGroundTime;
    private int life;
    private SoundEvent soundEvent;

    private static final int BLOOD_SPATTER_TEXTURES = 7;
    public static final int WALL_SLIDE_DOWN_AMOUNT = 20;
    public static final int MAX_DRIP_LENGTH = 50;

    public static final double BLOOD_SPATTER_AMOUNT = 0.3;
    public static final float SPATTER_SPEED = 1.5F;

    public int randomTextureNumber;

    public float yMinLimit;
    public float yMaxLimit;
    public float zMinLimit;
    public float zMaxLimit;
    public float xMin;
    public float xMax = 0;
    public float yMin;
    public float yMax;
    public float zMin;
    public float zMax;
    public float drip;

    public double xHitAngle;
    public double yHitAngle;
    public double zHitAngle;

    public String ownerName;

    public boolean shouldDrip;
    public boolean isSolid;
    public boolean wasInGround;
    public Direction entityDirection;
    public BlockPos hitBlockPos;
    public Vec3 hitPosition;
    public Vec3 previousPosition;
    public int inAirTicks;
    public int red = 200;
    public int green = 1;
    public int blue = 1;

    public BloodSprayEntity(EntityType<BloodSprayEntity> entityType, Level level) {
        super(entityType, level);
        this.randomTextureNumber = new Random().nextInt(BLOOD_SPATTER_TEXTURES);
        this.previousPosition = this.position();
    }

    public BloodSprayEntity(EntityType<BloodSprayEntity> entityType, double pX, double pY, double pZ, Level pLevel) {
        this(entityType, pLevel);
        this.setPos(pX, pY, pZ);
    }

    public BloodSprayEntity(EntityType<BloodSprayEntity> entityType, LivingEntity shooter, Level level) {
        this(entityType, shooter.getX(), shooter.getEyeY() - (double)0.1F, shooter.getZ(), level);
        this.setOwner(shooter);
        this.previousPosition = this.position();

    }

    public int getLife() {
        return this.life;
    }

    public void setSoundEvent(SoundEvent pSoundEvent) {
        this.soundEvent = pSoundEvent;
    }

    /**
     * Checks if the entity is in range to render.
     */
    public boolean shouldRenderAtSqrDistance(double pDistance) {
        double d0 = this.getBoundingBox().getSize() * 10.0D;
        if (Double.isNaN(d0)) {
            d0 = 1.0D;
        }

        d0 *= 64.0D * getViewScale();
        return pDistance < d0 * d0;
    }

    protected void defineSynchedData() {
        this.entityData.define(ID_FLAGS, (byte)0);
        this.entityData.define(PIERCE_LEVEL, (byte)0);
    }

    /**
     * Similar to setArrowHeading, it's point the throwable entity to a x, y, z direction.
     */
    public void shoot(double pX, double pY, double pZ, float pVelocity, float pInaccuracy) {
        super.shoot(pX, pY, pZ, pVelocity, pInaccuracy);
        this.life = 0;
    }

    /**
     * Sets a target for the client to interpolate towards over the next few ticks
     */
    public void lerpTo(double pX, double pY, double pZ, float pYaw, float pPitch, int pPosRotationIncrements, boolean pTeleport) {
        this.setPos(pX, pY, pZ);
        this.setRot(pYaw, pPitch);
    }

    /**
     * Updates the entity motion clientside, called by packets from the server
     */
    public void lerpMotion(double pX, double pY, double pZ) {
        super.lerpMotion(pX, pY, pZ);
        this.life = 0;
    }

    /**
     * Called to update the entity's position/logic.
     */
    public void tick() {
        if (this.isRemoved()) {
            return;
        }

        if (!this.level().isClientSide() && this.ownerName == null) {
            this.discard();
            BloodyBitsUtils.BLOOD_SPRAY_ENTITIES.remove(this);
        }
        else {
            super.tick();
            Vec3 vec3 = this.getDeltaMovement();
            if (this.xRotO == 0.0F && this.yRotO == 0.0F) {
                double d0 = vec3.horizontalDistance();
                this.setYRot((float)(Mth.atan2(vec3.x, vec3.z) * (double)(180F / (float)Math.PI)));
                this.setXRot((float)(Mth.atan2(vec3.y, d0) * (double)(180F / (float)Math.PI)));
                this.yRotO = this.getYRot();
                this.xRotO = this.getXRot();
            }

            BlockPos blockpos = this.blockPosition();
            BlockState blockstate = this.level().getBlockState(blockpos);
            if (!blockstate.isAir()) {
                VoxelShape voxelshape = blockstate.getCollisionShape(this.level(), blockpos);
                if (!voxelshape.isEmpty()) {
                    Vec3 vec31 = this.position();

                    for(AABB aabb : voxelshape.toAabbs()) {
                        if (aabb.move(blockpos).contains(vec31)) {
                            this.inGround = true;
                            break;
                        }
                    }
                }
            }

            if (this.inGround) {
                if (this.lastState != blockstate && this.shouldFall()) {
                    this.startFalling();
                } else if (!this.level().isClientSide) {
                    this.tickDespawn();
                }

                ++this.inGroundTime;

                if (!this.isSolid && this.xMin < this.xMax) {
                    this.xMin = this.xMax;
                }

                if (!this.isSolid && this.entityDirection != null) {
                    setYMin();
                    setYMax();
                    setZMin();
                    setZMax();
                    setDrip();
                }

                // Rapidly decrease life if the blood entity is in rain or water.
                if (!this.isSolid && this.isInWaterOrRain()) {
                    this.yMin -= 0.1F;
                    this.yMax += 0.1F;
                    this.zMin -= 0.1F;
                    this.zMax += 0.1F;

                    this.life += (CommonConfig.despawnTime() / 50);
                }

                // Done to set the lifetime client side as well. Needed to properly fade the rendered blood.
                if (!this.shouldFall() && this.level().isClientSide()) {
                    ++this.life;
                }

            }
            else {
                boilerplateTickCodeFromAbstractArrow(vec3);

                if (this.isSolid) {
                    double velocity = this.getDeltaMovement().length();
                    float length = 2;
                    this.xMin = -(length);

                    float widthAndHeight = (10 - length) / 4;
                    this.yMin = -(widthAndHeight / 2);
                    this.yMax = (widthAndHeight / 2);
                    this.zMin = -(widthAndHeight / 2);
                    this.zMax = (widthAndHeight / 2);

                    if (this.isInWater()) {
                        this.discard();
                    }
                }
                else if (!this.isInWater()) {
                    double velocity = this.getDeltaMovement().length();
                    float length = (float) (velocity * 10);
                    this.xMin = -(length);

                    float widthAndHeight = (length > 10) ? (length - 10) / 4 : (10 - length) / 4;
                    this.yMin = -(widthAndHeight / 2);
                    this.yMax = (widthAndHeight / 2);
                    this.zMin = -(widthAndHeight / 2);
                    this.zMax = (widthAndHeight / 2);

                    if (this.inAirTicks > 100) {
                        this.discard();
                        BloodyBitsUtils.BLOOD_SPRAY_ENTITIES.remove(this);
                    }
                    else {
                        this.inAirTicks++;
                    }
                }
                else {
                    this.yMin -= 0.1F;
                    this.yMax += 0.1F;
                    this.xMin -= 0.01F;
                    this.xMax += 0.01F;
                    this.zMin -= 0.1F;
                    this.zMax += 0.1F;

                    // Rapidly decrease the life of the entity in water.
                    this.life += (CommonConfig.despawnTime() / 50);
                    this.tickDespawn();
                }
            }
        }
    }

    /**
     * Most of the boilerplate code taken from the AbstractArrow class since we don't
     * want to actually extend that class because it causes unwanted interactions with entities.
     */
    public void boilerplateTickCodeFromAbstractArrow(Vec3 vec3) {
        this.inGroundTime = 0;
        Vec3 vec32 = this.position();
        Vec3 vec33 = vec32.add(vec3);
        HitResult hitresult = this.level().clip(new ClipContext(vec32, vec33, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));

        while(!this.isRemoved()) {
            if (hitresult != null && hitresult.getType() != HitResult.Type.MISS) {
                switch (net.minecraftforge.event.ForgeEventFactory.onProjectileImpactResult(this, hitresult)) {
                    case SKIP_ENTITY:
                        if (hitresult.getType() != HitResult.Type.ENTITY) { // If there is no entity, we just return default behaviour
                            this.onHit(hitresult);
                            this.hasImpulse = true;
                            break;
                        }
                        break;
                    case STOP_AT_CURRENT_NO_DAMAGE:
                        this.discard();
                        break;
                    case STOP_AT_CURRENT:
                        this.setPierceLevel((byte) 0);
                    case DEFAULT:
                        this.onHit(hitresult);
                        this.hasImpulse = true;
                        break;
                }
            }

            if (this.getPierceLevel() <= 0) {
                break;
            }

            hitresult = null;
        }
        if (this.isRemoved()) return;

        vec3 = this.getDeltaMovement();
        double d5 = vec3.x;
        double d6 = vec3.y;
        double d1 = vec3.z;

        double d7 = this.getX() + d5;
        double d2 = this.getY() + d6;
        double d3 = this.getZ() + d1;
        double d4 = vec3.horizontalDistance();

        // Removes rotation from the blood spray if it has entered the water.
        if (!this.isInWater()) {
            this.setYRot((float)(Mth.atan2(d5, d1) * (double)(180F / (float)Math.PI)));
        }
        else {
            this.setYRot((float)(Mth.atan2(-d5, -d1) * (double)(180F / (float)Math.PI)));
        }

        this.setXRot((float)(Mth.atan2(d6, d4) * (double)(180F / (float)Math.PI)));
        this.setXRot(lerpRotation(this.xRotO, this.getXRot()));
        this.setYRot(lerpRotation(this.yRotO, this.getYRot()));
        float f = 0.99F;
        if (this.isInWater()) {
            f = this.getWaterInertia();
        }

        this.setDeltaMovement(vec3.scale(f));
        if (!this.isNoGravity()) {
            Vec3 vec34 = this.getDeltaMovement();
            if (this.isInWater()) {
                this.setDeltaMovement(vec34.x, 0, vec34.z);
            }
            else {
                this.setDeltaMovement(vec34.x, vec34.y - (double)0.05F, vec34.z);
            }
        }

        this.setPos(d7, d2, d3);
        this.checkInsideBlocks();
    }

    public boolean shouldFall() {
        return this.inGround && this.level().noCollision((new AABB(this.position(), this.position())).inflate(0.06D));
    }

    private void startFalling() {
        this.inGround = false;
        Vec3 vec3 = this.getDeltaMovement();
        this.setDeltaMovement(vec3.multiply(this.random.nextFloat() * 0.2F, (double)(this.random.nextFloat() * 0.2F), (double)(this.random.nextFloat() * 0.2F)));
        this.life = 0;
    }

    public void move(@NotNull MoverType pType, @NotNull Vec3 pPos) {
        super.move(pType, pPos);
        if (pType != MoverType.SELF && this.shouldFall()) {
            this.startFalling();
        }

    }

    protected void tickDespawn() {
        ++this.life;
        if (this.life >= CommonConfig.despawnTime()) {
            this.discard();
            BloodyBitsUtils.BLOOD_SPRAY_ENTITIES.remove(this);
        }

    }

    @Override
    protected void onHitBlock(@NotNull BlockHitResult result) {
        if (this.isSolid) {
            this.setSoundEvent((Math.random() > 0.5) ? SoundEvents.BONE_BLOCK_FALL : SoundEvents.BONE_BLOCK_HIT);
            float volume = (float) CommonConfig.bloodSpatterVolume();
            this.playSound(this.getHitGroundSoundEvent(), volume, 1.8F / (this.random.nextFloat() * 0.2F + 0.9F));

            if (result.getDirection().equals(Direction.UP)) {
                this.inGround = true;
                this.wasInGround = true;

                Vec3 vec3 = result.getLocation().subtract(this.getX(), this.getY(), this.getZ());

                this.setDeltaMovement(vec3);

                Vec3 vec31 = vec3.normalize().scale(0.05F);
                this.setPosRaw(this.getX() - vec31.x, this.getY() - vec31.y, this.getZ() - vec31.z);
            }
            else {
                Vec3 vec3 = result.getLocation().subtract(this.getX(), this.getY(), this.getZ());

                this.setDeltaMovement(0, -0.25, 0);
                Vec3 vec31 = vec3.normalize().scale(0.1F);
                this.setPosRaw(this.getX() - vec31.x, this.getY() - vec31.y, this.getZ() - vec31.z);
            }
        }
        else {
            this.hitBlockPos = result.getBlockPos();
            this.entityDirection = result.getDirection();
            this.hitPosition = result.getLocation();
            this.lastState = this.level().getBlockState(result.getBlockPos());
            this.xHitAngle = -this.getLookAngle().x;
            this.yHitAngle = -this.getLookAngle().y;
            this.zHitAngle = this.getLookAngle().z;

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
//                BloodyBitsMod.LOGGER.info("Y MIN POSITIVE HIT ANGLE: {}", initialYMinVal);
//                BloodyBitsMod.LOGGER.info("Y MAX POSITIVE HIT ANGLE: {}", initialYMaxVal);
                }
                else {
                    initialYMinVal = hitPosition.y - BLOOD_SPATTER_AMOUNT + this.yHitAngle;
                    initialYMaxVal = hitPosition.y + BLOOD_SPATTER_AMOUNT;
//                BloodyBitsMod.LOGGER.info("Y MIN NEGATIVE HIT ANGLE: {}", initialYMinVal);
//                BloodyBitsMod.LOGGER.info("Y MAX NEGATIVE HIT ANGLE: {}", initialYMaxVal);
                }
            }
            else {
                if (this.xHitAngle > 0) {
                    initialYMinVal = hitPosition.x - BLOOD_SPATTER_AMOUNT;
                    initialYMaxVal = hitPosition.x + BLOOD_SPATTER_AMOUNT + this.xHitAngle;
//                BloodyBitsMod.LOGGER.info("X MIN POSITIVE HIT ANGLE: {}", initialYMinVal);
//                BloodyBitsMod.LOGGER.info("X MAX POSITIVE HIT ANGLE: {}", initialYMaxVal);
                }
                else {
                    initialYMinVal = hitPosition.x - BLOOD_SPATTER_AMOUNT + this.xHitAngle;
                    initialYMaxVal = hitPosition.x + BLOOD_SPATTER_AMOUNT;
//                BloodyBitsMod.LOGGER.info("X MIN NEGATIVE HIT ANGLE: {}", initialYMinVal);
//                BloodyBitsMod.LOGGER.info("X MAX NEGATIVE HIT ANGLE: {}", initialYMaxVal);
                }
            }

            if (isZNorm) {
                if (this.zHitAngle > 0) {
                    initialZMinVal = hitPosition.z - BLOOD_SPATTER_AMOUNT;
                    initialZMaxVal = hitPosition.z + BLOOD_SPATTER_AMOUNT + this.zHitAngle;
//                BloodyBitsMod.LOGGER.info("Z MIN POSITIVE HIT ANGLE: {}", initialZMinVal);
//                BloodyBitsMod.LOGGER.info("Z MAX POSITIVE HIT ANGLE: {}", initialZMaxVal);
                }
                else {
                    initialZMinVal = hitPosition.z - BLOOD_SPATTER_AMOUNT + this.zHitAngle;
                    initialZMaxVal = hitPosition.z + BLOOD_SPATTER_AMOUNT;
//                BloodyBitsMod.LOGGER.info("Z MIN NEGATIVE HIT ANGLE: {}", initialZMinVal);
//                BloodyBitsMod.LOGGER.info("Z MAX NEGATIVE HIT ANGLE: {}", initialZMaxVal);
                }
            }
            else {
                //TODO: Looks like this can be broken up into a separate method? Maybe included in the determineSpatterExpansion. Include hitAngle
                if (this.xHitAngle > 0) {
                    initialZMinVal = hitPosition.x - BLOOD_SPATTER_AMOUNT;
                    initialZMaxVal = hitPosition.x + BLOOD_SPATTER_AMOUNT + this.xHitAngle;
//                BloodyBitsMod.LOGGER.info("X MIN POSITIVE HIT ANGLE: {}", initialZMinVal);
//                BloodyBitsMod.LOGGER.info("X MAX POSITIVE HIT ANGLE: {}", initialZMaxVal);
                }
                else {
                    initialZMinVal = hitPosition.x - BLOOD_SPATTER_AMOUNT + this.xHitAngle;
                    initialZMaxVal = hitPosition.x + BLOOD_SPATTER_AMOUNT;
//                BloodyBitsMod.LOGGER.info("X MIN NEGATIVE HIT ANGLE: {}", initialZMinVal);
//                BloodyBitsMod.LOGGER.info("X MAX NEGATIVE HIT ANGLE: {}", initialZMaxVal);
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


            Vec3 vec3 = result.getLocation().subtract(this.getX(), this.getY(), this.getZ());
            this.setDeltaMovement(vec3);
            Vec3 vec31 = vec3.normalize().scale(0.05F);
            this.setPosRaw(this.getX() - vec31.x, this.getY() - vec31.y, this.getZ() - vec31.z);

            // Modified sound to be a deeper pitch of Mud and Wet Grass sounds.
            this.setSoundEvent(BloodyBitsUtils.getRandomSound(new Random().nextInt(3)));
            float volume = (float) CommonConfig.bloodSpatterVolume();
            this.playSound(this.getHitGroundSoundEvent(), volume, 1.8F / (this.random.nextFloat() * 0.2F + 0.9F));

            this.inGround = true;
            this.wasInGround = true;
        }
    }

    protected final SoundEvent getHitGroundSoundEvent() {
        return this.soundEvent;
    }

    protected boolean canHitEntity(Entity entity) {
        return false;
    }

    public void addAdditionalSaveData(CompoundTag pCompound) {
        super.addAdditionalSaveData(pCompound);
        pCompound.putShort("life", (short)this.life);
        if (this.lastState != null) {
            pCompound.put("inBlockState", NbtUtils.writeBlockState(this.lastState));
        }

        pCompound.putBoolean("inGround", this.inGround);
    }

    /**
     * (abstract) Protected helper method to read subclass entity data from NBT.
     */
    public void readAdditionalSaveData(CompoundTag pCompound) {
        super.readAdditionalSaveData(pCompound);
        this.life = pCompound.getShort("life");
        if (pCompound.contains("inBlockState", 10)) {
            this.lastState = NbtUtils.readBlockState(this.level().holderLookup(Registries.BLOCK), pCompound.getCompound("inBlockState"));
        }

        this.inGround = pCompound.getBoolean("inGround");
    }

    /**
     * If the owner of the entity is not null, then the string field 'ownerName' will be set to the
     * entity's encoded ID (e.x., minecraft:villager), and if the entity is a player, then it will
     * simply be set to 'player'.
     *
     * This value is used by the configs in order to determine which mobs have custom blood colors,
     * and if a mob is found in the list, then its color will be set based on the hex format specified
     * in the config.
     */
    public void setOwner(@Nullable Entity ownerEntity) {
        super.setOwner(ownerEntity);

        if (ownerEntity != null) {
            this.ownerName = (ownerEntity.toString().contains("Player")) ? "player" : ownerEntity.getEncodeId();
            this.isSolid = CommonConfig.solidEntities().contains(this.ownerName);
            if (this.level().isClientSide()) {
                for (Map.Entry<String, List<String>> mapElement : ClientConfig.entityBloodColors().entrySet()) {
                    if (mapElement.getValue().contains(this.ownerName)) {
                        String bloodColorHexVal = mapElement.getKey();
                        this.red = HexFormat.fromHexDigits(bloodColorHexVal, 1, 3);
                        this.green = HexFormat.fromHexDigits(bloodColorHexVal, 3, 5);
                        this.blue = HexFormat.fromHexDigits(bloodColorHexVal.substring(5));
                        break;
                    }
                }
            }
        }
    }

    protected Entity.MovementEmission getMovementEmission() {
        return Entity.MovementEmission.NONE;
    }


    /**
     * Returns {@code true} if it's possible to attack this entity with an item.
     */
    public boolean isAttackable() {
        return false;
    }

    protected float getEyeHeight(Pose pPose, EntityDimensions pSize) {
        return 0.13F;
    }


    public void setPierceLevel(byte pPierceLevel) {
        this.entityData.set(PIERCE_LEVEL, pPierceLevel);
    }

    public byte getPierceLevel() {
        return this.entityData.get(PIERCE_LEVEL);
    }

    protected float getWaterInertia() {
        return 0.1F;
    }

    /*
        Below are all private methods dedicated to the blood spray entity.
     */

    /**
     * Gets the maximum bounds that a blood spatter can expand to when hitting a block. This is determined by the block
     * hit and the blocks in the direction of the spatter expansion.
     *
     * @param initialExpansionAmount The initial amount the blood spatter will try to expand.
     * @param isYAxis If the hit position is on the y-axis.
     * @param isMax Whether the point is a max or min val (e.x., xMax).
     */
    private double determineSpatterExpansion(double initialExpansionAmount, boolean isYAxis, boolean isMax)  {
        boolean isNonExpandable;
        double modifiedExpansionAmount;

        if (this.entityDirection.equals(Direction.EAST) || entityDirection.equals(Direction.WEST)) {
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
        }
        else if (this.entityDirection.equals(Direction.NORTH) || entityDirection.equals(Direction.SOUTH)) {

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
            }
        }
        else {
            if (isYAxis) {
                modifiedExpansionAmount = getMaxBlockBoundsExpAmount(this.hitBlockPos.getX(), initialExpansionAmount, isMax);
                isNonExpandable = nonExpandableBlocks(this.level().getBlockState(BlockPos.containing(modifiedExpansionAmount, this.hitBlockPos.getY(), hitPosition.z)).getBlock().toString());
                modifiedExpansionAmount = (isNonExpandable) ? getBlockBoundsExpAmount(this.hitBlockPos.getX(), isMax) : modifiedExpansionAmount;
                return modifiedExpansionAmount - this.hitPosition.x;
            }
            else {
                modifiedExpansionAmount = getMaxBlockBoundsExpAmount(this.hitBlockPos.getZ(), initialExpansionAmount, isMax);
                isNonExpandable = nonExpandableBlocks(this.level().getBlockState(BlockPos.containing(hitPosition.x, this.hitBlockPos.getY(), modifiedExpansionAmount)).getBlock().toString());
                modifiedExpansionAmount = (isNonExpandable) ? getBlockBoundsExpAmount(this.hitBlockPos.getZ(), isMax) : modifiedExpansionAmount;
                return modifiedExpansionAmount - this.hitPosition.z;
            }
        }
    }

    /**
     * Gets the maximum bounds that a blood spatter can expand to when hitting a block. This is determined by the block
     * hit and the blocks in the direction of the spatter expansion.
     *
     * @param blockPos Block hit.
     * @param expansionAmount The initial amount the blood spatter will try to expand.
     * @param isMax
     * @return
     */
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

    private boolean nonExpandableBlocks(String blockName) {
        return blockName.contains("air") || blockName.contains("water") || blockName.contains("lava");
    }

    private void setDrip() {
        if (this.drip < MAX_DRIP_LENGTH) {
            if (this.life > 50 && this.entityDirection.equals(Direction.DOWN)) {
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
            blockExpandingTo = this.level().getBlockState(BlockPos.containing(this.hitBlockPos.getX(), this.hitPosition.y + (this.yMin * 0.1) - 0.025F, this.hitPosition.z));
        }
        else if (this.entityDirection.equals(Direction.NORTH) || this.entityDirection.equals(Direction.SOUTH)) {
            blockExpandingTo = this.level().getBlockState(BlockPos.containing(this.hitPosition.x, this.hitPosition.y + (this.yMin * 0.1) - 0.025F, this.hitBlockPos.getZ()));
        }

        if (!this.entityDirection.equals(Direction.UP) && !this.entityDirection.equals(Direction.DOWN)) {
            if (this.yMin > this.yMinLimit) {
                this.yMin -= (this.yMin - SPATTER_SPEED < this.yMinLimit) ? this.yMin - this.yMinLimit : SPATTER_SPEED;
            }
            else if (!nonExpandableBlocks(blockExpandingTo.getBlock().toString())) {
                this.yMin -= (this.yMin - 0.025F < this.yMinLimit - WALL_SLIDE_DOWN_AMOUNT) ? this.yMin - (this.yMinLimit - WALL_SLIDE_DOWN_AMOUNT) : 0.025F;
            }
        }
        else if (this.yMin > this.yMinLimit) {
            this.yMin -= (this.yMin - SPATTER_SPEED < this.yMinLimit) ? this.yMin - this.yMinLimit : SPATTER_SPEED;
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

    public void setInGround(boolean isInGround) {
        this.inGround = isInGround;
    }
}