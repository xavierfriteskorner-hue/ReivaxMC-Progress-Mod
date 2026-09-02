package fr.reivaxmc.progress.narrator;

/**
 * Point d'entrée unique du Narrateur.
 *
 * Depuis 0.7.11 le pont ne passe plus par la réflexion : les événements NeoForge
 * appellent directement le moteur pilote. C'est plus simple, plus rapide et les
 * erreurs de compilation deviennent visibles au build au lieu d'être silencieuses
 * en jeu.
 */
public final class NarratorEngine {
   private NarratorEngine() {
   }

   public static void onLogin(Object event) {
      NarratorLegacy.onLogin(event);
   }

   public static void onServerTick(Object event) {
      NarratorLegacy.onServerTick(event);
   }

   public static void onPlayerTick(Object event) {
      NarratorLegacy.onPlayerTick(event);
   }

   public static void inventoryScan(Object player, Object campaign) {
      NarratorLegacy.inventoryScan(player, campaign);
   }

   public static void onBlockPlaced(Object event) {
      NarratorLegacy.onBlockPlaced(event);
   }

   public static void onBlockBroken(Object event) {
      NarratorLegacy.onBlockBroken(event);
   }

   public static void onRightClickBlock(Object event) {
      NarratorLegacy.onRightClickBlock(event);
   }

   public static void onRightClickItem(Object event) {
      NarratorLegacy.onRightClickItem(event);
   }

   public static void onCommands(Object event) {
      NarratorLegacy.onCommands(event);
   }

   public static void onItemCrafted(Object event) {
      NarratorLegacy.onItemCrafted(event);
   }

   public static void onItemSmelted(Object event) {
      NarratorLegacy.onItemSmelted(event);
   }

   public static void onLivingDeath(Object event) {
      NarratorLegacy.onLivingDeath(event);
   }

   public static void onAnimalTame(Object event) {
      NarratorLegacy.onAnimalTame(event);
   }

   public static void onItemToss(Object event) {
      NarratorLegacy.onItemToss(event);
   }

   public static void onItemPickup(Object event) {
      NarratorLegacy.onItemPickup(event);
   }
}
