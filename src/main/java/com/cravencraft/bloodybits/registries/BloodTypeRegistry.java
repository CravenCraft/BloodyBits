package com.cravencraft.bloodybits.registries;

import com.cravencraft.bloodybits.model.BloodType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BloodTypeRegistry {
    private static final List<BloodType> BLOOD_TYPES = new ArrayList<>();

    public static void register(BloodType bloodType) {
        BLOOD_TYPES.add(bloodType);
    }

    public static void load(List<BloodType> bloodTypes) {
        BLOOD_TYPES.clear();
        BLOOD_TYPES.addAll(bloodTypes);
    }

    public static List<BloodType> getBloodTypes() {
        return Collections.unmodifiableList(BLOOD_TYPES);
    }
}
