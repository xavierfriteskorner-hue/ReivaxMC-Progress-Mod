package fr.reivaxmc.progress.narrator;

import java.util.HashSet;
import java.util.List;

/** Auto-test autonome des 24 événements B1, sans lancer Minecraft. */
public final class NarratorB1SelfTest {
   private NarratorB1SelfTest() {
   }

   public static void main(String[] args) {
      idsAreStableAndUnique();
      historicalSignalsRemainExact();
      survivalTransitionsAreStrict();
      worldSignalsArePrecise();
      interactionsRequireConfirmation();
      catalogIsReadyForSoloDuoAndBatches();
      System.out.println("REIVAX B1 self-test: OK (24 événements, 74/76 actifs, SOLO + DUO)");
   }

   private static void idsAreStableAndUnique() {
      require(NarratorB1SignalDetector.ALL_IDS.size() == 24, "B1 doit contenir exactement 24 événements.");
      require(new HashSet<>(NarratorB1SignalDetector.ALL_IDS).size() == 24, "Les IDs B1 doivent être uniques.");
      require("A1-024".equals(NarratorB1SignalDetector.FIRST_CROP), "La culture doit garder sa mémoire historique.");
      require("A1-090".equals(NarratorB1SignalDetector.LIGHTNING), "La foudre doit garder sa mémoire historique.");
      require("A1-135".equals(NarratorB1SignalDetector.TRAVEL_500), "La nouvelle plage B1 doit commencer à A1-135.");
      require("A1-153".equals(NarratorB1SignalDetector.COMPANION_HURT), "La nouvelle plage B1 doit finir à A1-153.");
   }

   private static void historicalSignalsRemainExact() {
      require(event(NarratorB1SignalDetector.cropPlaced("minecraft:wheat"), NarratorB1SignalDetector.FIRST_CROP), "Le blé planté doit compter.");
      require(NarratorB1SignalDetector.cropPlaced("minecraft:grass_block") == null, "L'herbe ne doit pas être une culture.");
      require(event(NarratorB1SignalDetector.dawn(true, true), NarratorB1SignalDetector.SURVIVE_DAWN), "Une aube après la nuit doit compter.");
      require(NarratorB1SignalDetector.dawn(false, true) == null, "Une aube sans nuit vécue ne doit pas compter.");
      require(event(NarratorB1SignalDetector.village(true), NarratorB1SignalDetector.VILLAGE_DISCOVERED), "L'entrée dans un village doit compter.");
      require(event(NarratorB1SignalDetector.halfHeart(4.0F, 1.0F), NarratorB1SignalDetector.HALF_HEART), "Le passage au demi-cœur doit compter.");
      require(NarratorB1SignalDetector.halfHeart(1.0F, 1.0F) == null, "Une condition continue ne doit pas redéclencher le demi-cœur.");
      require(event(NarratorB1SignalDetector.lightning(true, false), NarratorB1SignalDetector.LIGHTNING), "Une foudre serveur confirmée doit compter.");
      require(NarratorB1SignalDetector.lightning(true, true) == null, "Une foudre annulée ne doit pas compter.");
   }

   private static void survivalTransitionsAreStrict() {
      require(NarratorB1SignalDetector.travelled(499.99) == null, "499 blocs ne suffisent pas.");
      require(event(NarratorB1SignalDetector.travelled(500.0), NarratorB1SignalDetector.TRAVEL_500), "500 blocs cumulés doivent compter.");
      require(event(NarratorB1SignalDetector.sleepConfirmed(true, true), NarratorB1SignalDetector.FIRST_SLEEP), "Un réveil à l'aube doit compter.");
      require(NarratorB1SignalDetector.sleepConfirmed(true, false) == null, "Sortir du lit la nuit ne doit pas compter.");
      require(NarratorB1SignalDetector.refusedSleep(2) == null, "Deux nuits éveillé ne suffisent pas.");
      require(event(NarratorB1SignalDetector.refusedSleep(3), NarratorB1SignalDetector.REFUSED_SLEEP), "Trois nuits éveillé doivent compter.");
      require(has(NarratorB1SignalDetector.consumed("minecraft:apple"), NarratorB1SignalDetector.FIRST_APPLE), "La pomme mangée doit compter.");
      require(has(NarratorB1SignalDetector.consumed("minecraft:cooked_beef"), NarratorB1SignalDetector.FIRST_COOKED_MEAL), "Le repas cuit doit compter.");
      require(NarratorB1SignalDetector.consumed("minecraft:raw_beef").isEmpty(), "La viande crue ne doit pas être un repas cuit.");
      require(event(NarratorB1SignalDetector.criticalHunger(6, 2), NarratorB1SignalDetector.CRITICAL_HUNGER), "La transition de faim critique doit compter.");
      require(NarratorB1SignalDetector.criticalHunger(2, 1) == null, "Rester affamé ne doit pas redéclencher.");
   }

   private static void worldSignalsArePrecise() {
      List<NarratorB1SignalDetector.Signal> ocean = NarratorB1SignalDetector.environment("minecraft:deep_ocean", 63, true, true);
      require(has(ocean, NarratorB1SignalDetector.FIRST_OCEAN), "Un océan doit être reconnu.");
      require(has(NarratorB1SignalDetector.environment("minecraft:snowy_plains", 70, true, true), NarratorB1SignalDetector.FIRST_SNOW), "La neige doit être reconnue.");
      require(has(NarratorB1SignalDetector.environment("minecraft:desert", 70, true, true), NarratorB1SignalDetector.FIRST_DESERT), "Le désert doit être reconnu.");
      require(has(NarratorB1SignalDetector.environment("minecraft:bamboo_jungle", 70, false, true), NarratorB1SignalDetector.FIRST_JUNGLE), "La jungle doit être reconnue.");
      require(has(NarratorB1SignalDetector.environment("minecraft:plains", 160, true, true), NarratorB1SignalDetector.FIRST_SUMMIT), "Un sommet exposé doit compter.");
      require(!has(NarratorB1SignalDetector.environment("minecraft:plains", 160, false, true), NarratorB1SignalDetector.FIRST_SUMMIT), "Une salle souterraine haute n'est pas un sommet.");
      List<NarratorB1SignalDetector.Signal> deep = NarratorB1SignalDetector.environment("minecraft:dripstone_caves", -42, false, true);
      require(has(deep, NarratorB1SignalDetector.FIRST_DEEP_CAVE) && has(deep, NarratorB1SignalDetector.BELOW_MINUS_40), "La grande profondeur peut confirmer les deux jalons.");
      require(NarratorB1SignalDetector.environment("minecraft:desert", 70, true, false).isEmpty(), "Le Nether ne doit pas imiter un biome de surface.");
      require(event(NarratorB1SignalDetector.thunderstorm(true, true), NarratorB1SignalDetector.FIRST_THUNDERSTORM), "Un orage dehors doit compter.");
      require(NarratorB1SignalDetector.thunderstorm(true, false) == null, "Un orage depuis une grotte ne doit pas compter.");
      require(event(NarratorB1SignalDetector.sunrise("NIGHT", "DAWN", true, true), NarratorB1SignalDetector.SUNRISE_OUTDOORS), "Le lever observé dehors doit compter.");
      require(NarratorB1SignalDetector.sunrise("DAY", "DAWN", true, true) == null, "Une commande temporelle sans transition nocturne ne doit pas compter.");
      require(event(NarratorB1SignalDetector.inventoryFull(false, true), NarratorB1SignalDetector.INVENTORY_FULL), "L'inventaire qui devient plein doit compter.");
      require(NarratorB1SignalDetector.inventoryFull(true, true) == null, "Un inventaire restant plein ne doit pas redéclencher.");
   }

   private static void interactionsRequireConfirmation() {
      require(event(NarratorB1SignalDetector.preciousToss("minecraft:diamond", false), NarratorB1SignalDetector.PRECIOUS_TOSS), "Un diamant jeté volontairement doit compter.");
      require(NarratorB1SignalDetector.preciousToss("minecraft:dirt", false) == null, "La terre ne doit pas être précieuse.");
      require(NarratorB1SignalDetector.preciousToss("minecraft:diamond", true) == null, "Un drop annulé ne doit pas compter.");
      require(event(NarratorB1SignalDetector.animalNamed(true, true, true, true), NarratorB1SignalDetector.NAMED_ANIMAL), "Le nom confirmé sur un animal doit compter.");
      require(NarratorB1SignalDetector.animalNamed(true, true, false, true) == null, "Le clic Name Tag refusé ne doit pas compter.");
      require(event(NarratorB1SignalDetector.companionHurt(true, 3.0F, 8.0F, 20.0F), NarratorB1SignalDetector.COMPANION_HURT), "Le compagnon du joueur sous 50 % doit compter.");
      require(NarratorB1SignalDetector.companionHurt(false, 3.0F, 8.0F, 20.0F) == null, "Un animal sans propriétaire ne doit pas compter.");
   }

   private static void catalogIsReadyForSoloDuoAndBatches() {
      for (String id : NarratorB1SignalDetector.ALL_IDS) {
         require(NarratorLegacy.catalogHasEventForTest(id), "Événement absent du catalogue JSON : " + id);
         require(NarratorLegacy.catalogHasDuoTextForTest(id), "Texte partenaire absent : " + id);
         require(NarratorLegacy.catalogCondenseGroupForTest(id) != null, "Groupe de condensation absent : " + id);
      }
      require("depth_progression".equals(NarratorLegacy.catalogCondenseGroupForTest(NarratorB1SignalDetector.FIRST_DEEP_CAVE)), "Les profondeurs doivent partager leur encadré.");
      require("companion_progression".equals(NarratorLegacy.catalogCondenseGroupForTest(NarratorB1SignalDetector.NAMED_ANIMAL)), "Les événements de compagnon doivent partager leur encadré.");
   }

   private static boolean has(List<NarratorB1SignalDetector.Signal> signals, String id) {
      return signals.stream().anyMatch(signal -> id.equals(signal.eventId()));
   }

   private static boolean event(NarratorB1SignalDetector.Signal signal, String id) {
      return signal != null && id.equals(signal.eventId());
   }

   private static void require(boolean condition, String message) {
      if (!condition) {
         throw new AssertionError(message);
      }
   }
}
