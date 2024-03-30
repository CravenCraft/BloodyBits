package com.cravencraft.bloodybits.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;


public class CommonConfig {
//    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    private static ForgeConfigSpec.IntValue DESPAWN_TIME;

    public static int despawnTime() {
        return DESPAWN_TIME.get();
    }

//    static {
//
//    }

    public CommonConfig(ForgeConfigSpec.Builder client) {
        client.push("blood spray settings");

        DESPAWN_TIME = client.comment("How long in ticks (20 ticks = 1 second) until a blood spatter despawns.")
                .defineInRange("despawn_time", 1000, 0, 100000);
        client.pop();

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, client.build());
    }
}
