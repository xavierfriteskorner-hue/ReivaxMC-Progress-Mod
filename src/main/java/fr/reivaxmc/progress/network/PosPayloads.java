package fr.reivaxmc.progress.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.ResourceLocation;

public final class PosPayloads {
   private PosPayloads() {
   }

   private static <T extends CustomPacketPayload> StreamCodec<ByteBuf, T> codec(final PosPayloads.Tri<T> f) {
      return new StreamCodec<ByteBuf, T>() {
         public T decode(ByteBuf b) {
            return f.make((Integer)ByteBufCodecs.VAR_INT.decode(b), (Integer)ByteBufCodecs.VAR_INT.decode(b), (Integer)ByteBufCodecs.VAR_INT.decode(b));
         }

         public void encode(ByteBuf b, T p) {
            int[] a = PosPayloads.xyz(p);
            ByteBufCodecs.VAR_INT.encode(b, a[0]);
            ByteBufCodecs.VAR_INT.encode(b, a[1]);
            ByteBufCodecs.VAR_INT.encode(b, a[2]);
         }
      };
   }

   private static int[] xyz(Object p) {
      if (p instanceof PosPayloads.OpenFoundation q) {
         return new int[]{q.x, q.y, q.z};
      } else if (p instanceof PosPayloads.ConfirmFoundation q) {
         return new int[]{q.x, q.y, q.z};
      } else if (p instanceof PosPayloads.OpenTransfer q) {
         return new int[]{q.x, q.y, q.z};
      } else {
         return p instanceof PosPayloads.ConfirmTransfer q ? new int[]{q.x, q.y, q.z} : new int[3];
      }
   }

   private static ResourceLocation id(String s) {
      return ResourceLocation.fromNamespaceAndPath("reivaxmc_progress", s);
   }

   public static record ConfirmFoundation(int x, int y, int z) implements CustomPacketPayload {
      public static final Type<PosPayloads.ConfirmFoundation> TYPE = new Type(PosPayloads.id("confirm_foundation"));
      public static final StreamCodec<ByteBuf, PosPayloads.ConfirmFoundation> CODEC = PosPayloads.codec(PosPayloads.ConfirmFoundation::new);

      public Type<? extends CustomPacketPayload> type() {
         return TYPE;
      }
   }

   public static record ConfirmTransfer(int x, int y, int z) implements CustomPacketPayload {
      public static final Type<PosPayloads.ConfirmTransfer> TYPE = new Type(PosPayloads.id("confirm_transfer"));
      public static final StreamCodec<ByteBuf, PosPayloads.ConfirmTransfer> CODEC = PosPayloads.codec(PosPayloads.ConfirmTransfer::new);

      public Type<? extends CustomPacketPayload> type() {
         return TYPE;
      }
   }

   public static record OpenFoundation(int x, int y, int z) implements CustomPacketPayload {
      public static final Type<PosPayloads.OpenFoundation> TYPE = new Type(PosPayloads.id("open_foundation"));
      public static final StreamCodec<ByteBuf, PosPayloads.OpenFoundation> CODEC = PosPayloads.codec(PosPayloads.OpenFoundation::new);

      public Type<? extends CustomPacketPayload> type() {
         return TYPE;
      }
   }

   public static record OpenTransfer(int x, int y, int z) implements CustomPacketPayload {
      public static final Type<PosPayloads.OpenTransfer> TYPE = new Type(PosPayloads.id("open_transfer"));
      public static final StreamCodec<ByteBuf, PosPayloads.OpenTransfer> CODEC = PosPayloads.codec(PosPayloads.OpenTransfer::new);

      public Type<? extends CustomPacketPayload> type() {
         return TYPE;
      }
   }

   private interface Tri<T> {
      T make(int var1, int var2, int var3);
   }
}
