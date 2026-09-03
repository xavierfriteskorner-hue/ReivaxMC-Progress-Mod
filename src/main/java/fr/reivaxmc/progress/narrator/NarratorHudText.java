package fr.reivaxmc.progress.narrator;

/** Texte compact partagé par le HUD et les auto-tests. */
final class NarratorHudText {
   private NarratorHudText() {
   }

   static String pointsLine(int agePoints, int civilizationPoints) {
      String age = agePoints > 0 ? "+" + agePoints + " ÂGE" : "";
      String civilization = civilizationPoints > 0 ? "+" + civilizationPoints + " CIVILISATION" : "";
      if (!age.isBlank() && !civilization.isBlank()) {
         return age + "   ·   " + civilization;
      }
      return !age.isBlank() ? age : civilization;
   }
}

