package fr.reivaxmc.progress.story;

import fr.reivaxmc.progress.progression.CampaignSavedData;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

/**
 * Autorité unique pour l'ordre des chapitres principaux.
 *
 * <p>Les moteurs restent propriétaires de leur gameplay. Ce coordinateur ne
 * joue jamais une scène à leur place : il vérifie seulement les prérequis et
 * construit les profils DEV de manière atomique.</p>
 */
public final class CampaignCoordinator {
   public static final int CHAPTER_1 = 1;
   public static final int CHAPTER_2 = 2;
   public static final int CHAPTER_3 = 3;
   public static final int CHAPTER_4 = 4;
   private static final Set<String> CHAPTER_1_STAGES = Set.of(F91ChapterRules.LOCKED, F91ChapterRules.SHAPE_FOYER,
      F91ChapterRules.CHOOSE_PRIORITY, F91ChapterRules.FIND_ECHO, F91ChapterRules.RETURN_FRAGMENT,
      F91ChapterRules.DEFEND_FOYER, F91ChapterRules.COMPLETE);
   private static final Set<String> CHAPTER_2_STAGES = Set.of(C110TrailRules.LOCKED, C110TrailRules.OFFERED,
      C110TrailRules.SEEK_RIFTS, C110TrailRules.RETURN_FOYER, C110TrailRules.CENSUS,
      C110TrailRules.RETURN_SANCTUARY, C110TrailRules.REGISTRY, C110TrailRules.COMPLETE);
   private static final Set<String> CHAPTER_3_STAGES = Set.of(V12TrailRules.LOCKED, V12TrailRules.OFFERED,
      V12TrailRules.EIGHTH_LINE, V12TrailRules.PROCESSION_LOCATE, V12TrailRules.PROCESSION_OBSERVE,
      V12TrailRules.HOUSE_LOCATE, V12TrailRules.HOUSE_INSPECT, V12TrailRules.RETURN_REGISTRY,
      V12TrailRules.CROSS_CALL, V12TrailRules.MEMORY_COUNCIL, V12TrailRules.COMPLETE);
   private static final Set<String> CHAPTER_4_STAGES = Set.of(V13Chapter4Rules.LOCKED, V13Chapter4Rules.OFFERED,
      V13Chapter4Rules.TESTIMONIES, V13Chapter4Rules.SEEK_FRAGMENT, V13Chapter4Rules.RETURN_GALLERY,
      V13Chapter4Rules.STRONG_CONCORDANCE, V13Chapter4Rules.MATRIX_READY, V13Chapter4Rules.REVELATION,
      V13Chapter4Rules.COMPLETE);

   private CampaignCoordinator() {}

   public static boolean canStart(MinecraftServer server, int chapter) {
      Snapshot state = inspect(server);
      if (chapter < CHAPTER_1 || chapter > CHAPTER_4 || state.activeCount() > 0) return false;
      return switch (chapter) {
         case CHAPTER_1 -> F91ChapterRules.LOCKED.equals(state.chapter1())
            && C110TrailRules.LOCKED.equals(state.chapter2()) && V12TrailRules.LOCKED.equals(state.chapter3())
            && V13Chapter4Rules.LOCKED.equals(state.chapter4());
         case CHAPTER_2 -> state.chapter1Complete() && C110TrailRules.LOCKED.equals(state.chapter2())
            && V12TrailRules.LOCKED.equals(state.chapter3()) && V13Chapter4Rules.LOCKED.equals(state.chapter4());
         case CHAPTER_3 -> state.chapter1Complete() && state.chapter2Complete()
            && V12TrailRules.LOCKED.equals(state.chapter3()) && V13Chapter4Rules.LOCKED.equals(state.chapter4());
         case CHAPTER_4 -> state.chapter1Complete() && state.chapter2Complete() && state.chapter3Complete()
            && V13Chapter4Rules.LOCKED.equals(state.chapter4());
         default -> false;
      };
   }

   /**
    * Prépare un chapitre sans créer les structures ni jouer ses scènes.
    * Tous les chapitres précédents deviennent terminés et tous les suivants
    * redeviennent verrouillés : il ne peut donc rester qu'une seule piste.
    */
   public static void prepareDevChapter(MinecraftServer server, ServerPlayer player, int chapter) {
      if (chapter < CHAPTER_1 || chapter > CHAPTER_4) throw new IllegalArgumentException("chapter=" + chapter);
      long tick = server.overworld().getGameTime();
      String actor = player == null ? "DEV" : player.getGameProfile().getName();

      F91FoyerChapterData chapter1 = F91FoyerChapterData.get(server);
      C110TrailData chapter2 = C110TrailData.get(server);
      V12TrailData chapter3 = V12TrailData.get(server);
      V13Chapter4Data chapter4 = V13Chapter4Data.get(server);

      if (chapter > CHAPTER_1) chapter1.devComplete(tick, actor, F91ChapterRules.MEMORY);
      else chapter1.reset();

      if (chapter > CHAPTER_2) chapter2.devComplete(tick, actor);
      else chapter2.reset();

      if (chapter > CHAPTER_3) chapter3.devComplete(tick, player == null ? actor : player.getUUID().toString(), "KEEP_BOTH");
      else chapter3.reset();

      chapter4.reset();

      CampaignSavedData.get(server).stage(switch (chapter) {
         case CHAPTER_1 -> "AFTER_FOUNDATION";
         case CHAPTER_2 -> "CH1_COMPLETE";
         case CHAPTER_3 -> "CH2_COMPLETE";
         case CHAPTER_4 -> "CH3_COMPLETE";
         default -> "DORMANT";
      });
   }

   public static void resetChapters(MinecraftServer server) {
      F91FoyerChapterData.get(server).reset();
      C110TrailData.get(server).reset();
      V12TrailData.get(server).reset();
      V13Chapter4Data.get(server).reset();
      CampaignSavedData campaign = CampaignSavedData.get(server);
      campaign.stage(campaign.foundationPlaced() ? "AFTER_FOUNDATION" : "DORMANT");
   }

   public static Snapshot inspect(MinecraftServer server) {
      F91FoyerChapterData.Snapshot one = F91FoyerChapterData.get(server).snapshot();
      C110TrailData.Snapshot two = C110TrailData.get(server).snapshot();
      V12TrailData.Snapshot three = V12TrailData.get(server).snapshot();
      V13Chapter4Data.Snapshot four = V13Chapter4Data.get(server).snapshot();
      StageAudit audit = auditStages(one.stage(), two.stage(), three.stage(), four.stage(),
         one.completed(), two.completed());
      return new Snapshot(CampaignSavedData.get(server).stage(), one.stage(), two.stage(), three.stage(), four.stage(),
         one.completed(), two.completed(), V12TrailRules.COMPLETE.equals(three.stage()), V13Chapter4Rules.COMPLETE.equals(four.stage()),
         audit.activeCount(), audit.issues());
   }

   static StageAudit auditStages(String one, String two, String three, String four, boolean oneComplete, boolean twoComplete) {
      int active = 0;
      if (active(one, F91ChapterRules.LOCKED, F91ChapterRules.COMPLETE)) active++;
      if (active(two, C110TrailRules.LOCKED, C110TrailRules.COMPLETE)) active++;
      if (active(three, V12TrailRules.LOCKED, V12TrailRules.COMPLETE)) active++;
      if (active(four, V13Chapter4Rules.LOCKED, V13Chapter4Rules.COMPLETE)) active++;

      List<String> issues = new ArrayList<>();
      reportUnknown(issues, "I", one, CHAPTER_1_STAGES);
      reportUnknown(issues, "II", two, CHAPTER_2_STAGES);
      reportUnknown(issues, "III", three, CHAPTER_3_STAGES);
      reportUnknown(issues, "IV", four, CHAPTER_4_STAGES);
      if (active > 1) issues.add("Plusieurs chapitres principaux sont actifs simultanément (" + active + ").");
      if (active(two, C110TrailRules.LOCKED, C110TrailRules.COMPLETE) && !oneComplete)
         issues.add("Le Chapitre II est actif avant la fin du Chapitre I.");
      if (active(three, V12TrailRules.LOCKED, V12TrailRules.COMPLETE) && !twoComplete)
         issues.add("Le Chapitre III est actif avant la fin du Chapitre II.");
      if (active(four, V13Chapter4Rules.LOCKED, V13Chapter4Rules.COMPLETE) && !V12TrailRules.COMPLETE.equals(three))
         issues.add("Le Chapitre IV est actif avant la fin du Chapitre III.");
      if (twoComplete && !oneComplete) issues.add("Le Chapitre II est terminé sans Chapitre I terminé.");
      if (V12TrailRules.COMPLETE.equals(three) && !twoComplete) issues.add("Le Chapitre III est terminé sans Chapitre II terminé.");
      if (V13Chapter4Rules.COMPLETE.equals(four) && !V12TrailRules.COMPLETE.equals(three))
         issues.add("Le Chapitre IV est terminé sans Chapitre III terminé.");
      return new StageAudit(active, List.copyOf(issues));
   }

   private static boolean active(String stage, String locked, String complete) {
      return stage != null && !stage.equals(locked) && !stage.equals(complete);
   }

   private static void reportUnknown(List<String> issues, String chapter, String stage, Set<String> knownStages) {
      if (stage == null || !knownStages.contains(stage))
         issues.add("Le Chapitre " + chapter + " contient une étape inconnue : " + String.valueOf(stage) + ".");
   }

   static record StageAudit(int activeCount, List<String> issues) {}

   public record Snapshot(String campaignStage, String chapter1, String chapter2, String chapter3, String chapter4,
      boolean chapter1Complete, boolean chapter2Complete, boolean chapter3Complete, boolean chapter4Complete,
      int activeCount, List<String> issues) {
      public boolean coherent() { return issues.isEmpty(); }
   }
}
