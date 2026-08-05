package com.cravencraft.bloodybits.config;

import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

public class CommonConfig {
    public static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.ConfigValue<List<? extends String>> BLACKLIST_INJURY_SOURCES;

    public static List<? extends String> blackListInjurySources() { return BLACKLIST_INJURY_SOURCES.get(); }

    public static final ModConfigSpec SPEC;

    static {
        BUILDER.push("blood_spray_settings");

        BLACKLIST_INJURY_SOURCES = BUILDER.comment("List of the damage sources that will not cause an entity to bleed.")
                .defineListAllowEmpty("blacklist_bleed_sources",
                        List.of("drown", "starve", "dryOut", "freeze", "fellOutOfWorld",
                                "burn", "lava", "hotFloor", "onFire", "inFire"),
                        () -> "",
                        it -> it instanceof String);

        BUILDER.pop();

        SPEC = BUILDER.build();
    }
}
