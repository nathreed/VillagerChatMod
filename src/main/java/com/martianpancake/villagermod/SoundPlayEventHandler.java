package com.martianpancake.villagermod;

import com.mojang.brigadier.Command;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class SoundPlayEventHandler {

    private Map<String, Date> villagerLastChatRequest = new HashMap<>();

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
        if(nearbyVillager != null && !soundId.equals("minecraft:entity.villager.ambient") && !soundId.contains("step")) {
            VillagerMod.LOGGER.info(String.format("Villager %s heard sound %s", nearbyVillager.getUuidAsString(), soundId));
            this.chatIfAvailable(nearbyVillager.getUuidAsString(), soundId, world);
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

    private void chatIfAvailable(String villagerUUID, String soundId, World world) {
        Date lastRequest = villagerLastChatRequest.getOrDefault(villagerUUID, Date.from(Instant.EPOCH));
        Date now = new Date();
        long secondsElapsed = (now.getTime() - lastRequest.getTime()) / 1000;
        if(secondsElapsed >= 10) {
            // Make request for this villager to ChatGPT
            villagerLastChatRequest.put(villagerUUID, now);
            //String translatedText = Text.translatable(soundId).get().toString();
            String translatedText = soundId;
            try {
                String encodedText = URLEncoder.encode(translatedText, StandardCharsets.UTF_8.toString());
                get(String.format("http://localhost:5000/chatgpt?sound_name=%s", encodedText), villagerUUID, world);
            } catch (UnsupportedEncodingException e) {
                VillagerMod.LOGGER.error("Unsupported encoding");
            }

        }
    }

    private void get(String uri, String villagerUUID, World world) {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenApply((string) -> {
                    world.getServer().getPlayerManager().broadcast(Text.literal(String.format("<%s> %s", villagerUUID, string)), false);
                    return null;
                });
    }
}
