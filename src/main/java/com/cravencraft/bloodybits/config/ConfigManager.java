package com.cravencraft.bloodybits.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class ConfigManager {
    public static CommonConfig COMMON_CONFIG;

    public static void registerCommonConfig() {
        ForgeConfigSpec.Builder client = new ForgeConfigSpec.Builder();
        COMMON_CONFIG = new CommonConfig(client);
    }
}