package com.cravencraft.bloodybits.client.renderer;

import com.cravencraft.bloodybits.BloodyBitsMod;
import com.cravencraft.bloodybits.entity.custom.BloodSprayEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class BloodSprayRenderer extends EntityRenderer<BloodSprayEntity> {
    public static final ResourceLocation BLOOD_SPRAY = new ResourceLocation(BloodyBitsMod.MODID, "textures/entity/projectiles/blood_spray.png");
    public static final ResourceLocation BLOOD_SPLATTER_1 = new ResourceLocation(BloodyBitsMod.MODID, "textures/entity/blood_splatter_1.png");
//    protected final EntityModel<BloodSprayEntity> model;
    protected EntityRendererProvider.Context context;

    public BloodSprayRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.context = context;
        BloodyBitsMod.LOGGER.info("RENDERER RESOURCE MANAGER: {}", context.getResourceManager());
        BloodyBitsMod.LOGGER.info("SKIN MAP: {}", context.getEntityRenderDispatcher().getSkinMap());
        BloodyBitsMod.LOGGER.info("MODEL SHAPER: {}", context.getItemRenderer().getItemModelShaper().shapes);

    }

    public void render(BloodSprayEntity entity, float pEntityYaw, float pPartialTicks, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight) {
        EntityRenderDispatcher dispatcher = this.context.getEntityRenderDispatcher();
        BloodyBitsMod.LOGGER.info("BloodSprayEntity last state: {}", entity.lastState);
//        BloodyBitsMod.LOGGER.info("RENDERER DISPATCHER INFO: {}", );
//        BloodyBitsMod.LOGGER.info("RENDERER DISPATCHER INFO: {}");
//        dispatcher.getRenderer(entity).getTextureLocation(entity);
//        BloodyBitsMod.LOGGER.info("X ROT AND X ROT0: {} - {}", entity.xRotO, entity.getXRot());
//        BloodyBitsMod.LOGGER.info("ENTITY YAW: {}", pEntityYaw);
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
        // Could make multiple textures based on what is being displayed? Wall splat being slightly different from a spray.
        VertexConsumer vertexconsumer = pBuffer.getBuffer(RenderType.entitySolid(this.getTextureLocation(entity)));
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
            this.vertex(matrix4f, matrix3f, vertexconsumer, entity.xMin, entity.yMin, entity.zMax, 0.0F, 0.15625F, 0, 1, 0, pPackedLight);
            this.vertex(matrix4f, matrix3f, vertexconsumer, entity.xMax, entity.yMin, entity.zMax, 0.5F, 0.15625F, 0, 1, 0, pPackedLight);
            this.vertex(matrix4f, matrix3f, vertexconsumer, entity.xMax, entity.yMax, entity.zMax, 0.5F, 0.15625F, 0, 1, 0, pPackedLight);
            this.vertex(matrix4f, matrix3f, vertexconsumer, entity.xMin, entity.yMax, entity.zMax, 0.0F, 0.15625F, 0, 1, 0, pPackedLight);

            // Left side
            this.vertex(matrix4f, matrix3f, vertexconsumer, entity.xMin, entity.yMax, entity.zMin, 0.0F, 0.15625F, 0, 1, 0, pPackedLight);
            this.vertex(matrix4f, matrix3f, vertexconsumer, entity.xMax, entity.yMax, entity.zMin, 0.5F, 0.15625F, 0, 1, 0, pPackedLight);
            this.vertex(matrix4f, matrix3f, vertexconsumer, entity.xMax, entity.yMin, entity.zMin, 0.5F, 0.15625F, 0, 1, 0, pPackedLight);
            this.vertex(matrix4f, matrix3f, vertexconsumer, entity.xMin, entity.yMin, entity.zMin, 0.0F, 0.15625F, 0, 1, 0, pPackedLight);

            // Top Side
            this.vertex(matrix4f, matrix3f, vertexconsumer, entity.xMin, entity.yMax, entity.zMin, 0.5F, 0.15625F, 0, 1, 0, pPackedLight);
            this.vertex(matrix4f, matrix3f, vertexconsumer, entity.xMin, entity.yMax, entity.zMax, 0.0F, 0.15625F, 0, 1, 0, pPackedLight);
            this.vertex(matrix4f, matrix3f, vertexconsumer, entity.xMax, entity.yMax, entity.zMax, 0.5F, 0.15625F, 0, 1, 0, pPackedLight);
            this.vertex(matrix4f, matrix3f, vertexconsumer, entity.xMax, entity.yMax, entity.zMin, 0.0F, 0.15625F, 0, 1, 0, pPackedLight);


            // Bottom Side
            this.vertex(matrix4f, matrix3f, vertexconsumer, entity.xMin, entity.yMin, entity.zMin, 0.5F, 0.15625F, 0, 1, 0, pPackedLight);
            this.vertex(matrix4f, matrix3f, vertexconsumer, entity.xMax, entity.yMin, entity.zMin, 0.0F, 0.15625F, 0, 1, 0, pPackedLight);
            this.vertex(matrix4f, matrix3f, vertexconsumer, entity.xMax, entity.yMin, entity.zMax, 0.5F, 0.15625F, 0, 1, 0, pPackedLight);
            this.vertex(matrix4f, matrix3f, vertexconsumer, entity.xMin, entity.yMin, entity.zMax, 0.0F, 0.15625F, 0, 1, 0, pPackedLight);
        }
        else {
//            if (entity.entityDirection.equals(Direction.NORTH) || entity.entityDirection.equals(Direction.SOUTH)) {
//                pPoseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
//            }
//            else if (entity.entityDirection.equals(Direction.UP) || entity.entityDirection.equals(Direction.DOWN)) {
//                pPoseStack.mulPose(Axis.ZP.rotationDegrees(90.0F));
//            }
        }

        // Front side
        this.vertex(matrix4f, matrix3f, vertexconsumer, entity.xMin, entity.yMax, entity.zMin, 0.5F, 0.15625F, 0, 4, 0, pPackedLight);
        this.vertex(matrix4f, matrix3f, vertexconsumer, entity.xMin, entity.yMin, entity.zMin, 0.0F, 0.15625F, 0, 4, 0, pPackedLight);
        this.vertex(matrix4f, matrix3f, vertexconsumer, entity.xMin, entity.yMin, entity.zMax, 0.5F, 0.15625F, 0, 4, 0, pPackedLight);
        this.vertex(matrix4f, matrix3f, vertexconsumer, entity.xMin, entity.yMax, entity.zMax, 0.0F, 0.15625F, 0, 4, 0, pPackedLight);

        // Back side
        this.vertex(matrix4f, matrix3f, vertexconsumer, entity.xMax, entity.yMax, entity.zMin, 0.5F, 0.15625F, 0, 1, 0, pPackedLight);
        this.vertex(matrix4f, matrix3f, vertexconsumer, entity.xMax, entity.yMax, entity.zMax, 0.0F, 0.15625F, 0, 1, 0, pPackedLight);
        this.vertex(matrix4f, matrix3f, vertexconsumer, entity.xMax, entity.yMin, entity.zMax, 0.5F, 0.15625F, 0, 1, 0, pPackedLight);
        this.vertex(matrix4f, matrix3f, vertexconsumer, entity.xMax, entity.yMin, entity.zMin, 0.0F, 0.15625F, 0, 1, 0, pPackedLight);


        //        this.vertex(matrix4f, matrix3f, vertexconsumer, -4, -1, 1, 0.0F, 0.15625F, 0, 1, 0, pPackedLight);
//        this.vertex(matrix4f, matrix3f, vertexconsumer, 4, -1, 1, 0.5F, 0.15625F, 0, 1, 0, pPackedLight);
//
//

//        pPoseStack.mulPose(Axis.YP.rotationDegrees(180.0F)); // YP = "Y-POSITIVE"
//        this.vertex(matrix4f, matrix3f, vertexconsumer, -4, -1, 0, 0.0F, 0.15625F, 0, 1, 0, pPackedLight);
//        this.vertex(matrix4f, matrix3f, vertexconsumer, 4, -1, 0, 0.5F, 0.15625F, 0, 1, 0, pPackedLight);
//        this.vertex(matrix4f, matrix3f, vertexconsumer, 4, 0, 0, 0.5F, 0.15625F, 0, 1, 0, pPackedLight);
//        this.vertex(matrix4f, matrix3f, vertexconsumer, -4, 0, 0, 0.0F, 0.15625F, 0, 1, 0, pPackedLight);

//        this.vertex(matrix4f, matrix3f, vertexconsumer, -4, -1, 1, 0.0F, 0.15625F, 0, 1, 0, pPackedLight);
//        this.vertex(matrix4f, matrix3f, vertexconsumer, 4, -1, 1, 0.5F, 0.15625F, 0, 1, 0, pPackedLight);
//        this.vertex(matrix4f, matrix3f, vertexconsumer, 4, 0, 1, 0.5F, 0.15625F, 0, 1, 0, pPackedLight);
//        this.vertex(matrix4f, matrix3f, vertexconsumer, -4, 0, 1, 0.0F, 0.15625F, 0, 1, 0, pPackedLight);
//        pPoseStack.mulPose(Axis.YP.rotationDegrees(180.0F)); // YP = "Y-POSITIVE"
//        this.vertex(matrix4f, matrix3f, vertexconsumer, -4, -1, 1, 0.0F, 0.15625F, 0, 1, 0, pPackedLight);
//        this.vertex(matrix4f, matrix3f, vertexconsumer, 4, -1, 1, 0.5F, 0.15625F, 0, 1, 0, pPackedLight);
//        this.vertex(matrix4f, matrix3f, vertexconsumer, 4, 0, 1, 0.5F, 0.15625F, 0, 1, 0, pPackedLight);
//        this.vertex(matrix4f, matrix3f, vertexconsumer, -4, 0, 1, 0.0F, 0.15625F, 0, 1, 0, pPackedLight);
//
//
//        this.vertex(matrix4f, matrix3f, vertexconsumer, -4, 0, 0, 0.0F, 0.0F, 0, 1, 0, pPackedLight);
//        this.vertex(matrix4f, matrix3f, vertexconsumer, -4, 0, 1, 0.0F, 0.15625F, 0, 1, 0, pPackedLight);
//        this.vertex(matrix4f, matrix3f, vertexconsumer, 4, 0, 1, 0.5F, 0.15625F, 0, 1, 0, pPackedLight);
//        this.vertex(matrix4f, matrix3f, vertexconsumer, 4, 0, 0, 0.5F, 0.0F, 0, 1, 0, pPackedLight);
//        pPoseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
//        this.vertex(matrix4f, matrix3f, vertexconsumer, -4, 0, 0, 0.0F, 0.0F, 0, 1, 0, pPackedLight);
//        this.vertex(matrix4f, matrix3f, vertexconsumer, -4, 0, 1, 0.0F, 0.15625F, 0, 1, 0, pPackedLight);
//        this.vertex(matrix4f, matrix3f, vertexconsumer, 4, 0, 1, 0.5F, 0.15625F, 0, 1, 0, pPackedLight);
//        this.vertex(matrix4f, matrix3f, vertexconsumer, 4, 0, 0, 0.5F, 0.0F, 0, 1, 0, pPackedLight);
//
//        this.vertex(matrix4f, matrix3f, vertexconsumer, -4, -1, 0, 0.0F, 0.0F, 0, 1, 0, pPackedLight);
//        this.vertex(matrix4f, matrix3f, vertexconsumer, -4, -1, 1, 0.0F, 0.15625F, 0, 1, 0, pPackedLight);
//        this.vertex(matrix4f, matrix3f, vertexconsumer, 4, -1, 1, 0.5F, 0.15625F, 0, 1, 0, pPackedLight);
//        this.vertex(matrix4f, matrix3f, vertexconsumer, 4, -1, 0, 0.5F, 0.0F, 0, 1, 0, pPackedLight);
//        pPoseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
//        this.vertex(matrix4f, matrix3f, vertexconsumer, -4, -1, 0, 0.0F, 0.0F, 0, 1, 0, pPackedLight);
//        this.vertex(matrix4f, matrix3f, vertexconsumer, -4, -1, 1, 0.0F, 0.15625F, 0, 1, 0, pPackedLight);
//        this.vertex(matrix4f, matrix3f, vertexconsumer, 4, -1, 1, 0.5F, 0.15625F, 0, 1, 0, pPackedLight);
//        this.vertex(matrix4f, matrix3f, vertexconsumer, 4, -1, 0, 0.5F, 0.0F, 0, 1, 0, pPackedLight);

        // TODO: We will need ~ 12 to make a rectangle. Two for each surface.
//        for(int j = 0; j < 4; ++j) {
//            pPoseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
//            this.vertex(matrix4f, matrix3f, vertexconsumer, -4, -1, 0, 0.0F, 0.0F, 0, 1, 0, pPackedLight);
//            this.vertex(matrix4f, matrix3f, vertexconsumer, 4, -1, 0, 0.5F, 0.0F, 0, 1, 0, pPackedLight);
//            this.vertex(matrix4f, matrix3f, vertexconsumer, 4, 1, 0, 0.5F, 0.15625F, 0, 1, 0, pPackedLight);
//            this.vertex(matrix4f, matrix3f, vertexconsumer, -4, 1, 0, 0.0F, 0.15625F, 0, 1, 0, pPackedLight);
//        }

        pPoseStack.popPose();
//        dispatcher.render(entity);
        super.render(entity, pEntityYaw, pPartialTicks, pPoseStack, pBuffer, pPackedLight);
    }

    public void vertex(Matrix4f pMatrix, Matrix3f pNormal, VertexConsumer pConsumer, float pX, float pY, float pZ, float pU, float pV, int pNormalX, int pNormalZ, int pNormalY, int pPackedLight) {
        pConsumer
                .vertex(pMatrix, pX, pY, pZ)
                .color(255, 255, 255, 255)
                .uv(pU, pV)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(pPackedLight)
                .normal(pNormal, (float)pNormalX, (float)pNormalY, (float)pNormalZ)
                .endVertex();
    }

    @Override
    public ResourceLocation getTextureLocation(BloodSprayEntity bloodSprayEntity) {
//        BloodyBitsMod.LOGGER.info("GETTING TEXTURE LOCATION. IN GROUND: {}", bloodSprayEntity.isInGround());
//        if (bloodSprayEntity.isInGround() && bloodSprayEntity.entityDirection.equals(Direction.EAST)) {
//            return BLOOD_SPLATTER_1;
//        }
//        else {
            return BLOOD_SPRAY;
//        }
    }
}
