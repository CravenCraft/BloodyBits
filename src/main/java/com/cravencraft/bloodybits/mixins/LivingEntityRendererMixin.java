package com.cravencraft.bloodybits.mixins;

import com.cravencraft.bloodybits.BloodyBitsMod;
import com.cravencraft.bloodybits.config.ClientConfig;
import com.cravencraft.bloodybits.config.CommonConfig;
import com.cravencraft.bloodybits.utils.BloodyBitsUtils;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.server.packs.resources.ResourceManager;
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
    private File file;
    private HashMap<UUID, List<ArrayList<Integer>>> patternMap = new HashMap<>();

    protected LivingEntityRendererMixin(EntityRendererProvider.Context pContext) {
        super(pContext);
    }

    /**
     * Simple injection to acquire the entity and buffer for use in the below method.
     */
    @Inject(method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At("HEAD"))
    private void getEntityType(T pEntity, float pEntityYaw, float pPartialTicks, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, CallbackInfo ci) {
        this.entity = pEntity;
        this.buffer = pBuffer;
    }

    /**
     * A monumental redirect method that I didn't think was possible. If the Client Config is set to show mob damage,
     * then this method will retrieve native image (texture) of an entity via its resource location.
     *
     * Custom blood colors are then mapped to the given entity via a client config (similar to how the blood spatters are made).
     *
     * The amount of pixels on the entity's texture to turn into blood textures (configurable) are determined by the
     * amount of health remaining for the entity.
     *
     * A pattern map is created for the particular entity via its unique UUID. Textures are selected at random to apply
     * the blood pattern to the map, then saved. As the damage to the entity grows, more will be added to the map, and
     * as the entity heals more will be taken away.
     *
     * The texture for the entity is then converted into a Dynamic Texture and applied.
     *
     * NOTE: This seems to work perfectly well for most vanilla and modded entities. There are issues with certain
     * modded entities such as MCDOOM enemies and Ice and Fire dragons. I will be looking into these to see how they
     * render the textures for their entities differently to try and add compatibility for most vanilla and modded mobs.
     *
     * EXTRA NOTE: The Epic Fight Mod also renders its entities differently than normal Minecraft, which allows for its
     * incredible animations. This is incompatible with this feature. So, for now it will be incompatible until I add in
     * optional support later down the line.
     */
    @Redirect(method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/EntityModel;renderToBuffer(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;IIFFFF)V"))
    private void renderEntitiesDifferently(EntityModel<net.minecraft.world.entity.Entity> instance, PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) throws IOException {
        VertexConsumer customVertexConsumer = vertexConsumer;

        if (ClientConfig.showMobDamage() && !this.entity.isDeadOrDying() && this.entity.getHealth() < this.entity.getMaxHealth()) {
            try {
                String entityName;
                NativeImage nativeImage;
                ResourceManager resourceManager = Minecraft.getInstance().getResourceManager();

                //TODO: Will this work when looking at other players?
                if (this.entity instanceof LocalPlayer localPlayer && localPlayer.isSkinLoaded()) {
                    AbstractTexture abstractTexture = Minecraft.getInstance().getTextureManager().getTexture(localPlayer.getSkinTextureLocation());
                    if (abstractTexture instanceof HttpTexture) {
                        //TODO: Need to add a check for if the player hashcode for some reason doesn't match up.
                        nativeImage = BloodyBitsUtils.PLAYER_SKINS.get(localPlayer.getName().hashCode());
                    }
                    else {
                        nativeImage = NativeImage.read(resourceManager.open(localPlayer.getSkinTextureLocation()));

                    }
                    entityName = "player";
                }
                else {
                    nativeImage = NativeImage.read(resourceManager.open(this.getTextureLocation((T) this.entity)));
                    entityName = this.entity.getEncodeId();
                }

                int redDamage = 200;
                int greenDamage = 1;
                int blueDamage = 1;
                int alphaDamage = 255;

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

//                    BloodyBitsMod.LOGGER.info("BEFORE GET PIXEL RGBA");
                    if (nativeImage.getPixelRGBA(randomChosenWidthStart, randomChosenHeightStart) != 0) {
//                        if (CommonConfig.gasEntities().contains(entityName)) {
//                            Color originalTempHolder = new Color(nativeImage.getPixelRGBA(randomChosenWidthStart, randomChosenHeightStart), true);
//                            Color gasDamage = new Color(originalTempHolder.getBlue(), originalTempHolder.getGreen(), originalTempHolder.getRed(), 200);
//                            nativeImage.setPixelRGBA(randomChosenWidthStart, randomChosenHeightStart, gasDamage.getRGB());
//                        }
//                        BloodyBitsMod.LOGGER.info("BEFORE SET PIXEL RGBA");
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


//                BloodyBitsMod.LOGGER.info("BEFORE DYNAMIC TEXTURE INSTANTIATION");
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