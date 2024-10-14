package com.cravencraft.bloodybits.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

import java.util.List;

public class CommonConfig {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    private static ForgeConfigSpec.BooleanValue BLEED_WHEN_DAMAGED;
    private static ForgeConfigSpec.IntValue DESPAWN_TIME;
    private static ForgeConfigSpec.IntValue MAX_SPATTERS;
    private static ForgeConfigSpec.DoubleValue BLOOD_SPRAY_DISTANCE;
    private static ForgeConfigSpec.DoubleValue BLOOD_SPATTER_VOLUME;

    private static ForgeConfigSpec.ConfigValue<List<? extends String>> SOLID_ENTITIES;
    private static ForgeConfigSpec.ConfigValue<List<? extends String>> BLACKLIST_ENTITIES;
    private static ForgeConfigSpec.ConfigValue<List<? extends String>> BLACKLIST_DAMAGE_SOURCES;

    public static boolean bleedWhenDamaged() { return BLEED_WHEN_DAMAGED.get(); }
    public static int despawnTime() {
        return DESPAWN_TIME.get();
    }

    public static int maxSpatters() { return MAX_SPATTERS.get(); }

    public static double bloodSpatterVolume() { return BLOOD_SPATTER_VOLUME.get(); }
    public static double bloodSprayDistance() { return BLOOD_SPRAY_DISTANCE.get(); }

    public static List<? extends String> solidEntities() { return SOLID_ENTITIES.get(); }
    public static List<? extends String> blackListEntities() { return BLACKLIST_ENTITIES.get(); }
    public static List<? extends String> blackListDamageSources() { return BLACKLIST_DAMAGE_SOURCES.get(); }

    public static void loadCommonConfig() {
        BUILDER.push("blood spray settings");

        DESPAWN_TIME = BUILDER.comment("How long in ticks (20 ticks = 1 second) until a blood spatter despawns.")
                .defineInRange("despawn_time", 2000, 0, 100000);
        BLEED_WHEN_DAMAGED = BUILDER.comment("Do entities bleed when damaged below 50% health. The more they are damaged, the more often they bleed.")
                .define("bleed_when_damaged", true);
        MAX_SPATTERS = BUILDER.comment("The maximum amount of blood spatters that can exist in the world at once.")
                .defineInRange("max_spatters", 500, 0, 10000);
        BLOOD_SPATTER_VOLUME = BUILDER.comment("How loud the blood spatters are.")
                .defineInRange("blood_spatter_volume", 0.75, 0, 1.0);
        BLOOD_SPRAY_DISTANCE = BUILDER.comment("How far blood will spray from an entity. Higher values mean blood sprays farther away.")
                .defineInRange("blood_spray_distance", 0.025, 0, 1.0);
        SOLID_ENTITIES = BUILDER.comment("Define what mobs 'bleed' solid bits. This is mainly skeletons. Instead of bleeding they will just shoot out colored bits," +
                        "and instead of getting bloodier when damaged, they will lose pixels.")
                .defineListAllowEmpty("solid_entities",
                        List.of("minecraft:skeleton", "minecraft:skeleton_horse", "minecraft:wither_skeleton", "minecraft:wither", "minecraft:shulker"),
                        it -> it instanceof String);
        BLACKLIST_ENTITIES = BUILDER.comment("Some mobs don't play nice with this mod, and may cause crashes. Define which mobs you want to blacklist here (enter 'player' for all players).")
                .defineListAllowEmpty("blacklist_entities",
                        List.of("alexsmobs:cachalot_whale"),
                        it -> it instanceof String);
        BLACKLIST_DAMAGE_SOURCES = BUILDER.comment("Define what damage sources will be blacklisted from producing blood sprays or explosions.")
                .defineListAllowEmpty("blacklist_damage_sources",
                        List.of("onFire", "inFire", "starve", "drown", "hotFloor", "dragonBreath", "dryOut", "freeze", "lava"),
                        it -> it instanceof String);

        BUILDER.pop();

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, BUILDER.build());
    }
}