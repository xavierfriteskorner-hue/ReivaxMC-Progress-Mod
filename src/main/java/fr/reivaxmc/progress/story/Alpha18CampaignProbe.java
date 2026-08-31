package fr.reivaxmc.progress.story;

public final class Alpha18CampaignProbe {
   private Alpha18CampaignProbe() {
   }

   static Object serverFromPlayer(Object var0) {
      Object var1 = optionalNoArg(var0, "getServer");
      if (var1 != null) {
         return var1;
      } else {
         Object var2 = optionalNoArg(var0, "serverLevel");
         if (var2 == null) {
            var2 = optionalNoArg(var0, "level");
         }

         return var2 == null ? null : optionalNoArg(var2, "getServer");
      }
   }

   private static Object optionalNoArg(Object var0, String var1) {
      if (var0 == null) {
         return null;
      } else {
         try {
            return Alpha18Probe.invokeNoArg(var0, var1);
         } catch (Throwable var3) {
            return null;
         }
      }
   }
}
