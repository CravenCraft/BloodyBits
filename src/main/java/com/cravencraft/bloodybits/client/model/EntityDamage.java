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
    private final String entityName;
    private final List<NativeImage> availableInjuryTextures = new ArrayList<>(ClientConfig.availableTexturesPerEntity());
//    private final List<NativeImage> unpaintedAppliedInjuryTextures = new ArrayList<>(ClientConfig.availableTexturesPerEntity());
    private final HashMap<Integer, NativeImage> paintedAppliedInjuryTextures = new HashMap<>(ClientConfig.maxEntityInjuries());
    private int entityInjuries;
    private final List<Integer> usedInjuryTextures = new ArrayList<>(ClientConfig.maxEntityInjuries());

    private final List<NativeImage> bloodColorInjuries = new ArrayList<>(ClientConfig.availableTexturesPerEntity());
    private final List<NativeImage> burnColorInjuries = new ArrayList<>(ClientConfig.availableTexturesPerEntity());
    private final HashMap<String, NativeImage> todoRenameInjuryMap = new HashMap<>();

    public EntityDamage(String entityName) {
        this.entityInjuries = 0;
        this.entityName = entityName;
//        this.entityDamageColor = this.getMobDamageColor(entityName);
        try {
            String modifiedEntityName = (entityName.equals("player")) ? entityName : decompose(entityName, ':')[1];

            for (int i = 0; i < ClientConfig.availableTexturesPerEntity(); i++) {
                String path = "textures/entity/" + modifiedEntityName + "/" + i + ".png";
                ResourceLocation injuryTextureResourceLocation = new ResourceLocation(BloodyBitsMod.MODID, path);
                NativeImage damageLayerTexture = NativeImage.read(Minecraft.getInstance().getResourceManager().open(injuryTextureResourceLocation));
//                this.paintDamageToNativeImage(damageLayerTexture, this.entityDamageColor);
                this.availableInjuryTextures.add(damageLayerTexture);

                NativeImage bloodColorDamage = damageLayerTexture;
                NativeImage burnColorDamage = damageLayerTexture;

                int entityDamageColor = this.getMobDamageColor(entityName);

                // Doing all paint logic. Currently, that means painting the blood and (if applicable)
                // burn overlay.
                bloodColorDamage = this.paintDamageToNativeImage(bloodColorDamage, entityDamageColor);
                burnColorDamage = this.paintDamageToNativeImage(burnColorDamage, this.getBurnDamageColor());

                BloodyBitsMod.LOGGER.error("{} ADDED TO THE LIST OF AVAILABLE {} TEXTURES.", path, modifiedEntityName); // TODO: Remove after you finish testing.
            }

        } catch (IOException ignored) {}

    }

    public List<NativeImage> getAvailableInjuryTextures() {
        return availableInjuryTextures;
    }

    public HashMap<Integer, NativeImage> getPaintedAppliedInjuryTextures() {
        return paintedAppliedInjuryTextures;
    }

    /**
     * Adds or removes injury texture images to the applied list depending on the entity's current
     * health percentage.
     */
    public void addInjuries(String damageType, float entityMissingHealthPercentage) {
        BloodyBitsMod.LOGGER.info("ADDING INJURIES");

        // Only do the texture work if the entity has available or applied textures.
        if (!this.availableInjuryTextures.isEmpty()) {

            int injuries = (int) (entityMissingHealthPercentage * ClientConfig.maxEntityInjuries());
            BloodyBitsMod.LOGGER.info("ENTITY MISSING HEALTH PERCENTAGE: {}", entityMissingHealthPercentage);

            if (injuries > this.entityInjuries && this.entityInjuries < ClientConfig.maxEntityInjuries()) {
                int injuriesToAdd = injuries - this.entityInjuries;
                int randomTextureRange = this.availableInjuryTextures.size();

                for (int i = 0; i < injuriesToAdd; i++) {
//                    BloodyBitsMod.LOGGER.info("DAMAGE TYPE: {}", damageType);
                    if (!ClientConfig.blackListInjurySources().contains(damageType) || this.usedInjuryTextures.size() == this.availableInjuryTextures.size()) {

//                        BloodyBitsMod.LOGGER.info("RANDOM TEXTURE RANGE: {}", randomTextureRange);

                        List<Integer> availableTextureIndexes = new ArrayList<>(randomTextureRange);
                        for (int j = 0; j < randomTextureRange; j++) {
                            availableTextureIndexes.add(j);
                        }

//                        BloodyBitsMod.LOGGER.info("----- ENTERING WHILE LOOP FOR RANDOM TEXTURE INDEX -----");
//                        BloodyBitsMod.LOGGER.info("USED INJURY TEXTURES PRIOR TO WHILE LOOP: {}", this.usedInjuryTextures);

                        boolean exitLoop = false;
                        while (!exitLoop) {
//                            BloodyBitsMod.LOGGER.info("AVAILABLE TEXTURE INDEXES:\n {}", availableTextureIndexes);
                            int chosenTextureIndex = availableTextureIndexes.get(new Random().nextInt(availableTextureIndexes.size()));
//                            BloodyBitsMod.LOGGER.info("CHOSEN TEXTURE: {}", chosenTextureIndex);

                            if (!this.usedInjuryTextures.contains(chosenTextureIndex)) {
                                this.usedInjuryTextures.add(chosenTextureIndex);
//                                BloodyBitsMod.LOGGER.info("USED INJURY TEXTURES AFTER THE RANDOMLY CHOSEN TEXTURE:\n {}", this.usedInjuryTextures);
                                int entityDamageColor = this.getMobDamageColor(this.entityName);
                                NativeImage availableInjuryImage = this.availableInjuryTextures.get(chosenTextureIndex);

                                // Doing all paint logic. Currently, that means painting the blood and (if applicable)
                                // burn overlay.
                                this.paintDamageToNativeImage(availableInjuryImage, entityDamageColor);
                                if (ClientConfig.burnDamageSources().contains(damageType)) {
//                                    BloodyBitsMod.LOGGER.info("APPLYING BURN DAMAGE COLOR FOR DAMAGE TYPE: {}", damageType);
                                    this.paintDamageToNativeImage(availableInjuryImage, this.getBurnDamageColor());
                                }

                                this.paintedAppliedInjuryTextures.put(this.entityInjuries, availableInjuryImage);
//                                BloodyBitsMod.LOGGER.info("RANDOM TEXTURE INDEX {} SUCCESSFULLY APPLIED FOR INJURY NUMBER: {}", chosenTextureIndex, this.entityInjuries);
                                exitLoop = true;
                            }
                            else {
                                availableTextureIndexes.remove(chosenTextureIndex);
                            }

                            if (availableTextureIndexes.isEmpty()) {
                                exitLoop = true;
                            }
                        }
//                        BloodyBitsMod.LOGGER.info("----- EXITING WHILE LOOP -----");
                    }

                    this.entityInjuries++;
//                    BloodyBitsMod.LOGGER.info("ENTITY INJURIES: {}", this.entityInjuries);
                }
            }
        }
    }

    public void removeInjuries(float entityMissingHealthPercentage) {
        BloodyBitsMod.LOGGER.info("----------------------- REMOVING INJURIES -----------------------");
        BloodyBitsMod.LOGGER.info("PAINTED INJURY TEXTURES BEFORE: {}", this.paintedAppliedInjuryTextures);
        if (!this.availableInjuryTextures.isEmpty() || !this.paintedAppliedInjuryTextures.isEmpty()) {

            int injuries = (int) (entityMissingHealthPercentage * ClientConfig.maxEntityInjuries());
//            BloodyBitsMod.LOGGER.info("HEALING ENTITY MISSING HEALTH PERCENTAGE: {}", entityMissingHealthPercentage);
//            BloodyBitsMod.LOGGER.info("ENTITY INJURIES BEFORE: {} CURRENT INJURIES: {}", this.entityInjuries, injuries);

            if (injuries < this.entityInjuries) {
                int injuriesToRemove = this.entityInjuries - injuries;
//                BloodyBitsMod.LOGGER.info("INJURIES TO REMOVE: {}", injuriesToRemove);

//                BloodyBitsMod.LOGGER.info("----- PRIOR TO INJURY REMOVAL -----");
                for (int i = 0; i < injuriesToRemove; i++) {
                    this.entityInjuries--;

//                    BloodyBitsMod.LOGGER.info("PAINTED APPLIED INJURY TEXTURES SIZE: {}", this.paintedAppliedInjuryTextures.size());
//                    BloodyBitsMod.LOGGER.info("USED INJURY TEXTURES PRIOR TO REMOVAL:\n {}", this.usedInjuryTextures);

                    if (!this.paintedAppliedInjuryTextures.isEmpty() && this.paintedAppliedInjuryTextures.containsKey(this.entityInjuries)) {

                        this.paintedAppliedInjuryTextures.remove(this.entityInjuries);
                        this.usedInjuryTextures.remove(this.usedInjuryTextures.size() - 1);
                    }
//                    BloodyBitsMod.LOGGER.info("----- AFTER INJURY REMOVAL -----");
//                    BloodyBitsMod.LOGGER.info("USED INJURY TEXTURES AFTER REMOVAL:\n {}", this.usedInjuryTextures);
//                    BloodyBitsMod.LOGGER.info("PAINTED APPLIED INJURY TEXTURES SIZE AFTER REMOVAL: {}", this.paintedAppliedInjuryTextures.size());
                }
            }
//            BloodyBitsMod.LOGGER.info("--- ENTITY INJURIES AFTER: {} CURRENT INJURIES: {}", this.entityInjuries, injuries);
//            BloodyBitsMod.LOGGER.info("PAINTED INJURY TEXTURES AFTER: {}", this.paintedAppliedInjuryTextures);
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

    private int getBurnDamageColor() {
        String bloodColorHexVal = ClientConfig.getBurnDamageColor();
        int redDamage = HexFormat.fromHexDigits(bloodColorHexVal, 1, 3);
        int greenDamage = HexFormat.fromHexDigits(bloodColorHexVal, 3, 5);
        int blueDamage = HexFormat.fromHexDigits(bloodColorHexVal.substring(5));
        return FastColor.ABGR32.color(255, blueDamage, greenDamage, redDamage);
    }

    private NativeImage paintDamageToNativeImage(NativeImage damageLayerTexture, int damageColorRGBA) {
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
        return damageLayerTexture;
    }
}
