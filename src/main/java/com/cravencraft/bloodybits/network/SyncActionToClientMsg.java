package com.cravencraft.bloodybits.network;

import net.minecraft.network.FriendlyByteBuf;

/**
 * A message that contains a record of the total amount of stamina to drain from
 * the player. Sent from server to client.
 *
 * @param
 */
public record SyncActionToClientMsg(String entityName) {
    public static SyncActionToClientMsg read(FriendlyByteBuf buffer) {
        return new SyncActionToClientMsg(buffer.readUtf());
    }

    public void write(FriendlyByteBuf buffer) {
        buffer.writeUtf(entityName);
    }
}
