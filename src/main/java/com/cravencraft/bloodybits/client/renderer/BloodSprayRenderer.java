package com.cravencraft.bloodybits.client.renderer;

import com.cravencraft.bloodybits.BloodyBitsMod;
import com.cravencraft.bloodybits.entity.custom.BloodSprayEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class BloodSprayRenderer extends EntityRenderer<BloodSprayEntity> {
    public static final ResourceLocation BLOOD_SPRAY = new ResourceLocation(BloodyBitsMod.MODID, "textures/entity/projectiles/blood_spray.png");
    public static final ResourceLocation BLOOD_SPLATTER_1 = new ResourceLocation(BloodyBitsMod.MODID, "textures/entity/blood_spatter.png");

    public BloodSprayRenderer(EntityRendererProvider.Context context) {
        super(context);

    }

    @Override
    public void render(BloodSprayEntity entity, float pEntityYaw, float pPartialTicks, PoseStack pPoseStack, @NotNull MultiBufferSource pBuffer, int pPackedLight) {
//        BloodyBitsMod.LOGGER.info("BLOOD POS: {} ON BLOCK POS: {}", entity.position(), entity.getOnPos());
//        BloodyBitsMod.LOGGER.info("BLOCK POS: {}", entity.blockPosition());
        pPoseStack.pushPose();
//        pPoseStack.mulPose(Axis.YP.rotationDegrees(Mth.lerp(pPartialTicks, entity.yRotO, entity.getYRot()) - 90.0F));
//        pPoseStack.mulPose(Axis.ZP.rotationDegrees(Mth.lerp(pPartialTicks, entity.xRotO, entity.getXRot())));
        // todo: I know this is dumb. I'll reverse it after testing.
        //TODO: For now this is the most accurate one. Maybe can play around with other methods later that
        //      Don't cause that small clipping through a block that only I will really notice.
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

        float f9 = (float)entity.shakeTime - pPartialTicks;
        if (f9 > 0.0F) {
            float f10 = -Mth.sin(f9 * 3.0F) * f9;
            pPoseStack.mulPose(Axis.ZP.rotationDegrees(f10));
        }

//        pPoseStack.mulPose(Axis.XP.rotationDegrees(45.0F));
        pPoseStack.scale(0.05625F, 0.05625F, 0.05625F);
//        pPoseStack.translate(0.0F, 0.0F, 0.0F);
//        RenderType.
        // Could make multiple textures based on what is being displayed? Wall splat being slightly different from a spray.
        // TODO: Make if statement here to swap between the textures when the entity is in a specific state.
        VertexConsumer vertexConsumer = pBuffer.getBuffer(RenderType.entityTranslucent(getTextureLocation(entity)));
//        VertexConsumer vertexconsumer = pBuffer.getBuffer();
        PoseStack.Pose posestack$pose = pPoseStack.last();
        Matrix4f matrix4f = posestack$pose.pose();
        Matrix3f matrix3f = posestack$pose.normal();

        //TODO: Math & for loops later. Figure this out now.
        //      We can definitely minimize the amount of vertices used here if they connect to one another.
        /*
            XP & XN = Goes through the Y-axis
            YP & YP = Goes through the Z-axis
            ZP & ZN = Goes through the X-axis
         */
//        BloodyBitsMod.LOGGER.info("RENDERER ENTITY STRETCH LIMIT {} X MIN {} X MAX", entity.stretchLimit, entity.xMinVal, entity.xMaxVal);

        // TODO: Can delete the sides once they're attached to the wall. Will actually work well and show them when it falls again.
        // TODO: Ok, this is the base vertices to create a closed rectangle. Now, we might be able to manipulate it.
        if (entity.xMin < entity.xMax) {

            // Right side
            this.vertex(matrix4f, matrix3f, vertexConsumer, entity.xMin, entity.yMin, entity.zMax, 0.5F, 0.0F, 0, 1, 0, pPackedLight);
            this.vertex(matrix4f, matrix3f, vertexConsumer, entity.xMax, entity.yMin, entity.zMax, 0.0F, 0.0F, 0, 1, 0, pPackedLight);
            this.vertex(matrix4f, matrix3f, vertexConsumer, entity.xMax, entity.yMax, entity.zMax, 0.0F, 0.125F, 0, 1, 0, pPackedLight);
            this.vertex(matrix4f, matrix3f, vertexConsumer, entity.xMin, entity.yMax, entity.zMax, 0.5F, 0.125F, 0, 1, 0, pPackedLight);

            // Left side
            this.vertex(matrix4f, matrix3f, vertexConsumer, entity.xMin, entity.yMax, entity.zMin, 0.5F, 0.0F, 0, 1, 0, pPackedLight);
            this.vertex(matrix4f, matrix3f, vertexConsumer, entity.xMax, entity.yMax, entity.zMin, 0.0F, 0.0F, 0, 1, 0, pPackedLight);
            this.vertex(matrix4f, matrix3f, vertexConsumer, entity.xMax, entity.yMin, entity.zMin, 0.0F, 0.125F, 0, 1, 0, pPackedLight);
            this.vertex(matrix4f, matrix3f, vertexConsumer, entity.xMin, entity.yMin, entity.zMin, 0.5F, 0.125F, 0, 1, 0, pPackedLight);

            // Top Side
            this.vertex(matrix4f, matrix3f, vertexConsumer, entity.xMin, entity.yMax, entity.zMin, 0.5F, 0.0F, 0, 1, 0, pPackedLight);
            this.vertex(matrix4f, matrix3f, vertexConsumer, entity.xMax, entity.yMax, entity.zMin, 0.0F, 0.0F, 0, 1, 0, pPackedLight);
            this.vertex(matrix4f, matrix3f, vertexConsumer, entity.xMax, entity.yMax, entity.zMax, 0.0F, 0.125F, 0, 1, 0, pPackedLight);
            this.vertex(matrix4f, matrix3f, vertexConsumer, entity.xMin, entity.yMax, entity.zMax, 0.5F, 0.125F, 0, 1, 0, pPackedLight);

            // Bottom Side
            this.vertex(matrix4f, matrix3f, vertexConsumer, entity.xMin, entity.yMin, entity.zMin, 0.5F, 0.0F, 0, 1, 0, pPackedLight);
            this.vertex(matrix4f, matrix3f, vertexConsumer, entity.xMax, entity.yMin, entity.zMin, 0.0F, 0.0F, 0, 1, 0, pPackedLight);
            this.vertex(matrix4f, matrix3f, vertexConsumer, entity.xMax, entity.yMin, entity.zMax, 0.0F, 0.125F, 0, 1, 0, pPackedLight);
            this.vertex(matrix4f, matrix3f, vertexConsumer, entity.xMin, entity.yMin, entity.zMax, 0.5F, 0.125F, 0, 1, 0, pPackedLight);

            // Front side
            this.vertex(matrix4f, matrix3f, vertexConsumer, entity.xMin, entity.yMax, entity.zMin, 0.375F, 0.0F, 0, 1, 0, pPackedLight);
            this.vertex(matrix4f, matrix3f, vertexConsumer, entity.xMin, entity.yMin, entity.zMin, 0.375F, 0.125F, 0, 1, 0, pPackedLight);
            this.vertex(matrix4f, matrix3f, vertexConsumer, entity.xMin, entity.yMin, entity.zMax, 0.5F, 0.125F, 0, 1, 0, pPackedLight);
            this.vertex(matrix4f, matrix3f, vertexConsumer, entity.xMin, entity.yMax, entity.zMax, 0.5F, 0.125F, 0, 1, 0, pPackedLight);

            // Back side
            this.vertex(matrix4f, matrix3f, vertexConsumer, entity.xMax, entity.yMax, entity.zMin, 0.0F, 0.0F, 0, 1, 0, pPackedLight);
            this.vertex(matrix4f, matrix3f, vertexConsumer, entity.xMax, entity.yMax, entity.zMax, 0.125F, 0.0F, 0, 1, 0, pPackedLight);
            this.vertex(matrix4f, matrix3f, vertexConsumer, entity.xMax, entity.yMin, entity.zMax, 0.125F, 0.125F, 0, 1, 0, pPackedLight);
            this.vertex(matrix4f, matrix3f, vertexConsumer, entity.xMax, entity.yMin, entity.zMin, 0.0F, 0.125F, 0, 1, 0, pPackedLight);
        }
        else {
//            if (entity.entityDirection.equals(Direction.NORTH) || entity.entityDirection.equals(Direction.SOUTH)) {
//                pPoseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
//            }
//            else if (entity.entityDirection.equals(Direction.UP) || entity.entityDirection.equals(Direction.DOWN)) {
//                pPoseStack.mulPose(Axis.ZP.rotationDegrees(90.0F));
//            }

            // Front side
            this.vertex(matrix4f, matrix3f, vertexConsumer, entity.xMin, entity.yMax, entity.zMin, 0.0F, 0.0F, 0, 1, 0, pPackedLight);
            this.vertex(matrix4f, matrix3f, vertexConsumer, entity.xMin, entity.yMin, entity.zMin, 0.0F, 1.0F, 0, 1, 0, pPackedLight);
            this.vertex(matrix4f, matrix3f, vertexConsumer, entity.xMin, entity.yMin, entity.zMax, 1.0F, 1.0F, 0, 1, 0, pPackedLight);
            this.vertex(matrix4f, matrix3f, vertexConsumer, entity.xMin, entity.yMax, entity.zMax, 1.0F, 0.0F, 0, 1, 0, pPackedLight);

            if (entity.entityDirection != null && !entity.entityDirection.equals(Direction.UP) && !entity.entityDirection.equals(Direction.DOWN)) {
                float zPos = (Math.abs(entity.zMax - entity.zMin) / 2) + entity.zMin;
                float yPos = (Math.abs(entity.yMax - entity.yMin) / 2) + entity.zMin;
                // Blood drip
                this.vertex(matrix4f, matrix3f, vertexConsumer, -0.5F, yPos, zPos - 0.25F, 0.53125F, 0.53125F, 0, 1, 0, pPackedLight);
                this.vertex(matrix4f, matrix3f, vertexConsumer, -0.5F, entity.yDrip, zPos - 0.25F, 0.53125F, 0.53125F, 0, 1, 0, pPackedLight);
                this.vertex(matrix4f, matrix3f, vertexConsumer, -0.5F, entity.yDrip, zPos + 0.25F, 0.53125F, 0.53125F, 0, 1, 0, pPackedLight);
                this.vertex(matrix4f, matrix3f, vertexConsumer, -0.5F, yPos, zPos + 0.25F, 0.53125F, 0.53125F, 0, 1, 0, pPackedLight);
            }

        }

        pPoseStack.popPose();
    }

    public void vertex(Matrix4f pMatrix, Matrix3f pNormal, VertexConsumer pConsumer, float pX, float pY, float pZ, float pU, float pV, int pNormalX, int pNormalZ, int pNormalY, int pPackedLight) {
        // TODO: Change the alpha as the entity is reaching the end of its life.
        //       Now that I'm using entityTranslucentCull the alpha is able to change.
        pConsumer
                .vertex(pMatrix, pX, pY, pZ)
                .color(255, 50, 50, 255)
                .uv(pU, pV)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(pPackedLight)
                .normal(pNormal, (float)pNormalX, (float)pNormalY, (float)pNormalZ)
                .endVertex();
    }

    @Override
    public ResourceLocation getTextureLocation(BloodSprayEntity bloodSprayEntity) {
        if (bloodSprayEntity.isInGround()) {
            return BLOOD_SPLATTER_1;
        }
        else {
            return BLOOD_SPRAY;
        }
    }
}
