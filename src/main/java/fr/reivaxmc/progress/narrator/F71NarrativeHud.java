package fr.reivaxmc.progress.narrator;

import fr.reivaxmc.progress.story.F81ClientState;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public final class F71NarrativeHud {
   private static final long VOICE_LIFETIME_DEFAULT_MS = 6000L;
   private static final long OBJECTIVE_LIFETIME_MS = 10000L;
   private static final long HINT_LIFETIME_MS = 2600L;
   private static final long GUIDANCE_LIFETIME_MS = 2600L;
   private static final long FOYER_PANEL_LIFETIME_MS = 9000L;
   private static final long MILESTONE_LIFETIME_MS = 4200L;
   private static final int MAX_PENDING_HISTORY = 120;
   private static String objective = "";
   private static String voice = "";
   private static String voiceKind = "";
   private static String voiceEventId = "";
   private static String voiceActorName = "";
   private static String voiceReward = "";
   private static int voiceGained = 0;
   private static boolean voiceRecipientIsActor = true;
   private static long voiceLifetimeMs = VOICE_LIFETIME_DEFAULT_MS;
   private static String hint = "";
   private static String guidance = "";
   private static String foyerPanel = "";
   private static String milestone = "";
   private static String lastEventKey = "";
   private static long objectiveAt = -1L;
   private static long voiceAt = -1L;
   private static long hintAt = -1L;
   private static long guidanceAt = -1L;
   private static long foyerAt = -1L;
   private static long milestoneAt = -1L;
   private static boolean historyHeaderInjected = false;
   private static final Deque<String> pendingHistory = new ArrayDeque<>();

   private F71NarrativeHud() {
   }

   public static void render(Object var0, Object var1) {
      if (var0 != null) {
         try {
            Object var2 = minecraft();
            if (var2 == null) {
               return;
            }

            captureLatestClientEvent();
            F81ClientState.enforceMask();
            Object var3 = field(var2, "screen");
            boolean var4 = isChatScreen(var3);
            if (var4) {
               flushHistoryIntoChat(var2);
               return;
            }

            Object var5 = field(var2, "player");
            if (var5 == null) {
               return;
            }

            Object var6 = field(var2, "font");
            if (var6 == null) {
               return;
            }

            int var7 = number(call(var0, "guiWidth")).intValue();
            int var8 = number(call(var0, "guiHeight")).intValue();
            long var9 = utilMillis();
            if (!objective.isBlank() && objectiveAt > 0L && var9 - objectiveAt < 10000L) {
               drawObjective(var0, var6, objective, var7, var8, var9 - objectiveAt);
            }

            if (!hint.isBlank() && hintAt > 0L && var9 - hintAt < 2600L) {
               drawHint(var0, var6, hint, var7, var8, var9 - hintAt);
            }

            if (!guidance.isBlank() && guidanceAt > 0L && var9 - guidanceAt < 2600L) {
               drawGuidance(var0, var6, guidance, var7, var8, var9 - guidanceAt);
            }

            if (!foyerPanel.isBlank() && foyerAt > 0L && var9 - foyerAt < 9000L) {
               drawFoyerPanel(var0, var6, foyerPanel, var7, var8, var9 - foyerAt);
            }

            if (!milestone.isBlank() && milestoneAt > 0L && var9 - milestoneAt < 4200L) {
               drawMilestone(var0, var6, milestone, var7, var8, var9 - milestoneAt);
            }

            if (!voice.isBlank() && voiceAt > 0L && var9 - voiceAt < voiceLifetimeMs) {
               drawVoice(var0, var6, voice, var7, var8, var9 - voiceAt);
            }
         } catch (Throwable var11) {
            System.err.println("[REIVAX Alpha 18F.8.3] Narrative HUD failed: " + var11.getClass().getSimpleName() + ": " + var11.getMessage());
         }
      }
   }

   private static void captureLatestClientEvent() throws Exception {
      Class<?> stateClass = Class.forName("fr.reivaxmc.progress.network.ClientCampaignState");
      String event = string(staticField(stateClass, "event"));
      String kind = string(staticField(stateClass, "kind"));
      String detail = string(staticField(stateClass, "detail"));
      int gained = number(staticField(stateClass, "gained")).intValue();
      long eventAt = number(staticField(stateClass, "eventAt")).longValue();
      if (event.isBlank() && kind.isBlank()) {
         return;
      }

      String key = event + "|" + kind + "|" + eventAt + "|" + detail;
      if (key.equals(lastEventKey)) {
         return;
      }
      lastEventKey = key;

      switch (kind) {
         case "F71_OBJECTIVE":
            String cleanObjective = cleanHudText(detail);
            if (!cleanObjective.equals(objective) || objectiveAt <= 0L) {
               objective = cleanObjective;
               objectiveAt = eventAt > 0L ? eventAt : utilMillis();
            }
            break;
         case "F71_HINT":
            hint = cleanHudText(detail);
            hintAt = eventAt > 0L ? eventAt : utilMillis();
            break;
         case "NARRATOR_WHISPER":
         case "NARRATOR_ACHIEVEMENT":
         case "NARRATOR_ANNOUNCEMENT":
         case "NARRATOR_RESONANCE":
            voice = F81ClientState.qaMask() ? "VOIX · Dialogue narratif déclenché ✓" : cleanHudText(detail);
            voiceKind = kind;
            VoiceEventContext context = VoiceEventContext.parse(event);
            voiceEventId = context.baseId;
            voiceActorName = context.actorName;
            voiceRecipientIsActor = context.recipientIsActor;
            voiceGained = Math.max(0, gained);
            voiceReward = F81ClientState.qaMask() ? "" : NarratorLegacy.rewardSummary(voiceEventId);
            voiceLifetimeMs = voiceLifetimeFor(voice, kind);
            voiceAt = eventAt > 0L ? eventAt : utilMillis();
            playVoiceCue(kind);
            break;
         case "F71_HISTORY":
            if (!detail.isBlank()) {
               pendingHistory.addLast(F81ClientState.qaMask() ? "§8[QA] §7Événement narratif enregistré ✓" : detail);
               while (pendingHistory.size() > MAX_PENDING_HISTORY) {
                  pendingHistory.removeFirst();
               }
            }
            break;
         case "F8_GUIDANCE":
            guidance = cleanHudText(detail);
            guidanceAt = eventAt > 0L ? eventAt : utilMillis();
            break;
         case "F8_FOYER_PANEL":
            String panel = detail == null ? "" : detail.trim();
            try {
               openFoyerScreen(panel);
               foyerPanel = "";
               foyerAt = -1L;
            } catch (Throwable ignored) {
               foyerPanel = panel;
               foyerAt = eventAt > 0L ? eventAt : utilMillis();
            }
            break;
         case "F8_MILESTONE":
            milestone = F81ClientState.qaMask() ? "JALON NARRATIF VALIDÉ ✓" : cleanHudText(detail);
            milestoneAt = eventAt > 0L ? eventAt : utilMillis();
            break;
         case "F81_QA":
            boolean enabled = "ON".equalsIgnoreCase(cleanHudText(detail));
            F81ClientState.setQaMask(enabled);
            hint = enabled ? "MODE QA ANTI-SPOIL · contenu narratif masqué" : "MODE QA ANTI-SPOIL DÉSACTIVÉ · contenu réel visible";
            hintAt = eventAt > 0L ? eventAt : utilMillis();
            break;
      }
   }

   private static void drawObjective(Object var0, Object var1, String var2, int var3, int var4, long var5) throws Exception {
      String var7 = "OBJECTIF PRINCIPAL";
      int var8 = fontWidth(var1, var7);
      int var9 = Math.max(320, var3 - 48);
      int var10 = var9 - 24 - var8 - 16;
      boolean var11 = fontWidth(var1, var2) <= var10;
      List<String> var12 = var11 ? List.of(var2) : wrapText(var1, var2, var9 - 24);
      int var13 = var11 ? 30 : 30 + Math.max(1, var12.size()) * 13;
      int var14;
      if (var11) {
         var14 = Math.min(var9, Math.max(390, var8 + 18 + fontWidth(var1, var2) + 34));
      } else {
         int var15 = var8;

         for (String var17 : var12) {
            var15 = Math.max(var15, fontWidth(var1, var17));
         }

         var14 = Math.min(var9, Math.max(390, var15 + 24));
      }

      int var27 = (var3 - var14) / 2;
      int var28 = var4 - var13 - 38;
      float var29 = 1.0F;
      if (var5 > 28500L) {
         var29 = Math.max(0.0F, (float)(30000L - var5) / 1500.0F);
      }

      int var18 = clampAlpha((int)(102.0F * var29));
      int var19 = clampAlpha((int)(217.0F * var29));
      int var20 = clampAlpha((int)(255.0F * var29));
      int var21 = clampAlpha((int)(255.0F * var29));
      fill(var0, var27 + 2, var28 + 2, var27 + var14 + 2, var28 + var13 + 2, var18 << 24);
      fill(var0, var27, var28, var27 + var14, var28 + var13, var19 << 24 | 1184791);
      fill(var0, var27, var28, var27 + 3, var28 + var13, var20 << 24 | 14722860);
      int var22 = var21 << 24 | 15775026;
      int var23 = var21 << 24 | 16052973;
      drawString(var0, var1, var7, var27 + 12, var28 + 10, var22, false);
      if (var11) {
         drawString(var0, var1, var2, var27 + 12 + var8 + 16, var28 + 10, var23, true);
      } else {
         int var24 = var28 + 25;

         for (String var26 : var12) {
            drawString(var0, var1, var26, var27 + 12, var24, var23, true);
            var24 += 13;
         }
      }
   }

   private static void drawHint(Object var0, Object var1, String var2, int var3, int var4, long var5) throws Exception {
      float var7 = 1.0F;
      if (var5 < 180L) {
         var7 = Math.max(0.0F, (float)var5 / 180.0F);
      } else if (var5 > 2050L) {
         var7 = Math.max(0.0F, (float)(2600L - var5) / 550.0F);
      }

      int var8 = clampAlpha((int)(220.0F * var7));
      int var9 = var8 << 24 | 15065302;
      int var10 = fontWidth(var1, var2);
      byte var11 = 10;
      int var12 = (var3 - var10) / 2;
      int var13 = var4 - 96;
      fill(var0, var12 - var11, var13 - 4, var12 + var10 + var11, var13 + 14, clampAlpha((int)(105.0F * var7)) << 24 | 592395);
      drawCentered(var0, var1, var2, var3 / 2, var13, var9);
   }

   private static void drawVoice(Object graphics, Object font, String text, int screenWidth, int screenHeight, long elapsed) throws Exception {
      long lifetime = Math.max(1L, voiceLifetimeMs);
      long fadeOutStart = Math.max(1200L, lifetime - 650L);

      float appear = Math.min(1.0F, Math.max(0.0F, elapsed / 230.0F));
      float disappear = elapsed <= fadeOutStart ? 1.0F : Math.max(0.0F, (float)(lifetime - elapsed) / (float)(lifetime - fadeOutStart));
      float alpha = smoothStep(appear) * smoothStep(disappear);
      if (alpha <= 0.01F) {
         return;
      }

      boolean revelation = isRevelation(voiceKind);
      int maxTextWidth = revelation
         ? Math.max(260, Math.min(600, (int)(screenWidth * 0.60F)))
         : Math.max(220, Math.min(470, (int)(screenWidth * 0.46F)));
      List<String> lines = wrapText(font, text, maxTextWidth);
      int widest = 0;
      for (String line : lines) {
         widest = Math.max(widest, fontWidth(font, line));
      }

      String meta = voiceMetaLine();
      boolean hasMeta = !meta.isBlank();
      int metaWidth = hasMeta ? fontWidth(font, meta) : 0;
      String label = revelation ? "LA VOIX · RÉSONANCE" : "LA VOIX";
      int contentWidth = Math.max(widest, Math.max(metaWidth, fontWidth(font, label) + 60));
      int desiredWidth = Math.max(revelation ? 300 : 250, contentWidth + 54);
      int maxPanelWidth = Math.max(300, (int)(screenWidth * (revelation ? 0.72F : 0.60F)));
      int panelWidth = Math.min(screenWidth - 54, Math.min(maxPanelWidth, desiredWidth));
      int panelHeight = 28 + Math.max(1, lines.size()) * 13 + (hasMeta ? 14 : 0);
      int x = (screenWidth - panelWidth) / 2;
      int restingY = Math.max(64, (int)(screenHeight * 0.255));
      int y = restingY + Math.round((1.0F - smoothStep(appear)) * (revelation ? 7.0F : 5.0F));

      // 0.7.12 : les interventions normales laissent davantage respirer le monde.
      int shadowA = clampAlpha((int)((revelation ? 78.0F : 56.0F) * alpha));
      int bodyA = clampAlpha((int)((revelation ? 205.0F : 181.0F) * alpha));
      int innerA = clampAlpha((int)((revelation ? 92.0F : 64.0F) * alpha));
      int textA = clampAlpha((int)(255.0F * alpha));
      int labelA = clampAlpha((int)(218.0F * alpha));
      int accentA = clampAlpha((int)((revelation ? 230.0F : 196.0F) * alpha));

      int gold = 0xC9A84C;
      int paleGold = 0xE7D9AE;
      int cyan = revelation ? 0x74D7D2 : 0x4FA9AD;
      int textColor = textA << 24 | 0xF3EFE5;
      int labelColor = labelA << 24 | paleGold;

      fill(graphics, x + 3, y + 3, x + panelWidth + 3, y + panelHeight + 3, shadowA << 24);
      fill(graphics, x, y, x + panelWidth, y + panelHeight, bodyA << 24 | 0x090B0E);
      fill(graphics, x + 2, y + 2, x + panelWidth - 2, y + panelHeight - 2, innerA << 24 | 0x10161A);

      int lineInset = 16;
      fill(graphics, x + lineInset, y, x + panelWidth - lineInset, y + 1, accentA << 24 | gold);
      fill(graphics, x + lineInset + 28, y + panelHeight - 1, x + panelWidth - lineInset - 28, y + panelHeight, clampAlpha((int)(122.0F * alpha)) << 24 | gold);
      fill(graphics, x, y + 7, x + 2, y + panelHeight - 7, clampAlpha((int)(145.0F * alpha)) << 24 | cyan);
      fill(graphics, x + panelWidth - 2, y + 7, x + panelWidth, y + panelHeight - 7, clampAlpha((int)(145.0F * alpha)) << 24 | cyan);

      fill(graphics, x + 8, y + 7, x + 13, y + 9, accentA << 24 | gold);
      fill(graphics, x + panelWidth - 13, y + 7, x + panelWidth - 8, y + 9, accentA << 24 | gold);
      fill(graphics, x + 8, y + panelHeight - 9, x + 13, y + panelHeight - 7, accentA << 24 | gold);
      fill(graphics, x + panelWidth - 13, y + panelHeight - 9, x + panelWidth - 8, y + panelHeight - 7, accentA << 24 | gold);

      drawString(graphics, font, label, x + 16, y + 7, labelColor, false);
      int headerLineStart = x + 16 + fontWidth(font, label) + 8;
      if (headerLineStart < x + panelWidth - 16) {
         fill(graphics, headerLineStart, y + 11, x + panelWidth - 16, y + 12, clampAlpha((int)(58.0F * alpha)) << 24 | cyan);
      }

      long typeStart = 100L;
      int visibleChars = elapsed <= typeStart ? 0 : Math.min(text.length(), (int)((elapsed - typeStart) * 63L / 1000L));
      int remaining = visibleChars;
      int textY = y + 21;
      for (String fullLine : lines) {
         String shown;
         if (remaining <= 0) {
            shown = "";
         } else if (remaining >= fullLine.length()) {
            shown = fullLine;
         } else {
            shown = fullLine.substring(0, remaining);
         }

         if (!shown.isEmpty()) {
            drawCentered(graphics, font, shown, screenWidth / 2, textY, textColor);
         }
         remaining -= fullLine.length();
         textY += 13;
      }

      if (hasMeta) {
         float metaAppear = smoothStep(Math.max(0.0F, Math.min(1.0F, (elapsed - 220L) / 220.0F)));
         int metaA = clampAlpha((int)(220.0F * alpha * metaAppear));
         int separatorA = clampAlpha((int)(64.0F * alpha * metaAppear));
         int metaY = y + panelHeight - 11;
         fill(graphics, x + 28, metaY - 4, x + panelWidth - 28, metaY - 3, separatorA << 24 | cyan);
         drawCentered(graphics, font, fit(font, meta, panelWidth - 42), screenWidth / 2, metaY, metaA << 24 | 0xD7C58F);
      }
   }

   private static String voiceMetaLine() {
      String points = voiceGained > 0 ? "+" + voiceGained + " PTS" : "";
      String reward = voiceReward == null ? "" : voiceReward.trim();
      if (!reward.isBlank()) {
         if (voiceRecipientIsActor || voiceActorName == null || voiceActorName.isBlank()) {
            reward = "RÉCOMPENSE : " + reward;
         } else {
            reward = voiceActorName + " reçoit : " + reward;
         }
      }

      if (!points.isBlank() && !reward.isBlank()) {
         return points + "   ·   " + reward;
      }
      return !points.isBlank() ? points : reward;
   }

   private static long voiceLifetimeFor(String text, String kind) {
      int chars = text == null ? 0 : text.length();
      long lifetime = 2800L + chars * 43L;
      if (isRevelation(kind)) {
         lifetime += 1100L;
      } else if ("NARRATOR_ANNOUNCEMENT".equals(kind)) {
         lifetime += 550L;
      }
      return Math.max(4300L, Math.min(9200L, lifetime));
   }

   private static boolean isRevelation(String kind) {
      return "NARRATOR_RESONANCE".equals(kind);
   }

   private static float smoothStep(float value) {
      float t = Math.max(0.0F, Math.min(1.0F, value));
      return t * t * (3.0F - 2.0F * t);
   }

   private static void playVoiceCue(String kind) {
      try {
         Object mc = minecraft();
         if (mc == null) {
            return;
         }
         Object player = field(mc, "player");
         if (player == null) {
            return;
         }
         Class<?> sounds = Class.forName("net.minecraft.sounds.SoundEvents");
         Object chime = staticField(sounds, "AMETHYST_BLOCK_CHIME");
         float volume = isRevelation(kind) ? 0.24F : 0.15F;
         float pitch = isRevelation(kind) ? 0.62F : ("NARRATOR_ANNOUNCEMENT".equals(kind) ? 0.78F : 0.92F);
         call(player, "playSound", chime, volume, pitch);
      } catch (Throwable ignored) {
         // Le son est un embellissement : il ne doit jamais pouvoir casser le HUD.
      }
   }

   private static void drawGuidance(Object var0, Object var1, String var2, int var3, int var4, long var5) throws Exception {
      float var7 = 0.88F + 0.12F * (float)Math.sin((double)var5 / 190.0);
      int var8 = fontWidth(var1, var2);
      int var9 = Math.min(var3 - 60, Math.max(250, var8 + 44));
      int var10 = (var3 - var9) / 2;
      byte var11 = 28;
      int var12 = clampAlpha((int)(210.0F * var7));
      fill(var0, var10, var11, var10 + var9, var11 + 24, clampAlpha((int)(125.0F * var7)) << 24 | 592653);
      fill(var0, var10 + 14, var11 + 22, var10 + var9 - 14, var11 + 23, clampAlpha((int)(145.0F * var7)) << 24 | 14209992);
      drawCentered(var0, var1, var2, var3 / 2, var11 + 8, var12 << 24 | 15920870);
   }

   private static void drawFoyerPanel(Object var0, Object var1, String var2, int var3, int var4, long var5) throws Exception {
      float var7 = var5 < 220L ? Math.max(0.0F, (float)var5 / 220.0F) : (var5 > 8200L ? Math.max(0.0F, (float)(9000L - var5) / 800.0F) : 1.0F);
      String[] var8 = var2.split("\\|", -1);
      int var9 = Math.min(360, var3 - 50);
      byte var10 = 78;
      int var11 = var3 - var9 - 20;
      int var12 = Math.max(72, (var4 - var10) / 2);
      fill(var0, var11, var12, var11 + var9, var12 + var10, clampAlpha((int)(220.0F * var7)) << 24 | 1053204);
      fill(var0, var11, var12, var11 + 4, var12 + var10, clampAlpha((int)(255.0F * var7)) << 24 | 14066746);
      drawString(var0, var1, "FOYER PRINCIPAL", var11 + 14, var12 + 12, clampAlpha((int)(255.0F * var7)) << 24 | 15775026, false);

      for (int var13 = 0; var13 < Math.min(var8.length, 3); var13++) {
         int var14 = var13 == 0 ? 16118249 : 14144459;
         drawString(var0, var1, cleanHudText(var8[var13]), var11 + 14, var12 + 30 + var13 * 14, clampAlpha((int)(245.0F * var7)) << 24 | var14, true);
      }
   }

   private static void drawMilestone(Object var0, Object var1, String var2, int var3, int var4, long var5) throws Exception {
      float var7;
      if (var5 < 450L) {
         var7 = (float)var5 / 450.0F;
      } else if (var5 > 3300L) {
         var7 = Math.max(0.0F, (float)(4200L - var5) / 900.0F);
      } else {
         var7 = 1.0F;
      }

      int var8 = Math.max(82, (int)((double)var4 * 0.22));
      int var9 = fontWidth(var1, var2);
      int var10 = Math.min(var3 - 80, Math.max(330, var9 + 120));
      int var11 = (var3 - var10) / 2;
      fill(var0, var11, var8 - 12, var11 + var10, var8 + 26, clampAlpha((int)(110.0F * var7)) << 24 | 526602);
      fill(var0, var11 + 50, var8 + 21, var11 + var10 - 50, var8 + 22, clampAlpha((int)(190.0F * var7)) << 24 | 14066746);
      drawCentered(var0, var1, var2, var3 / 2, var8, clampAlpha((int)(255.0F * var7)) << 24 | 16050372);
   }

   private static void flushHistoryIntoChat(Object var0) {
      if (!pendingHistory.isEmpty()) {
         try {
            Object var1 = field(var0, "gui");
            if (var1 == null) {
               return;
            }

            Object var2 = call(var1, "getChat");
            if (var2 == null) {
               return;
            }

            if (!historyHeaderInjected) {
               addChatMessage(var2, "§8—— §7Historique REIVAX §8——");
               historyHeaderInjected = true;
            }

            while (!pendingHistory.isEmpty()) {
               addChatMessage(var2, pendingHistory.removeFirst());
            }
         } catch (Throwable var3) {
            System.err.println("[REIVAX Alpha 18F.8.3] History flush failed: " + var3.getClass().getSimpleName());
         }
      }
   }

   private static void addChatMessage(Object var0, String var1) throws Exception {
      Class var2 = Class.forName("net.minecraft.network.chat.Component");
      Method var3 = var2.getMethod("literal", String.class);
      Object var4 = var3.invoke(null, var1);
      call(var0, "addMessage", var4);
   }

   private static boolean isChatScreen(Object var0) {
      if (var0 == null) {
         return false;
      } else {
         String var1 = var0.getClass().getName();
         return var1.endsWith("ChatScreen") || var1.contains(".ChatScreen");
      }
   }

   private static String cleanHudText(String var0) {
      if (var0 == null) {
         return "";
      } else {
         StringBuilder var1 = new StringBuilder(var0.length());

         for (int var2 = 0; var2 < var0.length(); var2++) {
            char var3 = var0.charAt(var2);
            if (var3 == 167 && var2 + 1 < var0.length()) {
               var2++;
            } else {
               var1.append(var3);
            }
         }

         return var1.toString().replace('\n', ' ').trim();
      }
   }

   private static List<String> wrapText(Object var0, String var1, int var2) {
      ArrayList var3 = new ArrayList();
      String var4 = var1 == null ? "" : var1.trim();

      while (!var4.isEmpty()) {
         try {
            String var5 = String.valueOf(call(var0, "plainSubstrByWidth", var4, var2));
            if (var5.isBlank()) {
               break;
            }

            int var6 = var5.length();
            if (var6 < var4.length()) {
               int var7 = var5.lastIndexOf(32);
               if (var7 > Math.max(8, var5.length() / 2)) {
                  var6 = var7;
               }
            }

            String var9 = var4.substring(0, Math.min(var6, var4.length())).trim();
            if (var9.isEmpty()) {
               var9 = var5.trim();
            }

            var3.add(var9);
            var4 = var4.substring(Math.min(var6, var4.length())).trim();
         } catch (Throwable var8) {
            var3.add(var4);
            break;
         }

         if (var3.size() >= 5) {
            if (!var4.isEmpty()) {
               var3.add(var4);
            }
            break;
         }
      }

      if (var3.isEmpty()) {
         var3.add("");
      }

      return var3;
   }

   private static void openFoyerScreen(String var0) throws Exception {
      Object var1 = minecraft();
      if (var1 == null) {
         throw new IllegalStateException("Minecraft absent");
      } else {
         Class var2 = Class.forName("fr.reivaxmc.progress.client.F82FoyerScreen");
         Object var3 = var2.getConstructor(String.class).newInstance(var0 == null ? "" : var0);
         Runnable var4 = () -> {
            try {
               call(var1, "setScreen", var3);
            } catch (Throwable var3x) {
               System.err.println("[REIVAX Alpha 18F.8.3] Foyer Screen deferred open failed: " + var3x.getClass().getSimpleName() + ": " + var3x.getMessage());
            }
         };

         try {
            call(var1, "execute", var4);
         } catch (Throwable var6) {
            call(var1, "setScreen", var3);
         }
      }
   }

   private static String fit(Object var0, String var1, int var2) {
      try {
         Object var3 = call(var0, "plainSubstrByWidth", var1, var2);
         String var4 = String.valueOf(var3);
         return var4.length() < var1.length() && var4.length() > 1 ? var4.substring(0, Math.max(1, var4.length() - 1)).trim() + "…" : var4;
      } catch (Throwable var5) {
         return var1;
      }
   }

   private static int fontWidth(Object var0, String var1) {
      try {
         return number(call(var0, "width", var1)).intValue();
      } catch (Throwable var3) {
         return Math.max(1, var1.length() * 6);
      }
   }

   private static void fill(Object var0, int var1, int var2, int var3, int var4, int var5) throws Exception {
      call(var0, "fill", var1, var2, var3, var4, var5);
   }

   private static void drawString(Object var0, Object var1, String var2, int var3, int var4, int var5, boolean var6) throws Exception {
      call(var0, "drawString", var1, var2, var3, var4, var5, var6);
   }

   private static void drawCentered(Object var0, Object var1, String var2, int var3, int var4, int var5) throws Exception {
      call(var0, "drawCenteredString", var1, var2, var3, var4, var5);
   }

   private static int clampAlpha(int var0) {
      return Math.max(0, Math.min(255, var0));
   }

   private static Object minecraft() {
      try {
         Class var0 = Class.forName("net.minecraft.client.Minecraft");
         return var0.getMethod("getInstance").invoke(null);
      } catch (Throwable var1) {
         return null;
      }
   }

   private static long utilMillis() {
      try {
         Class var0 = Class.forName("net.minecraft.Util");
         return number(var0.getMethod("getMillis").invoke(null)).longValue();
      } catch (Throwable var1) {
         return System.currentTimeMillis();
      }
   }

   private static Object staticField(Class<?> var0, String var1) throws Exception {
      Field var2 = var0.getField(var1);
      return var2.get(null);
   }

   private static Object field(Object var0, String var1) throws Exception {
      if (var0 == null) {
         return null;
      } else {
         for (Class var2 = var0.getClass(); var2 != null; var2 = var2.getSuperclass()) {
            try {
               Field var3 = var2.getDeclaredField(var1);
               var3.setAccessible(true);
               return var3.get(var0);
            } catch (NoSuchFieldException var4) {
            }
         }

         throw new NoSuchFieldException(var1);
      }
   }

   private static Object call(Object var0, String var1, Object... var2) throws Exception {
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

   private static String string(Object var0) {
      return var0 == null ? "" : String.valueOf(var0);
   }
   private static final class VoiceEventContext {
      final String baseId;
      final boolean recipientIsActor;
      final String actorName;

      private VoiceEventContext(String baseId, boolean recipientIsActor, String actorName) {
         this.baseId = baseId == null ? "" : baseId;
         this.recipientIsActor = recipientIsActor;
         this.actorName = actorName == null ? "" : actorName;
      }

      static VoiceEventContext parse(String raw) {
         if (raw == null || raw.isBlank()) {
            return new VoiceEventContext("", true, "");
         }
         String[] parts = raw.split("\\|", 3);
         String base = parts.length > 0 ? parts[0] : raw;
         boolean self = parts.length < 2 || !"OTHER".equals(parts[1]);
         String actor = parts.length >= 3 ? parts[2] : "";
         return new VoiceEventContext(base, self, actor);
      }
   }

}
