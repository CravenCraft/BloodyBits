package com.cravencraft.bloodybits.model;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;

public record BloodModel(TagKey<EntityType<?>> entityTag, String color) {

//    private static final Codec<Integer> HEX_COLOR_CODEC = Codec.STRING.flatXmap(
//            str -> {
//                String hex = str.startsWith("#") ? str.substring(1) : str;
//                if (hex.length() != 6 && hex.length() != 8) {
//                    return DataResult.error(() -> "Hex color must be 6 or 8 digits: " + str);
//                }
//                try {
//                    long value = Long.parseLong(hex, 16);
//                    if (hex.length() == 6) {
//                        value = 0xFF000000L | value;
//                    }
//                    return DataResult.success((int) value);
//                } catch (NumberFormatException exception) {
//                    return DataResult.error(() -> "Invalid hex color: " + str);
//                }
//            },
//            color -> DataResult.success(String.format("#%06X", color & 0xFFFFFF))
//    );

    public static final Codec<BloodModel> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    TagKey.hashedCodec(Registries.ENTITY_TYPE).fieldOf("entity_tag").forGetter(BloodModel::entityTag),
                    Codec.STRING.fieldOf("color").forGetter(BloodModel::color)
        ).apply(instance, BloodModel::new)
    );

}
