package com.cravencraft.bloodybits.client.particle.spatter;

import com.cravencraft.bloodybits.BloodyBitsMod;
import com.cravencraft.bloodybits.config.ClientConfig;
import com.cravencraft.bloodybits.client.particle.drip.BloodDripParticleOptions;
import com.cravencraft.bloodybits.utils.BloodyBitsUtils;
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
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.*;
import java.util.function.Consumer;

/**
 * TODO: What I want spatters to do:
 *       - Have occasional smaller drips slide down the wall at a faster pace (NORTH, SOUTH, EAST, & WEST).
 */
public class BloodSpatterParticle extends TextureSheetParticle {
//    private final SpriteSet spriteSet;
    private static final Vector3f ROTATION_VECTOR = Util.make(new Vector3f(0.5F, 0.5F, 0.5F), Vector3f::normalize);
    private static final Vector3f TRANSFORM_VECTOR = new Vector3f(-1.0F, -1.0F, 0.0F);
    private static final float DEGREES_90 = Mth.PI / 2f;
    private static final int FADEOUT_BUFFER = 20;
    private static final float INITIAL_ALPHA = 1.0f;
    private static final int FALLBACK_LIFETIME = 600;
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
    private final String color;
    private int light;
    private int startDepth;
    private int endDepth;
    private boolean shouldDrip;
    private int dripAge;

    // First four parameters are self-explanatory. The SpriteSet parameter is provided by the
    // ParticleProvider, see below. You may also add additional parameters as needed, e.g. xSpeed/ySpeed/zSpeed.
    public BloodSpatterParticle(ClientLevel level, double x, double y, double z,
                                SpriteSet spriteSet, String color, int direction, float scale,
                                double xSpeed, double ySpeed, double zSpeed) {
        super(level, x, y, z);
        this.xd = xSpeed;
        this.yd = ySpeed;
        this.zd = zSpeed;
        this.quadSize = 1.5f * scale;
        this.gravity = 0.0f;
        this.lifetime = (BloodyBitsMod.isClientConfigLoaded) ? ClientConfig.getBloodSpatterLifeTime() : FALLBACK_LIFETIME;
        this.setSize(1.0f, 1.0f);
        this.scale(3f);
        this.pickSprite(spriteSet);
        this.direction = Direction.from3DDataValue(direction);
        this.fadeoutTime = this.lifetime / 2;
        this.yawRotation = this.random.nextInt(4) * DEGREES_90;
        this.color = color;
        this.rCol = BloodyBitsUtils.normalizeColorValue(HexFormat.fromHexDigits(color, 1, 3));
        this.gCol = BloodyBitsUtils.normalizeColorValue(HexFormat.fromHexDigits(color, 3, 5));
        this.bCol = BloodyBitsUtils.normalizeColorValue(HexFormat.fromHexDigits(color.substring(5)));
        this.alpha = INITIAL_ALPHA;
        this.zFightOffset = this.random.nextFloat();
//        this.shouldDrip = true;

        // We set the initial sprite here since ticking is not guaranteed to set the sprite
        // before the render method is called.
//        this.setSpriteFromAge(spriteSet);
    }

    @Override
    public void tick() {
        super.tick();

        var blockPos = BlockPos.containing(this.centerX, this.centerY, this.centerZ);
        if (this.level.isRainingAt(blockPos) || this.level.isWaterAt(blockPos)) {
            this.age += 10;
        }

        this.createDrip();
    }

    @Override
    public void render(@NotNull VertexConsumer buffer, @NotNull Camera camera, float partialTick) {
        int fadeThreshold = lifetime - fadeoutTime; // Determines when the particle should start fading
        float quadSize = this.getQuadSize(partialTick); // Gets the quad size of the particle
        float f = this.age + partialTick; // Current age with the given partial tick
        this.cameraPos = camera.getPosition();
        this.vertexConsumer = buffer;

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

        this.renderRotatedParticle(partialTick);
        this.renderColumnDecal();
    }

    private static final float SPLAT_IN_TIME = 1.5f;
    private static final float MAX_PROJECTION_DEPTH = 2.0f;
    private static final double HEIGHT_BRACKET_EPSILON = 1.0E-4D; // 0.0001

    private void renderRotatedParticle(float partialTick) {
//        BloodyBitsMod.LOGGER.info("in renderRotatedParticle");

        // Where the blood spatter particle is in relation to the camera.
        float localX = (float) (Mth.lerp(partialTick, this.xo, this.x) - this.cameraPos.x());
        float localY = (float) (Mth.lerp(partialTick, this.yo, this.y) - this.cameraPos.y());
        float localZ = (float) (Mth.lerp(partialTick, this.zo, this.z) - this.cameraPos.z());

        // Will perform the operations defined in the lambda into arguments of the consumer's accept() method.
        // This flips the quaternion upside down (on the y-axis), then places it on its side (on the x-axis).
        // This would make a vertical standing image appear flat on the ground facing up.
        Consumer<Quaternionf> quatConsumer = (quat) -> {

            switch (this.direction) {
                case UP, DOWN -> {
                    quat.mul(Axis.YN.rotation((float) Math.PI));
                    quat.mul(Axis.XP.rotation(DEGREES_90));
                }
                case NORTH, SOUTH -> quat.mul(Axis.ZP.rotation((float) Math.PI));
                case EAST, WEST -> {
                    quat.mul(Axis.XN.rotation((float) Math.PI));
                    quat.mul(Axis.YP.rotation(DEGREES_90));
                }
            }
        };

        Quaternionf quaternion = (new Quaternionf()).setAngleAxis(0.0F, ROTATION_VECTOR.x(), ROTATION_VECTOR.y(), ROTATION_VECTOR.z());

        // Performs the operations defined in the lambda expression of pQuaternion.
        quatConsumer.accept(quaternion);

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
            float slide = 0.0f;
            if (this.age > 30) {
                slide = 2.0f;
            }
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

        this.light = this.getLightColor(partialTick);

        // Gets the center position of this particle in the level.
        this.centerX = Mth.lerp(partialTick, this.xo, this.x);
        this.centerY = Mth.lerp(partialTick, this.yo, this.y);
        this.centerZ = Mth.lerp(partialTick, this.zo, this.z);

        switch (this.direction) {
            case UP -> {
                this.centerY += 0.5;
                this.startDepth = Mth.floor(this.centerY - 1.0D);
                this.endDepth = Mth.floor(this.centerY + MAX_PROJECTION_DEPTH);
            }
            case DOWN -> {
                this.startDepth = Mth.floor(this.centerY + 1.0D);
                this.endDepth = Mth.floor(this.centerY - MAX_PROJECTION_DEPTH);
            }
            case NORTH ->  {
                this.centerZ -= 0.25;
                this.startDepth = Mth.floor(this.centerZ + 1.0D);
                this.endDepth = Mth.floor(this.centerZ - MAX_PROJECTION_DEPTH);
            }
            case SOUTH ->  {
                this.centerZ += 0.25;
                this.startDepth = Mth.floor(this.centerZ - 1.0D);
                this.endDepth = Mth.floor(this.centerZ + MAX_PROJECTION_DEPTH);
            }
            case EAST ->  {
                this.centerX += 0.25;
                this.startDepth = Mth.floor(this.centerX - 1.0D);
                this.endDepth = Mth.floor(this.centerX + MAX_PROJECTION_DEPTH);
            }
            case WEST ->  {
                this.centerX -= 0.25;
                this.startDepth = Mth.floor(this.centerX + 1.0D);
                this.endDepth = Mth.floor(this.centerX - MAX_PROJECTION_DEPTH);
            }
        }
    }

    private void renderColumnDecal() {

        // Gets the min/max values for the x and z axes based on what block positions contain the
        // total extent of the particle.
        int minBlockWidth = 0;
        int maxBlockWidth = 0;
        int minBlockLength = 0;
        int maxBlockLength = 0;

        switch (this.direction) {
            case UP, DOWN -> {
                minBlockWidth = BlockPos.containing(this.worldExtentMin).getX();
                maxBlockWidth = BlockPos.containing(this.worldExtentMax).getX();
                minBlockLength = BlockPos.containing(this.worldExtentMin).getZ();
                maxBlockLength = BlockPos.containing(this.worldExtentMax).getZ();
            }
            case NORTH, SOUTH ->  {
                minBlockWidth = BlockPos.containing(this.worldExtentMin).getX();
                maxBlockWidth = BlockPos.containing(this.worldExtentMax).getX();
                minBlockLength = BlockPos.containing(this.worldExtentMin).getY();
                maxBlockLength = BlockPos.containing(this.worldExtentMax).getY();
            }
            case EAST, WEST ->  {
                minBlockWidth = BlockPos.containing(this.worldExtentMin).getY();
                maxBlockWidth = BlockPos.containing(this.worldExtentMax).getY();
                minBlockLength = BlockPos.containing(this.worldExtentMin).getZ();
                maxBlockLength = BlockPos.containing(this.worldExtentMax).getZ();
            }
        }

        // Render the particle over each block contained within the min/max x/z coordinates.
        for (int blockWidth = minBlockWidth; blockWidth <= maxBlockWidth; blockWidth++) {
            for (int blockLength = minBlockLength; blockLength <= maxBlockLength; blockLength++) {
                var columnPos = new BlockPos.MutableBlockPos();
                var blockDepth = this.startDepth;

                while (Math.abs(blockDepth - this.endDepth) > 0) {
                    BlockPos surfacePos;
                    switch (this.direction) {
                        case UP -> {
                            columnPos.set(blockWidth, blockDepth, blockLength);
                            blockDepth++;
                            surfacePos = columnPos.above();
                        }
                        case DOWN -> {
                            columnPos.set(blockWidth, blockDepth, blockLength);
                            blockDepth--;
                            surfacePos = columnPos.below();
                        }
                        case NORTH -> {
                            columnPos.set(blockWidth, blockLength, blockDepth);
                            blockDepth--;
                            surfacePos = columnPos.north();
                        }
                        case SOUTH -> {
                            columnPos.set(blockWidth, blockLength, blockDepth);
                            blockDepth++;
                            surfacePos = columnPos.south();
                        }
                        case EAST -> {
                            columnPos.set(blockDepth, blockWidth, blockLength);
                            blockDepth++;
                            surfacePos = columnPos.east();
                        }
                        case WEST -> {
                            columnPos.set(blockDepth, blockWidth, blockLength);
                            blockDepth--;
                            surfacePos = columnPos.west();
                        }
                        default -> {
                            // Just immediately break out of the loop.
                            blockDepth = this.endDepth;
                            surfacePos = columnPos.below();
                        }
                    }

                    BlockState blockState = this.level.getBlockState(surfacePos);

                    // If the block underneath this one is invisible or empty, then the rest of the loop is skipped.
                    if (blockState.getRenderShape() == RenderShape.INVISIBLE || blockState.getCollisionShape(this.level, surfacePos).isEmpty()) {
                        continue;
                    }

                    // If the block has no shape (is not drawn?), then skip the rest of the loop.
                    VoxelShape shape = blockState.getShape(this.level, surfacePos);
                    if (shape.isEmpty()) {
                        continue;
                    }

                    if (this.renderBlockDecal(surfacePos, shape)) {
                        break;
                    }
                }
            }
        }
    }

    private boolean renderBlockDecal(BlockPos surfacePos, VoxelShape shape) {
        AABB bounds = shape.bounds();
        List<AABB> boxes = shape.toAabbs();
        TreeSet<Double> depthBrackets = new TreeSet<>();
        boolean renderedAny = false;

        float minWidth;
        float maxWidth;
        float minLength;
        float maxLength;
        switch (this.direction) {
            case UP, DOWN -> {
                minWidth = (float) Math.max(surfacePos.getX() + (float) bounds.minX, this.worldExtentMin.x);
                maxWidth = (float) Math.min(surfacePos.getX() + (float) bounds.maxX, this.worldExtentMax.x);
                minLength = (float) Math.max(surfacePos.getZ() + (float) bounds.minZ, this.worldExtentMin.z);
                maxLength = (float) Math.min(surfacePos.getZ() + (float) bounds.maxZ, this.worldExtentMax.z);
            }
            case NORTH, SOUTH -> {
                minWidth = (float) Math.max(surfacePos.getX() + (float) bounds.minX, this.worldExtentMin.x);
                maxWidth = (float) Math.min(surfacePos.getX() + (float) bounds.maxX, this.worldExtentMax.x);
                minLength = (float) Math.max(surfacePos.getY() + (float) bounds.minY, this.worldExtentMin.y);
                maxLength = (float) Math.min(surfacePos.getY() + (float) bounds.maxY, this.worldExtentMax.y);
            }
            case EAST, WEST -> {
                minWidth = (float) Math.max(surfacePos.getY() + (float) bounds.minY, this.worldExtentMin.y);
                maxWidth = (float) Math.min(surfacePos.getY() + (float) bounds.maxY, this.worldExtentMax.y);
                minLength = (float) Math.max(surfacePos.getZ() + (float) bounds.minZ, this.worldExtentMin.z);
                maxLength = (float) Math.min(surfacePos.getZ() + (float) bounds.maxZ, this.worldExtentMax.z);
            }
            default -> {
                return false;
            }
        }

        for (AABB box : boxes) {
            switch (this.direction) {
                case UP -> depthBrackets.add(box.minY);
                case DOWN -> depthBrackets.add(box.maxY);
                case NORTH -> depthBrackets.add(box.maxZ);
                case SOUTH -> depthBrackets.add(box.minZ);
                case EAST -> depthBrackets.add(box.minX);
                case WEST -> depthBrackets.add(box.maxX);
            }
        }

        // Looping through the depth established in the above loop. The first switch statement will check to see if
        // the depth is too far from the particle itself. The inner loop will loop through the boxes of the blocks
        // that the blood spatter particle is contacting, and it will skip values that don't match up with the current
        // localDepth, which ensures that the box in the loop is the one that needs to be modified.
        for (double localDepth : depthBrackets) {

            double worldMaxDepth = 0.0;
            switch(this.direction) {
                case UP -> {
                    worldMaxDepth = surfacePos.getY() + localDepth;
                    if (worldMaxDepth < this.centerY - 0.25D) {
                        continue;
                    }
                }
                case DOWN -> {
                    worldMaxDepth = surfacePos.getY() + localDepth;
                    if (worldMaxDepth > this.centerY + 0.25D) {
                        continue;
                    }
                }
                case NORTH -> {
                    worldMaxDepth = surfacePos.getZ() + localDepth;
                    if (worldMaxDepth > this.centerZ + 0.25D) {
                        continue;
                    }
                }
                case SOUTH -> {
                    worldMaxDepth = surfacePos.getZ() + localDepth;
                    if (worldMaxDepth < this.centerZ - 0.25D) {
                        continue;
                    }
                }
                case EAST -> {
                    worldMaxDepth = surfacePos.getX() + localDepth;
                    if (worldMaxDepth < this.centerX - 0.25D) {
                        continue;
                    }
                }
                case WEST -> {
                    worldMaxDepth = surfacePos.getX() + localDepth;
                    if (worldMaxDepth > this.centerX + 0.25D) {
                        continue;
                    }
                }
            }

            // Loop through each box that that particle intersects
            for (AABB box : boxes) {
                // Skip current loop iteration if the box depth and localDepth values don't match.
                switch (this.direction) {
                    case UP -> {
                        if (Math.abs(box.minY - localDepth) > HEIGHT_BRACKET_EPSILON) {
                            continue;
                        }
                    }
                    case DOWN -> {
                        if (Math.abs(box.maxY - localDepth) > HEIGHT_BRACKET_EPSILON) {
                            continue;
                        }
                    }
                    case NORTH -> {
                        if (Math.abs(box.maxZ - localDepth) > HEIGHT_BRACKET_EPSILON) {
                            continue;
                        }
                    }
                    case SOUTH -> {
                        if (Math.abs(box.minZ - localDepth) > HEIGHT_BRACKET_EPSILON) {
                            continue;
                        }
                    }
                    case EAST -> {
                        if (Math.abs(box.minX - localDepth) > HEIGHT_BRACKET_EPSILON) {
                            continue;
                        }
                    }
                    case WEST -> {
                        if (Math.abs(box.maxX - localDepth) > HEIGHT_BRACKET_EPSILON) {
                            continue;
                        }
                    }
                }

                float planeMinWidth = 0.0F;
                float planeMaxWidth = 0.0F;
                float planeMinLength = 0.0F;
                float planeMaxLength = 0.0F;
                float drop = 0.0F;

                // Establish the minimum and maximum depth dimensions for the plane.
                switch (this.direction) {
                    case UP, DOWN -> {
                        planeMinWidth = Math.max(minWidth, surfacePos.getX() + (float) box.minX);
                        planeMaxWidth = Math.min(maxWidth, surfacePos.getX() + (float) box.maxX);
                        planeMinLength = Math.max(minLength, surfacePos.getZ() + (float) box.minZ);
                        planeMaxLength = Math.min(maxLength, surfacePos.getZ() + (float) box.maxZ);
                        drop = (float) (this.centerY - worldMaxDepth);
                    }
                    case NORTH, SOUTH -> {
                        planeMinWidth = Math.max(minWidth, surfacePos.getX() + (float) box.minX);
                        planeMaxWidth = Math.min(maxWidth, surfacePos.getX() + (float) box.maxX);
                        planeMinLength = Math.max(minLength, surfacePos.getY() + (float) box.minY);
                        planeMaxLength = Math.min(maxLength, surfacePos.getY() + (float) box.maxY);
                        drop = (float) (this.centerZ - worldMaxDepth);
                    }
                    case EAST, WEST -> {
                        planeMinWidth = Math.max(minWidth, surfacePos.getY() + (float) box.minY);
                        planeMaxWidth = Math.min(maxWidth, surfacePos.getY() + (float) box.maxY);
                        planeMinLength = Math.max(minLength, surfacePos.getZ() + (float) box.minZ);
                        planeMaxLength = Math.min(maxLength, surfacePos.getZ() + (float) box.maxZ);
                        drop = (float) (this.centerX - worldMaxDepth);
                    }
                }

                // Break if mins and maxes aren't properly setup.
                if (planeMinWidth >= planeMaxWidth || planeMinLength >= planeMaxLength) {
                    continue;
                }

                float alphaMultiplier = Mth.lerp(
                        Mth.clamp(drop / MAX_PROJECTION_DEPTH, 0.0F, 1.0F),
                        1.0F, 0.25F);

                var corners = BloodyBitsUtils.createCorners(planeMinWidth, planeMaxWidth,
                        planeMinLength, planeMaxLength);

                this.renderFlatDecalPlane(corners, (float) worldMaxDepth, alphaMultiplier);
                renderedAny = true;
            }
        }

        return renderedAny;
    }

    /**
     * TODO: Can likely modify the inputs of this method to be min and max values for the plane being modified.
     *
     * @param corners
     * @param planeSurface
     * @param alphaMultiplier
     */
    private void renderFlatDecalPlane(Vec2[] corners, float planeSurface, float alphaMultiplier) {
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
        // Gotta flip it & draw it the opposite way for it being direction UP.
        var cornersList = new ArrayList<>(List.of(corners));

        // Want to reverse the list if we need to flip the image.
        if (direction == Direction.UP || this.direction == Direction.NORTH || direction == Direction.WEST) {
            Collections.reverse(cornersList);
        }

        for (Vec2 corner : cornersList) {

            float offsetWidth = 0.0F;
            float offsetHeight = 0.0F;
            switch (this.direction) {
                case UP, DOWN -> {
                    offsetWidth = corner.x - (float) this.centerX;
                    offsetHeight = corner.y - (float) this.centerZ;
                }
                case NORTH, SOUTH -> {
                    offsetWidth = corner.x - (float) this.centerX;
                    offsetHeight = corner.y - (float) this.centerY;
                }
                case EAST, WEST -> {
                    offsetWidth = corner.x - (float) this.centerY;
                    offsetHeight = corner.y - (float) this.centerZ;
                }
            }

            // The z-fighting to make the texture hoover ever so slightly above the block.
            float zFightDepth = (this.zFightOffset + 0.08f) * Math.max(0.05f, alpha - 0.3f) * 0.05f;

            float uvLocalX = offsetWidth * cosYaw - offsetHeight * sinYaw;
            float uvLocalZ = offsetWidth * sinYaw + offsetHeight * cosYaw;
            float u = (uvLocalX / (2.0F * halfSize) + 0.5F) * (u1 - u0) + u0;
            float v = (uvLocalZ / (2.0F * halfSize) + 0.5F) * (v1 - v0) + v0;

            Vector3f vec3f = getCornerVector(planeSurface, corner, zFightDepth);
            this.makeCornerVertex(vec3f, u, v, alphaMultiplier);
        }
    }

    private @NotNull Vector3f getCornerVector(float planeSurface, Vec2 corner, float zFightDepth) {
        Vector3f vec3f = new Vector3f();
        switch (this.direction) {
            case UP -> vec3f = new Vector3f(
                    corner.x - (float) this.cameraPos.x,
                    planeSurface - (float) this.cameraPos.y - zFightDepth,
                    corner.y - (float) this.cameraPos.z
            );
            case DOWN -> vec3f = new Vector3f(
                    corner.x - (float) this.cameraPos.x,
                    planeSurface - (float) this.cameraPos.y + zFightDepth,
                    corner.y - (float) this.cameraPos.z
            );
            case NORTH -> vec3f = new Vector3f(
                    corner.x - (float) this.cameraPos.x,
                    corner.y - (float) this.cameraPos.y,
                    planeSurface - (float) this.cameraPos.z + zFightDepth
            );
            case SOUTH -> vec3f = new Vector3f(
                    corner.x - (float) this.cameraPos.x,
                    corner.y - (float) this.cameraPos.y,
                    planeSurface - (float) this.cameraPos.z - zFightDepth
            );
            case EAST -> vec3f = new Vector3f(
                    planeSurface - (float) this.cameraPos.x - zFightDepth,
                     corner.x - (float) this.cameraPos.y,
                    corner.y - (float) this.cameraPos.z
            );
            case WEST -> vec3f = new Vector3f(
                    planeSurface - (float) this.cameraPos.x + zFightDepth,
                    corner.x - (float) this.cameraPos.y,
                    corner.y - (float) this.cameraPos.z
            );
        }
        return vec3f;
    }

    private void makeCornerVertex(Vector3f pVertex, float pU, float pV, float alphaMultiplier) {
        this.vertexConsumer
                .vertex(pVertex.x(), pVertex.y(), pVertex.z())
                .uv(pU, pV)
                .color(this.rCol, this.gCol, this.bCol, this.alpha * alphaMultiplier)
                .uv2(this.light)
                .endVertex();
    }

    private void createDrip() {

        if (this.shouldDrip) {
            if (this.direction == Direction.UP) {
                this.level.addAlwaysVisibleParticle(
                        new BloodDripParticleOptions(this.color, Direction.UP.get3DDataValue(), this.alpha),
                        true, this.x, this.y, this.z,
                        0.0D, 0.0D, 0.0D);
                this.shouldDrip = false;
                this.dripAge = 0;
            }
        }
        else {
            if (this.dripAge < 25) {
                this.dripAge++;
            }
            else {
                this.shouldDrip = Math.random() > 0.99;
            }
        }
    }

    @NotNull
    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    /**
     * @param spriteSet A set of particle sprites.
     */
    @OnlyIn(Dist.CLIENT)
    public record Provider(SpriteSet spriteSet) implements ParticleProvider<BloodSpatterParticleOptions> {

        // This is where the magic happens. We return a new particle each time this method is called!
        // The type of the first parameter matches the generic type passed to the super interface.
        @Override
        public Particle createParticle(@NotNull BloodSpatterParticleOptions options, @NotNull ClientLevel level,
                                                double x, double y, double z,
                                                double xSpeed, double ySpeed, double zSpeed) {
            // We don't use the type and speed, and pass in everything else. You may of course use them if needed.
            return new BloodSpatterParticle(
                    level, x, y, z,
                    this.spriteSet, options.color(), options.direction(),
                    options.scale(), xSpeed, ySpeed, zSpeed);
        }
    }
}
