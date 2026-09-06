package fr.reivaxmc.progress.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public final class JournalPayloads {
   private JournalPayloads() {
   }

   private static ResourceLocation id(String path) {
      return ResourceLocation.fromNamespaceAndPath("reivaxmc_progress", path);
   }

   public record OpenJournal(String chronicle, String shared, String personal) implements CustomPacketPayload {
      public static final Type<OpenJournal> TYPE = new Type<>(id("open_inherited_journal"));
      public static final StreamCodec<ByteBuf, OpenJournal> CODEC = new StreamCodec<>() {
         @Override public OpenJournal decode(ByteBuf buffer) {
            return new OpenJournal(ByteBufCodecs.STRING_UTF8.decode(buffer), ByteBufCodecs.STRING_UTF8.decode(buffer), ByteBufCodecs.STRING_UTF8.decode(buffer));
         }
         @Override public void encode(ByteBuf buffer, OpenJournal payload) {
            ByteBufCodecs.STRING_UTF8.encode(buffer, payload.chronicle == null ? "" : payload.chronicle);
            ByteBufCodecs.STRING_UTF8.encode(buffer, payload.shared == null ? "" : payload.shared);
            ByteBufCodecs.STRING_UTF8.encode(buffer, payload.personal == null ? "" : payload.personal);
         }
      };
      @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
   }

   public record SaveNote(String text, boolean shared) implements CustomPacketPayload {
      public static final Type<SaveNote> TYPE = new Type<>(id("save_journal_note"));
      public static final StreamCodec<ByteBuf, SaveNote> CODEC = new StreamCodec<>() {
         @Override public SaveNote decode(ByteBuf buffer) {
            return new SaveNote(ByteBufCodecs.STRING_UTF8.decode(buffer), ByteBufCodecs.BOOL.decode(buffer));
         }
         @Override public void encode(ByteBuf buffer, SaveNote payload) {
            ByteBufCodecs.STRING_UTF8.encode(buffer, payload.text == null ? "" : payload.text);
            ByteBufCodecs.BOOL.encode(buffer, payload.shared);
         }
      };
      @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
   }
}
