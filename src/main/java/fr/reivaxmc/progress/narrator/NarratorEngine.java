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

   public static void onLivingDamage(Object event) {
      NarratorLegacy.onLivingDamage(event);
   }

   public static void onAnimalTame(Object event) {
      NarratorLegacy.onAnimalTame(event);
   }

   public static void onEntityInteract(Object event) {
      NarratorLegacy.onEntityInteract(event);
   }

   public static void onAnimalBred(Object event) {
      NarratorLegacy.onAnimalBred(event);
   }

   public static void onEntityMounted(Object event) {
      NarratorLegacy.onEntityMounted(event);
   }

   public static void onVillagerTrade(Object event) {
      NarratorLegacy.onVillagerTrade(event);
   }

   public static void onItemToss(Object event) {
      NarratorLegacy.onItemToss(event);
   }

   public static void onItemPickup(Object event) {
      NarratorLegacy.onItemPickup(event);
   }

   public static void onItemConsumed(Object event) {
      NarratorLegacy.onItemConsumed(event);
   }

   public static void onPlayerWakeUp(Object event) {
      NarratorLegacy.onPlayerWakeUp(event);
   }

   public static void onEntityStruckByLightning(Object event) {
      NarratorLegacy.onEntityStruckByLightning(event);
   }

   /**
    * Entrée stable pour les futurs choix scénaristiques. Le scénario peut faire
    * évoluer la confiance/défiance de La Voix sans dépendre du stockage interne.
    */
   public static void rememberDecision(Object player, String decisionTag, int trustDelta, int defianceDelta) {
      NarratorLegacy.rememberDecision(player, decisionTag, trustDelta, defianceDelta);
   }

   /** Ajoute un fait mémorisé exploitable par les futures variantes contextuelles. */
   public static void rememberFact(Object player, String tag, int amount) {
      NarratorLegacy.rememberFact(player, tag, amount);
   }
}
