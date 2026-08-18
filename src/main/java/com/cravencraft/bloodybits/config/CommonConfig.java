package com.cravencraft.bloodybits.config;

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.List;

public class CommonConfig {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> BLACKLIST_DAMAGE_SOURCES;
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> BLOOD_MIST_DAMAGE_SOURCES;

    public static List<? extends String> blackListDamageSources() { return BLACKLIST_DAMAGE_SOURCES.get(); }
    public static List<? extends String> bloodMistDamageSources() { return BLOOD_MIST_DAMAGE_SOURCES.get(); }

    static {
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
