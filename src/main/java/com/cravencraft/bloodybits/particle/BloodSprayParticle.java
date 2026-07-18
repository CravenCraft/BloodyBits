package com.cravencraft.bloodybits.particle;

import com.cravencraft.bloodybits.BloodyBitsMod;
import com.cravencraft.bloodybits.registries.ParticleRegistry;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Position;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import javax.sound.sampled.Clip;

import static net.minecraft.world.level.ClipContext.Block.VISUAL;
import static net.minecraft.world.level.ClipContext.Fluid.NONE;

public class BloodSprayParticle extends TextureSheetParticle {
    private final int color;
    float scaleTransition;
    private boolean mirrored;
    private boolean underwater;

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
        BloodyBitsMod.LOGGER.info("BloodSprayParticle speed args: {}, {}, {}", xd, yd, zd);
        BloodyBitsMod.LOGGER.info("BloodSprayParticle x, y, and z speeds before mod: {}, {}, {}", this.xd, this.yd, this.zd);
        this.color = color;
        this.xd = 1;
        this.yd = 0;
        this.zd = 0;
        BloodyBitsMod.LOGGER.info("BloodSprayParticle x, y, and z speeds AFTER mod: {}, {}, {}", this.xd, this.yd, this.zd);
        this.quadSize *= 1f + (float) Math.random();
        this.scale(scale * 2.5f);
        this.lifetime = 100 + (int) (Math.random() * 40);
        this.gravity = 1.5F;
        this.pickSprite(spriteSet);

        this.rCol = BloodParticleOptions.red(color);
        this.gCol = BloodParticleOptions.green(color);
        this.bCol = BloodParticleOptions.blue(color);

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
        this.isCollidingWithWall();

//        BloodyBitsMod.LOGGER.info("Is colliding with a wall: {}", this.isCollidingWithWall());
//        BloodyBitsMod.LOGGER.info("Is on ground: {}", this.onGround);

//        if (this.isCollidingWithWall())

        if (this.underwater) {
            this.gravity *= .99f;
        }

//        if (this.onGround) {
//            Vec3 groundLevel = level.clip(new ClipContext(this.getPos().add(0, 0.6, 0), this.getPos(), VISUAL, NONE, CollisionContext.empty())).getLocation();
//            this.level.addParticle(new BloodGroundParticleOptions(this.color, this.getQuadSize(0.0F)), true, groundLevel.x, groundLevel.y, groundLevel.z, 0.0D, 0.0D, 0.0D);
//
//            this.remove();
//        }
    }

    private boolean isCollidingWithWall() {
        AABB box = new AABB(this.x - 0.1, this.y - 0.1, this.z - 0.1,
                this.x + 1, this.y + 1, this.z + 1);
//        box.clip()
//                AABB.clip(box, )


        if (!this.level.noCollision(box)) {
            BloodyBitsMod.LOGGER.info("is colliding with wall");
//            var blockCollisions = this.level.getBlockCollisions(null, box);
            var clipContext = new ClipContext(box.getMinPosition(), box.getMaxPosition(), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, CollisionContext.empty());
            var blockHitResult = level.clip(clipContext);
            var blockHitPos = blockHitResult.getBlockPos();
            var blockHitDir = blockHitResult.getDirection();
            BloodyBitsMod.LOGGER.info("Block hit pos: {}, block hit direction: {}", blockHitPos, blockHitDir);

//            blockCollisions.forEach(voxelShape -> {
//                box.getMinPosition()



//                BloodyBitsMod.LOGGER.info("collision voxel shape: {}", voxelShape);
//                BlockPos.betweenClosedStream(voxelShape.bounds()).forEach((blockPos) -> {
//                    BloodyBitsMod.LOGGER.info("block pos: {}", blockPos);
//                    var blockHitResult = voxelShape.clip(voxelShape.bounds().getMinPosition(), voxelShape.bounds().getMaxPosition(), blockPos);
//                    if (blockHitResult != null) {
//                        var blockHitPos = blockHitResult.getBlockPos();
//                        var blockHitDirection = blockHitResult.getDirection();
//
//                        BloodyBitsMod.LOGGER.info("Block hit pos: {}, block hit direction: {}", blockHitPos, blockHitDirection);
//                    }
//                });




//            });

//            BlockPos.betweenClosedStream(box).forEach((blockPos) -> {
//                BloodyBitsMod.LOGGER.info("collision block pos: {}", blockPos);
//
//            });
//            var blockState = this.level.getBlockState(BlockPos.betweenClosedStream(box));
//            BloodyBitsMod.LOGGER.info("Particle at pos {} is colliding.", box);


        }

        return !this.level.noCollision(box);
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
        public Particle create(BloodParticleOptions options, ClientLevel level, double x, double y, double z, double dx, double dy, double dz) {
            return new BloodSprayParticle(level, x, y, z, this.sprites, options.color(), options.scale(), dx, dy, dz);
        }
    }
}
