package com.cravencraft.bloodybits.mixins;

import com.cravencraft.bloodybits.BloodyBitsMod;
import com.cravencraft.bloodybits.config.ClientConfig;
import com.cravencraft.bloodybits.config.CommonConfig;
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
        VertexConsumer customVertexConsumer = vertexConsumer;
        //TODO: Add a client config option to make this optional (will probably save a lot of memory on lower end machines).
        //      Maybe add some options for how much of the entity is bloodied as well.
        if (!this.entity.isDeadOrDying() && this.entity.getHealth() < this.entity.getMaxHealth()) {
            try {
                InputStream fileInput = Minecraft.getInstance().getResourceManager().open(this.getTextureLocation((T) this.entity));
                NativeImage nativeImage = NativeImage.read(fileInput);

                int redDamage = 200;
                int greenDamage = 1;
                int blueDamage = 1;
                int alphaDamage = 255;
                String entityName = (this.entity.toString().contains("Player")) ? "player" : this.entity.getEncodeId();

                if (CommonConfig.noBloodMobs().contains(entityName)) {

                    redDamage = 0;
                    greenDamage = 0;
                    blueDamage = 0;
                    alphaDamage = 0;
                }
                else {
                    for (List<?> mobBloodType : ClientConfig.mobBloodTypes()) {
                        if (mobBloodType.get(0).toString().contains(Objects.requireNonNull(entityName))) {
                            String bloodColorHexVal = (String) mobBloodType.get(1);
                            redDamage = HexFormat.fromHexDigits(bloodColorHexVal, 1, 3);
                            greenDamage = HexFormat.fromHexDigits(bloodColorHexVal, 3, 5);
                            blueDamage = HexFormat.fromHexDigits(bloodColorHexVal.substring(5));
                            break;
                        }
                    }
                }

                Color damageColor = new Color(blueDamage, greenDamage, redDamage, alphaDamage);

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
                        if (randomColorHue < 0 && !CommonConfig.noBloodMobs().contains(entityName)) {
                            nativeImage.setPixelRGBA(randomChosenWidthStart, randomChosenHeightStart, damageColor.darker().getRGB());
                        }
                        else if (randomColorHue > 0 && !CommonConfig.noBloodMobs().contains(entityName)) {
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
            catch (FileNotFoundException e) {
                BloodyBitsMod.LOGGER.error("ERROR: File for {} not found at resource location {}!", this.entity, this.getTextureLocation((T) this.entity));
            }
        }
        else {
            patternMap.remove(this.entity.getUUID());
        }
        instance.renderToBuffer(poseStack, customVertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}