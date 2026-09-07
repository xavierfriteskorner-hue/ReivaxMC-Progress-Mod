package fr.reivaxmc.progress.story;

/** Règles pures de la première Piste du Chapitre II. */
public final class C110TrailRules {
   public static final String LOCKED = "LOCKED";
   public static final String OFFERED = "OFFERED";
   public static final String SEEK_RIFTS = "SEEK_RIFTS";
   public static final String RETURN_FOYER = "RETURN_FOYER";
   public static final String CENSUS = "CENSUS";
   public static final String RETURN_SANCTUARY = "RETURN_SANCTUARY";
   public static final String REGISTRY = "REGISTRY";
   public static final String COMPLETE = "COMPLETE";

   public static final int REQUIRED_RIFTS = 2;
   public static final int TOTAL_RIFTS = 3;
   public static final int REQUIRED_WITNESSES = 3;

   private C110TrailRules() {
   }

   public static int count(int mask) {
      return Integer.bitCount(mask & 7);
   }

   public static String title(String stage) {
      return switch (stage) {
         case OFFERED -> "La Dette du Foyer";
         case SEEK_RIFTS -> "Les Fêlures";
         case RETURN_FOYER -> "L'Accord du Foyer";
         case CENSUS -> "Le Recensement";
         case RETURN_SANCTUARY -> "La Concordance";
         case REGISTRY -> "La Salle du Registre";
         case COMPLETE -> "La Dette du Foyer — achevée";
         default -> "Aucune piste suivie";
      };
   }

   public static String objective(String stage, int rifts, int witnesses) {
      return switch (stage) {
         case OFFERED -> "Une anomalie attend dans la Borne. Vous restez libre de la suivre plus tard.";
         case SEEK_RIFTS -> "Explorez les alentours du Foyer · Fêlures comprises " + rifts + "/2.";
         case RETURN_FOYER -> "Revenez à la Borne : le Foyer doit donner un sens à ce que vous avez vu.";
         case CENSUS -> "Approchez les silhouettes sans les combattre · Veilleurs reconnus " + witnesses + "/3.";
         case RETURN_SANCTUARY -> "Rapportez le Fragment au Sanctuaire. La Matrice seule pourra un jour l'analyser.";
         case REGISTRY -> "Franchissez le seuil oriental et consultez le Registre des Absents.";
         case COMPLETE -> "Cette piste est achevée. Le troisième site demeure facultatif.";
         default -> "Aucune piste active.";
      };
   }
}
