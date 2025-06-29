package retr0.travellerstoasts.network;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import retr0.travellerstoasts.util.BiomeToastManager;

import java.util.ArrayList;
import java.util.Collection;

import static retr0.travellerstoasts.TravellersToasts.MOD_ID;

/**
 * A packet to synchronize already-explored biomes from the server to the client. This exists as a fallback to the client-side
 * method present in {@link BiomeToastManager}.
 */
public class UpdateVisitedBiomesS2CPacket {
    public static final ResourceLocation UPDATE_VISITED_BIOMES_ID = new ResourceLocation(MOD_ID, "update_visited_biomes");

    public static void send(Collection<String> visited, ServerPlayer player) {
        var buf = PacketByteBufs.create();
        buf.writeInt(visited.size());
        visited.forEach(id -> buf.writeResourceLocation(new ResourceLocation(id)));

        ServerPlayNetworking.send(player, UPDATE_VISITED_BIOMES_ID, buf);
    }



    public static void receive(Minecraft client, ClientPacketListener handler, FriendlyByteBuf buf, PacketSender responseSender)
    {
        var numVisited = buf.readInt();
        var visitedBiomes = new ArrayList<ResourceLocation>();
        for (var i = 0; i < numVisited; ++i)
            visitedBiomes.add(i, buf.readResourceLocation());

        client.execute(() -> BiomeToastManager.getInstance().addVisitedBiomes(visitedBiomes));
    }
}
