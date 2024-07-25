package com.cravencraft.bloodybits.config;

import com.cravencraft.bloodybits.BloodyBitsMod;
import com.google.common.reflect.TypeToken;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;


public class ClientConfig {
    public static final Gson GSON = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).setPrettyPrinting().create();

    private static final String BLOOD_BLACK = "#323232";
    private static final String BLOOD_BLUE = "#2acbf7";
    private static final String BLOOD_GREEN = "#01c801";
    private static final String BLOOD_GREY = "#c8c8c8";
    private static final String BLOOD_PURPLE = "#c832ff";
    private static final String BLOOD_ORANGE = "#fac832";

    private static final List<String> BLOOD_BLACK_MOBS = List.of("minecraft:wither_skeleton", "minecraft:wither");
    private static final List<String> BLOOD_BLUE_MOBS = List.of("minecraft:allay", "minecraft:warden");
    private static final List<String> BLOOD_GREEN_MOBS = List.of("minecraft:spider", "minecraft:cave_spider", "minecraft:creeper", "minecraft:bee", "minecraft:slime");
    private static final List<String> BLOOD_GREY_MOBS = List.of("minecraft:skeleton", "minecraft:skeleton_horse", "minecraft:snow_golem", "minecraft:shulker");
    private static final List<String> BLOOD_PURPLE_MOBS = List.of("minecraft:enderman", "minecraft:shulker", "minecraft:ender_dragon", "minecraft:endermite");
    private static final List<String> BLOOD_ORANGE_MOBS = List.of("minecraft:magma_cube", "minecraft:blaze");
    private static final HashMap<String, List<String>> DEFAULT_MOB_BLOOD_COLORS = new HashMap<>();
    private static HashMap<String, List<String>> MOB_BLOOD_COLORS;

    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    private static ForgeConfigSpec.BooleanValue SHOW_MOB_DAMAGE;
    private static ForgeConfigSpec.IntValue ENTITY_DAMAGE_SHOWN_PERCENT;

    public static boolean showMobDamage() { return SHOW_MOB_DAMAGE.get(); }
    public static int entityDamageShownPercent() { return ENTITY_DAMAGE_SHOWN_PERCENT.get(); }
    public static HashMap<String, List<String>> mobBloodColors() { return MOB_BLOOD_COLORS; }

    public static void loadClientConfig() {
        BUILDER.push("blood spray settings");

        SHOW_MOB_DAMAGE = BUILDER.comment("Whether or not an entity should show textured damage when hit.")
                .define("show_mob_damage", false);

        ENTITY_DAMAGE_SHOWN_PERCENT = BUILDER.comment("What percentage of an entity should be covered in blood based on its current damage.")
                .defineInRange("entity_damage_percent", 75, 0, 100);

        BUILDER.pop();

        DEFAULT_MOB_BLOOD_COLORS.put(BLOOD_BLACK, BLOOD_BLACK_MOBS);
        DEFAULT_MOB_BLOOD_COLORS.put(BLOOD_BLUE, BLOOD_BLUE_MOBS);
        DEFAULT_MOB_BLOOD_COLORS.put(BLOOD_GREEN, BLOOD_GREEN_MOBS);
        DEFAULT_MOB_BLOOD_COLORS.put(BLOOD_GREY, BLOOD_GREY_MOBS);
        DEFAULT_MOB_BLOOD_COLORS.put(BLOOD_PURPLE, BLOOD_PURPLE_MOBS);
        DEFAULT_MOB_BLOOD_COLORS.put(BLOOD_ORANGE, BLOOD_ORANGE_MOBS);

        MOB_BLOOD_COLORS = getConfigData(DEFAULT_MOB_BLOOD_COLORS);

        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, BUILDER.build());
    }

    private static <T> T getOrCreateConfigFile(File configDir, String configName, T defaults, Type type) {
        File configFile = new File(configDir, configName + ".json");

        if (!configFile.exists()) {
            try {
                FileUtils.write(configFile, GSON.toJson(defaults));
            }
            catch (IOException e) {
                BloodyBitsMod.LOGGER.error("Bloody Bits color config file could not be written.");
            }
        }

        try {
            T found = GSON.fromJson(FileUtils.readFileToString(configFile), type);

//            if (isInvalid.test(found)) {
//                BloodyBitsMod.LOGGER.error("Old blood colors found for {}, replacing with new one.", configName);
//                try {
//                    FileUtils.write(configFile, GSON.toJson(defaults));
//                }
//                catch (IOException e) {
//                    BloodyBitsMod.LOGGER.error("Bloody Bits color config file could not be written.");
//                }
//            }
//            else {
                return found;
//            }
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }

//        return defaults;
    }

    private static File getConfigDirectory() {
        Path configPath = FMLPaths.CONFIGDIR.get();
        Path jsonPath = Paths.get(configPath.toAbsolutePath().toString(), "bloodybits_colors");
        return jsonPath.toFile();
    }

    private static HashMap<String, List<String>> getConfigData(HashMap<String, List<String>> defaultConfigData) {
        return getOrCreateConfigFile(getConfigDirectory(), "test_blood_mobs", defaultConfigData, new TypeToken<HashMap<String, List<String>>>(){}.getType());
    }
}
