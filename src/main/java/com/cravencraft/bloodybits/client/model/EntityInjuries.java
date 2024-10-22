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

    public HashMap<NativeImage, String> smallInjuries = new HashMap<>();
    public HashMap<NativeImage, String> mediumInjuries = new HashMap<>();
    public HashMap<NativeImage, String> largeInjuries = new HashMap<>();

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
                BloodyBitsUtils.paintDamageToNativeImage(burnColorDamageLayer, BloodyBitsUtils.getMobDamageColor(entityName));
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

    /**
     *  Adds to the small hits for an entity, then updates the injuries in case the small hits pass the threshold
     *  to add a small injury to the entity.
     */
    public void addSmallHit(String injuryType) {
        switch (injuryType) {
            case "bleed" -> {
                if (this.smallBleedInjuries.isEmpty()) {
                    if (!this.mediumBleedInjuries.isEmpty()) {
                        if (this.smallBleedHits >= 3) {
                            this.mediumBleedHits++;
                            this.smallBleedHits = 0;
                        }
                        else {
                            this.smallBleedHits++;
                        }
                    }
                    else if (!this.largeBleedInjuries.isEmpty()) {
                        if (this.smallBleedHits >= 5) {
                            this.largeBleedHits++;
                            this.smallBleedHits = 0;
                        }
                        else {
                            this.smallBleedHits++;
                        }
                    }
                }
                else {
                    this.smallBleedHits++;
                }
            }
            case "burn" -> {
                if (this.smallBurnInjuries.isEmpty()) {
                    if (!this.mediumBurnInjuries.isEmpty()) {
                        if (this.smallBurnHits >= 3) {
                            this.mediumBurnHits++;
                            this.smallBurnHits = 0;
                        }
                        else {
                            this.smallBurnHits++;
                        }
                    }
                    else if (!this.largeBurnInjuries.isEmpty()) {
                        if (this.smallBurnHits >= 5) {
                            this.largeBurnHits++;
                            this.smallBurnHits = 0;
                        }
                        else {
                            this.smallBurnHits++;
                        }
                    }
                }
                else {
                    this.smallBurnHits++;
                }
            }
        }

        this.updateInjuries();
    }

    /**
     *  Adds to the medium hits for an entity, then updates the injuries in case the medium hits pass the threshold
     *  to add a medium injury to the entity.
     */
    public void addMediumHit(String injuryType) {
        switch (injuryType) {
            case "bleed" -> {
                if (this.mediumBleedInjuries.isEmpty()) {
                    if (!this.largeBleedInjuries.isEmpty()) {
                        if (this.mediumBleedHits >= 2) {
                            this.largeBleedHits++;
                            this.mediumBleedHits = 0;
                        }
                        else {
                            this.mediumBleedHits++;
                        }
                    }
                    else if (!this.smallBleedInjuries.isEmpty()) {
                        this.smallBleedHits += 2;
                    }
                }
                else {
                    this.mediumBleedHits++;
                }
            }
            case "burn" -> {
                if (this.mediumBurnInjuries.isEmpty()) {
                    if (!this.largeBurnInjuries.isEmpty()) {
                        if (this.mediumBurnHits >= 2) {
                            this.largeBurnHits++;
                            this.mediumBurnHits = 0;
                        }
                        else {
                            this.mediumBurnHits++;
                        }
                    }
                    else if (!this.smallBurnInjuries.isEmpty()) {
                        this.smallBurnHits += 2;
                    }
                }
                else {
                    this.mediumBurnHits++;
                }
            }
        }

        this.updateInjuries();
    }

    /**
     *  Adds to the large hits for an entity, then updates the injuries in case the large hits pass the threshold
     *  to add a large injury to the entity.
     */
    public void addLargeHit(String injuryType) {
        switch (injuryType) {
            case "bleed" -> {
                if (this.largeBleedInjuries.isEmpty()) {
                    if (!this.mediumBleedInjuries.isEmpty()) {
                        this.mediumBleedHits += 2;
                    }
                    else if (!this.smallBleedInjuries.isEmpty()) {
                        this.smallBleedHits += 5;
                    }
                }
                else {
                    this.largeBleedHits++;
                }
            }
            case "burn" -> {
                if (this.largeBurnInjuries.isEmpty()) {
                    if (!this.mediumBurnInjuries.isEmpty()) {
                        this.mediumBurnHits += 2;
                    }
                    else if (!this.smallBurnInjuries.isEmpty()) {
                        this.smallBurnHits += 5;
                    }
                }
                else {
                    this.largeBurnHits++;
                }
            }
        }

        this.updateInjuries();
    }

    public void addSmallHealAmount() {

        if (this.smallInjuries.isEmpty()) {
            if (!this.mediumInjuries.isEmpty()) {
                if (this.smallHealAmount >= 3) {
                    this.mediumHealAmount++;
                    this.smallHealAmount = 0;
                }
                else {
                    this.smallHealAmount++;
                }
            }
            else if (!this.largeInjuries.isEmpty()) {
                if (this.smallHealAmount >= 5) {
                    this.mediumHealAmount++;
                    this.smallHealAmount = 0;
                }
                else {
                    this.smallHealAmount++;
                }
            }
        }
        else {
            this.smallHealAmount++;
        }

        this.smallHealAmount++;
        this.updateHealInjuries();
    }

    public void addMediumHealAmount() {

        if (this.mediumInjuries.isEmpty()) {
            if (!this.largeInjuries.isEmpty()) {
                if (this.mediumHealAmount >= 2) {
                    this.largeHealAmount++;
                    this.mediumHealAmount = 0;
                }
                else {
                    this.mediumHealAmount++;
                }
            }
            else if (!this.smallInjuries.isEmpty()) {
                this.smallHealAmount += 3;
            }
        }
        else {
            this.mediumHealAmount++;
        }

        this.updateHealInjuries();
    }

    public void addLargeHealAmount() {

        if (this.largeInjuries.isEmpty()) {
            if (!this.mediumInjuries.isEmpty()) {
                this.mediumHealAmount += 2;
            }
            else if (!this.smallInjuries.isEmpty()) {
                this.smallHealAmount += 5;
            }
        }
        else {
            this.largeHealAmount++;
        }

        this.updateHealInjuries();
    }

    private void updateHealInjuries() {
//        BloodyBitsMod.LOGGER.info("----------------------------------------------------------------------------------");
//        BloodyBitsMod.LOGGER.info("BEFORE healing SMALL injuries heal amount: {}. Small injuries: {}", this.smallHealAmount, this.smallInjuries.size());
//        BloodyBitsMod.LOGGER.info("BEFORE healing MEDIUM injuries heal amount: {}. Medium injuries: {}", this.mediumHealAmount, this.mediumInjuries.size());
//        BloodyBitsMod.LOGGER.info("BEFORE healing LARGE injuries heal amount: {}. Large injuries: {}", this.largeHealAmount, this.largeInjuries.size());
//        BloodyBitsMod.LOGGER.info("----------------------------------------------------------------------------------");

        if (this.largeHealAmount >= 1) {
            this.healLargeInjuries();
        }
        else if (this.mediumHealAmount >= 2) {
            this.healMediumInjuries();
        }
        else if (this.smallHealAmount >= 3) {
            this.healSmallInjuries();
        }

//        BloodyBitsMod.LOGGER.info("----------------------------------------------------------------------------------");
//        BloodyBitsMod.LOGGER.info("AFTER healing SMALL injuries heal amount: {}. Small injuries: {}", this.smallHealAmount, this.smallInjuries.size());
//        BloodyBitsMod.LOGGER.info("AFTER healing MEDIUM injuries heal amount: {}. Medium injuries: {}", this.mediumHealAmount, this.mediumInjuries.size());
//        BloodyBitsMod.LOGGER.info("AFTER healing LARGE injuries heal amount: {}. Large injuries: {}", this.largeHealAmount, this.largeInjuries.size());
//        BloodyBitsMod.LOGGER.info("----------------------------------------------------------------------------------");
    }

    private void healLargeInjuries() {
        Map.Entry<NativeImage, String> injury = null;
        if (!this.largeInjuries.isEmpty()) {
            // Get the last entry in the HashMap.
            for (var integerNativeImageEntry : this.largeInjuries.entrySet()) {
                injury = integerNativeImageEntry;
            }

            if (injury != null) {
                if (injury.getValue().equals("bleed")) {
                    this.largeBleedInjuries.add(injury.getKey());
                }
                else if (injury.getValue().equals("burn")) {
                    this.largeBurnInjuries.add(injury.getKey());
                }

                this.largeInjuries.remove(injury.getKey(), injury.getValue());
            }
        }
        else {
            this.healMediumInjuries();
        }

        this.largeHealAmount--;
    }

    private void healMediumInjuries() {
        Map.Entry<NativeImage, String> injury = null;
        if (!this.mediumInjuries.isEmpty()) {
            for (var integerNativeImageEntry : this.mediumInjuries.entrySet()) {
                injury = integerNativeImageEntry;
            }

            if (injury != null) {
                if (injury.getValue().equals("bleed")) {
                    this.mediumBleedInjuries.add(injury.getKey());
                }
                else if (injury.getValue().equals("burn")) {
                    this.mediumBurnInjuries.add(injury.getKey());
                }

                this.mediumInjuries.remove(injury.getKey(), injury.getValue());
            }
        }
        else {
            this.healSmallInjuries();
        }

        this.mediumHealAmount -= 2;
    }

    private void healSmallInjuries() {
        Map.Entry<NativeImage, String> injury = null;
        if (!this.smallInjuries.isEmpty()) {
            for (var integerNativeImageEntry : this.smallInjuries.entrySet()) {
                injury = integerNativeImageEntry;
            }

            if (injury != null) {
                if (injury.getValue().equals("bleed")) {
                    this.smallBleedInjuries.add(injury.getKey());
                }
                else if (injury.getValue().equals("burn")) {
                    this.smallBurnInjuries.add(injury.getKey());
                }

                this.smallInjuries.remove(injury.getKey(), injury.getValue());
            }
        }

        this.smallHealAmount -= 3;
    }

    /**
     * TODO: Maybe just break this down into the specific addHit methods for each damage size?
     *       Could simplify things & make this more readable.
     * Updates all injuries if the entity has sustained enough hits of the given damage type & size.
     */
    private void updateInjuries() {
//        BloodyBitsMod.LOGGER.info("----------------------------------------------------------------------------------");
//        BloodyBitsMod.LOGGER.info("Small BLEED hit BEFORE updating bleed injuries: {} Small bleed injuries: {}", this.smallBleedHits, this.smallInjuries.size());
//        BloodyBitsMod.LOGGER.info("Medium BLEED hit BEFORE updating bleed injuries: {} Medium bleed injuries: {}", this.mediumBleedHits, this.mediumInjuries.size());
//        BloodyBitsMod.LOGGER.info("Large BLEED hit BEFORE updating bleed injuries: {} Large bleed injuries: {}", this.largeBleedHits, this.largeInjuries.size());
//        BloodyBitsMod.LOGGER.info("----------------------------------------------------------------------------------");

        // Update all bleed injuries.
        if (this.largeBleedHits >= 1) {
            this.updateLargeBleedInjuries();
        }
        else if (this.mediumBleedHits >= 2) {
            this.updateMediumBleedInjuries();
        }
        else if (this.smallBleedHits >= 3) {
            this.updateSmallBleedInjuries();
        }

        // Update all burn injuries.
        if (this.largeBurnHits >= 2) {
            this.updateLargeBurnInjuries();
        }
        else if (this.mediumBurnHits >= 3) {
            this.updateMediumBurnInjuries();
        }
        else if (this.smallBurnHits >= 5) {
            this.updateSmallBurnInjuries();
        }

//        BloodyBitsMod.LOGGER.info("----------------------------------------------------------------------------------");
//        BloodyBitsMod.LOGGER.info("Small BLEED hit AFTER updating bleed injuries: {} Small bleed injuries: {}", this.smallBleedHits, this.smallInjuries.size());
//        BloodyBitsMod.LOGGER.info("Medium BLEED hit AFTER updating bleed injuries: {} Medium bleed injuries: {}", this.mediumBleedHits, this.mediumInjuries.size());
//        BloodyBitsMod.LOGGER.info("Large BLEED hit AFTER updating bleed injuries: {} Large bleed injuries: {}", this.largeBleedHits, this.largeInjuries.size());
//        BloodyBitsMod.LOGGER.info("----------------------------------------------------------------------------------");
    }

    private void updateLargeBleedInjuries() {
        int randomIndex;
        NativeImage randomInjury;

        if (!this.largeBleedInjuries.isEmpty()) {
            randomIndex = new Random().nextInt(this.largeBleedInjuries.size());
            randomInjury = this.largeBleedInjuries.get(randomIndex);

            this.largeBleedInjuries.remove(randomIndex);
            this.largeInjuries.put(randomInjury, "bleed");
        }
        else {
            this.updateMediumBleedInjuries();
        }

        this.largeBleedHits--;
    }

    private void updateLargeBurnInjuries() {
        int randomIndex;
        NativeImage randomInjury;

        if (!this.largeBurnInjuries.isEmpty()) {
            randomIndex = new Random().nextInt(this.largeBurnInjuries.size());
            randomInjury = this.largeBurnInjuries.get(randomIndex);

            this.largeBurnInjuries.remove(randomIndex);
            this.largeInjuries.put(randomInjury, "burn");
        }
        else {
            this.updateMediumBurnInjuries();
        }

        this.largeBurnHits--;
    }

    private void updateMediumBleedInjuries() {
        int randomIndex;
        NativeImage randomInjury;

        if (!this.mediumBleedInjuries.isEmpty()) {
            randomIndex = new Random().nextInt(this.mediumBleedInjuries.size());
            randomInjury = this.mediumBleedInjuries.get(randomIndex);

            this.mediumBleedInjuries.remove(randomIndex);
            this.mediumInjuries.put(randomInjury, "bleed");
        }
        else {
            this.updateSmallBleedInjuries();
        }

        this.mediumBleedHits -= 2;
    }

    private void updateMediumBurnInjuries() {
        int randomIndex;
        NativeImage randomInjury;

        if (!this.mediumBurnInjuries.isEmpty()) {
            randomIndex = new Random().nextInt(this.mediumBurnInjuries.size());
            randomInjury = this.mediumBurnInjuries.get(randomIndex);

            this.mediumBurnInjuries.remove(randomIndex);
            this.mediumInjuries.put(randomInjury, "burn");
        }
        else {
            this.updateSmallBurnInjuries();
        }

        this.mediumBurnHits -= 2;
    }

    private void updateSmallBleedInjuries() {
        int randomIndex;
        NativeImage randomInjury;

        if (!this.smallBleedInjuries.isEmpty()) {
            randomIndex = new Random().nextInt(this.smallBleedInjuries.size());
            randomInjury = this.smallBleedInjuries.get(randomIndex);

            this.smallInjuries.put(randomInjury, "bleed");
            this.smallBleedInjuries.remove(randomIndex);
        }

        this.smallBleedHits -= 3;
    }

    private void updateSmallBurnInjuries() {
        int randomIndex;
        NativeImage randomInjury;

        if (!this.smallBurnInjuries.isEmpty()) {
            randomIndex = new Random().nextInt(this.smallBurnInjuries.size());
            randomInjury = this.smallBurnInjuries.get(randomIndex);

            this.smallBurnInjuries.remove(randomIndex);
            this.smallInjuries.put(randomInjury, "burn");
        }

        this.smallBurnHits -= 3;
    }
}
