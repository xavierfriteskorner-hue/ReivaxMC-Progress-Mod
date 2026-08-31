package fr.reivaxmc.progress.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.ResourceLocation;

public record MemorialInfoPayload(String text) implements CustomPacketPayload {
   public static final Type<MemorialInfoPayload> TYPE = new Type(ResourceLocation.fromNamespaceAndPath("reivaxmc_progress", "memorial_info"));
   public static final StreamCodec<ByteBuf, MemorialInfoPayload> CODEC = new StreamCodec<ByteBuf, MemorialInfoPayload>() {
      public MemorialInfoPayload decode(ByteBuf b) {
         return new MemorialInfoPayload((String)ByteBufCodecs.STRING_UTF8.decode(b));
      }

      public void encode(ByteBuf b, MemorialInfoPayload p) {
         ByteBufCodecs.STRING_UTF8.encode(b, p.text);
      }
   };

   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }
}
