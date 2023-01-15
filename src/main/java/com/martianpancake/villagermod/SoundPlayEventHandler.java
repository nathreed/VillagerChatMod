package com.martianpancake.villagermod;

import net.minecraft.server.world.ServerWorld;

public class SoundPlayEventHandler {

    public void clientPlayedSound(ServerWorld world, SoundPlayPacket packet) {
        // todo refactor this, it doesn't need to be static and we can bring it in here
        SoundPlayUtil.evaluateSoundForNearbyVillagersAndProcess(world, packet.x, packet.y, packet.z, packet.distance, packet.soundId);
    }
}
