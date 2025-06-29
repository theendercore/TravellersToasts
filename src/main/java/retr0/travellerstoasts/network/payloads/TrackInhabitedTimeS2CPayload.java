package retr0.travellerstoasts.network.payloads;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import retr0.travellerstoasts.util.BiomeToastManager;

import static retr0.travellerstoasts.TravellersToasts.modRl;

public class TrackInhabitedTimeS2CPayload implements CustomPacketPayload {
    public boolean finishedQuery;

    public TrackInhabitedTimeS2CPayload(boolean finishedQuery) {
        this.finishedQuery = finishedQuery;
    }

    public TrackInhabitedTimeS2CPayload(FriendlyByteBuf buf) {
        this.finishedQuery = buf.readBoolean();
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeBoolean(finishedQuery);
    }

    @Override
    public @NotNull CustomPacketPayload.Type<TrackInhabitedTimeS2CPayload> type() {
        return ID;
    }

    public static CustomPacketPayload.Type<TrackInhabitedTimeS2CPayload> ID = new CustomPacketPayload.Type<>(modRl("inhabited_time_track_response"));
    public static StreamCodec<FriendlyByteBuf, TrackInhabitedTimeS2CPayload> CODEC = CustomPacketPayload.codec(TrackInhabitedTimeS2CPayload::write, TrackInhabitedTimeS2CPayload::new);

    public static void send(boolean finishedQuery, ServerPlayer player) {
        if (ServerPlayNetworking.canSend(player, ID)) {
            ServerPlayNetworking.send(player, new TrackInhabitedTimeS2CPayload(finishedQuery));
        }
    }

    public static void receive(TrackInhabitedTimeS2CPayload payload, ClientPlayNetworking.Context context) {
        context.client().execute(() -> BiomeToastManager.getInstance().processServerResponse(payload.finishedQuery));
    }

}
