package fr.reivaxmc.progress.story;

import java.lang.reflect.Method;
import java.util.Map;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.ItemCraftedEvent;
import net.neoforged.neoforge.event.level.BlockEvent.BreakEvent;
import net.neoforged.neoforge.event.level.BlockEvent.EntityPlaceEvent;

@EventBusSubscriber(
   modid = "reivaxmc_progress"
)
public final class Alpha18RealEventProbe {
   private Alpha18RealEventProbe() {
   }

   @SubscribeEvent
   public static void onCraft(ItemCraftedEvent var0) {
      try {
         Object var1 = invokeNoArg(var0, "getEntity");
         if (!isServerPlayer(var1)) {
            return;
         }

         Object var2 = invokeNoArg(var0, "getCrafting");
         publish(var0, var1, "ITEM_CRAFTED", "PILOT_REAL_CRAFT", itemStackKey(var2), "");
      } catch (Throwable var3) {
         debugError("craft", var3);
      }
   }

   @SubscribeEvent
   public static void onBreak(BreakEvent var0) {
      try {
         Object var1 = invokeNoArg(var0, "getPlayer");
         if (!isServerPlayer(var1)) {
            return;
         }

         Object var2 = invokeNoArg(var0, "getState");
         publish(var0, var1, "BLOCK_BROKEN", "PILOT_REAL_BREAK", blockStateKey(var2), stringify(optionalNoArg(var0, "getPos")));
      } catch (Throwable var3) {
         debugError("break", var3);
      }
   }

   @SubscribeEvent
   public static void onPlace(EntityPlaceEvent var0) {
      try {
         Object var1 = invokeNoArg(var0, "getEntity");
         if (!isServerPlayer(var1)) {
            return;
         }

         Object var2 = invokeNoArg(var0, "getPlacedBlock");
         publish(var0, var1, "BLOCK_PLACED", "PILOT_REAL_PLACE", blockStateKey(var2), stringify(optionalNoArg(var0, "getPos")));
      } catch (Throwable var3) {
         debugError("place", var3);
      }
   }

   private static void publish(Object var0, Object var1, String var2, String var3, String var4, String var5) throws Exception {
      String var6 = stringify(invokeNoArg(var1, "getUUID"));
      String var7 = var2 + ":" + var6 + ":" + Integer.toUnsignedString(System.identityHashCode(var0));
      Map var8 = var5 != null && !var5.isBlank() && !"null".equals(var5)
         ? Map.of("subject", safe(var4), "position", var5, "source", "NEOFORGE_EVENT")
         : Map.of("subject", safe(var4), "source", "NEOFORGE_EVENT");
      StoryFact var9 = StoryFact.identified(var7, var2, var3, var6, var8);
      Object var10 = Alpha18CampaignProbe.serverFromPlayer(var1);
      CampaignRuntimeContext.withServer(var10, () -> StoryRuntime.publish(var9));
   }

   private static boolean isServerPlayer(Object var0) {
      if (var0 == null) {
         return false;
      } else {
         for (Class var1 = var0.getClass(); var1 != null; var1 = var1.getSuperclass()) {
            if ("net.minecraft.server.level.ServerPlayer".equals(var1.getName())) {
               return true;
            }
         }

         return false;
      }
   }

   private static Object invokeNoArg(Object var0, String var1) throws Exception {
      Method var2 = var0.getClass().getMethod(var1);
      return var2.invoke(var0);
   }

   private static Object optionalNoArg(Object var0, String var1) {
      try {
         return invokeNoArg(var0, var1);
      } catch (Throwable var3) {
         return null;
      }
   }

   private static String itemStackKey(Object var0) {
      try {
         Object var1 = invokeNoArg(var0, "getItem");
         return registryKey("net.minecraft.core.registries.BuiltInRegistries", "ITEM", var1);
      } catch (Throwable var2) {
         return "unknown";
      }
   }

   private static String blockStateKey(Object var0) {
      try {
         Object var1 = invokeNoArg(var0, "getBlock");
         return registryKey("net.minecraft.core.registries.BuiltInRegistries", "BLOCK", var1);
      } catch (Throwable var2) {
         return "unknown";
      }
   }

   private static String registryKey(String var0, String var1, Object var2) throws Exception {
      Class var3 = Class.forName(var0);
      Object var4 = var3.getField(var1).get(null);

      for (Method var8 : var4.getClass().getMethods()) {
         if (var8.getName().equals("getKey") && var8.getParameterCount() == 1) {
            Object var9 = var8.invoke(var4, var2);
            if (var9 != null) {
               return var9.toString();
            }
         }
      }

      return "unknown";
   }

   private static String stringify(Object var0) {
      return var0 == null ? "" : String.valueOf(var0);
   }

   private static String safe(String var0) {
      return var0 == null ? "" : var0;
   }

   private static void debugError(String var0, Throwable var1) {
      System.err.println("[REIVAX Alpha18E] real event " + var0 + " failed: " + var1);
   }
}
