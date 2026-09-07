package fr.reivaxmc.progress.story;

import java.util.Set;

/** Auto-test autonome des invariants du moteur de Pistes et du Chapitre II. */
public final class C110TrailRulesSelfTest {
   private C110TrailRulesSelfTest() {
   }

   public static void main(String[] args) {
      require(C110TrailRules.REQUIRED_RIFTS == 2, "Deux Fêlures doivent suffire à poursuivre.");
      require(C110TrailRules.TOTAL_RIFTS == 3, "La troisième Fêlure doit rester facultative.");
      require(C110TrailRules.REQUIRED_WITNESSES == 3, "Trois Veilleurs doivent être reconnus.");
      require(C110TrailRules.count(0b011) == 2, "Le masque doit compter deux Fêlures.");
      require(C110TrailRules.count(0b111) == 3, "Le masque doit compter la Fêlure facultative.");
      require(C110TrailRules.objective(C110TrailRules.OFFERED, 0, 0).contains("libre"),
         "Une Piste proposée doit préserver explicitement la liberté du joueur.");
      require(C110TrailRules.objective(C110TrailRules.RETURN_SANCTUARY, 2, 3).contains("Matrice"),
         "Le Fragment doit rester rattaché à la Matrice et non à la Borne.");
      Set<String> stages = Set.of(C110TrailRules.LOCKED, C110TrailRules.OFFERED, C110TrailRules.SEEK_RIFTS,
         C110TrailRules.RETURN_FOYER, C110TrailRules.CENSUS, C110TrailRules.RETURN_SANCTUARY,
         C110TrailRules.REGISTRY, C110TrailRules.COMPLETE);
      require(stages.size() == 8, "Les huit états de la Piste doivent être uniques.");
      System.out.println("REIVAX Chapitre II self-test: OK (Pistes, Fêlures, Recensement, Concordance)");
   }

   private static void require(boolean condition, String message) {
      if (!condition) throw new AssertionError(message);
   }
}
