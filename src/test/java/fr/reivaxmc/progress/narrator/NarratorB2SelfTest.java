package fr.reivaxmc.progress.narrator;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Auto-test autonome des 15 événements B2, sans lancer Minecraft. */
public final class NarratorB2SelfTest {
   private NarratorB2SelfTest() {
   }

   public static void main(String[] args) {
      idsAreStableAndUnique();
      homeTransitionsAreStrict();
      duoCorrelationsAreStrict();
      storageRequiresConfirmedState();
      catalogIsReadyForSoloDuoAndBatches();
      System.out.println("REIVAX B2 self-test: OK (15 événements foyer/DUO)");
   }

   private static void idsAreStableAndUnique() {
      require(NarratorB2SignalDetector.ALL_IDS.size() == 15, "B2 doit contenir exactement 15 événements.");
      require(new HashSet<>(NarratorB2SignalDetector.ALL_IDS).size() == 15, "Les IDs B2 doivent être uniques.");
      require("A1-043".equals(NarratorB2SignalDetector.FAR_FROM_HOME), "L'éloignement historique doit garder son ID.");
      require("A1-083".equals(NarratorB2SignalDetector.HOME_EXPLOSION), "L'explosion historique doit garder son ID.");
      require("A1-154".equals(NarratorB2SignalDetector.FIRST_HOME_DOOR), "La nouvelle plage B2 doit commencer à A1-154.");
      require("A1-164".equals(NarratorB2SignalDetector.CHEST_ALMOST_FULL), "La nouvelle plage B2 doit finir à A1-164.");
   }

   private static void homeTransitionsAreStrict() {
      require(NarratorB2SignalDetector.farFromHome(249.9) == null, "249 blocs ne suffisent pas.");
      require(event(NarratorB2SignalDetector.farFromHome(250.0), NarratorB2SignalDetector.FAR_FROM_HOME), "250 blocs doivent compter.");
      List<NarratorB2SignalDetector.Signal> normal = NarratorB2SignalDetector.homeReturn(true, 300.0, 60_000L, false);
      require(normal.isEmpty(), "Un retour ordinaire ne doit pas inventer un jalon majeur.");
      List<NarratorB2SignalDetector.Signal> longTrip = NarratorB2SignalDetector.homeReturn(true, 1000.0, 20L * 60L * 1000L, true);
      require(has(longTrip, NarratorB2SignalDetector.RETURN_HOME), "Le retour après 1000 blocs doit compter.");
      require(has(longTrip, NarratorB2SignalDetector.NIGHT_RETURN), "Le retour de nuit doit compter.");
      require(has(longTrip, NarratorB2SignalDetector.LONG_RETURN), "Le retour après 20 minutes doit compter.");
      require(event(NarratorB2SignalDetector.homeDoor("minecraft:oak_door", true), NarratorB2SignalDetector.FIRST_HOME_DOOR), "Une porte au foyer doit compter.");
      require(NarratorB2SignalDetector.homeDoor("minecraft:oak_door", false) == null, "Une porte hors foyer ne doit pas compter.");
      require(event(NarratorB2SignalDetector.homeExplosion(true, true), NarratorB2SignalDetector.HOME_EXPLOSION), "Une explosion au foyer doit compter.");
   }

   private static void duoCorrelationsAreStrict() {
      List<NarratorB2SignalDetector.Signal> far = NarratorB2SignalDetector.duoDistance(true, 1000.0);
      require(has(far, NarratorB2SignalDetector.PARTNER_500), "Le seuil 500 doit compter.");
      require(has(far, NarratorB2SignalDetector.DUO_VERY_FAR), "Le seuil 1000 doit compter.");
      require(NarratorB2SignalDetector.duoDistance(false, 5000.0).isEmpty(), "Deux dimensions ne doivent pas inventer une distance exacte.");
      require(event(NarratorB2SignalDetector.reunion(true, true, 20.0), NarratorB2SignalDetector.DUO_REUNION), "Les retrouvailles doivent compter.");
      require(NarratorB2SignalDetector.reunion(false, true, 5.0) == null, "Être déjà ensemble n'est pas une retrouvaille.");
      require(event(NarratorB2SignalDetector.partnerNearDeath(true, 32.0), NarratorB2SignalDetector.PARTNER_NEAR_DEATH), "Une mort proche doit compter.");
      require(NarratorB2SignalDetector.partnerNearDeath(true, 32.1) == null, "Une mort lointaine ne doit pas compter.");
      require(event(NarratorB2SignalDetector.bothLowHealth(5.0F, 20.0F, 4.0F, 20.0F), NarratorB2SignalDetector.BOTH_LOW_HEALTH), "Deux joueurs à 25 % doivent compter.");
      require(NarratorB2SignalDetector.bothLowHealth(6.0F, 20.0F, 4.0F, 20.0F) == null, "Un seul joueur faible ne suffit pas.");
      require(event(NarratorB2SignalDetector.sharedDiscovery("a", "b", 10_000L, "A1-141"), NarratorB2SignalDetector.SHARED_DISCOVERY), "La même découverte en dix secondes doit compter.");
      require(NarratorB2SignalDetector.sharedDiscovery("a", "a", 100L, "A1-141") == null, "Un joueur seul ne doit pas imiter le DUO.");
      require(event(NarratorB2SignalDetector.partnerGift("a", "b", 15_000L, true), NarratorB2SignalDetector.FIRST_PARTNER_GIFT), "Le même objet ramassé par le partenaire doit compter.");
      require(NarratorB2SignalDetector.partnerGift("a", "b", 15_001L, true) == null, "Un ancien objet au sol ne doit pas compter comme cadeau.");
   }

   private static void storageRequiresConfirmedState() {
      List<NarratorB2SignalDetector.Signal> full = NarratorB2SignalDetector.homeContainer(
         "minecraft:chest", 25, 27, Set.of("minecraft:diamond_block")
      );
      require(has(full, NarratorB2SignalDetector.PRECIOUS_STORED), "Un bloc précieux stocké doit compter.");
      require(has(full, NarratorB2SignalDetector.CHEST_ALMOST_FULL), "25 cases sur 27 doivent compter.");
      require(!has(NarratorB2SignalDetector.homeContainer("minecraft:chest", 24, 27, Set.of("minecraft:dirt")), NarratorB2SignalDetector.CHEST_ALMOST_FULL), "24 cases sur 27 ne suffisent pas.");
      require(!has(NarratorB2SignalDetector.homeContainer("minecraft:barrel", 27, 27, Set.of()), NarratorB2SignalDetector.CHEST_ALMOST_FULL), "Le jalon coffre ne doit pas viser un tonneau.");
   }

   private static void catalogIsReadyForSoloDuoAndBatches() {
      for (String id : NarratorB2SignalDetector.ALL_IDS) {
         require(NarratorLegacy.catalogHasEventForTest(id), "Événement absent du catalogue JSON : " + id);
         require(NarratorLegacy.catalogHasDuoTextForTest(id), "Texte partenaire absent : " + id);
         require(!NarratorLegacy.catalogCondenseGroupForTest(id).isBlank(), "Groupe de condensation absent : " + id);
      }
   }

   private static boolean has(List<NarratorB2SignalDetector.Signal> signals, String id) {
      return signals.stream().anyMatch(signal -> id.equals(signal.eventId()));
   }

   private static boolean event(NarratorB2SignalDetector.Signal signal, String id) {
      return signal != null && id.equals(signal.eventId());
   }

   private static void require(boolean condition, String message) {
      if (!condition) {
         throw new AssertionError(message);
      }
   }
}
