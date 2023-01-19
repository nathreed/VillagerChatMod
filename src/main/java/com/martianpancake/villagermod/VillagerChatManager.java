package com.martianpancake.villagermod;

import net.minecraft.text.Text;
import net.minecraft.world.World;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VillagerChatManager {

    // names from ChatGPT: "generate a list of 5 male names, 5 female names, and 5 gender-neutral names, all with a medieval theme"
    private final String[] villagerNames = {"William", "Richard", "Robert", "Charles", "Edward", "Elizabeth", "Margaret", "Catherine", "Anne", "Joan", "Robin", "Sterling", "Galen", "Rowan", "Finley"};
    // uninteresting sounds from manual testing
    private final String[] uninterestingSoundRegexes = {"bat", "entity\\.villager\\.ambient", "step", "bucket\\.use", "entity\\.item\\.pickup", "entity\\.player\\.attack"};
    // keeps track of the last time each villager chatted to prevent chat spam
    private final Map<String, Date> villagerLastChatRequest = new HashMap<>();
    // keeps track of assigned names given villager UUID
    private final Map<String, String> villagerNameMap = new HashMap<>();
    private final Deque<String> mostRecentSounds = new ArrayDeque<>();

    void chatIfAvailable(String villagerUUID, String soundId, World world) {
        Date lastRequest = villagerLastChatRequest.getOrDefault(villagerUUID, Date.from(Instant.EPOCH));
        Date now = new Date();
        long secondsElapsed = (now.getTime() - lastRequest.getTime()) / 1000;
        if(isSoundInteresting(soundId) && secondsElapsed >= 10 && !mostRecentSounds.contains(soundId)) {
            // Make request for this villager to ChatGPT
            // It will get put in the chat when the response comes back from the server
            villagerLastChatRequest.put(villagerUUID, now);
            if(mostRecentSounds.size() > 5) {
                mostRecentSounds.removeLast();
            }
            mostRecentSounds.addFirst(soundId);
            chatResultFromGPT(soundId, nameForVillager(villagerUUID), world);
        }
    }

    private boolean isSoundInteresting(String soundId) {
        for (String uninterestingSound : uninterestingSoundRegexes) {
            Pattern pattern = Pattern.compile(uninterestingSound);
            Matcher matcher = pattern.matcher(soundId);
            if(matcher.find()) {
                return false;
            }
        }
        return true;
    }

    private String nameForVillager(String villagerUUID) {
        String villagerName = villagerNameMap.getOrDefault(villagerUUID, villagerNames[new Random().nextInt(villagerNames.length)]);
        villagerNameMap.put(villagerUUID, villagerName);
        return villagerName;
    }

    public void askVillagerQuestion(String villagerName, String question, World world) {
        String uri = "";
        try {
            String encodedText = URLEncoder.encode(question, StandardCharsets.UTF_8.toString());
            uri = String.format("http://localhost:5000/talk_to_villager?message=%s", encodedText);
        } catch (UnsupportedEncodingException e) {
            VillagerMod.LOGGER.error("Unsupported encoding");
        }
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenApply((string) -> {
                    world.getServer().execute(() -> {
                        world.getServer().getPlayerManager().broadcast(Text.literal(String.format("<Villager %s> %s", villagerName, string)), false);
                    });
                    return null;
                });
    }

    private void chatResultFromGPT(String soundId, String villagerName, World world) {
        String translatedText = Text.translatable(soundId).asTruncatedString(Integer.MAX_VALUE);
        String uri = "";
        try {
            String encodedText = URLEncoder.encode(translatedText, StandardCharsets.UTF_8.toString());
            uri = String.format("http://localhost:5000/chatgpt?sound_name=%s", encodedText);
        } catch (UnsupportedEncodingException e) {
            VillagerMod.LOGGER.error("Unsupported encoding");
        }
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenApply((string) -> {
                    world.getServer().execute(() -> {
                        world.getServer().getPlayerManager().broadcast(Text.literal(String.format("<Villager %s> %s", villagerName, string)), false);
                    });
                    return null;
                });
    }
}
