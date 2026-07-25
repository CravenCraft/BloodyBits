package com.cravencraft.bloodybits.particle.spatter;

import com.cravencraft.bloodybits.BloodyBitsMod;
import com.cravencraft.bloodybits.particle.BloodSprayParticleOptions;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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

public class BloodSpatterParticle extends TextureSheetParticle {
    private final SpriteSet spriteSet;
    private static final Vector3f ROTATION_VECTOR = Util.make(new Vector3f(0.5F, 0.5F, 0.5F), Vector3f::normalize);
    private static final Vector3f TRANSFORM_VECTOR = new Vector3f(-1.0F, -1.0F, 0.0F);
    private static final float DEGREES_90 = Mth.PI / 2f;
    private static final int FADEOUT_BUFFER = 20;
    private static final float INITIAL_ALPHA = 0.7f;
    private final int fadeoutTime;
    private final Direction direction;
    private final float yawRotation;
    private final float zFightOffset;
    private float spatterQuadSize;
    private Vec3 cameraPos;
    private Vec3 worldExtentMin;
    private Vec3 worldExtentMax;
    private VertexConsumer vertexConsumer;
    private double centerX;
    private double centerY;
    private double centerZ;
    private int light;
    private int startDepth;
    private int endDepth;

    // First four parameters are self-explanatory. The SpriteSet parameter is provided by the
    // ParticleProvider, see below. You may also add additional parameters as needed, e.g. xSpeed/ySpeed/zSpeed.
    public BloodSpatterParticle(ClientLevel level, double x, double y, double z,
                                SpriteSet spriteSet, int color, int direction, float scale,
                                double xSpeed, double ySpeed, double zSpeed) {
        super(level, x, y, z);
        this.xd = xSpeed;
        this.yd = ySpeed;
        this.zd = zSpeed;
        this.quadSize = 1.5f * scale;
        this.friction = 0.5f;
        this.gravity = 1.0f;
        this.lifetime = 300;
        this.setSize(1.0f, 1.0f);
        this.scale(3f);
        this.spriteSet = spriteSet;
        this.direction = Direction.from3DDataValue(direction);
        this.fadeoutTime = 150;
        this.yawRotation = this.random.nextInt(4) * DEGREES_90;

        this.rCol = BloodSprayParticleOptions.red(color);
        this.gCol = BloodSprayParticleOptions.green(color);
        this.bCol = BloodSprayParticleOptions.blue(color);
        this.alpha = INITIAL_ALPHA;
        this.zFightOffset = this.random.nextFloat();

        // We set the initial sprite here since ticking is not guaranteed to set the sprite
        // before the render method is called.
        this.setSpriteFromAge(spriteSet);
    }

@Override
public void render(@NotNull VertexConsumer buffer, @NotNull Camera camera, float partialTick) {
    int fadeThreshold = lifetime - fadeoutTime; // Determines when the particle should start fading
    float quadSize = this.getQuadSize(partialTick); // Gets the quad size of the particle
    float f = this.age + partialTick; // Current age with the given partial tick
    this.cameraPos = camera.getPosition();
    this.vertexConsumer = buffer;

    if (this.direction == Direction.NORTH ||
        this.direction == Direction.SOUTH ||
        this.direction == Direction.EAST  ||
        this.direction == Direction.WEST) return;

    // If the current age is less than the splat time, then expand the quad size
    if (f <= SPLAT_IN_TIME) {
        quadSize *= (f) / (SPLAT_IN_TIME * 2f) + .5f;
    }

    // If the current age reaches the fadeThreshold, then decrease the quad size while fading the alpha.
    if (f > fadeThreshold) {
        quadSize *= (float) Mth.smoothstep(1.0 - Math.max(f - fadeThreshold - 60, 0) / fadeoutTime);
        this.alpha = 1.0F - Mth.clamp((f - fadeThreshold) / fadeoutTime, 1f - INITIAL_ALPHA, 1F);
    }

    this.spatterQuadSize = quadSize;

    // Will perform the operations defined in the lambda into arguments of the consumer's accept() method.
    // This flips the quaternion upside down (on the y-axis), then places it on its side (on the x-axis).
    // This would make a vertical standing image appear flat on the ground facing up.
    Consumer<Quaternionf> quatConsumer = (quat) -> {
        quat.mul(Axis.YP.rotation(-(float) Math.PI)); // I THINK tht Math.PI being negative here doesn't matter since it's a perfect 180 flip.
        quat.mul(Axis.XP.rotation(DEGREES_90));
        BloodyBitsMod.LOGGER.debug("Test Point");
    };

    this.renderRotatedParticle(partialTick, quatConsumer);
    this.renderColumnDecal();
}

    private static final float SPLAT_IN_TIME = 1.5f;
    private static final float MAX_PROJECTION_HEIGHT = 2.0f;
    private static final double HEIGHT_BRACKET_EPSILON = 1.0E-4D; // 0.0001

    private void renderRotatedParticle(float partialTick, Consumer<Quaternionf> pQuaternion) {
//        Vec3 cameraPos = camera.getPosition(); // The current camera position

        // Where the blood spatter x value is in relation to the camera.
        float localX = (float) (Mth.lerp(partialTick, this.xo, this.x) - this.cameraPos.x());

        // Where the blood spatter y value is in relation to the camera. It is at least 0.01f higher to potentially avoid
        // clipping through the boxes. Looks like as the particle gets closer to the end of its life it rises ever so slightly as well.
        // Don't know why this is.
        float localY = (float) (Mth.lerp(partialTick, this.yo, this.y) - this.cameraPos.y()) + 0.01f + (.005f * (this.age / (float) this.lifetime));

        // Where the blood spatter z value is in relation to the camera.
        float localZ = (float) (Mth.lerp(partialTick, this.zo, this.z) - this.cameraPos.z());

        // To my knowledge, a quaternion represents a 3-dimensional object's location and rotation.
        // So, maybe this is setting the rotation of the spatter to face up?
        Quaternionf quaternion = (new Quaternionf()).setAngleAxis(0.0F, ROTATION_VECTOR.x(), ROTATION_VECTOR.y(), ROTATION_VECTOR.z());

        // Performs the operations defined in the lambda expression of pQuaternion.
        pQuaternion.accept(quaternion); // Applies the new quaternion to the one in the argument.

        // Transforms the quaternion to a single location, which to my knowledge appears to be center and on top of
        // the blocks.
        quaternion.transform(TRANSFORM_VECTOR);

        // Creates a cube from vector points.
        Vector3f[] avector3f = new Vector3f[] {
                new Vector3f(-1.0F, -1.0F, 0.0F),
                new Vector3f(-1.0F, 1.0F, 0.0F),
                new Vector3f(1.0F, 1.0F, 0.0F),
                new Vector3f(1.0F, -1.0F, 0.0F)
        };

        // Applies the quaternion rotation and local positions to the cube.
        // The local
        for (int i = 0; i < 4; ++i) {
            Vector3f vector3f = avector3f[i];
            vector3f.rotate(quaternion);
            vector3f.mul(this.spatterQuadSize * 0.5f); // vector is a 2x2 plane, cut in half
            vector3f.add(localX, localY, localZ);
        }

        // Looks like this just sets the very edge of where the particle should be rendered.
        this.worldExtentMin = new Vec3(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
        this.worldExtentMax = new Vec3(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);

        // Go through each corner of the cube to determine the minimum and maximum corner values for all 3 axes
        for (Vector3f corner : avector3f) {
            Vec3 worldCorner = cameraPos.add(corner.x(), corner.y(), corner.z());
            this.worldExtentMin = new Vec3(
                    Math.min(this.worldExtentMin.x, worldCorner.x),
                    Math.min(this.worldExtentMin.y, worldCorner.y),
                    Math.min(this.worldExtentMin.z, worldCorner.z)
            );
            this.worldExtentMax = new Vec3(
                    Math.max(this.worldExtentMax.x, worldCorner.x),
                    Math.max(this.worldExtentMax.y, worldCorner.y),
                    Math.max(this.worldExtentMax.z, worldCorner.z)
            );
        }

        // Gets the light color (level?) of this particle
        this.light = this.getLightColor(partialTick);

        // Gets the center position of this particle in the level.
        // TODO: Could potentially get the world min/max from this center. Need to look into why the camera position
        //       is required so much here. Maybe in order to properly rotate the particle in relation to the camera
        //       before applying the rest of the math?
        this.centerX = Mth.lerp(partialTick, this.xo, this.x);
        this.centerY = Mth.lerp(partialTick, this.yo, this.y);
        this.centerZ = Mth.lerp(partialTick, this.zo, this.z);

        // Determine max depth
        this.startDepth = Mth.floor(centerY + 1.0D);
        this.endDepth = Mth.floor(centerY - MAX_PROJECTION_HEIGHT);
    }

    private void renderColumnDecal() {

        // Gets the min/max values for the x and z axes based on what block positions contain the
        // total extent of the particle.
        int minBlockX = BlockPos.containing(this.worldExtentMin).getX();
        int maxBlockX = BlockPos.containing(this.worldExtentMax).getX();
        int minBlockZ = BlockPos.containing(this.worldExtentMin).getZ();
        int maxBlockZ = BlockPos.containing(this.worldExtentMax).getZ();

        // Render the particle over each block contained within the min/max x/z coordinates.
        for (int blockX = minBlockX; blockX <= maxBlockX; blockX++) {
            for (int blockZ = minBlockZ; blockZ <= maxBlockZ; blockZ++) {
                var columnPos = new BlockPos.MutableBlockPos();

                for (int y = this.startDepth; y >= this.endDepth; y--) {
                    columnPos.set(blockX, y, blockZ);
                    // TODO: Check direction here
                    BlockPos surfacePos = columnPos.below();
                    BlockState blockState = this.level.getBlockState(surfacePos);

                    // If the block underneath this one is invisible or empty, then the rest of the loop is skipped.
                    // TODO: Check direction here
                    if (blockState.getRenderShape() == RenderShape.INVISIBLE || blockState.getCollisionShape(this.level, surfacePos).isEmpty()) {
                        continue;
                    }

                    // If the block has no shape (is not drawn?), then skip the rest of the loop.
                    VoxelShape shape = blockState.getShape(this.level, surfacePos, CollisionContext.empty());
                    if (shape.isEmpty()) {
                        continue;
                    }

                    if (this.renderBlockDecal(surfacePos, shape)) {
                        break; // TODO: This return is what causes the issue.
                    }
                }
            }
        }
    }

    private boolean renderBlockDecal(BlockPos surfacePos, VoxelShape shape) {
        AABB bounds = shape.bounds();
        float minX = surfacePos.getX() + (float) bounds.minX;
        float maxX = surfacePos.getX() + (float) bounds.maxX;
        float minZ = surfacePos.getZ() + (float) bounds.minZ;
        float maxZ = surfacePos.getZ() + (float) bounds.maxZ;

        minX = (float) Math.max(minX, this.worldExtentMin.x);
        maxX = (float) Math.min(maxX, this.worldExtentMax.x);
        minZ = (float) Math.max(minZ, this.worldExtentMin.z);
        maxZ = (float) Math.min(maxZ, this.worldExtentMax.z);

        List<AABB> boxes = shape.toAabbs();
        TreeSet<Double> heightBrackets = new TreeSet<>();
        for (AABB box : boxes) {
            heightBrackets.add(box.maxY);
        }

        boolean renderedAny = false;
        for (double localTopY : heightBrackets) {
            double worldTopY = surfacePos.getY() + localTopY;

            if (worldTopY > this.centerY + 0.25D) {
                continue;
            }

            // Loop through each box that that particle intersects
            for (AABB box : boxes) {

                // TODO: Keeping this as is for now, but will likely need to adjust it soon.
                if (Math.abs(box.maxY - localTopY) > HEIGHT_BRACKET_EPSILON) {
                    continue;
                }

                // Get the surface 2D vector positions for the block.
                float planeMinX = Math.max(minX, surfacePos.getX() + (float) box.minX);
                float planeMaxX = Math.min(maxX, surfacePos.getX() + (float) box.maxX);
                float planeMinZ = Math.max(minZ, surfacePos.getZ() + (float) box.minZ);
                float planeMaxZ = Math.min(maxZ, surfacePos.getZ() + (float) box.maxZ);

                if (planeMinX >= planeMaxX || planeMinZ >= planeMaxZ) {
                    continue;
                }

                float drop = (float) (this.centerY - worldTopY);
                float alphaMultiplier = Mth.lerp(
                        Mth.clamp(drop / MAX_PROJECTION_HEIGHT, 0.0F, 1.0F),
                        1.0F, 0.25F);
                var corners = BloodSpatterUtils.createCorners(planeMinX, planeMaxX, planeMinZ, planeMaxZ);

                this.renderFlatDecalPlane(corners, (float) worldTopY, alphaMultiplier);
                renderedAny = true;
            }
        }
        return renderedAny;
    }

    /**
     * TODO: Can likely modify the inputs of this method to be min and max values for the plane being modified.
     *
     * @param corners
     * @param surfaceY
     * @param alphaMultiplier
     */
    private void renderFlatDecalPlane(Vec2[] corners, float surfaceY, float alphaMultiplier) {
        // Just gets the given texture coordinates for this particle.
        float u0 = this.getU0();
        float u1 = this.getU1();
        float v0 = this.getV0();
        float v1 = this.getV1();

        float halfSize = this.spatterQuadSize * 0.5F;

        // These values SHOULD be fine since I believe they're only determining how much to rotate the texture
        // for spatter randomization, which shouldn't affect rendering at different angles?
        float cosYaw = Mth.cos(this.yawRotation);
        float sinYaw = Mth.sin(this.yawRotation);

        // Determines the corners
        for (Vec2 corner : corners) {
            // TODO: Will want to change these offsets based on the direction
            float offsetX = corner.x - (float) this.centerX;
            float offsetZ = corner.y - (float) this.centerZ;

            // The z-fighting to make the texture hoover ever so slightly above the block.
            //  TODO: Gonna want to create a variable to apply this to the vector argument for the vertex.
            //        Which will honestly probably be done when reworking the for-loop for the corners above.
            float zFightY = (zFightOffset + 0.08f) * Math.max(0.05f, alpha - 0.3f) * 0.05f;

            // I believe this just adjusts the texture coordinates for the vertex. Basically rotates the textures.
            // Should be able to leave this alone so long as I modify the offsetX and offsetZ values appropriately.
            float uvLocalX = offsetX * cosYaw - offsetZ * sinYaw;
            float uvLocalZ = offsetX * sinYaw + offsetZ * cosYaw;
            float u = (uvLocalX / (2.0F * halfSize) + 0.5F) * (u1 - u0) + u0;
            float v = (uvLocalZ / (2.0F * halfSize) + 0.5F) * (v1 - v0) + v0;
            var vec3f = new Vector3f(
                    corner.x - (float) this.cameraPos.x,
                    surfaceY - (float) this.cameraPos.y + zFightY,
                    corner.y - (float) this.cameraPos.z
            );

            this.makeCornerVertex(vec3f, u, v, alphaMultiplier);
        }
    }

    /*
        Remember, it takes 4 vertices to create a 2D texture. Sooooo, this is gonna be called 4 times per
        BlockPos that it interacts with? Or just 4 times total?
     */
    private void makeCornerVertex(Vector3f pVertex, float pU, float pV, float alphaMultiplier) {
        this.vertexConsumer
                .addVertex(pVertex.x(), pVertex.y(), pVertex.z())
                .setColor(this.rCol, this.gCol, this.bCol, this.alpha * alphaMultiplier)
                .setUv(pU, pV)
                .setLight(this.light);
    }

    @NotNull
    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<BloodSpatterParticleOptions> {
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
        public Particle createParticle(@NotNull BloodSpatterParticleOptions options, @NotNull ClientLevel level,
                                       double x, double y, double z,
                                       double xSpeed, double ySpeed, double zSpeed) {
            // We don't use the type and speed, and pass in everything else. You may of course use them if needed.
            return new BloodSpatterParticle(level, x, y, z, this.spriteSet, options.color(), options.direction(), options.scale(), xSpeed, ySpeed, zSpeed);
        }

//        @Override
//        public Particle create(BloodParticleOptions options, ClientLevel level, double x, double y, double z, double dx, double dy, double dz) {
//            return new BloodParticle(level, x, y, z, this.sprites, this.decalType, this.decalDirection, options.color(), options.scale(), dx, dy, dz);
//        }
    }
}
