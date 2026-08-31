package fr.reivaxmc.progress.network;

import io.netty.buffer.ByteBuf;
import java.util.function.Supplier;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.ResourceLocation;

public final class SimplePayloads {
   private SimplePayloads() {
   }

   private static <T extends CustomPacketPayload> StreamCodec<ByteBuf, T> unit(final Supplier<T> s) {
      return new StreamCodec<ByteBuf, T>() {
         public T decode(ByteBuf b) {
            return s.get();
         }

         public void encode(ByteBuf b, T p) {
         }
      };
   }

   private static ResourceLocation id(String s) {
      return ResourceLocation.fromNamespaceAndPath("reivaxmc_progress", s);
   }

   public static record ClaimFragment() implements CustomPacketPayload {
      public static final Type<SimplePayloads.ClaimFragment> TYPE = new Type(SimplePayloads.id("claim_fragment"));
      public static final StreamCodec<ByteBuf, SimplePayloads.ClaimFragment> CODEC = SimplePayloads.unit(SimplePayloads.ClaimFragment::new);

      public Type<? extends CustomPacketPayload> type() {
         return TYPE;
      }
   }

   public static record ClaimSeal() implements CustomPacketPayload {
      public static final Type<SimplePayloads.ClaimSeal> TYPE = new Type(SimplePayloads.id("claim_seal"));
      public static final StreamCodec<ByteBuf, SimplePayloads.ClaimSeal> CODEC = SimplePayloads.unit(SimplePayloads.ClaimSeal::new);

      public Type<? extends CustomPacketPayload> type() {
         return TYPE;
      }
   }

   public static record OpenBook() implements CustomPacketPayload {
      public static final Type<SimplePayloads.OpenBook> TYPE = new Type(SimplePayloads.id("open_book"));
      public static final StreamCodec<ByteBuf, SimplePayloads.OpenBook> CODEC = SimplePayloads.unit(SimplePayloads.OpenBook::new);

      public Type<? extends CustomPacketPayload> type() {
         return TYPE;
      }
   }

   public static record OpenFragment() implements CustomPacketPayload {
      public static final Type<SimplePayloads.OpenFragment> TYPE = new Type(SimplePayloads.id("open_fragment"));
      public static final StreamCodec<ByteBuf, SimplePayloads.OpenFragment> CODEC = SimplePayloads.unit(SimplePayloads.OpenFragment::new);

      public Type<? extends CustomPacketPayload> type() {
         return TYPE;
      }
   }

   public static record OpenMatrix() implements CustomPacketPayload {
      public static final Type<SimplePayloads.OpenMatrix> TYPE = new Type(SimplePayloads.id("open_matrix"));
      public static final StreamCodec<ByteBuf, SimplePayloads.OpenMatrix> CODEC = SimplePayloads.unit(SimplePayloads.OpenMatrix::new);

      public Type<? extends CustomPacketPayload> type() {
         return TYPE;
      }
   }

   public static record OpenReliquary() implements CustomPacketPayload {
      public static final Type<SimplePayloads.OpenReliquary> TYPE = new Type(SimplePayloads.id("open_reliquary"));
      public static final StreamCodec<ByteBuf, SimplePayloads.OpenReliquary> CODEC = SimplePayloads.unit(SimplePayloads.OpenReliquary::new);

      public Type<? extends CustomPacketPayload> type() {
         return TYPE;
      }
   }

   public static record StartIntro() implements CustomPacketPayload {
      public static final Type<SimplePayloads.StartIntro> TYPE = new Type(SimplePayloads.id("start_intro"));
      public static final StreamCodec<ByteBuf, SimplePayloads.StartIntro> CODEC = SimplePayloads.unit(SimplePayloads.StartIntro::new);

      public Type<? extends CustomPacketPayload> type() {
         return TYPE;
      }
   }
}
