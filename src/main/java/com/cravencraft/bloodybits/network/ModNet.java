package com.cravencraft.bloodybits.network;
import com.cravencraft.bloodybits.BloodyBitsMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * Very similar to the ModNet class in Paragliders. This one simply sends a message to and from the client to the server
 * containing the amount of stamina to drain from the player.
 */
public class ModNet {
    private ModNet() {}

    public static final String NET_VERSION = "1.0";
    public static final SimpleChannel NET = NetworkRegistry.newSimpleChannel(new ResourceLocation(BloodyBitsMod.MODID, "master"), () -> NET_VERSION, NET_VERSION::equals, NET_VERSION::equals);

    public static void init() {
        NET.registerMessage(1, SyncActionToClientMsg.class,
                SyncActionToClientMsg::write, SyncActionToClientMsg::read,
                Client::handleActionToClientStaminaCost, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
    }
    private static final class Client {

        private Client(){}
        public static void handleActionToClientStaminaCost(SyncActionToClientMsg msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().setPacketHandled(true);
            LocalPlayer localPlayer = Minecraft.getInstance().player;

//            ctx.get()

            if (localPlayer == null) return;
//            BotWStamina botWStamina = (BotWStamina) PlayerMovementProvider.of(localPlayer).stamina();
            if (localPlayer != null) {
//                ((StaminaOverride) botWStamina).setTotalActionStaminaCost(msg.totalActionStaminaCost());
            }
            else {
                BloodyBitsMod.LOGGER.error("Couldn't handle packet {}, capability not found", msg);
            }
        }
    }
}