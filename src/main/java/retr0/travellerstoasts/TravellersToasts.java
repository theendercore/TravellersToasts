package retr0.travellerstoasts;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retr0.travellerstoasts.extension.ExtensionServerPlayerEntity;
import retr0.travellerstoasts.network.PacketRegistry;
import retr0.travellerstoasts.network.payloads.UpdateVisitedBiomesS2CPayload;
import retr0.travellerstoasts.util.ModUsageManager;

import java.util.Collection;

public class TravellersToasts implements ModInitializer {
    public static final String MOD_ID = "travellerstoasts";
    // This logger is used to write text to the console and the log file.
    // It is considered best practice to use your mod id as the logger's name.
    // That way, it's clear which mod wrote info, warnings, and errors.
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        // TODO: BETTER HANDLING TOAST WHEN SWITCHING BIOMES WHILE ACTIVE.
        // TODO: BETTER WATER CHECKS FOR WHEN IN WATER THAT LOOKS LIKE RIVER BUT IS ANOTHER BIOME (E.G. BEACH).
        // TODO: IMPLMEMNENT CUSTOM BIOMES
        LOGGER.info("Initialized TravellersToasts!");

        PacketRegistry.registerPayloads();
        PacketRegistry.registerC2SPackets();
        ModUsageManager.init();

        ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register(((player, origin, destination) ->
                ((ExtensionServerPlayerEntity) player).travellersToasts$stopTracking(false)));

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            var advancementEntry = server.getAdvancements().get(mc("adventure/adventuring_time"));
            var obtainedCriteria = (Collection<String>) handler.player.getAdvancements().getOrStartProgress(advancementEntry).getCompletedCriteria();

            UpdateVisitedBiomesS2CPayload.send(obtainedCriteria, handler.player);
        });
    }

    public static ResourceLocation rl(String namespace, String path) {
        return ResourceLocation.fromNamespaceAndPath(namespace, path);
    }

    public static ResourceLocation modRl(String path) {
        return rl(MOD_ID, path);
    }

    public static ResourceLocation mc(String path) {
        return ResourceLocation.withDefaultNamespace(path);
    }

    public static ResourceLocation parse(String rl) {
        return ResourceLocation.parse(rl);
    }
}
