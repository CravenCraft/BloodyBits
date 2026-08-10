package com.cravencraft.bloodybits.model;

import com.cravencraft.bloodybits.BloodyBitsMod;
import com.cravencraft.bloodybits.registries.BloodTypeRegistry;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Mod.EventBusSubscriber
public class BloodTypeReloadListener extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    public static final String DIRECTORY = "blood_colors";

    public BloodTypeReloadListener() {
        super(GSON, DIRECTORY);
    }

    @SubscribeEvent
    public static void onAddReloadListener(AddReloadListenerEvent event) {
        event.addListener(new BloodTypeReloadListener());
    }

    @Override
    protected void apply(@NotNull Map<ResourceLocation, JsonElement> object,
                         @NotNull ResourceManager resourceManager,
                         @NotNull ProfilerFiller profiler) {
        List<BloodType> bloodTypes = new ArrayList<>();

        for (Map.Entry<ResourceLocation, JsonElement> entry : object.entrySet()) {

            BloodType.CODEC.parse(JsonOps.INSTANCE, entry.getValue())
                    .resultOrPartial(error -> BloodyBitsMod.LOGGER.error("Failed to parse blood color {}: {}",
                            entry.getKey(), error))
                    .ifPresent(bloodTypes::add);

            GSON.fromJson(entry.getValue(), BloodType.class);
        }

        BloodTypeRegistry.load(bloodTypes);
        BloodyBitsMod.LOGGER.info("Loaded {} blood type(s)", bloodTypes.size());
    }
}
