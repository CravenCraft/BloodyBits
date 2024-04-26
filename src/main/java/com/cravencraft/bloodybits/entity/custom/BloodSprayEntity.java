package com.cravencraft.bloodybits.entity.custom;

import com.cravencraft.bloodybits.BloodyBitsMod;
import com.cravencraft.bloodybits.config.ClientConfig;
import com.cravencraft.bloodybits.config.CommonConfig;
import com.cravencraft.bloodybits.utils.BloodyBitsUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
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

import javax.annotation.Nullable;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;

public class BloodSprayEntity extends AbstractArrow {

    private static final int BLOOD_SPATTER_TEXTURES = 7;
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
    public Direction entityDirection;
    public BlockPos hitBlockPos;
    public Vec3 hitPosition;
    public int red = 200;
    public int green = 1;
    public int blue = 1;

    public BloodSprayEntity(EntityType<BloodSprayEntity> entityType, Level level) {
        super(entityType, level);
        this.randomTextureNumber = new Random().nextInt(BLOOD_SPATTER_TEXTURES);
    }

    public BloodSprayEntity(EntityType<BloodSprayEntity> entityType, LivingEntity shooter, Level level) {
        super(entityType, shooter, level);

    }

    /**
     * If the owner of the entity is not null, then the string field 'ownerName' will be set to the
     * entity's encoded ID (e.x., minecraft:villager), and if the entity is a player, then it will
     * simply be set to 'player'.
     *
     * This value is used by the configs in order to determine which mobs have custom blood colors,
     * and if a mob is found in the list, then its color will be set based on the hex format specified
     * in the config.
     *
     * @param ownerEntity
     */
    @Override
    public void setOwner(@Nullable Entity ownerEntity) {
        super.setOwner(ownerEntity);

        if (ownerEntity != null) {
            this.ownerName = (ownerEntity.toString().contains("Player")) ? "player" : ownerEntity.getEncodeId();
            this.isSolid = CommonConfig.solidEntities().contains(this.ownerName);
            if (this.level().isClientSide()) {
                for (Map.Entry<String, List<String>> mapElement : ClientConfig.mobBloodColors().entrySet()) {
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

    /**
     * Will tick down the entity & discard it whenever it reaches the time specified in the Common Config.
     * This method only ticks client side since all the logic that matters is on this side. The blood spray
     * will also slowly decrease in size based on its initial size and the current percentage of its lifetime.
     */
    @Override
    protected void tickDespawn() {
        ++this.currentLifeTime;

        if (this.currentLifeTime >= CommonConfig.despawnTime()) {
            this.discard();
            BloodyBitsUtils.BLOOD_SPRAY_ENTITIES.remove(this);
        }
    }

    /**
     * Do nothing when the player interacts with the entity.
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

    /**
     * Sets the water inertia to be very low. It being negative also helps the AbstractArrowMixin to remove
     * the bubble particles from the blood spray entity when it's in the water.
     */
    @Override
    protected float getWaterInertia() {
        return -0.01F;
    }

    /**
     * Ensures that the entity will not start counting down ticks to discard until it has hit a surface.
     * Will also discard if the entity has no owner, which will discard the entity upon restarting of the game.
     *
     * Determines the blood spray shape whenever it is in the air, the water, and the ground. Also, ensures that
     * if the entity is a solid that it does not change shape.
     */
    @Override
    public void tick() {
        // Removes any blood spray entity that has a null owner. This usually happens whenever the game closes & opens back up.
        if (this.ownerName == null) {
            this.discard();
        }
        super.tick();
        if (this.inGround) {
            if (!this.isSolid && this.xMin < this.xMax) {
                this.xMin = this.xMax;
            }

            // Done to set the lifetime client side as well. It's done in the super class for server side.
            if (!this.shouldFall() && this.level().isClientSide()) {
                this.tickDespawn();
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

                this.currentLifeTime += (CommonConfig.despawnTime() / 50);
            }
        }
        else if (this.isSolid) {
            this.currentLifeTime = 0;
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
            this.currentLifeTime = 0;
            double velocity = this.getDeltaMovement().length();
            float length = (float) (velocity * 10);
            this.xMin = -(length);

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
     * Is called once on block hit. Can get the direction the entity has hit the block on, which will dictate how
     * it is expanded. If the entity is a solid, then will make the entity bounce off a wall until it lands on a floor surface.
     */
    @Override
    protected void onHitBlock(BlockHitResult result) {
        if (this.isSolid) {
            // Modified sound to be a deeper pitch of Slime Block sounds.
            this.setSoundEvent((Math.random() > 0.5) ? SoundEvents.BONE_BLOCK_FALL : SoundEvents.BONE_BLOCK_HIT);
            this.playSound(this.getHitGroundSoundEvent(), 0.75F, 1.8F / (this.random.nextFloat() * 0.2F + 0.9F));

            if (result.getDirection().equals(Direction.UP)) {
                this.inGround = true;

                Vec3 vec3 = result.getLocation().subtract(this.getX(), this.getY(), this.getZ());

                this.setDeltaMovement(vec3);

                Vec3 vec31 = vec3.normalize().scale(0.05F);
                this.setPosRaw(this.getX() - vec31.x, this.getY() - vec31.y, this.getZ() - vec31.z);
            }
            else {

                Vec3 vec3 = result.getLocation().subtract(this.getX(), this.getY(), this.getZ());

                this.setDeltaMovement(0, -0.25, 0);
                Vec3 vec31 = vec3.normalize().scale((double)0.1F);
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
        }

        this.setCritArrow(false);
        this.setPierceLevel((byte)0);
        this.setShotFromCrossbow(false);
        this.resetPiercedEntities();
    }

    /**
     * Gets the maximum bounds that a blood spatter can expand to when hitting a block. This is determined by the block
     * hit and the blocks in the direction of the spatter expansion.
     *
     * @param initialExpansionAmount The initial amount the blood spatter will try to expand.
     * @param isYAxis If the hit position is on the y-axis.
     * @param isMax Whether the point is a max or min val (e.x., xMax).
     * @return
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
}