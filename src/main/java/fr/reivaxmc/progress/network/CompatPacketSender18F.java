package fr.reivaxmc.progress.network;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import net.minecraft.server.level.ServerPlayer;

public final class CompatPacketSender18F {
   private static volatile Method SEND_TO_PLAYER;
   private static volatile Class<?> PAYLOAD_CLASS;
   private static volatile Class<?> PAYLOAD_ARRAY_CLASS;

   private CompatPacketSender18F() {
   }

   public static void sendToPlayer(ServerPlayer var0, Object var1) {
      if (var0 != null && var1 != null) {
         try {
            Method var2 = SEND_TO_PLAYER;
            Class var11 = PAYLOAD_CLASS;
            Class var13 = PAYLOAD_ARRAY_CLASS;
            if (var2 == null || var11 == null || var13 == null) {
               synchronized (CompatPacketSender18F.class) {
                  var2 = SEND_TO_PLAYER;
                  var11 = PAYLOAD_CLASS;
                  var13 = PAYLOAD_ARRAY_CLASS;
                  if (var2 == null || var11 == null || var13 == null) {
                     var11 = Class.forName("net.minecraft.network.protocol.common.custom.CustomPacketPayload");
                     var13 = Array.newInstance(var11, 0).getClass();
                     Class var6 = Class.forName("net.neoforged.neoforge.network.PacketDistributor");
                     var2 = var6.getMethod("sendToPlayer", ServerPlayer.class, var11, var13);
                     var2.setAccessible(true);
                     PAYLOAD_CLASS = var11;
                     PAYLOAD_ARRAY_CLASS = var13;
                     SEND_TO_PLAYER = var2;
                  }
               }
            }

            if (!var11.isInstance(var1)) {
               throw new IllegalArgumentException("Payload does not implement CustomPacketPayload: " + var1.getClass().getName());
            } else {
               Object var16 = Array.newInstance(var11, 0);
               var2.invoke(null, var0, var1, var16);
            }
         } catch (InvocationTargetException var9) {
            Throwable var3 = var9.getCause();
            if (var3 instanceof RuntimeException var12) {
               throw var12;
            } else if (var3 instanceof Error var4) {
               throw var4;
            } else {
               throw new RuntimeException("NeoForge sendToPlayer failed", var3);
            }
         } catch (ReflectiveOperationException var10) {
            throw new RuntimeException("Cannot resolve NeoForge 21.1.x PacketDistributor.sendToPlayer", var10);
         }
      }
   }
}
