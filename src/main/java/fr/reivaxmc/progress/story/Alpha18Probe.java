package fr.reivaxmc.progress.story;

import java.lang.reflect.Method;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;

@EventBusSubscriber(
   modid = "reivaxmc_progress"
)
public final class Alpha18Probe {
   private Alpha18Probe() {
   }

   @SubscribeEvent
   public static void onLogin(PlayerLoggedInEvent var0) {
      try {
         Object var1 = invokeNoArg(var0, "getEntity");
         if (var1 == null) {
            return;
         }

         String var2 = String.valueOf(invokeNoArg(var1, "getUUID"));
         StoryRuntime.SelfTestResult var3 = StoryRuntime.selfTest(var2);
         System.out.println("[REIVAX Alpha18F] core self-test=" + var3);
      } catch (Throwable var4) {
         System.err.println("[REIVAX Alpha18F] core self-test failed: " + var4);
      }
   }

   static Object invokeNoArg(Object var0, String var1) throws Exception {
      Method var2 = var0.getClass().getMethod(var1);
      return var2.invoke(var0);
   }

   static void sendSystemMessage(Object var0, String var1) throws Exception {
      Class var2 = Class.forName("net.minecraft.network.chat.Component");
      Object var3 = var2.getMethod("literal", String.class).invoke(null, var1);

      for (Method var7 : var0.getClass().getMethods()) {
         if (var7.getName().equals("sendSystemMessage") && var7.getParameterCount() == 1) {
            var7.invoke(var0, var3);
            return;
         }
      }
   }
}
