package com.cravencraft.bloodybits.entity.custom;

import com.cravencraft.bloodybits.config.ClientConfig;
import com.cravencraft.bloodybits.config.CommonConfig;
import com.cravencraft.bloodybits.utils.BloodyBitsUtils;
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
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import org.joml.Random;

import javax.annotation.Nullable;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;

/**
 * This class will create a blood chunk whenever an entity dies (if specified in the Common Config).
 * The chunks will spew from the area that the entity died and land in random directions all around it.
 * I'm trying to emulate a slight gib effect with this class to make entity deaths in Minecraft more dramatic.
 */
public class BloodChunkEntity extends AbstractArrow {

    private static final int BLOOD_CHUNK_TEXTURES = 2;

    public int currentLifeTime;
    public int randomTextureNumber;
    public float initialMinX, xMin;
    public float initialMaxX, xMax;
    public float initialMinY, yMin;
    public float initialMaxY, yMax;
    public float initialMinZ, zMin;
    public float initialMaxZ, zMax;

    public String ownerName;
    public boolean isSolid;
    public Direction entityDirection;
    public int red = 255;
    public int green = 1;
    public int blue = 1;

    /**
     * When the Blood Chunk Entity is set so to will its initial and current size values.
     *
     * @param entityType
     * @param level
     */
    public BloodChunkEntity(EntityType<BloodChunkEntity> entityType, Level level) {
        super(entityType, level);
        this.randomTextureNumber = new Random().nextInt(BLOOD_CHUNK_TEXTURES);
        if (this.level().isClientSide()) {
            this.initialMinX = this.xMin = -new Random().nextInt(3) - 1;
            this.initialMaxX = this.xMax = new Random().nextInt(3) + 1;
            this.initialMinY = this.yMin = -new Random().nextInt(3) - 1;
            this.initialMaxY = this.yMax = new Random().nextInt(3) + 1;
            this.initialMinZ = this.zMin = -new Random().nextInt(3) - 1;
            this.initialMaxZ = this.zMax = new Random().nextInt(3) + 1;
        }
    }

    public BloodChunkEntity(EntityType<BloodChunkEntity> entityType, LivingEntity shooter, Level level, float damageAmount) {
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
     * This method only ticks client side since all the logic that matters is on this side. The blood chunk
     * will also slowly decrease in size based on its initial size and the current percentage of its lifetime.
     */
    @Override
    protected void tickDespawn() {
        if (this.level().isClientSide()) {
            ++this.currentLifeTime;

            BloodyBitsUtils.shrinkBloodChunk(this);
            if (this.currentLifeTime >= CommonConfig.despawnTime()) {
                this.discard();
                BloodyBitsUtils.BLOOD_CHUNK_ENTITIES.remove(this);
            }
        }
    }

    /**
     * Do nothing when the player interacts with the entity.
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

    /**
     * Ensures that the entity will not start counting down ticks to discard until it has hit a surface.
     * Will also discard if the entity has no owner, which will discard the entity upon restarting of the game.
     */
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
        }
    }

    /**
     * Is called once on block hit. Can get the direction the entity has hit the block on, which will dictate how
     * it is expanded.
     *
     * @param result
     */
    @Override
    protected void onHitBlock(BlockHitResult result) {
        if (this.isSolid) {
            // Modified sound to be a deeper pitch of Slime Block sounds.
            this.setSoundEvent(SoundEvents.BONE_BLOCK_BREAK);
//            this.playSound(this.getHitGroundSoundEvent(), 0.75F, 1.8F / (this.random.nextFloat() * 0.2F + 0.9F));

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
            // All of this is boilerplate from AbstractArrow except the setSoundEvent now playing a slime sound.
            BlockState blockstate = this.level().getBlockState(result.getBlockPos());
            blockstate.onProjectileHit(this.level(), blockstate, result, this);
            Vec3 vec3 = result.getLocation().subtract(this.getX(), this.getY(), this.getZ());
            this.setDeltaMovement(vec3);
            Vec3 vec31 = vec3.normalize().scale((double)0.05F);
            this.setPosRaw(this.getX() - vec31.x, this.getY() - vec31.y, this.getZ() - vec31.z);

            // Modified sound to be a deeper pitch of Slime Block sounds.
            this.setSoundEvent((Math.random() > 0.5) ? SoundEvents.SLIME_BLOCK_HIT : SoundEvents.SLIME_BLOCK_STEP);
        }


        this.playSound(this.getHitGroundSoundEvent(), 0.75F, 1.8F / (this.random.nextFloat() * 0.2F + 0.9F));

        this.inGround = true;
        this.setCritArrow(false);
        this.setPierceLevel((byte)0);
        this.setShotFromCrossbow(false);
        this.resetPiercedEntities();
    }

    /**
     * Returns nothing so that this never interacts with other entities.
     *
     * @param pResult
     */
    @Override
    protected void onHitEntity(EntityHitResult pResult) {}

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}