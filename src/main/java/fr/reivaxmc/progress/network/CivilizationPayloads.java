package fr.reivaxmc.progress.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public final class CivilizationPayloads {
   private CivilizationPayloads() {
   }

   public record BuyUpgrade(String id) implements CustomPacketPayload {
      public static final Type<BuyUpgrade> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("reivaxmc_progress", "buy_civilization_upgrade"));
      public static final StreamCodec<ByteBuf, BuyUpgrade> CODEC = new StreamCodec<>() {
         @Override public BuyUpgrade decode(ByteBuf buffer) { return new BuyUpgrade(ByteBufCodecs.STRING_UTF8.decode(buffer)); }
         @Override public void encode(ByteBuf buffer, BuyUpgrade payload) { ByteBufCodecs.STRING_UTF8.encode(buffer, payload.id == null ? "" : payload.id); }
      };
      @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
   }
}
