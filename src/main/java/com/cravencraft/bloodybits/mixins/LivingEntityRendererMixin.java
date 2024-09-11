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
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;
import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity, M extends EntityModel<T>> extends EntityRenderer<T> {
    @Shadow public abstract boolean addLayer(RenderLayer<T, M> p_115327_);

    @Shadow protected M model;

    @Shadow protected abstract boolean isBodyVisible(T pLivingEntity);

    @Shadow @Nullable protected abstract RenderType getRenderType(T pLivingEntity, boolean pBodyVisible, boolean pTranslucent, boolean pGlowing);

    @Shadow protected abstract float getWhiteOverlayProgress(T pLivingEntity, float pPartialTicks);

    @Shadow protected abstract void scale(T pLivingEntity, PoseStack pPoseStack, float pPartialTickTime);

    private boolean hasErrorMessageDisplayed;
    private LivingEntity entity;
    private MultiBufferSource buffer;
    private HashMap<UUID, List<ArrayList<Integer>>> pixelDamageMap = new HashMap<>();
    private HashMap<UUID, NativeImage> entityTextureDamageMap = new HashMap<>();
    private HashMap<UUID, List<List<ArrayList<Integer>>>> entityInjuriesMap = new HashMap<>();

    protected LivingEntityRendererMixin(EntityRendererProvider.Context pContext) {
        super(pContext);
    }

//    @Inject(at = @At("TAIL"), method = "<init>", remap = false)
//    private void injectBloodRenderLayer(EntityRendererProvider.Context pContext, EntityModel pModel, float pShadowRadius, CallbackInfo ci) {
//        this.addLayer(new BloodyEntityLayer<>(pContext, (LivingEntityRenderer) (Object) this));
//        BloodyBitsMod.LOGGER.info("NEW BLOOD ENTITY LAYER HAS BEEN CREATED.");
//    }

    /**
     * Simple injection to acquire the entity and buffer for use in the below method.
     */
    @Inject(method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At("HEAD"))
    private void getEntityType(T pEntity, float pEntityYaw, float pPartialTicks, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, CallbackInfo ci) {
        this.entity = pEntity;
        this.buffer = pBuffer;
    }

    /**
     * TODO: Update this method javadoc.
     *
     * A monumental redirect method that I didn't think was possible. If the Client Config is set to show mob damage,
     * then this method will retrieve native image (texture) of an entity via its resource location.
     * <p>
     * Custom blood colors are then mapped to the given entity via a client config (similar to how the blood spatters are made).
     * <p>
     * The amount of pixels on the entity's texture to turn into blood textures (configurable) are determined by the
     * amount of health remaining for the entity.
     * <p>
     * A pattern map is created for the particular entity via its unique UUID. Textures are selected at random to apply
     * the blood pattern to the map, then saved. As the damage to the entity grows, more will be added to the map, and
     * as the entity heals more will be taken away.
     * <p>
     * The texture for the entity is then converted into a Dynamic Texture and applied.
     * <p>
     * NOTE: This seems to work perfectly well for most vanilla and modded entities. There are issues with certain
     * modded entities such as MCDOOM enemies and Ice and Fire dragons. I will be looking into these to see how they
     * render the textures for their entities differently to try and add compatibility for most vanilla and modded mobs.
     * <p>
     * EXTRA NOTE: The Epic Fight Mod also renders its entities differently than normal Minecraft, which allows for its
     * incredible animations. This is incompatible with this feature. So, for now it will be incompatible until I add in
     * optional support later down the line.
     */
    @Inject(method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
              at = @At(value = "INVOKE", shift = At.Shift.BEFORE, target = "Lcom/mojang/blaze3d/vertex/PoseStack;popPose()V")
    )
    private void renderBloodLayer(T pEntity, float pEntityYaw, float pPartialTicks, PoseStack poseStack, MultiBufferSource pBuffer, int pPackedLight, CallbackInfo ci) {
        // TODO: Need to rework this if statement. Probably don't want the 'deadOrDying' one. Probably want to add a blacklist for entities. Maybe even a mod blacklist in the namespace too.
        if (ClientConfig.showMobDamage() && this.entity.isAlive() && this.entity.getHealth() < this.entity.getMaxHealth() && !(this.entity instanceof Player)) {
            String entityName = this.entity.getEncodeId();

            // Retrieve the damage layer to overlay onto the current entity.
            this.mapEntityDamageLayer();
            NativeImage damageLayerTexture = this.entityTextureDamageMap.get(this.entity.getUUID());

            // Retrieve the damage color for the given entity.
            Color damageColor = this.getMobDamageColor(entityName);

            // Define the pixelDamageMap that holds the pixel damage coordinates to paint
            // onto the damage texture layer.
//            this.setPixelDamageMap(damageLayerTexture);
            this.setInjuriesMap(damageLayerTexture);

            // Paints the damage pixels from the pixelDamageMap onto the new damage texture layer.
//            this.paintDamageToNativeImage(entityName, damageLayerTexture, damageColor);
            this.paintInjuriesToNativeImage(entityName, damageLayerTexture, damageColor);

            // Renders the new damage layer over the entity's existing texture layer.
            this.renderDamageLayerToBuffer(damageLayerTexture, pEntity, poseStack, pPartialTicks, pPackedLight);
        }
        else {
            pixelDamageMap.remove(this.entity.getUUID());
            entityTextureDamageMap.remove(this.entity.getUUID());
        }
    }

    /**
     * All the code needed to render the new entity damage layer.
     */
    private void renderDamageLayerToBuffer(NativeImage damageLayerTexture, T pEntity, PoseStack poseStack, float pPartialTicks,  int pPackedLight) {
        DynamicTexture dynamicTexture = new DynamicTexture(damageLayerTexture);
        VertexConsumer customVertexConsumer = this.buffer.getBuffer(RenderType.entityTranslucent(Minecraft.getInstance().getTextureManager().register("test", dynamicTexture)));

        // TODO: New code. Remove this line when everything else is reworked.
        Minecraft minecraft = Minecraft.getInstance();
        boolean flag = this.isBodyVisible(pEntity);
        boolean flag1 = !flag && !pEntity.isInvisibleTo(minecraft.player);
        boolean flag2 = minecraft.shouldEntityAppearGlowing(pEntity);

        RenderType rendertype = this.getRenderType(pEntity, flag, flag1, flag2);
        if (rendertype != null) {
            int i = LivingEntityRenderer.getOverlayCoords(pEntity, this.getWhiteOverlayProgress(pEntity, pPartialTicks));
            this.model.renderToBuffer(poseStack, customVertexConsumer, pPackedLight, i, 1.0F, 1.0F, 1.0F, flag1 ? 0.15F : 1.0F);
        }
    }

    private void paintDamageToNativeImage(String entityName, NativeImage damageLayerTexture, Color damageColor) {
        for (ArrayList<Integer> currentPattern : pixelDamageMap.get(this.entity.getUUID())) {
            int randomChosenWidthStart = currentPattern.get(0);
            int randomChosenHeightStart = currentPattern.get(1);
            int randomColorHue = currentPattern.get(2);

            if (randomColorHue < 0 && !CommonConfig.solidEntities().contains(entityName)) {
                damageLayerTexture.setPixelRGBA(randomChosenWidthStart, randomChosenHeightStart, damageColor.darker().getRGB());
            }
            else if (randomColorHue > 0 && !CommonConfig.solidEntities().contains(entityName)) {
                damageLayerTexture.setPixelRGBA(randomChosenWidthStart, randomChosenHeightStart, damageColor.brighter().getRGB());
            }
            else {
                damageLayerTexture.setPixelRGBA(randomChosenWidthStart, randomChosenHeightStart, damageColor.getRGB());
            }
        }
    }

    private void paintInjuriesToNativeImage(String entityName, NativeImage damageLayerTexture, Color damageColor) {
        for (List<ArrayList<Integer>> injuryCoords : this.entityInjuriesMap.get(this.entity.getUUID())) {
            for (ArrayList<Integer> pixelDamageLocation : injuryCoords) {
                int width = pixelDamageLocation.get(0);
                int height = pixelDamageLocation.get(1);
                int hue = pixelDamageLocation.get(2);

                if (hue < 0 && !CommonConfig.solidEntities().contains(entityName)) {
                    damageLayerTexture.setPixelRGBA(width, height, damageColor.darker().getRGB());
                }
                else if (hue > 0 && !CommonConfig.solidEntities().contains(entityName)) {
                    damageLayerTexture.setPixelRGBA(width, height, damageColor.darker().getRGB());
                }
                else {
                    damageLayerTexture.setPixelRGBA(width, height, damageColor.getRGB());
                }

                int width2 = pixelDamageLocation.get(3);
                int height2 = pixelDamageLocation.get(4);
                int hue2 = pixelDamageLocation.get(5);

                if (hue2 < 0 && !CommonConfig.solidEntities().contains(entityName)) {
                    damageLayerTexture.setPixelRGBA(width2, height2, damageColor.brighter().getRGB());
                }
                else if (hue2 > 0 && !CommonConfig.solidEntities().contains(entityName)) {
                    damageLayerTexture.setPixelRGBA(width2, height2, damageColor.darker().getRGB());
                }
                else {
                    damageLayerTexture.setPixelRGBA(width2, height2, damageColor.getRGB());
                }

                int width3 = pixelDamageLocation.get(6);
                int height3 = pixelDamageLocation.get(7);
                int hue3 = pixelDamageLocation.get(8);

                if (hue3 < 0 && !CommonConfig.solidEntities().contains(entityName)) {
                    damageLayerTexture.setPixelRGBA(width3, height3, damageColor.brighter().getRGB());
                }
                else if (hue3 > 0 && !CommonConfig.solidEntities().contains(entityName)) {
                    damageLayerTexture.setPixelRGBA(width3, height3, damageColor.brighter().getRGB());
                }
                else {
                    damageLayerTexture.setPixelRGBA(width3, height3, damageColor.getRGB());
                }
            }
        }
    }

    private void setInjuriesMap(NativeImage damageLayerTexture) {
        int totalPixels = damageLayerTexture.getWidth() * damageLayerTexture.getHeight();
        int maxNumberOfInjuries = (int) (this.entity.getMaxHealth() * (ClientConfig.entityDamageShownPercent() * 0.01));
        int currentNumberOfInjuries = (int) Math.ceil((this.entity.getMaxHealth() - this.entity.getHealth()) * (ClientConfig.entityDamageShownPercent() * 0.01));
//        entityInjuriesMap.putIfAbsent()
        if (!entityInjuriesMap.containsKey(this.entity.getUUID())) {
            entityInjuriesMap.put(this.entity.getUUID(), new ArrayList<>());
        }

        List<List<ArrayList<Integer>>> injuries = entityInjuriesMap.get(this.entity.getUUID());
        if (injuries.size() < currentNumberOfInjuries) {
            boolean isDamageLocationSet = false;
            int width;
            int height;
//            int hue;
            int injuryLengthLimit = 12;
            int injuriesToAdd = currentNumberOfInjuries - injuries.size();
            BloodyBitsMod.LOGGER.info("INJURIES TO ADD: {}", injuriesToAdd);

            for (int i = 0; i < injuriesToAdd; i++) {
                List<ArrayList<Integer>> injuryCoords = new ArrayList<>();

                do {
                    width = new Random().nextInt(damageLayerTexture.getWidth() - 1);
                    height = new Random().nextInt(damageLayerTexture.getHeight() - 1);
//                    hue = new Random().ints(-1, 1).findFirst().getAsInt();

                    BloodyBitsMod.LOGGER.info("NEW INJURY CHOSEN STARTING COORDS: WIDTH {} HEIGHT {}", width, height);
                }
                while (damageLayerTexture.getPixelRGBA(width, height) != 65280);

                // TODO: Can rework this more to make detailed injury types.
                for (int j = 0; j < injuryLengthLimit; j++) {
                    ArrayList<Integer> pixelDamageLocationToAdd = new ArrayList<>(9);
                    pixelDamageLocationToAdd.add(width + j);
                    pixelDamageLocationToAdd.add(height - j);
                    pixelDamageLocationToAdd.add(new Random().ints(-1, 1).findFirst().getAsInt());

                    pixelDamageLocationToAdd.add(width + j);
                    pixelDamageLocationToAdd.add(height - (j + 1));
                    pixelDamageLocationToAdd.add(new Random().ints(-1, 1).findFirst().getAsInt());

                    pixelDamageLocationToAdd.add(width + j);
                    pixelDamageLocationToAdd.add(height - (j - 1));
                    pixelDamageLocationToAdd.add(new Random().ints(-1, 1).findFirst().getAsInt());
//                    BloodyBitsMod.LOGGER.info("PIXEL DAMAGE LOCATION WIDTH: {} HEIGHT: {}", width + j, height - j);

                    if (pixelDamageLocationToAdd.get(0) < damageLayerTexture.getWidth() - 1 &&
                        pixelDamageLocationToAdd.get(1) < damageLayerTexture.getHeight() - 1 &&
                        pixelDamageLocationToAdd.get(0) > 0 &&
                        pixelDamageLocationToAdd.get(1) > 0) {
                        injuryCoords.add(pixelDamageLocationToAdd);
                    }
//                    pixelDamageLocationToAdd.clear();
                }
                injuries.add(injuryCoords);
            }
            this.entityInjuriesMap.put(this.entity.getUUID(), injuries);
        }
    }

    private void setPixelDamageMap(NativeImage damageLayerTexture) {
        int pixelsToModifyPerHealthPoint = (int) (((damageLayerTexture.getHeight() * damageLayerTexture.getWidth()) / this.entity.getMaxHealth()) * (ClientConfig.entityDamageShownPercent() * 0.01));
        int currentDamagePixels = (int) Math.ceil((this.entity.getMaxHealth() - this.entity.getHealth())) * pixelsToModifyPerHealthPoint;

        // Sets up the initial map for pixel damage.
        if (!pixelDamageMap.containsKey(this.entity.getUUID())) {
            pixelDamageMap.put(this.entity.getUUID(), new ArrayList<>());
        }


        if (pixelDamageMap.get(this.entity.getUUID()).size() < currentDamagePixels) {
            int amountToAdd = currentDamagePixels - pixelDamageMap.get(this.entity.getUUID()).size();

            List<ArrayList<Integer>> updatedPixelDamageList = pixelDamageMap.get(this.entity.getUUID());
            for (int i = 0; i < amountToAdd; i++) {

                // The array list here is for setting the height, width, and hue of the pixel in the image
                // to add a damage texture to.
                ArrayList<Integer> pixelDamageLocationToAdd = new ArrayList<>(3);
                int width = new Random().nextInt(damageLayerTexture.getWidth() - 1);
                int height = new Random().nextInt(damageLayerTexture.getHeight() - 1);
                int hue = new Random().ints(-1, 1).findFirst().getAsInt();

                // Only change the pixels that we have marked for change (65280).
                if (damageLayerTexture.getPixelRGBA(width, height) == 65280) {
                    pixelDamageLocationToAdd.add(width);
                    pixelDamageLocationToAdd.add(height);
                    pixelDamageLocationToAdd.add(hue);
                    updatedPixelDamageList.add(pixelDamageLocationToAdd);
                }
            }
            pixelDamageMap.put(this.entity.getUUID(), updatedPixelDamageList);
        }
        else {
            for (int i = pixelDamageMap.get(this.entity.getUUID()).size() - 1; i > currentDamagePixels; i--) {
                pixelDamageMap.get(this.entity.getUUID()).remove(i);
            }
        }
    }

    private void upwardAngleCut() {

    }

    private void mapEntityDamageLayer() {

        if (!this.entityTextureDamageMap.containsKey(this.entity.getUUID())) {
            BloodyBitsMod.LOGGER.info("ENTITY {} BOUNDING BOX: {}", this.entity.getName(), this.entity.getBoundingBox().getSize());
            NativeImage entityDamageTexture;
            try {
                NativeImage originalImage = NativeImage.read(Minecraft.getInstance().getResourceManager().open(this.getTextureLocation((T) this.entity)));
                entityDamageTexture = new NativeImage(originalImage.getWidth(), originalImage.getHeight(), false);
                for (int x = 0; x < originalImage.getWidth() - 1; x++) {
                    for (int y = 0; y < originalImage.getHeight() - 1; y++) {

//                        BloodyBitsMod.LOGGER.info("ENTITY {} HAS AN RGB VALUE OF {} AT POS X: {} Y: {}", this.entity.getName(), originalImage.getPixelRGBA(x, y), x, y);
                        // We want to ensure that no blank values or "white" values are mapped. Essentially no values that are already 0 on the alpha channel.
                        if (originalImage.getPixelRGBA(x, y) == 0 || originalImage.getPixelRGBA(x, y) == 16777215) {
//                            BloodyBitsMod.LOGGER.info("ENTITY {} HAS BLANK PIXELS HERE: X: {} Y: {}", this.entity.getName(), x, y);
                            Color invisibleLayer = new Color(255, 255, 255, 0);
                            entityDamageTexture.setPixelRGBA(x, y, invisibleLayer.getRGB());
                        }
                        else {
//                            if (x < 64 && y < 16) {
//                                BloodyBitsMod.LOGGER.info("ENTITY {} HAS AN RGB VALUE OF {} AT POS X: {} Y: {}", this.entity.getName(), originalImage.getPixelRGBA(x, y), x, y);
//                            }
//                            originalImage.getPixelRGBA()
                            entityDamageTexture.setPixelRGBA(x, y, new Color(0, 255, 0,0).getRGB());
                            BloodyBitsMod.LOGGER.info("RGBA VALUE TO FIND: {}", entityDamageTexture.getPixelRGBA(x, y));
                        }
                    }
                }

                BloodyBitsMod.LOGGER.info("ENTITY {} WIDTH {} HEIGHT {}", this.entity.getName(), originalImage.getWidth(), originalImage.getHeight());
            }
            catch (IOException e) {
                if (!this.hasErrorMessageDisplayed) {
                    BloodyBitsMod.LOGGER.error("ERROR: File for {} not found at resource location {}!", this.entity, this.getTextureLocation((T) this.entity));
                    this.hasErrorMessageDisplayed = true;

                }
                // TODO: do we want to set a config for the default size of this to create more detailed damage bits.
                //       Also, maybe we can get the current entity's size and base it off of that.
                entityDamageTexture = new NativeImage(64, 64, false);
            }

            this.entityTextureDamageMap.put(this.entity.getUUID(), entityDamageTexture);
        }
    }

    private Color getMobDamageColor(String entityName) {
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

        return new Color(blueDamage, greenDamage, redDamage, alphaDamage);
    }
}
