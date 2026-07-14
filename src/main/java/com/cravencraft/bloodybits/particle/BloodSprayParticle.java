package com.cravencraft.bloodybits.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.List;
import java.util.TreeSet;
import java.util.function.Consumer;

public class BloodSprayParticle extends TextureSheetParticle {
    private final SpriteSet spriteSet;
    private static final Vector3f ROTATION_VECTOR = Util.make(new Vector3f(0.5F, 0.5F, 0.5F), Vector3f::normalize);
    private static final Vector3f TRANSFORM_VECTOR = new Vector3f(-1.0F, -1.0F, 0.0F);
    private static final float DEGREES_90 = Mth.PI / 2f;
    private static final int FADEOUT_BUFFER = 20;
    private static final float INITIAL_ALPHA = 0.7f;
    private final int fadeoutTime;
    private final float yawRotation;
    private final float zFightOffset;

    // First four parameters are self-explanatory. The SpriteSet parameter is provided by the
    // ParticleProvider, see below. You may also add additional parameters as needed, e.g. xSpeed/ySpeed/zSpeed.
    public BloodSprayParticle(ClientLevel level, double x, double y, double z, SpriteSet spriteSet) {
        super(level, x, y, z);
//        this.xd = x;
//        this.yd = y;
//        this.zd = z;
        this.quadSize = 1.0F;
        this.friction = 0.5f;
        this.gravity = 0.95f;
        this.lifetime = 600;
        this.setSize(2.0f, 2.0f);
        this.scale(1f);
        this.spriteSet = spriteSet;
        this.fadeoutTime = 150;
//        this.quadSize = 1.5f * scale;
        this.yawRotation = this.random.nextInt(4) * DEGREES_90;
        this.zFightOffset = this.random.nextFloat();
//        this.rCol = 0;
//        this.gCol = 0;
//        this.bCol = 0;

        // We set the initial sprite here since ticking is not guaranteed to set the sprite
        // before the render method is called.
        this.setSpriteFromAge(spriteSet);
    }

//    @Override
//    public @NotNull Particle scale(float scale) {
//        super.scale(scale);
//
//        var size = this.quadSize / 10.0F;
//
////        if (FancyBlockParticles.CONFIG.terrain.isRestOnFloor() && this.destroyed)
////            this.y = this.startY - size;
//
//        this.yo = this.y;
//
//        this.setBoundingBox(new AABB(this.x - size, this.y - size, this.z - size, this.x + size, this.y + size, this.z + size));
//        this.setLocationFromBoundingbox();
//
//        return this;
//    }

@Override
public void render(VertexConsumer buffer, Camera camera, float partialTick) {
    int fadeThreshold = lifetime - fadeoutTime;
    float quadSize = this.getQuadSize(partialTick);
    float f = this.age + partialTick;
    if (f <= SPLAT_IN_TIME) {
        quadSize *= (f) / (SPLAT_IN_TIME * 2f) + .5f;
    }

    if (f > fadeThreshold) {
        quadSize *= (float) Mth.smoothstep(1.0 - Math.max(f - fadeThreshold - 60, 0) / fadeoutTime);
        this.alpha = 1.0F - Mth.clamp((f - fadeThreshold) / fadeoutTime, 1f - INITIAL_ALPHA, 1F);
    }

    this.renderRotatedParticle(buffer, camera, partialTick, quadSize, (quat) -> {
        quat.mul(Axis.YP.rotation(-(float) Math.PI));
        quat.mul(Axis.XP.rotation(DEGREES_90));
    });
}

    private static final float SPLAT_IN_TIME = 1.5f;
    private static final float MAX_PROJECTION_HEIGHT = 2.0f;
    private static final double HEIGHT_BRACKET_EPSILON = 1.0E-4D;

    private void renderRotatedParticle(VertexConsumer pConsumer, Camera camera, float partialTick, float quadSize, Consumer<Quaternionf> pQuaternion) {
        Vec3 cameraPos = camera.getPosition();
        float localX = (float) (Mth.lerp(partialTick, this.xo, this.x) - cameraPos.x());
        float localY = (float) (Mth.lerp(partialTick, this.yo, this.y) - cameraPos.y()) + 0.01f + .005f * (this.age / (float) this.lifetime);
        float localZ = (float) (Mth.lerp(partialTick, this.zo, this.z) - cameraPos.z());
        Quaternionf quaternion = (new Quaternionf()).setAngleAxis(0.0F, ROTATION_VECTOR.x(), ROTATION_VECTOR.y(), ROTATION_VECTOR.z());

        pQuaternion.accept(quaternion);
        quaternion.transform(TRANSFORM_VECTOR);

        Vector3f[] avector3f = new Vector3f[] {
                new Vector3f(-1.0F, -1.0F, 0.0F),
                new Vector3f(-1.0F, 1.0F, 0.0F),
                new Vector3f(1.0F, 1.0F, 0.0F),
                new Vector3f(1.0F, -1.0F, 0.0F)
        };


        for (int i = 0; i < 4; ++i) {
            Vector3f vector3f = avector3f[i];
            vector3f.rotate(quaternion);
            vector3f.mul(quadSize * 0.5f); // vector is a 2x2 plane, cut in half
            vector3f.add(localX, localY, localZ);
        }

        Vec3 worldExtentMin = new Vec3(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
        Vec3 worldExtentMax = new Vec3(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);
        for (Vector3f corner : avector3f) {
            Vec3 worldCorner = cameraPos.add(corner.x(), corner.y(), corner.z());
            worldExtentMin = new Vec3(
                    Math.min(worldExtentMin.x, worldCorner.x),
                    Math.min(worldExtentMin.y, worldCorner.y),
                    Math.min(worldExtentMin.z, worldCorner.z)
            );
            worldExtentMax = new Vec3(
                    Math.max(worldExtentMax.x, worldCorner.x),
                    Math.max(worldExtentMax.y, worldCorner.y),
                    Math.max(worldExtentMax.z, worldCorner.z)
            );
        }
        int light = this.getLightColor(partialTick);
        double centerX = Mth.lerp(partialTick, this.xo, this.x);
        double centerY = Mth.lerp(partialTick, this.yo, this.y);
        double centerZ = Mth.lerp(partialTick, this.zo, this.z);
        int startY = Mth.floor(centerY + 1.0D);
        int endY = Mth.floor(centerY - MAX_PROJECTION_HEIGHT);

        int minBlockX = BlockPos.containing(worldExtentMin).getX();
        int maxBlockX = BlockPos.containing(worldExtentMax).getX();
        int minBlockZ = BlockPos.containing(worldExtentMin).getZ();
        int maxBlockZ = BlockPos.containing(worldExtentMax).getZ();

        for (int blockX = minBlockX; blockX <= maxBlockX; blockX++) {
            for (int blockZ = minBlockZ; blockZ <= maxBlockZ; blockZ++) {
                this.renderColumnDecal(pConsumer, camera, blockX, blockZ, startY, endY, centerX, centerY, centerZ, worldExtentMin, worldExtentMax, quadSize, light);
            }
        }
    }

    private void renderColumnDecal(
            VertexConsumer buffer,
            Camera camera,
            int blockX,
            int blockZ,
            int startY,
            int endY,
            double centerX,
            double centerY,
            double centerZ,
            Vec3 worldExtentMin,
            Vec3 worldExtentMax,
            float quadSize,
            int light
    ) {
        BlockPos.MutableBlockPos columnPos = new BlockPos.MutableBlockPos();

        for (int y = startY; y >= endY; y--) {
            columnPos.set(blockX, y, blockZ);
            BlockPos surfacePos = columnPos.below();
            BlockState blockState = this.level.getBlockState(surfacePos);
            if (blockState.getRenderShape() == RenderShape.INVISIBLE || blockState.getCollisionShape(this.level, surfacePos).isEmpty()) {
                continue;
            }

            VoxelShape shape = blockState.getShape(this.level, surfacePos, CollisionContext.empty());
            if (shape.isEmpty()) {
                continue;
            }

            if (this.renderBlockDecal(
                    buffer,
                    camera,
                    surfacePos,
                    shape,
                    centerX,
                    centerY,
                    centerZ,
                    worldExtentMin,
                    worldExtentMax,
                    quadSize,
                    light
            )) {
                return;
            }
        }
    }

    private boolean renderBlockDecal(
            VertexConsumer buffer,
            Camera camera,
            BlockPos surfacePos,
            VoxelShape shape,
            double centerX,
            double centerY,
            double centerZ,
            Vec3 worldExtentMin,
            Vec3 worldExtentMax,
            float quadSize,
            int light
    ) {
        AABB bounds = shape.bounds();
        float minX = surfacePos.getX() + (float) bounds.minX;
        float maxX = surfacePos.getX() + (float) bounds.maxX;
        float minZ = surfacePos.getZ() + (float) bounds.minZ;
        float maxZ = surfacePos.getZ() + (float) bounds.maxZ;

        if (minX < worldExtentMin.x) {
            minX = (float) worldExtentMin.x;
        }
        if (maxX > worldExtentMax.x) {
            maxX = (float) worldExtentMax.x;
        }
        if (minZ < worldExtentMin.z) {
            minZ = (float) worldExtentMin.z;
        }
        if (maxZ > worldExtentMax.z) {
            maxZ = (float) worldExtentMax.z;
        }
        if (minX >= maxX || minZ >= maxZ) {
            return false;
        }

        List<AABB> boxes = shape.toAabbs();
        TreeSet<Double> heightBrackets = new TreeSet<>();
        for (AABB box : boxes) {
            heightBrackets.add(box.maxY);
        }

        boolean renderedAny = false;
        for (double localTopY : heightBrackets) {
            double worldTopY = surfacePos.getY() + localTopY;
            if (worldTopY > centerY + 0.25D) {
                continue;
            }

            for (AABB box : boxes) {
                if (Math.abs(box.maxY - localTopY) > HEIGHT_BRACKET_EPSILON) {
                    continue;
                }

                float planeMinX = Math.max(minX, surfacePos.getX() + (float) box.minX);
                float planeMaxX = Math.min(maxX, surfacePos.getX() + (float) box.maxX);
                float planeMinZ = Math.max(minZ, surfacePos.getZ() + (float) box.minZ);
                float planeMaxZ = Math.min(maxZ, surfacePos.getZ() + (float) box.maxZ);
                if (planeMinX >= planeMaxX || planeMinZ >= planeMaxZ) {
                    continue;
                }

                float drop = (float) (centerY - worldTopY);
                float alphaMultiplier = Mth.lerp(Mth.clamp(drop / MAX_PROJECTION_HEIGHT, 0.0F, 1.0F), 1.0F, 0.25F);
                float clipOffset = 0;//0.005625f; // now incorporated in anti-zfight offset
                this.renderFlatDecalPlane(
                        buffer,
                        camera,
                        planeMinX,
                        planeMaxX,
                        planeMinZ,
                        planeMaxZ,
                        (float) worldTopY + clipOffset,
                        centerX,
                        centerZ,
                        quadSize,
                        light,
                        alphaMultiplier
                );
                renderedAny = true;
            }
        }
        return renderedAny;
    }

    private void renderFlatDecalPlane(
            VertexConsumer buffer,
            Camera camera,
            float minX,
            float maxX,
            float minZ,
            float maxZ,
            float surfaceY,
            double centerX,
            double centerZ,
            float quadSize,
            int light,
            float alphaMultiplier
    ) {
        float u0 = this.getU0();
        float u1 = this.getU1();
        float v0 = this.getV0();
        float v1 = this.getV1();
        float halfSize = quadSize * 0.5F;
        Vec3 cameraPos = camera.getPosition();
        Vec2[] corners = new Vec2[]{
                new Vec2(minX, minZ),
                new Vec2(minX, maxZ),
                new Vec2(maxX, maxZ),
                new Vec2(maxX, minZ),
        };

        float cosYaw = Mth.cos(this.yawRotation);
        float sinYaw = Mth.sin(this.yawRotation);
        for (Vec2 corner : corners) {
            float offsetX = corner.x - (float) centerX;
            float offsetZ = corner.y - (float) centerZ;

            float zFightY = (zFightOffset + 0.08f) * Math.max(0.05f, alpha - 0.3f) * 0.05f;
            float uvLocalX = offsetX * cosYaw - offsetZ * sinYaw;
            float uvLocalZ = offsetX * sinYaw + offsetZ * cosYaw;
            float u = (uvLocalX / (2.0F * halfSize) + 0.5F) * (u1 - u0) + u0;
            float v = (uvLocalZ / (2.0F * halfSize) + 0.5F) * (v1 - v0) + v0;
            this.makeCornerVertex(
                    buffer,
                    new Vector3f(
                            corner.x - (float) cameraPos.x,
                            surfaceY - (float) cameraPos.y + zFightY,
                            corner.y - (float) cameraPos.z
                    ),
                    u,
                    v,
                    light,
                    alphaMultiplier
            );
        }
    }

    private void makeCornerVertex(VertexConsumer pConsumer, Vector3f pVertex, float pU, float pV, int pPackedLight, float alphaMultiplier) {
        pConsumer.addVertex(pVertex.x(), pVertex.y(), pVertex.z())
                .setColor(this.rCol, this.gCol, this.bCol, this.alpha * alphaMultiplier)
                .setUv(pU, pV)
                .setLight(pPackedLight);
    }

    @NotNull
    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<SimpleParticleType> {
        // A set of particle sprites.
        private final SpriteSet spriteSet;

        // The registration function passes a SpriteSet, so we accept that and store it for further use.
        public Provider(SpriteSet spriteSet) {
            this.spriteSet = spriteSet;
        }

        // This is where the magic happens. We return a new particle each time this method is called!
        // The type of the first parameter matches the generic type passed to the super interface.
        @Override
        @Nullable
        public Particle createParticle(@NotNull SimpleParticleType type, @NotNull ClientLevel level,
                                       double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            // We don't use the type and speed, and pass in everything else. You may of course use them if needed.
            return new BloodSprayParticle(level, x, y, z, spriteSet);
        }

//        @Override
//        public Particle create(BloodParticleOptions options, ClientLevel level, double x, double y, double z, double dx, double dy, double dz) {
//            return new BloodParticle(level, x, y, z, this.sprites, this.decalType, this.decalDirection, options.color(), options.scale(), dx, dy, dz);
//        }
    }
}
