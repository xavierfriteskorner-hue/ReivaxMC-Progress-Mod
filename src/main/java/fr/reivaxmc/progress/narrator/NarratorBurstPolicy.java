package fr.reivaxmc.progress.narrator;

import java.util.List;

/** Règles pures de condensation des petits événements rapprochés. */
final class NarratorBurstPolicy {
   static final long COLLECTION_DELAY_MS = 1600L;
   static final long SAME_BURST_WINDOW_MS = 2500L;
   static final int MAX_EVENTS_PER_PANEL = 3;

   private NarratorBurstPolicy() {
   }

   static boolean shouldWait(String group, long createdAt, long now) {
      return group != null && !group.isBlank() && now - createdAt < COLLECTION_DELAY_MS;
   }

   static boolean canJoin(
      String anchorGroup,
      String candidateGroup,
      String anchorActor,
      String candidateActor,
      long anchorCreatedAt,
      long candidateCreatedAt,
      boolean candidateHasReward
   ) {
      return !candidateHasReward
         && anchorGroup != null
         && !anchorGroup.isBlank()
         && anchorGroup.equals(candidateGroup)
         && anchorActor != null
         && anchorActor.equals(candidateActor)
         && Math.abs(candidateCreatedAt - anchorCreatedAt) <= SAME_BURST_WINDOW_MS;
   }

   static String combine(List<String> texts) {
      return texts.stream().filter(text -> text != null && !text.isBlank()).limit(MAX_EVENTS_PER_PANEL).reduce((a, b) -> a + "  •  " + b).orElse("");
   }
}

