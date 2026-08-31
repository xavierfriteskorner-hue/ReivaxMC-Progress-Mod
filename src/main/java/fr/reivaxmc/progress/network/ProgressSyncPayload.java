package fr.reivaxmc.progress.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.ResourceLocation;

public record ProgressSyncPayload(
   int progress,
   int score,
   boolean introCompleted,
   int targetX,
   int targetY,
   int targetZ,
   String stage,
   String mission,
   String timeline,
   String artifacts,
   String event,
   String kind,
   String title,
   String detail,
   int gained,
   boolean reliquaryOpened,
   boolean matrixDiscovered,
   boolean foundationPlaced,
   boolean migration,
   String foundationName,
   int territoryRadius,
   int historicalCount
) implements CustomPacketPayload {
   public static final Type<ProgressSyncPayload> TYPE = new Type(ResourceLocation.fromNamespaceAndPath("reivaxmc_progress", "sync_v13"));
   public static final StreamCodec<ByteBuf, ProgressSyncPayload> CODEC = new StreamCodec<ByteBuf, ProgressSyncPayload>() {
      public ProgressSyncPayload decode(ByteBuf b) {
         return new ProgressSyncPayload(
            this.i(b),
            this.i(b),
            this.q(b),
            this.i(b),
            this.i(b),
            this.i(b),
            this.s(b),
            this.s(b),
            this.s(b),
            this.s(b),
            this.s(b),
            this.s(b),
            this.s(b),
            this.s(b),
            this.i(b),
            this.q(b),
            this.q(b),
            this.q(b),
            this.q(b),
            this.s(b),
            this.i(b),
            this.i(b)
         );
      }

      public void encode(ByteBuf b, ProgressSyncPayload p) {
         this.i(b, p.progress);
         this.i(b, p.score);
         this.q(b, p.introCompleted);
         this.i(b, p.targetX);
         this.i(b, p.targetY);
         this.i(b, p.targetZ);
         this.s(b, p.stage);
         this.s(b, p.mission);
         this.s(b, p.timeline);
         this.s(b, p.artifacts);
         this.s(b, p.event);
         this.s(b, p.kind);
         this.s(b, p.title);
         this.s(b, p.detail);
         this.i(b, p.gained);
         this.q(b, p.reliquaryOpened);
         this.q(b, p.matrixDiscovered);
         this.q(b, p.foundationPlaced);
         this.q(b, p.migration);
         this.s(b, p.foundationName);
         this.i(b, p.territoryRadius);
         this.i(b, p.historicalCount);
      }

      private int i(ByteBuf b) {
         return (Integer)ByteBufCodecs.VAR_INT.decode(b);
      }

      private void i(ByteBuf b, int v) {
         ByteBufCodecs.VAR_INT.encode(b, v);
      }

      private boolean q(ByteBuf b) {
         return (Boolean)ByteBufCodecs.BOOL.decode(b);
      }

      private void q(ByteBuf b, boolean v) {
         ByteBufCodecs.BOOL.encode(b, v);
      }

      private String s(ByteBuf b) {
         return (String)ByteBufCodecs.STRING_UTF8.decode(b);
      }

      private void s(ByteBuf b, String v) {
         ByteBufCodecs.STRING_UTF8.encode(b, v == null ? "" : v);
      }
   };

   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }
}
