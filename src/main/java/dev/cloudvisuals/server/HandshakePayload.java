package dev.cloudvisuals.server;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/**
 * Simple C2S handshake payload that tells the server
 * "this player is running CloudVisuals".
 */
public record HandshakePayload() implements CustomPayload {

    public static final CustomPayload.Id<HandshakePayload> ID =
            new CustomPayload.Id<>(Identifier.of("cloudvisuals", "handshake"));

    public static final PacketCodec<PacketByteBuf, HandshakePayload> CODEC =
            PacketCodec.unit(new HandshakePayload());

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
