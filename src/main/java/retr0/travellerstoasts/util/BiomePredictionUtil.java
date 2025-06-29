package retr0.travellerstoasts.util;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBiomeTags;
import net.minecraft.SharedConstants;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;

@Environment(EnvType.CLIENT)
public final class BiomePredictionUtil {
    /**
     * Gets the biome at a relative future position of {@code player} based on their velocity over {@code PEEK_SECONDS}.
     *
     * @param player The target player.
     * @return The {@link Holder} of the biome at the relative future position.
     */
    public static Holder<Biome> getFutureBiome(LocalPlayer player) {
        // Ticks ahead for which the future biome will be checked.
        final var FUTURE_TICKS = SharedConstants.TICKS_PER_SECOND * 10;

        // We only check the x and z-directions as underground biomes generally aren't that tall with respect to the
        // speeds a player falls at.
        var absoluteFuturePos = player.getDeltaMovement().scale(FUTURE_TICKS).multiply(1d, 0d, 1d);
        var relativeFuturePos = player.position().add(absoluteFuturePos);
        var relativeFutureBlockPos = new BlockPos(
                (int) relativeFuturePos.x, (int) relativeFuturePos.y, (int) relativeFuturePos.z);
        // Relative block position in PEEK_SECONDS seconds based on current velocity.
        var futureBlockPos = new BlockPos(relativeFutureBlockPos);

        return player.clientLevel.getBiome(futureBlockPos);
    }



    /**
     * Approximates if the 'features' of {@code biome} (e.g. Jungle trees in jungle biome) are currently visible to
     * {@code player}.
     *
     * @param biome  The target biome.
     * @param player the target player.
     * @return {@code true} if the approximation believes {@code biome}'s features to be visible; otherwise,
     *         {@code false}.
     */
    // TODO: This entire thing needs a rework!
    public static boolean areBiomeFeaturesVisible(Holder<Biome> biome, LocalPlayer player) {
        var worldRegistryKey = player.clientLevel.dimension();
        // We use the current mood percentage to ensure that when mining, the player is not notified of biome changes
        // they would not be able to see.
        var moodPercentage = player.getCurrentMood();

        if (worldRegistryKey == Level.NETHER) {
            // For the nether, the mood percentage tends to fluctuate more often and at higher values (e.g. >0.0008f).
            if (moodPercentage < 0.001f) return true;

            for (var i = 1; i <= 3; ++i) {
                if (!player.level().getBlockState(player.blockPosition().above(i)).isAir()) return false;
            }
            return true;
        } else if (worldRegistryKey == Level.END) {
            // For The End, we consider biomes' features to always be visible.
            return true;
        }

        // For the overworld, the mood percentage tends to stay at 0f, except in denser environments (e.g. Dark Forest)
        // where it instead may go as high as 0.0004f.
        return moodPercentage < 0.0005f
            // For ocean biomes, the mood may be too high. We instead check if the sky is visible (i.e. underwater).
            || player.clientLevel.canSeeSkyFromBelowWater(player.blockPosition())
            // For cave biomes, we consider their features to always be visible.
            || biome.is(ConventionalBiomeTags.IS_CAVE);
    }
}
