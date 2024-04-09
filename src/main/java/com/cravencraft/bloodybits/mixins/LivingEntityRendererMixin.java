package com.cravencraft.bloodybits.mixins;

import com.cravencraft.bloodybits.config.ClientConfig;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity, M extends EntityModel<T>> extends EntityRenderer<T> {
    private LivingEntity entity;
    private MultiBufferSource buffer;
    private HashMap<UUID, List<ArrayList<Integer>>> patternMap = new HashMap<>();

    protected LivingEntityRendererMixin(EntityRendererProvider.Context pContext) {
        super(pContext);
    }

    @Inject(method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At("HEAD"))
    private void getEntityType(T pEntity, float pEntityYaw, float pPartialTicks, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, CallbackInfo ci) {
        this.entity = pEntity;
        this.buffer = pBuffer;
    }

    @Redirect(method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/EntityModel;renderToBuffer(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;IIFFFF)V"))
    private void renderEntitiesDifferently(EntityModel<net.minecraft.world.entity.Entity> instance, PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) throws IOException {
        if (!this.entity.isDeadOrDying() && this.entity.getHealth() < this.entity.getMaxHealth()) {
            int redDamage = 200;
            int greenDamage = 1;
            int blueDamage = 1;
            for (List<?> mobBloodType : ClientConfig.mobBloodTypes()) {
                if (mobBloodType.get(0).toString().contains(this.entity.getEncodeId())) {
                    String bloodColorHexVal = (String) mobBloodType.get(1);
                    redDamage = HexFormat.fromHexDigits(bloodColorHexVal, 1, 3);
                    greenDamage = HexFormat.fromHexDigits(bloodColorHexVal, 3, 5);
                    blueDamage = HexFormat.fromHexDigits(bloodColorHexVal.substring(5));
                    break;
                }
            }
            Color damageColor = new Color(blueDamage, greenDamage, redDamage, 255);

            NativeImage nativeImage = NativeImage.read(Minecraft.getInstance().getResourceManager().open(this.getTextureLocation((T) this.entity)));

            int pixelsToModifyPerHealthPoint = (int) (((nativeImage.getHeight() * nativeImage.getWidth()) / this.entity.getMaxHealth()) / 2);
            int currentPatterns = (int) Math.ceil((this.entity.getMaxHealth() - this.entity.getHealth())) * pixelsToModifyPerHealthPoint;

            if (!patternMap.containsKey(this.entity.getUUID())) {
                patternMap.put(this.entity.getUUID(), new ArrayList<>());
            }

            if (patternMap.get(this.entity.getUUID()).size() < currentPatterns) {
                int amountToAdd = currentPatterns - patternMap.get(this.entity.getUUID()).size();

                for (int i = 0; i < amountToAdd; i++){
                    List<ArrayList<Integer>> updatedPatternList = patternMap.get(this.entity.getUUID());

                    ArrayList<Integer> patternToAdd = new ArrayList<>(3);
//                    boolean isWidth = Math.random() < 0.5;
//                    patternToAdd.add(isWidth);
                    patternToAdd.add(new Random().nextInt(nativeImage.getWidth() - 1));
//                    patternToAdd.add(new Random().nextInt((20 - 5) + 5));
                    patternToAdd.add(new Random().nextInt(nativeImage.getHeight() - 1));
                    patternToAdd.add(new Random().ints(-1, 1).findFirst().getAsInt());
                    updatedPatternList.add(patternToAdd);
//                    BloodyBitsMod.LOGGER.info("ITERATION {} PATTERN TO ADD: {}", i, updatedPatternList);

                    patternMap.put(this.entity.getUUID(), updatedPatternList);
                }

//                BloodyBitsMod.LOGGER.info("PATTERN MAP SIZE {} INFO: {}", patternMap.size(), patternMap.get(this.entity.getUUID()));
            }

            for (ArrayList<Integer> currentPattern : patternMap.get(this.entity.getUUID())) {
//                boolean isWidth = (boolean) currentPattern.get(0);
                int randomChosenWidthStart = currentPattern.get(0);
                int randomChosenHeightStart = currentPattern.get(1);
                int randomColorHue = currentPattern.get(2);
//                int randomHeightLength = (int) currentPattern.get(4);
//                int iteratorStart;
//                int damagePixelLength;
//                int randomExpansionLength;
//                BloodyBitsMod.LOGGER.info("RANDOM CHOSEN WIDTH AND HEIGHT START: {} - {}", randomChosenWidthStart, randomChosenHeightStart);

                if (nativeImage.getPixelRGBA(randomChosenWidthStart, randomChosenHeightStart) != 0) {
//                    BloodyBitsMod.LOGGER.info("RANDOM HUE: {}", randomColorHue);
                    if (randomColorHue < 0) {
                        nativeImage.setPixelRGBA(randomChosenWidthStart, randomChosenHeightStart, damageColor.darker().getRGB());
                    }
                    else if (randomColorHue > 0) {
                        nativeImage.setPixelRGBA(randomChosenWidthStart, randomChosenHeightStart, damageColor.brighter().getRGB());
                    }
                    else {
                        nativeImage.setPixelRGBA(randomChosenWidthStart, randomChosenHeightStart, damageColor.getRGB());
                    }
                }
            }

            DynamicTexture dynamicTexture = new DynamicTexture(nativeImage);
            VertexConsumer vertexConsumer1 = this.buffer.getBuffer(RenderType.entityTranslucent(Minecraft.getInstance().getTextureManager().register("test", dynamicTexture)));

            instance.renderToBuffer(poseStack, vertexConsumer1, packedLight, packedOverlay, red, green, blue, alpha);
        }
        else {
            if (patternMap.containsKey(this.entity.getUUID())) {
                patternMap.remove(this.entity.getUUID());
            }
            instance.renderToBuffer(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        }

    }
}