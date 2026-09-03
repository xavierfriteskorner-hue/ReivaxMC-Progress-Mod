package fr.reivaxmc.progress.narrator;

import java.util.List;
import java.util.Map;

/** Test autonome sans monde Minecraft : exécutable par la tâche Gradle checkA1Signals. */
public final class NarratorA1SelfTest {
   private NarratorA1SelfTest() {
   }

   public static void main(String[] args) {
      detectorEmitsOnlyFalseToTrueTransitions();
      detectorKeepsCanonicalOrder();
      brainStillChoosesContextualVariants();
      System.out.println("REIVAX A1 self-test: OK");
   }

   private static void detectorEmitsOnlyFalseToTrueTransitions() {
      NarratorStorySignalDetector.Snapshot before = state(false, false, false);
      NarratorStorySignalDetector.Snapshot after = state(false, true, false);
      List<NarratorStorySignalDetector.Signal> first = NarratorStorySignalDetector.detect(before, after);
      require(first.size() == 1, "La première Résonance doit produire exactement un signal.");
      require(NarratorStorySignalDetector.FIRST_RESONANCE.equals(first.getFirst().eventId()), "Mauvais ID pour la Résonance.");
      require(NarratorStorySignalDetector.detect(after, after).isEmpty(), "Un état inchangé ne doit jamais redéclencher.");
      require(NarratorStorySignalDetector.detect(after, before).isEmpty(), "Un retour vrai → faux ne doit pas produire de first-trigger.");
   }

   private static void detectorKeepsCanonicalOrder() {
      List<NarratorStorySignalDetector.Signal> signals = NarratorStorySignalDetector.detect(
         state(false, false, false),
         state(true, true, true)
      );
      List<String> ids = signals.stream().map(NarratorStorySignalDetector.Signal::eventId).toList();
      require(ids.equals(List.of("A1-096", "A1-097", "A1-051")), "Ordre canonique A1 incorrect: " + ids);
   }

   private static void brainStillChoosesContextualVariants() {
      NarratorContextBrain.Variant familiar = new NarratorContextBrain.Variant(
         "stela_after_origins",
         35,
         Map.of("global_tag_origins_gte", 2),
         "variante",
         "",
         "MYSTERY"
      );
      NarratorContextBrain.Choice choice = NarratorContextBrain.choose(
         List.of(familiar),
         new NarratorContextBrain.Snapshot(Map.of("global_tag_origins", 2)),
         "fallback",
         "fallback duo",
         "A1-test"
      );
      require("stela_after_origins".equals(choice.variantId()), "Le signal doit encore passer par le Brain contextuel.");
      require("variante".equals(choice.actorText()), "Le Brain n'a pas conservé le texte contextuel attendu.");
   }

   private static NarratorStorySignalDetector.Snapshot state(
      boolean home,
      boolean resonance,
      boolean stela
   ) {
      return new NarratorStorySignalDetector.Snapshot(home, resonance, stela);
   }

   private static void require(boolean condition, String message) {
      if (!condition) {
         throw new AssertionError(message);
      }
   }
}
