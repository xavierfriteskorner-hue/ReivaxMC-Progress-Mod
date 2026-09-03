package fr.reivaxmc.progress.narrator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Détecteur pur des cinq transitions narratives internes du sous-lot A1.
 *
 * <p>Il ne connaît ni Minecraft, ni les textes, ni le HUD. Il compare deux
 * instantanés du scénario et produit uniquement des faits neutres destinés au
 * Narrator Brain. La déduplication persistante reste assurée au moment où le
 * Brain accepte l'événement dans CampaignSavedData.</p>
 */
final class NarratorStorySignalDetector {
   static final String FIRST_HOME = "A1-051";
   static final String MATRIX_INSTALLED = "A1-066";
   static final String FIRST_RESONANCE = "A1-096";
   static final String STELA_DISCOVERED = "A1-097";
   static final String MATRIX_RECOGNIZED = "A1-099";

   private NarratorStorySignalDetector() {
   }

   record Snapshot(
      boolean firstHome,
      boolean matrixInstalled,
      boolean firstResonance,
      boolean stelaDiscovered,
      boolean matrixRecognized
   ) {
   }

   record Signal(String eventId, String storyState) {
   }

   /**
    * Retourne chaque passage faux → vrai une seule fois pour la paire
    * d'instantanés fournie. L'ordre suit la chronologie canonique de l'Âge I.
    */
   static List<Signal> detect(Snapshot previous, Snapshot current) {
      if (previous == null || current == null) {
         return List.of();
      }

      ArrayList<Signal> signals = new ArrayList<>(5);
      addTransition(signals, previous.firstResonance(), current.firstResonance(), FIRST_RESONANCE, "FIRST_RESONANCE");
      addTransition(signals, previous.stelaDiscovered(), current.stelaDiscovered(), STELA_DISCOVERED, "STELA_DISCOVERED");
      addTransition(signals, previous.matrixRecognized(), current.matrixRecognized(), MATRIX_RECOGNIZED, "MATRIX_RECOGNIZED");
      addTransition(signals, previous.firstHome(), current.firstHome(), FIRST_HOME, "FIRST_HOME");
      addTransition(signals, previous.matrixInstalled(), current.matrixInstalled(), MATRIX_INSTALLED, "MATRIX_INSTALLED");
      return Collections.unmodifiableList(signals);
   }

   private static void addTransition(List<Signal> signals, boolean before, boolean after, String eventId, String storyState) {
      if (!before && after) {
         signals.add(new Signal(eventId, storyState));
      }
   }
}
