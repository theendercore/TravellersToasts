package retr0.travellerstoasts.network.payloads;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.SharedConstants;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;
import retr0.travellerstoasts.extension.ExtensionServerPlayerEntity;

import static retr0.travellerstoasts.TravellersToasts.modRl;

public class TrackInhabitedTimeC2SPayload implements CustomPacketPayload {
    public int maxInhabitedTimeTicks;

    public TrackInhabitedTimeC2SPayload(int maxInhabitedTimeTicks) {
        this.maxInhabitedTimeTicks = maxInhabitedTimeTicks;
    }

    public TrackInhabitedTimeC2SPayload(FriendlyByteBuf buf) {
        this.maxInhabitedTimeTicks = buf.readInt();
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeInt(maxInhabitedTimeTicks);
    }

    @Override
    public @NotNull Type<TrackInhabitedTimeC2SPayload> type() {
        return ID;
    }

    public static Type<TrackInhabitedTimeC2SPayload> ID = new CustomPacketPayload.Type<>(modRl("inhabited_time_track_request"));
    public static StreamCodec<FriendlyByteBuf, TrackInhabitedTimeC2SPayload> CODEC = CustomPacketPayload.codec(TrackInhabitedTimeC2SPayload::write, TrackInhabitedTimeC2SPayload::new);


    public static void send(float maxInhabitedTimeM) {
        if (ClientPlayNetworking.canSend(ID)) {
            ClientPlayNetworking.send(new TrackInhabitedTimeC2SPayload((int) (SharedConstants.TICKS_PER_MINUTE * maxInhabitedTimeM)));
        }
    }

    public static void receive(TrackInhabitedTimeC2SPayload payload, ServerPlayNetworking.Context context) {
        context.server().execute(() -> {
            var player = context.player();
            if (player == null) return;

            ((ExtensionServerPlayerEntity) player).travellersToasts$beginTracking(payload.maxInhabitedTimeTicks);
        });
    }
}
