package fr.reivaxmc.progress.narrator;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public final class CompactHudHook {
   private CompactHudHook() {
   }

   public static void render(Object var0, Object var1) {
      F71NarrativeHud.render(var0, var1);

      try {
         Class var2 = Class.forName("net.minecraft.client.Minecraft");
         Object var3 = var2.getMethod("getInstance").invoke(null);
         Object var4 = field(var3, "player");
         if (var4 == null) {
            return;
         }

         Object var5 = field(var3, "screen");
         if (var5 != null) {
            return;
         }

         Class var6 = Class.forName("fr.reivaxmc.progress.network.ClientCampaignState");
         if (!boolField(var6, "introCompleted")) {
            return;
         }

         Object var7 = field(var3, "font");
         int var8 = ((Number)call(var0, "guiWidth")).intValue();
         int var9 = intField(var6, "progress");
         boolean var10 = intField(var6, "targetX") != 0 || intField(var6, "targetY") != 0 || intField(var6, "targetZ") != 0;
         short var11 = 162;
         int var12 = var10 ? 49 : 37;
         int var13 = var8 - var11 - 10;
         byte var14 = 10;
         call(var0, "fill", var13 + 2, var14 + 2, var13 + var11 + 2, var14 + var12 + 2, 1426063360);
         call(var0, "fill", var13, Integer.valueOf(var14), var13 + var11, var14 + var12, -921099741);
         call(var0, "fill", var13, Integer.valueOf(var14), var13 + 3, var14 + var12, -3761605);
         int var15 = Math.min(100, Math.max(0, var9 / 10));
         String var16 = "ÂGE I · ORIGINES  " + var15 + "%";
         call(var0, "drawString", var7, var16, var13 + 8, var14 + 7, -792376, false);
         int var17 = var13 + 8;
         int var18 = var14 + 20;
         int var19 = var11 - 16;
         call(var0, "fill", var17, var18, var17 + var19, var18 + 4, -15193304);
         int var20 = Math.max(0, Math.min(var19, (int)((double)(var19 * Math.min(1000, Math.max(0, var9))) / 1000.0)));
         if (var20 > 0) {
            call(var0, "fill", var17, var18, var17 + var20, var18 + 4, -11620729);
         }

         if (var10) {
            String var21 = invokeStaticString("fr.reivaxmc.progress.client.ReivaxMCProgressClient", "resonanceStrength");
            String var22 = invokeStaticString("fr.reivaxmc.progress.client.ReivaxMCProgressClient", "resonanceArrow");
            String var23 = (var21 == null ? "" : var21) + (var22 != null && !var22.isBlank() ? "  " + var22 : "");
            call(var0, "drawString", var7, var23, var13 + 8, var14 + 31, -8795740, false);
         } else {
            call(var0, "drawString", var7, "NUM 3", var13 + var11 - 39, var14 + 27, -3693747, false);
         }

         NarratorClientHook.render(var0, var3);
      } catch (Throwable var24) {
         System.err.println("[ReivaxMC A17.3 HUD] " + var24.getClass().getSimpleName() + ": " + var24.getMessage());
      }
   }

   private static String invokeStaticString(String var0, String var1) {
      try {
         return String.valueOf(Class.forName(var0).getMethod(var1).invoke(null));
      } catch (Throwable var3) {
         return "";
      }
   }

   private static boolean boolField(Class<?> var0, String var1) throws Exception {
      return (Boolean)field(var0, var1);
   }

   private static int intField(Class<?> var0, String var1) throws Exception {
      return ((Number)field(var0, var1)).intValue();
   }

   private static Object field(Object var0, String var1) throws Exception {
      Class var2 = var0 instanceof Class var3 ? var3 : var0.getClass();
      Field var7 = null;

      for (Class var4 = var2; var4 != null; var4 = var4.getSuperclass()) {
         try {
            var7 = var4.getDeclaredField(var1);
            break;
         } catch (Exception var6) {
         }
      }

      if (var7 == null) {
         throw new NoSuchFieldException(var1);
      } else {
         var7.setAccessible(true);
         return var7.get(var0 instanceof Class ? null : var0);
      }
   }

   private static Object call(Object var0, String var1, Object... var2) throws Exception {
      for (Method var6 : var0.getClass().getMethods()) {
         if (var6.getName().equals(var1) && var6.getParameterCount() == var2.length) {
            try {
               return var6.invoke(var0, var2);
            } catch (IllegalArgumentException var9) {
            }
         }
      }

      for (Method var13 : var0.getClass().getDeclaredMethods()) {
         if (var13.getName().equals(var1) && var13.getParameterCount() == var2.length) {
            try {
               var13.setAccessible(true);
               return var13.invoke(var0, var2);
            } catch (IllegalArgumentException var8) {
            }
         }
      }

      throw new NoSuchMethodException(var1);
   }
}
