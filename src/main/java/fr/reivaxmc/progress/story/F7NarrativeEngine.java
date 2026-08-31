package fr.reivaxmc.progress.story;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.RecordComponent;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

public final class F7NarrativeEngine {
   private static final double TRACE_SCENE_RADIUS = 14.0;
   private static final double TRACE_SCENE_RADIUS_SQ = 196.0;
   private static final long STEP = 5L;
   private static final long VOICE_AT = 40L;
   private static final long FOLD_AT = 100L;
   private static final long SEAL_AT = 180L;
   private static final long MIN_PREP_TICKS = 200L;
   private static final int SCENE_CONTACT = 0;
   private static final int SCENE_SEAL = 1;
   private static final int SCENE_PREP = 2;
   private static final int SCENE_NIGHT = 3;
   private static final int SCENE_FOYER = 4;
   private static final Map<Object, F7NarrativeEngine.Session> SESSIONS = Collections.synchronizedMap(new WeakHashMap<>());
   private static final AtomicLong UI_SEQUENCE = new AtomicLong();
   private static volatile String legacyObjective = "§6OBJECTIF PRINCIPAL §8• §fRestez près de la Trace · observez ce qui se produit.";

   private F7NarrativeEngine() {
   }

   public static void tick(Object var0) {
      if (var0 != null) {
         try {
            int var1 = number(invokeNoArg(var0, "getTickCount")).intValue();
            if (var1 % 5 != 0) {
               return;
            }

            Object var2 = callStatic("fr.reivaxmc.progress.story.StoryModeGate18F", "state", var0);
            Object var3 = invokeNoArg(var2, "snapshot");
            if (!bool(var3, "managed") || !bool(var3, "started") || !bool(var3, "tracePlaced") || !bool(var3, "traceExamined")) {
               return;
            }

            List var4 = players(var0);
            if (var4.isEmpty()) {
               return;
            }

            int var5 = integer(var3, "traceX");
            int var6 = integer(var3, "traceY");
            int var7 = integer(var3, "traceZ");
            String var8 = string(var3, "traceExaminedBy");
            F7NarrativeEngine.Session var9 = SESSIONS.computeIfAbsent(var0, var0x -> new F7NarrativeEngine.Session());
            if (!var9.initialized) {
               initialize(var0, var4, var9, var8);
            }

            switch (var9.scene) {
               case 0:
                  tickContact(var0, var4, var9, var8, var5, var6, var7);
                  break;
               case 1:
                  tickSeal(var0, var4, var9, var5, var6, var7);
                  break;
               case 2:
                  tickPrep(var0, var4, var9, var5, var6, var7);
                  break;
               case 3:
                  tickNight(var0, var4, var9);
                  break;
               case 4:
                  F8SanctuaryEngine.tick(var0, var4, var5, var6, var7);
            }
         } catch (Throwable var10) {
            System.err.println("[REIVAX Alpha 18F.8.1] F7NarrativeEngine.tick failed: " + var10);
         }
      }
   }

   public static void devReset(Object var0) {
      if (var0 != null) {
         SESSIONS.remove(var0);
      }

      legacyObjective = "§6OBJECTIF PRINCIPAL §8• §fRestez près de la Trace · observez ce qui se produit.";
   }

   public static String legacyObjective() {
      return legacyObjective;
   }

   private static void initialize(Object var0, List<?> var1, F7NarrativeEngine.Session var2, String var3) throws Exception {
      var2.initialized = true;
      var2.actorId = var3 == null ? "" : var3;
      var2.actorName = findPlayerName(var1, var2.actorId);
      boolean var4 = resonancePersisted(var0);
      Object var5 = originSealItem();
      Object var6 = findSealHolder(var1, var5);
      if (var4) {
         var2.sealClaimed = true;
         long var7 = dayTime(var0);
         long var9 = floorMod(var7, 24000L);
         if (var9 >= 12000L) {
            var2.scene = 3;
            var2.resonanceTriggered = true;
            setObjective("§6OBJECTIF PRINCIPAL §8• §fSurvivez à votre première nuit · tenez jusqu'au lever du jour.");
         } else {
            var2.scene = 4;
            var2.resonanceTriggered = true;
            var2.dawnCompleted = true;
            setObjective("§6OBJECTIF PRINCIPAL §8• §fLe Sceau réagit à nouveau · observez-le.");
         }
      } else if (var6 != null) {
         var2.sealClaimed = true;
         var2.scene = 2;
         var2.actorName = playerName(var6);
         setObjective("§6OBJECTIF PRINCIPAL §8• §fPréparez-vous avant la nuit · bois · nourriture · abri.");
      } else {
         var2.scene = 0;
         setObjective("§6OBJECTIF PRINCIPAL §8• §fRestez près de la Trace · ne vous éloignez pas.");
      }
   }

   private static void tickContact(Object var0, List<?> var1, F7NarrativeEngine.Session var2, String var3, int var4, int var5, int var6) throws Exception {
      if (!var2.contactAnnounced) {
         var2.contactAnnounced = true;
         if (var2.actorName == null || var2.actorName.isBlank()) {
            var2.actorName = "L'autre joueur";
         }

         chronicleOnce(
            var0,
            "F72_CONTACT",
            var2.actorName,
            "Premier contact avec la Trace",
            var2.actorName + " a établi le premier contact. La matière inconnue a immédiatement réagi à sa présence."
         );

         for (Object var8 : var1) {
            String var9 = playerId(var8);
            if (var9.equals(var3)) {
               message(var8, "§6CONTACT §8• §fVous avez touché la Trace.", false);
            } else {
               message(
                  var8, "§6CONTACT §8• §f" + var2.actorName + " a établi le premier contact. La matière inconnue a immédiatement réagi à sa présence.", false
               );
            }
         }
      }

      int var11 = countNear(var1, var4, var5, var6);
      int var12 = var1.size();
      if (var11 < var12) {
         setObjective("§6OBJECTIF PRINCIPAL §8• §fRegroupez-vous près de la Trace · " + var11 + "/" + var12 + " présents.");
         if (!var2.wasPaused) {
            var2.wasPaused = true;
         }
      } else {
         if (var2.wasPaused) {
            var2.wasPaused = false;
         }

         setObjective("§6OBJECTIF PRINCIPAL §8• §fRestez près de la Trace · observez ce qui se produit.");
         var2.nearTicks += 5L;
         if (!var2.voiceSpoken && var2.nearTicks >= 40L) {
            var2.voiceSpoken = true;
            pulse(var0, (double)var4, (double)var5 + 2.8, (double)var6, 32, 18);
            playTraceSound(var1, 0.7F, 0.86F);
            voice(var0, var1, "…Vous m'entendez ?");
         }

         if (!var2.foldSpoken && var2.nearTicks >= 100L) {
            var2.foldSpoken = true;
            pulse(var0, (double)var4, (double)var5 + 3.5, (double)var6, 52, 26);
            chronicleOnce(
               var0,
               "F72_TRACE_FOLD",
               "La Trace",
               "Réaction de la Trace",
               "Sous vos yeux, sa matière s'est repliée vers un point unique, comme si elle répondait à une règle invisible."
            );
            broadcast(var1, "§6TRACE §8• §fLa matière se replie vers un point unique.", false);
         }

         if (!var2.sealSpawned && var2.nearTicks >= 180L) {
            var2.sealSpawned = true;
            boolean var13 = spawnSeal(var0, var4, var5, var6);
            pulse(var0, (double)var4 + 1.7, (double)var5 + 2.1, (double)var6 + 0.3, 78, 36);
            playTraceSound(var1, 0.95F, 1.12F);
            chronicleOnce(
               var0,
               "F72_SEAL_BORN",
               "La Trace",
               "Naissance du Sceau",
               "Une partie de la Trace s'est détachée puis condensée en un objet impossible : le Sceau des Origines."
            );
            voice(var0, var1, "Prenez-le.");
            if (var13) {
               broadcast(var1, "§6SCEAU §8• §fQuelque chose vient de se détacher de la Trace.", false);
            } else {
               Object var10 = actorOrFirst(var1, var3);
               giveSeal(var10);
               broadcast(var1, "§6SCEAU §8• §fLa matière s'est condensée dans votre inventaire.", false);
            }

            var2.scene = 1;
            setObjective("§6OBJECTIF PRINCIPAL §8• §fRécupérez le Sceau · il se trouve au pied de la Trace.");
         }
      }
   }

   private static void tickSeal(Object var0, List<?> var1, F7NarrativeEngine.Session var2, int var3, int var4, int var5) throws Exception {
      Object var6 = originSealItem();
      Object var7 = findSealHolder(var1, var6);
      if (var7 == null) {
         int var11 = countNear(var1, var3, var4, var5);
         if (var11 == 0) {
            setObjective("§6OBJECTIF PRINCIPAL §8• §fRetournez à la Trace et récupérez le Sceau.");
         } else {
            setObjective("§6OBJECTIF PRINCIPAL §8• §fRécupérez le Sceau · il se trouve au pied de la Trace.");
         }
      } else {
         var2.sealClaimed = true;
         var2.scene = 2;
         var2.survivalTicks = 0L;
         String var8 = playerName(var7);
         chronicleOnce(
            var0,
            "F72_SEAL_CLAIMED",
            var8,
            "Sceau récupéré",
            "Le Sceau né de la Trace a été récupéré. Pour la première fois, vous emportez avec vous une part de ce phénomène."
         );

         for (Object var10 : var1) {
            if (var10 == var7) {
               message(var10, "§6SCEAU §8• §fVous avez récupéré le Sceau.", false);
            } else {
               message(var10, "§6SCEAU §8• §f" + var8 + " a récupéré le Sceau.", false);
            }
         }

         voice(var0, var1, "Gardez-le.");
         setObjective("§6OBJECTIF PRINCIPAL §8• §fPréparez-vous avant la nuit · bois · nourriture · abri.");
      }
   }

   private static void tickPrep(Object var0, List<?> var1, F7NarrativeEngine.Session var2, int var3, int var4, int var5) throws Exception {
      var2.survivalTicks += 5L;
      setObjective("§6OBJECTIF PRINCIPAL §8• §fPréparez-vous avant la nuit · bois · nourriture · abri.");
      long var6 = dayTime(var0);
      long var8 = floorMod(var6, 24000L);
      if (!var2.duskHint && var8 >= 10000L && var8 < 12000L) {
         var2.duskHint = true;
      }

      if (!var2.resonanceTriggered && var2.survivalTicks >= 200L && var8 >= 12000L) {
         var2.resonanceTriggered = true;
         var2.scene = 3;
         pulseAroundPlayers(var0, var1);
         playTraceSound(var1, 0.72F, 0.72F);
         chronicleOnce(
            var0,
            "F72_FIRST_RESONANCE",
            "Le Sceau",
            "Première Résonance",
            "Au cœur de la première nuit, le Sceau s'est mis à vibrer. Quelque chose, très loin, semblait lui répondre."
         );
         broadcast(var1, "§6RÉSONANCE §8• §fLe Sceau se met à vibrer.", false);
         broadcast(var1, "§6RÉSONANCE §8• §fQuelque chose répond, très loin.", false);
         setObjective("§6OBJECTIF PRINCIPAL §8• §fSurvivez à votre première nuit · tenez jusqu'au lever du jour.");
         publishResonance(var0, var2.actorId);
      }
   }

   private static void tickNight(Object var0, List<?> var1, F7NarrativeEngine.Session var2) throws Exception {
      setObjective("§6OBJECTIF PRINCIPAL §8• §fSurvivez à votre première nuit · tenez jusqu'au lever du jour.");
      long var3 = dayTime(var0);
      long var5 = floorMod(var3, 24000L);
      if (!var2.dawnCompleted && var5 < 1000L) {
         var2.dawnCompleted = true;
         var2.scene = 4;
         chronicleOnce(
            var0,
            "F72_FIRST_NIGHT",
            "Vous",
            "Première nuit traversée",
            "La première nuit s'est achevée. Vous avez tenu jusqu'au lever du jour, tandis que la Résonance demeurait inexpliquée."
         );
         broadcast(var1, "§6OBJECTIF ACCOMPLI §8• §fVous avez traversé votre première nuit.", false);
         setObjective("§6OBJECTIF PRINCIPAL §8• §fLe Sceau réagit à nouveau · observez-le.");
      }
   }

   private static void setObjective(String var0) {
      legacyObjective = var0;
   }

   public static void setCurrentObjective(String var0) {
      if (var0 != null && !var0.isBlank()) {
         legacyObjective = var0;
      }
   }

   private static int countNear(List<?> var0, int var1, int var2, int var3) throws Exception {
      int var4 = 0;
      double var5 = (double)var1 + 0.5;
      double var7 = (double)var2 + 2.0;
      double var9 = (double)var3 + 0.5;

      for (Object var12 : var0) {
         double var13 = number(invokeNoArg(var12, "getX")).doubleValue() - var5;
         double var15 = number(invokeNoArg(var12, "getY")).doubleValue() - var7;
         double var17 = number(invokeNoArg(var12, "getZ")).doubleValue() - var9;
         if (var13 * var13 + var15 * var15 + var17 * var17 <= 196.0) {
            var4++;
         }
      }

      return var4;
   }

   private static List<?> players(Object var0) throws Exception {
      Object var1 = invokeNoArg(var0, "getPlayerList");
      return invokeNoArg(var1, "getPlayers") instanceof List var3 ? var3 : List.of();
   }

   private static Object actorOrFirst(List<?> var0, String var1) throws Exception {
      for (Object var3 : var0) {
         if (playerId(var3).equals(var1)) {
            return var3;
         }
      }

      return var0.isEmpty() ? null : var0.get(0);
   }

   private static String findPlayerName(List<?> var0, String var1) throws Exception {
      for (Object var3 : var0) {
         if (playerId(var3).equals(var1)) {
            return playerName(var3);
         }
      }

      return "";
   }

   private static String playerId(Object var0) throws Exception {
      Object var1 = invokeNoArg(var0, "getUUID");
      return var1 == null ? "" : var1.toString();
   }

   private static String playerName(Object var0) throws Exception {
      try {
         Object var1 = invokeNoArg(var0, "getGameProfile");
         Object var2 = invokeNoArg(var1, "getName");
         return String.valueOf(var2);
      } catch (Throwable var3) {
         return playerId(var0);
      }
   }

   public static void routeStoryMessage(Object var0, String var1, boolean var2) {
      if (var0 != null && var1 != null) {
         try {
            String var3 = stripLegacy(var1).trim();
            if (var2) {
               if (var3.startsWith("OBJECTIF PRINCIPAL")) {
                  sendUiToPlayer(var0, "F71_OBJECTIVE", "", objectiveBody(var3), "F71_OBJECTIVE");
               } else if (!var3.isBlank()) {
                  sendUiToPlayer(var0, "F71_HINT", "", var3, "F71_HINT:" + UI_SEQUENCE.incrementAndGet());
               }

               return;
            }

            if (var3.startsWith("[LA VOIX]")) {
               String var4 = var3.substring("[LA VOIX]".length()).trim();
               sendUiToPlayer(var0, "NARRATOR_WHISPER", "", var4, "F71_VOICE:" + UI_SEQUENCE.incrementAndGet());
               return;
            }

            if (var3.startsWith("OBJECTIF MIS À JOUR") || var3.startsWith("NOUVEL OBJECTIF")) {
               return;
            }

            if (var3.startsWith("TRACE") && var3.contains("contact a déclenché une réaction")) {
               return;
            }

            if (isStoryHistory(var3)) {
               sendUiToPlayer(var0, "F71_HISTORY", "", var1, "F71_HISTORY:" + UI_SEQUENCE.incrementAndGet());
               return;
            }

            directClientMessage(var0, var1, false);
         } catch (Throwable var6) {
            try {
               directClientMessage(var0, qaFallbackText(var0, var1), var2);
            } catch (Throwable var5) {
               System.err.println("[REIVAX Alpha 18F.8.1] message router failed: " + var6);
            }
         }
      }
   }

   private static void message(Object var0, String var1, boolean var2) throws Exception {
      routeStoryMessage(var0, var1, var2);
   }

   private static void broadcast(List<?> var0, String var1, boolean var2) throws Exception {
      for (Object var4 : var0) {
         message(var4, var1, var2);
      }
   }

   private static void voice(Object var0, List<?> var1, String var2) throws Exception {
      for (Object var4 : var1) {
         try {
            sendUiToPlayer(var4, "NARRATOR_WHISPER", "", var2, "F71_VOICE:" + UI_SEQUENCE.incrementAndGet());
         } catch (Throwable var8) {
            try {
               directClientMessage(var4, qaFallbackText(var4, "§8[§fLA VOIX§8] §f" + var2), false);
            } catch (Throwable var7) {
            }
         }
      }
   }

   private static String qaFallbackText(Object var0, String var1) {
      try {
         Object var2 = invokeNoArg(var0, "getServer");
         if (F81DevTools.isQaAntiSpoil(var2)) {
            String var3 = stripLegacy(var1).trim();
            if (var3.startsWith("[LA VOIX]") || isStoryHistory(var3)) {
               return "§8[QA] §7Événement narratif déclenché ✓";
            }
         }
      } catch (Throwable var4) {
      }

      return var1;
   }

   private static boolean isStoryHistory(String var0) {
      return var0.startsWith("TRACE")
         || var0.startsWith("CONTACT")
         || var0.startsWith("SCEAU")
         || var0.startsWith("RÉSONANCE")
         || var0.startsWith("REIVAX")
         || var0.startsWith("OBJECTIF ACCOMPLI")
         || var0.startsWith("SANCTUAIRE")
         || var0.startsWith("VEILLEURS")
         || var0.startsWith("BORNE")
         || var0.startsWith("FOYER");
   }

   private static String objectiveBody(String var0) {
      String var1 = var0;
      if (var0.startsWith("OBJECTIF PRINCIPAL")) {
         var1 = var0.substring("OBJECTIF PRINCIPAL".length()).trim();
      }

      while (var1.startsWith("•") || var1.startsWith("·") || var1.startsWith("-") || var1.startsWith(":")) {
         var1 = var1.substring(1).trim();
      }

      return var1;
   }

   private static String stripLegacy(String var0) {
      if (var0 != null && !var0.isEmpty()) {
         StringBuilder var1 = new StringBuilder(var0.length());

         for (int var2 = 0; var2 < var0.length(); var2++) {
            char var3 = var0.charAt(var2);
            if (var3 == 167 && var2 + 1 < var0.length()) {
               var2++;
            } else {
               var1.append(var3);
            }
         }

         return var1.toString();
      } else {
         return "";
      }
   }

   public static void pushUi(Object var0, String var1, String var2) {
      if (var0 != null && var1 != null) {
         try {
            sendUiToPlayer(var0, var1, "", var2 == null ? "" : var2, "F8:" + var1 + ":" + UI_SEQUENCE.incrementAndGet());
         } catch (Throwable var4) {
            System.err.println("[REIVAX Alpha 18F.8.1] UI bridge failed: " + var4.getClass().getSimpleName());
         }
      }
   }

   private static void sendUiToPlayer(Object var0, String var1, String var2, String var3, String var4) throws Exception {
      Object var5 = invokeNoArg(var0, "getServer");
      if (var5 != null) {
         Object var6 = callStatic("fr.reivaxmc.progress.progression.CampaignSavedData", "get", var5);
         Object var7 = callStatic(
            "fr.reivaxmc.progress.network.ProgressNetworking",
            "packet",
            var0,
            var6,
            var4 != null && !var4.isBlank() ? var4 : "F71:" + UI_SEQUENCE.incrementAndGet(),
            var1,
            var2 == null ? "" : var2,
            var3 == null ? "" : var3,
            0
         );
         var7 = forceRecordBoolean(var7, "introCompleted", true);
         callStatic("fr.reivaxmc.progress.network.CompatPacketSender18F", "sendToPlayer", var0, var7);
      }
   }

   private static Object forceRecordBoolean(Object var0, String var1, boolean var2) {
      if (var0 == null) {
         return null;
      } else {
         try {
            Class var3 = var0.getClass();
            RecordComponent[] var4 = var3.getRecordComponents();
            if (var4 != null && var4.length != 0) {
               Class[] var5 = new Class[var4.length];
               Object[] var6 = new Object[var4.length];
               boolean var7 = false;

               for (int var8 = 0; var8 < var4.length; var8++) {
                  var5[var8] = var4[var8].getType();
                  var6[var8] = var4[var8].getAccessor().invoke(var0);
                  if (var4[var8].getName().equals(var1) && (var5[var8] == boolean.class || var5[var8] == Boolean.class)) {
                     var6[var8] = var2;
                     var7 = true;
                  }
               }

               if (!var7) {
                  return var0;
               } else {
                  Constructor var10 = var3.getDeclaredConstructor(var5);
                  var10.setAccessible(true);
                  return var10.newInstance(var6);
               }
            } else {
               return var0;
            }
         } catch (Throwable var9) {
            return var0;
         }
      }
   }

   private static void directClientMessage(Object var0, String var1, boolean var2) throws Exception {
      Class var3 = Class.forName("net.minecraft.network.chat.Component");
      Method var4 = var3.getMethod("literal", String.class);
      Object var5 = var4.invoke(null, var1);
      invoke(var0, "displayClientMessage", var5, var2);
   }

   private static void chronicleOnce(Object var0, String var1, String var2, String var3, String var4) {
      if (var0 != null && var3 != null && !var3.isBlank()) {
         try {
            Object var5 = callStatic("fr.reivaxmc.progress.progression.CampaignSavedData", "get", var0);
            String var6 = String.valueOf(invokeNoArg(var5, "timelinePacket"));
            String var7 = "¦" + var3 + "¦";
            if (var6 != null && var6.contains(var7)) {
               return;
            }

            int var8 = (int)(dayTime(var0) / 24000L) + 1;
            invoke(var5, "addTimeline", var8, var2 != null && !var2.isBlank() ? var2 : "Monde", var3, var4 == null ? "" : var4);
         } catch (Throwable var9) {
            System.err.println("[REIVAX Alpha 18F.8.1] Chronology append failed " + var1 + ": " + var9.getClass().getSimpleName());
         }
      }
   }

   private static long dayTime(Object var0) throws Exception {
      Object var1 = invokeNoArg(var0, "overworld");
      return number(invokeNoArg(var1, "getDayTime")).longValue();
   }

   private static Object originSealItem() throws Exception {
      Object var0 = staticField("fr.reivaxmc.progress.ReivaxMCProgress", "ORIGIN_SEAL");
      return invokeNoArg(var0, "get");
   }

   private static Object findSealHolder(List<?> var0, Object var1) throws Exception {
      if (var1 == null) {
         return null;
      } else {
         for (Object var3 : var0) {
            Object var4 = invokeNoArg(var3, "getInventory");
            int var5 = number(invokeNoArg(var4, "getContainerSize")).intValue();

            for (int var6 = 0; var6 < var5; var6++) {
               Object var7 = invoke(var4, "getItem", var6);
               if (var7 != null) {
                  Object var8 = invokeNoArg(var7, "getItem");
                  if (var8 == var1) {
                     return var3;
                  }
               }
            }
         }

         return null;
      }
   }

   private static boolean giveSeal(Object var0) throws Exception {
      if (var0 == null) {
         return false;
      } else {
         Object var1 = originSealItem();
         Object var2 = invokeNoArg(var1, "getDefaultInstance");
         Object var3 = invokeNoArg(var0, "getInventory");
         if (invoke(var3, "add", var2) instanceof Boolean var5 && var5) {
            return true;
         }

         return false;
      }
   }

   private static boolean spawnSeal(Object var0, int var1, int var2, int var3) {
      try {
         Object var4 = invokeNoArg(var0, "overworld");
         Object var5 = originSealItem();
         Object var6 = invokeNoArg(var5, "getDefaultInstance");
         Class var7 = Class.forName("net.minecraft.world.entity.item.ItemEntity");
         Object var8 = null;
         double var9 = (double)var1 + 2.35;
         double var11 = (double)var2 + 3.3;
         double var13 = (double)var3 + 0.55;

         for (Constructor var18 : var7.getConstructors()) {
            Class[] var19 = var18.getParameterTypes();
            if (var19.length == 5
               && compatible(var19[0], var4)
               && primitiveDouble(var19[1])
               && primitiveDouble(var19[2])
               && primitiveDouble(var19[3])
               && compatible(var19[4], var6)) {
               var8 = var18.newInstance(var4, var9, var11, var13, var6);
               break;
            }
         }

         if (var8 == null) {
            return false;
         } else {
            try {
               invoke(var8, "setPickUpDelay", 28);
            } catch (Throwable var23) {
            }

            try {
               invokeNoArg(var8, "setUnlimitedLifetime");
            } catch (Throwable var22) {
            }

            try {
               invoke(var8, "setGlowingTag", true);
            } catch (Throwable var21) {
            }

            try {
               invoke(var8, "setDeltaMovement", 0.04, 0.12, 0.0);
            } catch (Throwable var20) {
            }

            if (invoke(var4, "addFreshEntity", var8) instanceof Boolean var26 && !var26) {
               return false;
            }

            return true;
         }
      } catch (Throwable var24) {
         System.err.println("[REIVAX Alpha 18F.8.1] Physical seal spawn failed, using inventory fallback: " + var24);
         return false;
      }
   }

   private static void pulse(Object var0, double var1, double var3, double var5, int var7, int var8) {
      try {
         Object var9 = invokeNoArg(var0, "overworld");
         sendParticles(var9, "WHITE_ASH", var1 + 0.5, var3, var5 + 0.5, var7, 1.15, 1.55, 1.15, 0.015);
         sendParticles(var9, "ELECTRIC_SPARK", var1 + 0.5, var3, var5 + 0.5, var8, 0.85, 1.2, 0.85, 0.035);
         sendParticles(var9, "END_ROD", var1 + 0.5, var3 + 0.25, var5 + 0.5, Math.max(6, var8 / 2), 0.6, 0.9, 0.6, 0.018);
      } catch (Throwable var10) {
         System.err.println("[REIVAX Alpha 18F.8.1] pulse failed: " + var10);
      }
   }

   private static void pulseAroundPlayers(Object var0, List<?> var1) {
      try {
         Object var2 = invokeNoArg(var0, "overworld");

         for (Object var4 : var1) {
            double var5 = number(invokeNoArg(var4, "getX")).doubleValue();
            double var7 = number(invokeNoArg(var4, "getY")).doubleValue() + 1.0;
            double var9 = number(invokeNoArg(var4, "getZ")).doubleValue();
            sendParticles(var2, "WHITE_ASH", var5, var7, var9, 22, 1.4, 1.4, 1.4, 0.012);
            sendParticles(var2, "ELECTRIC_SPARK", var5, var7, var9, 13, 0.8, 1.1, 0.8, 0.025);
         }
      } catch (Throwable var11) {
         System.err.println("[REIVAX Alpha 18F.8.1] player pulse failed: " + var11);
      }
   }

   private static void sendParticles(
      Object var0, String var1, double var2, double var4, double var6, int var8, double var9, double var11, double var13, double var15
   ) throws Exception {
      Object var17 = staticField("net.minecraft.core.particles.ParticleTypes", var1);
      invoke(var0, "sendParticles", var17, var2, var4, var6, var8, var9, var11, var13, var15);
   }

   private static void playTraceSound(List<?> var0, float var1, float var2) {
      try {
         Object var3 = staticField("fr.reivaxmc.progress.block.Alpha18FContent", "TRACE_INTERACT_SOUND");
         Object var4 = staticField("net.minecraft.sounds.SoundSource", "MASTER");
         if (var3 == null || var4 == null) {
            return;
         }

         for (Object var6 : var0) {
            try {
               invoke(var6, "playNotifySound", var3, var4, var1, var2);
            } catch (Throwable var8) {
            }
         }
      } catch (Throwable var9) {
      }
   }

   private static boolean resonancePersisted(Object var0) {
      try {
         Object var1 = callStatic("fr.reivaxmc.progress.story.CampaignStateData18", "getForServer", var0);
         Field var2 = var1.getClass().getDeclaredField("age1Resonance");
         var2.setAccessible(true);
         return var2.getBoolean(var1);
      } catch (Throwable var3) {
         return false;
      }
   }

   private static void publishResonance(Object var0, String var1) {
      try {
         String var2 = var1 != null && !var1.isBlank() ? var1 : "WORLD";
         Object var3 = callStatic(
            "fr.reivaxmc.progress.story.StoryFact",
            "identified",
            "AGE1_F7:FIRST_NIGHT_RESONANCE",
            "AGE1_RESONANCE_MANIFESTED",
            "AGE1.F7.FIRST_NIGHT",
            var2,
            Map.of("source", "origin_seal", "phase", "first_night")
         );
         Supplier var4 = () -> {
            try {
               return callStatic("fr.reivaxmc.progress.story.StoryRuntime", "publish", var3);
            } catch (Throwable var2x) {
               throw new RuntimeException(var2x);
            }
         };
         callStatic("fr.reivaxmc.progress.story.CampaignRuntimeContext", "withServer", var0, var4);
      } catch (Throwable var5) {
         System.err.println("[REIVAX Alpha 18F.8.1] resonance fact publish failed: " + var5);
      }
   }

   private static long floorMod(long var0, long var2) {
      long var4 = var0 % var2;
      return var4 < 0L ? var4 + var2 : var4;
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

   private static String string(Object var0, String var1) throws Exception {
      Object var2 = invokeNoArg(var0, var1);
      return var2 == null ? "" : String.valueOf(var2);
   }

   private static Number number(Object var0) {
      if (var0 instanceof Number) {
         return (Number)var0;
      } else {
         throw new IllegalArgumentException("Expected number, got " + var0);
      }
   }

   private static Object staticField(String var0, String var1) throws Exception {
      Class var2 = Class.forName(var0);
      Field var3 = var2.getField(var1);
      return var3.get(null);
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
            || var0 == short.class && (var2 == Short.class || var2 == Byte.class)
            || var0 == int.class && (var2 == Integer.class || var2 == Short.class || var2 == Byte.class)
            || var0 == long.class && Number.class.isAssignableFrom(var2)
            || var0 == float.class && Number.class.isAssignableFrom(var2)
            || var0 == double.class && Number.class.isAssignableFrom(var2)
            || var0 == char.class && var2 == Character.class;
      }
   }

   private static boolean primitiveDouble(Class<?> var0) {
      return var0 == double.class || var0 == Double.class;
   }

   private static final class Session {
      boolean initialized;
      int scene = 0;
      String actorId = "";
      String actorName = "";
      long nearTicks;
      long survivalTicks;
      boolean contactAnnounced;
      boolean wasPaused;
      boolean voiceSpoken;
      boolean foldSpoken;
      boolean sealSpawned;
      boolean sealClaimed;
      boolean duskHint;
      boolean resonanceTriggered;
      boolean dawnCompleted;
   }
}
