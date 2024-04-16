package com.cravencraft.bloodybits.entity.custom;

import com.cravencraft.bloodybits.BloodyBitsMod;
import com.cravencraft.bloodybits.config.ClientConfig;
import com.cravencraft.bloodybits.config.CommonConfig;
import com.cravencraft.bloodybits.network.BloodyBitsPacketHandler;
import com.cravencraft.bloodybits.network.messages.EntityMessage;
import com.cravencraft.bloodybits.registries.EntityRegistry;
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
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.joml.Random;

import javax.annotation.Nullable;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;

public class BloodChunkEntity extends AbstractArrow {

    private static final int BLOOD_SPATTER_TEXTURES = 4;
    public static final int MAX_DRIP_LENGTH = 50;

//    private boolean isInitialChunk;

    public int currentLifeTime;
    public int randomTextureNumber;
    public float xMin;
    public float xMax; // TODO: I bet this needs to be at least 0.001 to not clip into blocks.
    public float yMin;
    public float yMax;
    public float zMin;
    public float zMax;
    public float drip;

    public String ownerName;

    public boolean shouldDrip;
    public boolean isSolid;
    public Direction entityDirection;
    public BlockPos hitBlockPos;
    public Vec3 hitPosition;
    public int red = 255;
    public int green = 1;
    public int blue = 1;
    private AABB ownerBoundingBox;

    public BloodChunkEntity(EntityType<BloodChunkEntity> entityType, Level level) {
        super(entityType, level);
        this.randomTextureNumber = new Random().nextInt(BLOOD_SPATTER_TEXTURES);
    }

    public BloodChunkEntity(EntityType<BloodChunkEntity> entityType, double x, double y, double z, Level level) {
        super(entityType, x, y, z, level);
    }

    public BloodChunkEntity(EntityType<BloodChunkEntity> entityType, LivingEntity shooter, Level level, float damageAmount) {
        super(entityType, shooter, level);

    }
//    public BloodChunkEntity(EntityType<BloodChunkEntity> entityType, LivingEntity shooter, Level level, boolean isInitialChunk) {
//        super(entityType, shooter, level);
//        this.isInitialChunk = isInitialChunk;
//        this.setNoGravity(true);
//    }

//    public boolean isInitialChunk() {
//        return isInitialChunk;
//    }
//
//    public void setInitialChunk(boolean initialChunk) {
//        isInitialChunk = initialChunk;
//    }

    @Override
    public void setOwner(@Nullable Entity ownerEntity) {
        super.setOwner(ownerEntity);
        BloodyBitsMod.LOGGER.info("IS BLOOD CHUNK ENTITY CLIENT SIDE: {}", this.level().isClientSide());
        if (ownerEntity != null) {
            this.ownerBoundingBox = ownerEntity.getBoundingBox();
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
        BloodyBitsMod.LOGGER.info("RED GREEN BLUE : {} - {} - {}", red, green, blue);

    }

    @Override
    protected void tickDespawn() {

        if (this.level().isClientSide()) {
            ++this.currentLifeTime;
        }

//        if (this.isInitialChunk && this.currentLifeTime >= 100) {
//            this.discard();
//        }
        else if (this.currentLifeTime >= CommonConfig.despawnTime()) {
            this.discard();
            BloodyBitsUtils.BLOOD_CHUNK_ENTITIES.remove(this);
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

    @Override
    public void tick() {
        // Removes any blood spray entity that has a null owner. This usually happens whenever the game closes & opens back up.
        if (this.ownerName == null) {
            this.discard();
        }
        super.tick();

        if (this.inGround) {

            if (!this.shouldFall()) {
                this.tickDespawn();
            }
        }
        else {
            this.currentLifeTime = 0;
            float length = 2;
            this.xMin = -length;
            this.xMax = length;
            this.yMin = -length;
            this.yMax = length;
            this.zMin = -length;
            this.zMax = length;
        }
    }

    /**
     * TODO: When hitting the TOP (bottom, actually) of a block and expanding WEST still seeing
     *       an issue with the texture expanding into air.
     *
     * Is called once on block hit. Can get the direction the entity has hit the block on, which will dictate how
     * it is expanded.
     *
     * @param result
     */
    @Override
    protected void onHitBlock(BlockHitResult result) {
//        BloodyBitsMod.LOGGER.info("CHUNK HIT GROUND {}, {}, {}, {}", xMin, xMax, yMin, yMax);
            // Modified sound to be a deeper pitch of Slime Block sounds.
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

//        if (BloodyBitsUtils.BLOOD_SPRAY_ENTITIES.size() >= CommonConfig.maxSpatters()) {
//            BloodyBitsUtils.BLOOD_SPRAY_ENTITIES.get(0).discard();
//            BloodyBitsUtils.BLOOD_SPRAY_ENTITIES.remove(0);
//        }
//        //TODO: Test this a lot. getOwner might fail in a few scenarios.
//        BloodSprayEntity bloodSprayEntity = new BloodSprayEntity(EntityRegistry.BLOOD_SPRAY.get(), this.level());
//        BloodyBitsUtils.BLOOD_SPRAY_ENTITIES.add(bloodSprayEntity);
//
//        bloodSprayEntity.setDeltaMovement(this.getDeltaMovement());
//        this.level().addFreshEntity(bloodSprayEntity);
//        BloodyBitsPacketHandler.INSTANCE.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> bloodSprayEntity),
//                new EntityMessage(bloodSprayEntity.getId(), this.getOwner().getId()));
    }

    @Override
    protected void onHitEntity(EntityHitResult pResult) {

    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}