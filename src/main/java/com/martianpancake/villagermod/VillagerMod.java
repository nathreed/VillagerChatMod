package com.martianpancake.villagermod;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.message.v1.ServerMessageDecoratorEvent;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

		ServerMessageDecoratorEvent.EVENT.register(ServerMessageDecoratorEvent.CONTENT_PHASE, (sender, message) -> {
			Pattern pattern = Pattern.compile("^Villager (.*?),(.*)");
			Matcher matcher = pattern.matcher(message.getString());
			LOGGER.info(message.getString());
			if(matcher.find()) {
				String playerMessage = matcher.group(2);
				if(playerMessage != null) {
					LOGGER.info(String.format("GOT PLAYER MESSAGE FOR VILLAGER %s: %s", matcher.group(1), playerMessage));
					VillagerChatManager manager = new VillagerChatManager();
					manager.askVillagerQuestion(matcher.group(1), playerMessage, sender.getWorld());
				}
			}
			return CompletableFuture.completedFuture(message);
		});
	}

	// This function called every time we receive a sound play packet from a client
	void serverSoundPlayPacketChannelHandler(MinecraftServer minecraftServer, ServerPlayerEntity serverPlayerEntity, ServerPlayNetworkHandler serverPlayNetworkHandler, PacketByteBuf packetByteBuf, PacketSender packetSender) {
		this.soundEventHandler.clientPlayedSound(serverPlayerEntity.getWorld(), new SoundPlayPacket(packetByteBuf));
	}


}
