package fr.reivaxmc.progress.story;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

/** Règles pures et testables utilisées lors du chargement des anciennes sauvegardes. */
final class StorySaveMigrationRules {
   static final int SCHEMA_VERSION = 2;

   private StorySaveMigrationRules() {}

   static FoyerState foyer(String stage, int signals, int target, int defeated,
      boolean echoPlaced, boolean echoExamined, boolean completed) {
      int knownSignals = F91ChapterRules.REST | F91ChapterRules.STORAGE | F91ChapterRules.WORK
         | F91ChapterRules.LIGHT | F91ChapterRules.IDENTITY;
      int cleanTarget = Math.max(0, target);
      boolean terminal = completed || F91ChapterRules.COMPLETE.equals(stage);
      return new FoyerState(terminal ? F91ChapterRules.COMPLETE : stage, signals & knownSignals,
         cleanTarget, Math.max(0, Math.min(cleanTarget, defeated)), echoPlaced || echoExamined, terminal);
   }

   static DebtState debt(String stage, int placedMask, int discoveredMask, int witnessMask,
      boolean concordance, boolean registryOpened, boolean completed) {
      int cleanPlaced = placedMask & 0b111;
      boolean terminal = completed || C110TrailRules.COMPLETE.equals(stage);
      return new DebtState(terminal ? C110TrailRules.COMPLETE : stage, cleanPlaced,
         discoveredMask & cleanPlaced, witnessMask & 0xff, concordance || registryOpened, terminal);
   }

   static Chapter3State chapter3(int traceMask, int vanished) {
      return new Chapter3State(traceMask & 0b1111, Math.max(0, Math.min(6, vanished)));
   }

   static Chapter4State chapter4(int testimonyMask, int optionalMask) {
      return new Chapter4State(testimonyMask & 0b111, optionalMask & 0b11);
   }

   static Set<String> participants(Collection<String> existing, Collection<String> perceptionIds,
      String leftPlayer, String rightPlayer) {
      Set<String> result = new LinkedHashSet<>(existing);
      if (result.isEmpty()) result.addAll(perceptionIds);
      if (result.isEmpty()) {
         if (leftPlayer != null && !leftPlayer.isBlank()) result.add(leftPlayer);
         if (rightPlayer != null && !rightPlayer.isBlank()) result.add(rightPlayer);
      }
      result.removeIf(id -> id == null || id.isBlank());
      return Set.copyOf(result);
   }

   record FoyerState(String stage, int signals, int target, int defeated, boolean echoPlaced, boolean completed) {}
   record DebtState(String stage, int placedMask, int discoveredMask, int witnessMask, boolean concordance, boolean completed) {}
   record Chapter3State(int traceMask, int vanished) {}
   record Chapter4State(int testimonyMask, int optionalMask) {}
}
