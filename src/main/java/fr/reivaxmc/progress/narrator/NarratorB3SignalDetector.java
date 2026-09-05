package fr.reivaxmc.progress.narrator;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Détecteurs purs du lot B3. Les heuristiques spécialisées sont volontairement
 * conservatrices : chaque événement exige plusieurs indices concordants afin de
 * rester fiable avec un gros modpack, en SOLO comme en DUO.
 */
final class NarratorB3SignalDetector {
   static final String MISSED_ARROW = "A1-165";
   static final String ENDERMAN_SEEN = "A1-166";
   static final String HOME_FIRE = "A1-167";
   static final String HOME_ROOF = "A1-168";
   static final String NAMED_SIGN = "A1-169";
   static final String FIRST_RAVINE = "A1-170";
   static final String PRECIOUS_RECOVERY = "A1-171";
   static final String COMPANION_TRAVEL = "A1-172";
   static final String LOST_WITH_COMPASS = "A1-173";

   static final List<String> ALL_IDS = List.of(
      MISSED_ARROW, ENDERMAN_SEEN, HOME_FIRE, HOME_ROOF, NAMED_SIGN,
      FIRST_RAVINE, PRECIOUS_RECOVERY, COMPANION_TRAVEL, LOST_WITH_COMPASS
   );

   static final double COMPANION_TRAVEL_BLOCKS = 500.0;
   static final double LOST_TRAVEL_BLOCKS = 600.0;
   static final double LOST_HOME_DISTANCE = 250.0;
   static final long PRECIOUS_NEAR_DESPAWN_MS = 270_000L;

   private static final Set<String> PRECIOUS_ITEMS = Set.of(
      "minecraft:diamond", "minecraft:netherite_ingot", "minecraft:enchanted_golden_apple",
      "minecraft:elytra", "minecraft:nether_star", "minecraft:dragon_egg", "minecraft:trident",
      "minecraft:heart_of_the_sea", "minecraft:recovery_compass"
   );

   private NarratorB3SignalDetector() {
   }

   record Signal(String eventId, String source, String targetId) {
   }

   static Signal missedArrow(boolean playerOwned, boolean arrow, boolean hitBlock, boolean canceled) {
      return playerOwned && arrow && hitBlock && !canceled
         ? signal(MISSED_ARROW, "PROJECTILE", "block") : null;
   }

   static Signal endermanSeen(boolean enderman, double distance, double lookDot, boolean lineOfSight) {
      return enderman && distance <= 32.0 && lookDot >= 0.985 && lineOfSight
         ? signal(ENDERMAN_SEEN, "GAZE", "minecraft:enderman") : null;
   }

   static Signal homeFire(String blockId, boolean insideHome) {
      String id = normalize(blockId);
      return insideHome && (id.endsWith(":fire") || id.endsWith(":soul_fire"))
         ? signal(HOME_FIRE, "PLACED", id) : null;
   }

   static Signal homeRoof(boolean insideHome, long homeBlocks, int coveredCells) {
      return insideHome && homeBlocks >= 32L && coveredCells >= 7
         ? signal(HOME_ROOF, "HOME_SCAN", String.valueOf(coveredCells)) : null;
   }

   static Signal namedSign(String blockId, boolean nonEmptyText) {
      String id = normalize(blockId);
      boolean sign = id.endsWith("_sign") || id.endsWith("_hanging_sign");
      return sign && nonEmptyText ? signal(NAMED_SIGN, "SIGN_TEXT", id) : null;
   }

   static Signal ravine(boolean overworld, int openDepth, int opposingWalls, boolean naturalStoneNearby) {
      return overworld && openDepth >= 24 && opposingWalls >= 2 && naturalStoneNearby
         ? signal(FIRST_RAVINE, "TERRAIN_SCAN", String.valueOf(openDepth)) : null;
   }

   static Signal preciousRecovery(String itemId, boolean sameOwner, long elapsedMs, boolean immediateDanger) {
      String id = normalize(itemId);
      return PRECIOUS_ITEMS.contains(id) && sameOwner
         && (elapsedMs >= PRECIOUS_NEAR_DESPAWN_MS || immediateDanger)
         ? signal(PRECIOUS_RECOVERY, "RECOVERY", id) : null;
   }

   static Signal companionTravel(boolean ownedCompanion, double cumulativeBlocks) {
      return ownedCompanion && cumulativeBlocks >= COMPANION_TRAVEL_BLOCKS
         ? signal(COMPANION_TRAVEL, "COMPANION", String.valueOf((int)cumulativeBlocks)) : null;
   }

   static Signal lostWithCompass(boolean hasCompass, boolean sameDimension, double homeDistance, double travelled, double netApproach) {
      return hasCompass && sameDimension && homeDistance >= LOST_HOME_DISTANCE
         && travelled >= LOST_TRAVEL_BLOCKS && netApproach < 100.0
         ? signal(LOST_WITH_COMPASS, "NAVIGATION", String.valueOf((int)travelled)) : null;
   }

   static boolean isPrecious(String itemId) {
      return PRECIOUS_ITEMS.contains(normalize(itemId));
   }

   private static Signal signal(String id, String source, String target) {
      return new Signal(id, source, target == null ? "" : target);
   }

   private static String normalize(String value) {
      return value == null ? "" : value.toLowerCase(Locale.ROOT);
   }
}
