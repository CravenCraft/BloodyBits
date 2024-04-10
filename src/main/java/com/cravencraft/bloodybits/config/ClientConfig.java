package com.cravencraft.bloodybits.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import java.util.List;


public class ClientConfig {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    private static ForgeConfigSpec.ConfigValue<List<? extends List<?>>> MOB_BLOOD_TYPES;

    public static List<? extends List<?>> mobBloodTypes() { return MOB_BLOOD_TYPES.get(); }

    public static void loadClientConfig() {
        BUILDER.push("blood spray settings");

        MOB_BLOOD_TYPES = BUILDER.comment("Define what color the blood from certain mobs should be. If a mob isn't listed, then it'll default to red.")
                .defineListAllowEmpty("blood_colors", List.of(
                        List.of("minecraft:spider, minecraft:cave_spider, minecraft:creeper, minecraft:bee, minecraft:slime", "#01c801"),
                        List.of("minecraft:enderman, minecraft:shulker, minecraft:ender_dragon, minecraft:endermite", "#c832ff"),
                        List.of("minecraft:skeleton", "#c8c8c8"),
                        List.of("minecraft:wither_skeleton, minecraft:wither", "#323232")
                        ), it -> it instanceof List<?> list &&
                                list.size() == 2 &&
                                list.get(0) instanceof List<?> entityList &&
                                entityList.get(0) instanceof String &&
                                list.get(1) instanceof String);

        BUILDER.pop();
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, BUILDER.build());
    }
}
