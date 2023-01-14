package com.martianpancake.villagermod.mixin;

import com.martianpancake.villagermod.SoundPlayUtil;
import com.martianpancake.villagermod.VillagerMod;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.MinecraftServer;
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

import java.util.function.Supplier;

@Mixin(ServerWorld.class)
public abstract class WorldPlaySoundMixin extends World {

	@Shadow @Final private MinecraftServer server;

	protected WorldPlaySoundMixin(MutableWorldProperties properties, RegistryKey<World> registryRef, RegistryEntry<DimensionType> dimension, Supplier<Profiler> profiler, boolean isClient, boolean debugWorld, long seed, int maxChainedNeighborUpdates) {
		super(properties, registryRef, dimension, profiler, isClient, debugWorld, seed, maxChainedNeighborUpdates);
	}

	@Inject(at = @At("HEAD"), method = "playSound")
	private void init(@Nullable PlayerEntity except, double x, double y, double z, RegistryEntry<SoundEvent> sound, SoundCategory category, float volume, float pitch, long seed, CallbackInfo info) {
		double soundDistance = sound.value().getDistanceToTravel(volume);
		SoundPlayUtil.evaluateSoundForNearbyVillagersAndProcess(this.server.getPlayerManager(), this, x, y, z, soundDistance, sound.value());
	}

	@Inject(at = @At("HEAD"), method = "playSoundFromEntity")
	private void init(@Nullable PlayerEntity except, Entity entity, RegistryEntry<SoundEvent> sound, SoundCategory category, float volume, float pitch, long seed, CallbackInfo info) {
		double soundDistance = sound.value().getDistanceToTravel(volume);
		double x = entity.getX();
		double y = entity.getY();
		double z = entity.getZ();
		VillagerMod.LOGGER.info(String.format("play sound from entity sound %s %.1f %.1f %.1f", sound.value().getId(), x, y, z));
		SoundPlayUtil.evaluateSoundForNearbyVillagersAndProcess(this.server.getPlayerManager(), this, x, y, z, soundDistance, sound.value());
	}
}
