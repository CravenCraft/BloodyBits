package com.cravencraft.bloodybits.config;

import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

public class ClientConfig {
    public static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    private static final String BURN_DAMAGE_COLOR = "#323232";


    private static ModConfigSpec.ConfigValue<List<? extends String>> BURN_DAMAGE_SOURCE;
    private static ModConfigSpec.ConfigValue<List<? extends String>> BLACKLIST_INJURY_SOURCES;


    private static ModConfigSpec.BooleanValue SHOW_MOB_DAMAGE;
    private static ModConfigSpec.IntValue AVAILABLE_TEXTURES_PER_ENTITY;
    private static ModConfigSpec.IntValue BLOOD_SPATTER_LIFETIME;

    public static boolean showEntityDamage() { return false; }

    public static int availableTexturesPerEntity() { return AVAILABLE_TEXTURES_PER_ENTITY.get(); }
    public static int getBloodSpatterLifeTime() { return BLOOD_SPATTER_LIFETIME.get(); }

    public static List<? extends String> burnDamageSources() { return BURN_DAMAGE_SOURCE.get(); }
    public static List<? extends String> blackListInjurySources() { return BLACKLIST_INJURY_SOURCES.get(); }
    public static String getBurnDamageColor() { return BURN_DAMAGE_COLOR; }

    public static final ModConfigSpec SPEC;

    static {
        BUILDER.push("blood_spray_settings");

        SHOW_MOB_DAMAGE = BUILDER.comment("Whether or not an entity should show injury textures when damaged.")
                .define("show_entity_damage", false);

        AVAILABLE_TEXTURES_PER_ENTITY = BUILDER.comment("The maximum amount of available injury textures permitted per entity.\n" +
                        "Resource packs can be created to add additional textures for entities, override existing textures, or to\n" +
                        "even create textures for entities that have none (only applies when show_entity_damage is true).")
                .defineInRange("available_textures_per_entity", 25, 0, 100);

        BLOOD_SPATTER_LIFETIME = BUILDER.comment("The maximum lifetime (in ticks) that a blood spatter will remain on screen for.")
                .defineInRange("blood_spatter_lifetime", 600, 0, 10000);

        BURN_DAMAGE_SOURCE = BUILDER.comment("List of the damage sources that will display burn damage for the entities (only applies when show_entity_damage is true).")
                .defineListAllowEmpty("burn_damage_sources",
                        List.of("burn", "fireball", "fireworks", "lava", "hotFloor", "onFire", "inFire", "lightningBolt"),
                        () -> "",
                        it -> it instanceof String);

        BLACKLIST_INJURY_SOURCES = BUILDER.comment("List of the damage sources that will not cause an entity to bleed.")
                .defineListAllowEmpty("blacklist_bleed_sources",
                        List.of("drown", "starve", "dryOut", "freeze", "fellOutOfWorld"),
                        () -> "",
                        it -> it instanceof String);

        BUILDER.pop();

        SPEC = BUILDER.build();
    }
}
