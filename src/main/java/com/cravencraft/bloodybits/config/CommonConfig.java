package com.cravencraft.bloodybits.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

import java.util.List;


public class CommonConfig {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    private static ForgeConfigSpec.IntValue DESPAWN_TIME;
    private static ForgeConfigSpec.IntValue MAX_SPATTERS;
    private static ForgeConfigSpec.IntValue DISTANCE_TO_PLAYERS;

    private static ForgeConfigSpec.ConfigValue<List<?>> NO_BLOOD_MOBS;


    public static int despawnTime() {
        return DESPAWN_TIME.get();
    }

    public static int maxSpatters() {
        return MAX_SPATTERS.get();
    }

    public static int distanceToPlayers() { return DISTANCE_TO_PLAYERS.get(); }
    public static List<?> noBloodMobs() { return NO_BLOOD_MOBS.get(); }

    public static void loadCommonConfig() {
        BUILDER.push("blood spray settings");

        DESPAWN_TIME = BUILDER.comment("How long in ticks (20 ticks = 1 second) until a blood spatter despawns.")
                .defineInRange("despawn_time", 1000, 0, 100000);
        MAX_SPATTERS = BUILDER.comment("The maximum amount of blood spatters that can exist in the world at once.")
                .defineInRange("max_spatters", 100, 0, 10000);
        DISTANCE_TO_PLAYERS = BUILDER.comment("The maximum amount of distance a player can be away from a damaged entity for blood to spray.")
                .defineInRange("distance_to_players", 100, 0, 1000);
        NO_BLOOD_MOBS = BUILDER.comment("Define what mobs won't have blood. This is mainly skeletons. Instead of bleeding, they will just shoot out colored rectangles that disappear when they hit a surface," +
                        "and instead of getting bloodier when damaged, they will lose pixels.")
                .defineListAllowEmpty("no_blood_mobs",
                        List.of("minecraft:skeleton", "minecraft:skeleton_horse", "minecraft:wither_skeleton", "minecraft:wither", "minecraft:shulker"),
                        it -> it instanceof List<?>);

        BUILDER.pop();

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, BUILDER.build());
    }
}
