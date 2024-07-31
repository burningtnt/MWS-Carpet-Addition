package net.burningtnt.mca.network;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

public interface PacketReceiver {
    void handle(MinecraftServer server, ServerPlayerEntity player, PacketSender sender, PacketByteBuf buf);
}
