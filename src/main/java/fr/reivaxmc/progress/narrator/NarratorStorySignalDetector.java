package fr.reivaxmc.progress.narrator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Détecteur pur des transitions narratives internes actuellement jouables du sous-lot A1.
 *
 * <p>Il ne connaît ni Minecraft, ni les textes, ni le HUD. Il compare deux
 * instantanés du scénario et produit uniquement des faits neutres destinés au
 * Narrator Brain. La déduplication persistante reste assurée au moment où le
 * Brain accepte l'événement dans CampaignSavedData.</p>
 */
final class NarratorStorySignalDetector {
   static final String FIRST_HOME = "A1-051";
   static final String FIRST_RESONANCE = "A1-096";
   static final String STELA_DISCOVERED = "A1-097";

   private NarratorStorySignalDetector() {
   }

   record Snapshot(
      boolean firstHome,
      boolean firstResonance,
      boolean stelaActivated
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

      ArrayList<Signal> signals = new ArrayList<>(3);
      addTransition(signals, previous.firstResonance(), current.firstResonance(), FIRST_RESONANCE, "FIRST_RESONANCE");
      addTransition(signals, previous.stelaActivated(), current.stelaActivated(), STELA_DISCOVERED, "SANCTUARY_STELA_ACTIVATED");
      addTransition(signals, previous.firstHome(), current.firstHome(), FIRST_HOME, "FIRST_HOME");
      return Collections.unmodifiableList(signals);
   }

   private static void addTransition(List<Signal> signals, boolean before, boolean after, String eventId, String storyState) {
      if (!before && after) {
         signals.add(new Signal(eventId, storyState));
      }
   }
}
