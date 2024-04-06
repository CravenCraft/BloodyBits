package com.cravencraft.bloodybits.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import java.util.Arrays;
import java.util.List;


public class ClientConfig {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    private static ForgeConfigSpec.ConfigValue<List<? extends List<?>>> MOB_BLOOD_TYPES;

    public static List<? extends List<?>> mobBloodTypes() { return MOB_BLOOD_TYPES.get(); }

    public static void loadClientConfig() {
        BUILDER.push("blood spray settings");

        MOB_BLOOD_TYPES = BUILDER.comment("Define what color the blood from certain mobs should be.")
                .defineListAllowEmpty("blood_test", List.of(
                        List.of(List.of("minecraft:cow", "minecraft:pillager"), 255, 0, 15)
                        ), it -> it instanceof List<?> list &&
                                list.size() == 4 &&
                                list.get(0) instanceof List entityList &&
                                entityList.get(0) instanceof String &&
                                list.get(1) instanceof Number &&
                                list.get(2) instanceof Number &&
                                list.get(3) instanceof Number);


        BUILDER.comment("How long in ticks (20 ticks = 1 second) until a blood spatter despawns.")
                .defineList("compressBlacklist", Arrays.asList("minecraft:sandstone", "minecraft:iron_trapdoor"), it -> it instanceof String);

        BUILDER.pop();

        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, BUILDER.build());
    }
}
