package fr.reivaxmc.progress.story;

/**
 * Ailes futures du Sanctuaire. Elles sont bâties avec le bâtiment initial, mais leurs
 * seuils restent scellés jusqu'aux futures Concordances narratives.
 */
public final class F94SanctuaryShell {
   public static final String BUILT = "F94_SANCTUARY_FUTURE_ROOMS";

   private F94SanctuaryShell() {
   }

   public static boolean isPresent(Object server, int[] origin) {
      try {
         Object level = F8SanctuaryEngine.invokeNoArg(server, "overworld");
         Object state = F8SanctuaryEngine.invoke(level, "getBlockState",
            F8SanctuaryEngine.blockPos(origin[0] + 28, origin[1] + 5, origin[2] - 10));
         return F8SanctuaryEngine.invokeNoArg(state, "getBlock") == F8SanctuaryEngine.sanctuaryLumenBlock();
      } catch (Throwable ignored) {
         return false;
      }
   }

   public static void build(Object server, int[] origin) {
      try {
         Object level = F8SanctuaryEngine.invokeNoArg(server, "overworld");
         Object stone = F8SanctuaryEngine.sanctuaryStoneBlock();
         Object light = F8SanctuaryEngine.sanctuaryLumenBlock();
         Object floor = F8SanctuaryEngine.staticField("net.minecraft.world.level.block.Blocks", "POLISHED_DEEPSLATE");
         Object seal = F8SanctuaryEngine.staticField("net.minecraft.world.level.block.Blocks", "REINFORCED_DEEPSLATE");
         int ox = origin[0];
         int oy = origin[1];
         int oz = origin[2];

         buildWing(level, ox, oy, oz, -1, stone, floor, light, seal);
         buildWing(level, ox, oy, oz, 1, stone, floor, light, seal);

         // La Matrice existe déjà derrière le sceau occidental. Elle est visible par une meurtrière,
         // mais aucun Fragment ne peut encore l'atteindre ni être analysé.
         F8SanctuaryEngine.setBlock(level, ox - 21, oy + 2, oz - 16, F8SanctuaryEngine.originMatrixBlock());
         F8SanctuaryEngine.setBlock(level, ox - 20, oy + 2, oz - 16, "IRON_BARS");
         F8SanctuaryEngine.setBlock(level, ox - 22, oy + 2, oz - 16, light);

         Object campaign = F8SanctuaryEngine.campaign(server);
         if (!F8SanctuaryEngine.completed(campaign, BUILT)) F8SanctuaryEngine.complete(campaign, BUILT);
      } catch (Throwable error) {
         System.err.println("[REIVAX F94 sanctuary shell] " + error.getClass().getSimpleName() + ": " + error.getMessage());
      }
   }

   private static void buildWing(Object level, int ox, int oy, int oz, int side, Object stone, Object floor, Object light, Object seal) throws Exception {
      int minX = side < 0 ? -28 : 15;
      int maxX = side < 0 ? -15 : 28;
      for (int dx = minX; dx <= maxX; dx++) {
         for (int dz = -22; dz <= -10; dz++) {
            for (int dy = 1; dy <= 7; dy++) {
               boolean wall = dx == minX || dx == maxX || dz == -22 || dz == -10 || dy == 7;
               F8SanctuaryEngine.setBlock(level, ox + dx, oy + dy, oz + dz, wall ? stone : "AIR");
            }
            F8SanctuaryEngine.setBlock(level, ox + dx, oy, oz + dz, (dx + dz) % 7 == 0 ? light : floor);
         }
      }

      // Couloir vers le grand hall : construit maintenant, fermé par une matière sans serrure.
      int inner = side < 0 ? -13 : 13;
      int outer = side < 0 ? -14 : 14;
      for (int dz = -18; dz <= -14; dz++) {
         for (int dy = 1; dy <= 5; dy++) {
            F8SanctuaryEngine.setBlock(level, ox + inner, oy + dy, oz + dz, seal);
            if (dy == 3 && dz == -16) F8SanctuaryEngine.setBlock(level, ox + inner, oy + dy, oz + dz, side < 0 ? "IRON_BARS" : light);
            if (dy == 4 && dz == -16) F8SanctuaryEngine.setBlock(level, ox + inner, oy + dy, oz + dz, light);
            F8SanctuaryEngine.setBlock(level, ox + outer, oy + dy, oz + dz, "AIR");
         }
      }

      // Marqueur discret et stable utilisé pour éviter toute reconstruction au rechargement.
      F8SanctuaryEngine.setBlock(level, ox + side * 28, oy + 5, oz - 10, light);
   }
}
