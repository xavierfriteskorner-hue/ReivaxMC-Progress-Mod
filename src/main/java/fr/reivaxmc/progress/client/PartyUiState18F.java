package fr.reivaxmc.progress.client;

import java.lang.reflect.Method;
import java.util.Collection;

public final class PartyUiState18F {
   private static final long REFRESH_NS = 250000000L;
   private static volatile int cachedOnline = 1;
   private static volatile long nextRefreshNs = 0L;
   private static volatile boolean reflectionResolved = false;
   private static Method minecraftGetInstance;
   private static Method minecraftGetConnection;
   private static Method connectionGetOnlinePlayers;

   private PartyUiState18F() {
   }

   public static String modeLine() {
      int var0 = onlineCount();
      return var0 <= 1 ? "MODE SOLO · 1 joueur dans la partie" : "MODE DUO · " + var0 + " joueurs dans la partie";
   }

   public static String idleButtonLabel() {
      return onlineCount() <= 1 ? "COMMENCER L'HISTOIRE" : "JE SUIS PRÊT";
   }

   public static String pendingButtonLabel() {
      return onlineCount() <= 1 ? "LANCEMENT..." : "PRÊT — EN ATTENTE...";
   }

   public static int onlineCount() {
      long var0 = System.nanoTime();
      if (var0 < nextRefreshNs) {
         return cachedOnline;
      } else {
         synchronized (PartyUiState18F.class) {
            var0 = System.nanoTime();
            if (var0 < nextRefreshNs) {
               return cachedOnline;
            } else {
               int var3 = readOnlineCount();
               if (var3 > 0) {
                  cachedOnline = var3;
               }

               nextRefreshNs = var0 + 250000000L;
               return cachedOnline;
            }
         }
      }
   }

   private static int readOnlineCount() {
      try {
         resolveReflection();
         Object var0 = minecraftGetInstance.invoke(null);
         if (var0 == null) {
            return cachedOnline;
         } else {
            Object var1 = minecraftGetConnection.invoke(var0);
            if (var1 == null) {
               return 1;
            } else {
               if (connectionGetOnlinePlayers == null || !connectionGetOnlinePlayers.getDeclaringClass().isInstance(var1)) {
                  connectionGetOnlinePlayers = var1.getClass().getMethod("getOnlinePlayers");
               }

               return connectionGetOnlinePlayers.invoke(var1) instanceof Collection var3 ? Math.max(1, var3.size()) : cachedOnline;
            }
         }
      } catch (Throwable var4) {
         return 1;
      }
   }

   private static void resolveReflection() throws Exception {
      if (!reflectionResolved) {
         synchronized (PartyUiState18F.class) {
            if (!reflectionResolved) {
               Class var1 = Class.forName("net.minecraft.client.Minecraft");
               minecraftGetInstance = var1.getMethod("getInstance");
               minecraftGetConnection = var1.getMethod("getConnection");
               reflectionResolved = true;
            }
         }
      }
   }
}
