package com.martianpancake.villagermod;

import net.minecraft.client.sound.Sound;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SoundPlayUtil {

    public static void evaluateSoundForNearbyVillagersAndProcess(PlayerManager pm, World world, double x, double y, double z, double soundDistance, SoundEvent sound) {
        if(SoundPlayUtil.arePlayersNearby(pm, world.getRegistryKey(), x, y, z, soundDistance)) {
            VillagerMod.LOGGER.info(String.format("ServerWorld playSound injected hook at %.1f, %.1f, %.1f %s", x, y, z, sound.getId()));
            VillagerEntity nearbyVillager = SoundPlayUtil.oneNearbyVillager(world, x, y, z, soundDistance);
            if(nearbyVillager != null) {
                VillagerMod.LOGGER.info(String.format("Villager %s heard sound %s", nearbyVillager.getUuidAsString(), sound.getId()));
            }
        }
    }

    // Returns true if at least one player is nearby the given coordinate with the given distance
    public static boolean arePlayersNearby(PlayerManager pm, RegistryKey<World> worldRegistryKey, double x, double y, double z, double distance) {
        List<ServerPlayerEntity> allPlayers = pm.getPlayerList();
        for (int i = 0; i < allPlayers.size(); ++i) {
            double f;
            double e;
            double d;
            ServerPlayerEntity serverPlayerEntity = allPlayers.get(i);
            if (serverPlayerEntity.world.getRegistryKey() != worldRegistryKey || !((d = x - serverPlayerEntity.getX()) * d + (e = y - serverPlayerEntity.getY()) * e + (f = z - serverPlayerEntity.getZ()) * f < distance * distance)) continue;
            return true;
        }
        return false;
    }

    public static @Nullable VillagerEntity oneNearbyVillager(World world, double x, double y, double z, double distance) {
        Box distanceBox = new Box(x - distance, y - distance, z - distance, x + distance, y + distance, z + distance);
        List<? super VillagerEntity> villagersList = new ArrayList<>();
        world.collectEntitiesByType(EntityType.VILLAGER, distanceBox, (Objects::nonNull), villagersList, 1);
        if (villagersList.size() == 1) {
            return (VillagerEntity) villagersList.get(0);
        } else {
            return null;
        }
    }
}