package com.martianpancake.villagermod;

import com.mojang.brigadier.Command;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SoundPlayEventHandler {

    // Called when the server receives a packet that a client has played a sound
    public void clientPlayedSound(ServerWorld world, SoundPlayPacket packet) {
        world.getServer().execute(() -> {
            // todo refactor this, it doesn't need to be static and we can bring it in here
            this.evaluateSoundForNearbyVillagersAndProcess(world, packet.x, packet.y, packet.z, packet.distance, packet.soundId);
        });
    }

    private void evaluateSoundForNearbyVillagersAndProcess(World world, double x, double y, double z, double soundDistance, String soundId) {
        VillagerMod.LOGGER.info(String.format("received packet for sound played at %.1f, %.1f, %.1f: %s", x, y, z, soundId));
        VillagerEntity nearbyVillager = this.oneNearbyVillager(world, x, y, z, soundDistance);
        if(nearbyVillager != null && !soundId.equals("minecraft:entity.villager.ambient")) {
            VillagerMod.LOGGER.info(String.format("Villager %s heard sound %s", nearbyVillager.getUuidAsString(), soundId));
            world.getServer().getPlayerManager().broadcast(Text.literal(String.format("<%s> I heard %s", nearbyVillager.getUuidAsString(), soundId)), false);
        }
    }


    private @Nullable VillagerEntity oneNearbyVillager(World world, double x, double y, double z, double distance) {
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
