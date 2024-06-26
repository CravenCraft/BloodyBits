package com.cravencraft.bloodybits.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

import java.util.List;


public class CommonConfig {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    private static ForgeConfigSpec.BooleanValue SHOW_BLOOD_CHUNKS;
    private static ForgeConfigSpec.IntValue DESPAWN_TIME;
    private static ForgeConfigSpec.IntValue MAX_SPATTERS;
    private static ForgeConfigSpec.IntValue MAX_CHUNKS;
    private static ForgeConfigSpec.IntValue DISTANCE_TO_PLAYERS;

    private static ForgeConfigSpec.ConfigValue<List<? extends String>> SOLID_ENTITIES;

    public static boolean showBloodChunks() { return SHOW_BLOOD_CHUNKS.get(); }

    public static int despawnTime() {
        return DESPAWN_TIME.get();
    }

    public static int maxSpatters() {
        return MAX_SPATTERS.get();
    }

    public static int maxChunks() {
        return MAX_CHUNKS.get();
    }

    public static int distanceToPlayers() { return DISTANCE_TO_PLAYERS.get(); }

    public static List<? extends String> solidEntities() { return SOLID_ENTITIES.get(); }

    public static void loadCommonConfig() {
        BUILDER.push("blood spray settings");

        DESPAWN_TIME = BUILDER.comment("How long in ticks (20 ticks = 1 second) until a blood spatter despawns.")
                .defineInRange("despawn_time", 2000, 0, 100000);
        MAX_SPATTERS = BUILDER.comment("The maximum amount of blood spatters that can exist in the world at once.")
                .defineInRange("max_spatters", 2000, 0, 10000);

        SHOW_BLOOD_CHUNKS = BUILDER.comment("Whether or not blood chunks should replace the poof particles when an entity dies.")
                .define("show_blood_chunks", true);
        MAX_CHUNKS = BUILDER.comment("The maximum amount of blood chunks that can exist in the world at once.")
                .defineInRange("max_chunks", 1000, 0, 10000);
        DISTANCE_TO_PLAYERS = BUILDER.comment("The maximum amount of distance a player can be away from a damaged entity for blood to spray.")
                .defineInRange("distance_to_players", 100, 0, 1000);
        SOLID_ENTITIES = BUILDER.comment("Define what mobs 'bleed' solid bits. This is mainly skeletons. Instead of bleeding they will just shoot out colored bits," +
                        "and instead of getting bloodier when damaged, they will lose pixels.")
                .defineListAllowEmpty("solid_entities",
                        List.of("minecraft:skeleton", "minecraft:skeleton_horse", "minecraft:wither_skeleton", "minecraft:wither", "minecraft:shulker"),
                        it -> it instanceof String);

        BUILDER.pop();

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, BUILDER.build());
    }
}