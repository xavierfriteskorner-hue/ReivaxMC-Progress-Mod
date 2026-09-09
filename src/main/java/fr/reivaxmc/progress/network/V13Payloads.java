package fr.reivaxmc.progress.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public final class V13Payloads {
   private V13Payloads(){}
   private static ResourceLocation id(String p){return ResourceLocation.fromNamespaceAndPath("reivaxmc_progress",p);}
   public record Follow() implements CustomPacketPayload {public static final Type<Follow> TYPE=new Type<>(id("follow_chapter4"));public static final StreamCodec<ByteBuf,Follow> CODEC=StreamCodec.unit(new Follow());public Type<? extends CustomPacketPayload> type(){return TYPE;}}
   public record Action(String action,String value) implements CustomPacketPayload {public static final Type<Action> TYPE=new Type<>(id("chapter4_action"));public static final StreamCodec<ByteBuf,Action> CODEC=new StreamCodec<>(){public Action decode(ByteBuf b){return new Action(s(b),s(b));}public void encode(ByteBuf b,Action p){w(b,p.action);w(b,p.value);}};public Type<? extends CustomPacketPayload> type(){return TYPE;}}
   public record OpenWitness(int index,String title,String account,String personal,String fracture) implements CustomPacketPayload {public static final Type<OpenWitness> TYPE=new Type<>(id("open_witness_v13"));public static final StreamCodec<ByteBuf,OpenWitness> CODEC=new StreamCodec<>(){public OpenWitness decode(ByteBuf b){return new OpenWitness(ByteBufCodecs.VAR_INT.decode(b),s(b),s(b),s(b),s(b));}public void encode(ByteBuf b,OpenWitness p){ByteBufCodecs.VAR_INT.encode(b,p.index);w(b,p.title);w(b,p.account);w(b,p.personal);w(b,p.fracture);}};public Type<? extends CustomPacketPayload> type(){return TYPE;}}
   public record OpenMatrix(String stage,String heading,String body,String personal,String policy,boolean canAnalyze,boolean canAcknowledge) implements CustomPacketPayload {public static final Type<OpenMatrix> TYPE=new Type<>(id("open_matrix_v13"));public static final StreamCodec<ByteBuf,OpenMatrix> CODEC=new StreamCodec<>(){public OpenMatrix decode(ByteBuf b){return new OpenMatrix(s(b),s(b),s(b),s(b),s(b),b.readBoolean(),b.readBoolean());}public void encode(ByteBuf b,OpenMatrix p){w(b,p.stage);w(b,p.heading);w(b,p.body);w(b,p.personal);w(b,p.policy);b.writeBoolean(p.canAnalyze);b.writeBoolean(p.canAcknowledge);}};public Type<? extends CustomPacketPayload> type(){return TYPE;}}
   private static String s(ByteBuf b){return ByteBufCodecs.STRING_UTF8.decode(b);}private static void w(ByteBuf b,String s){ByteBufCodecs.STRING_UTF8.encode(b,s==null?"":s);}
}
