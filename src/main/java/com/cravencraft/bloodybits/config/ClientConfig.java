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
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;


public class ClientConfig {

    // TODO: Either here or in the common config add an option for blood explosion and spatter volume sounds.
    public static final Gson GSON = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).setPrettyPrinting().create();

    private static final String BLOOD_BLACK = "#323232";
    private static final String BLOOD_BLUE = "#2acbf7";
    private static final String BLOOD_GREEN = "#01c801";
    private static final String BLOOD_GREY = "#c8c8c8";
    private static final String BLOOD_PURPLE = "#c832ff";
    private static final String BLOOD_ORANGE = "#fac832";

    private static final List<String> BLOOD_BLACK_ENTITIES = List.of("minecraft:wither_skeleton", "minecraft:wither");
    private static final List<String> BLOOD_BLUE_ENTITIES = List.of("minecraft:allay", "minecraft:warden");
    private static final List<String> BLOOD_GREEN_ENTITIES = List.of("minecraft:spider", "minecraft:cave_spider", "minecraft:creeper", "minecraft:bee", "minecraft:slime");
    private static final List<String> BLOOD_GREY_ENTITIES = List.of("minecraft:skeleton", "minecraft:skeleton_horse", "minecraft:snow_golem", "minecraft:shulker", "minecraft:stray");
    private static final List<String> BLOOD_PURPLE_ENTITIES = List.of("minecraft:enderman", "minecraft:shulker", "minecraft:ender_dragon", "minecraft:endermite");
    private static final List<String> BLOOD_ORANGE_ENTITIES = List.of("minecraft:magma_cube", "minecraft:blaze");
    private static final HashMap<String, List<String>> DEFAULT_ENTITY_BLOOD_COLORS = new HashMap<>();
    private static HashMap<String, List<String>> ENTITY_BLOOD_COLORS;

    private static final String BURN_DAMAGE_COLOR = "#323232";

    private static ForgeConfigSpec.ConfigValue<List<? extends String>> BURN_DAMAGE_SOURCE;
    private static ForgeConfigSpec.ConfigValue<List<? extends String>> BLACKLIST_INJURY_SOURCES;

    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    private static ForgeConfigSpec.BooleanValue SHOW_MOB_DAMAGE;
    private static ForgeConfigSpec.IntValue AVAILABLE_TEXTURES_PER_ENTITY;

    public static boolean showEntityDamage() { return false; }

    public static int availableTexturesPerEntity() { return AVAILABLE_TEXTURES_PER_ENTITY.get(); }
    public static HashMap<String, List<String>> entityBloodColors() { return ENTITY_BLOOD_COLORS; }

    public static List<? extends String> burnDamageSources() { return BURN_DAMAGE_SOURCE.get(); }
    public static List<? extends String> blackListInjurySources() { return BLACKLIST_INJURY_SOURCES.get(); }
    public static String getBurnDamageColor() { return BURN_DAMAGE_COLOR; }

    public static void loadClientConfig() {
        BUILDER.push("blood spray settings");

        SHOW_MOB_DAMAGE = BUILDER.comment("Whether or not an entity should show injury textures when damaged.")
                .define("show_entity_damage", false);

        AVAILABLE_TEXTURES_PER_ENTITY = BUILDER.comment("The maximum amount of available injury textures permitted per entity.\n" +
                        "Resource packs can be created to add additional textures for entities, override existing textures, or to\n" +
                        "even create textures for entities that have none (only applies when show_entity_damage is true).")
                .defineInRange("available_textures_per_entity", 25, 0, 100);

        BURN_DAMAGE_SOURCE = BUILDER.comment("List of the damage sources that will display burn damage for the entities (only applies when show_entity_damage is true).")
                .defineListAllowEmpty("burn_damage_sources",
                        List.of("burn", "fireball", "fireworks", "lava", "hotFloor", "onFire", "inFire", "lightningBolt"),
                        it -> it instanceof String);

        BLACKLIST_INJURY_SOURCES = BUILDER.comment("List of the damage sources that will not display texture damage to the entity (only applies when show_entity_damage is true).")
                .defineListAllowEmpty("blacklist_injury_sources",
                        List.of("drown", "starve", "dryOut", "freeze", "fellOutOfWorld"),
                        it -> it instanceof String);

        BUILDER.pop();

        ENTITY_BLOOD_COLORS = getConfigData();

        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, BUILDER.build());
    }

    private static HashMap<String, List<String>> getOrCreateConfigFile(File configFile, Type type) {

        if (!configFile.exists()) {
            try {
                // Populates the default entity blood colors map. This is what will be populated by default in the
                // entity_blood_colors.json file.
                DEFAULT_ENTITY_BLOOD_COLORS.put(BLOOD_BLACK, BLOOD_BLACK_ENTITIES);
                DEFAULT_ENTITY_BLOOD_COLORS.put(BLOOD_BLUE, BLOOD_BLUE_ENTITIES);
                DEFAULT_ENTITY_BLOOD_COLORS.put(BLOOD_GREEN, BLOOD_GREEN_ENTITIES);
                DEFAULT_ENTITY_BLOOD_COLORS.put(BLOOD_GREY, BLOOD_GREY_ENTITIES);
                DEFAULT_ENTITY_BLOOD_COLORS.put(BLOOD_PURPLE, BLOOD_PURPLE_ENTITIES);
                DEFAULT_ENTITY_BLOOD_COLORS.put(BLOOD_ORANGE, BLOOD_ORANGE_ENTITIES);
                FileUtils.write(configFile, GSON.toJson(ClientConfig.DEFAULT_ENTITY_BLOOD_COLORS), Charset.defaultCharset());
            }
            catch (IOException e) {
                BloodyBitsMod.LOGGER.error("Bloody Bits color config file could not be written.");
            }
        }

        try {
            return GSON.fromJson(FileUtils.readFileToString(configFile, Charset.defaultCharset()), type);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static File getConfigDirectory() {
        Path configPath = FMLPaths.CONFIGDIR.get();
        Path jsonPath = Paths.get(configPath.toAbsolutePath().toString(), "bloodybits_colors");
        return jsonPath.toFile();
    }

    private static HashMap<String, List<String>> getConfigData() {
        File configDir = getConfigDirectory();
        File entityBloodColorsConfigFile = new File(configDir, "entity_blood_colors" + ".json");
        return getOrCreateConfigFile(entityBloodColorsConfigFile, new TypeToken<HashMap<String, List<String>>>(){}.getType());
    }
}
