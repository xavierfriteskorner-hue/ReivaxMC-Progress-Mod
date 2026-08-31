package fr.reivaxmc.progress.narrator;

import fr.reivaxmc.progress.story.StoryModeGate18F;
import java.lang.reflect.Method;

public final class NarratorEngine {
   private NarratorEngine() {
   }

   private static void call(String var0, Object... var1) {
      Object var2 = var1.length == 0 ? null : var1[0];
      if (StoryModeGate18F.allowLegacy(var2)) {
         try {
            Class var3 = Class.forName("fr.reivaxmc.progress.narrator.NarratorLegacy");

            for (Method var7 : var3.getMethods()) {
               if (var7.getName().equals(var0) && var7.getParameterCount() == var1.length) {
                  var7.invoke(null, var1);
                  return;
               }
            }
         } catch (Throwable var8) {
            System.err.println("[REIVAX 18F] legacy narrator " + var0 + " failed: " + var8);
         }
      }
   }

   public static void onLogin(Object var0) {
      call("onLogin", var0);
   }

   public static void onServerTick(Object var0) {
      call("onServerTick", var0);
   }

   public static void onPlayerTick(Object var0) {
      call("onPlayerTick", var0);
   }

   public static void inventoryScan(Object var0, Object var1) {
      call("inventoryScan", var0, var1);
   }

   public static void onBlockPlaced(Object var0) {
      call("onBlockPlaced", var0);
   }

   public static void onBlockBroken(Object var0) {
      call("onBlockBroken", var0);
   }

   public static void onRightClickBlock(Object var0) {
      call("onRightClickBlock", var0);
   }

   public static void onRightClickItem(Object var0) {
      call("onRightClickItem", var0);
   }

   public static void onCommands(Object var0) {
      call("onCommands", var0);
   }

   public static void onItemCrafted(Object var0) {
      call("onItemCrafted", var0);
   }

   public static void onItemSmelted(Object var0) {
      call("onItemSmelted", var0);
   }

   public static void onLivingDeath(Object var0) {
      call("onLivingDeath", var0);
   }

   public static void onAnimalTame(Object var0) {
      call("onAnimalTame", var0);
   }

   public static void onItemToss(Object var0) {
      call("onItemToss", var0);
   }

   public static void onItemPickup(Object var0) {
      call("onItemPickup", var0);
   }
}
