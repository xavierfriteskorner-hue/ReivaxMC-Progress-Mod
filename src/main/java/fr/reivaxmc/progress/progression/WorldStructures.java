package fr.reivaxmc.progress.progression;

import fr.reivaxmc.progress.story.StoryModeGate18F;
import java.lang.reflect.Method;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;

public final class WorldStructures {
   private WorldStructures() {
   }

   static void placeSanctuary(MinecraftServer var0, CampaignSavedData var1) {
      if (!StoryModeGate18F.isManagedServer(var0)) {
         legacy("placeSanctuary", var0, var1);
      }
   }

   static void placeStela(MinecraftServer var0, CampaignSavedData var1) {
      if (!StoryModeGate18F.isManagedServer(var0)) {
         legacy("placeStela", var0, var1);
      }
   }

   public static void revealMatrix(ServerLevel var0, CampaignSavedData var1) {
      Object var2 = null;

      try {
         Method var3 = var0.getClass().getMethod("getServer");
         var2 = var3.invoke(var0);
      } catch (Throwable var4) {
      }

      if (var2 == null || !StoryModeGate18F.isManagedServer(var2)) {
         legacy("revealMatrix", var0, var1);
      }
   }

   private static void legacy(String var0, Object... var1) {
      try {
         Class var2 = Class.forName("fr.reivaxmc.progress.progression.WorldStructureZ");

         for (Method var6 : var2.getDeclaredMethods()) {
            if (var6.getName().equals(var0) && var6.getParameterCount() == var1.length) {
               var6.setAccessible(true);
               var6.invoke(null, var1);
               return;
            }
         }
      } catch (Throwable var7) {
         System.err.println("[REIVAX 18F] legacy structure " + var0 + " failed: " + var7);
      }
   }
}
