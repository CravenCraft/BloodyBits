package com.cravencraft.bloodybits.client.model;

import com.cravencraft.bloodybits.BloodyBitsMod;
import com.cravencraft.bloodybits.config.ClientConfig;
import com.cravencraft.bloodybits.config.CommonConfig;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;

import java.io.IOException;
import java.util.*;

public class EntityDamage {
    private final List<NativeImage> availableInjuryTextures = new ArrayList<>();
    private final ArrayList<NativeImage> appliedInjuryTextures = new ArrayList<>(10); // TODO: Make the maximum allowed applied textures a client config.

    public EntityDamage(String entityName) {
        try {
            String modifiedEntityName = (entityName.equals("player")) ? entityName : decompose(entityName, ':')[1];

            // TODO: Make the 25 here a client config for the maximum allowed injury textures per entity.
            for (int i = 0; i < 25; i++) {
                String path = "textures/entity/" + modifiedEntityName + "/" + i + ".png";
                ResourceLocation injuryTextureResourceLocation = new ResourceLocation(BloodyBitsMod.MODID, path);
                NativeImage damageLayerTexture = NativeImage.read(Minecraft.getInstance().getResourceManager().open(injuryTextureResourceLocation));
                int entityDamageColor = this.getMobDamageColor(entityName);
                this.paintDamageToNativeImage(damageLayerTexture, entityDamageColor);
                this.availableInjuryTextures.add(damageLayerTexture);
                BloodyBitsMod.LOGGER.error("{} ADDED TO THE LIST OF AVAILABLE {} TEXTURES.", path, modifiedEntityName); // TODO: Remove after you finish testing.
            }

        } catch (IOException ignored) {}

    }

    public List<NativeImage> getAvailableInjuryTextures() {
        return availableInjuryTextures;
    }

    public List<NativeImage> getAppliedInjuryTextures() {
        return appliedInjuryTextures;
    }

    /**
     * Adds or removes injury texture images to the applied list depending on the entity's current
     * health percentage.
     *
     * @param entityHealthPercentage
     */
    public void modifyInjuryTextures(float entityHealthPercentage) {

        // Only do the texture work if the entity has available or applied textures.
        if (!this.availableInjuryTextures.isEmpty() || !this.appliedInjuryTextures.isEmpty()) {

            int injuries = (int) (entityHealthPercentage * 10); // TODO: Make the 10 here a client config for max available injuries and apply where needed.

            if (this.appliedInjuryTextures.size() != injuries) {
                // TODO: Client config value to be added here too.
                if (this.appliedInjuryTextures.size() < 10 && injuries > this.appliedInjuryTextures.size()) {
                    int injuriesToAdd = injuries - this.appliedInjuryTextures.size();
                    for (int i = 0; i < injuriesToAdd; i++) {
                        if (this.availableInjuryTextures.isEmpty()) {
                            break;
                        }
                        BloodyBitsMod.LOGGER.info("INDEX: {}", i);
                        BloodyBitsMod.LOGGER.info("AVAILABLE INJURY TEXTURE SIZE: {}", this.availableInjuryTextures.size());
                        int chosenTextureIndex = (this.availableInjuryTextures.size() > 1) ? new Random().nextInt(this.availableInjuryTextures.size() - 1) : 0;
                        BloodyBitsMod.LOGGER.info("RANDOM TEXTURE TO BE APPLIED: {}", chosenTextureIndex);

                        NativeImage availableInjuryImage = this.availableInjuryTextures.get(chosenTextureIndex);

                        this.appliedInjuryTextures.add(availableInjuryImage);
                        this.availableInjuryTextures.remove(availableInjuryImage);

                        BloodyBitsMod.LOGGER.info("INJURY ADDED. INJURIES LIST SIZE: {}", this.appliedInjuryTextures.size());
                    }
                }
                else if (injuries < this.appliedInjuryTextures.size()) {
                    int injuriesToRemove = this.appliedInjuryTextures.size() - injuries;
                    for (int i = this.appliedInjuryTextures.size(); i > injuriesToRemove; i--) {
                        NativeImage injuryImageToRemove = this.appliedInjuryTextures.get(this.appliedInjuryTextures.size() - 1);

                        this.availableInjuryTextures.add(injuryImageToRemove);
                        this.appliedInjuryTextures.remove(injuryImageToRemove);

                        BloodyBitsMod.LOGGER.info("INJURY REMOVED. INJURY LIST SIZE: {}", this.appliedInjuryTextures.size());
                    }
                }

                BloodyBitsMod.LOGGER.info("AVAILABLE INJURIES AFTER: {} APPLIED INJURIES AFTER: {}", this.availableInjuryTextures.size(), this.appliedInjuryTextures.size());
            }
        }


    }

    protected static String[] decompose(String pLocation, char pSeparator) {
        String[] astring = new String[]{"minecraft", pLocation};
        int i = pLocation.indexOf(pSeparator);
        if (i >= 0) {
            astring[1] = pLocation.substring(i + 1);
            if (i >= 1) {
                astring[0] = pLocation.substring(0, i);
            }
        }

        return astring;
    }

    private int getMobDamageColor(String entityName) {
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
            for (Map.Entry<String, List<String>> mapElement : ClientConfig.entityBloodColors().entrySet()) {
                if (mapElement.getValue().contains(Objects.requireNonNull(entityName))) {
                    String bloodColorHexVal = mapElement.getKey();
                    redDamage = HexFormat.fromHexDigits(bloodColorHexVal, 1, 3);
                    greenDamage = HexFormat.fromHexDigits(bloodColorHexVal, 3, 5);
                    blueDamage = HexFormat.fromHexDigits(bloodColorHexVal.substring(5));
                    break;
                }
            }
        }
        return FastColor.ABGR32.color(alphaDamage, blueDamage, greenDamage, redDamage);
    }

    private void paintDamageToNativeImage(NativeImage damageLayerTexture, int damageColorRGBA) {
        for (int x = 0; x < damageLayerTexture.getWidth(); x++) {
            for (int y = 0; y < damageLayerTexture.getHeight(); y++) {
                if (damageLayerTexture.getPixelRGBA(x, y) != 0) {
                    int median = 125;

                    int damageLayerPixelRGBA = damageLayerTexture.getPixelRGBA(x, y);
                    int currentDamageLayerAlpha = FastColor.ABGR32.alpha(damageLayerPixelRGBA);
                    int currentDamageLayerRed = FastColor.ABGR32.red(damageLayerPixelRGBA);
                    int currentDamageLayerGreen = FastColor.ABGR32.green(damageLayerPixelRGBA);
                    int currentDamageLayerBlue = FastColor.ABGR32.blue(damageLayerPixelRGBA);

                    int newDamageColorRed = FastColor.ABGR32.red(damageColorRGBA);
                    int newDamageColorGreen = FastColor.ABGR32.green(damageColorRGBA);
                    int newDamageColorBlue = FastColor.ABGR32.blue(damageColorRGBA);

                    newDamageColorRed = (int) Math.min(newDamageColorRed * ((float) currentDamageLayerRed / median), 255);
                    newDamageColorGreen = (int) Math.min(newDamageColorGreen * ((float) currentDamageLayerGreen / median), 255);
                    newDamageColorBlue = (int) Math.min(newDamageColorBlue * ((float) currentDamageLayerBlue / median), 255);

                    int newDamageLayerRGBA = FastColor.ABGR32.color(currentDamageLayerAlpha, newDamageColorBlue, newDamageColorGreen, newDamageColorRed);

                    damageLayerTexture.setPixelRGBA(x, y, newDamageLayerRGBA);
                }
            }
        }
    }
}
