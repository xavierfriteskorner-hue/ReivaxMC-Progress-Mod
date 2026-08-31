package fr.reivaxmc.progress.story;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public final class F85SanctuaryPatch {
   private static final Map<Object, Boolean> DECORATED = Collections.synchronizedMap(new WeakHashMap<>());
   private static final Map<Object, Long> LAST_MOB_GUARD_TICK = Collections.synchronizedMap(new WeakHashMap<>());
   private static final String K_BUILT = "F8_SANCTUARY_BUILT";
   private static final String K_SEAL = "F84_SEAL_INSERTED";
   private static final String K_BEACON = "F8_FOUNDATION_BEACON_RECOVERED";
   private static final String K_BOOK = "F84_BOOK_RECOVERED";
   private static final String K_RELIQUARY = "F84_RELIQUARY_CLAIMED";
   private static final String K_FG1 = "F82_FOUNDATION_GUARD_1_DEFEATED";
   private static final String K_FG2 = "F82_FOUNDATION_GUARD_2_DEFEATED";

   private F85SanctuaryPatch() {
   }

   public static void tick(Object var0, List<?> var1) {
      if (var0 != null && var1 != null && !var1.isEmpty()) {
         try {
            Object var2 = F8SanctuaryEngine.campaign(var0);
            if (!F8SanctuaryEngine.completed(var2, "F8_SANCTUARY_BUILT")) {
               return;
            }

            int[] var3 = F8SanctuaryEngine.target(var0);
            Object var4 = F8SanctuaryEngine.invokeNoArg(var0, "overworld");
            if (var4 == null || var3 == null || var3.length < 3) {
               return;
            }

            if (!Boolean.TRUE.equals(DECORATED.get(var0))) {
               decorate(var4, var3, F8SanctuaryEngine.completed(var2, "F84_SEAL_INSERTED"));
               DECORATED.put(var0, Boolean.TRUE);
            }

            refreshSteleState(var4, var3, F8SanctuaryEngine.completed(var2, "F84_SEAL_INSERTED"));
            long var5 = gameTime(var4);
            Long var7 = LAST_MOB_GUARD_TICK.get(var0);
            if (var7 == null || var5 - var7 >= 10L) {
               LAST_MOB_GUARD_TICK.put(var0, var5);
               enforceProtectorPresence(var0, var3);
            }
         } catch (Throwable var8) {
            System.err.println("[REIVAX F8.5] sanctuary patch tick failed: " + var8.getClass().getSimpleName() + ": " + var8.getMessage());
         }
      }
   }

   public static boolean directBeaconUse(Object var0, Object var1, Object var2) {
      if (var0 != null && var1 != null && var2 != null) {
         try {
            if (safeNoArg(var0, "isClientSide") instanceof Boolean var4 && var4) {
               return true;
            }

            Object var9 = safeNoArg(var2, "getServer");
            if (var9 == null) {
               var9 = safeNoArg(var0, "getServer");
            }

            if (var9 == null) {
               return false;
            }

            Object var5 = F8SanctuaryEngine.campaign(var9);
            if (F8SanctuaryEngine.boolInvoke(var5, "foundationPlaced")) {
               Object var6 = safeNoArg(var5, "foundationPos");
               if (var6 != null && samePos(var6, var1)) {
                  openFoyerPanel(var2, var5, var6);
                  return true;
               }
            }

            if (F8SanctuaryEngine.isSanctuaryBeaconPos(var9, var1) && !F8SanctuaryEngine.completed(var5, "F8_FOUNDATION_BEACON_RECOVERED")) {
               int var10 = (F8SanctuaryEngine.completed(var5, "F82_FOUNDATION_GUARD_1_DEFEATED") ? 1 : 0)
                  + (F8SanctuaryEngine.completed(var5, "F82_FOUNDATION_GUARD_2_DEFEATED") ? 1 : 0);
               if (var10 < 2) {
                  send(var2, "§6BORNE §8• §fLa Borne reste protégée tant que les Gardiens de Fondation sont actifs.");
                  return true;
               }

               if (!F8SanctuaryEngine.completed(var5, "F84_BOOK_RECOVERED")) {
                  send(var2, "§6BORNE §8• §fAvant de partir, examinez le Livre ancien conservé dans la chambre.");
                  return true;
               }

               if (!F8SanctuaryEngine.completed(var5, "F84_RELIQUARY_CLAIMED")) {
                  send(var2, "§6BORNE §8• §fLe Reliquaire vient de se déverrouiller. Examinez-le avant de retirer la Borne.");
                  return true;
               }

               Object var7 = F8SanctuaryEngine.invokeNoArg(var9, "overworld");
               F8SanctuaryEngine.setBlock(var7, F8SanctuaryEngine.posX(var1), F8SanctuaryEngine.posY(var1), F8SanctuaryEngine.posZ(var1), "AIR");
               giveBeacon(var2);
               F8SanctuaryEngine.onBeaconRecovered(var9, var2);
               syncAll(var9, var5);
               return true;
            }
         } catch (Throwable var8) {
            System.err.println("[REIVAX F8.5] direct beacon use failed: " + var8.getClass().getSimpleName() + ": " + var8.getMessage());
         }

         return false;
      } else {
         return false;
      }
   }

   private static void decorate(Object var0, int[] var1, boolean var2) throws Exception {
      int var3 = var1[0];
      int var4 = var1[1];
      int var5 = var1[2];
      Object var6 = F8SanctuaryEngine.sanctuaryStoneBlock();
      Object var7 = F8SanctuaryEngine.sanctuaryLumenBlock();
      int var8 = var3;
      int var9 = var4 + 2;
      int var10 = var5 + 29;

      for (int var11 = -2; var11 <= 2; var11++) {
         for (int var12 = 0; var12 <= 2; var12++) {
            set(var0, var8 + var11, var9 - 2, var10 + var12, (var11 + var12 & 1) == 0 ? var6 : "POLISHED_DEEPSLATE");
         }
      }

      set(var0, var8 - 3, var9 - 2, var10 + 1, "MOSS_BLOCK");
      set(var0, var8 + 3, var9 - 2, var10 + 1, "MOSS_BLOCK");
      set(var0, var8 - 3, var9 - 1, var10 + 1, "FLOWERING_AZALEA");
      set(var0, var8 + 3, var9 - 1, var10 + 1, "AZALEA");
      set(var0, var8, var9 - 1, var10 + 1, "CHISELED_DEEPSLATE");
      set(var0, var8, var9, var10 + 1, var6);
      set(var0, var8, var9 + 1, var10 + 1, var6);
      set(var0, var8, var9 + 2, var10 + 1, "CHISELED_DEEPSLATE");
      set(var0, var8 - 1, var9, var10 + 1, "POLISHED_DEEPSLATE");
      set(var0, var8 + 1, var9, var10 + 1, "POLISHED_DEEPSLATE");
      set(var0, var8 - 1, var9 + 1, var10 + 1, var6);
      set(var0, var8 + 1, var9 + 1, var10 + 1, var6);
      set(var0, var8 - 2, var9 - 1, var10 + 1, "TUFF_BRICKS");
      set(var0, var8 + 2, var9 - 1, var10 + 1, "TUFF_BRICKS");
      set(var0, var8 - 2, var9, var10 + 1, "CHISELED_DEEPSLATE");
      set(var0, var8 + 2, var9, var10 + 1, "CHISELED_DEEPSLATE");
      set(var0, var8 - 1, var9 + 2, var10 + 1, var7);
      set(var0, var8 + 1, var9 + 2, var10 + 1, var7);
      refreshSteleState(var0, var1, var2);
      brokenMarker(var0, var8 - 5, var9 - 2, var10 - 1, var6, false);
      brokenMarker(var0, var8 + 5, var9 - 2, var10 - 1, var6, true);
      int var14 = var5 + 14;

      for (int var15 = -4; var15 <= 4; var15++) {
         if (var15 != 0) {
            set(var0, var3 + var15, var4, var14, Math.abs(var15) == 4 ? var7 : var6);
         }
      }

      for (int var16 = -3; var16 <= 3; var16++) {
         if (var16 != 0) {
            set(var0, var3, var4, var14 + var16, Math.abs(var16) == 3 ? var7 : "POLISHED_DEEPSLATE");
         }
      }

      hallShrine(var0, var3 - 10, var4, var14 - 4, var6, var7, false);
      hallShrine(var0, var3 + 10, var4, var14 - 4, var6, var7, true);
      hallShrine(var0, var3 - 10, var4, var14 + 4, var6, var7, false);
      hallShrine(var0, var3 + 10, var4, var14 + 4, var6, var7, true);
      set(var0, var3 - 10, var4 + 1, var14 + 1, "MOSS_BLOCK");
      set(var0, var3 - 10, var4 + 2, var14 + 1, "MOSS_CARPET");
      set(var0, var3 + 10, var4 + 1, var14 - 1, "MOSS_BLOCK");
      set(var0, var3 + 10, var4 + 2, var14 - 1, "FLOWERING_AZALEA");
      hangingLight(var0, var3 - 9, var4 + 7, var14, var6, var7);
      hangingLight(var0, var3 + 9, var4 + 7, var14, var6, var7);

      for (int var17 = var5 + 3; var17 >= var5 - 5; var17 -= 4) {
         set(var0, var3 - 5, var4 + 2, var17, var6);
         set(var0, var3 + 5, var4 + 2, var17, var6);
         set(var0, var3 - 5, var4 + 3, var17, var7);
         set(var0, var3 + 5, var4 + 3, var17, var7);
         set(var0, var3 - 5, var4 + 4, var17, "CHISELED_DEEPSLATE");
         set(var0, var3 + 5, var4 + 4, var17, "CHISELED_DEEPSLATE");
      }

      int var18 = var5 - 18;

      for (int var13 = -7; var13 <= 7; var13++) {
         if (var13 != -5 && var13 != 5) {
            set(var0, var3 + var13, var4, var18, Math.abs(var13) % 3 == 0 ? var7 : var6);
         }
      }

      for (int var19 = -5; var19 <= 5; var19++) {
         set(var0, var3, var4, var18 + var19, Math.abs(var19) != 5 && var19 != 0 ? "POLISHED_DEEPSLATE" : var7);
      }

      chamberRib(var0, var3 - 11, var4, var18 - 3, var6, var7);
      chamberRib(var0, var3 + 11, var4, var18 - 3, var6, var7);
      chamberRib(var0, var3 - 11, var4, var18 + 5, var6, var7);
      chamberRib(var0, var3 + 11, var4, var18 + 5, var6, var7);
      artifactNiche(var0, var3 + 8, var4, var18, var6, var7, true);
      artifactNiche(var0, var3 - 8, var4, var18, var6, var7, false);
      set(var0, var3 - 1, var4 + 3, var18, "CHISELED_DEEPSLATE");
      set(var0, var3 + 1, var4 + 3, var18, "CHISELED_DEEPSLATE");
      set(var0, var3 - 1, var4 + 4, var18, var7);
      set(var0, var3 + 1, var4 + 4, var18, var7);
      set(var0, var3 - 2, var4 + 2, var18, var6);
      set(var0, var3 + 2, var4 + 2, var18, var6);
      set(var0, var3 - 12, var4 + 1, var18 + 7, "MOSS_BLOCK");
      set(var0, var3 - 12, var4 + 2, var18 + 7, "AZALEA");
      set(var0, var3 + 12, var4 + 1, var18 - 6, "MOSS_BLOCK");
      set(var0, var3 + 12, var4 + 2, var18 - 6, "FLOWERING_AZALEA");
   }

   private static void refreshSteleState(Object var0, int[] var1, boolean var2) throws Exception {
      int var3 = var1[0];
      int var4 = var1[1] + 2;
      int var5 = var1[2] + 29;
      if (var2) {
         set(var0, var3, var4, var5, F8SanctuaryEngine.originMatrixBlock());
         set(var0, var3 - 1, var4, var5, F8SanctuaryEngine.sanctuaryLumenBlock());
         set(var0, var3 + 1, var4, var5, F8SanctuaryEngine.sanctuaryLumenBlock());
         set(var0, var3, var4 + 1, var5, F8SanctuaryEngine.sanctuaryLumenBlock());
      } else {
         set(var0, var3, var4, var5, "CHISELED_DEEPSLATE");
         set(var0, var3 - 1, var4, var5, "POLISHED_DEEPSLATE");
         set(var0, var3 + 1, var4, var5, "POLISHED_DEEPSLATE");
         set(var0, var3, var4 + 1, var5, F8SanctuaryEngine.sanctuaryStoneBlock());
      }
   }

   private static void brokenMarker(Object var0, int var1, int var2, int var3, Object var4, boolean var5) throws Exception {
      set(var0, var1, var2, var3, "TUFF_BRICKS");
      set(var0, var1, var2 + 1, var3, var4);
      set(var0, var1, var2 + 2, var3, "CHISELED_DEEPSLATE");
      if (var5) {
         set(var0, var1 - 1, var2, var3, "MOSS_BLOCK");
         set(var0, var1 - 1, var2 + 1, var3, "MOSS_CARPET");
      } else {
         set(var0, var1 + 1, var2, var3, "MOSS_BLOCK");
         set(var0, var1 + 1, var2 + 1, var3, "MOSS_CARPET");
      }
   }

   private static void hallShrine(Object var0, int var1, int var2, int var3, Object var4, Object var5, boolean var6) throws Exception {
      int var7 = var6 ? -1 : 1;

      for (int var8 = 1; var8 <= 5; var8++) {
         set(var0, var1, var2 + var8, var3, var8 == 3 ? var5 : var4);
      }

      set(var0, var1 + var7, var2 + 1, var3, "CHISELED_DEEPSLATE");
      set(var0, var1 + var7, var2 + 5, var3, "TUFF_BRICKS");
      set(var0, var1, var2 + 6, var3, "POLISHED_DEEPSLATE");
   }

   private static void hangingLight(Object var0, int var1, int var2, int var3, Object var4, Object var5) throws Exception {
      set(var0, var1, var2, var3, var4);
      set(var0, var1, var2 - 1, var3, "CHAIN");
      set(var0, var1, var2 - 2, var3, var5);
   }

   private static void chamberRib(Object var0, int var1, int var2, int var3, Object var4, Object var5) throws Exception {
      for (int var6 = 1; var6 <= 8; var6++) {
         set(var0, var1, var2 + var6, var3, var6 != 4 && var6 != 7 ? var4 : var5);
      }

      set(var0, var1, var2 + 9, var3, "CHISELED_DEEPSLATE");
   }

   private static void artifactNiche(Object var0, int var1, int var2, int var3, Object var4, Object var5, boolean var6) throws Exception {
      int var7 = var6 ? 1 : -1;
      set(var0, var1 + var7, var2 + 1, var3, "POLISHED_DEEPSLATE");
      set(var0, var1 + var7, var2 + 2, var3, var4);
      set(var0, var1 + var7, var2 + 3, var3, var5);
      set(var0, var1 + var7, var2 + 4, var3, var4);
      set(var0, var1, var2 + 4, var3, "CHISELED_DEEPSLATE");
      set(var0, var1, var2 + 5, var3, var5);
   }

   private static void enforceProtectorPresence(Object var0, int[] var1) {
      try {
         command(var0, "attribute @e[tag=reivax_f83_w1,limit=1] minecraft:generic.scale base set 1.95");
         command(var0, "attribute @e[tag=reivax_f83_w2,limit=1] minecraft:generic.scale base set 1.95");
         command(var0, "attribute @e[tag=reivax_f83_fg1,limit=1] minecraft:generic.scale base set 2.25");
         command(var0, "attribute @e[tag=reivax_f83_fg2,limit=1] minecraft:generic.scale base set 2.25");
         command(var0, "attribute @e[tag=reivax_f83_w1,limit=1] minecraft:generic.movement_speed base set 0.31");
         command(var0, "attribute @e[tag=reivax_f83_w2,limit=1] minecraft:generic.movement_speed base set 0.31");
         command(var0, "attribute @e[tag=reivax_f83_fg1,limit=1] minecraft:generic.movement_speed base set 0.27");
         command(var0, "attribute @e[tag=reivax_f83_fg2,limit=1] minecraft:generic.movement_speed base set 0.27");
         int var2 = var1[0];
         int var3 = var1[1];
         int var4 = var1[2];
         leash(var0, "reivax_f83_w1", var2 - 10, var3, var4 + 9, 20, 9, 12, var2 - 5, var3 + 1, var4 + 14);
         leash(var0, "reivax_f83_w2", var2 - 10, var3, var4 + 9, 20, 9, 12, var2 + 5, var3 + 1, var4 + 14);
         leash(var0, "reivax_f83_fg1", var2 - 11, var3, var4 - 22, 22, 12, 13, var2 - 5, var3 + 1, var4 - 15);
         leash(var0, "reivax_f83_fg2", var2 - 11, var3, var4 - 22, 22, 12, 13, var2 + 5, var3 + 1, var4 - 15);
      } catch (Throwable var5) {
         System.err.println("[REIVAX F8.5] protector guard failed: " + var5.getClass().getSimpleName() + ": " + var5.getMessage());
      }
   }

   private static void leash(Object var0, String var1, int var2, int var3, int var4, int var5, int var6, int var7, int var8, int var9, int var10) throws Exception {
      String var11 = "execute as @e[tag="
         + var1
         + ",limit=1] unless entity @s[x="
         + var2
         + ",y="
         + var3
         + ",z="
         + var4
         + ",dx="
         + var5
         + ",dy="
         + var6
         + ",dz="
         + var7
         + "] run tp @s "
         + ((double)var8 + 0.5)
         + " "
         + var9
         + " "
         + ((double)var10 + 0.5);
      command(var0, var11);
   }

   private static void openFoyerPanel(Object var0, Object var1, Object var2) throws Exception {
      String var3 = String.valueOf(safeNoArg(var1, "foundationName"));
      int var4 = F8SanctuaryEngine.number(safeNoArg(var1, "territoryRadius")).intValue();

      Object var5;
      try {
         var5 = F8SanctuaryEngine.fieldValue(var1, "foundationFounder");
      } catch (Throwable var12) {
         var5 = safeNoArg(var1, "foundationFounder");
      }

      String var6 = String.valueOf(var5);

      Object var7;
      try {
         var7 = F8SanctuaryEngine.fieldValue(var1, "foundationDay");
      } catch (Throwable var11) {
         var7 = safeNoArg(var1, "foundationDay");
      }

      int var8 = F8SanctuaryEngine.number(var7).intValue();
      String var9 = F8SanctuaryEngine.posX(var2) + "," + F8SanctuaryEngine.posY(var2) + "," + F8SanctuaryEngine.posZ(var2);
      String var10 = var3 + "|" + var4 + "|" + var6 + "|" + var8 + "|" + var9;
      F7NarrativeEngine.pushUi(var0, "F8_FOYER_PANEL", var10);
   }

   private static void giveBeacon(Object var0) throws Exception {
      Object var1 = F8SanctuaryEngine.foundationBeaconItem();
      Object var2 = F8SanctuaryEngine.invokeNoArg(var1, "getDefaultInstance");
      Object var3 = F8SanctuaryEngine.invokeNoArg(var0, "getInventory");
      if (F8SanctuaryEngine.invoke(var3, "add", var2) instanceof Boolean var5 && !var5) {
         try {
            F8SanctuaryEngine.invoke(var0, "drop", var2, Boolean.FALSE);
         } catch (Throwable var7) {
         }
      }
   }

   private static void syncAll(Object var0, Object var1) {
      try {
         for (Object var3 : F8SanctuaryEngine.players(var0)) {
            try {
               F8SanctuaryEngine.callStatic("fr.reivaxmc.progress.network.ProgressNetworking", "sync", var3, var1);
            } catch (Throwable var5) {
            }
         }
      } catch (Throwable var6) {
      }
   }

   private static void send(Object var0, String var1) {
      try {
         Object var2 = F8SanctuaryEngine.callStatic("net.minecraft.network.chat.Component", "literal", var1);
         F8SanctuaryEngine.invoke(var0, "sendSystemMessage", var2);
      } catch (Throwable var5) {
         try {
            F7NarrativeEngine.routeStoryMessage(var0, var1, false);
         } catch (Throwable var4) {
         }
      }
   }

   private static boolean samePos(Object var0, Object var1) throws Exception {
      return F8SanctuaryEngine.posX(var0) == F8SanctuaryEngine.posX(var1)
         && F8SanctuaryEngine.posY(var0) == F8SanctuaryEngine.posY(var1)
         && F8SanctuaryEngine.posZ(var0) == F8SanctuaryEngine.posZ(var1);
   }

   private static long gameTime(Object var0) {
      try {
         return F8SanctuaryEngine.number(F8SanctuaryEngine.invokeNoArg(var0, "getGameTime")).longValue();
      } catch (Throwable var2) {
         return System.nanoTime() / 50000000L;
      }
   }

   private static Object safeNoArg(Object var0, String var1) {
      try {
         return F8SanctuaryEngine.invokeNoArg(var0, var1);
      } catch (Throwable var3) {
         return null;
      }
   }

   private static void set(Object var0, int var1, int var2, int var3, Object var4) throws Exception {
      if (var4 instanceof String var5) {
         F8SanctuaryEngine.setBlock(var0, var1, var2, var3, var5);
      } else {
         F8SanctuaryEngine.setBlock(var0, var1, var2, var3, var4);
      }
   }

   private static int command(Object var0, String var1) throws Exception {
      Method var2 = F8SanctuaryEngine.class.getDeclaredMethod("runCommand", Object.class, String.class);
      var2.setAccessible(true);
      return var2.invoke(null, var0, var1) instanceof Number var4 ? var4.intValue() : 0;
   }
}
