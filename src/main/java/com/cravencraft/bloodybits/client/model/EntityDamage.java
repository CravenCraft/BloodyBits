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
    private int entityDamageColor;
    private final String entityName;
    private final List<NativeImage> availableInjuryTextures = new ArrayList<>();
    private final List<NativeImage> unpaintedAppliedInjuryTextures = new ArrayList<>();
    private final HashMap<NativeImage, String> paintedAppliedInjuryTextures = new HashMap<>(10); // TODO: Make the maximum allowed applied textures a client config.
//    private int entityInjuries;

    public EntityDamage(String entityName) {
//        this.entityInjuries = 0;
        this.entityName = entityName;
//        this.entityDamageColor = this.getMobDamageColor(entityName);
        try {
            String modifiedEntityName = (entityName.equals("player")) ? entityName : decompose(entityName, ':')[1];

            // TODO: Make the 25 here a client config for the maximum allowed injury textures per entity.
            for (int i = 0; i < 25; i++) {
                String path = "textures/entity/" + modifiedEntityName + "/" + i + ".png";
                ResourceLocation injuryTextureResourceLocation = new ResourceLocation(BloodyBitsMod.MODID, path);
                NativeImage damageLayerTexture = NativeImage.read(Minecraft.getInstance().getResourceManager().open(injuryTextureResourceLocation));
//                this.paintDamageToNativeImage(damageLayerTexture, this.entityDamageColor);
                this.availableInjuryTextures.add(damageLayerTexture);
                BloodyBitsMod.LOGGER.error("{} ADDED TO THE LIST OF AVAILABLE {} TEXTURES.", path, modifiedEntityName); // TODO: Remove after you finish testing.
            }

        } catch (IOException ignored) {}

    }

    public List<NativeImage> getAvailableInjuryTextures() {
        return availableInjuryTextures;
    }

    public HashMap<NativeImage, String> getPaintedAppliedInjuryTextures() {
        return paintedAppliedInjuryTextures;
    }

    /**
     * Adds or removes injury texture images to the applied list depending on the entity's current
     * health percentage.
     *
     * @param entityHealthPercentage
     */
    public void modifyInjuryTextures(String damageType, float entityHealthPercentage) {

        // Only do the texture work if the entity has available or applied textures.
        if (!this.availableInjuryTextures.isEmpty() || !this.paintedAppliedInjuryTextures.isEmpty()) {

            int injuries = (int) (entityHealthPercentage * 10); // TODO: Make the 10 here a client config for max available injuries and apply where needed.

            if (this.paintedAppliedInjuryTextures.size() != injuries) {
                // TODO: Client config value to be added here too.
                if (this.paintedAppliedInjuryTextures.size() < 10 && injuries > this.paintedAppliedInjuryTextures.size()) {
                    int injuriesToAdd = injuries - this.paintedAppliedInjuryTextures.size();
                    for (int i = 0; i < injuriesToAdd; i++) {
                        if (this.availableInjuryTextures.isEmpty()) {
                            break;
                        }
                        BloodyBitsMod.LOGGER.info("INDEX: {}", i);
                        BloodyBitsMod.LOGGER.info("AVAILABLE INJURY TEXTURE SIZE: {}", this.availableInjuryTextures.size());
                        int chosenTextureIndex = (this.availableInjuryTextures.size() > 1) ? new Random().nextInt(this.availableInjuryTextures.size() - 1) : 0;
                        BloodyBitsMod.LOGGER.info("RANDOM TEXTURE TO BE APPLIED: {}", chosenTextureIndex);

                        NativeImage availableInjuryImage = this.availableInjuryTextures.get(chosenTextureIndex);

                        // TODO: Remember to only do this adding/subtracting if the damage source allows it.
                        //       Which probably means that we should for loop through the size of the painted textures?
                        this.unpaintedAppliedInjuryTextures.add(availableInjuryImage);
                        this.availableInjuryTextures.remove(availableInjuryImage);
                        int entityDamageColor = this.getMobDamageColor(entityName); // TODO: Flesh out to account for damage types as well.
                        BloodyBitsMod.LOGGER.info("ENTITY DAMAGE COLOR: {}", entityDamageColor);
                        this.paintDamageToNativeImage(availableInjuryImage, entityDamageColor);
                        // TODO: IDK adding and removing things from the hashmap feels inconsistent. Was basically flawless as a list.
                        //       Need to revisit why I wanted a hashmap and what the benefit of storing the damage type there is worth
                        //       if I'm just going to use the buffer list anyway.
                        this.paintedAppliedInjuryTextures.put(availableInjuryImage, damageType);
                        BloodyBitsMod.LOGGER.info("INJURY OF TYPE {} ADDED.", damageType);

//                        this.entityInjuries++;

//                        BloodyBitsMod.LOGGER.info("INJURY ADDED. INJURIES LIST SIZE: {}", this.paintedAppliedInjuryTextures.size());
                        BloodyBitsMod.LOGGER.info("INJURY ADDED. AVAILABLE INJURIES AFTER: {} APPLIED INJURIES AFTER: {}", this.availableInjuryTextures.size(), this.paintedAppliedInjuryTextures.size());
                    }
                }
                else if (injuries < this.paintedAppliedInjuryTextures.size()) {
                    int injuriesToRemove = this.paintedAppliedInjuryTextures.size() - injuries;

                    for (int i = 0; i < injuriesToRemove; i++) {
                        BloodyBitsMod.LOGGER.info("AVAILABLE INJURY TEXTURE SIZE: {}", this.availableInjuryTextures.size());
//                        int index = this.paintedAppliedInjuryTextures.size() - 1;
                        NativeImage injuryImageToRemove = this.unpaintedAppliedInjuryTextures.get(this.paintedAppliedInjuryTextures.size() - 1);

                        this.availableInjuryTextures.add(injuryImageToRemove);
                        this.unpaintedAppliedInjuryTextures.remove(injuryImageToRemove);

//                        injuryImageToRemove = this.paintedAppliedInjuryTextures.get(index);
//                        this.paintedAppliedInjuryTextures.remove(injuryImageToRemove);
                        List<NativeImage> paintedTextures = this.paintedAppliedInjuryTextures.keySet().stream().toList();

                        NativeImage paintedTextureToRemove = paintedTextures.get(paintedTextures.size() - 1);
                        this.paintedAppliedInjuryTextures.remove(paintedTextureToRemove);

//                        ArrayList<> paintedInjuriesArray = this.paintedAppliedInjuryTextures.entrySet().toArray();
                        BloodyBitsMod.LOGGER.info("INJURY OF TYPE {} REMOVED.", damageType);
//                        this.entityInjuries--;

//                        BloodyBitsMod.LOGGER.info("INJURY REMOVED. INJURY LIST SIZE: {}", this.paintedAppliedInjuryTextures.size());
                        BloodyBitsMod.LOGGER.info("INJURY REMOVED. AVAILABLE INJURIES AFTER: {} APPLIED INJURIES AFTER: {}", this.availableInjuryTextures.size(), this.paintedAppliedInjuryTextures.size());
                    }
                }

//                BloodyBitsMod.LOGGER.info("AVAILABLE INJURIES AFTER: {} APPLIED INJURIES AFTER: {}", this.availableInjuryTextures.size(), this.paintedAppliedInjuryTextures.size());
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
