package fr.reivaxmc.progress.story;

/**
 * Pont de compatibilité : depuis 0.11.0, toute l'architecture du Sanctuaire est
 * bâtie d'un seul tenant et ses salles sont ouvertes par les Concordances.
 */
public final class F94SanctuaryShell {
   public static final String BUILT = "F110_SANCTUARY_GRAND_ARCHITECTURE";

   private F94SanctuaryShell() {
   }

   public static boolean isPresent(Object server, int[] origin) {
      return C110SanctuaryArchitecture.isPresent(server, origin);
   }

   public static void build(Object server, int[] origin) {
      C110SanctuaryArchitecture.build(server, origin);
   }
}
