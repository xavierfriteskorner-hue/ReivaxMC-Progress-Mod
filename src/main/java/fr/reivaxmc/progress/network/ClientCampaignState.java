package fr.reivaxmc.progress.network;

import net.minecraft.Util;

public final class ClientCampaignState {
   public static int progress;
   public static int score;
   public static int targetX;
   public static int targetY;
   public static int targetZ;
   public static int gained;
   public static int territoryRadius;
   public static int historicalCount;
   public static boolean introCompleted;
   public static boolean reliquaryOpened;
   public static boolean matrixDiscovered;
   public static boolean foundationPlaced;
   public static boolean migration;
   public static long eventAt;
   public static String stage = "";
   public static String mission = "";
   public static String timeline = "";
   public static String artifacts = "";
   public static String event = "";
   public static String kind = "";
   public static String title = "";
   public static String detail = "";
   public static String foundationName = "";

   public static void apply(ProgressSyncPayload p) {
      progress = p.progress();
      score = p.score();
      introCompleted = p.introCompleted();
      targetX = p.targetX();
      targetY = p.targetY();
      targetZ = p.targetZ();
      stage = p.stage();
      mission = p.mission();
      timeline = p.timeline();
      artifacts = p.artifacts();
      if (p.event() != null && !p.event().isEmpty()) {
         event = p.event();
         kind = p.kind();
         title = p.title();
         detail = p.detail();
         gained = p.gained();
         eventAt = Util.getMillis();
      }

      reliquaryOpened = p.reliquaryOpened();
      matrixDiscovered = p.matrixDiscovered();
      foundationPlaced = p.foundationPlaced();
      migration = p.migration();
      foundationName = p.foundationName();
      territoryRadius = p.territoryRadius();
      historicalCount = p.historicalCount();
   }
}
