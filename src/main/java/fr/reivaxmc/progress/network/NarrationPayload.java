package fr.reivaxmc.progress.network;

import fr.reivaxmc.progress.ReivaxMCProgress;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Le paquet serveur -> client qui porte une intervention de la Voix vers le panneau HUD.
 */
public record NarrationPayload(String title, String text, int points) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<NarrationPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(ReivaxMCProgress.MODID, "narration"));

    public static final StreamCodec<ByteBuf, NarrationPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, NarrationPayload::title,
            ByteBufCodecs.STRING_UTF8, NarrationPayload::text,
            ByteBufCodecs.VAR_INT, NarrationPayload::points,
            NarrationPayload::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
