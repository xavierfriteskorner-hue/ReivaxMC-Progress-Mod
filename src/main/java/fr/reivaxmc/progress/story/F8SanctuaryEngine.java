package fr.reivaxmc.progress.story;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

public final class F8SanctuaryEngine {
   static final String K_STARTED = "F8_SANCTUARY_QUEST_STARTED";
   static final String K_INTRO_VOICE = "F8_SANCTUARY_INTRO_VOICE";
   static final String K_BUILT = "F8_SANCTUARY_BUILT";
   static final String K_DISCOVERED = "F8_SANCTUARY_DISCOVERED";
   static final String K_GUARDS_AWAKENED = "F8_GUARDS_AWAKENED";
   static final String K_G1 = "F8_GUARD_1_DEFEATED";
   static final String K_G2 = "F8_GUARD_2_DEFEATED";
   static final String K_GUARDS_CLEARED = "F8_GUARDS_CLEARED";
   static final String K_FOUNDATION_GUARDS_AWAKENED = "F82_FOUNDATION_GUARDS_AWAKENED";
   static final String K_FG1 = "F82_FOUNDATION_GUARD_1_DEFEATED";
   static final String K_FG2 = "F82_FOUNDATION_GUARD_2_DEFEATED";
   static final String K_FOUNDATION_GUARDS_CLEARED = "F82_FOUNDATION_GUARDS_CLEARED";
   static final String K_VOICE_FOUNDATION_1 = "F8_VOICE_FOUNDATION_1";
   static final String K_VOICE_FOUNDATION_2 = "F8_VOICE_FOUNDATION_2";
   static final String K_BEACON_RECOVERED = "F8_FOUNDATION_BEACON_RECOVERED";
   static final String K_FOYER_ESTABLISHED = "F8_FIRST_FOYER_ESTABLISHED";
   static final String K_ROUTE_ECHO_1 = "F84_ROUTE_ECHO_1";
   static final String K_ROUTE_ECHO_2 = "F84_ROUTE_ECHO_2";
   static final String K_SEAL_INSERTED = "F84_SEAL_INSERTED";
   static final String K_BOOK_RECOVERED = "F84_BOOK_RECOVERED";
   static final String K_RELIQUARY_CLAIMED = "F84_RELIQUARY_CLAIMED";
   private static final double DISCOVERY_RADIUS = 72.0;
   private static final double ENTRY_RADIUS = 22.0;
   private static final double INNER_GATE_RADIUS = 13.0;
   private static final double GROUP_RADIUS = 38.0;
   static final double FOUNDATION_MIN_DISTANCE_FROM_SANCTUARY = 64.0;
   private static final Map<Object, F8SanctuaryEngine.Session> SESSIONS = Collections.synchronizedMap(new WeakHashMap<>());
   private static volatile String lastObjectiveSent = "";
   private static volatile boolean guidanceVisible = false;

   private F8SanctuaryEngine() {
   }

   public static void devReset(Object var0) {
      if (var0 != null) {
         SESSIONS.remove(var0);
      }

      lastObjectiveSent = "";
      guidanceVisible = false;
   }

   public static void tick(Object var0, List<?> var1, int var2, int var3, int var4) {
      if (var0 != null && var1 != null && !var1.isEmpty()) {
         try {
            Object var5 = campaign(var0);
            F8SanctuaryEngine.Session var6 = SESSIONS.computeIfAbsent(var0, var0x -> new F8SanctuaryEngine.Session());
            if (var6.target == null) {
               var6.target = findTarget(var0, var2, var4);
            }

            int[] var7 = var6.target;
            if (!isV84StructurePresent(var0, var7)) {
               buildSanctuary(var0, var7[0], var7[1], var7[2], completed(var5, "F8_FOUNDATION_BEACON_RECOVERED"));
               if (!completed(var5, "F8_SANCTUARY_BUILT")) {
                  complete(var5, "F8_SANCTUARY_BUILT");
               }
            } else if (!completed(var5, "F8_SANCTUARY_BUILT")) {
               complete(var5, "F8_SANCTUARY_BUILT");
            }

            ensureProtectors(var0, var7, var5);
            F90Sanctuary.runtimeTick(var0, target(var0));
            if (completed(var5, "F84_SEAL_INSERTED")) {
               openThresholdGate(var0, var7);
            }

            if (completed(var5, "F8_GUARDS_CLEARED")) {
               openOuterGate(var0, var7);
            }

            if (completed(var5, "F82_FOUNDATION_GUARDS_AWAKENED")) {
               openFoundationGate(var0, var7, 3);
            }

            if (!completed(var5, "F8_FOUNDATION_BEACON_RECOVERED")) {
               ensureSanctuaryBeacon(var0, var7);
            }

            if (boolInvoke(var5, "foundationPlaced")) {
               if (!completed(var5, "F8_FIRST_FOYER_ESTABLISHED")) {
                  complete(var5, "F8_FIRST_FOYER_ESTABLISHED");
                  chronicleOnce(
                     var0,
                     "Premier Foyer",
                     "La Borne de Fondation a été installée. Pour la première fois, vous avez choisi un lieu appelé à conserver votre propre histoire.",
                     "Vous"
                  );
               }

               clearGuidance(var1);
               objective(var1, "Installez-vous dans votre Foyer · commencez à construire votre base et sécurisez les environs.");
               return;
            }

            if (completed(var5, "F8_FOUNDATION_BEACON_RECOVERED")) {
               clearGuidance(var1);
               objective(var1, "Établissez votre premier Foyer · choisissez un lieu durable puis PLACEZ la Borne de Fondation.");
               if (!var6.beaconPlacementHint && ++var6.beaconHintTicks >= 120L) {
                  var6.beaconPlacementHint = true;
                  hint(var1, "Quand l'emplacement vous convient, posez la Borne au sol puis confirmez la fondation.");
               }

               return;
            }

            if (!completed(var5, "F8_SANCTUARY_QUEST_STARTED")) {
               complete(var5, "F8_SANCTUARY_QUEST_STARTED");
               chronicleOnce(
                  var0,
                  "Le Sceau s'éveille",
                  "Au lever du jour, le Sceau s'est remis à vibrer. Cette fois, la Résonance semblait provenir d'un point précis du monde.",
                  "Le Sceau"
               );
               history(var1, "§6RÉSONANCE §8• §fLe Sceau réagit à nouveau. Le signal possède maintenant une direction.");
               var6.introTicks = 0L;
            }

            var6.introTicks += 5L;
            if (!completed(var5, "F8_SANCTUARY_INTRO_VOICE") && var6.introTicks >= 35L) {
               complete(var5, "F8_SANCTUARY_INTRO_VOICE");
               voice(var1, "…Il reconnaît quelque chose.");
            }

            double var8 = nearestDistance(var1, var7[0], var7[1], var7[2]);
            if (!completed(var5, "F8_SANCTUARY_DISCOVERED")) {
               objective(var1, "Suivez la résonance du Sceau · gardez le cap indiqué par la flèche.");
               guidance(var0, var1, var7);
               if (var8 <= 720.0 && !completed(var5, "F84_ROUTE_ECHO_1")) {
                  complete(var5, "F84_ROUTE_ECHO_1");
                  hint(var1, "Le Sceau pulse plus nettement · la Résonance se resserre.");
                  history(var1, "§6RÉSONANCE §8• §fLe signal vient de gagner en intensité.");
                  playAtPlayers(var0, var1, "sanctuary_presence", 0.72, 0.58);
               }

               if (var8 <= 410.0 && !completed(var5, "F84_ROUTE_ECHO_2")) {
                  complete(var5, "F84_ROUTE_ECHO_2");
                  hint(var1, "Pendant un instant, les sons autour de vous semblent s'étouffer.");
                  history(var1, "§6RÉSONANCE §8• §fQuelque chose, devant vous, répond désormais presque sans interruption.");
                  playAtPlayers(var0, var1, "protector_presence", 0.58, 0.52);
               }

               if (var8 <= 72.0) {
                  complete(var5, "F8_SANCTUARY_DISCOVERED");
                  clearGuidance(var1);
                  chronicleOnce(
                     var0,
                     "Le Sanctuaire de Fondation",
                     "Guidés par le Sceau, vous avez découvert un sanctuaire ancien. Sa matière, son architecture et les silhouettes immobiles de son seuil ne ressemblaient à rien de connu.",
                     "Vous"
                  );
                  history(var1, "§6SANCTUAIRE §8• §fLa Résonance vous a conduits jusqu'à une structure ancienne.");
                  playSanctuarySound(var0, "sanctuary_presence", var7[0], var7[1] + 4, var7[2] + 14, 1.3, 0.82);
                  objective(var1, "Trouvez l'origine de la Résonance · approchez-vous du Sanctuaire et franchissez son seuil.");
               }

               return;
            }

            clearGuidance(var1);
            if (!completed(var5, "F84_SEAL_INSERTED")) {
               int[] var17 = sealStelePos(var7);
               double var18 = nearestDistance(var1, var17[0], var17[1], var17[2]);
               if (var18 > 14.0) {
                  objective(var1, "Deux Réceptacles des Sceaux encadrent l'entrée du Sanctuaire. Approchez-vous de l'une des deux bornes.");
               } else {
                  objective(var1, "Insérez un Sceau dans chaque Réceptacle — tenez un Sceau des Origines en main puis faites CLIC DROIT sur la borne.");
               }

               return;
            }

            openThresholdGate(var0, var7);
            if (!completed(var5, "F8_GUARDS_AWAKENED")) {
               int var10 = countNear(var1, var7[0], var7[1] + 1, var7[2] + 16, 38.0);
               if (var10 < var1.size()) {
                  objective(var1, "Regroupez-vous devant le Sanctuaire · " + var10 + "/" + var1.size() + " présents.");
                  return;
               }

               if (var8 > 22.0) {
                  objective(var1, "Franchissez le seuil du Sanctuaire · avancez dans le hall d'entrée.");
                  return;
               }

               activateProtectorGroup(var0, var7, false, var1.size());
               complete(var5, "F8_GUARDS_AWAKENED");
               chronicleOnce(
                  var0,
                  "Les Veilleurs du Sanctuaire",
                  "Deux silhouettes jusque-là immobiles se sont éveillées dans le hall. Elles semblaient exister pour une seule fonction : empêcher quiconque de poursuivre.",
                  "Le Sanctuaire"
               );
               history(var1, "§6VEILLEURS §8• §fLes deux silhouettes du hall viennent de s'éveiller.");
               playSanctuarySound(var0, "protector_awaken", var7[0], var7[1] + 3, var7[2] + 13, 1.8, 0.78);
            } else {
               activateProtectorGroup(var0, var7, false, var1.size());
            }

            if (completed(var5, "F8_GUARDS_AWAKENED") && !completed(var5, "F8_GUARDS_CLEARED")) {
               var6.presenceSoundTicks += 5L;
               if (var6.presenceSoundTicks >= 95L) {
                  var6.presenceSoundTicks = 0L;
                  playSanctuarySound(var0, "protector_presence", var7[0], var7[1] + 3, var7[2] + 13, 0.9, 0.86);
               }
            }

            int var16 = (completed(var5, "F8_GUARD_1_DEFEATED") ? 1 : 0) + (completed(var5, "F8_GUARD_2_DEFEATED") ? 1 : 0);
            if (var16 < 2) {
               objective(var1, "Neutralisez les Veilleurs du Sanctuaire · " + var16 + "/2.");
               return;
            }

            if (!completed(var5, "F8_GUARDS_CLEARED")) {
               complete(var5, "F8_GUARDS_CLEARED");
               openOuterGate(var0, var7);
               chronicleOnce(
                  var0,
                  "Le passage intérieur",
                  "Les Veilleurs neutralisés, la grille du hall s'est libérée. Un passage plus sombre mène vers une seconde porte, entièrement fermée.",
                  "Vous"
               );
               history(var1, "§6SANCTUAIRE §8• §fLa grille du hall s'est ouverte.");
               playSanctuarySound(var0, "sanctuary_gate", var7[0], var7[1] + 3, var7[2] + 6, 1.4, 0.92);
            } else {
               openOuterGate(var0, var7);
            }

            double var11 = nearestDistance(var1, var7[0], var7[1] + 2, var7[2] - 7);
            if (!completed(var5, "F82_FOUNDATION_GUARDS_AWAKENED")) {
               if (var11 > 13.0 && !var6.innerGateOpening) {
                  objective(var1, "Progressez dans le Sanctuaire · rejoignez la grande porte au bout du passage.");
                  return;
               }

               int var13 = countNear(var1, var7[0], var7[1] + 2, var7[2] - 7, 19.0);
               if (var13 < var1.size() && !var6.innerGateOpening) {
                  objective(var1, "Regroupez-vous devant la porte intérieure · " + var13 + "/" + var1.size() + " présents.");
                  return;
               }

               if (!var6.innerGateOpening) {
                  var6.innerGateOpening = true;
                  var6.innerGateTicks = 0L;
                  history(var1, "§6SANCTUAIRE §8• §fLe mécanisme intérieur répond à votre présence.");
                  playSanctuarySound(var0, "sanctuary_gate", var7[0], var7[1] + 4, var7[2] - 7, 1.8, 0.66);
               }

               var6.innerGateTicks += 5L;
               int var14 = var6.innerGateTicks < 15L ? 1 : (var6.innerGateTicks < 30L ? 2 : 3);
               if (var14 > var6.innerGateStage) {
                  var6.innerGateStage = var14;
                  openFoundationGate(var0, var7, var14);
               }

               objective(var1, "Restez devant la porte · le mécanisme du Sanctuaire est en train de s'ouvrir.");
               if (var6.innerGateTicks < 40L) {
                  return;
               }

               activateProtectorGroup(var0, var7, true, var1.size());
               complete(var5, "F82_FOUNDATION_GUARDS_AWAKENED");
               chronicleOnce(
                  var0,
                  "Les Gardiens de Fondation",
                  "Derrière la porte, deux protecteurs attendaient déjà dans la chambre de Fondation. Ils n'avaient pas été invoqués : ils montaient la garde depuis bien avant votre arrivée.",
                  "Le Sanctuaire"
               );
               history(var1, "§6GARDIENS §8• §fLa chambre de Fondation est gardée.");
               playSanctuarySound(var0, "protector_awaken", var7[0], var7[1] + 4, var7[2] - 15, 2.0, 0.62);
            } else {
               openFoundationGate(var0, var7, 3);
               activateProtectorGroup(var0, var7, true, var1.size());
            }

            if (completed(var5, "F82_FOUNDATION_GUARDS_AWAKENED") && !completed(var5, "F82_FOUNDATION_GUARDS_CLEARED")) {
               var6.guardianPresenceSoundTicks += 5L;
               if (var6.guardianPresenceSoundTicks >= 85L) {
                  var6.guardianPresenceSoundTicks = 0L;
                  playSanctuarySound(var0, "protector_presence", var7[0], var7[1] + 4, var7[2] - 15, 1.05, 0.72);
               }
            }

            int var19 = (completed(var5, "F82_FOUNDATION_GUARD_1_DEFEATED") ? 1 : 0) + (completed(var5, "F82_FOUNDATION_GUARD_2_DEFEATED") ? 1 : 0);
            if (var19 < 2) {
               objective(var1, "Neutralisez les Gardiens de Fondation · " + var19 + "/2.");
               return;
            }

            if (!completed(var5, "F82_FOUNDATION_GUARDS_CLEARED")) {
               complete(var5, "F82_FOUNDATION_GUARDS_CLEARED");
               var6.guardClearTicks = 0L;
               chronicleOnce(
                  var0,
                  "La chambre de Fondation",
                  "Les derniers protecteurs ont été neutralisés. Au fond de la chambre, la Borne demeure intacte sur son autel.",
                  "Vous"
               );
               history(var1, "§6GARDIENS §8• §fLa Borne de Fondation est désormais accessible.");
               playSanctuarySound(var0, "protector_fall", var7[0], var7[1] + 4, var7[2] - 16, 1.0, 0.7);
            }

            var6.guardClearTicks += 5L;
            if (!completed(var5, "F8_VOICE_FOUNDATION_1") && var6.guardClearTicks >= 25L) {
               complete(var5, "F8_VOICE_FOUNDATION_1");
               voice(var1, "Une fondation…");
            }

            if (!completed(var5, "F8_VOICE_FOUNDATION_2") && var6.guardClearTicks >= 75L) {
               complete(var5, "F8_VOICE_FOUNDATION_2");
               voice(var1, "Je me souviens de ce mot.");
            }

            if (!completed(var5, "F84_BOOK_RECOVERED")) {
               objective(var1, "Examinez le Livre ancien · il repose sur le pupitre à droite de la chambre.");
               return;
            }

            if (!completed(var5, "F84_RELIQUARY_CLAIMED")) {
               objective(var1, "Ouvrez le Reliquaire du Sanctuaire · il s'est déverrouillé après la chute des Protecteurs.");
               return;
            }

            objective(var1, "Récupérez la Borne de Fondation · approchez-vous de l'autel et faites CLIC DROIT sur la Borne.");
         } catch (Throwable var15) {
            System.err.println("[REIVAX Alpha 18F.8.4] Sanctuary tick failed: " + var15.getClass().getSimpleName() + ": " + var15.getMessage());
         }
      }
   }

   static int[] target(Object var0) throws Exception {
      F8SanctuaryEngine.Session var1 = SESSIONS.computeIfAbsent(var0, var0x -> new F8SanctuaryEngine.Session());
      if (var1.target != null) {
         return var1.target;
      } else {
         Object var2 = callStatic("fr.reivaxmc.progress.story.StoryModeGate18F", "state", var0);
         Object var3 = invokeNoArg(var2, "snapshot");
         int var4 = number(invokeNoArg(var3, "traceX")).intValue();
         int var5 = number(invokeNoArg(var3, "traceZ")).intValue();
         var1.target = findTarget(var0, var4, var5);
         return var1.target;
      }
   }

   static int[] beaconPos(Object var0) throws Exception {
      int[] var1 = target(var0);
      return new int[]{var1[0], var1[1] + 4, var1[2] - 18};
   }

   static void onBeaconRecovered(Object var0, Object var1) {
      try {
         Object var2 = campaign(var0);
         if (completed(var2, "F8_FOUNDATION_BEACON_RECOVERED")) {
            return;
         }

         complete(var2, "F8_FOUNDATION_BEACON_RECOVERED");
         String var3 = var1 == null ? "Vous" : playerName(var1);
         chronicleOnce(
            var0,
            "La Borne de Fondation",
            "La Borne conservée au cœur du Sanctuaire a été retirée de son autel. Elle peut désormais servir à établir votre propre Foyer.",
            var3
         );
         List var4 = players(var0);
         history(var4, "§6BORNE §8• §f" + var3 + " a récupéré la Borne de Fondation.");
         voice(var4, "Choisissez bien.");
         clearGuidance(var4);
         objective(var4, "Établissez votre premier Foyer · choisissez un lieu durable puis PLACEZ la Borne de Fondation.");
      } catch (Throwable var5) {
         System.err.println("[REIVAX Alpha 18F.8.4] Beacon recovery state failed: " + var5);
      }
   }

   static void onFoundationEstablished(Object var0, Object var1) {
      try {
         Object var2 = campaign(var0);
         complete(var2, "F8_FIRST_FOYER_ESTABLISHED");
         String var3 = var1 == null ? "Vous" : playerName(var1);
         chronicleOnce(
            var0,
            "Premier Foyer",
            "La Borne de Fondation a été installée. Un territoire de 96 blocs autour de ce lieu devient votre premier point d'ancrage dans le monde.",
            var3
         );
         List var4 = players(var0);
         history(var4, "§6FOYER §8• §fPremier Foyer établi. La Borne est désormais liée à ce lieu.");

         for (Object var6 : var4) {
            F7NarrativeEngine.pushUi(var6, "F8_MILESTONE", "PREMIER FOYER ÉTABLI");
         }

         voice(var4, "Ici, donc.");
         objective(var4, "Installez-vous dans votre Foyer · commencez à construire votre base et sécurisez les environs.");
      } catch (Throwable var7) {
         System.err.println("[REIVAX Alpha 18F.8.4] Foundation completion failed: " + var7);
      }
   }

   static void onGuardianDefeated(Object var0, String var1, Object var2) {
      try {
         Object var3 = campaign(var0);
         boolean var4 = var1 != null && var1.startsWith("fg");
         String var5;
         if ("g2".equals(var1)) {
            var5 = "F8_GUARD_2_DEFEATED";
         } else if ("fg1".equals(var1)) {
            var5 = "F82_FOUNDATION_GUARD_1_DEFEATED";
         } else if ("fg2".equals(var1)) {
            var5 = "F82_FOUNDATION_GUARD_2_DEFEATED";
         } else {
            var5 = "F8_GUARD_1_DEFEATED";
         }

         if (completed(var3, var5)) {
            return;
         }

         complete(var3, var5);
         List var6 = players(var0);
         if (var4) {
            int var7 = (completed(var3, "F82_FOUNDATION_GUARD_1_DEFEATED") ? 1 : 0) + (completed(var3, "F82_FOUNDATION_GUARD_2_DEFEATED") ? 1 : 0);
            history(var6, "§6GARDIENS §8• §fGardien de Fondation neutralisé · " + var7 + "/2.");
            objective(
               var6,
               var7 < 2
                  ? "Neutralisez les Gardiens de Fondation · " + var7 + "/2."
                  : "Restez dans la chambre de Fondation · la Borne est maintenant accessible."
            );

            try {
               int[] var8 = target(var0);
               playSanctuarySound(var0, "protector_fall", var8[0], var8[1] + 3, var8[2] - 15, 1.0, 0.72);
            } catch (Throwable var10) {
            }
         } else {
            int var12 = (completed(var3, "F8_GUARD_1_DEFEATED") ? 1 : 0) + (completed(var3, "F8_GUARD_2_DEFEATED") ? 1 : 0);
            history(var6, "§6VEILLEURS §8• §fVeilleur du Sanctuaire neutralisé · " + var12 + "/2.");
            objective(
               var6,
               var12 < 2 ? "Neutralisez les Veilleurs du Sanctuaire · " + var12 + "/2." : "Avancez vers la chambre intérieure · le passage vient de s'ouvrir."
            );

            try {
               int[] var13 = target(var0);
               playSanctuarySound(var0, "protector_fall", var13[0], var13[1] + 3, var13[2] + 14, 0.9, 0.84);
            } catch (Throwable var9) {
            }
         }
      } catch (Throwable var11) {
         System.err.println("[REIVAX Alpha 18F.8.4] Guardian state failed: " + var11);
      }
   }

   static boolean completed(Object var0, String var1) throws Exception {
      if (invoke(var0, "isCompleted", var1) instanceof Boolean var3 && var3) {
         return true;
      }

      return false;
   }

   static void complete(Object var0, String var1) throws Exception {
      invoke(var0, "complete", var1, 0, 0);
   }

   static Object campaign(Object var0) throws Exception {
      return callStatic("fr.reivaxmc.progress.progression.CampaignSavedData", "get", var0);
   }

   static void objective(List<?> var0, String var1) {
      String var2 = "§6OBJECTIF PRINCIPAL §8• §f" + var1;
      F7NarrativeEngine.setCurrentObjective(var2);
      if (!var2.equals(lastObjectiveSent)) {
         lastObjectiveSent = var2;

         for (Object var4 : var0) {
            F7NarrativeEngine.routeStoryMessage(var4, var2, true);
         }
      }
   }

   static void hint(List<?> var0, String var1) {
      for (Object var3 : var0) {
         F7NarrativeEngine.pushUi(var3, "F71_HINT", var1);
      }
   }

   static void history(List<?> var0, String var1) {
      for (Object var3 : var0) {
         F7NarrativeEngine.routeStoryMessage(var3, var1, false);
      }
   }

   static void voice(List<?> var0, String var1) {
      for (Object var3 : var0) {
         F7NarrativeEngine.routeStoryMessage(var3, "§8[§fLA VOIX§8] §f" + var1, false);
      }
   }

   static List<?> players(Object var0) throws Exception {
      Object var1 = invokeNoArg(var0, "getPlayerList");
      return invokeNoArg(var1, "getPlayers") instanceof List var3 ? var3 : List.of();
   }

   private static void guidance(Object var0, List<?> var1, int[] var2) throws Exception {
      int var3 = number(invokeNoArg(var0, "getTickCount")).intValue();
      if (var3 % 20 == 0) {
         for (Object var5 : var1) {
            double var6 = (double)var2[0] + 0.5 - number(invokeNoArg(var5, "getX")).doubleValue();
            double var8 = (double)var2[2] + 0.5 - number(invokeNoArg(var5, "getZ")).doubleValue();
            double var10 = Math.sqrt(var6 * var6 + var8 * var8);
            float var12 = number(invokeNoArg(var5, "getYRot")).floatValue();
            double var13 = Math.toDegrees(Math.atan2(-var6, var8));
            double var15 = normalize(var13 - (double)var12);
            String var17 = arrow(var15);
            String var18 = var10 > 180.0 ? "FAIBLE" : (var10 > 110.0 ? "PERCEPTIBLE" : (var10 > 60.0 ? "FORTE" : "TRÈS FORTE"));
            F7NarrativeEngine.pushUi(var5, "F8_GUIDANCE", var17 + "  " + Math.max(0L, Math.round(var10)) + " m  ·  RÉSONANCE " + var18);
            guidanceVisible = true;
         }
      }
   }

   private static void clearGuidance(List<?> var0) {
      if (guidanceVisible) {
         guidanceVisible = false;

         for (Object var2 : var0) {
            F7NarrativeEngine.pushUi(var2, "F8_GUIDANCE", "");
         }
      }
   }

   private static String arrow(double var0) {
      if (var0 >= -22.5 && var0 < 22.5) {
         return "↑";
      } else if (var0 >= 22.5 && var0 < 67.5) {
         return "↖";
      } else if (var0 >= 67.5 && var0 < 112.5) {
         return "←";
      } else if (var0 >= 112.5 && var0 < 157.5) {
         return "↙";
      } else if (var0 >= 157.5 || var0 < -157.5) {
         return "↓";
      } else if (var0 >= -157.5 && var0 < -112.5) {
         return "↘";
      } else {
         return var0 >= -112.5 && var0 < -67.5 ? "→" : "↗";
      }
   }

   private static double normalize(double var0) {
      while (var0 <= -180.0) {
         var0 += 360.0;
      }

      while (var0 > 180.0) {
         var0 -= 360.0;
      }

      return var0;
   }

   private static double nearestDistance(List<?> var0, int var1, int var2, int var3) throws Exception {
      double var4 = Double.MAX_VALUE;

      for (Object var7 : var0) {
         double var8 = number(invokeNoArg(var7, "getX")).doubleValue() - ((double)var1 + 0.5);
         double var10 = number(invokeNoArg(var7, "getY")).doubleValue() - ((double)var2 + 1.0);
         double var12 = number(invokeNoArg(var7, "getZ")).doubleValue() - ((double)var3 + 0.5);
         var4 = Math.min(var4, Math.sqrt(var8 * var8 + var10 * var10 + var12 * var12));
      }

      return var4;
   }

   static int countNear(List<?> var0, int var1, int var2, int var3, double var4) throws Exception {
      double var6 = var4 * var4;
      int var8 = 0;

      for (Object var10 : var0) {
         double var11 = number(invokeNoArg(var10, "getX")).doubleValue() - ((double)var1 + 0.5);
         double var13 = number(invokeNoArg(var10, "getY")).doubleValue() - ((double)var2 + 0.5);
         double var15 = number(invokeNoArg(var10, "getZ")).doubleValue() - ((double)var3 + 0.5);
         if (var11 * var11 + var13 * var13 + var15 * var15 <= var6) {
            var8++;
         }
      }

      return var8;
   }

   private static int[] findTarget(Object var0, int var1, int var2) throws Exception {
      return F90Terrain.findTarget(var0, var1, var2);
   }

   private static F8SanctuaryEngine.Candidate evaluateLand(Object var0, int var1, int var2) {
      try {
         int[][] var3 = new int[][]{
            {0, 0},
            {-15, -23},
            {15, -23},
            {-14, 22},
            {14, 22},
            {-10, 30},
            {10, 30},
            {-15, -10},
            {15, -10},
            {-13, 8},
            {13, 8},
            {-13, 20},
            {13, 20},
            {0, -23},
            {0, 22},
            {-7, 0},
            {7, 0},
            {-10, -16},
            {10, -16}
         };
         int var4 = Integer.MAX_VALUE;
         int var5 = Integer.MIN_VALUE;
         int var6 = 0;

         for (int[] var10 : var3) {
            int var11 = height(var0, var1 + var10[0], var2 + var10[1]);
            if (var11 < 20) {
               return null;
            }

            if (!dry(var0, var1 + var10[0], var11, var2 + var10[1])) {
               return null;
            }

            var4 = Math.min(var4, var11);
            var5 = Math.max(var5, var11);
            var6 += var11;
         }

         int var13 = var5 - var4;
         if (var13 > 5) {
            return null;
         } else {
            int var14 = Math.max(var4, Math.round((float)var6 / (float)var3.length)) - 1;
            F8SanctuaryEngine.Candidate var15 = new F8SanctuaryEngine.Candidate(var1, var14, var2, var13);
            var15.score = var13 * 1800;
            return var15;
         }
      } catch (Throwable var12) {
         return null;
      }
   }

   private static int height(Object var0, int var1, int var2) throws Exception {
      Class var3 = Class.forName("net.minecraft.world.level.levelgen.Heightmap$Types");
      Object var4 = var3.getField("MOTION_BLOCKING_NO_LEAVES").get(null);
      return number(invoke(var0, "getHeight", var4, var1, var2)).intValue();
   }

   private static boolean dry(Object var0, int var1, int var2, int var3) throws Exception {
      Object var4 = blockPos(var1, var2 - 1, var3);
      Object var5 = invoke(var0, "getBlockState", var4);
      Object var6 = invokeNoArg(var5, "getFluidState");
      if (invokeNoArg(var6, "isEmpty") instanceof Boolean var8 && var8) {
         return true;
      }

      return false;
   }

   private static void buildSanctuary(Object var0, int var1, int var2, int var3, boolean var4) throws Exception {
      Object var5 = invokeNoArg(var0, "overworld");
      Object var6 = sanctuaryStoneBlock();
      Object var7 = sanctuaryLumenBlock();
      Object var8 = staticField("net.minecraft.world.level.block.Blocks", "TUFF_BRICKS");
      Object var9 = staticField("net.minecraft.world.level.block.Blocks", "DEEPSLATE_TILES");
      Object var10 = staticField("net.minecraft.world.level.block.Blocks", "POLISHED_DEEPSLATE");
      Object var11 = staticField("net.minecraft.world.level.block.Blocks", "CHISELED_DEEPSLATE");
      Object var12 = campaign(var0);
      boolean var13 = completed(var12, "F84_SEAL_INSERTED");
      boolean var14 = false;

      try {
         Object var15 = invoke(var5, "getBlockState", blockPos(var1 + 13, var2 + 4, var3 + 14));
         var14 = invokeNoArg(var15, "getBlock") == var6;
         Object var16 = invoke(var5, "getBlockState", blockPos(var1, var2 + 15, var3 - 16));
         if (invokeNoArg(var16, "getBlock") == var7) {
            var14 = false;
         }
      } catch (Throwable var27) {
      }

      if (var14) {
         Object var29 = staticField("net.minecraft.world.level.block.Blocks", "POLISHED_BLACKSTONE_BRICKS");

         for (int var68 = -16; var68 <= 16; var68++) {
            for (int var17 = -23; var17 <= 23; var17++) {
               for (int var18 = 1; var18 <= 17; var18++) {
                  setBlock(var5, var1 + var68, var2 + var18, var3 + var17, "AIR");
               }

               try {
                  Object var127 = invoke(var5, "getBlockState", blockPos(var1 + var68, var2, var3 + var17));
                  Object var19 = invokeNoArg(var127, "getBlock");
                  if (var19 == var6 || var19 == var29 || var19 == var8 || var19 == var9) {
                     setBlock(var5, var1 + var68, var2, var3 + var17, "GRASS_BLOCK");
                  }
               } catch (Throwable var28) {
               }
            }
         }
      }

      try {
         runCommand(var0, "kill @e[tag=reivax_f8_guardian,x=" + var1 + ",y=" + (var2 + 4) + ",z=" + var3 + ",distance=..75]");
      } catch (Throwable var26) {
      }

      clearVolume(var5, var1, var2, var3, -12, 12, 19, 32, 1, 11);
      clearVolume(var5, var1, var2, var3, -12, 12, 8, 21, 1, 10);
      clearVolume(var5, var1, var2, var3, -6, 6, -6, 7, 1, 8);
      clearVolume(var5, var1, var2, var3, -13, 13, -23, -8, 1, 15);
      foundationFloor(var5, var1, var2, var3, -11, 11, 19, 32, var6, var8);
      foundationFloor(var5, var1, var2, var3, -12, 12, 8, 21, var6, var8);
      foundationFloor(var5, var1, var2, var3, -6, 6, -6, 7, var6, var9);
      foundationFloor(var5, var1, var2, var3, -13, 13, -23, -8, var6, var8);

      for (int var30 = 20; var30 <= 32; var30++) {
         for (int var69 = -9; var69 <= 9; var69++) {
            if ((Math.abs(var69) + var30) % 11 == 0) {
               setBlock(var5, var1 + var69, var2, var3 + var30, "MOSS_BLOCK");
            }

            if (Math.abs(var69) >= 7 && (var69 + var30 & 2) == 0) {
               setBlock(var5, var1 + var69, var2 + 1, var3 + var30, "MOSS_CARPET");
            }
         }
      }

      for (int[] var128 : new int[][]{{-10, 23}, {10, 23}, {-8, 20}, {8, 20}, {-9, 30}, {9, 30}}) {
         setBlock(var5, var1 + var128[0], var2 + 1, var3 + var128[1], (var128[0] + var128[1] & 1) == 0 ? "AZALEA" : "FLOWERING_AZALEA");
      }

      for (int var32 = 25; var32 <= 31; var32++) {
         for (int var71 = -2; var71 <= 2; var71++) {
            if ((var71 + var32) % 5 != 0) {
               setBlock(var5, var1 + var71, var2 + 1, var3 + var32, var10);
            }
         }
      }

      for (int var129 : new int[]{-1, 1}) {
         int var144 = var1 + var129 * 6;

         for (int var20 = 1; var20 <= 4; var20++) {
            setBlock(var5, var144, var2 + var20, var3 + 28, var20 == 3 ? var7 : var6);
         }

         setBlock(var5, var144, var2 + 5, var3 + 28, "SOUL_LANTERN");
      }

      for (int var34 = -2; var34 <= 2; var34++) {
         for (int var73 = 28; var73 <= 30; var73++) {
            setBlock(var5, var1 + var34, var2 + 1, var3 + var73, var6);
         }
      }

      setBlock(var5, var1, var2 + 2, var3 + 29, originMatrixBlock());
      setBlock(var5, var1 - 1, var2 + 2, var3 + 29, var7);
      setBlock(var5, var1 + 1, var2 + 2, var3 + 29, var7);

      for (int var35 = -13; var35 <= 13; var35++) {
         for (int var74 = 1; var74 <= 9; var74++) {
            boolean var108 = Math.abs(var35) <= 3 && var74 <= 6;
            if (!var108) {
               Object var130 = (Math.abs(var35) + var74) % 5 == 0 ? var8 : var6;
               setBlock(var5, var1 + var35, var2 + var74, var3 + 22, var130);
            }
         }
      }

      if (!var13) {
         for (int var36 = -3; var36 <= 3; var36++) {
            for (int var75 = 1; var75 <= 6; var75++) {
               setBlock(var5, var1 + var36, var2 + var75, var3 + 22, "IRON_BARS");
            }
         }
      }

      for (int var131 : new int[]{-1, 1}) {
         int var145 = var131 * 13;

         for (int var150 = 0; var150 <= 3; var150++) {
            int var21 = var145 + var131 * var150;
            int var22 = 10 - var150;

            for (int var23 = 1; var23 <= var22; var23++) {
               for (int var24 = 20; var24 <= 22; var24++) {
                  setBlock(var5, var1 + var21, var2 + var23, var3 + var24, var23 == var22 ? var7 : var6);
               }
            }
         }
      }

      for (int var38 = -5; var38 <= 5; var38++) {
         if (Math.abs(var38) > 3) {
            setBlock(var5, var1 + var38, var2 + 7, var3 + 21, var7);
         }

         setBlock(var5, var1 + var38, var2 + 9, var3 + 22, var38 % 2 == 0 ? var11 : var6);
      }

      for (int var39 = 8; var39 <= 21; var39++) {
         for (int var77 = 1; var77 <= 9; var77++) {
            Object var110 = var77 != 1 && var77 != 5 && var77 != 9 ? var8 : var6;
            setBlock(var5, var1 - 12, var2 + var77, var3 + var39, var110);
            setBlock(var5, var1 + 12, var2 + var77, var3 + var39, var110);
         }
      }

      for (int var40 = -12; var40 <= 12; var40++) {
         for (int var78 = 1; var78 <= 9; var78++) {
            boolean var111 = Math.abs(var40) <= 2 && var78 <= 5;
            if (var111) {
               setBlock(var5, var1 + var40, var2 + var78, var3 + 7, "IRON_BARS");
            } else {
               setBlock(var5, var1 + var40, var2 + var78, var3 + 7, var78 != 4 && var78 != 8 ? var8 : var6);
            }
         }
      }

      for (int var41 = -12; var41 <= 12; var41++) {
         for (int var79 = 8; var79 <= 21; var79++) {
            boolean var112 = Math.abs(var41) <= 2 && var79 >= 13 && var79 <= 15;
            if (var112) {
               setBlock(var5, var1 + var41, var2 + 10, var3 + var79, "IRON_BARS");
            } else {
               setBlock(var5, var1 + var41, var2 + 10, var3 + var79, var41 % 6 != 0 && var79 % 5 != 0 ? var9 : var6);
            }
         }
      }

      for (int var132 : new int[]{10, 15, 20}) {
         for (int var155 : new int[]{-9, 9}) {
            for (int var157 = 1; var157 <= 8; var157++) {
               setBlock(var5, var1 + var155, var2 + var157, var3 + var132, var157 % 4 == 0 ? var7 : var6);
            }

            setBlock(var5, var1 + var155, var2, var3 + var132, var11);
         }
      }

      for (int[] var133 : new int[][]{{-10, 11}, {10, 11}, {-10, 18}, {10, 18}}) {
         setBlock(var5, var1 + var133[0], var2 + 1, var3 + var133[1], "SOUL_LANTERN");
      }

      for (int[] var134 : new int[][]{{-7, 12}, {7, 17}, {-5, 19}, {5, 10}}) {
         setBlock(var5, var1 + var134[0], var2 + 1, var3 + var134[1], "MOSS_CARPET");
      }

      for (int var45 = -6; var45 <= 6; var45++) {
         for (int var83 = 1; var83 <= 7; var83++) {
            setBlock(var5, var1 - 6, var2 + var83, var3 + var45, var83 == 4 ? var7 : var6);
            setBlock(var5, var1 + 6, var2 + var83, var3 + var45, var83 == 4 ? var7 : var6);
         }
      }

      for (int var46 = -6; var46 <= 6; var46++) {
         for (int var84 = -6; var84 <= 6; var84++) {
            setBlock(var5, var1 + var46, var2 + 8, var3 + var84, var84 % 4 != 0 && var46 != -6 && var46 != 6 ? var9 : var6);
         }
      }

      for (int var135 : new int[]{4, 0, -4}) {
         setBlock(var5, var1 - 5, var2 + 3, var3 + var135, var7);
         setBlock(var5, var1 + 5, var2 + 3, var3 + var135, var7);
      }

      for (int var48 = -9; var48 <= 9; var48++) {
         for (int var86 = 1; var86 <= 10; var86++) {
            boolean var117 = Math.abs(var48) <= 3 && var86 <= 7;
            Object var136 = var117 ? var6 : (var86 != 3 && var86 != 8 ? var8 : var6);
            setBlock(var5, var1 + var48, var2 + var86, var3 - 7, var136);
         }
      }

      for (int var137 : new int[]{2, 4, 6}) {
         setBlock(var5, var1, var2 + var137, var3 - 7, var7);
      }

      for (int var138 : new int[]{-7, 7}) {
         for (int var147 = 1; var147 <= 10; var147++) {
            setBlock(var5, var1 + var138, var2 + var147, var3 - 7, var147 % 3 == 0 ? var11 : var6);
         }
      }

      for (int var51 = -23; var51 <= -8; var51++) {
         for (int var89 = 1; var89 <= 11; var89++) {
            Object var120 = var89 > 3 && var89 != 7 && var89 != 11 ? var8 : var6;
            setBlock(var5, var1 - 13, var2 + var89, var3 + var51, var120);
            setBlock(var5, var1 + 13, var2 + var89, var3 + var51, var120);
         }
      }

      for (int var52 = -13; var52 <= 13; var52++) {
         for (int var90 = 1; var90 <= 11; var90++) {
            setBlock(var5, var1 + var52, var2 + var90, var3 - 23, var90 != 4 && var90 != 9 ? var8 : var6);
         }
      }

      for (int var53 = -13; var53 <= 13; var53++) {
         for (int var91 = 1; var91 <= 11; var91++) {
            if (Math.abs(var53) > 4 || var91 > 8) {
               setBlock(var5, var1 + var53, var2 + var91, var3 - 8, Math.abs(var53) % 5 == 0 ? var6 : var8);
            }
         }
      }

      for (int var54 = -13; var54 <= 13; var54++) {
         for (int var92 = -23; var92 <= -8; var92++) {
            boolean var121 = Math.abs(var54) <= 7 && var92 >= -21 && var92 <= -11;
            if (!var121) {
               setBlock(var5, var1 + var54, var2 + 12, var3 + var92, var54 % 6 != 0 && var92 % 5 != 0 ? var9 : var6);
            }
         }
      }

      for (int var55 = -21; var55 <= -11; var55++) {
         for (int var93 = 12; var93 <= 15; var93++) {
            setBlock(var5, var1 - 8, var2 + var93, var3 + var55, var93 == 14 ? var7 : var6);
            setBlock(var5, var1 + 8, var2 + var93, var3 + var55, var93 == 14 ? var7 : var6);
         }
      }

      for (int var56 = -8; var56 <= 8; var56++) {
         for (int var94 = 12; var94 <= 15; var94++) {
            setBlock(var5, var1 + var56, var2 + var94, var3 - 22, var94 == 14 ? var7 : var6);
            setBlock(var5, var1 + var56, var2 + var94, var3 - 10, var94 == 14 ? var7 : var6);
         }
      }

      for (int var57 = -8; var57 <= 8; var57++) {
         for (int var95 = -22; var95 <= -10; var95++) {
            setBlock(var5, var1 + var57, var2 + 16, var3 + var95, Math.abs(var57) != 8 && var95 != -22 && var95 != -10 ? var9 : var6);
         }
      }

      setBlock(var5, var1, var2 + 15, var3 - 16, var7);
      setBlock(var5, var1 + 1, var2 + 15, var3 - 16, var7);

      for (int var139 : new int[]{-10, -6, 6, 10}) {
         for (int var156 : new int[]{-11, -20}) {
            for (int var158 = 1; var158 <= 10; var158++) {
               setBlock(var5, var1 + var139, var2 + var158, var3 + var156, var158 != 4 && var158 != 8 ? var6 : var7);
            }

            setBlock(var5, var1 + var139, var2, var3 + var156, var11);
         }
      }

      for (int[] var140 : new int[][]{{-11, -10}, {11, -10}, {-11, -21}, {11, -21}}) {
         setBlock(var5, var1 + var140[0], var2 + 1, var3 + var140[1], "SOUL_LANTERN");
      }

      for (int[] var141 : new int[][]{{-9, -14}, {9, -17}, {-8, -21}, {8, -11}}) {
         setBlock(var5, var1 + var141[0], var2 + 1, var3 + var141[1], "MOSS_CARPET");
      }

      setBlock(var5, var1 - 11, var2, var3 - 17, "MOSS_BLOCK");
      setBlock(var5, var1 - 11, var2 + 1, var3 - 17, "FLOWERING_AZALEA");
      setBlock(var5, var1 + 11, var2, var3 - 13, "MOSS_BLOCK");
      setBlock(var5, var1 + 11, var2 + 1, var3 - 13, "AZALEA");

      for (int[] var142 : new int[][]{{-9, -16}, {9, -16}, {-9, -19}, {9, -19}}) {
         setBlock(var5, var1 + var142[0], var2 + 1, var3 + var142[1], var10);
         setBlock(var5, var1 + var142[0], var2 + 2, var3 + var142[1], (var142[0] + var142[1] & 1) == 0 ? var7 : var11);
      }

      for (int[] var143 : new int[][]{{-6, -14}, {6, -14}, {-6, -20}, {6, -20}}) {
         for (int var149 = 12; var149 >= 9; var149--) {
            setBlock(var5, var1 + var143[0], var2 + var149, var3 + var143[1], "CHAIN");
         }

         setBlock(var5, var1 + var143[0], var2 + 8, var3 + var143[1], "SOUL_LANTERN");
      }

      for (int var63 = -10; var63 <= -7; var63++) {
         for (int var101 = -20; var101 <= -17; var101++) {
            setBlock(var5, var1 + var63, var2 + 1, var3 + var101, var10);
         }
      }

      setBlock(var5, var1 - 8, var2 + 2, var3 - 18, originReliquaryBlock());

      for (int var64 = 7; var64 <= 10; var64++) {
         for (int var102 = -20; var102 <= -17; var102++) {
            setBlock(var5, var1 + var64, var2 + 1, var3 + var102, var10);
         }
      }

      setBlock(var5, var1 + 8, var2 + 2, var3 - 18, "LECTERN");

      for (int var65 = -5; var65 <= 5; var65++) {
         for (int var103 = -21; var103 <= -15; var103++) {
            setBlock(var5, var1 + var65, var2 + 1, var3 + var103, var6);
         }
      }

      for (int var66 = -3; var66 <= 3; var66++) {
         for (int var104 = -20; var104 <= -16; var104++) {
            setBlock(var5, var1 + var66, var2 + 2, var3 + var104, var10);
         }
      }

      for (int var67 = -1; var67 <= 1; var67++) {
         for (int var105 = -19; var105 <= -17; var105++) {
            setBlock(var5, var1 + var67, var2 + 3, var3 + var105, var6);
         }
      }

      setBlock(var5, var1 - 4, var2 + 2, var3 - 18, var7);
      setBlock(var5, var1 + 4, var2 + 2, var3 - 18, var7);
      if (!var4) {
         setBlock(var5, var1, var2 + 4, var3 - 18, foundationBeaconBlock());
      } else {
         setBlock(var5, var1, var2 + 4, var3 - 18, "AIR");
      }

      try {
         ensureProtectors(var0, new int[]{var1, var2, var3}, var12);
      } catch (Throwable var25) {
      }

      F90Sanctuary.postBuild(var0, var1, var2, var3, var4);
   }

   private static void clearVolume(Object var0, int var1, int var2, int var3, int var4, int var5, int var6, int var7, int var8, int var9) throws Exception {
      for (int var10 = var4; var10 <= var5; var10++) {
         for (int var11 = var6; var11 <= var7; var11++) {
            for (int var12 = var8; var12 <= var9; var12++) {
               setBlock(var0, var1 + var10, var2 + var12, var3 + var11, "AIR");
            }
         }
      }
   }

   private static void foundationFloor(Object var0, int var1, int var2, int var3, int var4, int var5, int var6, int var7, Object var8, Object var9) throws Exception {
      for (int var10 = var4; var10 <= var5; var10++) {
         for (int var11 = var6; var11 <= var7; var11++) {
            for (int var12 = -4; var12 <= -1; var12++) {
               setBlock(var0, var1 + var10, var2 + var12, var3 + var11, var8);
            }

            Object var13 = (var10 * 13 + var11 * 7) % 11 == 0 ? var9 : var8;
            setBlock(var0, var1 + var10, var2, var3 + var11, var13);
         }
      }
   }

   private static void ensureSanctuaryBeacon(Object var0, int[] var1) {
      try {
         Object var2 = invokeNoArg(var0, "overworld");
         int[] var3 = new int[]{var1[0], var1[1] + 4, var1[2] - 18};
         Object var4 = invoke(var2, "getBlockState", blockPos(var3[0], var3[1], var3[2]));
         Object var5 = invokeNoArg(var4, "getBlock");
         if (var5 != foundationBeaconBlock()) {
            setBlock(var2, var3[0], var3[1], var3[2], foundationBeaconBlock());
         }
      } catch (Throwable var6) {
      }
   }

   private static boolean isV84StructurePresent(Object var0, int[] var1) {
      try {
         Object var2 = invokeNoArg(var0, "overworld");
         Object var3 = invoke(var2, "getBlockState", blockPos(var1[0] + 1, var1[1] + 15, var1[2] - 16));
         Object var4 = invokeNoArg(var3, "getBlock");
         return var4 == sanctuaryLumenBlock();
      } catch (Throwable var5) {
         return false;
      }
   }

   static boolean isSanctuaryProtectedPos(Object var0, Object var1) {
      try {
         F8SanctuaryEngine.Session var2 = SESSIONS.get(var0);
         if (var2 != null && var2.target != null && var1 != null) {
            int[] var3 = var2.target;
            int var4 = posX(var1) - var3[0];
            int var5 = posY(var1) - var3[1];
            int var6 = posZ(var1) - var3[2];
            return var4 >= -18 && var4 <= 18 && var6 >= -25 && var6 <= 33 && var5 >= -5 && var5 <= 17;
         } else {
            return false;
         }
      } catch (Throwable var7) {
         return false;
      }
   }

   static int[] sealStelePos(int[] var0) {
      return new int[]{var0[0], var0[1] + 2, var0[2] + 29};
   }

   static int[] reliquaryPos(int[] var0) {
      return new int[]{var0[0] - 8, var0[1] + 2, var0[2] - 18};
   }

   static int[] bookPedestalPos(int[] var0) {
      return new int[]{var0[0] + 8, var0[1] + 2, var0[2] - 18};
   }

   static boolean isSealStelePos(Object var0, Object var1) {
      try {
         int[] var2 = sealStelePos(target(var0));
         return posX(var1) == var2[0] && posY(var1) == var2[1] && posZ(var1) == var2[2];
      } catch (Throwable var3) {
         return false;
      }
   }

   static boolean isReliquaryPos(Object var0, Object var1) {
      try {
         int[] var2 = reliquaryPos(target(var0));
         return posX(var1) == var2[0] && posY(var1) == var2[1] && posZ(var1) == var2[2];
      } catch (Throwable var3) {
         return false;
      }
   }

   static boolean isBookPedestalPos(Object var0, Object var1) {
      try {
         int[] var2 = bookPedestalPos(target(var0));
         return posX(var1) == var2[0] && posY(var1) == var2[1] && posZ(var1) == var2[2];
      } catch (Throwable var3) {
         return false;
      }
   }

   static void openThresholdGate(Object var0, int[] var1) {
      try {
         Object var2 = invokeNoArg(var0, "overworld");

         for (int var3 = -3; var3 <= 3; var3++) {
            for (int var4 = 1; var4 <= 6; var4++) {
               setBlock(var2, var1[0] + var3, var1[1] + var4, var1[2] + 22, "AIR");
            }
         }
      } catch (Throwable var5) {
      }
   }

   static void onSealInserted(Object var0, Object var1) {
      try {
         Object var2 = campaign(var0);
         if (completed(var2, "F84_SEAL_INSERTED")) {
            return;
         }

         complete(var2, "F84_SEAL_INSERTED");
         int[] var3 = target(var0);
         openThresholdGate(var0, var3);
         String var4 = var1 == null ? "Vous" : playerName(var1);
         chronicleOnce(
            var0,
            "Le Seuil du Sanctuaire",
            "Les deux Sceaux ont été enchâssés dans les Réceptacles. La matière du Sanctuaire a répondu et le passage s'est ouvert.",
            var4
         );
         List var5 = players(var0);
         history(var5, "§6STÈLE §8• §f" + var4 + " a inséré le Sceau. Le seuil du Sanctuaire s'ouvre.");
         playSanctuarySound(var0, "sanctuary_gate", var3[0], var3[1] + 4, var3[2] + 22, 1.9, 0.58);
         voice(var5, "…Le passage vous reconnaît.");
         objective(var5, "Franchissez le seuil du Sanctuaire · avancez dans le hall d'entrée.");
      } catch (Throwable var6) {
         System.err.println("[REIVAX Alpha 18F.8.4] seal insertion failed: " + var6);
      }
   }

   static void onBookRecovered(Object var0, Object var1) {
      try {
         Object var2 = campaign(var0);
         if (completed(var2, "F84_BOOK_RECOVERED")) {
            return;
         }

         complete(var2, "F84_BOOK_RECOVERED");
         String var3 = var1 == null ? "Vous" : playerName(var1);
         chronicleOnce(
            var0,
            "Le Livre ancien",
            "Un livre préservé dans la chambre de Fondation a été récupéré. Son contenu semble avoir été laissé pour ceux qui atteindraient ce lieu.",
            var3
         );
         history(players(var0), "§6LIVRE §8• §f" + var3 + " a récupéré un ouvrage ancien dans la chambre de Fondation.");
      } catch (Throwable var4) {
         System.err.println("[REIVAX Alpha 18F.8.4] book state failed: " + var4);
      }
   }

   static void onReliquaryClaimed(Object var0, Object var1) {
      try {
         Object var2 = campaign(var0);
         if (completed(var2, "F84_RELIQUARY_CLAIMED")) {
            return;
         }

         complete(var2, "F84_RELIQUARY_CLAIMED");
         String var3 = var1 == null ? "Vous" : playerName(var1);
         chronicleOnce(
            var0,
            "Le Reliquaire",
            "La chute des Protecteurs a déverrouillé un reliquaire secondaire. Il contenait quelques ressources conservées avec soin.",
            var3
         );
         history(players(var0), "§6RELIQUAIRE §8• §fLe Reliquaire du Sanctuaire a été ouvert.");
      } catch (Throwable var4) {
         System.err.println("[REIVAX Alpha 18F.8.4] reliquary state failed: " + var4);
      }
   }

   private static void playAtPlayers(Object var0, List<?> var1, String var2, double var3, double var5) {
      for (Object var8 : var1) {
         try {
            String var9 = playerName(var8);
            runCommand(var0, "execute at " + var9 + " run playsound reivaxmc_progress:" + var2 + " ambient " + var9 + " ~ ~ ~ " + var3 + " " + var5);
         } catch (Throwable var10) {
         }
      }
   }

   private static void openOuterGate(Object var0, int[] var1) {
      try {
         Object var2 = invokeNoArg(var0, "overworld");

         for (int var3 = -2; var3 <= 2; var3++) {
            for (int var4 = 1; var4 <= 5; var4++) {
               setBlock(var2, var1[0] + var3, var1[1] + var4, var1[2] + 7, "AIR");
            }
         }
      } catch (Throwable var5) {
      }
   }

   private static void openFoundationGate(Object var0, int[] var1, int var2) {
      try {
         Object var3 = invokeNoArg(var0, "overworld");
         int var4 = var2 <= 1 ? 2 : (var2 == 2 ? 5 : 7);
         int var5 = var2 <= 1 ? 1 : (var2 == 2 ? 2 : 3);

         for (int var6 = -var5; var6 <= var5; var6++) {
            for (int var7 = 1; var7 <= var4; var7++) {
               setBlock(var3, var1[0] + var6, var1[1] + var7, var1[2] - 7, "AIR");
            }
         }
      } catch (Throwable var8) {
      }
   }

   // Anti-boucle de spawn : un protecteur donné ne peut réapparaître qu'au plus toutes les 5 s.
   // Le temps que la vérification d'existence (hasTaggedEntity) prenne le relais, ça empêche
   // l'entassement d'entités au même endroit (mort « a subi trop de pression » = cramming).
   private static final java.util.Map<String, Long> LAST_SPAWN_MS = new java.util.concurrent.ConcurrentHashMap<>();

   private static boolean canRespawn(String key) {
      long now = System.currentTimeMillis();
      Long last = LAST_SPAWN_MS.get(key);
      if (last != null && now - last < 5000L) {
         return false;
      }
      LAST_SPAWN_MS.put(key, now);
      return true;
   }

   private static void ensureProtectors(Object var0, int[] var1, Object var2) {
      try {
         Object var3 = invokeNoArg(var0, "overworld");
         if (!completed(var2, "F8_GUARD_1_DEFEATED") && !hasTaggedEntity(var3, "reivax_f83_w1") && canRespawn("reivax_f83_w1")) {
            summonProtector(var0, var1[0] - 5, var1[1] + 1, var1[2] + 14, "reivax_f83_w1", "Veilleur du Sanctuaire", false, false);
         }

         if (!completed(var2, "F8_GUARD_2_DEFEATED") && !hasTaggedEntity(var3, "reivax_f83_w2") && canRespawn("reivax_f83_w2")) {
            summonProtector(var0, var1[0] + 5, var1[1] + 1, var1[2] + 14, "reivax_f83_w2", "Veilleur du Sanctuaire", false, true);
         }

         if (!completed(var2, "F82_FOUNDATION_GUARD_1_DEFEATED") && !hasTaggedEntity(var3, "reivax_f83_fg1") && canRespawn("reivax_f83_fg1")) {
            summonProtector(var0, var1[0] - 5, var1[1] + 1, var1[2] - 15, "reivax_f83_fg1", "Gardien de Fondation", true, false);
         }

         if (!completed(var2, "F82_FOUNDATION_GUARD_2_DEFEATED") && !hasTaggedEntity(var3, "reivax_f83_fg2") && canRespawn("reivax_f83_fg2")) {
            summonProtector(var0, var1[0] + 5, var1[1] + 1, var1[2] - 15, "reivax_f83_fg2", "Gardien de Fondation", true, true);
         }

         if (completed(var2, "F8_GUARDS_AWAKENED")) {
            activateProtectorGroup(var0, var1, false, players(var0).size());
         }

         if (completed(var2, "F82_FOUNDATION_GUARDS_AWAKENED")) {
            activateProtectorGroup(var0, var1, true, players(var0).size());
         }
      } catch (Throwable var4) {
         System.err.println("[REIVAX Alpha 18F.8.4] Protector ensure failed: " + var4.getClass().getSimpleName() + ": " + var4.getMessage());
      }
   }

   private static void summonProtector(Object var0, int var1, int var2, int var3, String var4, String var5, boolean var6, boolean var7) throws Exception {
      String var8 = var6 ? "reivax_f83_foundation_guardian" : "reivax_f83_watcher";
      String var9 = "summon minecraft:piglin_brute "
         + var1
         + " "
         + var2
         + " "
         + var3
         + " {Tags:[\"reivax_f8_guardian\",\""
         + var8
         + "\",\""
         + var4
         + "\"],CustomName:'{\"text\":\""
         + var5
         + "\"}',CustomNameVisible:0b,PersistenceRequired:1b,Silent:1b,NoAI:1b,IsImmuneToZombification:1b,CanPickUpLoot:0b}";
      runCommand(var0, var9);
      String var10 = "@e[type=minecraft:piglin_brute,x=" + var1 + ",y=" + var2 + ",z=" + var3 + ",distance=..5,sort=nearest,limit=1]";
      runCommand(var0, "tag " + var10 + " add reivax_f8_guardian");
      runCommand(var0, "tag " + var10 + " add " + var8);
      runCommand(var0, "tag " + var10 + " add " + var4);
      String var11;
      if (var6) {
         var11 = var7 ? "minecraft:iron_axe" : "minecraft:iron_sword";
      } else {
         var11 = var7 ? "minecraft:golden_axe" : "minecraft:iron_sword";
      }

      try {
         runCommand(var0, "item replace entity " + var10 + " weapon.mainhand with " + var11);
      } catch (Throwable var18) {
      }

      int var12 = 1;

      try {
         var12 = Math.max(1, players(var0).size());
      } catch (Throwable var17) {
      }

      double var13 = var6 ? (var12 >= 2 ? 36.0 : 30.0) : (var12 >= 2 ? 24.0 : 18.0);
      runCommand(var0, "attribute " + var10 + " minecraft:generic.max_health base set " + var13);
      runCommand(var0, "attribute " + var10 + " minecraft:generic.follow_range base set 48");
      runCommand(var0, "attribute " + var10 + " minecraft:generic.movement_speed base set " + (var6 ? 0.28 : (var7 ? 0.30 : 0.32)));
      runCommand(var0, "attribute " + var10 + " minecraft:generic.attack_damage base set " + (var6 ? (var7 ? 6.0 : 5.5) : (var7 ? 4.0 : 3.5)));

      try {
         runCommand(var0, "attribute " + var10 + " minecraft:generic.knockback_resistance base set " + (var6 ? 0.30 : 0.10));
      } catch (Throwable var16) {
      }

      runCommand(var0, "data merge entity " + var10 + " {Health:" + var13 + "f,NoAI:1b,Silent:1b,IsImmuneToZombification:1b}");
      F90Sanctuary.tuneProtector(var0, var4, var6, var7);
   }

   private static void activateProtectorGroup(Object var0, int[] var1, boolean var2, int var3) {
      try {
         F8SanctuaryEngine.Session var4 = SESSIONS.computeIfAbsent(var0, var0x -> new F8SanctuaryEngine.Session());
         if (var2 ? var4.guardiansActivatedThisSession : var4.watchersActivatedThisSession) {
            return;
         }

         String[] var5 = var2 ? new String[]{"reivax_f83_fg1", "reivax_f83_fg2"} : new String[]{"reivax_f83_w1", "reivax_f83_w2"};

         for (int var6 = 0; var6 < var5.length; var6++) {
            String var7 = "@e[tag=" + var5[var6] + ",sort=nearest,limit=1]";
            runCommand(var0, "data merge entity " + var7 + " {NoAI:0b,Silent:1b,IsImmuneToZombification:1b}");
            double var8 = var2 ? (var3 >= 2 ? 36.0 : 30.0) : (var3 >= 2 ? 24.0 : 18.0);
            double var10;
            if (var2) {
               var10 = var6 == 1 ? (var3 >= 2 ? 6.5 : 5.5) : (var3 >= 2 ? 6.0 : 5.0);
            } else {
               var10 = var6 == 1 ? (var3 >= 2 ? 4.0 : 3.5) : (var3 >= 2 ? 3.5 : 3.0);
            }

            runCommand(var0, "attribute " + var7 + " minecraft:generic.max_health base set " + var8);
            runCommand(var0, "attribute " + var7 + " minecraft:generic.attack_damage base set " + var10);
            if (var2) {
               try {
                  runCommand(var0, "attribute " + var7 + " minecraft:generic.scale base set 1.70");
               } catch (Throwable var14) {
               }

               try {
                  runCommand(var0, "effect give " + var7 + " minecraft:resistance 999999 0 true");
               } catch (Throwable var13) {
               }
            }
         }

         if (var2) {
            var4.guardiansActivatedThisSession = true;
         } else {
            var4.watchersActivatedThisSession = true;
         }
      } catch (Throwable var15) {
         System.err.println("[REIVAX Alpha 18F.8.4] Protector activation failed: " + var15.getClass().getSimpleName());
      }
   }

   private static void playSanctuarySound(Object var0, String var1, int var2, int var3, int var4, double var5, double var7) {
      try {
         runCommand(var0, "playsound reivaxmc_progress:" + var1 + " hostile @a " + var2 + " " + var3 + " " + var4 + " " + var5 + " " + var7 + " 0");
      } catch (Throwable var10) {
      }
   }

   private static int runCommand(Object var0, String var1) throws Exception {
      Object var2 = invokeNoArg(var0, "getCommands");
      Object var3 = invokeNoArg(var0, "createCommandSourceStack");

      try {
         var3 = invokeNoArg(var3, "withSuppressedOutput");
      } catch (Throwable var7) {
      }

      try {
         return invoke(var2, "performPrefixedCommand", var3, var1) instanceof Number var9 ? var9.intValue() : 1;
      } catch (Throwable var8) {
         return invoke(var2, "performCommand", var3, var1) instanceof Number var6 ? var6.intValue() : 1;
      }
   }

   private static boolean hasTaggedEntity(Object var0, String var1) {
      try {
         Object var2 = null;

         try {
            var2 = invokeNoArg(var0, "getAllEntities");
         } catch (Throwable var11) {
         }

         if (var2 == null) {
            try {
               Object var3 = invokeNoArg(var0, "getEntities");
               var2 = invokeNoArg(var3, "getAll");
            } catch (Throwable var10) {
            }
         }

         if (var2 instanceof Iterable var14) {
            Iterator var4 = var14.iterator();

            while (true) {
               if (!var4.hasNext()) {
                  return false;
               }

               Object var5 = var4.next();
               if (invokeNoArg(var5, "getTags") instanceof Set var7 && var7.contains(var1)) {
                  try {
                     if (!(invokeNoArg(var5, "isAlive") instanceof Boolean var9) || var9) {
                        break;
                     }
                  } catch (Throwable var12) {
                     break;
                  }
               }
            }

            return true;
         }
      } catch (Throwable var13) {
      }

      return false;
   }

   static Object sanctuaryStoneBlock() throws Exception {
      Object var0 = staticField("fr.reivaxmc.progress.ReivaxMCProgress", "MEMORIAL_PLAQUE");
      return invokeNoArg(var0, "get");
   }

   static Object sanctuaryLumenBlock() throws Exception {
      Object var0 = staticField("fr.reivaxmc.progress.ReivaxMCProgress", "FRAGMENT_ALTAR");
      return invokeNoArg(var0, "get");
   }

   static Object foundationBeaconBlock() throws Exception {
      Object var0 = staticField("fr.reivaxmc.progress.ReivaxMCProgress", "FOUNDATION_BEACON");
      return invokeNoArg(var0, "get");
   }

   static Object foundationBeaconItem() throws Exception {
      Object var0 = staticField("fr.reivaxmc.progress.ReivaxMCProgress", "FOUNDATION_BEACON_ITEM");
      return invokeNoArg(var0, "get");
   }

   static Object originSealItem() throws Exception {
      Object var0 = staticField("fr.reivaxmc.progress.ReivaxMCProgress", "ORIGIN_SEAL");
      return invokeNoArg(var0, "get");
   }

   static Object destinyBookItem() throws Exception {
      Object var0 = staticField("fr.reivaxmc.progress.ReivaxMCProgress", "DESTINY_BOOK");
      return invokeNoArg(var0, "get");
   }

   static Object originMatrixBlock() throws Exception {
      Object var0 = staticField("fr.reivaxmc.progress.ReivaxMCProgress", "ORIGIN_MATRIX");
      return invokeNoArg(var0, "get");
   }

   static Object originReliquaryBlock() throws Exception {
      Object var0 = staticField("fr.reivaxmc.progress.ReivaxMCProgress", "ORIGIN_RELIQUARY");
      return invokeNoArg(var0, "get");
   }

   static void setBlock(Object var0, int var1, int var2, int var3, String var4) throws Exception {
      Object var5 = staticField("net.minecraft.world.level.block.Blocks", var4);
      setBlock(var0, var1, var2, var3, var5);
   }

   static void setBlock(Object var0, int var1, int var2, int var3, Object var4) throws Exception {
      Object var5 = invokeNoArg(var4, "defaultBlockState");
      invoke(var0, "setBlock", blockPos(var1, var2, var3), var5, 3);
   }

   static Object blockPos(int var0, int var1, int var2) throws Exception {
      Class var3 = Class.forName("net.minecraft.core.BlockPos");
      Constructor var4 = var3.getConstructor(int.class, int.class, int.class);
      return var4.newInstance(var0, var1, var2);
   }

   static int posX(Object var0) throws Exception {
      return number(invokeNoArg(var0, "getX")).intValue();
   }

   static int posY(Object var0) throws Exception {
      return number(invokeNoArg(var0, "getY")).intValue();
   }

   static int posZ(Object var0) throws Exception {
      return number(invokeNoArg(var0, "getZ")).intValue();
   }

   static double distance2(Object var0, Object var1) throws Exception {
      double var2 = number(invokeNoArg(var0, "getX")).doubleValue() - ((double)posX(var1) + 0.5);
      double var4 = number(invokeNoArg(var0, "getY")).doubleValue() - ((double)posY(var1) + 0.5);
      double var6 = number(invokeNoArg(var0, "getZ")).doubleValue() - ((double)posZ(var1) + 0.5);
      return var2 * var2 + var4 * var4 + var6 * var6;
   }

   static boolean isSanctuaryBeaconPos(Object var0, Object var1) throws Exception {
      int[] var2 = beaconPos(var0);
      return posX(var1) == var2[0] && posY(var1) == var2[1] && posZ(var1) == var2[2];
   }

   static boolean tooCloseToSanctuary(Object var0, Object var1) throws Exception {
      int[] var2 = target(var0);
      double var3 = (double)(posX(var1) - var2[0]);
      double var5 = (double)(posZ(var1) - var2[2]);
      return var3 * var3 + var5 * var5 < 4096.0;
   }

   static String playerName(Object var0) throws Exception {
      try {
         Object var1 = invokeNoArg(var0, "getGameProfile");
         return String.valueOf(invokeNoArg(var1, "getName"));
      } catch (Throwable var3) {
         Object var2 = invokeNoArg(var0, "getUUID");
         return String.valueOf(var2);
      }
   }

   static int worldDay(Object var0) throws Exception {
      Object var1 = invokeNoArg(var0, "overworld");
      return (int)(number(invokeNoArg(var1, "getDayTime")).longValue() / 24000L) + 1;
   }

   static void chronicleOnce(Object var0, String var1, String var2, String var3) {
      try {
         Object var4 = campaign(var0);
         String var5 = String.valueOf(invokeNoArg(var4, "timelinePacket"));
         if (var5 != null && var5.contains("¦" + var1 + "¦")) {
            return;
         }

         invoke(var4, "addTimeline", worldDay(var0), var3 != null && !var3.isBlank() ? var3 : "Monde", var1, var2 == null ? "" : var2);
      } catch (Throwable var6) {
         System.err.println("[REIVAX Alpha 18F.8.4] Chronology append failed: " + var6.getClass().getSimpleName());
      }
   }

   static boolean boolInvoke(Object var0, String var1) throws Exception {
      if (invokeNoArg(var0, var1) instanceof Boolean var3 && var3) {
         return true;
      }

      return false;
   }

   static Object staticField(String var0, String var1) throws Exception {
      Class var2 = Class.forName(var0);
      Field var3 = var2.getField(var1);
      return var3.get(null);
   }

   static Object fieldValue(Object var0, String var1) throws Exception {
      if (var0 == null) {
         throw new NullPointerException("target field " + var1);
      } else {
         for (Class var2 = var0.getClass(); var2 != null; var2 = var2.getSuperclass()) {
            try {
               Field var3 = var2.getDeclaredField(var1);
               var3.setAccessible(true);
               return var3.get(var0);
            } catch (NoSuchFieldException var4) {
            }
         }

         throw new NoSuchFieldException(var0.getClass().getName() + "." + var1);
      }
   }

   static Object invokeNoArg(Object var0, String var1) throws Exception {
      return invoke(var0, var1);
   }

   static Object invoke(Object var0, String var1, Object... var2) throws Exception {
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

   static Object callStatic(String var0, String var1, Object... var2) throws Exception {
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
            || var0 == byte.class && Number.class.isAssignableFrom(var2)
            || var0 == short.class && Number.class.isAssignableFrom(var2)
            || var0 == int.class && Number.class.isAssignableFrom(var2)
            || var0 == long.class && Number.class.isAssignableFrom(var2)
            || var0 == float.class && Number.class.isAssignableFrom(var2)
            || var0 == double.class && Number.class.isAssignableFrom(var2)
            || var0 == char.class && var2 == Character.class;
      }
   }

   static Number number(Object var0) {
      if (var0 instanceof Number) {
         return (Number)var0;
      } else {
         throw new IllegalArgumentException("Expected number: " + var0);
      }
   }

   private static long mix(long var0) {
      var0 ^= var0 >>> 33;
      var0 *= -49064778989728563L;
      var0 ^= var0 >>> 33;
      var0 *= -4265267296055464877L;
      return var0 ^ var0 >>> 33;
   }

   private static final class Candidate {
      final int x;
      final int y;
      final int z;
      final int variation;
      int score;

      Candidate(int var1, int var2, int var3, int var4) {
         this.x = var1;
         this.y = var2;
         this.z = var3;
         this.variation = var4;
      }
   }

   private static final class Session {
      int[] target;
      long introTicks;
      long guardClearTicks;
      long beaconHintTicks;
      long innerGateTicks;
      long presenceSoundTicks;
      long guardianPresenceSoundTicks;
      int innerGateStage;
      boolean beaconPlacementHint;
      boolean innerGateOpening;
      boolean watchersActivatedThisSession;
      boolean guardiansActivatedThisSession;
   }
}
