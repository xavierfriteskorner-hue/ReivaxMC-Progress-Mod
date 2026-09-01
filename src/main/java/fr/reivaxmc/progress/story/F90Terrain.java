package fr.reivaxmc.progress.story;

import java.util.ArrayList;
import java.util.Collections;

public final class F90Terrain {
   private static final int MIN_DISTANCE = 2000;
   private static final int MAX_DISTANCE = 4000;
   private static final int SAFE_MIN = 2200;
   private static final int SAFE_MAX = 3800;
   private static final double GOLDEN_ANGLE = 2.399963229728653;

   private F90Terrain() {
   }

   public static int[] findTarget(Object var0, int var1, int var2) {
      try {
         Object var3 = F8SanctuaryEngine.invokeNoArg(var0, "overworld");
         if (var3 == null) {
            throw new IllegalStateException("Monde principal indisponible");
         } else {
            long var4 = worldSeed(var3) ^ (long)var1 << 32 ^ (long)var2 & 4294967295L ^ 4179255021L;
            double var6 = unit(var4 ^ -7046029254386353131L) * Math.PI * 2.0;
            F90Terrain.Site var8 = null;

            for (int var9 = 0; var9 < 28; var9++) {
               long var10 = mix(var4 + (long)(var9 + 1) * -7046029254386353131L);
               int var12 = 2200 + (int)Math.floorMod(var10, 1601L);
               double var13 = var6 + (double)var9 * 2.399963229728653 + (unit(var10 ^ -3335678366873096957L) - 0.5) * 0.28;
               int var15 = var1 + (int)Math.round(Math.cos(var13) * (double)var12);
               int var16 = var2 + (int)Math.round(Math.sin(var13) * (double)var12);
               F90Terrain.Site var17 = evaluateNaturalEntrance(var3, var1, var2, var15, var16);
               if (var17 != null) {
                  if (var8 == null || var17.score < var8.score) {
                     var8 = var17;
                  }

                  if (var17.roughness <= 6) {
                     prepareNaturalEntrance(var3, var17);
                     return var17.target();
                  }
               }
            }

            if (var8 != null) {
               prepareNaturalEntrance(var3, var8);
               return var8.target();
            } else {
               int var22 = 3000 + (int)Math.floorMod(mix(var4 ^ -6752110988234923001L), 601L) - 300;
               double var23 = var6 + 0.731;
               int var24 = var1 + (int)Math.round(Math.cos(var23) * (double)var22);
               int var25 = var2 + (int)Math.round(Math.sin(var23) * (double)var22);
               forceChunk(var3, var24, var25);
               int var14 = seaLevel(var3);
               int var26 = height(var3, "OCEAN_FLOOR", var24, var25) - 1;
               int var27 = height(var3, "WORLD_SURFACE", var24, var25) - 1;
               boolean var28 = isWet(var3, var24, var27, var25);
               int var18 = var28 ? Math.max(var14 + 1, var26 + 2) : groundY(var3, var24, var25);
               var18 = Math.max(var18, var14 + (var28 ? 1 : -32));
               F90Terrain.Site var19 = new F90Terrain.Site(var24, var18, var25, var22, 99999, 0, true);
               if (var28) {
                  prepareRockPromontory(var3, var19);
               } else {
                  prepareNaturalEntrance(var3, var19);
               }

               return var19.target();
            }
         }
      } catch (RuntimeException var20) {
         throw var20;
      } catch (Throwable var21) {
         throw new IllegalStateException("Placement du Sanctuaire impossible: " + var21.getClass().getSimpleName() + ": " + var21.getMessage(), var21);
      }
   }

   private static F90Terrain.Site evaluateNaturalEntrance(Object var0, int var1, int var2, int var3, int var4) throws Exception {
      int var5 = (int)Math.round(Math.hypot((double)var3 - (double)var1, (double)var4 - (double)var2));
      if (var5 >= 2000 && var5 <= 4000) {
         int[][] var6 = new int[][]{{0, 0}, {-4, 0}, {4, 0}, {0, 4}, {0, -4}};
         ArrayList<Integer> var7 = new ArrayList<>();

         for (int[] var11 : var6) {
            int var12 = var3 + var11[0];
            int var13 = var4 + var11[1];
            forceChunk(var0, var12, var13);
            int var14 = height(var0, "WORLD_SURFACE", var12, var13) - 1;
            if (var14 < 24 || isWet(var0, var12, var14, var13)) {
               return null;
            }

            int var15 = groundY(var0, var12, var13);
            if (var15 < 24) {
               return null;
            }

            var7.add(var15);
         }

         int var16 = Collections.min(var7);
         int var17 = Collections.max(var7);
         int var18 = var17 - var16;
         // Dénivelé au seuil : on autorise désormais un terrain accidenté (jusqu'à 30) pour permettre
         // au Sanctuaire de s'ENCASTRER dans une montagne. L'entrée reste taillée accessible par
         // prepareNaturalEntrance (parvis + tunnel d'accès dégagé). Au-delà = falaise, on rejette.
         if (var18 > 30) {
            return null;
         } else {
            Collections.sort(var7);
            int var19 = (Integer)var7.get(var7.size() / 2);
            int var20 = var18 * 120 + Math.abs(var5 - 3000) / 3;
            return new F90Terrain.Site(var3, var19, var4, var5, var20, var18, false);
         }
      } else {
         return null;
      }
   }

   private static boolean isWet(Object var0, int var1, int var2, int var3) throws Exception {
      if (var2 < 0) {
         return true;
      } else {
         Object var4 = F8SanctuaryEngine.invoke(var0, "getBlockState", F8SanctuaryEngine.blockPos(var1, var2, var3));
         Object var5 = F8SanctuaryEngine.invokeNoArg(var4, "getFluidState");
         if (F8SanctuaryEngine.invokeNoArg(var5, "isEmpty") instanceof Boolean var7 && var7) {
            return false;
         }

         return true;
      }
   }

   private static int groundY(Object var0, int var1, int var2) throws Exception {
      forceChunk(var0, var1, var2);
      return height(var0, "MOTION_BLOCKING_NO_LEAVES", var1, var2) - 1;
   }

   private static int height(Object var0, String var1, int var2, int var3) throws Exception {
      Class var4 = Class.forName("net.minecraft.world.level.levelgen.Heightmap$Types");
      Object var5 = var4.getField(var1).get(null);
      return F8SanctuaryEngine.number(F8SanctuaryEngine.invoke(var0, "getHeight", var5, var2, var3)).intValue();
   }

   private static void forceChunk(Object var0, int var1, int var2) throws Exception {
      F8SanctuaryEngine.invoke(var0, "getChunk", Math.floorDiv(var1, 16), Math.floorDiv(var2, 16));
   }

   private static int seaLevel(Object var0) {
      try {
         return F8SanctuaryEngine.number(F8SanctuaryEngine.invokeNoArg(var0, "getSeaLevel")).intValue();
      } catch (Throwable var2) {
         return 63;
      }
   }

   private static void prepareNaturalEntrance(Object var0, F90Terrain.Site var1) throws Exception {
      int var2 = var1.entranceX;
      int var3 = var1.entranceZ;
      int var4 = var1.floorY;

      // Parvis du seuil : dalle stable, comblée sur 4 blocs en dessous (évite le parvis flottant en
      // terrain accidenté / montagne) et dégagée sur 6 blocs au-dessus.
      for (int var5 = -5; var5 <= 5; var5++) {
         for (int var6 = -3; var6 <= 7; var6++) {
            int var7 = var2 + var5;
            int var8 = var3 + var6;
            F8SanctuaryEngine.setBlock(var0, var7, var4, var8, (Math.abs(var5) + Math.abs(var6)) % 5 == 0 ? "CHISELED_DEEPSLATE" : "POLISHED_DEEPSLATE");

            for (int var9 = 1; var9 <= 4; var9++) {
               F8SanctuaryEngine.setBlock(var0, var7, var4 - var9, var8, "COBBLED_DEEPSLATE");
            }

            for (int var10 = 1; var10 <= 6; var10++) {
               F8SanctuaryEngine.setBlock(var0, var7, var4 + var10, var8, "AIR");
            }
         }
      }

      // Couloir d'accès taillé vers l'extérieur (+z, côté approche des joueurs) : garantit une entrée
      // PRATICABLE même quand le corps du Sanctuaire est encastré dans une montagne — sol + murs +
      // plafond dégagé sur 5 blocs, comme un tunnel percé dans le relief.
      for (int var11 = 8; var11 <= 18; var11++) {
         int var12 = var3 + var11;

         for (int var13 = -3; var13 <= 3; var13++) {
            int var14 = var2 + var13;
            F8SanctuaryEngine.setBlock(var0, var14, var4, var12, Math.abs(var13) == 3 ? "COBBLED_DEEPSLATE" : "POLISHED_DEEPSLATE");
            F8SanctuaryEngine.setBlock(var0, var14, var4 - 1, var12, "COBBLED_DEEPSLATE");

            for (int var15 = 1; var15 <= 5; var15++) {
               F8SanctuaryEngine.setBlock(var0, var14, var4 + var15, var12, "AIR");
            }
         }
      }
   }

   private static void prepareRockPromontory(Object var0, F90Terrain.Site var1) throws Exception {
      int var2 = var1.entranceX;
      int var3 = var1.entranceZ;
      int var4 = var1.floorY;

      for (int var5 = -10; var5 <= 10; var5++) {
         for (int var6 = -7; var6 <= 13; var6++) {
            double var7 = (double)var5 / 10.5;
            double var9 = ((double)var6 - 2.0) / 11.5;
            if (!(var7 * var7 + var9 * var9 > 1.0)) {
               int var11 = var2 + var5;
               int var12 = var3 + var6;
               forceChunk(var0, var11, var12);
               int var13 = Math.max(1, height(var0, "OCEAN_FLOOR", var11, var12) - 1);
               int var14 = Math.max(var13, var4 - 48);

               for (int var15 = var14; var15 < var4; var15++) {
                  String var16 = var15 > var4 - 4 ? "DEEPSLATE_BRICKS" : "COBBLED_DEEPSLATE";
                  F8SanctuaryEngine.setBlock(var0, var11, var15, var12, var16);
               }

               F8SanctuaryEngine.setBlock(var0, var11, var4, var12, (var5 * 31 + var6 * 17 & 7) == 0 ? "CHISELED_DEEPSLATE" : "POLISHED_DEEPSLATE");
               if (Math.abs(var5) <= 6 && var6 >= -3 && var6 <= 9) {
                  for (int var17 = 1; var17 <= 6; var17++) {
                     F8SanctuaryEngine.setBlock(var0, var11, var4 + var17, var12, "AIR");
                  }
               }
            }
         }
      }
   }

   private static long worldSeed(Object var0) {
      try {
         return F8SanctuaryEngine.number(F8SanctuaryEngine.invokeNoArg(var0, "getSeed")).longValue();
      } catch (Throwable var2) {
         return (long)System.identityHashCode(var0) * -3335678366873096957L;
      }
   }

   private static long mix(long var0) {
      var0 = (var0 ^ var0 >>> 30) * -4658895280553007687L;
      var0 = (var0 ^ var0 >>> 27) * -7723592293110705685L;
      return var0 ^ var0 >>> 31;
   }

   private static double unit(long var0) {
      return (double)(mix(var0) >>> 11) * 1.110223E-16F;
   }

   private static final class Site {
      final int entranceX;
      final int floorY;
      final int entranceZ;
      final int distance;
      final int score;
      final int roughness;
      final boolean artificial;

      Site(int var1, int var2, int var3, int var4, int var5, int var6, boolean var7) {
         this.entranceX = var1;
         this.floorY = var2;
         this.entranceZ = var3;
         this.distance = var4;
         this.score = var5;
         this.roughness = var6;
         this.artificial = var7;
      }

      int[] target() {
         return new int[]{this.entranceX, this.floorY, this.entranceZ - 31};
      }
   }
}
