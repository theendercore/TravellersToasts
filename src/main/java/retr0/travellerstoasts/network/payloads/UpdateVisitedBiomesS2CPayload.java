package retr0.travellerstoasts.network.payloads;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import retr0.travellerstoasts.TravellersToasts;
import retr0.travellerstoasts.util.BiomeToastManager;

import java.util.ArrayList;
import java.util.Collection;

import static retr0.travellerstoasts.TravellersToasts.modRl;

/**
 * A packet to synchronize already-explored biomes from the server to the client. This exists as a fallback to the client-side
 * method present in {@link BiomeToastManager}.
 */
public class UpdateVisitedBiomesS2CPayload implements CustomPacketPayload {
    public Collection<ResourceLocation> visited;

    public UpdateVisitedBiomesS2CPayload(Collection<String> finishedQuery) {
        this.visited = finishedQuery.stream().map(TravellersToasts::parse).toList();
    }

    public UpdateVisitedBiomesS2CPayload(FriendlyByteBuf buf) {
        var numVisited = buf.readInt();
        var visitedBiomes = new ArrayList<ResourceLocation>();
        for (var i = 0; i < numVisited; ++i) {
            visitedBiomes.add(i, buf.readResourceLocation());
        }
        this.visited = visitedBiomes;
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeInt(visited.size());
        visited.forEach(buf::writeResourceLocation);
    }

    @Override
    public @NotNull CustomPacketPayload.Type<UpdateVisitedBiomesS2CPayload> type() {
        return ID;
    }

    public static CustomPacketPayload.Type<UpdateVisitedBiomesS2CPayload> ID = new CustomPacketPayload.Type<>(modRl("update_visited_biomes"));
    public static StreamCodec<FriendlyByteBuf, UpdateVisitedBiomesS2CPayload> CODEC = CustomPacketPayload.codec(UpdateVisitedBiomesS2CPayload::write, UpdateVisitedBiomesS2CPayload::new);

    public static void send(Collection<String> visited, ServerPlayer player) {
        if (ServerPlayNetworking.canSend(player, ID)) {
            ServerPlayNetworking.send(player, new UpdateVisitedBiomesS2CPayload(visited));
        }
    }

    public static void receive(UpdateVisitedBiomesS2CPayload payload, ClientPlayNetworking.Context context) {
        context.client().execute(() -> BiomeToastManager.getInstance().addVisitedBiomes(payload.visited.stream().toList()));
    }
}
