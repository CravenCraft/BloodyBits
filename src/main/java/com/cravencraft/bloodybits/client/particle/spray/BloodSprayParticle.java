package com.cravencraft.bloodybits.client.particle.spray;

import com.cravencraft.bloodybits.BloodyBitsMod;
import com.cravencraft.bloodybits.client.particle.spatter.BloodSpatterParticle;
import com.cravencraft.bloodybits.client.particle.spatter.BloodSpatterParticleOptions;
import com.cravencraft.bloodybits.config.ClientConfig;
import com.cravencraft.bloodybits.events.BloodyBitsEvents;
import com.cravencraft.bloodybits.utils.BloodyBitsUtils;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Random;

public class BloodSprayParticle extends TextureSheetParticle {
    private static final float  FALLBACK_BLOOD_SPRAY_FRICTION = 0.5F;
    private static final double FALLBACK_SOUND_VOLUME = 0.75;
    private static final float FALLBACK_MAX_VELOCITY = 2.0F;

    private final float soundVolume;
    private final float maxVelocity;
    private final float maxLength;
    private final float maxThickness;
    private final String color;

    private boolean underwater;
    private float spatterSize;
    private float scaleTransition;
    private float currentThickness;
    private float currentLength;
    private Vec3 collisionVector;
    private SoundEvent soundEvent;

    public BloodSprayParticle(
            ClientLevel level,
            double xCoord,
            double yCoord,
            double zCoord,
            SpriteSet spriteSet,
            String color,
            float scale,
            double xd,
            double yd,
            double zd
    ) {
        super(level, xCoord, yCoord, zCoord, xd, yd, zd);

        this.color = color;
        this.xd = xd;
        this.yd = yd;
        this.zd = zd;

        this.collisionVector = new Vec3(xd, yd, zd);
        this.quadSize = 0.1f;
        this.spatterSize = scale;
//        this.quadSize *= 0.25f + (float) Math.random();
//        this.scale(scale * 2.5f);
        this.lifetime = 200;
        this.friction = (BloodyBitsEvents.isConfigLoaded) ? (float) ClientConfig.getBloodSprayFriction() : FALLBACK_BLOOD_SPRAY_FRICTION;
        this.maxVelocity = FALLBACK_MAX_VELOCITY;
        this.gravity = 1.0F;
        this.maxLength = 5.0F;
        this.maxThickness = 1.0F;
        this.quadSize *= 0.25f + (float) Math.random();
        this.scale(scale * 2.5f);
        this.lifetime = 40;
        this.gravity = 1f;
        this.pickSprite(spriteSet);
        this.rCol = BloodyBitsUtils.normalizeColorValue(HexFormat.fromHexDigits(color, 1, 3));
        this.gCol = BloodyBitsUtils.normalizeColorValue(HexFormat.fromHexDigits(color, 3, 5));
        this.bCol = BloodyBitsUtils.normalizeColorValue(HexFormat.fromHexDigits(color.substring(5)));
        this.soundVolume = (float) ((BloodyBitsEvents.isConfigLoaded) ? ClientConfig.bloodSpatterSoundVolume() : FALLBACK_SOUND_VOLUME);

        this.scaleTransition = 1f + (float) Math.random();
        if (!level.getFluidState(BlockPos.containing(x, y, z)).isEmpty()) {
            this.underwater = true;
            this.xd *= 0.5f;
            this.yd *= 0.5f;
            this.zd *= 0.5f;
            this.gravity *= .25f;
        }
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        var targetVelocity = new Vec3(this.xd, this.yd, this.zd).length();
        this.currentLength = (float) (Math.min(targetVelocity, this.maxVelocity) / this.maxVelocity) * this.maxLength;
        this.currentThickness = (float) (this.maxThickness - ((Math.min(targetVelocity, this.maxVelocity) / this.maxVelocity) * this.maxThickness));

        if (this.age++ >= this.lifetime) {
            this.remove();
        }
        else {
            this.yd -= 0.04D * (double)this.gravity;
            this.move(this.xd, this.yd, this.zd);

            this.xd *= this.friction;

            if (this.yd > 0) {
                this.yd *= this.friction;
            }
            this.zd *= this.friction;
        }

        if (this.underwater) {
            this.gravity *= .99f;
        }

        this.checkBloodSprayLevelCollision();
    }

    /**
     * Checks if the {@link #collisionVector} will collide with the level based on its current velocity. If so,
     * the direction of the collision will be determined based on the x, y, and z values of the current collision
     * vector. If there is a collision, then the collision direction is used in
     * {@link #createSpatterAtCollisionPoint(int)} to create a new {@link BloodSpatterParticle}. If there is no
     * collision, then the {@link #collisionVector} is updated to the current one determined in this method.
     */
    private void checkBloodSprayLevelCollision() {
        var previousColVec = this.collisionVector;
        var currentColVec = Entity.collideBoundingBox(null,
                new Vec3(this.xd, this.yd, this.zd),
                this.getBoundingBox(),
                this.level,
                List.of());

        if (previousColVec.x > 0.001 && currentColVec.x == 0.0) {
            this.createSpatterAtCollisionPoint(Direction.EAST.get3DDataValue());
        }
        else if (previousColVec.x < -0.001 && currentColVec.x == 0.0) {
            this.createSpatterAtCollisionPoint(Direction.WEST.get3DDataValue());
        }
        else if (previousColVec.y > 0.001 && currentColVec.y == 0.0) {
            this.createSpatterAtCollisionPoint(Direction.UP.get3DDataValue());
        }
        else if (previousColVec.y < -0.001 && currentColVec.y == 0.0) {
            this.createSpatterAtCollisionPoint(Direction.DOWN.get3DDataValue());
        }
        else if (previousColVec.z > 0.001 && currentColVec.z == 0.0) {
            this.createSpatterAtCollisionPoint(Direction.SOUTH.get3DDataValue());
        }
        else if (previousColVec.z < -0.001 && currentColVec.z == 0.0) {
            this.createSpatterAtCollisionPoint(Direction.NORTH.get3DDataValue());
        }
        else {
            this.collisionVector = currentColVec;
        }
    }

    /**
     * Adds a {@link BloodSpatterParticle} at the point of collision with the given level. Will use
     * {@link Direction#from3DDataValue(int)} to determine the direction that the spatter should face on the surface.
     * Finally, this {@link BloodSpatterParticle} will be removed upon the creation of the blood spatter particle.
     *
     * @param collisionDirection The direction that the spatter should face when placed in the level.
     */
    private void createSpatterAtCollisionPoint(int collisionDirection) {
        this.level.addParticle(
                new BloodSpatterParticleOptions(this.color, collisionDirection, this.spatterSize),
                true, this.x, this.y, this.z,
                0.0D, 0.0D, 0.0D);

        this.soundEvent = BloodyBitsUtils.getRandomSound(new Random().nextInt(3));
        var pitch = BloodyBitsUtils.getRandomPitch();
        this.level.playLocalSound(this.xo, this.yo, this.zo, this.soundEvent, SoundSource.AMBIENT, this.soundVolume, pitch, true);

        this.remove();
    }

    @Override
    public float getQuadSize(float partialTicks) {
        float scaleMult = (this.age + partialTicks) > scaleTransition ? 1f : (this.age + partialTicks) / (scaleTransition * 2f) + .5f;
        return super.getQuadSize(partialTicks) * scaleMult;
    }

    @Override
    public void render(@NotNull VertexConsumer buffer, @NotNull Camera renderInfo, float partialTicks) {
        if (this.underwater) {
            this.alpha -= 0.005f;
            scale(1.005f);
            if (this.alpha < .1) {
                remove();
                return;
            }
        }
        this.renderRotatedQuad(buffer, renderInfo, partialTicks);
    }

    private void renderRotatedQuad(VertexConsumer buffer, Camera camera, float partialTicks) {
        var cameraPosition = camera.getPosition();
        float xLerp = (float) (Mth.lerp(partialTicks, this.xo, this.x) - cameraPosition.x());
        float yLerp = (float) (Mth.lerp(partialTicks, this.yo, this.y) - cameraPosition.y());
        float zLerp = (float) (Mth.lerp(partialTicks, this.zo, this.z) - cameraPosition.z());

        var targetDirection = new Vec3(this.xd, this.yd, this.zd);
        var quaternionf = getRotation(new Quaternionf(), targetDirection);
        var bloodSprayVectors = new ArrayList<Vector3f[]>();

        // East Face
        bloodSprayVectors.add(new Vector3f[] {
                new Vector3f(-this.currentThickness, this.currentThickness, -this.currentLength),
                new Vector3f(-this.currentThickness, -this.currentThickness, -this.currentLength),
                new Vector3f(-this.currentThickness, -this.currentThickness, this.currentLength),
                new Vector3f(-this.currentThickness, this.currentThickness, this.currentLength)
        });

        // West Face
        bloodSprayVectors.add(new Vector3f[] {
                new Vector3f(this.currentThickness, this.currentThickness, this.currentLength),
                new Vector3f(this.currentThickness, -this.currentThickness, this.currentLength),
                new Vector3f(this.currentThickness, -this.currentThickness, -this.currentLength),
                new Vector3f(this.currentThickness, this.currentThickness, -this.currentLength)
        });


        // Up Face
        bloodSprayVectors.add(new Vector3f[] {
                new Vector3f(this.currentThickness, this.currentThickness, -this.currentLength),
                new Vector3f(-this.currentThickness, this.currentThickness, -this.currentLength),
                new Vector3f(-this.currentThickness, this.currentThickness, this.currentLength),
                new Vector3f(this.currentThickness, this.currentThickness, this.currentLength)
        });

        // Down Face
        bloodSprayVectors.add(new Vector3f[] {
                new Vector3f(this.currentThickness, -this.currentThickness, this.currentLength),
                new Vector3f(-this.currentThickness, -this.currentThickness, this.currentLength),
                new Vector3f(-this.currentThickness, -this.currentThickness, -this.currentLength),
                new Vector3f(this.currentThickness, -this.currentThickness, -this.currentLength)
        });

        // South Face
        bloodSprayVectors.add(new Vector3f[] {
                new Vector3f(-this.currentThickness, this.currentThickness, this.currentLength),
                new Vector3f(-this.currentThickness, -this.currentThickness, this.currentLength),
                new Vector3f(this.currentThickness, -this.currentThickness, this.currentLength),
                new Vector3f(this.currentThickness, this.currentThickness, this.currentLength)
        });

        // North Face
        bloodSprayVectors.add(new Vector3f[] {
                new Vector3f(this.currentThickness, this.currentThickness, -this.currentLength),
                new Vector3f(this.currentThickness, -this.currentThickness, -this.currentLength),
                new Vector3f(-this.currentThickness, -this.currentThickness, -this.currentLength),
                new Vector3f(-this.currentThickness, this.currentThickness, -this.currentLength)
        });

        int packedLight = this.getLightColor(partialTicks);
        float u0 = this.getU0();
        float u1 = this.getU1();
        float v0 = this.getV0();
        float v1 = this.getV1();

        float quadSizePartialTicks = this.getQuadSize(partialTicks);
        for (var bloodSprayVector : bloodSprayVectors) {
            for (Vector3f vector3f : bloodSprayVector) {
                vector3f.rotate(quaternionf);
                vector3f.mul(quadSizePartialTicks);
                vector3f.add(xLerp, yLerp, zLerp);
            }

            renderVertex2(buffer, bloodSprayVector, u0, u1, v0, v1, packedLight);
        }
    }

    private void renderVertex(
            VertexConsumer buffer,
            Quaternionf quaternion,
            float x,
            float y,
            float z,
            float xOffset,
            float yOffset,
            float quadSize,
            float u,
            float v,
            int packedLight
    ) {


        Vector3f vector3f = new Vector3f(xOffset, yOffset, 0.0F).rotate(quaternion).mul(quadSize).add(x, y, z);
//        BloodyBitsMod.LOGGER.info("renderVertex: {}", vector3f);
        buffer.addVertex(vector3f.x(), vector3f.y(), vector3f.z())
                .setUv(u, v)
                .setColor(this.rCol, this.gCol, this.bCol, this.alpha)
                .setLight(packedLight);
    }

    private Quaternionf getRotation(Quaternionf source, Vec3 dir) {
        float yaw = (float) Math.atan2(dir.x, dir.z);
        float pitch = (float) Math.atan2(-dir.y, Math.sqrt(dir.x * dir.x + dir.z * dir.z));
        return new Quaternionf(source)
                .rotationY(yaw)
                .rotateX(pitch);
    }

    private void renderVertex2(
            VertexConsumer buffer,
            Vector3f[] face,
            float u0,
            float u1,
            float v0,
            float v1,
            int packedLight
    ) {
        buffer.addVertex(face[0].x(), face[0].y(), face[0].z()).setUv(u1, v1).setColor(this.rCol, this.gCol, this.bCol, this.alpha).setLight(packedLight);
        buffer.addVertex(face[1].x(), face[1].y(), face[1].z()).setUv(u1, v0).setColor(this.rCol, this.gCol, this.bCol, this.alpha).setLight(packedLight);
        buffer.addVertex(face[2].x(), face[2].y(), face[2].z()).setUv(u0, v0).setColor(this.rCol, this.gCol, this.bCol, this.alpha).setLight(packedLight);
        buffer.addVertex(face[3].x(), face[3].y(), face[3].z()).setUv(u0, v1).setColor(this.rCol, this.gCol, this.bCol, this.alpha).setLight(packedLight);
    }

    @Override
    public @NotNull ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @OnlyIn(Dist.CLIENT)
    public record Provider(SpriteSet sprites) implements ParticleProvider<BloodSprayParticleOptions> {

        @Override
        public Particle createParticle(@NotNull BloodSprayParticleOptions options, @NotNull ClientLevel level,
                                       double x, double y, double z,
                                       double dx, double dy, double dz) {
            return new BloodSprayParticle(
                    level, x, y, z,
                    this.sprites, options.color(),
                    options.scale(), options.direction().x, options.direction().y, options.direction().z);
        }
    }
}
