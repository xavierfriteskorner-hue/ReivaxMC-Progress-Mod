package fr.reivaxmc.progress.story;

public final class Alpha18FClientState {
   public static volatile boolean available = true;
   public static volatile boolean started = false;
   public static volatile boolean managed = true;

   private Alpha18FClientState() {
   }

   public static void update(boolean var0, boolean var1, boolean var2) {
      boolean var3 = started;
      managed = true;
      available = !var2 && var1;
      started = var2;
      if (var2 && !var3) {
         try {
            Class var4 = Class.forName("net.minecraft.client.Minecraft");
            Object var5 = var4.getMethod("getInstance").invoke(null);
            Object var6 = var4.getField("screen").get(var5);
            if (var6 != null && "fr.reivaxmc.progress.client.StoryLauncherScreen".equals(var6.getClass().getName())) {
               Class var7 = Class.forName("fr.reivaxmc.progress.client.IntroCinematicScreen");
               Object var8 = var7.getConstructor().newInstance();
               Class var9 = Class.forName("net.minecraft.client.gui.screens.Screen");
               var4.getMethod("setScreen", var9).invoke(var5, var8);
            }
         } catch (Throwable var10) {
         }
      }
   }

   public static void resetForWorld() {
      managed = true;
      available = true;
      started = false;
   }
}
