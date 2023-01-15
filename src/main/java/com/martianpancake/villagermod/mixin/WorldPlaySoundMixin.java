package com.martianpancake.villagermod.mixin;

import com.martianpancake.villagermod.SoundPlayPacket;
import com.martianpancake.villagermod.SoundPlayUtil;
import com.martianpancake.villagermod.VillagerMod;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.MutableWorldProperties;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.function.Supplier;

@Mixin(ClientWorld.class)
public abstract class WorldPlaySoundMixin extends World {

	@Shadow @Final private MinecraftClient client;

	protected WorldPlaySoundMixin(MutableWorldProperties properties, RegistryKey<World> registryRef, RegistryEntry<DimensionType> dimension, Supplier<Profiler> profiler, boolean isClient, boolean debugWorld, long seed, int maxChainedNeighborUpdates) {
		super(properties, registryRef, dimension, profiler, isClient, debugWorld, seed, maxChainedNeighborUpdates);
	}

	@Inject(at = @At("HEAD"), method = "playSound(Lnet/minecraft/entity/player/PlayerEntity;DDDLnet/minecraft/registry/entry/RegistryEntry;Lnet/minecraft/sound/SoundCategory;FFJ)V")
	private void init(@Nullable PlayerEntity except, double x, double y, double z, RegistryEntry<SoundEvent> sound, SoundCategory category, float volume, float pitch, long seed, CallbackInfo info) {
		double distance = sound.value().getDistanceToTravel(volume);
		if (isSoundNearbyPlayer(x, y, z, distance)) {
			sendSoundPlayPacket(x, y, z, distance, sound.value().getId().toString());
		}
	}

	@Inject(at = @At("HEAD"), method = "playSoundFromEntity")
	private void init(@Nullable PlayerEntity except, Entity entity, RegistryEntry<SoundEvent> sound, SoundCategory category, float volume, float pitch, long seed, CallbackInfo info) {
		double distance = sound.value().getDistanceToTravel(volume);
		double x = entity.getX();
		double y = entity.getY();
		double z = entity.getZ();
		if(isSoundNearbyPlayer(x, y, z, distance)) {
			sendSoundPlayPacket(x, y, z, distance, sound.value().getId().toString());
		}
	}

	@Inject(at = @At("HEAD"), method = "playSound(DDDLnet/minecraft/sound/SoundEvent;Lnet/minecraft/sound/SoundCategory;FFZ)V")
	private void init(double x, double y, double z, SoundEvent sound, SoundCategory category, float volume, float pitch, boolean useDistance, CallbackInfo ci) {
		double distance = this.client.gameRenderer.getCamera().getPos().squaredDistanceTo(x, y, z);
		if (isSoundNearbyPlayer(x, y, z, distance)) {
			sendSoundPlayPacket(x, y, z, distance, sound.getId().toString());
		}
	}

	private void sendSoundPlayPacket(double x, double y, double z, double distance, String soundId) {
		ClientPlayNetworking.send(VillagerMod.SOUND_PLAY_PACKET_ID, new SoundPlayPacket(soundId, x, y, z, distance).toPacketBuf());
	}

	// Returns true if the client player is within <distance> of the sound
	private boolean isSoundNearbyPlayer(double x, double y, double z, double distance) {
			double f;
			double e;
			double d;
			ClientPlayerEntity clientPlayerEntity = this.client.player;
			if (clientPlayerEntity != null) {
				return (d = x - clientPlayerEntity.getX()) * d + (e = y - clientPlayerEntity.getY()) * e + (f = z - clientPlayerEntity.getZ()) * f < distance * distance;
			} else {
				return false;
			}
	}
}
