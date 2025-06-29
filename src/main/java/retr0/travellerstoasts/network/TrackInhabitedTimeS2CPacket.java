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

import static retr0.travellerstoasts.TravellersToasts.modRl;

public class TrackInhabitedTimeS2CPacket {
    public static final ResourceLocation INHABITED_TIME_TRACK_RESPONSE_ID = modRl( "inhabited_time_track_response");

    public static void send(boolean finishedQuery, ServerPlayer player) {
        var buf = PacketByteBufs.create();
        buf.writeBoolean(finishedQuery);

        ServerPlayNetworking.send(player, INHABITED_TIME_TRACK_RESPONSE_ID, buf);
    }



    public static void receive(
        Minecraft client, ClientPacketListener handler, FriendlyByteBuf buf, PacketSender responseSender)
    {
        boolean finishedQuery = buf.readBoolean();

        client.execute(() -> BiomeToastManager.getInstance().processServerResponse(finishedQuery));
    }
}
