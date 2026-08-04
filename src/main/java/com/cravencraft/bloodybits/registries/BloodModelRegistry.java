package com.cravencraft.bloodybits.registries;

import com.cravencraft.bloodybits.model.BloodModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BloodModelRegistry {
    private static final List<BloodModel> BLOOD_MODELS = new ArrayList<>();

    public static void register(BloodModel bloodModel) {
        BLOOD_MODELS.add(bloodModel);
    }

    public static void load(List<BloodModel> bloodModels) {
        BLOOD_MODELS.clear();
        BLOOD_MODELS.addAll(bloodModels);
    }

    public static List<BloodModel> getBloodModels() {
        return Collections.unmodifiableList(BLOOD_MODELS);
    }
}
