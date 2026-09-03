package fr.reivaxmc.progress.narrator;

import java.util.List;

/** Test autonome du petit sous-lot A2, sans lancer Minecraft. */
public final class NarratorA2SelfTest {
   private NarratorA2SelfTest() {
   }

   public static void main(String[] args) {
      inventoryDetectorIsExact();
      placementDetectorIsExact();
      catalogContainsSoloAndDuoTexts();
      System.out.println("REIVAX A2 self-test: OK");
   }

   private static void inventoryDetectorIsExact() {
      NarratorA2SignalDetector.Signal stick = NarratorA2SignalDetector.inventoryIncrease("minecraft:stick");
      require(stick != null && NarratorA2SignalDetector.FIRST_STICK.equals(stick.eventId()), "Le premier bâton doit être détecté.");
      require(NarratorA2SignalDetector.inventoryIncrease("minecraft:bamboo") == null, "Le bambou ne doit pas être confondu avec un bâton.");
      require(NarratorA2SignalDetector.inventoryIncrease(null) == null, "Un item absent ne doit produire aucun signal.");
   }

   private static void placementDetectorIsExact() {
      NarratorA2SignalDetector.Signal chest = NarratorA2SignalDetector.blockPlaced("minecraft:chest");
      NarratorA2SignalDetector.Signal trappedChest = NarratorA2SignalDetector.blockPlaced("minecraft:trapped_chest");
      NarratorA2SignalDetector.Signal bed = NarratorA2SignalDetector.blockPlaced("minecraft:red_bed");
      require(chest != null && NarratorA2SignalDetector.FIRST_CHEST.equals(chest.eventId()), "Le coffre posé doit être détecté.");
      require(trappedChest != null && NarratorA2SignalDetector.FIRST_CHEST.equals(trappedChest.eventId()), "Le coffre piégé doit compter comme coffre.");
      require(bed != null && NarratorA2SignalDetector.FIRST_BED.equals(bed.eventId()), "Chaque couleur de lit doit être détectée.");
      require(NarratorA2SignalDetector.blockPlaced("minecraft:barrel") == null, "Un tonneau ne doit pas être confondu avec un coffre.");
      require(NarratorA2SignalDetector.blockPlaced("modded:blue_bedside_table") == null, "Un bloc moddé ressemblant à un lit ne doit pas créer de faux positif.");
   }

   private static void catalogContainsSoloAndDuoTexts() {
      for (String id : List.of(
         NarratorA2SignalDetector.FIRST_STICK,
         NarratorA2SignalDetector.FIRST_CHEST,
         NarratorA2SignalDetector.FIRST_BED
      )) {
         require(NarratorLegacy.catalogHasEventForTest(id), "Événement absent du catalogue JSON: " + id);
         require(NarratorLegacy.catalogHasDuoTextForTest(id), "Texte partenaire absent du catalogue JSON: " + id);
      }
   }

   private static void require(boolean condition, String message) {
      if (!condition) {
         throw new AssertionError(message);
      }
   }
}
