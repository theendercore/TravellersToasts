package retr0.travellerstoasts.network;

import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerLoginNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientHandshakePacketListenerImpl;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerLoginPacketListenerImpl;
import org.jetbrains.annotations.Nullable;
import retr0.travellerstoasts.util.ModUsageManager;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static retr0.travellerstoasts.TravellersToasts.modRl;

public class ModUsagePacket {
    public static final ResourceLocation NOTIFY_MOD_USAGE_ID = modRl("notify_mod_usage");

    public static void send(PacketSender sender) {
        sender.sendPacket(NOTIFY_MOD_USAGE_ID, PacketByteBufs.empty());
    }

    public static void receive(
            MinecraftServer server, ServerLoginPacketListenerImpl handler, boolean understood, FriendlyByteBuf buf, ServerLoginNetworking.LoginSynchronizer synchronizer, PacketSender responseSender)
    {
    }

    @Environment(EnvType.CLIENT)
    public static CompletableFuture<@Nullable FriendlyByteBuf> receive(
            Minecraft client, ClientHandshakePacketListenerImpl handler, FriendlyByteBuf buf, Consumer<GenericFutureListener<? extends Future<? super Void>>> listenerAdder)
    {
        ModUsageManager.getInstance().setServerModUsage(true);
        return CompletableFuture.completedFuture(null);
    }
}
