package com.martianpancake.villagermod;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VillagerMod implements ModInitializer {
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger("villagermod");

	public static final Identifier SOUND_PLAY_PACKET_ID = new Identifier("villagermod", "sound_play_packet");

	private final SoundPlayEventHandler soundEventHandler = new SoundPlayEventHandler();

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		LOGGER.info("Hello Fabric world!");
		ServerPlayNetworking.registerGlobalReceiver(VillagerMod.SOUND_PLAY_PACKET_ID, this::serverSoundPlayPacketChannelHandler);
	}

	// This function called every time we receive a sound play packet from a client
	void serverSoundPlayPacketChannelHandler(MinecraftServer minecraftServer, ServerPlayerEntity serverPlayerEntity, ServerPlayNetworkHandler serverPlayNetworkHandler, PacketByteBuf packetByteBuf, PacketSender packetSender) {
		this.soundEventHandler.clientPlayedSound(serverPlayerEntity.getWorld(), new SoundPlayPacket(packetByteBuf));
	}


}
