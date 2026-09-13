package fr.reivaxmc.progress.story;

import java.util.Map;
import java.util.Set;

/** Vérifie sans lancer Minecraft les réparations appliquées aux sauvegardes historiques. */
public final class StorySaveMigrationSelfTest {
   public static void main(String[] args) {
      StorySaveMigrationRules.FoyerState foyer = StorySaveMigrationRules.foyer(
         F91ChapterRules.DEFEND_FOYER, 0xffff, 2, 7, false, true, true);
      check(F91ChapterRules.COMPLETE.equals(foyer.stage()) && foyer.completed(), "chapitre I terminal réparé");
      check(foyer.echoPlaced() && foyer.defeated() == 2, "invariants du Foyer réparés");
      check(F91ChapterRules.signalCount(foyer.signals()) == 5, "masque du Foyer nettoyé");

      StorySaveMigrationRules.DebtState debt = StorySaveMigrationRules.debt(
         C110TrailRules.REGISTRY, 0b001, 0xff, 0xffff, false, true, true);
      check(C110TrailRules.COMPLETE.equals(debt.stage()) && debt.completed(), "chapitre II terminal réparé");
      check(debt.discoveredMask() == 1 && debt.witnessMask() == 0xff && debt.concordance(), "compteurs II normalisés");

      StorySaveMigrationRules.Chapter3State chapter3 = StorySaveMigrationRules.chapter3(0xff, 99);
      check(chapter3.traceMask() == 0b1111 && chapter3.vanished() == 6, "compteurs III bornés");
      Set<String> fromPerceptions = StorySaveMigrationRules.participants(
         Set.of(), Map.of("joueur-a", "sept noms").keySet(), "", "");
      check(fromPerceptions.equals(Set.of("joueur-a")), "participants III restaurés depuis les perceptions");

      StorySaveMigrationRules.Chapter4State chapter4 = StorySaveMigrationRules.chapter4(0xff, 0xff);
      check(chapter4.testimonyMask() == 0b111 && chapter4.optionalMask() == 0b11, "masques IV bornés");
      Set<String> fromLecterns = StorySaveMigrationRules.participants(Set.of(), Set.of(), "joueur-a", "joueur-b");
      check(fromLecterns.equals(Set.of("joueur-a", "joueur-b")), "participants IV restaurés depuis les pupitres");

      StorySaveMigrationRules.CampaignState campaign = StorySaveMigrationRules.campaign(
         "", -20, 40, 90, true, true, false, true);
      check("DORMANT".equals(campaign.stage()) && campaign.progress() == 0, "socle narratif historique normalisé");
      check(campaign.score() == 40 && campaign.civilizationSpent() == 40, "solde de civilisation borné");
      check(!campaign.introRunning() && campaign.matrixDiscovered(), "drapeaux de campagne réparés");

      check(StorySaveMigrationRules.SCHEMA_VERSION == 2, "version de schéma attendue");
      System.out.println("StorySaveMigrationSelfTest: OK");
   }

   private static void check(boolean condition, String message) {
      if (!condition) throw new AssertionError(message);
   }
}
