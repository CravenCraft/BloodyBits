package com.cravencraft.bloodybits.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class ClientConfig {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    private static final ForgeConfigSpec.IntValue BLOOD_SPATTER_LIFETIME;
    private static final ForgeConfigSpec.DoubleValue BLOOD_SPATTER_SOUND_VOLUME;

    public static int getBloodSpatterLifeTime() { return BLOOD_SPATTER_LIFETIME.get(); }
    public static double bloodSpatterSoundVolume() { return BLOOD_SPATTER_SOUND_VOLUME.get(); }

    public static final ForgeConfigSpec SPEC;

    static {

        BLOOD_SPATTER_LIFETIME = BUILDER
                .comment("The maximum lifetime (20 ticks = 1 second) that a blood spatter will remain on screen for.")
                .defineInRange("blood_spatter_lifetime", 600, 0, 10000);

        BLOOD_SPATTER_SOUND_VOLUME = BUILDER
                .comment("How loud the blood spatters are.")
                .defineInRange("blood_spatter_sound_volume", 0.75, 0, 1.0);

        SPEC = BUILDER.build();
    }
}
