package fr.reivaxmc.progress.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Paquets du Conseil du Foyer. Le serveur reste seul juge de la décision. */
public final class CouncilPayloads {
   private CouncilPayloads() {
   }

   public record CastVote(String doctrine) implements CustomPacketPayload {
      public static final Type<CastVote> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("reivaxmc_progress", "cast_council_vote"));
      public static final StreamCodec<ByteBuf, CastVote> CODEC = new StreamCodec<>() {
         @Override public CastVote decode(ByteBuf buffer) { return new CastVote(ByteBufCodecs.STRING_UTF8.decode(buffer)); }
         @Override public void encode(ByteBuf buffer, CastVote payload) {
            ByteBufCodecs.STRING_UTF8.encode(buffer, payload.doctrine == null ? "" : payload.doctrine);
         }
      };

      @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
   }
}
