package fr.reivaxmc.progress.story;

/** Vérifie les invariants purs qui empêchent le chevauchement des chapitres. */
public final class CampaignCoordinatorSelfTest {
   public static void main(String[] args) {
      CampaignCoordinator.StageAudit fresh = CampaignCoordinator.auditStages(
         F91ChapterRules.LOCKED, C110TrailRules.LOCKED, V12TrailRules.LOCKED, V13Chapter4Rules.LOCKED, false, false);
      check(fresh.activeCount() == 0 && fresh.issues().isEmpty(), "monde neuf cohérent");

      CampaignCoordinator.StageAudit normal = CampaignCoordinator.auditStages(
         F91ChapterRules.COMPLETE, C110TrailRules.COMPLETE, V12TrailRules.COMPLETE, V13Chapter4Rules.OFFERED, true, true);
      check(normal.activeCount() == 1 && normal.issues().isEmpty(), "chaîne I→IV cohérente");

      CampaignCoordinator.StageAudit overlap = CampaignCoordinator.auditStages(
         F91ChapterRules.SHAPE_FOYER, C110TrailRules.SEEK_RIFTS, V12TrailRules.LOCKED, V13Chapter4Rules.LOCKED, false, false);
      check(overlap.activeCount() == 2, "chevauchement compté");
      check(overlap.issues().stream().anyMatch(s -> s.contains("Plusieurs chapitres")), "chevauchement signalé");
      check(overlap.issues().stream().anyMatch(s -> s.contains("Chapitre II")), "prérequis II signalé");

      CampaignCoordinator.StageAudit brokenFour = CampaignCoordinator.auditStages(
         F91ChapterRules.COMPLETE, C110TrailRules.COMPLETE, V12TrailRules.MEMORY_COUNCIL, V13Chapter4Rules.TESTIMONIES, true, true);
      check(brokenFour.issues().stream().anyMatch(s -> s.contains("Chapitre IV")), "prérequis IV signalé");
      System.out.println("CampaignCoordinatorSelfTest: OK");
   }

   private static void check(boolean condition, String message) {
      if (!condition) throw new AssertionError(message);
   }
}
