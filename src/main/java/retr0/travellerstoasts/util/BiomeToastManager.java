package retr0.travellerstoasts.util;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.tag.convention.v1.ConventionalBiomeTags;
import net.minecraft.SharedConstants;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementNode;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientAdvancements;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import retr0.carrotconfig.config.ConfigSavedCallback;
import retr0.travellerstoasts.BiomeToast;
import retr0.travellerstoasts.config.TravellersToastsConfig;
import retr0.travellerstoasts.mixin.AccessorBossBarHud;
import retr0.travellerstoasts.network.TrackInhabitedTimeC2SPacket;

import java.util.*;

import static net.fabricmc.fabric.api.tag.convention.v1.ConventionalBiomeTags.OCEAN;
import static net.fabricmc.fabric.api.tag.convention.v1.ConventionalBiomeTags.RIVER;

@Environment(EnvType.CLIENT)
public class BiomeToastManager {
    private static BiomeToastManager instance;
    // Required ticks for the "entering biome" condition.
    private final static int HOLD_TICKS = SharedConstants.TICKS_PER_SECOND * 3;

    private final CooldownHandler<Holder<Biome>> biomeCooldownHandler =
            new CooldownHandler<>(() -> (long) TravellersToastsConfig.toastCooldownTime * 60000L);

    private final Set<ResourceLocation> visitedBiomes = new HashSet<>();

    private boolean awaitingServerResponse = false;
    private int ticksExploringBiome = 0;
    private Vec3 previousPos = Vec3.ZERO;
    private Holder<Biome> previousBiome;
    private Holder<Biome> currentBiome;

    public static void init() {
        if (instance != null) return;

        instance = new BiomeToastManager();

        ConfigSavedCallback.EVENT.register(configClass -> {
            if (!configClass.isAssignableFrom(TravellersToastsConfig.class)) return;

            instance.resetState(false);
            TrackInhabitedTimeC2SPacket.send(-1); // Stop server from tracking player.
        });

        // Try to get already-explored biomes client-side. This solution works whether the server has TravellersToasts
        // installed or not--BUT ONLY if the `adventure/adventuring_time` advancement exists on the server.
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            handler.getAdvancements().setListener(new ClientAdvancements.Listener() {
                @Override
                public void onUpdateAdvancementProgress(AdvancementNode advancement, AdvancementProgress progress) {
                    if (!advancement.holder().id().equals(new ResourceLocation("adventure/adventuring_time"))) return;

                    var visitedBiomes = ((Collection<String>) progress.getCompletedCriteria()).stream().map(ResourceLocation::new).toList();
                    BiomeToastManager.getInstance().addVisitedBiomes(visitedBiomes);
                }

                @Override public void onSelectedTabChanged(@Nullable AdvancementHolder advancement) { }
                @Override public void onAddAdvancementRoot(AdvancementNode root) { }
                @Override public void onRemoveAdvancementRoot(AdvancementNode root) { }
                @Override public void onAddAdvancementTask(AdvancementNode dependent) { }
                @Override public void onRemoveAdvancementTask(AdvancementNode dependent) { }
                @Override public void onAdvancementsCleared() { }
            });
        });
    }

    public static BiomeToastManager getInstance() {
        return instance;
    }

    /**
     * Attempts to show a biome toast if the player has been 'exploring' a biome for a valid amount of time.
     * @param requestServerCheck If {@code true}, additionally requests the server to check for a valid inhabited time
     *                          before showing the toast.
     */
    private void showToast(boolean requestServerCheck) {
        if (ticksExploringBiome != HOLD_TICKS) return;

        // If the current server has the mod loaded, we do an additional check by requesting the current chunk's
        // inhabited time to be tracked (waiting for a response).
        if (requestServerCheck) {
            if (!awaitingServerResponse) {
                TrackInhabitedTimeC2SPacket.send(TravellersToastsConfig.maxInhabitedTime);
                awaitingServerResponse = true;
            }
            return;
        }

        BiomeToast.show(Minecraft.getInstance().getToasts(), currentBiome);

        currentBiome.unwrapKey().ifPresent(key -> addVisitedBiome(key.location()));
        biomeCooldownHandler.refresh(previousBiome);
        previousBiome = currentBiome;
        resetState(false);
    }



    /**
     * Resets the state of the {@link BiomeToastManager} (not resetting cooldowns by default).
     * @param fullReset If {@code true}, all fields will be reset.
     */
    public void resetState(boolean fullReset) {
        if (fullReset) {
            biomeCooldownHandler.reset();
            visitedBiomes.clear();
            previousBiome = null;
        }
        ticksExploringBiome = 0;
        awaitingServerResponse = false;
    }



    /**
     * Runs various checks on the player's 'state' (e.g. velocity, pose, etc.) to estimate whether it is appropriate
     * for a biome toast to show.
     * @implNote Object state change—previous player position is updated within method.
     */
    private boolean doesPlayerHaveValidState(LocalPlayer player) {
        var currentPos = player.position();
        var isOceanBiome = (currentBiome.is(RIVER) || currentBiome.is(OCEAN)) && !currentBiome.is(ConventionalBiomeTags.AQUATIC_ICY);

        // Don't decrement or increment ticksEnteringBiome if the player is not moving.
        var movementChecks = player.input.hasForwardImpulse() && currentPos.distanceToSqr(previousPos) > 0.004;
        var bossBarChecks = ((AccessorBossBarHud) Minecraft.getInstance().gui.getBossOverlay()).getBossBars().isEmpty();
        // Permit only swimming in ocean biomes to count as 'exploration'; otherwise, allow both swimming and walking.
        var oceanBiomeChecks = (isOceanBiome && player.isUnderWater() && player.isVisuallySwimming())
                || (!isOceanBiome && (!player.isUnderWater() || player.isVisuallySwimming()));

        previousPos = currentPos;

        return movementChecks && bossBarChecks && oceanBiomeChecks;
    }



    /**
     * Runs various checks on the player's location (or predicted future location) to estimate whether it is appropriate
     * for a biome toast to show.
     */
    private boolean isPlayerInValidLocation(LocalPlayer player) {
        return BiomePredictionUtil.getFutureBiome(player).equals(currentBiome) &&
               BiomePredictionUtil.areBiomeFeaturesVisible(currentBiome, player);
    }



    /**
     * Updates the time tracked for 'exploring' a biome. Should be called every game tick.
     */
    public void tick(LocalPlayer player) {
        currentBiome = player.level().getBiome(player.blockPosition());
        // Do nothing if the current biome is still on cooldown.
        if (currentBiome.equals(previousBiome) || !biomeCooldownHandler.hasCooled(currentBiome)) return;

        // Do nothing if persistent exploration is enabled and if the current biome has already been visited.
        var currentBiomeId = currentBiome.unwrapKey().map(ResourceKey::location).orElse(null);
        if (TravellersToastsConfig.usePersistentExploration && currentBiomeId != null && visitedBiomes.contains(currentBiomeId))
            return;

        showToast(ModUsageManager.getInstance().doesServerUseMod() && TravellersToastsConfig.maxInhabitedTime > 0f);
        ticksExploringBiome += doesPlayerHaveValidState(player) && isPlayerInValidLocation(player) ? 1 : -1;
        ticksExploringBiome = Mth.clamp(ticksExploringBiome, 0, HOLD_TICKS);
    }


    public void addVisitedBiomes(List<ResourceLocation> biomeIds) {
        visitedBiomes.addAll(biomeIds);
    }

    public void addVisitedBiome(ResourceLocation biomeId) {
        visitedBiomes.add(biomeId);
    }



    public void processServerResponse(boolean finishedQuery) {
        awaitingServerResponse = false;
        if (finishedQuery)
            showToast(false);
        else
            resetState(false);
    }

    private BiomeToastManager() { }
}
