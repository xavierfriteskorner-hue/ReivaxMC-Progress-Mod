package fr.reivaxmc.progress.story;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

public final class F90Sanctuary {
   public static final String LEFT = "F90_LEFT_SEAL";
   public static final String RIGHT = "F90_RIGHT_SEAL";
   private static final Map<Object, Integer> TICKS = Collections.synchronizedMap(new WeakHashMap<>());

   private F90Sanctuary() {
   }

   public static void postBuild(Object var0, int var1, int var2, int var3, boolean var4) {
      try {
         Object var5 = F8SanctuaryEngine.invokeNoArg(var0, "overworld");
         if (var5 == null) {
            return;
         }

         for (int var6 = -2; var6 <= 2; var6++) {
            for (int var7 = 1; var7 <= 4; var7++) {
               for (int var8 = 28; var8 <= 30; var8++) {
                  F8SanctuaryEngine.setBlock(var5, var1 + var6, var2 + var7, var3 + var8, "AIR");
               }
            }
         }

         Object var10 = F8SanctuaryEngine.sanctuaryStoneBlock();

         for (int var11 = -3; var11 <= 3; var11++) {
            for (int var12 = 27; var12 <= 31; var12++) {
               F8SanctuaryEngine.setBlock(var5, var1 + var11, var2, var3 + var12, var10);
            }
         }

         buildReceptacle(var5, var1, var2, var3, -1, false);
         buildReceptacle(var5, var1, var2, var3, 1, false);
         decorateOnce(var5, var1, var2, var3);
      } catch (Throwable var9) {
         System.err.println("[REIVAX F9 sanctuary build] " + var9.getClass().getSimpleName() + ": " + var9.getMessage());
      }
   }

   // Stèle = GROS POTEAU de pierre très noire, avec un CREUX au milieu où l'on voit le disque (Sceau)
   // enchâssé -> on comprend qu'il faut y déposer le Sceau. var5 = true quand le Sceau est en place (lueur).
   private static void buildReceptacle(Object var0, int var1, int var2, int var3, int var4, boolean var5) throws Exception {
      int var6 = var1 + var4 * 7; // x du poteau (de part et d'autre de l'entrée)
      int var7 = var3 + 28;       // z ; la face avant (vers +z) regarde le joueur
      Object var8 = F8SanctuaryEngine.originMatrixBlock();   // le Sceau/disque, au fond du creux
      Object var9 = F8SanctuaryEngine.sanctuaryLumenBlock(); // lueur quand enchâssé
      Object var10 = F8SanctuaryEngine.staticField("net.minecraft.world.level.block.Blocks", "POLISHED_BLACKSTONE");
      Object var11 = F8SanctuaryEngine.staticField("net.minecraft.world.level.block.Blocks", "POLISHED_BLACKSTONE_BRICKS");
      Object var12 = F8SanctuaryEngine.staticField("net.minecraft.world.level.block.Blocks", "CHISELED_POLISHED_BLACKSTONE");
      Object var13 = F8SanctuaryEngine.staticField("net.minecraft.world.level.block.Blocks", "POLISHED_DEEPSLATE");

      // Socle 3x3
      for (int var14 = -1; var14 <= 1; var14++) {
         for (int var15 = -1; var15 <= 1; var15++) {
            F8SanctuaryEngine.setBlock(var0, var6 + var14, var2, var7 + var15, var13);
         }
      }

      // Corps du poteau : mur du fond (z-1) massif, 3 large x 5 haut
      for (int var16 = -1; var16 <= 1; var16++) {
         for (int var17 = 1; var17 <= 5; var17++) {
            F8SanctuaryEngine.setBlock(var0, var6 + var16, var2 + var17, var7 - 1, var11);
         }
      }

      // Cadre avant (z) : colonnes latérales, base et linteau (le centre reste OUVERT = le creux)
      for (int var18 = 1; var18 <= 5; var18++) {
         F8SanctuaryEngine.setBlock(var0, var6 - 1, var2 + var18, var7, var10);
         F8SanctuaryEngine.setBlock(var0, var6 + 1, var2 + var18, var7, var10);
      }

      F8SanctuaryEngine.setBlock(var0, var6, var2 + 1, var7, var10);
      F8SanctuaryEngine.setBlock(var0, var6, var2 + 4, var7, var12);
      F8SanctuaryEngine.setBlock(var0, var6, var2 + 5, var7, var12);

      // Chapiteau
      for (int var19 = -1; var19 <= 1; var19++) {
         F8SanctuaryEngine.setBlock(var0, var6 + var19, var2 + 6, var7 - 1, var12);
         F8SanctuaryEngine.setBlock(var0, var6 + var19, var2 + 6, var7, var12);
      }

      // LE CREUX : ouverture (air) à hauteur des yeux sur la face avant, et le DISQUE (Sceau) enchâssé
      // au fond -> on voit clairement le disque posé dans un trou creusé dans la roche noire.
      F8SanctuaryEngine.setBlock(var0, var6, var2 + 2, var7, "AIR");
      F8SanctuaryEngine.setBlock(var0, var6, var2 + 3, var7, "AIR");
      F8SanctuaryEngine.setBlock(var0, var6, var2 + 2, var7 - 1, var8);
      F8SanctuaryEngine.setBlock(var0, var6, var2 + 3, var7 - 1, var5 ? var9 : var12);
      if (var5) {
         F8SanctuaryEngine.setBlock(var0, var6, var2 + 1, var7 - 1, var9);
      }
   }

   public static int receptacleSide(Object var0, Object var1) {
      try {
         int[] var2 = F8SanctuaryEngine.target(var0);
         int var3 = F8SanctuaryEngine.posX(var1);
         int var4 = F8SanctuaryEngine.posY(var1);
         int var5 = F8SanctuaryEngine.posZ(var1);

         for (int var9 : new int[]{-1, 1}) {
            int var10 = var2[0] + var9 * 7;
            int var11 = var2[2] + 28;
            if (Math.abs(var3 - var10) <= 2 && var4 >= var2[1] && var4 <= var2[1] + 4 && Math.abs(var5 - var11) <= 2) {
               return var9;
            }
         }
      } catch (Throwable var12) {
      }

      return 0;
   }

   public static void activateVisual(Object var0, int var1) {
      try {
         int[] var2 = F8SanctuaryEngine.target(var0);
         Object var3 = F8SanctuaryEngine.invokeNoArg(var0, "overworld");
         buildReceptacle(var3, var2[0], var2[1], var2[2], var1, true);
      } catch (Throwable var4) {
      }
   }

   private static void decorateOnce(Object var0, int var1, int var2, int var3) throws Exception {
      Object var4 = F8SanctuaryEngine.sanctuaryStoneBlock();
      Object var5 = F8SanctuaryEngine.sanctuaryLumenBlock();
      Object var6 = F8SanctuaryEngine.staticField("net.minecraft.world.level.block.Blocks", "CHISELED_DEEPSLATE");
      Object var7 = F8SanctuaryEngine.staticField("net.minecraft.world.level.block.Blocks", "POLISHED_DEEPSLATE");

      for (int var11 : new int[]{7, 14, 21}) {
         for (int var15 : new int[]{-1, 1}) {
            int var16 = var1 + var15 * 11;

            for (int var17 = 1; var17 <= 5; var17++) {
               F8SanctuaryEngine.setBlock(var0, var16, var2 + var17, var3 + var11, var17 == 3 ? var6 : var4);
            }

            F8SanctuaryEngine.setBlock(var0, var16 - var15, var2 + 3, var3 + var11, var5);
         }
      }

      for (byte var18 = 6; var18 <= 21; var18 += 3) {
         F8SanctuaryEngine.setBlock(var0, var1, var2, var3 + var18, var7);
      }

      for (int var25 : new int[]{0, -7}) {
         for (int var32 : new int[]{-1, 1}) {
            for (int var33 = 1; var33 <= 5; var33++) {
               F8SanctuaryEngine.setBlock(var0, var1 + var32 * 9, var2 + var33, var3 + var25, var4);
            }
         }

         for (int var28 = -8; var28 <= 8; var28++) {
            if (Math.abs(var28) > 3) {
               F8SanctuaryEngine.setBlock(var0, var1 + var28, var2 + 6, var3 + var25, var6);
            }
         }
      }

      for (int var26 : new int[]{-1, 1}) {
         for (int var29 = -21; var29 <= -12; var29++) {
            F8SanctuaryEngine.setBlock(var0, var1 + var26 * 11, var2 + 1, var3 + var29, var4);
         }

         F8SanctuaryEngine.setBlock(var0, var1 + var26 * 10, var2 + 3, var3 - 19, var5);
      }
   }

   public static void runtimeTick(Object var0, int[] var1) {
      if (var0 != null && var1 != null) {
         int var2 = TICKS.merge(var0, 1, Integer::sum);
         if (var2 >= 20) {
            TICKS.put(var0, 0);

            try {
               // 6 Veilleurs : patrouillent une large zone autour de leur poste ; ne quittent pas
               // les abords du Sanctuaire. Le Protecteur reste rivé à la chambre de la Borne.
               String[] var5 = F8SanctuaryEngine.TAG_WATCHERS;
               int[][] var6 = F8SanctuaryEngine.WATCHER_OFFSETS;

               for (int var7 = 0; var7 < var5.length; var7++) {
                  leash(var0, var5[var7], var1[0] + var6[var7][0], var1[1] + 1, var1[2] + var6[var7][1], 30);
               }

               leash(var0, "reivax_f83_fg1", var1[0], var1[1] + 1, var1[2] - 15, 16);
            } catch (Throwable var4) {
            }
         }
      }
   }

   private static void leash(Object var0, String var1, int var2, int var3, int var4, int var5) throws Exception {
      command(
         var0,
         "execute as @e[tag="
            + var1
            + "] unless entity @s[x="
            + var2
            + ",y="
            + var3
            + ",z="
            + var4
            + ",distance=.."
            + var5
            + "] run tp @s "
            + ((double)var2 + 0.5)
            + " "
            + var3
            + " "
            + ((double)var4 + 0.5)
      );
   }

   public static void tuneProtector(Object var0, String var1, boolean var2, boolean var3) {
      try {
         String var4 = "@e[tag=" + var1 + ",limit=1]";
         // Valeurs V1 (brief Veilleur/Protecteur) — doivent rester cohérentes avec
         // F8SanctuaryEngine.summonProtector / activateProtectorGroup.
         // var2 = true -> Protecteur (massif) ; false -> Veilleur (fin, vertical).
         double var5 = var2 ? 100.0 : 40.0;
         double var7 = var2 ? 1.7 : 1.25;
         double var9 = var2 ? 0.19 : 0.26;
         double var11 = var2 ? 8.0 : 5.0;
         double var13 = var2 ? 8.0 : 4.0;
         command(var0, "attribute " + var4 + " minecraft:generic.scale base set " + var7);
         command(var0, "attribute " + var4 + " minecraft:generic.movement_speed base set " + var9);
         command(var0, "attribute " + var4 + " minecraft:generic.max_health base set " + var5);
         command(var0, "attribute " + var4 + " minecraft:generic.attack_damage base set " + var11);
         command(var0, "attribute " + var4 + " minecraft:generic.armor base set " + var13);
         command(var0, "attribute " + var4 + " minecraft:generic.knockback_resistance base set " + (var2 ? 0.9 : 0.35));
         command(var0, "data merge entity " + var4 + " {Health:" + var5 + "f,PersistenceRequired:1b}");
      } catch (Throwable var15) {
         System.err.println("[REIVAX F9 protector tune] " + var15.getMessage());
      }
   }

   public static void reset(Object var0) {
      TICKS.remove(var0);
      if (var0 != null) {
         try {
            Object var1 = F8SanctuaryEngine.campaign(var0);
            Field var2 = findField(var1.getClass(), "done");
            var2.setAccessible(true);
            if (var2.get(var1) instanceof Set var4) {
               var4.remove("F90_LEFT_SEAL");
               var4.remove("F90_RIGHT_SEAL");
            }

            try {
               F8SanctuaryEngine.invokeNoArg(var1, "setDirty");
            } catch (Throwable var6) {
            }
         } catch (Throwable var7) {
         }
      }
   }

   private static int command(Object var0, String var1) throws Exception {
      Method var2 = F8SanctuaryEngine.class.getDeclaredMethod("runCommand", Object.class, String.class);
      var2.setAccessible(true);
      return var2.invoke(null, var0, var1) instanceof Number var4 ? var4.intValue() : 0;
   }

   private static Field findField(Class<?> var0, String var1) throws Exception {
      for (Class var2 = var0; var2 != null; var2 = var2.getSuperclass()) {
         try {
            return var2.getDeclaredField(var1);
         } catch (NoSuchFieldException var4) {
         }
      }

      throw new NoSuchFieldException(var1);
   }
}
