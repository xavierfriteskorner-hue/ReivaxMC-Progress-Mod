package fr.reivaxmc.progress.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.ResourceLocation;

public final class Alpha18FPayloads {
   private Alpha18FPayloads() {
   }

   private static ResourceLocation id(String var0) {
      return ResourceLocation.fromNamespaceAndPath("reivaxmc_progress", var0);
   }

   public static record StoryStartRequest() implements CustomPacketPayload {
      public static final Type<Alpha18FPayloads.StoryStartRequest> TYPE = new Type(Alpha18FPayloads.id("story_start_18f"));
      public static final StreamCodec<ByteBuf, Alpha18FPayloads.StoryStartRequest> CODEC = new StreamCodec<ByteBuf, Alpha18FPayloads.StoryStartRequest>() {
         public Alpha18FPayloads.StoryStartRequest decode(ByteBuf var1) {
            return new Alpha18FPayloads.StoryStartRequest();
         }

         public void encode(ByteBuf var1, Alpha18FPayloads.StoryStartRequest var2) {
         }
      };

      public Type<? extends CustomPacketPayload> type() {
         return TYPE;
      }
   }

   public static record StoryStatus(boolean managed, boolean available, boolean started) implements CustomPacketPayload {
      public static final Type<Alpha18FPayloads.StoryStatus> TYPE = new Type(Alpha18FPayloads.id("story_status_18f"));
      public static final StreamCodec<ByteBuf, Alpha18FPayloads.StoryStatus> CODEC = new StreamCodec<ByteBuf, Alpha18FPayloads.StoryStatus>() {
         public Alpha18FPayloads.StoryStatus decode(ByteBuf var1) {
            return new Alpha18FPayloads.StoryStatus(var1.readBoolean(), var1.readBoolean(), var1.readBoolean());
         }

         public void encode(ByteBuf var1, Alpha18FPayloads.StoryStatus var2) {
            var1.writeBoolean(var2.managed());
            var1.writeBoolean(var2.available());
            var1.writeBoolean(var2.started());
         }
      };

      public Type<? extends CustomPacketPayload> type() {
         return TYPE;
      }
   }
}
