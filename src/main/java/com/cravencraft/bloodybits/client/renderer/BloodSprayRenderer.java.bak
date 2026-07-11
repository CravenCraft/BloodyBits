package com.cravencraft.bloodybits.client.renderer;

import com.cravencraft.bloodybits.BloodyBitsMod;
import com.cravencraft.bloodybits.config.CommonConfig;
import com.cravencraft.bloodybits.entity.BloodSprayEntity;
import com.cravencraft.bloodybits.utils.BloodyBitsUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

/**
 * This class will render the BloodSprayEntity on the client. It will use values defined within the BloodSprayEntity
 * (xMin, xMax, etc.) to render a rectangle dynamically that will act as a blood chunk.
 */
public class BloodSprayRenderer extends EntityRenderer<BloodSprayEntity, BloodSprayRenderState> {
    public static final Identifier SPRAY = net.minecraft.resources.Identifier.fromNamespaceAndPath(BloodyBitsMod.MODID, "textures/entity/blood_projectile/spray.png");

    public BloodSprayRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public BloodSprayRenderState createRenderState() {
        return new BloodSprayRenderState();
    }

    @Override
    public void extractRenderState(BloodSprayEntity entity, BloodSprayRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.entityDirection = entity.entityDirection;
        state.yRotO = entity.yRotO;
        state.yRot = entity.getYRot();
        state.xRotO = entity.xRotO;
        state.xRot = entity.getXRot();
        state.life = entity.getLife();
        state.xMin = entity.xMin;
        state.xMax = entity.xMax;
        state.yMin = entity.yMin;
        state.yMax = entity.yMax;
        state.zMin = entity.zMin;
        state.zMax = entity.zMax;
        state.red = entity.red;
        state.green = entity.green;
        state.blue = entity.blue;
        state.drip = entity.drip;
        
        if (!entity.isSolid && entity.isInGround()) {
            state.textureLocation = this.getRandomSpatterTexture(entity.randomTextureNumber);
        } else {
            state.textureLocation = SPRAY;
        }
    }

    @Override
    public void submit(BloodSprayRenderState state, PoseStack pPoseStack, net.minecraft.client.renderer.SubmitNodeCollector pBuffer, net.minecraft.client.renderer.state.CameraRenderState cameraRenderState) {
        int correctedPackedLight = 15728880;
        pPoseStack.pushPose();

        if (state.entityDirection == null) {
            pPoseStack.mulPose(Axis.YP.rotationDegrees(state.yRot - 90.0F));
            pPoseStack.mulPose(Axis.ZP.rotationDegrees(state.xRot));
        }
        else if (state.entityDirection.equals(Direction.NORTH) || state.entityDirection.equals(Direction.SOUTH)) {
            pPoseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
        }
        else if (state.entityDirection.equals(Direction.UP) || state.entityDirection.equals(Direction.DOWN)) {
            pPoseStack.mulPose(Axis.ZP.rotationDegrees(90.0F));
        }

        int alpha = (int) (225 - (((double) state.life / CommonConfig.despawnTime()) * 225));
        alpha = Math.max(0, alpha);
        
        pPoseStack.scale(0.05625F, 0.05625F, 0.05625F);

        int finalAlpha = alpha;
        pBuffer.order(0).submitCustomGeometry(pPoseStack, RenderTypes.entityTranslucent(state.textureLocation), (posestack$pose, vertexConsumer) -> {
            if (state.entityDirection == null || !state.entityDirection.equals(Direction.DOWN)) {
                // Bottom Side
                BloodyBitsUtils.vertex(posestack$pose, vertexConsumer, state.xMin, state.yMin, state.zMin, 0.5F, 0.0F,1, 1, 1, correctedPackedLight, state.red, state.green, state.blue, finalAlpha);
                BloodyBitsUtils.vertex(posestack$pose, vertexConsumer, state.xMax, state.yMin, state.zMin, 0.0F, 0.0F,1, 1, 1, correctedPackedLight, state.red, state.green, state.blue, finalAlpha);
                BloodyBitsUtils.vertex(posestack$pose, vertexConsumer, state.xMax, state.yMin, state.zMax, 0.0F, 0.125F,1, 1, 1, correctedPackedLight, state.red, state.green, state.blue, finalAlpha);
                BloodyBitsUtils.vertex(posestack$pose, vertexConsumer, state.xMin, state.yMin, state.zMax, 0.5F, 0.125F,1, 1, 1, correctedPackedLight, state.red, state.green, state.blue, finalAlpha);

                // Front side
                BloodyBitsUtils.vertex(posestack$pose, vertexConsumer, state.xMin, state.yMax, state.zMin, 0.375F, 0.0F,1, 1, 1, correctedPackedLight, state.red, state.green, state.blue, finalAlpha);
                BloodyBitsUtils.vertex(posestack$pose, vertexConsumer, state.xMin, state.yMin, state.zMin, 0.375F, 0.125F,1, 1, 1, correctedPackedLight, state.red, state.green, state.blue, finalAlpha);
                BloodyBitsUtils.vertex(posestack$pose, vertexConsumer, state.xMin, state.yMin, state.zMax, 0.5F, 0.125F,1, 1, 1, correctedPackedLight, state.red, state.green, state.blue, finalAlpha);
                BloodyBitsUtils.vertex(posestack$pose, vertexConsumer, state.xMin, state.yMax, state.zMax, 0.5F, 0.125F,1, 1, 1, correctedPackedLight, state.red, state.green, state.blue, finalAlpha);

                // Back side
                BloodyBitsUtils.vertex(posestack$pose, vertexConsumer, state.xMax, state.yMax, state.zMin, 0.0F, 0.0F,1, 1, 1, correctedPackedLight, state.red, state.green, state.blue, finalAlpha);
                BloodyBitsUtils.vertex(posestack$pose, vertexConsumer, state.xMax, state.yMax, state.zMax, 0.125F, 0.0F,1, 1, 1, correctedPackedLight, state.red, state.green, state.blue, finalAlpha);
                BloodyBitsUtils.vertex(posestack$pose, vertexConsumer, state.xMax, state.yMin, state.zMax, 0.125F, 0.125F,1, 1, 1, correctedPackedLight, state.red, state.green, state.blue, finalAlpha);
                BloodyBitsUtils.vertex(posestack$pose, vertexConsumer, state.xMax, state.yMin, state.zMin, 0.0F, 0.125F,1, 1, 1, correctedPackedLight, state.red, state.green, state.blue, finalAlpha);
            } else {
                // Front side
                BloodyBitsUtils.vertex(posestack$pose, vertexConsumer, state.xMin, state.yMax, state.zMin, 0.0F, 0.0F,1, 1, 1, correctedPackedLight, state.red, state.green, state.blue, finalAlpha);
                BloodyBitsUtils.vertex(posestack$pose, vertexConsumer, state.xMin, state.yMin, state.zMin, 0.0F, 1.0F,1, 1, 1, correctedPackedLight, state.red, state.green, state.blue, finalAlpha);
                BloodyBitsUtils.vertex(posestack$pose, vertexConsumer, state.xMin, state.yMin, state.zMax, 1.0F, 1.0F,1, 1, 1, correctedPackedLight, state.red, state.green, state.blue, finalAlpha);
                BloodyBitsUtils.vertex(posestack$pose, vertexConsumer, state.xMin, state.yMax, state.zMax, 1.0F, 0.0F,1, 1, 1, correctedPackedLight, state.red, state.green, state.blue, finalAlpha);
            }
        });

        if (state.life > 50 && state.entityDirection != null && state.entityDirection.equals(Direction.DOWN)) {
            pBuffer.order(0).submitCustomGeometry(pPoseStack, RenderTypes.entityTranslucent(SPRAY), (posestack$pose, dripVertexConsumer) -> {
                float zPos = (Math.abs(state.zMax - state.zMin) / 2) + state.zMin;
                float yPos = (Math.abs(state.yMax - state.yMin) / 2) + state.yMin;
                float thickness = (state.drip * 0.01F);
                int dripAlpha = (int) (255 - (255 * (state.drip / BloodSprayEntity.MAX_DRIP_LENGTH)));

                // Z-axis view
                BloodyBitsUtils.vertex(posestack$pose, dripVertexConsumer, state.xMax, yPos, zPos - 0.5F + thickness, 0.5F, 0.0F,1, 1, 1, correctedPackedLight, state.red, state.green, state.blue, dripAlpha);
                BloodyBitsUtils.vertex(posestack$pose, dripVertexConsumer, -state.drip, yPos, zPos - 0.5F + thickness, 0.0F, 0.0F,1, 1, 1, correctedPackedLight, state.red, state.green, state.blue, dripAlpha);
                BloodyBitsUtils.vertex(posestack$pose, dripVertexConsumer, -state.drip, yPos, zPos + 0.5F - thickness, 0.0F, 0.125F,1, 1, 1, correctedPackedLight, state.red, state.green, state.blue, dripAlpha);
                BloodyBitsUtils.vertex(posestack$pose, dripVertexConsumer, state.xMax, yPos, zPos + 0.5F - thickness, 0.5F, 0.125F,1, 1, 1, correctedPackedLight, state.red, state.green, state.blue, dripAlpha);

                // Y-axis view
                BloodyBitsUtils.vertex(posestack$pose, dripVertexConsumer, state.xMax, yPos - 0.5F + thickness, zPos, 0.5F, 0.0F,1, 1, 1, correctedPackedLight, state.red, state.green, state.blue, dripAlpha);
                BloodyBitsUtils.vertex(posestack$pose, dripVertexConsumer, -state.drip, yPos - 0.5F + thickness, zPos, 0.0F, 0.0F,1, 1, 1, correctedPackedLight, state.red, state.green, state.blue, dripAlpha);
                BloodyBitsUtils.vertex(posestack$pose, dripVertexConsumer, -state.drip, yPos + 0.5F - thickness, zPos, 0.0F, 0.125F,1, 1, 1, correctedPackedLight, state.red, state.green, state.blue, dripAlpha);
                BloodyBitsUtils.vertex(posestack$pose, dripVertexConsumer, state.xMax, yPos + 0.5F - thickness, zPos, 0.5F, 0.125F,1, 1, 1, correctedPackedLight, state.red, state.green, state.blue, dripAlpha);
            });
        }

        pPoseStack.popPose();
    }

    private Identifier getRandomSpatterTexture(int randomInt) {
        return switch (randomInt) {
            case 1 -> Identifier.fromNamespaceAndPath(BloodyBitsMod.MODID, "textures/entity/blood_spatter/spatter_1.png");
            case 2 -> Identifier.fromNamespaceAndPath(BloodyBitsMod.MODID, "textures/entity/blood_spatter/spatter_2.png");
            case 3 -> Identifier.fromNamespaceAndPath(BloodyBitsMod.MODID, "textures/entity/blood_spatter/spatter_3.png");
            case 4 -> Identifier.fromNamespaceAndPath(BloodyBitsMod.MODID, "textures/entity/blood_spatter/spatter_4.png");
            case 5 -> Identifier.fromNamespaceAndPath(BloodyBitsMod.MODID, "textures/entity/blood_spatter/spatter_5.png");
            case 6 -> Identifier.fromNamespaceAndPath(BloodyBitsMod.MODID, "textures/entity/blood_spatter/spatter_6.png");
            case 7 -> Identifier.fromNamespaceAndPath(BloodyBitsMod.MODID, "textures/entity/blood_spatter/spatter_7.png");
            default -> Identifier.fromNamespaceAndPath(BloodyBitsMod.MODID, "textures/entity/blood_spatter/spatter_0.png");
        };
    }
}
