package com.cravencraft.bloodybits.model;

import com.cravencraft.bloodybits.BloodyBitsMod;
import com.cravencraft.bloodybits.registries.BloodModelRegistry;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.DynamicOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@EventBusSubscriber
public class BloodModelReloadListener extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    public static final String DIRECTORY = "blood_colors";

    public BloodModelReloadListener() {
        super(GSON, DIRECTORY);
    }

    @SubscribeEvent
    public static void onAddReloadListener(AddReloadListenerEvent event) {
        event.addListener(new BloodModelReloadListener());
    }

    @Override
    protected void apply(@NotNull Map<ResourceLocation, JsonElement> object,
                         @NotNull ResourceManager resourceManager,
                         @NotNull ProfilerFiller profiler) {

        DynamicOps<JsonElement> ops = this.makeConditionalOps();
        List<BloodModel> bloodModels = new ArrayList<>();

        for (Map.Entry<ResourceLocation, JsonElement> entry : object.entrySet()) {

            BloodModel.CODEC.parse(ops, entry.getValue())
                    .resultOrPartial(error -> BloodyBitsMod.LOGGER.error("Failed to parse blood color {}: {}", entry.getKey(), error))
                    .ifPresent(bloodModels::add);

            GSON.fromJson(entry.getValue(), BloodModel.class);

//            BloodModel.CODEC.parse(ops, entry.getValue())
//                    .resultOrPartial(error -> BloodyBitsMod.LOGGER.error("Failed to parse blood type {}: {}", entry.getKey(), error))
//                    .ifPresent(bloodModels::add);
        }

        BloodModelRegistry.load(bloodModels);
        BloodyBitsMod.LOGGER.info("Loaded {} blood type(s)", bloodModels.size());
    }
}
