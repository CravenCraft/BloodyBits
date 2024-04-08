package com.cravencraft.bloodybits.client.renderer;

import com.cravencraft.bloodybits.BloodyBitsMod;
import com.cravencraft.bloodybits.config.CommonConfig;
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
    public static final ResourceLocation SPRAY = new ResourceLocation(BloodyBitsMod.MODID, "textures/entity/spray.png");

    public BloodSprayRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(BloodSprayEntity entity, float pEntityYaw, float pPartialTicks, PoseStack pPoseStack, @NotNull MultiBufferSource pBuffer, int pPackedLight) {
        // Stops the blood from rendering as black when in a dark location such as a ceiling.
        int correctedPackedLight = (pPackedLight < 10485776) ? 10485776 : pPackedLight;
        pPoseStack.pushPose();
        // todo: I know this is dumb. I'll reverse it after testing.
        //TODO: For now this is the most accurate one. Maybe can play around with other methods later that
        //      Don't cause that small clipping through a block that only I will really notice.
        //TODO: Can probably put most of the below code in these blocks.
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

//        pPoseStack.mulPose(Axis.XP.rotationDegrees(45.0F));
        pPoseStack.scale(0.05625F, 0.05625F, 0.05625F);
        // TODO: Make if statement here to swap between the textures when the entity is in a specific state.
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
            this.vertex(matrix4f, matrix3f, vertexConsumer, entity.xMin, entity.yMin, entity.zMax, 0.5F, 0.0F, 0, 1, 0, correctedPackedLight, entity.currentLifeTime, entity.red, entity.green, entity.blue);
            this.vertex(matrix4f, matrix3f, vertexConsumer, entity.xMax, entity.yMin, entity.zMax, 0.0F, 0.0F, 0, 1, 0, correctedPackedLight, entity.currentLifeTime, entity.red, entity.green, entity.blue);
            this.vertex(matrix4f, matrix3f, vertexConsumer, entity.xMax, entity.yMax, entity.zMax, 0.0F, 0.125F, 0, 1, 0, correctedPackedLight, entity.currentLifeTime, entity.red, entity.green, entity.blue);
            this.vertex(matrix4f, matrix3f, vertexConsumer, entity.xMin, entity.yMax, entity.zMax, 0.5F, 0.125F, 0, 1, 0, correctedPackedLight, entity.currentLifeTime, entity.red, entity.green, entity.blue);

            // Left side
            this.vertex(matrix4f, matrix3f, vertexConsumer, entity.xMin, entity.yMax, entity.zMin, 0.5F, 0.0F, 0, 1, 0, correctedPackedLight, entity.currentLifeTime, entity.red, entity.green, entity.blue);
            this.vertex(matrix4f, matrix3f, vertexConsumer, entity.xMax, entity.yMax, entity.zMin, 0.0F, 0.0F, 0, 1, 0, correctedPackedLight, entity.currentLifeTime, entity.red, entity.green, entity.blue);
            this.vertex(matrix4f, matrix3f, vertexConsumer, entity.xMax, entity.yMin, entity.zMin, 0.0F, 0.125F, 0, 1, 0, correctedPackedLight, entity.currentLifeTime, entity.red, entity.green, entity.blue);
            this.vertex(matrix4f, matrix3f, vertexConsumer, entity.xMin, entity.yMin, entity.zMin, 0.5F, 0.125F, 0, 1, 0, correctedPackedLight, entity.currentLifeTime, entity.red, entity.green, entity.blue);

            // Top Side
            this.vertex(matrix4f, matrix3f, vertexConsumer, entity.xMin, entity.yMax, entity.zMin, 0.5F, 0.0F, 0, 1, 0, correctedPackedLight, entity.currentLifeTime, entity.red, entity.green, entity.blue);
            this.vertex(matrix4f, matrix3f, vertexConsumer, entity.xMax, entity.yMax, entity.zMin, 0.0F, 0.0F, 0, 1, 0, correctedPackedLight, entity.currentLifeTime, entity.red, entity.green, entity.blue);
            this.vertex(matrix4f, matrix3f, vertexConsumer, entity.xMax, entity.yMax, entity.zMax, 0.0F, 0.125F, 0, 1, 0, correctedPackedLight, entity.currentLifeTime, entity.red, entity.green, entity.blue);
            this.vertex(matrix4f, matrix3f, vertexConsumer, entity.xMin, entity.yMax, entity.zMax, 0.5F, 0.125F, 0, 1, 0, correctedPackedLight, entity.currentLifeTime, entity.red, entity.green, entity.blue);

            // Bottom Side
            this.vertex(matrix4f, matrix3f, vertexConsumer, entity.xMin, entity.yMin, entity.zMin, 0.5F, 0.0F, 0, 1, 0, correctedPackedLight, entity.currentLifeTime, entity.red, entity.green, entity.blue);
            this.vertex(matrix4f, matrix3f, vertexConsumer, entity.xMax, entity.yMin, entity.zMin, 0.0F, 0.0F, 0, 1, 0, correctedPackedLight, entity.currentLifeTime, entity.red, entity.green, entity.blue);
            this.vertex(matrix4f, matrix3f, vertexConsumer, entity.xMax, entity.yMin, entity.zMax, 0.0F, 0.125F, 0, 1, 0, correctedPackedLight, entity.currentLifeTime, entity.red, entity.green, entity.blue);
            this.vertex(matrix4f, matrix3f, vertexConsumer, entity.xMin, entity.yMin, entity.zMax, 0.5F, 0.125F, 0, 1, 0, correctedPackedLight, entity.currentLifeTime, entity.red, entity.green, entity.blue);

            // Front side
            this.vertex(matrix4f, matrix3f, vertexConsumer, entity.xMin, entity.yMax, entity.zMin, 0.375F, 0.0F, 0, 1, 0, correctedPackedLight, entity.currentLifeTime, entity.red, entity.green, entity.blue);
            this.vertex(matrix4f, matrix3f, vertexConsumer, entity.xMin, entity.yMin, entity.zMin, 0.375F, 0.125F, 0, 1, 0, correctedPackedLight, entity.currentLifeTime, entity.red, entity.green, entity.blue);
            this.vertex(matrix4f, matrix3f, vertexConsumer, entity.xMin, entity.yMin, entity.zMax, 0.5F, 0.125F, 0, 1, 0, correctedPackedLight, entity.currentLifeTime, entity.red, entity.green, entity.blue);
            this.vertex(matrix4f, matrix3f, vertexConsumer, entity.xMin, entity.yMax, entity.zMax, 0.5F, 0.125F, 0, 1, 0, correctedPackedLight, entity.currentLifeTime, entity.red, entity.green, entity.blue);

            // Back side
            this.vertex(matrix4f, matrix3f, vertexConsumer, entity.xMax, entity.yMax, entity.zMin, 0.0F, 0.0F, 0, 1, 0, correctedPackedLight, entity.currentLifeTime, entity.red, entity.green, entity.blue);
            this.vertex(matrix4f, matrix3f, vertexConsumer, entity.xMax, entity.yMax, entity.zMax, 0.125F, 0.0F, 0, 1, 0, correctedPackedLight, entity.currentLifeTime, entity.red, entity.green, entity.blue);
            this.vertex(matrix4f, matrix3f, vertexConsumer, entity.xMax, entity.yMin, entity.zMax, 0.125F, 0.125F, 0, 1, 0, correctedPackedLight, entity.currentLifeTime, entity.red, entity.green, entity.blue);
            this.vertex(matrix4f, matrix3f, vertexConsumer, entity.xMax, entity.yMin, entity.zMin, 0.0F, 0.125F, 0, 1, 0, correctedPackedLight, entity.currentLifeTime, entity.red, entity.green, entity.blue);
        }
        else {

            // Front side
            this.vertex(matrix4f, matrix3f, vertexConsumer, entity.xMin, entity.yMax, entity.zMin, 0.0F, 0.0F, 0, 0, 0, correctedPackedLight, entity.currentLifeTime, entity.red, entity.green, entity.blue);
            this.vertex(matrix4f, matrix3f, vertexConsumer, entity.xMin, entity.yMin, entity.zMin, 0.0F, 1.0F, 0, 0, 0, correctedPackedLight, entity.currentLifeTime, entity.red, entity.green, entity.blue);
            this.vertex(matrix4f, matrix3f, vertexConsumer, entity.xMin, entity.yMin, entity.zMax, 1.0F, 1.0F, 0, 0, 0, correctedPackedLight, entity.currentLifeTime, entity.red, entity.green, entity.blue);
            this.vertex(matrix4f, matrix3f, vertexConsumer, entity.xMin, entity.yMax, entity.zMax, 1.0F, 0.0F, 0, 0, 0, correctedPackedLight, entity.currentLifeTime, entity.red, entity.green, entity.blue);

            if (entity.currentLifeTime > 50 && entity.entityDirection != null && entity.entityDirection.equals(Direction.DOWN)) {
                VertexConsumer dripVertexConsumer = pBuffer.getBuffer(RenderType.entityTranslucent(SPRAY));
                float zPos = (Math.abs(entity.zMax - entity.zMin) / 2) + entity.zMin; // Gets the center point to make the Z-axis.
                float yPos = (Math.abs(entity.yMax - entity.yMin) / 2) + entity.yMin;
                float thickness = (entity.drip * 0.01F);
                int dripLifeTime = (int) (entity.currentLifeTime + ((entity.drip / BloodSprayEntity.MAX_DRIP_LENGTH) * (CommonConfig.despawnTime() - entity.currentLifeTime)));

                /*
                 * Blood drip. Keeping it at just 2 drips so the player can see a drip at all angles without having to do too much crazy math
                 * to make an actual rectangle that would also add a performance cost. This is a good compromise.
                 */
                // Z-axis view
                this.vertex(matrix4f, matrix3f, dripVertexConsumer, entity.xMax, yPos, zPos - 0.5F + thickness, 0.5F, 0.0F, 0, 1, 0, correctedPackedLight, dripLifeTime, entity.red, entity.green, entity.blue);
                this.vertex(matrix4f, matrix3f, dripVertexConsumer, -entity.drip, yPos, zPos - 0.5F + thickness, 0.0F, 0.0F, 0, 1, 0, correctedPackedLight, dripLifeTime, entity.red, entity.green, entity.blue);
                this.vertex(matrix4f, matrix3f, dripVertexConsumer, -entity.drip, yPos, zPos + 0.5F - thickness, 0.0F, 0.125F, 0, 1, 0, correctedPackedLight, dripLifeTime, entity.red, entity.green, entity.blue);
                this.vertex(matrix4f, matrix3f, dripVertexConsumer, entity.xMax, yPos, zPos + 0.5F - thickness, 0.5F, 0.125F, 0, 1, 0, correctedPackedLight, dripLifeTime, entity.red, entity.green, entity.blue);

                // Y-axis view
                this.vertex(matrix4f, matrix3f, vertexConsumer, entity.xMax, yPos - 0.5F + thickness, zPos, 0.5F, 0.0F, 0, 1, 0, correctedPackedLight, dripLifeTime, entity.red, entity.green, entity.blue);
                this.vertex(matrix4f, matrix3f, vertexConsumer, -entity.drip, yPos - 0.5F + thickness, zPos, 0.0F, 0.0F, 0, 1, 0, correctedPackedLight, dripLifeTime, entity.red, entity.green, entity.blue);
                this.vertex(matrix4f, matrix3f, vertexConsumer, -entity.drip, yPos + 0.5F - thickness, zPos, 0.0F, 0.125F, 0, 1, 0, correctedPackedLight, dripLifeTime, entity.red, entity.green, entity.blue);
                this.vertex(matrix4f, matrix3f, vertexConsumer, entity.xMax, yPos + 0.5F - thickness, zPos, 0.5F, 0.125F, 0, 1, 0, correctedPackedLight, dripLifeTime, entity.red, entity.green, entity.blue);
            }

        }

        pPoseStack.popPose();
    }

    public void vertex(Matrix4f pMatrix, Matrix3f pNormal, VertexConsumer pConsumer, float pX, float pY, float pZ, float pU, float pV, int pNormalX, int pNormalZ, int pNormalY, int packedLight, int lifeTime, int red, int green, int blue) {
        int alpha = (int) (255 - (((double) lifeTime / CommonConfig.despawnTime()) * 255));
        pConsumer
                .vertex(pMatrix, pX, pY, pZ)
                .color(red, green, blue, alpha)
                .uv(pU, pV)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(pNormal, (float)pNormalX, (float)pNormalY, (float)pNormalZ)
                .endVertex();
    }

    @Override
    public ResourceLocation getTextureLocation(BloodSprayEntity bloodSprayEntity) {
        if (bloodSprayEntity.isInGround()) {
            return this.getRandomSpatterTexture(bloodSprayEntity.randomTextureNumber);
        }
        else {
            return SPRAY;
        }
    }

    private ResourceLocation getRandomSpatterTexture(int randomInt) {
        return switch (randomInt) {
            case 1 -> new ResourceLocation(BloodyBitsMod.MODID, "textures/entity/spatter_1.png");
            case 2 -> new ResourceLocation(BloodyBitsMod.MODID, "textures/entity/spatter_2.png");
            case 3 -> new ResourceLocation(BloodyBitsMod.MODID, "textures/entity/spatter_3.png");
            default -> new ResourceLocation(BloodyBitsMod.MODID, "textures/entity/spatter_0.png");
        };
    }
}
