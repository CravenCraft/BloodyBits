package com.cravencraft.bloodybits.config;

import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

public class CommonConfig {
    public static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec SPEC;

    private static final ModConfigSpec.ConfigValue<Boolean> DAMAGE_TYPE_DEBUG;
    private static final ModConfigSpec.IntValue BLOOD_SPRAY_DAMAGE_CAP;
    private static final ModConfigSpec.IntValue BLOOD_SPRAY_MAX_PER_HIT;
    private static final ModConfigSpec.ConfigValue<List<? extends String>> BLACKLIST_DAMAGE_SOURCES;
    private static final ModConfigSpec.ConfigValue<List<? extends String>> BLOOD_MIST_DAMAGE_SOURCES;

    public static boolean damageTypeDebug() {return DAMAGE_TYPE_DEBUG.get(); }
    public static int getBloodSprayDamageCap() { return BLOOD_SPRAY_DAMAGE_CAP.get(); }
    public static int getBloodSprayMaxPerHit() { return BLOOD_SPRAY_MAX_PER_HIT.get(); }
    public static List<? extends String> blackListDamageSources() { return BLACKLIST_DAMAGE_SOURCES.get(); }
    public static List<? extends String> bloodMistDamageSources() { return BLOOD_MIST_DAMAGE_SOURCES.get(); }

    static {
        DAMAGE_TYPE_DEBUG = BUILDER
                .comment("Enable this debug to view damage types inflicted on entities in the player chat." +
                        " Useful for determining the exact damage type to add to the BLACKLIST_DAMAGE_SOURCES " +
                        "and BLOOD_MIST_DAMAGE_SOURCES config lists.")
                .define("damage_type_debug", false);

        BLOOD_SPRAY_DAMAGE_CAP = BUILDER
                .comment("The damage cap for the amount of blood sprays produced per hit.")
                .defineInRange("blood_spray_damage_cap", 50, 10, 1000);

        BLOOD_SPRAY_MAX_PER_HIT = BUILDER
                .comment("The maximum amount of blood sprays per hit based on the 'Blood Spray Damage Cap'. " +
                        " The formula is (blood_spray_damage_cap / blood_spray_max_per_hit), which would mean " +
                        " at default there is an additional blood spray per 5 points of damage upwards to 50.")
                .defineInRange("blood_spray_max_per_hit", 10, 1, 100);

        BLACKLIST_DAMAGE_SOURCES = BUILDER.comment("List of the damage sources that will not cause an entity to bleed.")
                .defineListAllowEmpty("blacklist_bleed_sources",
                        List.of("drown", "starve", "dryOut", "freeze", "fellOutOfWorld",
                                "burn", "lava", "hotFloor", "onFire", "inFire"),
                        () -> "",
                        it -> it instanceof String);

        BLOOD_MIST_DAMAGE_SOURCES = BUILDER
                .comment("List of damage sources that will cause an entity to emit a blood mist when hit.")
                .defineListAllowEmpty("blood_mist_damage_sources",
                        List.of("arrow", "explosion.player", "tacz.bullet", "scguns.bullet"),
                        () -> "",
                        it -> it instanceof String);

        SPEC = BUILDER.build();
    }
}
