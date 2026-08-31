package fr.reivaxmc.progress.story;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent.Post;

public final class Alpha18Age1Bridge {
   private Alpha18Age1Bridge() {
   }

   @SubscribeEvent
   public static void onLogin(PlayerLoggedInEvent var0) {
      bridge(var0);
   }

   @SubscribeEvent
   public static void onPlayerTick(Post var0) {
      try {
         Object var1 = Alpha18Probe.invokeNoArg(var0, "getEntity");
         if (var1 == null || !isServerPlayer(var1)) {
            return;
         }

         int var2 = intField(var1, "tickCount", -1);
         if (var2 < 0 || var2 % 20 != 0) {
            return;
         }

         bridgePlayer(var1);
      } catch (Throwable var3) {
         System.err.println("[REIVAX 18F] legacy bridge tick: " + var3);
      }
   }

   private static void bridge(Object var0) {
      try {
         Object var1 = Alpha18Probe.invokeNoArg(var0, "getEntity");
         bridgePlayer(var1);
      } catch (Throwable var2) {
         System.err.println("[REIVAX 18F] legacy bridge login: " + var2);
      }
   }

   private static void bridgePlayer(Object var0) throws Exception {
      if (var0 != null) {
         Object var1 = Alpha18CampaignProbe.serverFromPlayer(var0);
         if (var1 != null && !StoryModeGate18F.isManagedServer(var1)) {
            sync(var1, String.valueOf(Alpha18Probe.invokeNoArg(var0, "getUUID")));
         }
      }
   }

   static Alpha18Age1Bridge.SyncResult sync(Object var0, String var1) throws Exception {
      Object var2 = historicalCampaignData(var0);
      if (var2 == null) {
         return new Alpha18Age1Bridge.SyncResult(0, 0, "");
      } else {
         Age1BridgeStateData18 var3 = Age1BridgeStateData18.getForServer(var0);
         String var4 = String.valueOf(Alpha18Probe.invokeNoArg(var2, "stage"));
         int var5 = var3.snapshot().emitted();
         LinkedHashMap<String, Boolean> var6 = new LinkedHashMap<>();
         var6.put("AGE1_RESONANCE_MANIFESTED", bool(var2, "stelaPlaced"));
         var6.put("AGE1_STELA_DISCOVERED", bool(var2, "stelaDiscovered"));
         var6.put("AGE1_FRAGMENT_FOUND", bool(var2, "fragmentFound"));
         var6.put("AGE1_MATRIX_RECOGNIZED", bool(var2, "matrixDiscovered"));
         var6.put("AGE1_FIRST_PAGE_RECORDED", bool(var2, "matrixDiscovered"));
         var6.put("AGE1_FOYER_FOUNDED", bool(var2, "foundationPlaced"));
         var6.put("AGE1_DISTANT_RESPONSE", bool(var2, "aftermathPlayed"));

         for (Entry var8 : var6.entrySet()) {
            if ((Boolean)var8.getValue() && !var3.has((String)var8.getKey())) {
               StoryFact var9 = StoryFact.identified(
                  "LEGACY_AGE1:" + (String)var8.getKey(),
                  (String)var8.getKey(),
                  Age1LegacyFacts.topicFor((String)var8.getKey()),
                  var1 == null ? "WORLD" : var1,
                  Map.of("source", "LEGACY_CAMPAIGN_STATE", "legacyStage", var4)
               );
               CampaignRuntimeContext.withServer(var0, () -> StoryRuntime.publish(var9));
               var3.markEmitted((String)var8.getKey());
            }
         }

         int var10 = var3.snapshot().emitted();
         return new Alpha18Age1Bridge.SyncResult(var10 - var5, var10, var4);
      }
   }

   private static Object historicalCampaignData(Object var0) throws Exception {
      Class var1 = Class.forName("fr.reivaxmc.progress.progression.CampaignSavedData");

      for (Method var5 : var1.getMethods()) {
         if (var5.getName().equals("get") && var5.getParameterCount() == 1 && var5.getParameterTypes()[0].isInstance(var0)) {
            return var5.invoke(null, var0);
         }
      }

      return null;
   }

   private static boolean bool(Object var0, String var1) throws Exception {
      if (Alpha18Probe.invokeNoArg(var0, var1) instanceof Boolean var3 && var3) {
         return true;
      }

      return false;
   }

   private static boolean isServerPlayer(Object var0) {
      for (Class var1 = var0.getClass(); var1 != null; var1 = var1.getSuperclass()) {
         if (var1.getName().equals("net.minecraft.server.level.ServerPlayer")) {
            return true;
         }
      }

      return false;
   }

   private static int intField(Object var0, String var1, int var2) {
      try {
         Field var3 = var0.getClass().getField(var1);
         return var3.getInt(var0);
      } catch (Throwable var4) {
         return var2;
      }
   }

   static record SyncResult(int newlyEmitted, int totalEmitted, String legacyStage) {
   }
}
