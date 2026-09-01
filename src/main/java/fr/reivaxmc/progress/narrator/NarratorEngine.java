package fr.reivaxmc.progress.narrator;

import java.lang.reflect.Method;

public final class NarratorEngine {
   private NarratorEngine() {
   }

   /**
    * Narrator V1 bridge.
    *
    * The previous 18F gate deliberately returned false for every legacy call,
    * which left the pilot catalogue present but disconnected from Minecraft.
    * NarratorLegacy is currently the implementation of the pilot event brain,
    * so narrator calls must reach it while the data-driven engine is migrated.
    */
   private static void call(String method, Object... args) {
      try {
         Class<?> legacy = Class.forName("fr.reivaxmc.progress.narrator.NarratorLegacy");
         for (Method candidate : legacy.getMethods()) {
            if (candidate.getName().equals(method) && candidate.getParameterCount() == args.length) {
               candidate.invoke(null, args);
               return;
            }
         }
      } catch (Throwable error) {
         System.err.println("[REIVAX Origin] narrator " + method + " failed: " + error);
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
