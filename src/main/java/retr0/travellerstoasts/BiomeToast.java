package retr0.travellerstoasts;

import retr0.travellerstoasts.config.TravellersToastsConfig;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;

import static retr0.travellerstoasts.TravellersToasts.*;

public class BiomeToast implements Toast {
    private static final ResourceLocation TEXTURE = mc("toast/recipe");
    private static final ResourceLocation PLAQUE_TEXTURE = modRl("toast/plaque");
    private static final ResourceLocation PLAQUE_ROUNDED_TEXTURE = modRl("toast/plaque_rounded");
    private static final ResourceLocation FALLBACK_BIOME_TEXTURE = getBiomeIconIdentifier(Biomes.MEADOW.location());
    private static final long DURATION = 5000L;

    private long startTime;
    private boolean justUpdated;
    private ResourceLocation biomeId;

    public BiomeToast(ResourceLocation biomeId) { this.biomeId = biomeId; }

    private static ResourceLocation getBiomeIconIdentifier(ResourceLocation biomeId) {
        return modRl("biome/" + biomeId.getNamespace() + "/" + biomeId.getPath());
    }

    private void drawBiomeIcon(GuiGraphics context, ToastComponent manager, ResourceLocation biomeId) {
        var guiAtlasManager = manager.getMinecraft().getGuiSprites();
        var sprite = guiAtlasManager.getSprite(getBiomeIconIdentifier(biomeId));
        if (sprite.contents().name().equals(MissingTextureAtlasSprite.getLocation()))
            sprite = guiAtlasManager.getSprite(FALLBACK_BIOME_TEXTURE);

        context.blit(8, 8, 0, 16, 16, sprite);
    }


    @Override
    public Visibility render(GuiGraphics context, ToastComponent manager, long startTime) {
        var toastHeader = Component.translatable(MOD_ID + ".toast.header");
        var biomeName = Component.translatable(biomeId.toLanguageKey("biome"));

        if (this.justUpdated) {
            this.startTime = startTime;
            this.justUpdated = false;
        }

        // --- Draw Toast Background ---
        context.blitSprite(TEXTURE, 0, 0, this.width(), this.height());

        // --- Draw Toast Description ---
        context.drawString(manager.getMinecraft().font, toastHeader, 30, 7, 0xFF500050, false);
        context.drawString(manager.getMinecraft().font, biomeName, 30, 18, 0xFF000000, false);

        // --- Draw Biome Icon Plaque ---
        context.blitSprite(TravellersToastsConfig.roundedIconBackground ? PLAQUE_ROUNDED_TEXTURE : PLAQUE_TEXTURE, 4, 4, 24, 24);

        // --- Draw Biome Icon ---
        drawBiomeIcon(context, manager, biomeId);

        return startTime - this.startTime >= DURATION ? Toast.Visibility.HIDE : Toast.Visibility.SHOW;
    }



    private void addBiome(ResourceLocation biomeId) {
        this.biomeId = biomeId;
        justUpdated = true;
    }



    public static void show(ToastComponent manager, Holder<Biome> biome) {
        BiomeToast biomeToast = manager.getToast(BiomeToast.class, NO_TOKEN);
        biome.unwrapKey().ifPresent(key -> {
            if (biomeToast == null)
                manager.addToast(new BiomeToast(key.location()));
            else
                biomeToast.addBiome(key.location());
        });
    }
}
