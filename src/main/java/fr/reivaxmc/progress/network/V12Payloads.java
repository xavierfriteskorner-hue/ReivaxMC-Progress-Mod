package fr.reivaxmc.progress.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public final class V12Payloads {
   private V12Payloads(){}
   private static ResourceLocation id(String p){return ResourceLocation.fromNamespaceAndPath("reivaxmc_progress",p);}
   public record OpenRegistry(String stage,String perception,String census,String testimonies,String versions,String memory) implements CustomPacketPayload {
      public static final Type<OpenRegistry> TYPE=new Type<>(id("open_registry_v12"));
      public static final StreamCodec<ByteBuf,OpenRegistry> CODEC=new StreamCodec<>(){
         public OpenRegistry decode(ByteBuf b){return new OpenRegistry(s(b),s(b),s(b),s(b),s(b),s(b));}
         public void encode(ByteBuf b,OpenRegistry p){w(b,p.stage);w(b,p.perception);w(b,p.census);w(b,p.testimonies);w(b,p.versions);w(b,p.memory);}
      };
      public Type<? extends CustomPacketPayload> type(){return TYPE;}
   }
   public record Action(String action,String value) implements CustomPacketPayload {
      public static final Type<Action> TYPE=new Type<>(id("registry_action_v12"));
      public static final StreamCodec<ByteBuf,Action> CODEC=new StreamCodec<>(){
         public Action decode(ByteBuf b){return new Action(s(b),s(b));}
         public void encode(ByteBuf b,Action p){w(b,p.action);w(b,p.value);}
      };
      public Type<? extends CustomPacketPayload> type(){return TYPE;}
   }
   public record Follow() implements CustomPacketPayload {
      public static final Type<Follow> TYPE=new Type<>(id("follow_chapter3")); public static final StreamCodec<ByteBuf,Follow> CODEC=StreamCodec.unit(new Follow());
      public Type<? extends CustomPacketPayload> type(){return TYPE;}
   }
   private static String s(ByteBuf b){return ByteBufCodecs.STRING_UTF8.decode(b);} private static void w(ByteBuf b,String s){ByteBufCodecs.STRING_UTF8.encode(b,s==null?"":s);}
}
