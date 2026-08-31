package fr.reivaxmc.progress.story;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public final class DuoStartGate18F {
   private static final double DUO_RADIUS_SQ = 1024.0;
   private static final Map<MinecraftServer, Set<UUID>> READY = Collections.synchronizedMap(new WeakHashMap<>());

   private DuoStartGate18F() {
   }

   public static void requestStart(ServerPlayer var0) {
      if (var0 != null) {
         MinecraftServer var1 = var0.getServer();
         if (var1 != null && var1.getPlayerList() != null) {
            List var2 = var1.getPlayerList().getPlayers();
            if (var2 != null && var2.size() > 1) {
               ServerPlayer var3 = findNearbyPartner(var0, var2, null);
               if (var3 == null) {
                  StoryOpening18F.message(var0, "§6[REIVAX] §7Mode §fDUO §7détecté — rapprochez-vous à moins de 32 blocs avant de confirmer.", false);
               } else {
                  Set var4 = READY.computeIfAbsent(var1, var0x -> Collections.synchronizedSet(new HashSet<>()));
                  var4.add(var0.getUUID());
                  ServerPlayer var5 = findNearbyPartner(var0, var2, var4);
                  if (var5 == null) {
                     StoryOpening18F.message(var0, "§a[REIVAX] §fVous êtes prêt. §7En attente de l'autre joueur...", false);
                     StoryOpening18F.message(var3, "§6[REIVAX] §7Votre partenaire est prêt. Ouvrez REIVAX et confirmez à votre tour.", false);
                  } else {
                     READY.remove(var1);
                     StoryOpening18F.message(var0, "§a[REIVAX] §fDUO prêt — lancement de l'histoire...", false);
                     StoryOpening18F.message(var5, "§a[REIVAX] §fDUO prêt — lancement de l'histoire...", false);
                     StoryOpening18F.requestStart(var0);
                  }
               }
            } else {
               READY.remove(var1);
               StoryOpening18F.message(var0, "§8[REIVAX] §7Mode §fSOLO §7détecté.", false);
               StoryOpening18F.requestStart(var0);
            }
         }
      }
   }

   private static ServerPlayer findNearbyPartner(ServerPlayer var0, List<ServerPlayer> var1, Set<UUID> var2) {
      ServerPlayer var3 = null;
      double var4 = Double.MAX_VALUE;
      UUID var6 = var0.getUUID();

      for (ServerPlayer var8 : var1) {
         if (var8 != null) {
            UUID var9 = var8.getUUID();
            if (var9 != null && !var9.equals(var6) && (var2 == null || var2.contains(var9))) {
               double var10 = distanceSq(var0, var8);
               if (var10 <= 1024.0 && var10 < var4) {
                  var3 = var8;
                  var4 = var10;
               }
            }
         }
      }

      return var3;
   }

   private static double distanceSq(ServerPlayer var0, ServerPlayer var1) {
      double var2 = var0.getX() - var1.getX();
      double var4 = var0.getY() - var1.getY();
      double var6 = var0.getZ() - var1.getZ();
      return var2 * var2 + var4 * var4 + var6 * var6;
   }
}
