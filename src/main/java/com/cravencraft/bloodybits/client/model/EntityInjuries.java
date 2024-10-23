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

    public List<NativeImage> availableSmallInjuries = new ArrayList<>();
    public List<NativeImage> availableMediumInjuries = new ArrayList<>();
    public List<NativeImage> availableLargeInjuries = new ArrayList<>();

    public Map<NativeImage, String> appliedSmallInjuries = new HashMap<>();
    public Map<NativeImage, String> appliedMediumInjuries = new HashMap<>();
    public Map<NativeImage, String> appliedLargeInjuries = new HashMap<>();

    public EntityInjuries(String entityName) {
        String modifiedEntityName = (entityName.equals("player")) ? entityName : BloodyBitsUtils.decompose(entityName, ':')[1];
        String path = "textures/entity/" + modifiedEntityName + "/";

        this.addEntityDamageTexture(path + "small_injuries/");
        this.addEntityDamageTexture(path + "medium_injuries/");
        this.addEntityDamageTexture(path + "large_injuries/");
    }

    private void addEntityDamageTexture(String path) {

        for (int i = 0; i < ClientConfig.availableTexturesPerEntity(); i++) {
            String modifiedPath = path.concat(i + ".png");
            try {
                ResourceLocation injuryTextureResourceLocation = new ResourceLocation(BloodyBitsMod.MODID, modifiedPath);
                NativeImage damageLayerTexture = NativeImage.read(Minecraft.getInstance().getResourceManager().open(injuryTextureResourceLocation));

                // Doing all paint logic. Currently, that means painting the blood and (if applicable)
                // burn overlay.
                BloodyBitsMod.LOGGER.info("Painting texture {}", modifiedPath);

                if (path.contains("small")) {
                    this.availableSmallInjuries.add(damageLayerTexture);
                }
                else if (path.contains("medium")) {
                    this.availableMediumInjuries.add(damageLayerTexture);
                }
                else if (path.contains("large")) {
                    this.availableLargeInjuries.add(damageLayerTexture);
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

        if (this.availableLargeInjuries.isEmpty() && this.largeBleedHits >= 1) {
            this.mediumBleedHits += (this.largeBleedHits * 2);
            this.largeBleedHits = 0;
        }

        if (this.availableSmallInjuries.isEmpty() && this.smallBleedHits >= 3) {
            this.mediumBleedHits += (this.smallBleedHits / 3);
            this.smallBleedHits = 0;
        }

        if (this.availableMediumInjuries.isEmpty()) {
            if (!this.availableLargeInjuries.isEmpty() && this.mediumBleedHits >= 2) {
                this.largeBleedHits += (this.mediumBleedHits / 2);
                this.mediumBleedHits = 0;
            }
            else if (!this.availableSmallInjuries.isEmpty() && this.mediumBleedHits >= 1) {
                this.smallBleedHits += (this.mediumBleedHits * 2);
                this.mediumBleedHits = 0;
            }
        }

        if (this.availableLargeInjuries.isEmpty() && this.largeBurnHits >= 1) {
            this.mediumBurnHits += (this.largeBurnHits * 2);
            this.largeBurnHits = 0;
        }

        if (this.availableSmallInjuries.isEmpty() && this.smallBurnHits >= 3) {
            this.mediumBurnHits += (this.smallBurnHits / 3);
            this.smallBurnHits = 0;
        }

        if (this.availableMediumInjuries.isEmpty()) {
            if (!this.availableLargeInjuries.isEmpty() && this.mediumBurnHits >= 2) {
                this.largeBurnHits += (this.mediumBurnHits / 2);
                this.mediumBurnHits = 0;
            }
            else if (!this.availableSmallInjuries.isEmpty() && this.mediumBurnHits >= 1) {
                this.smallBurnHits += (this.mediumBurnHits * 2);
                this.mediumBurnHits = 0;
            }
        }

        this.updateInjuries(injuryType);
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

        if (this.appliedLargeInjuries.isEmpty() && this.largeHealAmount >= 1) {
            this.mediumHealAmount += (this.largeHealAmount * 2);
            this.largeHealAmount = 0;
        }

        if (this.appliedSmallInjuries.isEmpty() && this.smallHealAmount >= 3) {
            this.mediumHealAmount += (this.smallHealAmount / 3);
            this.smallHealAmount = 0;
        }

        if (this.appliedMediumInjuries.isEmpty()) {
            if (!this.appliedLargeInjuries.isEmpty() && this.mediumHealAmount >= 2) {
                this.largeHealAmount += (this.mediumHealAmount / 2);
                this.mediumHealAmount = 0;
            }
            else if (!this.appliedSmallInjuries.isEmpty() && this.mediumHealAmount >= 1) {
                this.smallHealAmount += (this.mediumHealAmount * 2);
                this.mediumHealAmount = 0;
            }
        }

        this.updateHealInjuries();
    }

    private void updateHealInjuries() {
        BloodyBitsMod.LOGGER.info("----------------------------------------------------------------------------------");
        BloodyBitsMod.LOGGER.info("BEFORE healing SMALL injuries heal amount: {}. Small injuries: {}", this.smallHealAmount, this.appliedSmallInjuries.size());
        BloodyBitsMod.LOGGER.info("BEFORE healing MEDIUM injuries heal amount: {}. Medium injuries: {}", this.mediumHealAmount, this.appliedMediumInjuries.size());
        BloodyBitsMod.LOGGER.info("BEFORE healing LARGE injuries heal amount: {}. Large injuries: {}", this.largeHealAmount, this.appliedLargeInjuries.size());
        BloodyBitsMod.LOGGER.info("----------------------------------------------------------------------------------");

        if (this.largeHealAmount >= 1) {
            this.healInjuries(this.availableLargeInjuries, this.appliedLargeInjuries);
            this.largeHealAmount--;
        }

        if (this.mediumHealAmount >= 2) {
            this.healInjuries(this.availableMediumInjuries, this.appliedMediumInjuries);
            this.mediumHealAmount -= 2;
        }

        if (this.smallHealAmount >= 3) {
            this.healInjuries(this.availableSmallInjuries, this.appliedSmallInjuries);
            this.smallHealAmount -= 3;
        }

        BloodyBitsMod.LOGGER.info("----------------------------------------------------------------------------------");
        BloodyBitsMod.LOGGER.info("AFTER healing SMALL injuries heal amount: {}. Small injuries: {}", this.smallHealAmount, this.appliedSmallInjuries.size());
        BloodyBitsMod.LOGGER.info("AFTER healing MEDIUM injuries heal amount: {}. Medium injuries: {}", this.mediumHealAmount, this.appliedMediumInjuries.size());
        BloodyBitsMod.LOGGER.info("AFTER healing LARGE injuries heal amount: {}. Large injuries: {}", this.largeHealAmount, this.appliedLargeInjuries.size());
        BloodyBitsMod.LOGGER.info("----------------------------------------------------------------------------------");
    }

    private void healInjuries(List<NativeImage> availableSizeInjuries, Map<NativeImage, String> injurySizeMap) {
        if (!injurySizeMap.isEmpty()) {
            NativeImage firstInjury = injurySizeMap.entrySet().stream().findFirst().get().getKey();

            // Honestly, will probably never be null.
            if (firstInjury != null) {
                availableSizeInjuries.add(firstInjury);
                injurySizeMap.remove(firstInjury);
            }

        }
    }

    /**
     * TODO: Maybe just break this down into the specific addHit methods for each damage size?
     *       Could simplify things & make this more readable.
     * Updates all injuries if the entity has sustained enough hits of the given damage type & size.
     */
    private void updateInjuries(String injuryType) {
        BloodyBitsMod.LOGGER.info("----------------------------------------------------------------------------------");
        BloodyBitsMod.LOGGER.info("Small BLEED hit BEFORE updating bleed injuries: {} Small bleed injuries: {}", this.smallBleedHits, this.appliedSmallInjuries.size());
        BloodyBitsMod.LOGGER.info("Medium BLEED hit BEFORE updating bleed injuries: {} Medium bleed injuries: {}", this.mediumBleedHits, this.appliedMediumInjuries.size());
        BloodyBitsMod.LOGGER.info("Large BLEED hit BEFORE updating bleed injuries: {} Large bleed injuries: {}", this.largeBleedHits, this.appliedLargeInjuries.size());
        BloodyBitsMod.LOGGER.info("----------------------------------------------------------------------------------");
        BloodyBitsMod.LOGGER.info("Small BURN hit BEFORE updating burn injuries: {} Small burn injuries: {}", this.smallBurnHits, this.appliedSmallInjuries.size());
        BloodyBitsMod.LOGGER.info("Medium BURN hit BEFORE updating burn injuries: {} Medium burn injuries: {}", this.mediumBurnHits, this.appliedMediumInjuries.size());
        BloodyBitsMod.LOGGER.info("Large BURN hit BEFORE updating burn injuries: {} Large burn injuries: {}", this.largeBurnHits, this.appliedLargeInjuries.size());
        BloodyBitsMod.LOGGER.info("----------------------------------------------------------------------------------");

        // Update all bleed injuries.
        if (this.largeBleedHits >= 1) {
            this.updateInjuriesList(injuryType, this.availableLargeInjuries, this.appliedLargeInjuries);
            this.largeBleedHits--;
        }
        else if (this.mediumBleedHits >= 2) {
            this.updateInjuriesList(injuryType, this.availableMediumInjuries, this.appliedMediumInjuries);
            this.mediumBleedHits -= 2;
        }
        else if (this.smallBleedHits >= 3) {
            this.updateInjuriesList(injuryType, this.availableSmallInjuries, this.appliedSmallInjuries);
            this.smallBleedHits -= 3;
        }

        // Update all burn injuries.
        if (this.largeBurnHits >= 1) {
            this.updateInjuriesList(injuryType, this.availableLargeInjuries, this.appliedLargeInjuries);
            this.largeBurnHits--;
        }
        else if (this.mediumBurnHits >= 2) {
            this.updateInjuriesList(injuryType, this.availableMediumInjuries, this.appliedMediumInjuries);
            this.mediumBurnHits -= 2;
        }
        else if (this.smallBurnHits >= 3) {
            this.updateInjuriesList(injuryType, this.availableSmallInjuries, this.appliedSmallInjuries);
            this.smallBurnHits -= 3;
        }

        BloodyBitsMod.LOGGER.info("----------------------------------------------------------------------------------");
        BloodyBitsMod.LOGGER.info("Small BLEED hit AFTER updating bleed injuries: {} Small bleed injuries: {}", this.smallBleedHits, this.appliedSmallInjuries.size());
        BloodyBitsMod.LOGGER.info("Medium BLEED hit AFTER updating bleed injuries: {} Medium bleed injuries: {}", this.mediumBleedHits, this.appliedMediumInjuries.size());
        BloodyBitsMod.LOGGER.info("Large BLEED hit AFTER updating bleed injuries: {} Large bleed injuries: {}", this.largeBleedHits, this.appliedLargeInjuries.size());
        BloodyBitsMod.LOGGER.info("----------------------------------------------------------------------------------");
        BloodyBitsMod.LOGGER.info("Small BURN hit AFTER updating burn injuries: {} Small burn injuries: {}", this.smallBurnHits, this.appliedSmallInjuries.size());
        BloodyBitsMod.LOGGER.info("Medium BURN hit AFTER updating burn injuries: {} Medium burn injuries: {}", this.mediumBurnHits, this.appliedMediumInjuries.size());
        BloodyBitsMod.LOGGER.info("Large BURN hit AFTER updating burn injuries: {} Large burn injuries: {}", this.largeBurnHits, this.appliedLargeInjuries.size());
        BloodyBitsMod.LOGGER.info("----------------------------------------------------------------------------------");
    }

    private void updateInjuriesList(String injuryType, List<NativeImage> availableSizeInjuries, Map<NativeImage, String> injurySizeMap) {

        if (!availableSizeInjuries.isEmpty()) {
            int randomIndex = new Random().nextInt(availableSizeInjuries.size());
            NativeImage randomInjury = availableSizeInjuries.get(randomIndex);

            injurySizeMap.put(randomInjury, injuryType);
            availableSizeInjuries.remove(randomIndex);
        }
    }
}
