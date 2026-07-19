package com.cravencraft.bloodybits.particle;

import com.cravencraft.bloodybits.BloodyBitsMod;
import com.cravencraft.bloodybits.registries.ParticleRegistry;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.List;

public class BloodSprayParticle extends TextureSheetParticle {
    private final int color;
    float scaleTransition;
    private boolean mirrored;
    private boolean underwater;
    private Vec3 collisionVector;

    public BloodSprayParticle(
            ClientLevel level,
            double xCoord,
            double yCoord,
            double zCoord,
            SpriteSet spriteSet,
            int color,
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
        BloodyBitsMod.LOGGER.info("initial blood spray particle collision vector: {}", this.collisionVector);
        this.quadSize *= 1f + (float) Math.random();
        this.scale(scale * 2.5f);
        this.lifetime = 40;
        this.gravity = 0.25F;
        this.pickSprite(spriteSet);

        this.rCol = BloodSprayParticleOptions.red(color);
        this.gCol = BloodSprayParticleOptions.green(color);
        this.bCol = BloodSprayParticleOptions.blue(color);

        this.scaleTransition = 1f + (float) Math.random();
        this.mirrored = level.random.nextBoolean();
        if (!level.getFluidState(BlockPos.containing(x, y, z)).isEmpty()) {
            this.underwater = true;
            this.xd *= 0.5f;
            this.yd *= 0.5f;
            this.zd *= 0.5f;
            this.gravity *= .1f;
        }
    }

    @Override
    public void tick() {
        super.tick();

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
                new Vec3(previousColVec.x, previousColVec.y, previousColVec.z),
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
        BloodyBitsMod.LOGGER.info("collision direction: {}", Direction.from3DDataValue(collisionDirection));
        this.level.addParticle(
                new BloodSpatterParticleOptions(this.color, collisionDirection, this.getQuadSize(0.0F)),
                true, this.x, this.y, this.z,
                0.0D, 0.0D, 0.0D);

        this.remove();
    }

    @Override
    public float getQuadSize(float partialTicks) {
        float scaleMult = (this.age + partialTicks) > scaleTransition ? 1f : (this.age + partialTicks) / (scaleTransition * 2f) + .5f;
        return super.getQuadSize(partialTicks) * scaleMult;
    }

    @Override
    public void render(@NotNull VertexConsumer buffer, @NotNull Camera renderInfo, float partialTicks) {
//        if (this.decalDirection != DecalDirection.OMNIDIRECTIONAL) {
//            Vec3 left = new Vec3(renderInfo.getLeftVector());
//            Vec3 horizontalVelocity = new Vec3(xd, 0, zd);
//            double dot = left.dot(horizontalVelocity.normalize());
//            if (Math.abs(dot) > 0.1) {
//                boolean facingRight = dot < 0;
//                this.mirrored = facingRight ^ this.decalDirection == DecalDirection.RIGHT;
//            }
//        }
        if (underwater) {
            alpha -= 0.005f;
            scale(1.005f);
            if (alpha < .1) {
                remove();
                return;
            }
        }
        super.render(buffer, renderInfo, partialTicks);
    }

    @Override
    protected void renderRotatedQuad(@NotNull VertexConsumer buffer, @NotNull Quaternionf quaternion, float x, float y, float z, float partialTicks) {
        float f = this.getQuadSize(partialTicks);
        float f1 = this.getU0();
        float f2 = this.getU1();
        float f3 = this.getV0();
        float f4 = this.getV1();
        if (this.mirrored) {
            float tmp = f1;
            f1 = f2;
            f2 = tmp;
        }
        int i = this.getLightColor(partialTicks);
        this.renderVertex(buffer, quaternion, x, y, z, 1.0F, -1.0F, f, f2, f4, i);
        this.renderVertex(buffer, quaternion, x, y, z, 1.0F, 1.0F, f, f2, f3, i);
        this.renderVertex(buffer, quaternion, x, y, z, -1.0F, 1.0F, f, f1, f3, i);
        this.renderVertex(buffer, quaternion, x, y, z, -1.0F, -1.0F, f, f1, f4, i);
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
        buffer.addVertex(vector3f.x(), vector3f.y(), vector3f.z())
                .setUv(u, v)
                .setColor(this.rCol, this.gCol, this.bCol, this.alpha)
                .setLight(packedLight);
    }

    @Override
    public @NotNull ParticleRenderType getRenderType() {
        return this.underwater ? ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT : ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<SimpleParticleType>, BloodEmitterParticle.VariantFactory {
        private final SpriteSet sprites;

        public Provider(SpriteSet spriteSet) {
            this.sprites = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType particleType, ClientLevel level,
                                       double x, double y, double z,
                                       double dx, double dy, double dz) {
            return new BloodSprayParticle(level, x, y, z, this.sprites, ParticleRegistry.DEFAULT_BLOOD_COLOR, 1f, dx, dy, dz);
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
