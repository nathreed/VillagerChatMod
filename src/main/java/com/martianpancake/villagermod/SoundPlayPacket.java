package com.martianpancake.villagermod;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.PacketByteBuf;

public class SoundPlayPacket {
    public final String soundId;
    public final double x;
    public final double y;
    public final double z;
    public final double distance;

    public SoundPlayPacket(String soundId, double x, double y, double z, double distance) {
        this.soundId = soundId;
        this.x = x;
        this.y = y;
        this.z = z;
        this.distance = distance;
    }

    public SoundPlayPacket(PacketByteBuf buf) {
        this.soundId = buf.readString();
        this.x = buf.readDouble();
        this.y = buf.readDouble();
        this.z = buf.readDouble();
        this.distance = buf.readDouble();
    }

    public PacketByteBuf toPacketBuf() {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeString(this.soundId);
        buf.writeDouble(this.x);
        buf.writeDouble(this.y);
        buf.writeDouble(this.z);
        buf.writeDouble(this.distance);

        return buf;
    }
}
