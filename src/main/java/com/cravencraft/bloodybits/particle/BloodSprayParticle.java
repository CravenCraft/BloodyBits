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
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.List;

import static net.minecraft.world.level.ClipContext.Block.VISUAL;
import static net.minecraft.world.level.ClipContext.Fluid.NONE;

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

//        BloodyBitsMod.LOGGER.info("Stopped by collision? {}");

//        if (Math.abs(d1) >= 1.0E-5F && Math.abs(y) < 1.0E-5F) {
//            this.stoppedByCollision = true;
//        }

        if (this.underwater) {
            this.gravity *= .99f;
        }

//        Vec3 vec3 = Entity.collideBoundingBox(null, new Vec3(this.xd, this.yd, this.zd), this.getBoundingBox(), this.level, List.of());
//        BloodyBitsMod.LOGGER.info("blood spray particle collision vector: {}, {}, {}", vec3.x, vec3.y, vec3.z);
//        BloodyBitsMod.LOGGER.info("BloodSprayParticle in ground {} vector velocities: {}, {}, {}", this.onGround, this.xd, this.yd, this.zd);

        this.createBloodSpatter();

//        BloodyBitsMod.LOGGER.info("Is colliding with a wall: {}", this.isCollidingWithWall());
//        BloodyBitsMod.LOGGER.info("Is on ground: {}", this.onGround);

//        if (this.isCollidingWithWall())



//        if (this.onGround) {
//            Vec3 groundLevel = level.clip(new ClipContext(this.getPos().add(0, 0.6, 0), this.getPos(), VISUAL, NONE, CollisionContext.empty())).getLocation();
////            this.level.addParticle(new BloodSpatterParticleOptions(this.color, this.getQuadSize(0.0F)), true, groundLevel.x, groundLevel.y, groundLevel.z, 0.0D, 0.0D, 0.0D);
//
//            this.remove();
//        }
    }

    private void createBloodSpatter() {
        Direction collisionDirection = Direction.UP;
        var previousColVec = this.collisionVector;
        BloodyBitsMod.LOGGER.info("previous blood spray particle collision vector: {}, {}, {}", previousColVec.x, previousColVec.y, previousColVec.z);
        var currentColVec = Entity.collideBoundingBox(null, new Vec3(this.xd, this.yd, this.zd), this.getBoundingBox(), this.level, List.of());
        BloodyBitsMod.LOGGER.info("current blood spray particle collision vector: {}, {}, {}", currentColVec.x, currentColVec.y, currentColVec.z);

        if (previousColVec.x > 0.001 && currentColVec.x == 0.0) {
            collisionDirection = Direction.EAST;
            this.remove();
        }
        if (previousColVec.x < -0.001 && currentColVec.x == 0.0) {
            collisionDirection = Direction.WEST;
            this.remove();
        }

        if (previousColVec.y > 0.001 && currentColVec.y == 0.0) {
            collisionDirection = Direction.UP;
            this.remove();
        }
        if (previousColVec.y < -0.001 && currentColVec.y == 0.0) {
            collisionDirection = Direction.DOWN;
            this.remove();
        }

        if (previousColVec.z > 0.001 && currentColVec.z == 0.0) {
            collisionDirection = Direction.SOUTH;
            this.remove();
        }
        if (previousColVec.z < -0.001 && currentColVec.z == 0.0) {
            collisionDirection = Direction.NORTH;
            this.remove();
        }

        this.collisionVector = currentColVec;

        BloodyBitsMod.LOGGER.info("collision direction: {}", collisionDirection);
//
//
//        var box = this.getBoundingBox();
//
////        BloodyBitsMod.LOGGER.info("bounding box {}", box);
//        AABB collidingBox = new AABB(
//                box.minX - 0.1, box.minY - 0.1, box.minZ - 0.1,
//                box.maxX + 0.1, box.maxY + 0.1, box.maxZ + 0.1);
//
//
//        // TODO: stoppedByCollision in the Particle class is the key. Look into the move() method the utilizes it.
//
//        if (!this.level.noCollision(collidingBox)) {
////            BloodyBitsMod.LOGGER.info("bounding box {} of particle is colliding with something.", collidingBox);
//            var clipContext = new ClipContext(collidingBox.getMinPosition(), collidingBox.getMaxPosition(), VISUAL, NONE, CollisionContext.empty());
//            var blockHitResult = level.clip(clipContext);
//            var location = blockHitResult.getLocation();
//            var direction = blockHitResult.getDirection().get3DDataValue();
//            var blockHitPos = blockHitResult.getBlockPos(); // TODO: Might want to get the center vec3 and just add 0.6 to whatever direction needs it.
//            var blockHitDir = blockHitResult.getDirection();
////            this.gravity = 0;
////            this.xd = 0;
////            this.yd = 0;
////            this.zd = 0;
//            // TODO: Change the x, y, and z positions based on the direction.
////            this.level.addParticle(
////                    new BloodSpatterParticleOptions(this.color, direction, this.getQuadSize(0.0F)),
////                    true, location.x, location.y, location.z,
////                    0.0D, 0.0D, 0.0D);
//            BloodyBitsMod.LOGGER.info("Block hit pos 3d val: {}, block hit direction: {}", direction, blockHitDir);
//            BloodyBitsMod.LOGGER.info("hit location: {}", location);
//            this.remove();
//        }
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
