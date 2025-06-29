package retr0.travellerstoasts.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientLoginNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerLoginNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import retr0.travellerstoasts.network.payloads.TrackInhabitedTimeC2SPayload;
import retr0.travellerstoasts.network.payloads.TrackInhabitedTimeS2CPayload;
import retr0.travellerstoasts.network.payloads.UpdateVisitedBiomesS2CPayload;

import static retr0.travellerstoasts.network.ModUsagePacket.NOTIFY_MOD_USAGE_ID;


public class PacketRegistry {
    public static void registerPayloads(){
        PayloadTypeRegistry.playC2S().register(TrackInhabitedTimeC2SPayload.ID, TrackInhabitedTimeC2SPayload.CODEC);

        PayloadTypeRegistry.playS2C().register(TrackInhabitedTimeS2CPayload.ID, TrackInhabitedTimeS2CPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(UpdateVisitedBiomesS2CPayload.ID, UpdateVisitedBiomesS2CPayload.CODEC);
    }

    public static void registerC2SPackets() {
        ServerPlayNetworking.registerGlobalReceiver(TrackInhabitedTimeC2SPayload.ID, TrackInhabitedTimeC2SPayload::receive);
        ServerLoginNetworking.registerGlobalReceiver(NOTIFY_MOD_USAGE_ID, ModUsagePacket::receive);
    }

    public static void registerS2CPackets() {
        ClientPlayNetworking.registerGlobalReceiver(TrackInhabitedTimeS2CPayload.ID, TrackInhabitedTimeS2CPayload::receive);
        ClientPlayNetworking.registerGlobalReceiver(UpdateVisitedBiomesS2CPayload.ID, UpdateVisitedBiomesS2CPayload::receive);
        ClientLoginNetworking.registerGlobalReceiver(NOTIFY_MOD_USAGE_ID, ModUsagePacket::receive);
    }
}
