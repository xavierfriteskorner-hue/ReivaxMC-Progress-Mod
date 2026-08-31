package fr.reivaxmc.progress.story;

import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap.Types;

public final class TracePlacementCompat18F {
   private static final int[][] SPIRE = new int[][]{
      {0, 0, 0},
      {1, 0, 0},
      {-1, 0, 0},
      {0, 0, 1},
      {0, 0, -1},
      {1, 1, 0},
      {-1, 1, 0},
      {0, 1, 0},
      {0, 1, 1},
      {0, 2, 0},
      {-1, 2, 0},
      {0, 2, -1},
      {0, 3, 0},
      {1, 3, 0},
      {0, 4, 0},
      {-1, 4, 0},
      {0, 5, 0},
      {1, 5, 0},
      {0, 6, 0}
   };

   private TracePlacementCompat18F() {
   }

   public static int[] findLandTarget(ServerLevel var0, ServerPlayer var1) {
      double var2 = Math.toRadians((double)var1.getYRot());
      int var4 = (int)Math.floor(var1.getX());
      int var5 = (int)Math.floor(var1.getZ());
      int var6 = (int)Math.floor(var1.getY());
      int var7 = (int)Math.floor(var1.getX() - Math.sin(var2) * 10.0);
      int var8 = (int)Math.floor(var1.getZ() + Math.cos(var2) * 10.0);
      int var9 = Integer.MIN_VALUE;
      int var10 = var6;
      int var11 = Integer.MIN_VALUE;
      double var12 = Double.MAX_VALUE;

      for (int var14 = -18; var14 <= 18; var14++) {
         for (int var15 = -18; var15 <= 18; var15++) {
            int var16 = var7 + var14;
            int var17 = var8 + var15;
            double var18 = sq((double)var16 + 0.5 - var1.getX()) + sq((double)var17 + 0.5 - var1.getZ());
            if (!(var18 < 36.0) && !(var18 > 900.0)) {
               int var20;
               try {
                  var20 = var0.getHeight(Types.MOTION_BLOCKING_NO_LEAVES, var16, var17);
               } catch (Throwable var35) {
                  continue;
               }

               if (isGoodLand(var0, var16, var20, var17)) {
                  double var21 = (double)(var14 * var14 + var15 * var15) + (double)Math.abs(var20 - var6) * 2.5;
                  double var23 = -Math.sin(var2);
                  double var25 = Math.cos(var2);
                  double var27 = (double)var16 + 0.5 - var1.getX();
                  double var29 = (double)var17 + 0.5 - var1.getZ();
                  double var31 = var23 * var27 + var25 * var29;
                  if (var31 < 2.0) {
                     var21 += 80.0;
                  }

                  if (var21 < var12) {
                     var12 = var21;
                     var9 = var16;
                     var10 = var20;
                     var11 = var17;
                  }
               }
            }
         }
      }

      if (var9 != Integer.MIN_VALUE) {
         return new int[]{var9, var10, var11};
      } else {
         for (int var36 = 6; var36 <= 30; var36++) {
            for (byte var38 = 0; var38 < 360; var38 += 15) {
               double var39 = Math.toRadians((double)var38);
               int var40 = var4 + (int)Math.round(Math.cos(var39) * (double)var36);
               int var19 = var5 + (int)Math.round(Math.sin(var39) * (double)var36);

               int var41;
               try {
                  var41 = var0.getHeight(Types.MOTION_BLOCKING_NO_LEAVES, var40, var19);
               } catch (Throwable var34) {
                  continue;
               }

               if (isGoodLand(var0, var40, var41, var19)) {
                  return new int[]{var40, var41, var19};
               }
            }
         }

         int var37 = var6;

         try {
            var37 = var0.getHeight(Types.MOTION_BLOCKING_NO_LEAVES, var7, var8);
         } catch (Throwable var33) {
         }

         return new int[]{var7, var37, var8};
      }
   }

   private static boolean isGoodLand(ServerLevel var0, int var1, int var2, int var3) {
      int[][] var4 = new int[][]{{0, 0}, {1, 0}, {-1, 0}, {0, 1}, {0, -1}, {1, 1}, {1, -1}, {-1, 1}, {-1, -1}};

      for (int[] var8 : var4) {
         int var9 = var1 + var8[0];
         int var10 = var3 + var8[1];

         int var11;
         try {
            var11 = var0.getHeight(Types.MOTION_BLOCKING_NO_LEAVES, var9, var10);
         } catch (Throwable var15) {
            return false;
         }

         if (Math.abs(var11 - var2) > 2) {
            return false;
         }

         BlockPos var12 = new BlockPos(var9, var11 - 1, var10);
         BlockPos var13 = new BlockPos(var9, var11, var10);

         try {
            if (!var0.getFluidState(var12).isEmpty() || !var0.getFluidState(var13).isEmpty()) {
               return false;
            }

            if (!var0.getBlockState(var12).isSolid()) {
               return false;
            }
         } catch (Throwable var17) {
            return false;
         }
      }

      for (int var18 = 0; var18 <= 8; var18++) {
         BlockPos var19 = new BlockPos(var1, var2 + var18, var3);

         try {
            if (!var0.getFluidState(var19).isEmpty()) {
               return false;
            }

            if (var0.getBlockState(var19).isSolid()) {
               return false;
            }
         } catch (Throwable var16) {
            return false;
         }
      }

      return true;
   }

   public static void buildGiantTrace(ServerLevel var0, BlockPos var1, Block var2) {
      if (var0 != null && var1 != null && var2 != null) {
         BlockState var3 = var2.defaultBlockState();

         for (int[] var7 : SPIRE) {
            try {
               var0.setBlock(var1.offset(var7[0], var7[1], var7[2]), var3, 3);
            } catch (Throwable var9) {
            }
         }
      }
   }

   public static void ensureGiantTrace(MinecraftServer var0, StoryStartStateData18F.Snapshot var1, Block var2) {
      if (var0 != null && var1 != null && var2 != null && var1.started() && var1.tracePlaced()) {
         ServerLevel var3;
         try {
            var3 = var0.overworld();
         } catch (Throwable var7) {
            return;
         }

         BlockPos var4 = new BlockPos(var1.traceX(), var1.traceY(), var1.traceZ());

         try {
            BlockPos var5 = var4.offset(0, 6, 0);
            if (var3.getBlockState(var5).getBlock() == var2) {
               return;
            }
         } catch (Throwable var6) {
         }

         buildGiantTrace(var3, var4, var2);
      }
   }

   private static double sq(double var0) {
      return var0 * var0;
   }
}
