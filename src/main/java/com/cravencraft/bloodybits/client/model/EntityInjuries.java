package com.cravencraft.bloodybits.client.model;

import com.cravencraft.bloodybits.BloodyBitsMod;
import com.cravencraft.bloodybits.config.ClientConfig;
import com.cravencraft.bloodybits.utils.BloodyBitsUtils;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

import java.io.IOException;
import java.util.*;

public class EntityInjuries {

    private int smallBleedHits;
    private int mediumBleedHits;
    private int largeBleedHits;

    private int smallBurnHits;
    private int mediumBurnHits;
    private int largeBurnHits;

    private int smallHealAmount;
    private int mediumHealAmount;
    private int largeHealAmount;

    public List<NativeImage> smallInjuries = new ArrayList<>();
    public List<NativeImage> mediumInjuries = new ArrayList<>();
    public List<NativeImage> largeInjuries = new ArrayList<>();

    private final List<NativeImage> smallBleedInjuries = new ArrayList<>(10);
    private final List<NativeImage> mediumBleedInjuries = new ArrayList<>(5);
    private final List<NativeImage> largeBleedInjuries = new ArrayList<>(3);

    private final List<NativeImage> smallBurnInjuries = new ArrayList<>(10);
    private final List<NativeImage> mediumBurnInjuries = new ArrayList<>(5);
    private final List<NativeImage> largeBurnInjuries = new ArrayList<>(3);


    public EntityInjuries(String entityName) {
        String modifiedEntityName = (entityName.equals("player")) ? entityName : BloodyBitsUtils.decompose(entityName, ':')[1];
        String path = "textures/entity/" + modifiedEntityName + "/";

        this.addEntityDamageTexture(path + "small_injuries/", entityName);
        this.addEntityDamageTexture(path + "medium_injuries/", entityName);
        this.addEntityDamageTexture(path + "large_injuries/", entityName);
    }

    private void addEntityDamageTexture(String path, String entityName) {

        for (int i = 0; i < ClientConfig.availableTexturesPerEntity(); i++) {
            String modifiedPath = path.concat(i + ".png");
            try {
                ResourceLocation injuryTextureResourceLocation = new ResourceLocation(BloodyBitsMod.MODID, modifiedPath);
                NativeImage bloodColorDamageLayer = NativeImage.read(Minecraft.getInstance().getResourceManager().open(injuryTextureResourceLocation));
                NativeImage burnColorDamageLayer = NativeImage.read(Minecraft.getInstance().getResourceManager().open(injuryTextureResourceLocation));

                // Doing all paint logic. Currently, that means painting the blood and (if applicable)
                // burn overlay.
                // TODO: Might just be able to pass in the original Native image to these. Test out after everything.
                BloodyBitsUtils.paintDamageToNativeImage(bloodColorDamageLayer, BloodyBitsUtils.getMobDamageColor(entityName));
//                BloodyBitsUtils.paintDamageToNativeImage(burnColorDamageLayer, BloodyBitsUtils.getMobDamageColor(entityName));
                BloodyBitsUtils.paintDamageToNativeImage(burnColorDamageLayer, BloodyBitsUtils.getBurnDamageColor());

                if (path.contains("small")) {
                    this.smallBleedInjuries.add(bloodColorDamageLayer);
                    this.smallBurnInjuries.add(burnColorDamageLayer);
                }
                else if (path.contains("medium")) {
                    this.mediumBleedInjuries.add(bloodColorDamageLayer);
                    this.mediumBurnInjuries.add(burnColorDamageLayer);
                }
                else if (path.contains("large")) {
                    this.largeBleedInjuries.add(bloodColorDamageLayer);
                    this.largeBurnInjuries.add(burnColorDamageLayer);
                }
            }
            catch (IOException ignore) {
                // Want to gracefully ignore when a texture for an entity does not exist.
                break;
            }
        }
    }

    public void addInjuryHits(String injuryType, double entityDamagePercentage) {

        // Apply the appropriate hit size depending on the damage amount.
        if (entityDamagePercentage >= 0.15) {
            switch (injuryType) {
                case "bleed" -> this.largeBleedHits++;
                case "burn" -> this.largeBurnHits++;
            }
        }
        else if (entityDamagePercentage >= 0.05) {
            switch (injuryType) {
                case "bleed" -> this.mediumBleedHits++;
                case "burn" -> this.mediumBurnHits++;
            }
        }
        else {
            switch (injuryType) {
                case "bleed" -> this.smallBleedHits++;
                case "burn" -> this.smallBurnHits++;
            }
        }

        /*
            Perform conversions for bleed and burn hit amounts. Want to do large and small first since the medium one
            will convert for both.
         */

        if (this.largeBleedInjuries.isEmpty() && this.largeBleedHits > 0) {
            this.mediumBleedHits += (this.largeBleedHits * 2);
            this.largeBleedHits = 0;
        }

        if (this.smallBleedInjuries.isEmpty() && this.smallBleedHits > 3) {
            this.mediumBleedHits += (this.smallBleedHits / 3);
            this.smallBleedHits = 0;

        }

        if (this.mediumBleedInjuries.isEmpty()) {
            if (!this.largeBleedInjuries.isEmpty() && this.mediumBleedHits > 1) {
                this.largeBleedHits += (this.mediumBleedHits / 2);
                this.mediumBleedHits = 0;
            }
            else if (!this.smallBleedInjuries.isEmpty() && this.mediumBleedHits > 0) {
                this.smallBleedHits += (this.mediumBleedHits * 2);
                this.mediumBleedHits = 0;
            }
        }

        if (this.largeBurnInjuries.isEmpty() && this.largeBurnHits > 0) {
            this.mediumBurnHits += (this.largeBurnHits * 2);
            this.largeBurnHits = 0;
        }

        if (this.smallBurnInjuries.isEmpty() && this.smallBurnHits > 3) {
            this.mediumBurnHits += (this.smallBurnHits / 3);
            this.smallBurnHits = 0;
        }

        if (this.mediumBurnInjuries.isEmpty()) {
            if (!this.largeBurnInjuries.isEmpty() && this.mediumBurnHits > 1) {
                this.largeBurnHits += (this.mediumBurnHits / 2);
                this.mediumBurnHits = 0;
            }
            else if (!this.smallBurnInjuries.isEmpty() && this.mediumBurnHits > 0) {
                this.smallBurnHits += (this.mediumBurnHits * 2);
                this.mediumBurnHits = 0;
            }
        }

        this.updateInjuries();
    }

    public void addHealAmount(double entityHealPercentage) {
        // Apply the appropriate hit size depending on the damage amount.
        if (entityHealPercentage >= 0.15) {
            this.largeHealAmount++;
        }
        else if (entityHealPercentage >= 0.05) {
            this.mediumHealAmount++;
        }
        else {
            this.smallHealAmount++;
        }

        /*
            Perform conversions for heal amounts. Want to do large and small first since the medium one
            will convert for both.
         */

        if (this.largeInjuries.isEmpty() && this.largeHealAmount > 0) {
            this.mediumHealAmount += (this.largeHealAmount * 2);
            this.largeHealAmount = 0;
        }

        if (this.smallInjuries.isEmpty() && this.smallHealAmount > 3) {
            this.mediumHealAmount += (this.smallHealAmount / 3);
            this.smallHealAmount = 0;
        }

        if (this.mediumInjuries.isEmpty()) {
            if (!this.largeInjuries.isEmpty() && this.mediumHealAmount > 1) {
                this.largeHealAmount += (this.mediumHealAmount / 2);
                this.mediumHealAmount = 0;
            }
            else if (!this.smallInjuries.isEmpty() && this.mediumHealAmount > 0) {
                this.smallHealAmount += (this.mediumHealAmount * 2);
                this.mediumHealAmount = 0;
            }
        }

        this.updateHealInjuries();
    }

    private void updateHealInjuries() {
        BloodyBitsMod.LOGGER.info("----------------------------------------------------------------------------------");
        BloodyBitsMod.LOGGER.info("BEFORE healing SMALL injuries heal amount: {}. Small injuries: {}", this.smallHealAmount, this.smallInjuries.size());
        BloodyBitsMod.LOGGER.info("BEFORE healing MEDIUM injuries heal amount: {}. Medium injuries: {}", this.mediumHealAmount, this.mediumInjuries.size());
        BloodyBitsMod.LOGGER.info("BEFORE healing LARGE injuries heal amount: {}. Large injuries: {}", this.largeHealAmount, this.largeInjuries.size());
        BloodyBitsMod.LOGGER.info("----------------------------------------------------------------------------------");

        if (this.largeHealAmount >= 1) {
            this.healInjuries(this.largeInjuries, this.largeBleedInjuries, this.largeBurnInjuries);
            this.largeHealAmount--;
        }

        if (this.mediumHealAmount >= 2) {
            this.healInjuries(this.mediumInjuries, this.mediumBleedInjuries, this.mediumBurnInjuries);
            this.mediumHealAmount -= 2;
        }

        if (this.smallHealAmount >= 3) {
            this.healInjuries(this.smallInjuries, this.smallBleedInjuries, this.smallBurnInjuries);
            this.smallHealAmount -= 3;
        }

        BloodyBitsMod.LOGGER.info("----------------------------------------------------------------------------------");
        BloodyBitsMod.LOGGER.info("AFTER healing SMALL injuries heal amount: {}. Small injuries: {}", this.smallHealAmount, this.smallInjuries.size());
        BloodyBitsMod.LOGGER.info("AFTER healing MEDIUM injuries heal amount: {}. Medium injuries: {}", this.mediumHealAmount, this.mediumInjuries.size());
        BloodyBitsMod.LOGGER.info("AFTER healing LARGE injuries heal amount: {}. Large injuries: {}", this.largeHealAmount, this.largeInjuries.size());
        BloodyBitsMod.LOGGER.info("----------------------------------------------------------------------------------");
    }

    private void healInjuries(List<NativeImage> currentInjuries, List<NativeImage> bleedInjuries, List<NativeImage> burnInjuries) {
        if (!currentInjuries.isEmpty()) {
            NativeImage lastInList = currentInjuries.get(currentInjuries.size() - 1);

            // Honestly, will probably never be null.
            if (lastInList != null) {
                bleedInjuries.add(lastInList);
                burnInjuries.add(lastInList);
                currentInjuries.remove(lastInList);
            }

        }
    }

    /**
     * TODO: Maybe just break this down into the specific addHit methods for each damage size?
     *       Could simplify things & make this more readable.
     * Updates all injuries if the entity has sustained enough hits of the given damage type & size.
     */
    private void updateInjuries() {
        BloodyBitsMod.LOGGER.info("----------------------------------------------------------------------------------");
        BloodyBitsMod.LOGGER.info("Small BLEED hit BEFORE updating bleed injuries: {} Small bleed injuries: {}", this.smallBleedHits, this.smallInjuries.size());
        BloodyBitsMod.LOGGER.info("Medium BLEED hit BEFORE updating bleed injuries: {} Medium bleed injuries: {}", this.mediumBleedHits, this.mediumInjuries.size());
        BloodyBitsMod.LOGGER.info("Large BLEED hit BEFORE updating bleed injuries: {} Large bleed injuries: {}", this.largeBleedHits, this.largeInjuries.size());
        BloodyBitsMod.LOGGER.info("----------------------------------------------------------------------------------");
        BloodyBitsMod.LOGGER.info("Small BURN hit BEFORE updating burn injuries: {} Small burn injuries: {}", this.smallBurnHits, this.smallInjuries.size());
        BloodyBitsMod.LOGGER.info("Medium BURN hit BEFORE updating burn injuries: {} Medium burn injuries: {}", this.mediumBurnHits, this.mediumInjuries.size());
        BloodyBitsMod.LOGGER.info("Large BURN hit BEFORE updating burn injuries: {} Large burn injuries: {}", this.largeBurnHits, this.largeInjuries.size());
        BloodyBitsMod.LOGGER.info("----------------------------------------------------------------------------------");

        // Update all bleed injuries.
        if (this.largeBleedHits >= 1) {
            this.updateInjuriesList(this.largeBleedInjuries, this.largeBurnInjuries, this.largeInjuries);
            this.largeBleedHits--;
        }
        else if (this.mediumBleedHits >= 2) {
            this.updateInjuriesList(this.mediumBleedInjuries, this.mediumBurnInjuries, this.mediumInjuries);
            this.mediumBleedHits -= 2;
        }
        else if (this.smallBleedHits >= 3) {
            this.updateInjuriesList(this.smallBleedInjuries, this.smallBurnInjuries, this.smallInjuries);
            this.smallBleedHits -= 3;
        }

        // Update all burn injuries.
        if (this.largeBurnHits >= 1) {
            this.updateInjuriesList(this.largeBurnInjuries, this.largeBleedInjuries, this.largeInjuries);
            this.largeBurnHits--;
        }
        else if (this.mediumBurnHits >= 2) {
            this.updateInjuriesList(this.mediumBurnInjuries, this.mediumBleedInjuries, this.mediumInjuries);
            this.mediumBurnHits -= 2;
        }
        else if (this.smallBurnHits >= 3) {
            this.updateInjuriesList(this.smallBurnInjuries, this.smallBleedInjuries, this.smallInjuries);
            this.smallBurnHits -= 3;
        }

        BloodyBitsMod.LOGGER.info("----------------------------------------------------------------------------------");
        BloodyBitsMod.LOGGER.info("Small BLEED hit AFTER updating bleed injuries: {} Small bleed injuries: {}", this.smallBleedHits, this.smallInjuries.size());
        BloodyBitsMod.LOGGER.info("Medium BLEED hit AFTER updating bleed injuries: {} Medium bleed injuries: {}", this.mediumBleedHits, this.mediumInjuries.size());
        BloodyBitsMod.LOGGER.info("Large BLEED hit AFTER updating bleed injuries: {} Large bleed injuries: {}", this.largeBleedHits, this.largeInjuries.size());
        BloodyBitsMod.LOGGER.info("----------------------------------------------------------------------------------");
        BloodyBitsMod.LOGGER.info("Small BURN hit AFTER updating burn injuries: {} Small burn injuries: {}", this.smallBurnHits, this.smallInjuries.size());
        BloodyBitsMod.LOGGER.info("Medium BURN hit AFTER updating burn injuries: {} Medium burn injuries: {}", this.mediumBurnHits, this.mediumInjuries.size());
        BloodyBitsMod.LOGGER.info("Large BURN hit AFTER updating burn injuries: {} Large burn injuries: {}", this.largeBurnHits, this.largeInjuries.size());
        BloodyBitsMod.LOGGER.info("----------------------------------------------------------------------------------");
    }

    private void updateInjuriesList(List<NativeImage> listToTakeFrom, List<NativeImage> otherListToRemove, List<NativeImage> listToAddTo) {
        int randomIndex;
        NativeImage randomInjury;

        if (!listToTakeFrom.isEmpty()) {
            randomIndex = new Random().nextInt(listToTakeFrom.size());
            randomInjury = listToTakeFrom.get(randomIndex);

            listToAddTo.add(randomInjury);
            listToTakeFrom.remove(randomIndex);
            otherListToRemove.remove(randomIndex);
        }
    }
}
