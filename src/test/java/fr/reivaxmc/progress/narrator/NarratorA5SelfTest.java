package fr.reivaxmc.progress.narrator;

import java.util.HashSet;

/** Auto-test autonome du lot A5, sans lancer Minecraft. */
public final class NarratorA5SelfTest {
   private NarratorA5SelfTest() {
   }

   public static void main(String[] args) {
      idsAreStableAndUnique();
      foundationsAreExact();
      animalSignalsRequireConfirmation();
      mobilityVillageAndOrientationAreExact();
      catalogIsReadyForSoloDuoAndBatches();
      System.out.println("REIVAX A5 self-test: OK (16 événements, 50/52 robustes actifs, SOLO + DUO)");
   }

   private static void idsAreStableAndUnique() {
      require(NarratorA5SignalDetector.ALL_IDS.size() == 16, "A5 doit contenir exactement 16 événements jouables.");
      require(new HashSet<>(NarratorA5SignalDetector.ALL_IDS).size() == 16, "Les IDs A5 doivent être uniques.");
      require(
         "A1-001".equals(NarratorA5SignalDetector.FIRST_WOOD)
            && "A1-073".equals(NarratorA5SignalDetector.FIRST_TAME),
         "Les huit événements historiques doivent conserver leur mémoire existante."
      );
      require(
         "A1-127".equals(NarratorA5SignalDetector.FIRST_ANIMAL_FED)
            && "A1-134".equals(NarratorA5SignalDetector.FIRST_COMPASS),
         "La nouvelle plage A5 doit rester stable."
      );
   }

   private static void foundationsAreExact() {
      expectInventory("minecraft:oak_log", NarratorA5SignalDetector.FIRST_WOOD);
      expectInventory("minecraft:bamboo_block", NarratorA5SignalDetector.FIRST_WOOD);
      expectInventory("minecraft:cobblestone", NarratorA5SignalDetector.FIRST_STONE);
      expectInventory("minecraft:charcoal", NarratorA5SignalDetector.FIRST_COAL);
      expectInventory("minecraft:raw_iron", NarratorA5SignalDetector.FIRST_RAW_IRON);
      expectInventory("minecraft:diamond", NarratorA5SignalDetector.FIRST_DIAMOND);
      require(NarratorA5SignalDetector.inventoryIncrease("modded:oak_log") == null, "Un faux bois moddé ne doit pas compter comme bois vanilla.");
      require(NarratorA5SignalDetector.inventoryIncrease("minecraft:iron_ingot") == null, "Le fer brut ne doit pas accepter un lingot.");

      require(
         NarratorA5SignalDetector.FIRST_CRAFTING_TABLE.equals(
            NarratorA5SignalDetector.itemCrafted("minecraft:crafting_table").eventId()
         ),
         "L'établi crafté doit être détecté."
      );
      require(NarratorA5SignalDetector.itemCrafted("minecraft:furnace") == null, "Un four ne doit pas être confondu avec un établi.");
      require(
         NarratorA5SignalDetector.FIRST_SMELTING.equals(NarratorA5SignalDetector.itemSmelted("minecraft:baked_potato").eventId()),
         "La première cuisson doit accepter autre chose qu'un lingot."
      );
      require(NarratorA5SignalDetector.itemSmelted("minecraft:air") == null, "Une sortie vide ne doit rien déclencher.");
   }

   private static void animalSignalsRequireConfirmation() {
      require(NarratorA5SignalDetector.animalTamed(true) != null, "Un apprivoisement serveur doit être détecté.");
      require(NarratorA5SignalDetector.animalTamed(false) == null, "Un faux signal client ne doit pas compter.");
      require(NarratorA5SignalDetector.animalFed(true, true) != null, "La nourriture acceptée et créditée doit compter.");
      require(NarratorA5SignalDetector.animalFed(true, false) == null, "Une nourriture non créditée au joueur ne doit pas compter.");
      require(NarratorA5SignalDetector.animalFed(false, true) == null, "Une interaction refusée ne doit pas compter.");
      require(NarratorA5SignalDetector.animalBred(true, true) != null, "Une naissance provoquée par le joueur doit compter.");
      require(NarratorA5SignalDetector.animalBred(true, false) == null, "Une naissance sans enfant ne doit pas compter.");
   }

   private static void mobilityVillageAndOrientationAreExact() {
      require(NarratorA5SignalDetector.entityMounted("minecraft:horse", true).eventId().equals(NarratorA5SignalDetector.FIRST_HORSE_MOUNTED), "Le cheval doit compter.");
      require(NarratorA5SignalDetector.entityMounted("minecraft:donkey", true) == null, "Un âne ne doit pas être confondu avec un cheval.");
      require(NarratorA5SignalDetector.entityMounted("minecraft:boat", true).eventId().equals(NarratorA5SignalDetector.FIRST_BOAT_MOUNTED), "Le bateau doit compter.");
      require(NarratorA5SignalDetector.entityMounted("minecraft:bamboo_raft", true).eventId().equals(NarratorA5SignalDetector.FIRST_BOAT_MOUNTED), "Le radeau doit compter.");
      require(NarratorA5SignalDetector.entityMounted("minecraft:boat", false) == null, "Descendre d'un bateau ne doit rien déclencher.");
      require(NarratorA5SignalDetector.villagerTrade(true) != null, "Un échange confirmé serveur doit compter.");
      require(NarratorA5SignalDetector.villagerTrade(false) == null, "Un échange non confirmé ne doit pas compter.");
      require(NarratorA5SignalDetector.villageBell("minecraft:bell", true) != null, "Une cloche réellement agitée doit compter.");
      require(NarratorA5SignalDetector.villageBell("minecraft:bell", false) == null, "Un clic refusé sur une cloche ne doit pas compter.");
      expectInventory("minecraft:filled_map", NarratorA5SignalDetector.FIRST_MAP);
      expectInventory("minecraft:compass", NarratorA5SignalDetector.FIRST_COMPASS);
   }

   private static void catalogIsReadyForSoloDuoAndBatches() {
      for (String id : NarratorA5SignalDetector.ALL_IDS) {
         require(NarratorLegacy.catalogHasEventForTest(id), "Événement absent du catalogue JSON : " + id);
         require(NarratorLegacy.catalogHasDuoTextForTest(id), "Texte partenaire absent : " + id);
      }
      require(
         "animal_progression".equals(NarratorLegacy.catalogCondenseGroupForTest(NarratorA5SignalDetector.FIRST_ANIMAL_FED))
            && "animal_progression".equals(NarratorLegacy.catalogCondenseGroupForTest(NarratorA5SignalDetector.FIRST_ANIMAL_BRED)),
         "Nourrissage et reproduction rapprochés doivent pouvoir partager un encadré."
      );
      require(
         "travel_progression".equals(NarratorLegacy.catalogCondenseGroupForTest(NarratorA5SignalDetector.FIRST_MAP))
            && "travel_progression".equals(NarratorLegacy.catalogCondenseGroupForTest(NarratorA5SignalDetector.FIRST_COMPASS)),
         "Carte et boussole rapprochées doivent pouvoir partager un encadré."
      );
   }

   private static void expectInventory(String itemId, String eventId) {
      NarratorA5SignalDetector.Signal signal = NarratorA5SignalDetector.inventoryIncrease(itemId);
      require(signal != null && eventId.equals(signal.eventId()), "Mauvais mapping A5 pour " + itemId);
   }

   private static void require(boolean condition, String message) {
      if (!condition) {
         throw new AssertionError(message);
      }
   }
}
