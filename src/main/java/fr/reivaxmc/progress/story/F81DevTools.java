package fr.reivaxmc.progress.story;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

public final class F81DevTools {
   private static final Map<Object, F81DevTools.DevState> STATES = Collections.synchronizedMap(new WeakHashMap<>());
   private static final String[] F8_KEYS = new String[]{
      "F8_SANCTUARY_QUEST_STARTED",
      "F8_SANCTUARY_INTRO_VOICE",
      "F8_SANCTUARY_BUILT",
      "F8_SANCTUARY_DISCOVERED",
      "F8_GUARDS_AWAKENED",
      "F8_GUARD_1_DEFEATED",
      "F8_GUARD_2_DEFEATED",
      "F8_GUARD_3_DEFEATED",
      "F8_GUARD_4_DEFEATED",
      "F8_GUARD_5_DEFEATED",
      "F8_GUARD_6_DEFEATED",
      "F8_GUARDS_CLEARED",
      "F84_ROUTE_ECHO_1",
      "F84_ROUTE_ECHO_2",
      "F84_SEAL_INSERTED",
      "F84_BOOK_RECOVERED",
      "F84_RELIQUARY_CLAIMED",
      "F8_VOICE_FOUNDATION_1",
      "F8_VOICE_FOUNDATION_2",
      "F8_FOUNDATION_BEACON_RECOVERED",
      "F8_FIRST_FOYER_ESTABLISHED"
   };

   private F81DevTools() {
   }

   public static void onRegisterCommands(Object var0) {
      if (var0 != null) {
         try {
            Object var1 = invokeNoArg(var0, "getDispatcher");
            Object var2 = literal("reivax");
            Object var3 = literal("dev");
            executes(var3, F81DevTools::cmdHelp);
            then(var3, literalExec("on", F81DevTools::cmdOn));
            then(var3, literalExec("off", F81DevTools::cmdOff));
            then(var3, literalExec("status", F81DevTools::cmdStatus));
            then(var3, literalExec("reset", F81DevTools::cmdReset));
            Object var4 = literal("qa");
            then(var4, literalExec("on", var0x -> cmdQa(var0x, true)));
            then(var4, literalExec("off", var0x -> cmdQa(var0x, false)));
            then(var3, var4);
            Object var5 = literal("goto");
            then(var5, literalExec("trace", var0x -> cmdGoto(var0x, "trace")));
            then(var5, literalExec("night", var0x -> cmdGoto(var0x, "night")));
            then(var5, literalExec("sanctuary", var0x -> cmdGoto(var0x, "sanctuary")));
            then(var5, literalExec("foundation", var0x -> cmdGoto(var0x, "foundation")));
            then(var3, var5);
            then(var2, var3);
            invoke(var1, "register", var2);
            System.out.println("[REIVAX Alpha 18F.9.1.0] DEV/QA commands registered.");
         } catch (Throwable var6) {
            System.err.println("[REIVAX Alpha 18F.8.4.1] command registration failed: " + var6.getClass().getSimpleName() + ": " + var6.getMessage());
         }
      }
   }

   public static void onLogin(Object var0) {
      if (var0 != null) {
         try {
            Object var1 = invokeNoArg(var0, "getEntity");
            Object var2 = server(var1);
            if (var2 == null) {
               return;
            }

            F81DevTools.DevState var3 = STATES.get(var2);
            if (var3 != null && var3.enabled) {
               sendQa(var1, var3.qaAntiSpoil);
            }
         } catch (Throwable var4) {
         }
      }
   }

   public static boolean isDevEnabled(Object var0) {
      F81DevTools.DevState var1 = STATES.get(var0);
      return var1 != null && var1.enabled;
   }

   public static boolean isQaAntiSpoil(Object var0) {
      F81DevTools.DevState var1 = STATES.get(var0);
      return var1 != null && var1.enabled && var1.qaAntiSpoil;
   }

   private static int cmdHelp(Object var0) {
      Object var1 = player(var0);
      if (!allowed(var0)) {
         return denied(var1);
      } else {
         msg(var1, "§6REIVAX DEV §8• §f/reivax dev on|off · status · qa on|off · goto trace|night|sanctuary|foundation · reset");
         return 1;
      }
   }

   private static int cmdOn(Object var0) {
      Object var1 = player(var0);
      if (!allowed(var0)) {
         return denied(var1);
      } else {
         Object var2 = serverFromContext(var0);
         if (var2 == null) {
            return 0;
         } else {
            F81DevTools.DevState var3 = STATES.computeIfAbsent(var2, var0x -> new F81DevTools.DevState());
            var3.enabled = true;
            var3.qaAntiSpoil = true;
            sendQaAll(var2, true);
            msg(var1, "§6REIVAX DEV §8• §aACTIVÉ §7· QA anti-spoil activé par défaut.");
            msg(var1, "§8Utilisez §f/reivax dev goto sanctuary §8ou §f/reivax dev goto foundation §8pour tester sans tout recommencer.");
            return 1;
         }
      }
   }

   private static int cmdOff(Object var0) {
      Object var1 = player(var0);
      if (!allowed(var0)) {
         return denied(var1);
      } else {
         Object var2 = serverFromContext(var0);
         if (var2 == null) {
            return 0;
         } else {
            F81DevTools.DevState var3 = STATES.computeIfAbsent(var2, var0x -> new F81DevTools.DevState());
            var3.enabled = false;
            var3.qaAntiSpoil = false;
            sendQaAll(var2, false);
            msg(var1, "§6REIVAX DEV §8• §fDÉSACTIVÉ · expérience joueur normale restaurée.");
            return 1;
         }
      }
   }

   private static int cmdQa(Object var0, boolean var1) {
      Object var2 = player(var0);
      if (!allowed(var0)) {
         return denied(var2);
      } else {
         Object var3 = serverFromContext(var0);
         if (var3 == null) {
            return 0;
         } else {
            F81DevTools.DevState var4 = STATES.computeIfAbsent(var3, var0x -> new F81DevTools.DevState());
            if (!var4.enabled) {
               msg(var2, "§cActivez d'abord /reivax dev on.");
               return 0;
            } else {
               var4.qaAntiSpoil = var1;
               sendQaAll(var3, var1);
               msg(
                  var2,
                  var1
                     ? "§6QA ANTI-SPOIL §8• §aACTIVÉ §7· dialogues, historique, Chronologie et Archives sont masqués côté test."
                     : "§6QA ANTI-SPOIL §8• §eDÉSACTIVÉ §7· le vrai contenu narratif est visible."
               );
               return 1;
            }
         }
      }
   }

   private static int cmdStatus(Object var0) {
      Object var1 = player(var0);
      if (!allowed(var0)) {
         return denied(var1);
      } else {
         Object var2 = serverFromContext(var0);
         if (var2 == null) {
            return 0;
         } else {
            F81DevTools.DevState var3 = STATES.computeIfAbsent(var2, var0x -> new F81DevTools.DevState());

            try {
               Object var4 = callStatic("fr.reivaxmc.progress.story.StoryModeGate18F", "state", var2);
               Object var5 = invokeNoArg(var4, "snapshot");
               Object var6 = F8SanctuaryEngine.campaign(var2);
               int var7 = 0;

               for (String var11 : F8_KEYS) {
                  if (F8SanctuaryEngine.completed(var6, var11)) {
                     var7++;
                  }
               }

               int var13 = F8SanctuaryEngine.players(var2).size();
               msg(
                  var1,
                  "§6REIVAX DEV STATUS §8• §fDEV " + onOff(var3.enabled) + " §8· §fQA " + onOff(var3.enabled && var3.qaAntiSpoil) + " §8· §fJoueurs " + var13
               );
               msg(
                  var1,
                  "§7Histoire: started="
                     + bool(var5, "started")
                     + " · trace="
                     + bool(var5, "tracePlaced")
                     + " · examinée="
                     + bool(var5, "traceExamined")
                     + " · F8="
                     + var7
                     + "/"
                     + F8_KEYS.length
                     + " · foyer="
                     + boolInvoke(var6, "foundationPlaced")
               );
               return 1;
            } catch (Throwable var12) {
               msg(var1, "§cImpossible de lire l'état DEV: " + var12.getClass().getSimpleName());
               return 0;
            }
         }
      }
   }

   private static int cmdGoto(Object var0, String var1) {
      Object var2 = player(var0);
      if (!allowed(var0)) {
         return denied(var2);
      } else {
         Object var3 = serverFromContext(var0);
         if (var3 != null && var2 != null) {
            F81DevTools.DevState var4 = STATES.computeIfAbsent(var3, var0x -> new F81DevTools.DevState());
            if (!var4.enabled) {
               msg(var2, "§cActivez d'abord /reivax dev on.");
               return 0;
            } else {
               try {
                  prepareCheckpoint(var3, var2);
                  switch (var1) {
                     case "trace":
                        gotoTrace(var3, var2);
                        break;
                     case "night":
                        gotoNight(var3, var2);
                        break;
                     case "sanctuary":
                        gotoSanctuary(var3, var2);
                        break;
                     case "foundation":
                        gotoFoundation(var3, var2);
                        break;
                     default:
                        return 0;
                  }

                  syncStoryAndCampaign(var3);
                  sendQaAll(var3, var4.qaAntiSpoil);
                  msg(var2, "§6CHECKPOINT DEV §8• §a" + var1.toUpperCase() + " chargé. §7Le monde de test est prêt.");
                  return 1;
               } catch (Throwable var7) {
                  System.err.println("[REIVAX Alpha 18F.8.4.1] goto " + var1 + " failed: " + var7);
                  msg(var2, "§cCheckpoint impossible: " + var7.getClass().getSimpleName() + " · " + var7.getMessage());
                  return 0;
               }
            }
         } else {
            return 0;
         }
      }
   }

   private static int cmdReset(Object var0) {
      Object var1 = player(var0);
      if (!allowed(var0)) {
         return denied(var1);
      } else {
         Object var2 = serverFromContext(var0);
         if (var2 != null && var1 != null) {
            F81DevTools.DevState var3 = STATES.computeIfAbsent(var2, var0x -> new F81DevTools.DevState());
            if (!var3.enabled) {
               msg(var1, "§cActivez d'abord /reivax dev on.");
               return 0;
            } else {
               try {
                  prepareCheckpoint(var2, var1);
                  resetStoryToLauncher(var2);
                  runCommand(var2, "time set day");

                  for (Object var5 : F8SanctuaryEngine.players(var2)) {
                     F7NarrativeEngine.pushUi(var5, "F71_OBJECTIVE", "");
                     F7NarrativeEngine.pushUi(var5, "F8_GUIDANCE", "");
                     F7NarrativeEngine.pushUi(var5, "F8_MILESTONE", "");
                  }

                  syncStoryAndCampaign(var2);
                  sendQaAll(var2, var3.qaAntiSpoil);
                  msg(var1, "§6REIVAX DEV §8• §fÉtat narratif de test réinitialisé · retour au lanceur de l'histoire.");
                  return 1;
               } catch (Throwable var6) {
                  System.err.println("[REIVAX Alpha 18F.8.4.1] reset failed: " + var6);
                  msg(var1, "§cReset DEV impossible: " + var6.getClass().getSimpleName());
                  return 0;
               }
            }
         } else {
            return 0;
         }
      }
   }

   private static void gotoTrace(Object var0, Object var1) throws Exception {
      int[] var2 = forceStoryCheckpoint(var0, var1, false, true);
      runCommand(var0, "time set day");
      F7NarrativeEngine.routeStoryMessage(
         var1, "§6OBJECTIF PRINCIPAL §8• §fExaminez la Trace inconnue · approchez-vous puis faites CLIC DROIT pour interagir.", true
      );
      msg(var1, "§8[DEV] Trace placée à proximité · interaction non effectuée.");
   }

   private static void gotoNight(Object var0, Object var1) throws Exception {
      forceStoryCheckpoint(var0, var1, true, true);
      giveItem(var1, "ORIGIN_SEAL");
      setAge1Resonance(var0, false);
      runCommand(var0, "time set 11950");
      F7NarrativeEngine.routeStoryMessage(var1, "§6OBJECTIF PRINCIPAL §8• §fPréparez-vous avant la nuit · la première Résonance va pouvoir être testée.", true);
   }

   private static void gotoSanctuary(Object var0, Object var1) throws Exception {
      forceStoryCheckpoint(var0, var1, true, true);
      giveItem(var1, "ORIGIN_SEAL");
      F90SealGate.giveDevSecondSeal(var1);
      setAge1Resonance(var0, true);
      runCommand(var0, "time set day");
      int[] var2 = F8SanctuaryEngine.target(var0);
      String var3 = F8SanctuaryEngine.playerName(var1);
      // Téléport DEV volontairement désactivé (test du trajet réel). Chaîne conservée mais inutilisée.
      String unusedDevTeleportCmd = "tp " + var3 + " " + var2[0] + " " + (var2[1] + 3) + " " + (var2[2] + 62);
      F7NarrativeEngine.routeStoryMessage(
         var1, "§6OBJECTIF PRINCIPAL §8· §fSuivez la Résonance jusqu'au Sanctuaire. La commande DEV ne vous téléporte plus : testez le trajet réel.", true
      );
   }

   private static void gotoFoundation(Object var0, Object var1) throws Exception {
      forceStoryCheckpoint(var0, var1, true, true);
      setAge1Resonance(var0, true);
      runCommand(var0, "time set day");
      Object var2 = F8SanctuaryEngine.campaign(var0);
      String[] var3 = new String[]{
         "F8_SANCTUARY_QUEST_STARTED",
         "F8_SANCTUARY_INTRO_VOICE",
         "F8_SANCTUARY_BUILT",
         "F8_SANCTUARY_DISCOVERED",
         "F8_GUARDS_AWAKENED",
         "F8_GUARD_1_DEFEATED",
         "F8_GUARD_2_DEFEATED",
         "F8_GUARD_3_DEFEATED",
         "F8_GUARD_4_DEFEATED",
         "F8_GUARD_5_DEFEATED",
         "F8_GUARD_6_DEFEATED",
         "F8_GUARDS_CLEARED",
         "F84_ROUTE_ECHO_1",
         "F84_ROUTE_ECHO_2",
         "F84_SEAL_INSERTED",
         "F84_BOOK_RECOVERED",
         "F84_RELIQUARY_CLAIMED",
         "F8_VOICE_FOUNDATION_1",
         "F8_VOICE_FOUNDATION_2",
         "F8_FOUNDATION_BEACON_RECOVERED"
      };

      for (String var7 : var3) {
         F8SanctuaryEngine.complete(var2, var7);
      }

      giveItem(var1, "FOUNDATION_BEACON_ITEM");
      F7NarrativeEngine.routeStoryMessage(
         var1, "§6OBJECTIF PRINCIPAL §8• §fÉtablissez votre premier Foyer · choisissez un lieu durable puis PLACEZ la Borne de Fondation.", true
      );
   }

   private static void prepareCheckpoint(Object var0, Object var1) throws Exception {
      removeOldFoundationBlock(var0);
      clearOldTrace(var0);
      killGuardians(var0);
      removeTestItems(var0);
      clearReadyGate(var0);
      clearCampaignF8(var0);
      clearCampaignMemory18(var0);
      F7NarrativeEngine.devReset(var0);
      F8SanctuaryEngine.devReset(var0);
   }

   private static void clearCampaignF8(Object var0) throws Exception {
      Object var1 = F8SanctuaryEngine.campaign(var0);
      Field var2 = field(var1.getClass(), "done");
      if (var2.get(var1) instanceof Set var4) {
         Set var5 = var4;

         for (String var9 : F8_KEYS) {
            var5.remove(var9);
         }
      }

      Field var11 = field(var1.getClass(), "timeline");
      if (var11.get(var1) instanceof List var13) {
         var13.clear();
      }

      setField(var1, "foundationPlaced", false);
      setField(var1, "migration", false);
      setField(var1, "foundationAt", 0L);
      setField(var1, "foundationX", 0);
      setField(var1, "foundationY", 0);
      setField(var1, "foundationZ", 0);
      setField(var1, "foundationDimension", "");
      setField(var1, "foundationName", "");
      setField(var1, "foundationFounder", "");
      setField(var1, "foundationFounderUuid", "");
      setField(var1, "foundationDay", 0);

      try {
         invokeNoArg(var1, "setDirty");
      } catch (Throwable var10) {
      }
   }

   private static void clearCampaignMemory18(Object var0) {
      try {
         Object var1 = callStatic("fr.reivaxmc.progress.story.CampaignStateData18", "getForServer", var0);

         for (String var5 : new String[]{"age1Resonance", "age1Stela", "age1Fragment", "age1Matrix", "age1FirstPage", "age1Foyer", "age1Response"}) {
            try {
               setField(var1, var5, false);
            } catch (Throwable var9) {
            }
         }

         try {
            setField(var1, "revision", numberField(var1, "revision") + 1);
         } catch (Throwable var8) {
         }

         try {
            invokeNoArg(var1, "setDirty");
         } catch (Throwable var7) {
         }
      } catch (Throwable var10) {
      }
   }

   private static void setAge1Resonance(Object var0, boolean var1) {
      try {
         Object var2 = callStatic("fr.reivaxmc.progress.story.CampaignStateData18", "getForServer", var0);
         setField(var2, "age1Resonance", var1);

         try {
            setField(var2, "revision", numberField(var2, "revision") + 1);
         } catch (Throwable var5) {
         }

         try {
            invokeNoArg(var2, "setDirty");
         } catch (Throwable var4) {
         }
      } catch (Throwable var6) {
         System.err.println("[REIVAX Alpha 18F.8.4.1] resonance checkpoint flag failed: " + var6.getClass().getSimpleName());
      }
   }

   private static int[] forceStoryCheckpoint(Object var0, Object var1, boolean var2, boolean var3) throws Exception {
      Object var4 = invokeNoArg(var1, "serverLevel");

      int[] var5;
      try {
         var5 = (int[])callStatic("fr.reivaxmc.progress.story.TracePlacementCompat18F", "findLandTarget", var4, var1);
      } catch (Throwable var14) {
         var5 = new int[]{
            number(invokeNoArg(var1, "getBlockX")).intValue() + 8,
            number(invokeNoArg(var1, "getBlockY")).intValue(),
            number(invokeNoArg(var1, "getBlockZ")).intValue()
         };
      }

      if (var3) {
         Object var6 = F8SanctuaryEngine.blockPos(var5[0], var5[1], var5[2]);
         Object var7 = F8SanctuaryEngine.staticField("fr.reivaxmc.progress.block.Alpha18FContent", "TRACE_BLOCK");
         callStatic("fr.reivaxmc.progress.story.TracePlacementCompat18F", "buildGiantTrace", var4, var6, var7);
      }

      Object var15 = callStatic("fr.reivaxmc.progress.story.StoryModeGate18F", "state", var0);
      String var16 = String.valueOf(invokeNoArg(var1, "getUUID"));
      String var8 = F8SanctuaryEngine.playerName(var1);
      long var9 = number(invokeNoArg(var0, "getTickCount")).longValue();
      setField(var15, "modeInitialized", true);
      setField(var15, "managed", true);
      setField(var15, "started", true);
      setField(var15, "tracePlaced", true);
      setField(var15, "traceExamined", var2);
      setField(var15, "startTick", Math.max(0L, var9 - 1000L));
      setField(var15, "traceExaminedTick", var2 ? var9 : 0L);
      setField(var15, "startedBy", var16);
      setField(var15, "startedByName", var8);
      setField(var15, "traceExaminedBy", var2 ? var16 : "");
      setField(var15, "traceX", var5[0]);
      setField(var15, "traceY", var5[1]);
      setField(var15, "traceZ", var5[2]);

      try {
         setField(var15, "revision", numberField(var15, "revision") + 1);
      } catch (Throwable var13) {
      }

      try {
         invokeNoArg(var15, "setDirty");
      } catch (Throwable var12) {
      }

      return var5;
   }

   private static void resetStoryToLauncher(Object var0) throws Exception {
      F90Sanctuary.reset(var0);
      Object var1 = callStatic("fr.reivaxmc.progress.story.StoryModeGate18F", "state", var0);
      setField(var1, "modeInitialized", true);
      setField(var1, "managed", true);
      setField(var1, "started", false);
      setField(var1, "tracePlaced", false);
      setField(var1, "traceExamined", false);
      setField(var1, "startTick", 0L);
      setField(var1, "traceExaminedTick", 0L);
      setField(var1, "startedBy", "");
      setField(var1, "startedByName", "");
      setField(var1, "traceExaminedBy", "");
      setField(var1, "traceX", 0);
      setField(var1, "traceY", 0);
      setField(var1, "traceZ", 0);

      try {
         setField(var1, "revision", numberField(var1, "revision") + 1);
      } catch (Throwable var4) {
      }

      try {
         invokeNoArg(var1, "setDirty");
      } catch (Throwable var3) {
      }
   }

   private static void removeOldFoundationBlock(Object var0) {
      try {
         Object var1 = F8SanctuaryEngine.campaign(var0);
         if (!boolInvoke(var1, "foundationPlaced")) {
            return;
         }

         Object var2 = invokeNoArg(var1, "foundationPos");
         Object var3 = invokeNoArg(var0, "overworld");
         F8SanctuaryEngine.setBlock(var3, F8SanctuaryEngine.posX(var2), F8SanctuaryEngine.posY(var2), F8SanctuaryEngine.posZ(var2), "AIR");
      } catch (Throwable var4) {
      }
   }

   private static void clearOldTrace(Object var0) {
      try {
         Object var1 = callStatic("fr.reivaxmc.progress.story.StoryModeGate18F", "state", var0);
         Object var2 = invokeNoArg(var1, "snapshot");
         if (!bool(var2, "tracePlaced")) {
            return;
         }

         int var3 = integer(var2, "traceX");
         int var4 = integer(var2, "traceY");
         int var5 = integer(var2, "traceZ");
         runCommand(
            var0,
            "fill "
               + (var3 - 5)
               + " "
               + (var4 - 2)
               + " "
               + (var5 - 5)
               + " "
               + (var3 + 5)
               + " "
               + (var4 + 12)
               + " "
               + (var5 + 5)
               + " air replace reivaxmc_progress:story_trace"
         );
      } catch (Throwable var6) {
      }
   }

   private static void killGuardians(Object var0) {
      try {
         runCommand(var0, "kill @e[tag=reivax_f8_guardian]");
      } catch (Throwable var2) {
      }
   }

   private static void clearReadyGate(Object var0) {
      try {
         Class var1 = Class.forName("fr.reivaxmc.progress.story.DuoStartGate18F");
         Field var2 = field(var1, "READY");
         if (var2.get(null) instanceof Map var4) {
            var4.remove(var0);
         }
      } catch (Throwable var6) {
      }
   }

   private static void removeTestItems(Object var0) {
      try {
         Object var1 = registryObject("ORIGIN_SEAL");
         Object var2 = registryObject("FOUNDATION_BEACON_ITEM");

         for (Object var4 : F8SanctuaryEngine.players(var0)) {
            Object var5 = invokeNoArg(var4, "getInventory");
            int var6 = number(invokeNoArg(var5, "getContainerSize")).intValue();

            for (int var7 = 0; var7 < var6; var7++) {
               Object var8 = invoke(var5, "getItem", var7);
               if (var8 != null) {
                  Object var9 = invokeNoArg(var8, "getItem");
                  if (var9 == var1 || var9 == var2) {
                     try {
                        invoke(var8, "setCount", 0);
                     } catch (Throwable var13) {
                        try {
                           invoke(var8, "shrink", number(invokeNoArg(var8, "getCount")).intValue());
                        } catch (Throwable var12) {
                        }
                     }
                  }
               }
            }
         }
      } catch (Throwable var14) {
      }
   }

   private static void giveItem(Object var0, String var1) throws Exception {
      Object var2 = registryObject(var1);
      Object var3 = invokeNoArg(var2, "getDefaultInstance");
      Object var4 = invokeNoArg(var0, "getInventory");
      if (invoke(var4, "add", var3) instanceof Boolean var6 && !var6) {
         try {
            invoke(var0, "drop", var3, false);
         } catch (Throwable var8) {
         }
      }
   }

   private static Object registryObject(String var0) throws Exception {
      Object var1 = F8SanctuaryEngine.staticField("fr.reivaxmc.progress.ReivaxMCProgress", var0);
      return invokeNoArg(var1, "get");
   }

   private static void syncStoryAndCampaign(Object var0) {
      try {
         Object var1 = callStatic("fr.reivaxmc.progress.story.StoryModeGate18F", "state", var0);
         Object var2 = F8SanctuaryEngine.campaign(var0);

         for (Object var4 : F8SanctuaryEngine.players(var0)) {
            try {
               callStatic("fr.reivaxmc.progress.story.StoryOpening18F", "sendStatus", var4, var1);
            } catch (Throwable var7) {
            }

            try {
               callStatic("fr.reivaxmc.progress.network.ProgressNetworking", "sync", var4, var2);
            } catch (Throwable var6) {
            }
         }
      } catch (Throwable var8) {
      }
   }

   private static void sendQaAll(Object var0, boolean var1) {
      try {
         for (Object var3 : F8SanctuaryEngine.players(var0)) {
            sendQa(var3, var1);
         }
      } catch (Throwable var4) {
      }
   }

   private static void sendQa(Object var0, boolean var1) {
      if (var0 != null) {
         F7NarrativeEngine.pushUi(var0, "F81_QA", var1 ? "ON" : "OFF");
      }
   }

   private static int runCommand(Object var0, String var1) throws Exception {
      Object var2 = invokeNoArg(var0, "getCommands");
      Object var3 = invokeNoArg(var0, "createCommandSourceStack");

      try {
         return invoke(var2, "performPrefixedCommand", var3, var1) instanceof Number var8 ? var8.intValue() : 1;
      } catch (Throwable var7) {
         return invoke(var2, "performCommand", var3, var1) instanceof Number var6 ? var6.intValue() : 1;
      }
   }

   private static Object literal(String var0) throws Exception {
      Class var1 = Class.forName("net.minecraft.commands.Commands");
      Method var2 = var1.getMethod("literal", String.class);
      return var2.invoke(null, var0);
   }

   private static Object literalExec(String var0, F81DevTools.Handler var1) throws Exception {
      Object var2 = literal(var0);
      executes(var2, var1);
      return var2;
   }

   private static void executes(Object var0, F81DevTools.Handler var1) throws Exception {
      Class var2 = Class.forName("com.mojang.brigadier.Command");
      Object var3 = Proxy.newProxyInstance(var2.getClassLoader(), new Class[]{var2}, (var1x, var2x, var3x) -> {
         if (var2x.getName().equals("run") && var3x != null && var3x.length == 1) {
            return var1.run(var3x[0]);
         } else if (var2x.getName().equals("equals") && var3x != null && var3x.length == 1) {
            return var1x == var3x[0];
         } else if (!var2x.getName().equals("hashCode") || var3x != null && var3x.length != 0) {
            if (var2x.getName().equals("toString")) {
               return "REIVAX_DEV_COMMAND";
            } else {
               Class var4 = var2x.getReturnType();
               if (var4 == boolean.class) {
                  return false;
               } else if (var4 == byte.class) {
                  return (byte)0;
               } else if (var4 == short.class) {
                  return (short)0;
               } else if (var4 == int.class) {
                  return 0;
               } else if (var4 == long.class) {
                  return 0L;
               } else if (var4 == float.class) {
                  return 0.0F;
               } else if (var4 == double.class) {
                  return 0.0;
               } else {
                  return var4 == char.class ? '\u0000' : null;
               }
            }
         } else {
            return System.identityHashCode(var1x);
         }
      });
      invoke(var0, "executes", var3);
   }

   private static void then(Object var0, Object var1) throws Exception {
      invoke(var0, "then", var1);
   }

   private static Object player(Object var0) {
      try {
         Object var1 = invokeNoArg(var0, "getSource");

         try {
            return invokeNoArg(var1, "getPlayer");
         } catch (Throwable var4) {
            try {
               return invokeNoArg(var1, "getPlayerOrException");
            } catch (Throwable var3) {
               return null;
            }
         }
      } catch (Throwable var5) {
         return null;
      }
   }

   private static Object serverFromContext(Object var0) {
      try {
         Object var1 = invokeNoArg(var0, "getSource");
         return invokeNoArg(var1, "getServer");
      } catch (Throwable var2) {
         return server(player(var0));
      }
   }

   private static boolean allowed(Object var0) {
      try {
         Object var1 = invokeNoArg(var0, "getSource");
         Object var2 = invokeNoArg(var1, "getServer");
         boolean var3 = false;

         try {
            boolean var10000;
            label35: {
               if (invokeNoArg(var2, "isDedicatedServer") instanceof Boolean var5 && var5) {
                  var10000 = true;
                  break label35;
               }

               var10000 = false;
            }

            var3 = var10000;
         } catch (Throwable var6) {
         }

         if (!var3) {
            return true;
         } else {
            if (invoke(var1, "hasPermission", 2) instanceof Boolean var9 && var9) {
               return true;
            }

            return false;
         }
      } catch (Throwable var7) {
         return false;
      }
   }

   private static int denied(Object var0) {
      msg(var0, "§cREIVAX DEV nécessite les droits OP sur un serveur dédié.");
      return 0;
   }

   private static void msg(Object var0, String var1) {
      if (var0 == null) {
         System.out.println(strip(var1));
      } else {
         try {
            Class var2 = Class.forName("net.minecraft.network.chat.Component");
            Object var3 = var2.getMethod("literal", String.class).invoke(null, var1);
            invoke(var0, "displayClientMessage", var3, false);
         } catch (Throwable var4) {
            System.out.println(strip(var1));
         }
      }
   }

   private static String onOff(boolean var0) {
      return var0 ? "§aON" : "§cOFF";
   }

   private static Object server(Object var0) {
      if (var0 == null) {
         return null;
      } else {
         try {
            return invokeNoArg(var0, "getServer");
         } catch (Throwable var4) {
            try {
               Object var2 = invokeNoArg(var0, "level");
               return invokeNoArg(var2, "getServer");
            } catch (Throwable var3) {
               return null;
            }
         }
      }
   }

   private static boolean bool(Object var0, String var1) throws Exception {
      if (invokeNoArg(var0, var1) instanceof Boolean var3 && var3) {
         return true;
      }

      return false;
   }

   private static int integer(Object var0, String var1) throws Exception {
      return number(invokeNoArg(var0, var1)).intValue();
   }

   private static boolean boolInvoke(Object var0, String var1) throws Exception {
      if (invokeNoArg(var0, var1) instanceof Boolean var3 && var3) {
         return true;
      }

      return false;
   }

   private static int numberField(Object var0, String var1) throws Exception {
      Field var2 = field(var0.getClass(), var1);
      return ((Number)var2.get(var0)).intValue();
   }

   private static void setField(Object var0, String var1, Object var2) throws Exception {
      Field var3 = field(var0.getClass(), var1);
      var3.set(var0, var2);
   }

   private static Field field(Class<?> var0, String var1) throws Exception {
      for (Class var2 = var0; var2 != null; var2 = var2.getSuperclass()) {
         try {
            Field var3 = var2.getDeclaredField(var1);
            var3.setAccessible(true);
            return var3;
         } catch (NoSuchFieldException var4) {
         }
      }

      throw new NoSuchFieldException(var0.getName() + "." + var1);
   }

   private static Object invokeNoArg(Object var0, String var1) throws Exception {
      return invoke(var0, var1);
   }

   private static Object invoke(Object var0, String var1, Object... var2) throws Exception {
      if (var0 == null) {
         throw new NullPointerException("target for " + var1);
      } else {
         Method var3 = findMethod(var0.getClass(), var1, false, var2);
         if (var3 == null) {
            throw new NoSuchMethodException(var0.getClass().getName() + "." + var1 + "/" + var2.length);
         } else {
            var3.setAccessible(true);
            return var3.invoke(var0, var2);
         }
      }
   }

   private static Object callStatic(String var0, String var1, Object... var2) throws Exception {
      Class var3 = Class.forName(var0);
      Method var4 = findMethod(var3, var1, true, var2);
      if (var4 == null) {
         throw new NoSuchMethodException(var0 + "." + var1 + "/" + var2.length);
      } else {
         var4.setAccessible(true);
         return var4.invoke(null, var2);
      }
   }

   private static Method findMethod(Class<?> var0, String var1, boolean var2, Object[] var3) {
      for (Method var7 : var0.getMethods()) {
         if (matches(var7, var1, var2, var3)) {
            return var7;
         }
      }

      for (Class var9 = var0; var9 != null; var9 = var9.getSuperclass()) {
         for (Method var8 : var9.getDeclaredMethods()) {
            if (matches(var8, var1, var2, var3)) {
               return var8;
            }
         }
      }

      return null;
   }

   private static boolean matches(Method var0, String var1, boolean var2, Object[] var3) {
      if (!var0.getName().equals(var1)) {
         return false;
      } else if (var2 != Modifier.isStatic(var0.getModifiers())) {
         return false;
      } else {
         Class[] var4 = var0.getParameterTypes();
         if (var4.length != var3.length) {
            return false;
         } else {
            for (int var5 = 0; var5 < var4.length; var5++) {
               if (!compatible(var4[var5], var3[var5])) {
                  return false;
               }
            }

            return true;
         }
      }
   }

   private static boolean compatible(Class<?> var0, Object var1) {
      if (var1 == null) {
         return !var0.isPrimitive();
      } else if (!var0.isPrimitive()) {
         return var0.isInstance(var1);
      } else {
         Class var2 = var1.getClass();
         return var0 == boolean.class && var2 == Boolean.class
            || var0 == byte.class && var2 == Byte.class
            || var0 == short.class && Number.class.isAssignableFrom(var2)
            || var0 == int.class && Number.class.isAssignableFrom(var2)
            || var0 == long.class && Number.class.isAssignableFrom(var2)
            || var0 == float.class && Number.class.isAssignableFrom(var2)
            || var0 == double.class && Number.class.isAssignableFrom(var2)
            || var0 == char.class && var2 == Character.class;
      }
   }

   private static Number number(Object var0) {
      if (var0 instanceof Number) {
         return (Number)var0;
      } else {
         throw new IllegalArgumentException("Expected number: " + var0);
      }
   }

   private static String strip(String var0) {
      if (var0 == null) {
         return "";
      } else {
         StringBuilder var1 = new StringBuilder();

         for (int var2 = 0; var2 < var0.length(); var2++) {
            if (var0.charAt(var2) == 167 && var2 + 1 < var0.length()) {
               var2++;
            } else {
               var1.append(var0.charAt(var2));
            }
         }

         return var1.toString();
      }
   }

   private static final class DevState {
      boolean enabled;
      boolean qaAntiSpoil;
   }

   private interface Handler {
      int run(Object var1) throws Exception;
   }
}
