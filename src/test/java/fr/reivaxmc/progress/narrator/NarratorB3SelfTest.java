package fr.reivaxmc.progress.narrator;

import java.util.HashSet;

/** Auto-test autonome des 9 derniers événements spécialisés B3. */
public final class NarratorB3SelfTest {
   private NarratorB3SelfTest() {
   }

   public static void main(String[] args) {
      require(NarratorB3SignalDetector.ALL_IDS.size() == 9, "B3 doit contenir exactement 9 événements.");
      require(new HashSet<>(NarratorB3SignalDetector.ALL_IDS).size() == 9, "Les IDs B3 doivent être uniques.");
      require(event(NarratorB3SignalDetector.missedArrow(true, true, true, false), NarratorB3SignalDetector.MISSED_ARROW), "Une flèche joueur dans un bloc doit compter.");
      require(NarratorB3SignalDetector.missedArrow(true, true, false, false) == null, "Une cible touchée n'est pas un tir raté.");
      require(event(NarratorB3SignalDetector.endermanSeen(true, 12.0, 0.99, true), NarratorB3SignalDetector.ENDERMAN_SEEN), "Un Enderman regardé doit compter.");
      require(NarratorB3SignalDetector.endermanSeen(true, 12.0, 0.80, true) == null, "Un Enderman seulement proche ne suffit pas.");
      require(event(NarratorB3SignalDetector.homeFire("minecraft:fire", true), NarratorB3SignalDetector.HOME_FIRE), "Un feu confirmé au Foyer doit compter.");
      require(NarratorB3SignalDetector.homeFire("minecraft:fire", false) == null, "Un feu hors Foyer ne doit pas compter.");
      require(event(NarratorB3SignalDetector.homeRoof(true, 32, 7), NarratorB3SignalDetector.HOME_ROOF), "Un toit suffisamment couvert doit compter.");
      require(NarratorB3SignalDetector.homeRoof(true, 31, 9) == null, "Une cavité naturelle ne doit pas devenir une maison.");
      require(event(NarratorB3SignalDetector.namedSign("minecraft:oak_sign", true), NarratorB3SignalDetector.NAMED_SIGN), "Un panneau écrit doit compter.");
      require(NarratorB3SignalDetector.namedSign("minecraft:oak_sign", false) == null, "Un panneau vide ne suffit pas.");
      require(event(NarratorB3SignalDetector.ravine(true, 24, 2, true), NarratorB3SignalDetector.FIRST_RAVINE), "Une fracture profonde encadrée de roche doit compter.");
      require(NarratorB3SignalDetector.ravine(true, 12, 2, true) == null, "Un petit trou n'est pas un ravin.");
      require(event(NarratorB3SignalDetector.preciousRecovery("minecraft:diamond", true, 270_000L, false), NarratorB3SignalDetector.PRECIOUS_RECOVERY), "Un diamant sauvé avant disparition doit compter.");
      require(event(NarratorB3SignalDetector.preciousRecovery("minecraft:diamond", true, 1_000L, true), NarratorB3SignalDetector.PRECIOUS_RECOVERY), "Un diamant sauvé du feu doit compter.");
      require(NarratorB3SignalDetector.preciousRecovery("minecraft:dirt", true, 300_000L, true) == null, "La terre n'est pas précieuse.");
      require(event(NarratorB3SignalDetector.companionTravel(true, 500.0), NarratorB3SignalDetector.COMPANION_TRAVEL), "500 blocs avec un compagnon doivent compter.");
      require(NarratorB3SignalDetector.companionTravel(false, 800.0) == null, "Un animal sauvage ne doit pas compter.");
      require(event(NarratorB3SignalDetector.lostWithCompass(true, true, 300.0, 600.0, 50.0), NarratorB3SignalDetector.LOST_WITH_COMPASS), "Errer avec une boussole doit compter.");
      require(NarratorB3SignalDetector.lostWithCompass(true, true, 300.0, 600.0, 150.0) == null, "Un retour efficace n'est pas une errance.");

      for (String id : NarratorB3SignalDetector.ALL_IDS) {
         require(NarratorLegacy.catalogHasEventForTest(id), "Événement absent du catalogue JSON : " + id);
         require(NarratorLegacy.catalogHasDuoTextForTest(id), "Texte partenaire absent : " + id);
         require(!NarratorLegacy.catalogCondenseGroupForTest(id).isBlank(), "Groupe de condensation absent : " + id);
      }
      System.out.println("REIVAX B3 self-test: OK (9 événements spécialisés, 97/100 actifs)");
   }

   private static boolean event(NarratorB3SignalDetector.Signal signal, String id) {
      return signal != null && id.equals(signal.eventId());
   }

   private static void require(boolean condition, String message) {
      if (!condition) {
         throw new AssertionError(message);
      }
   }
}
