package fr.reivaxmc.progress.story;

import java.nio.file.Files;
import java.nio.file.Path;

/** Audit structurel autonome : évite le retour des régressions 0.12.0. */
public final class SanctuaryArchitectureSelfTest {
   public static void main(String[] args) throws Exception {
      check(F8SanctuaryEngine.WATCHER_OFFSETS.length == 6, "six Veilleurs attendus");
      check(F8SanctuaryEngine.WATCHER_OFFSETS[0][1] == 25 && F8SanctuaryEngine.WATCHER_OFFSETS[1][1] == 25, "deux Veilleurs au seuil");
      check(F8SanctuaryEngine.WATCHER_OFFSETS[2][1] == 14 && F8SanctuaryEngine.WATCHER_OFFSETS[3][1] == 14, "deux Veilleurs dans le hall");
      check(Math.abs(F8SanctuaryEngine.WATCHER_OFFSETS[4][0]) > 18 && Math.abs(F8SanctuaryEngine.WATCHER_OFFSETS[5][0]) > 18, "rôdeurs hors de la nef");

      String engine = Files.readString(Path.of("src/main/java/fr/reivaxmc/progress/story/F8SanctuaryEngine.java"));
      String architecture = Files.readString(Path.of("src/main/java/fr/reivaxmc/progress/story/C110SanctuaryArchitecture.java"));
      String dev = Files.readString(Path.of("src/main/java/fr/reivaxmc/progress/story/F81DevTools.java"));
      check(!engine.contains("F94SanctuaryShell.build(var0, var7)"), "aucun second constructeur dans la boucle");
      check(engine.contains("C110SanctuaryArchitecture.build(var0, var7)"), "constructeur canonique actif");
      check(!architecture.contains("ensureSilentPresences"), "aucun faux gardien décoratif");
      check(architecture.contains("monolith(l,o,-9,36") && architecture.contains("monolith(l,o,9,36"), "deux monolithes symétriques");
      check(architecture.contains("F122_SANCTUARY_DECORATED") && architecture.contains("at(origin, 0, 20, -3)"), "migration décorative unique");
      check(architecture.contains("roofscape(level, origin") && architecture.contains("interior(level, origin"), "toiture et intérieur décorés");
      check(architecture.contains("foundationAltar(level, origin") && architecture.contains("Socle à trois degrés"), "socle de Borne présent");
      check(architecture.contains("if(!(dz>=3&&Math.abs(dx)<=1))"), "approche du Protecteur préservée");
      check(dev.contains("runCommand(var0, \"tp \" + var3"), "téléportation DEV réelle");
      System.out.println("SanctuaryArchitectureSelfTest: OK");
   }

   private static void check(boolean condition, String message) {
      if (!condition) throw new AssertionError(message);
   }
}
