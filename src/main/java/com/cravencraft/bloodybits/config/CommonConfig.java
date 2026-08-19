package com.cravencraft.bloodybits.config;

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.List;

public class CommonConfig {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    private static final ForgeConfigSpec.ConfigValue<Boolean> DAMAGE_TYPE_DEBUG;
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> BLACKLIST_DAMAGE_SOURCES;
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> BLOOD_MIST_DAMAGE_SOURCES;

    public static boolean damageTypeDebug() {return DAMAGE_TYPE_DEBUG.get(); }
    public static List<? extends String> blackListDamageSources() { return BLACKLIST_DAMAGE_SOURCES.get(); }
    public static List<? extends String> bloodMistDamageSources() { return BLOOD_MIST_DAMAGE_SOURCES.get(); }

    static {
        DAMAGE_TYPE_DEBUG = BUILDER
                .comment("Enable this debug to view damage types inflicted on entities in the player chat." +
                        " Useful for determining the exact damage type to add to the BLACKLIST_DAMAGE_SOURCES " +
                        "and BLOOD_MIST_DAMAGE_SOURCES config lists.")
                .define("damageTypeDebug", false);

        BLACKLIST_DAMAGE_SOURCES = BUILDER.comment("List of the damage sources that will not cause an entity to bleed.")
                .defineListAllowEmpty("blacklist_bleed_sources",
                        List.of("drown", "starve", "dryOut", "freeze", "fellOutOfWorld",
                                "burn", "lava", "hotFloor", "onFire", "inFire"),
                        (string) -> true);

        BLOOD_MIST_DAMAGE_SOURCES = BUILDER
                .comment("List of damage sources that will cause an entity to emit a blood mist when hit.")
                .defineListAllowEmpty("blood_mist_damage_sources",
                        List.of("arrow", "explosion.player", "tacz.bullet", "scguns.bullet"),
                        (string) -> true);

        SPEC = BUILDER.build();
    }
}
