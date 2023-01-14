package com.martianpancake.villagermod.mixin;

import com.martianpancake.villagermod.VillagerMod;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.Box;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

@Mixin(ServerWorld.class)
public abstract class VillagerPlaySoundMixin extends World {

	@Shadow @Final private MinecraftServer server;

	protected VillagerPlaySoundMixin(MutableWorldProperties properties, RegistryKey<World> registryRef, RegistryEntry<DimensionType> dimension, Supplier<Profiler> profiler, boolean isClient, boolean debugWorld, long seed, int maxChainedNeighborUpdates) {
		super(properties, registryRef, dimension, profiler, isClient, debugWorld, seed, maxChainedNeighborUpdates);
	}

	@Inject(at = @At("HEAD"), method = "playSound")
	private void init(@Nullable PlayerEntity except, double x, double y, double z, RegistryEntry<SoundEvent> sound, SoundCategory category, float volume, float pitch, long seed, CallbackInfo info) {
		double soundDistance = sound.value().getDistanceToTravel(volume);
		if(arePlayersNearby(x, y, z, soundDistance)) {
			VillagerMod.LOGGER.info(String.format("ServerWorld playSound injected hook at %.1f, %.1f, %.1f %s", x, y, z, sound.value().getId()));
			VillagerEntity nearbyVillager = oneNearbyVillager(x, y, z, soundDistance);
			if(nearbyVillager != null) {
				VillagerMod.LOGGER.info(String.format("Villager %s heard sound %s", nearbyVillager.getUuidAsString(), sound.value().getId()));
			}
		}

	}

	// Returns true if at least one player is nearby the given coordinate with the given distance
	private boolean arePlayersNearby(double x, double y, double z, double distance) {
		PlayerManager pm = this.server.getPlayerManager();
		List<ServerPlayerEntity> allPlayers = pm.getPlayerList();
		for (int i = 0; i < allPlayers.size(); ++i) {
			double f;
			double e;
			double d;
			ServerPlayerEntity serverPlayerEntity = allPlayers.get(i);
			if (serverPlayerEntity.world.getRegistryKey() != this.getRegistryKey() || !((d = x - serverPlayerEntity.getX()) * d + (e = y - serverPlayerEntity.getY()) * e + (f = z - serverPlayerEntity.getZ()) * f < distance * distance)) continue;
			return true;
		}
		return false;
	}

	private @Nullable VillagerEntity oneNearbyVillager(double x, double y, double z, double distance) {
		Box distanceBox = new Box(x - distance, y - distance, z - distance, x + distance, y + distance, z + distance);
		List<? super VillagerEntity> villagersList = new ArrayList<>();
		this.collectEntitiesByType(EntityType.VILLAGER, distanceBox, (Objects::nonNull), villagersList, 1);
		if (villagersList.size() == 1) {
			return (VillagerEntity) villagersList.get(0);
		} else {
			return null;
		}
	}

}
