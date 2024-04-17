package com.cravencraft.bloodybits.mixins;

import com.cravencraft.bloodybits.BloodyBitsMod;
import com.cravencraft.bloodybits.config.ClientConfig;
import com.cravencraft.bloodybits.config.CommonConfig;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EnderDragonRenderer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import org.checkerframework.checker.units.qual.A;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.List;

@Mixin(EnderDragonRenderer.class)
public class EnderDragonRendererMixin extends EntityRenderer<EnderDragon> {
    @Shadow @Final private static ResourceLocation DRAGON_LOCATION;
    private HashMap<UUID, List<ArrayList<Integer>>> patternMap = new HashMap<>();
    private EnderDragon entity;
    private MultiBufferSource buffer;

    protected EnderDragonRendererMixin(EntityRendererProvider.Context pContext) {
        super(pContext);
    }

    @Override
    public ResourceLocation getTextureLocation(EnderDragon entity) {
        return DRAGON_LOCATION;
    }

    /**
     * Simple injection to acquire the entity and buffer for use in the below method.
     */
    @Inject(method = "render(Lnet/minecraft/world/entity/boss/enderdragon/EnderDragon;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At("HEAD"))
    private void getEntityType(EnderDragon pEntity, float pEntityYaw, float pPartialTicks, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, CallbackInfo ci) {
        this.entity = pEntity;
        this.buffer = pBuffer;
    }

    /**
     * See LivingEntityRendererMixin. This method is basically a copy/paste to ensure the effects happen to the
     * Ender Dragon as well since it is the child of LivingEntity's super class, unfortunately.
     */
    @Redirect(method = "render(Lnet/minecraft/world/entity/boss/enderdragon/EnderDragon;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/EnderDragonRenderer$DragonModel;renderToBuffer(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;IIFFFF)V"))
    private void replaceWithDamageTextures(EnderDragonRenderer.DragonModel dragonModel, PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        VertexConsumer customVertexConsumer = buffer;

        if (ClientConfig.showMobDamage() && !this.entity.isDeadOrDying() && this.entity.getHealth() < this.entity.getMaxHealth()) {
            try {
                InputStream fileInput = Minecraft.getInstance().getResourceManager().open(this.getTextureLocation(this.entity));
                NativeImage nativeImage = NativeImage.read(fileInput);

                int redDamage = 200;
                int greenDamage = 1;
                int blueDamage = 1;
                int alphaDamage = 255;
                String entityName = (this.entity.toString().contains("Player")) ? "player" : this.entity.getEncodeId();

                if (CommonConfig.solidEntities().contains(entityName)) {
                    redDamage = 0;
                    greenDamage = 0;
                    blueDamage = 0;
                    alphaDamage = 0;
                }
                else {
                    for (Map.Entry<String, List<String>> mapElement : ClientConfig.mobBloodColors().entrySet()) {
                        if (mapElement.getValue().contains(Objects.requireNonNull(entityName))) {
                            String bloodColorHexVal = mapElement.getKey();
                            redDamage = HexFormat.fromHexDigits(bloodColorHexVal, 1, 3);
                            greenDamage = HexFormat.fromHexDigits(bloodColorHexVal, 3, 5);
                            blueDamage = HexFormat.fromHexDigits(bloodColorHexVal.substring(5));
                            break;
                        }
                    }
                }

                Color damageColor = new Color(blueDamage, greenDamage, redDamage, alphaDamage);

                int pixelsToModifyPerHealthPoint = (int) (((nativeImage.getHeight() * nativeImage.getWidth()) / this.entity.getMaxHealth()) * (ClientConfig.entityDamageShownPercent() * 0.01));
                int currentPatterns = (int) Math.ceil((this.entity.getMaxHealth() - this.entity.getHealth())) * pixelsToModifyPerHealthPoint;

                if (!patternMap.containsKey(this.entity.getUUID())) {
                    patternMap.put(this.entity.getUUID(), new ArrayList<>());
                }

                if (patternMap.get(this.entity.getUUID()).size() < currentPatterns) {
                    int amountToAdd = currentPatterns - patternMap.get(this.entity.getUUID()).size();

                    for (int i = 0; i < amountToAdd; i++){
                        List<ArrayList<Integer>> updatedPatternList = patternMap.get(this.entity.getUUID());

                        ArrayList<Integer> patternToAdd = new ArrayList<>(3);
                        patternToAdd.add(new Random().nextInt(nativeImage.getWidth() - 1));
                        patternToAdd.add(new Random().nextInt(nativeImage.getHeight() - 1));
                        patternToAdd.add(new Random().ints(-1, 1).findFirst().getAsInt());
                        updatedPatternList.add(patternToAdd);

                        patternMap.put(this.entity.getUUID(), updatedPatternList);
                    }
                }
                else {
                    for (int i = patternMap.get(this.entity.getUUID()).size() - 1; i > currentPatterns; i--) {
                        patternMap.get(this.entity.getUUID()).remove(i);
                    }
                }

                for (ArrayList<Integer> currentPattern : patternMap.get(this.entity.getUUID())) {
                    int randomChosenWidthStart = currentPattern.get(0);
                    int randomChosenHeightStart = currentPattern.get(1);
                    int randomColorHue = currentPattern.get(2);

                    if (nativeImage.getPixelRGBA(randomChosenWidthStart, randomChosenHeightStart) != 0) {
//                        if (CommonConfig.gasEntities().contains(entityName)) {
//                            Color originalTempHolder = new Color(nativeImage.getPixelRGBA(randomChosenWidthStart, randomChosenHeightStart), true);
//                            Color gasDamage = new Color(originalTempHolder.getBlue(), originalTempHolder.getGreen(), originalTempHolder.getRed(), 200);
//                            nativeImage.setPixelRGBA(randomChosenWidthStart, randomChosenHeightStart, gasDamage.getRGB());
//                        }
                        if (randomColorHue < 0 && !CommonConfig.solidEntities().contains(entityName)) {
                            nativeImage.setPixelRGBA(randomChosenWidthStart, randomChosenHeightStart, damageColor.darker().getRGB());
                        }
                        else if (randomColorHue > 0 && !CommonConfig.solidEntities().contains(entityName)) {
                            nativeImage.setPixelRGBA(randomChosenWidthStart, randomChosenHeightStart, damageColor.brighter().getRGB());
                        }
                        else {
                            nativeImage.setPixelRGBA(randomChosenWidthStart, randomChosenHeightStart, damageColor.getRGB());
                        }
                    }
                }

                DynamicTexture dynamicTexture = new DynamicTexture(nativeImage);
                customVertexConsumer = this.buffer.getBuffer(RenderType.entityTranslucent(Minecraft.getInstance().getTextureManager().register("test", dynamicTexture)));
            }
            catch (IOException e) {
                BloodyBitsMod.LOGGER.error("ERROR: File for {} not found at resource location {}!", this.entity, this.getTextureLocation(this.entity));
            }
//            catch (IOException e) {
//                throw new RuntimeException(e);
//            }
        }
        else {
            patternMap.remove(this.entity.getUUID());
        }
        dragonModel.renderToBuffer(poseStack, customVertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
