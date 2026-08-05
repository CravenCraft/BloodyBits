package com.cravencraft.bloodybits.model;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;

public record BloodType(TagKey<EntityType<?>> entityTag, String color) {

    public static final Codec<BloodType> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    TagKey.hashedCodec(Registries.ENTITY_TYPE).fieldOf("entity_tag").forGetter(BloodType::entityTag),
                    Codec.STRING.fieldOf("color").forGetter(BloodType::color)
        ).apply(instance, BloodType::new)
    );

}
