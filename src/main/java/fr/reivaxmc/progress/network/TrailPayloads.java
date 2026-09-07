package fr.reivaxmc.progress.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Actions réseau du moteur de Pistes. */
public final class TrailPayloads {
   private TrailPayloads() {
   }

   public record Follow() implements CustomPacketPayload {
      public static final Type<Follow> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("reivaxmc_progress", "follow_trail"));
      public static final StreamCodec<FriendlyByteBuf, Follow> CODEC = StreamCodec.unit(new Follow());
      @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
   }
}
