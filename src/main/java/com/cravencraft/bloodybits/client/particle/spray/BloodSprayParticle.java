package com.cravencraft.bloodybits.client.particle.spray;

import com.cravencraft.bloodybits.BloodyBitsMod;
import com.cravencraft.bloodybits.client.particle.emitter.BloodEmitterParticle;
import com.cravencraft.bloodybits.client.particle.spatter.BloodSpatterParticle;
import com.cravencraft.bloodybits.client.particle.spatter.BloodSpatterParticleOptions;
import com.cravencraft.bloodybits.config.ClientConfig;
import com.cravencraft.bloodybits.registries.ParticleRegistry;
import com.cravencraft.bloodybits.utils.BloodyBitsUtils;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.HexFormat;
import java.util.List;
import java.util.Random;

public class BloodSprayParticle extends TextureSheetParticle {
    private static final double FALLBACK_SOUND_VOLUME = 0.75;
    private final String color;
    float scaleTransition;
    private boolean mirrored;
    private boolean underwater;
    private Vec3 collisionVector;
    private final float angularVelocity;
    private final double soundVolume;
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
        this.quadSize *= 0.25f + (float) Math.random();
        this.scale(scale * 2.5f);
        this.lifetime = 40;
        this.gravity = 1f;
        this.angularVelocity = 0.075f;
        this.pickSprite(spriteSet);
        this.rCol = BloodyBitsUtils.normalizeColorValue(HexFormat.fromHexDigits(color, 1, 3));
        this.gCol = BloodyBitsUtils.normalizeColorValue(HexFormat.fromHexDigits(color, 3, 5));
        this.bCol = BloodyBitsUtils.normalizeColorValue(HexFormat.fromHexDigits(color.substring(5)));
        this.soundVolume = (BloodyBitsMod.isClientConfigLoaded) ? ClientConfig.bloodSpatterSoundVolume() : FALLBACK_SOUND_VOLUME;

        this.scaleTransition = 1f + (float) Math.random();
        this.mirrored = level.random.nextBoolean();
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
        super.tick();
        this.oRoll = this.roll;
        this.roll += this.angularVelocity;
        this.quadSize += 0.01f;

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
                new BloodSpatterParticleOptions(this.color, collisionDirection, this.getQuadSize(0.0F)),
                true, this.x, this.y, this.z,
                0.0D, 0.0D, 0.0D);

        var soundEvent = BloodyBitsUtils.getRandomSound(new Random().nextInt(3));
        this.level.playLocalSound(this.x, this.y, this.z,
                soundEvent, SoundSource.AMBIENT, (float) this.soundVolume,
                BloodyBitsUtils.getRandomPitch(), true);

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

    protected void renderRotatedQuad(@NotNull VertexConsumer buffer, Camera camera, float partialTicks) {
        Vec3 vec3 = camera.getPosition();
        float f = (float) (Mth.lerp(partialTicks, this.xo, this.x) - vec3.x());
        float f1 = (float) (Mth.lerp(partialTicks, this.yo, this.y) - vec3.y());
        float f2 = (float) (Mth.lerp(partialTicks, this.zo, this.z) - vec3.z());
        Quaternionf quaternionf;
        if (this.roll == 0.0F) {
            quaternionf = camera.rotation();
        } else {
            quaternionf = new Quaternionf(camera.rotation());
            quaternionf.rotateZ(Mth.lerp(partialTicks, this.oRoll, this.roll));
        }
        Vector3f[] avector3f = new Vector3f[]{new Vector3f(-1.0F, -1.0F, 0.0F), new Vector3f(-1.0F, 1.0F, 0.0F), new Vector3f(1.0F, 1.0F, 0.0F), new Vector3f(1.0F, -1.0F, 0.0F)};
        float f3 = this.getQuadSize(partialTicks);

        for (int i = 0; i < 4; ++i) {
            Vector3f vector3f = avector3f[i];
            vector3f.rotate(quaternionf);
            vector3f.mul(f3);
            vector3f.add(f, f1, f2);
        }

        float f6 = this.getU0();
        float f7 = this.getU1();
        if (this.mirrored) {
            float tmp = f6;
            f6 = f7;
            f7 = tmp;
        }
        float f4 = this.getV0();
        float f5 = this.getV1();
        int j = this.getLightColor(partialTicks);
        buffer.vertex(avector3f[0].x(), avector3f[0].y(), avector3f[0].z()).uv(f7, f5).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(j).endVertex();
        buffer.vertex(avector3f[1].x(), avector3f[1].y(), avector3f[1].z()).uv(f7, f4).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(j).endVertex();
        buffer.vertex(avector3f[2].x(), avector3f[2].y(), avector3f[2].z()).uv(f6, f4).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(j).endVertex();
        buffer.vertex(avector3f[3].x(), avector3f[3].y(), avector3f[3].z()).uv(f6, f5).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(j).endVertex();
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
        buffer.vertex(vector3f.x(), vector3f.y(), vector3f.z())
                .uv(u, v)
                .color(this.rCol, this.gCol, this.bCol, this.alpha)
                .uv2(packedLight);
    }

    @Override
    public @NotNull ParticleRenderType getRenderType() {
        return this.underwater ? ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT : ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    @OnlyIn(Dist.CLIENT)
        public record Provider(SpriteSet sprites)
            implements ParticleProvider<SimpleParticleType>, BloodEmitterParticle.VariantFactory {

            @Override
            public Particle createParticle(SimpleParticleType particleType, ClientLevel level,
                                           double x, double y, double z,
                                           double dx, double dy, double dz) {
                return new BloodSprayParticle(level, x, y, z,
                        this.sprites, ParticleRegistry.DEFAULT_BLOOD_COLOR,
                        1f, dx, dy, dz);
            }

            @Override
            public Particle create(BloodSprayParticleOptions options, ClientLevel level,
                                   double x, double y, double z,
                                   double dx, double dy, double dz) {
                return new BloodSprayParticle(level, x, y, z, this.sprites, options.color(), options.scale(),
                        options.direction().x, options.direction().y, options.direction().z);
            }
        }
}
