package com.cravencraft.bloodybits.client.renderer;

import com.cravencraft.bloodybits.BloodyBitsMod;
import com.cravencraft.bloodybits.config.CommonConfig;
import com.cravencraft.bloodybits.entity.custom.BloodSprayEntity;
import com.cravencraft.bloodybits.utils.BloodyBitsUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

/**
 * This class will render the BloodSprayEntity on the client. It will use values defined within the BloodSprayEntity
 * (xMin, xMax, etc.) to render a rectangle dynamically that will act as a blood chunk.
 */
public class BloodSprayRenderer extends EntityRenderer<BloodSprayEntity> {
    public static final ResourceLocation SPRAY = new ResourceLocation(BloodyBitsMod.MODID, "textures/entity/blood_projectile/spray.png");

    public BloodSprayRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    /**
     * Renders a rectangle for blood chunks based on the position values defined in the BloodSprayEntity class.
     */
    @Override
    public void render(BloodSprayEntity entity, float pEntityYaw, float pPartialTicks, PoseStack pPoseStack, @NotNull MultiBufferSource pBuffer, int pPackedLight) {
        // Stops the blood from rendering as black when in a dark location such as a ceiling.
//        int correctedPackedLight = Math.max(pPackedLight, 10485776);
        pPoseStack.pushPose();

        if (entity.entityDirection == null) {
            pPoseStack.mulPose(Axis.YP.rotationDegrees(Mth.lerp(pPartialTicks, entity.yRotO, entity.getYRot()) - 90.0F));
            pPoseStack.mulPose(Axis.ZP.rotationDegrees(Mth.lerp(pPartialTicks, entity.xRotO, entity.getXRot())));
        }
        else if (entity.entityDirection.equals(Direction.NORTH) || entity.entityDirection.equals(Direction.SOUTH)) {
            pPoseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
        }
        else if (entity.entityDirection.equals(Direction.UP) || entity.entityDirection.equals(Direction.DOWN)) {
            pPoseStack.mulPose(Axis.ZP.rotationDegrees(90.0F));
        }

        int alpha = (int) (225 - (((double) entity.currentLifeTime / CommonConfig.despawnTime()) * 225));
        alpha = Math.max(0, alpha);
        
//        pPoseStack.mulPose(Axis.XP.rotationDegrees(45.0F));
        pPoseStack.scale(0.05625F, 0.05625F, 0.05625F);
        VertexConsumer vertexConsumer = pBuffer.getBuffer(RenderType.entityTranslucent(getTextureLocation(entity)));
        PoseStack.Pose posestack$pose = pPoseStack.last();
        Matrix4f matrix4f = posestack$pose.pose();
        Matrix3f matrix3f = posestack$pose.normal();

        /*
            XP & XN = Goes through the Y-axis
            YP & YP = Goes through the Z-axis
            ZP & ZN = Goes through the X-axis
         */
        if (entity.xMin < entity.xMax) {

            // Right side
            BloodyBitsUtils.vertex(matrix4f, matrix3f, vertexConsumer, entity.xMin, entity.yMin, entity.zMax, 0.5F, 0.0F,1, 1, 1, pPackedLight, entity.red, entity.green, entity.blue, alpha);
            BloodyBitsUtils.vertex(matrix4f, matrix3f, vertexConsumer, entity.xMax, entity.yMin, entity.zMax, 0.0F, 0.0F,1, 1, 1, pPackedLight, entity.red, entity.green, entity.blue, alpha);
            BloodyBitsUtils.vertex(matrix4f, matrix3f, vertexConsumer, entity.xMax, entity.yMax, entity.zMax, 0.0F, 0.125F,1, 1, 1, pPackedLight, entity.red, entity.green, entity.blue, alpha);
            BloodyBitsUtils.vertex(matrix4f, matrix3f, vertexConsumer, entity.xMin, entity.yMax, entity.zMax, 0.5F, 0.125F,1, 1, 1, pPackedLight, entity.red, entity.green, entity.blue, alpha);

            // Left side
            BloodyBitsUtils.vertex(matrix4f, matrix3f, vertexConsumer, entity.xMin, entity.yMax, entity.zMin, 0.5F, 0.0F,1, 1, 1, pPackedLight, entity.red, entity.green, entity.blue, alpha);
            BloodyBitsUtils.vertex(matrix4f, matrix3f, vertexConsumer, entity.xMax, entity.yMax, entity.zMin, 0.0F, 0.0F,1, 1, 1, pPackedLight, entity.red, entity.green, entity.blue, alpha);
            BloodyBitsUtils.vertex(matrix4f, matrix3f, vertexConsumer, entity.xMax, entity.yMin, entity.zMin, 0.0F, 0.125F,1, 1, 1, pPackedLight, entity.red, entity.green, entity.blue, alpha);
            BloodyBitsUtils.vertex(matrix4f, matrix3f, vertexConsumer, entity.xMin, entity.yMin, entity.zMin, 0.5F, 0.125F,1, 1, 1, pPackedLight, entity.red, entity.green, entity.blue, alpha);

            // Top Side
            BloodyBitsUtils.vertex(matrix4f, matrix3f, vertexConsumer, entity.xMin, entity.yMax, entity.zMin, 0.5F, 0.0F,1, 1, 1, pPackedLight, entity.red, entity.green, entity.blue, alpha);
            BloodyBitsUtils.vertex(matrix4f, matrix3f, vertexConsumer, entity.xMax, entity.yMax, entity.zMin, 0.0F, 0.0F,1, 1, 1, pPackedLight, entity.red, entity.green, entity.blue, alpha);
            BloodyBitsUtils.vertex(matrix4f, matrix3f, vertexConsumer, entity.xMax, entity.yMax, entity.zMax, 0.0F, 0.125F,1, 1, 1, pPackedLight, entity.red, entity.green, entity.blue, alpha);
            BloodyBitsUtils.vertex(matrix4f, matrix3f, vertexConsumer, entity.xMin, entity.yMax, entity.zMax, 0.5F, 0.125F,1, 1, 1, pPackedLight, entity.red, entity.green, entity.blue, alpha);

            // Bottom Side
            BloodyBitsUtils.vertex(matrix4f, matrix3f, vertexConsumer, entity.xMin, entity.yMin, entity.zMin, 0.5F, 0.0F,1, 1, 1, pPackedLight, entity.red, entity.green, entity.blue, alpha);
            BloodyBitsUtils.vertex(matrix4f, matrix3f, vertexConsumer, entity.xMax, entity.yMin, entity.zMin, 0.0F, 0.0F,1, 1, 1, pPackedLight, entity.red, entity.green, entity.blue, alpha);
            BloodyBitsUtils.vertex(matrix4f, matrix3f, vertexConsumer, entity.xMax, entity.yMin, entity.zMax, 0.0F, 0.125F,1, 1, 1, pPackedLight, entity.red, entity.green, entity.blue, alpha);
            BloodyBitsUtils.vertex(matrix4f, matrix3f, vertexConsumer, entity.xMin, entity.yMin, entity.zMax, 0.5F, 0.125F,1, 1, 1, pPackedLight, entity.red, entity.green, entity.blue, alpha);

            // Front side
            BloodyBitsUtils.vertex(matrix4f, matrix3f, vertexConsumer, entity.xMin, entity.yMax, entity.zMin, 0.375F, 0.0F,1, 1, 1, pPackedLight, entity.red, entity.green, entity.blue, alpha);
            BloodyBitsUtils.vertex(matrix4f, matrix3f, vertexConsumer, entity.xMin, entity.yMin, entity.zMin, 0.375F, 0.125F,1, 1, 1, pPackedLight, entity.red, entity.green, entity.blue, alpha);
            BloodyBitsUtils.vertex(matrix4f, matrix3f, vertexConsumer, entity.xMin, entity.yMin, entity.zMax, 0.5F, 0.125F,1, 1, 1, pPackedLight, entity.red, entity.green, entity.blue, alpha);
            BloodyBitsUtils.vertex(matrix4f, matrix3f, vertexConsumer, entity.xMin, entity.yMax, entity.zMax, 0.5F, 0.125F,1, 1, 1, pPackedLight, entity.red, entity.green, entity.blue, alpha);

            // Back side
            BloodyBitsUtils.vertex(matrix4f, matrix3f, vertexConsumer, entity.xMax, entity.yMax, entity.zMin, 0.0F, 0.0F,1, 1, 1, pPackedLight, entity.red, entity.green, entity.blue, alpha);
            BloodyBitsUtils.vertex(matrix4f, matrix3f, vertexConsumer, entity.xMax, entity.yMax, entity.zMax, 0.125F, 0.0F,1, 1, 1, pPackedLight, entity.red, entity.green, entity.blue, alpha);
            BloodyBitsUtils.vertex(matrix4f, matrix3f, vertexConsumer, entity.xMax, entity.yMin, entity.zMax, 0.125F, 0.125F,1, 1, 1, pPackedLight, entity.red, entity.green, entity.blue, alpha);
            BloodyBitsUtils.vertex(matrix4f, matrix3f, vertexConsumer, entity.xMax, entity.yMin, entity.zMin, 0.0F, 0.125F,1, 1, 1, pPackedLight, entity.red, entity.green, entity.blue, alpha);
        }
        else {

            // Front side
            BloodyBitsUtils.vertex(matrix4f, matrix3f, vertexConsumer, entity.xMin, entity.yMax, entity.zMin, 0.0F, 0.0F,1, 1, 1, pPackedLight, entity.red, entity.green, entity.blue, alpha);
            BloodyBitsUtils.vertex(matrix4f, matrix3f, vertexConsumer, entity.xMin, entity.yMin, entity.zMin, 0.0F, 1.0F,1, 1, 1, pPackedLight, entity.red, entity.green, entity.blue, alpha);
            BloodyBitsUtils.vertex(matrix4f, matrix3f, vertexConsumer, entity.xMin, entity.yMin, entity.zMax, 1.0F, 1.0F,1, 1, 1, pPackedLight, entity.red, entity.green, entity.blue, alpha);
            BloodyBitsUtils.vertex(matrix4f, matrix3f, vertexConsumer, entity.xMin, entity.yMax, entity.zMax, 1.0F, 0.0F,1, 1, 1, pPackedLight, entity.red, entity.green, entity.blue, alpha);

            if (entity.currentLifeTime > 50 && entity.entityDirection != null && entity.entityDirection.equals(Direction.DOWN)) {
                int correctedPackedLight = Math.max(pPackedLight, 10485776);
                VertexConsumer dripVertexConsumer = pBuffer.getBuffer(RenderType.entityTranslucent(SPRAY));
                float zPos = (Math.abs(entity.zMax - entity.zMin) / 2) + entity.zMin; // Gets the center point to make the Z-axis.
                float yPos = (Math.abs(entity.yMax - entity.yMin) / 2) + entity.yMin;
                float thickness = (entity.drip * 0.01F);

                // Decreases based on the lifetime of the drip.
                // TODO: This needs to happen BEFORE the original alpha calc. So, factor that in.
                alpha = (int) (255 - (255 * (entity.drip / BloodSprayEntity.MAX_DRIP_LENGTH)));

                /*
                 * Blood drip. Keeping it at just 2 drips so the player can see a drip at all angles without having to do too much crazy math
                 * to make an actual rectangle that would also add a performance cost. This is a good compromise.
                 */
                // Z-axis view
                BloodyBitsUtils.vertex(matrix4f, matrix3f, dripVertexConsumer, entity.xMax, yPos, zPos - 0.5F + thickness, 0.5F, 0.0F,1, 1, 1, correctedPackedLight, entity.red, entity.green, entity.blue, alpha);
                BloodyBitsUtils.vertex(matrix4f, matrix3f, dripVertexConsumer, -entity.drip, yPos, zPos - 0.5F + thickness, 0.0F, 0.0F,1, 1, 1, correctedPackedLight, entity.red, entity.green, entity.blue, alpha);
                BloodyBitsUtils.vertex(matrix4f, matrix3f, dripVertexConsumer, -entity.drip, yPos, zPos + 0.5F - thickness, 0.0F, 0.125F,1, 1, 1, correctedPackedLight, entity.red, entity.green, entity.blue, alpha);
                BloodyBitsUtils.vertex(matrix4f, matrix3f, dripVertexConsumer, entity.xMax, yPos, zPos + 0.5F - thickness, 0.5F, 0.125F,1, 1, 1, correctedPackedLight, entity.red, entity.green, entity.blue, alpha);

                // Y-axis view
                BloodyBitsUtils.vertex(matrix4f, matrix3f, vertexConsumer, entity.xMax, yPos - 0.5F + thickness, zPos, 0.5F, 0.0F,1, 1, 1, correctedPackedLight, entity.red, entity.green, entity.blue, alpha);
                BloodyBitsUtils.vertex(matrix4f, matrix3f, vertexConsumer, -entity.drip, yPos - 0.5F + thickness, zPos, 0.0F, 0.0F,1, 1, 1, correctedPackedLight, entity.red, entity.green, entity.blue, alpha);
                BloodyBitsUtils.vertex(matrix4f, matrix3f, vertexConsumer, -entity.drip, yPos + 0.5F - thickness, zPos, 0.0F, 0.125F,1, 1, 1, correctedPackedLight, entity.red, entity.green, entity.blue, alpha);
                BloodyBitsUtils.vertex(matrix4f, matrix3f, vertexConsumer, entity.xMax, yPos + 0.5F - thickness, zPos, 0.5F, 0.125F,1, 1, 1, correctedPackedLight, entity.red, entity.green, entity.blue, alpha);
            }

        }

        pPoseStack.popPose();
    }

    /**
     * Gets a random blood chunk texture to render for the entity.
     *
     * @param bloodSprayEntity
     * @return
     */
    @Override
    public ResourceLocation getTextureLocation(BloodSprayEntity bloodSprayEntity) {
        if (!bloodSprayEntity.isSolid && bloodSprayEntity.isInGround()) {
            return this.getRandomSpatterTexture(bloodSprayEntity.randomTextureNumber);
        }
        else {
            return SPRAY;
        }
    }

    /**
     * Stores the random blood spatter textures
     *
     * @param randomInt
     * @return
     */
    private ResourceLocation getRandomSpatterTexture(int randomInt) {
        return switch (randomInt) {
            case 1 -> new ResourceLocation(BloodyBitsMod.MODID, "textures/entity/blood_spatter/spatter_1.png");
            case 2 -> new ResourceLocation(BloodyBitsMod.MODID, "textures/entity/blood_spatter/spatter_2.png");
            case 3 -> new ResourceLocation(BloodyBitsMod.MODID, "textures/entity/blood_spatter/spatter_3.png");
            case 4 -> new ResourceLocation(BloodyBitsMod.MODID, "textures/entity/blood_spatter/spatter_4.png");
            case 5 -> new ResourceLocation(BloodyBitsMod.MODID, "textures/entity/blood_spatter/spatter_5.png");
            case 6 -> new ResourceLocation(BloodyBitsMod.MODID, "textures/entity/blood_spatter/spatter_6.png");
            default -> new ResourceLocation(BloodyBitsMod.MODID, "textures/entity/blood_spatter/spatter_0.png");
        };
    }
}
