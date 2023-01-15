package com.martianpancake.villagermod;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SoundPlayUtil {

    public static void evaluateSoundForNearbyVillagersAndProcess(World world, double x, double y, double z, double soundDistance, String soundId) {
        VillagerMod.LOGGER.info(String.format("received packet for sound played at %.1f, %.1f, %.1f: %s", x, y, z, soundId));
        VillagerEntity nearbyVillager = SoundPlayUtil.oneNearbyVillager(world, x, y, z, soundDistance);
        if(nearbyVillager != null) {
            VillagerMod.LOGGER.info(String.format("Villager %s heard sound %s", nearbyVillager.getUuidAsString(), soundId));
        }
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
