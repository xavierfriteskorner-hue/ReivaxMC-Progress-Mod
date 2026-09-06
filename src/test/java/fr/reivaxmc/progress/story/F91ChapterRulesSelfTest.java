package fr.reivaxmc.progress.story;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/** Auto-test autonome des règles déterministes du Chapitre I. */
public final class F91ChapterRulesSelfTest {
   private F91ChapterRulesSelfTest() {
   }

   public static void main(String[] args) {
      require(F91ChapterRules.classifyBlock("minecraft:red_bed") == F91ChapterRules.REST, "Un lit doit représenter le repos.");
      require(F91ChapterRules.classifyBlock("minecraft:barrel") == F91ChapterRules.STORAGE, "Un tonneau doit représenter la réserve.");
      require(F91ChapterRules.classifyBlock("minecraft:blast_furnace") == F91ChapterRules.WORK, "Un haut fourneau doit représenter le travail.");
      require(F91ChapterRules.classifyBlock("minecraft:soul_lantern") == F91ChapterRules.LIGHT, "Une lanterne doit représenter la lumière.");
      require(F91ChapterRules.classifyBlock("minecraft:white_banner") == F91ChapterRules.IDENTITY, "Une bannière doit représenter l'identité.");
      require(F91ChapterRules.classifyBlock("minecraft:dirt") == 0, "Un bloc quelconque ne doit pas façonner le Foyer.");

      int threeSignals = F91ChapterRules.REST | F91ChapterRules.STORAGE | F91ChapterRules.LIGHT;
      require(F91ChapterRules.signalCount(threeSignals) == 3, "Trois catégories distinctes doivent ouvrir le choix.");
      require(F91ChapterRules.signalCount(threeSignals | F91ChapterRules.LIGHT) == 3, "Répéter une catégorie ne doit pas compter deux fois.");

      require(F91ChapterRules.BASTION.equals(F91ChapterRules.doctrineForItem("minecraft:iron_ingot")), "Le fer doit choisir Protéger.");
      require(F91ChapterRules.MEMORY.equals(F91ChapterRules.doctrineForItem("minecraft:book")), "Le livre doit choisir Comprendre.");
      require(F91ChapterRules.SOLIDARITY.equals(F91ChapterRules.doctrineForItem("minecraft:bread")), "Le pain doit choisir Partager.");
      require(F91ChapterRules.doctrineForItem("minecraft:diamond").isBlank(), "Un autre objet ne doit pas choisir à la place du joueur.");

      Set<String> stages = new HashSet<>(Set.of(
         F91ChapterRules.LOCKED,
         F91ChapterRules.SHAPE_FOYER,
         F91ChapterRules.CHOOSE_PRIORITY,
         F91ChapterRules.FIND_ECHO,
         F91ChapterRules.RETURN_FRAGMENT,
         F91ChapterRules.DEFEND_FOYER,
         F91ChapterRules.COMPLETE
      ));
      require(stages.size() == 7, "Les sept états du chapitre doivent être uniques.");

      Set<String> duo = Set.of("uuid-a", "uuid-b");
      require(F91CouncilRules.evaluate(duo, Map.of("uuid-a", F91ChapterRules.BASTION)).state() == F91CouncilRules.State.WAITING,
         "Le premier vote DUO doit attendre le partenaire.");
      require(F91CouncilRules.evaluate(duo, Map.of("uuid-a", F91ChapterRules.BASTION, "uuid-b", F91ChapterRules.MEMORY)).state()
         == F91CouncilRules.State.DISSONANCE,
         "Deux priorités différentes doivent créer une Dissonance.");
      F91CouncilRules.Result consensus = F91CouncilRules.evaluate(duo,
         Map.of("uuid-a", F91ChapterRules.BASTION, "uuid-b", F91ChapterRules.BASTION));
      require(consensus.state() == F91CouncilRules.State.CONFIRMED,
         "Un consensus DUO doit confirmer la priorité commune.");
      require(F91ChapterRules.BASTION.equals(consensus.doctrine()), "La doctrine confirmée doit être retournée.");
      System.out.println("REIVAX Chapitre I self-test: OK (Foyer, Conseil SOLO/DUO et Dissonance)");
   }

   private static void require(boolean condition, String message) {
      if (!condition) throw new AssertionError(message);
   }
}
