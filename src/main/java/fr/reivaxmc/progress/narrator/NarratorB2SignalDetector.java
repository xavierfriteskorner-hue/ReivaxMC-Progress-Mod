package fr.reivaxmc.progress.narrator;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Détecteurs purs du lot B2. Le monde Minecraft est d'abord traduit en faits
 * simples (distance, durée, santé, contenu d'un coffre), puis seulement en
 * événements narratifs. Cela garde les corrélations SOLO/DUO testables sans jeu.
 */
final class NarratorB2SignalDetector {
   static final String FAR_FROM_HOME = "A1-043";
   static final String RETURN_HOME = "A1-047";
   static final String DUO_VERY_FAR = "A1-049";
   static final String HOME_EXPLOSION = "A1-083";
   static final String FIRST_HOME_DOOR = "A1-154";
   static final String NIGHT_RETURN = "A1-155";
   static final String LONG_RETURN = "A1-156";
   static final String PARTNER_500 = "A1-157";
   static final String DUO_REUNION = "A1-158";
   static final String PARTNER_NEAR_DEATH = "A1-159";
   static final String BOTH_LOW_HEALTH = "A1-160";
   static final String SHARED_DISCOVERY = "A1-161";
   static final String FIRST_PARTNER_GIFT = "A1-162";
   static final String PRECIOUS_STORED = "A1-163";
   static final String CHEST_ALMOST_FULL = "A1-164";

   static final List<String> ALL_IDS = List.of(
      FAR_FROM_HOME, RETURN_HOME, DUO_VERY_FAR, HOME_EXPLOSION,
      FIRST_HOME_DOOR, NIGHT_RETURN, LONG_RETURN, PARTNER_500,
      DUO_REUNION, PARTNER_NEAR_DEATH, BOTH_LOW_HEALTH, SHARED_DISCOVERY,
      FIRST_PARTNER_GIFT, PRECIOUS_STORED, CHEST_ALMOST_FULL
   );

   static final double FAR_HOME_BLOCKS = 250.0;
   static final double LONG_RETURN_BLOCKS = 1000.0;
   static final double PARTNER_FAR_BLOCKS = 500.0;
   static final double PARTNER_VERY_FAR_BLOCKS = 1000.0;
   static final double REUNION_BLOCKS = 20.0;
   static final double NEAR_DEATH_BLOCKS = 32.0;
   static final long LONG_RETURN_MS = 20L * 60L * 1000L;
   static final long SHARED_DISCOVERY_MS = 10_000L;
   static final long GIFT_PICKUP_MS = 15_000L;

   private static final Set<String> PRECIOUS_BLOCKS = Set.of(
      "minecraft:diamond_block", "minecraft:emerald_block", "minecraft:netherite_block"
   );

   private NarratorB2SignalDetector() {
   }

   record Signal(String eventId, String source, String targetId) {
   }

   static List<Signal> homeReturn(boolean wasAway, double maxDistance, long awayDurationMs, boolean night) {
      ArrayList<Signal> out = new ArrayList<>(3);
      if (!wasAway) {
         return out;
      }
      if (maxDistance >= LONG_RETURN_BLOCKS) {
         out.add(signal(RETURN_HOME, "RETURN", String.valueOf((int)maxDistance)));
      }
      if (maxDistance >= FAR_HOME_BLOCKS && night) {
         out.add(signal(NIGHT_RETURN, "RETURN", "night"));
      }
      if (maxDistance >= FAR_HOME_BLOCKS && awayDurationMs >= LONG_RETURN_MS) {
         out.add(signal(LONG_RETURN, "RETURN", String.valueOf(awayDurationMs)));
      }
      return out;
   }

   static Signal farFromHome(double distance) {
      return distance >= FAR_HOME_BLOCKS ? signal(FAR_FROM_HOME, "LOCATION", String.valueOf((int)distance)) : null;
   }

   static List<Signal> duoDistance(boolean sameDimension, double distance) {
      ArrayList<Signal> out = new ArrayList<>(2);
      if (!sameDimension) {
         return out;
      }
      if (distance >= PARTNER_FAR_BLOCKS) {
         out.add(signal(PARTNER_500, "DISTANCE", String.valueOf((int)distance)));
      }
      if (distance >= PARTNER_VERY_FAR_BLOCKS) {
         out.add(signal(DUO_VERY_FAR, "DISTANCE", String.valueOf((int)distance)));
      }
      return out;
   }

   static Signal reunion(boolean wasSeparated, boolean sameDimension, double distance) {
      return wasSeparated && sameDimension && distance <= REUNION_BLOCKS
         ? signal(DUO_REUNION, "DUO_BRAIN", String.valueOf((int)distance)) : null;
   }

   static Signal partnerNearDeath(boolean sameDimension, double distance) {
      return sameDimension && distance <= NEAR_DEATH_BLOCKS
         ? signal(PARTNER_NEAR_DEATH, "DUO_BRAIN", String.valueOf((int)distance)) : null;
   }

   static Signal bothLowHealth(float firstHealth, float firstMax, float secondHealth, float secondMax) {
      boolean firstLow = firstHealth > 0.0F && firstMax > 0.0F && firstHealth / firstMax <= 0.25F;
      boolean secondLow = secondHealth > 0.0F && secondMax > 0.0F && secondHealth / secondMax <= 0.25F;
      return firstLow && secondLow ? signal(BOTH_LOW_HEALTH, "DUO_BRAIN", "both_low") : null;
   }

   static Signal homeExplosion(boolean insideHome, boolean explosion) {
      return insideHome && explosion ? signal(HOME_EXPLOSION, "DAMAGE", "home") : null;
   }

   static Signal homeDoor(String blockId, boolean insideHome) {
      String id = normalize(blockId);
      return insideHome && id.endsWith("_door")
         ? signal(FIRST_HOME_DOOR, "PLACED", id) : null;
   }

   static Signal sharedDiscovery(String firstActor, String secondActor, long elapsedMs, String eventId) {
      boolean differentActors = firstActor != null && secondActor != null && !firstActor.equals(secondActor);
      boolean eligibleEvent = eventId != null && eventId.startsWith("A1-") && !SHARED_DISCOVERY.equals(eventId);
      return differentActors && eligibleEvent && elapsedMs >= 0L && elapsedMs <= SHARED_DISCOVERY_MS
         ? signal(SHARED_DISCOVERY, "DUO_BRAIN", eventId) : null;
   }

   static Signal partnerGift(String giver, String receiver, long elapsedMs, boolean sameItemEntity) {
      boolean differentActors = giver != null && receiver != null && !giver.equals(receiver);
      return differentActors && sameItemEntity && elapsedMs >= 0L && elapsedMs <= GIFT_PICKUP_MS
         ? signal(FIRST_PARTNER_GIFT, "DUO_BRAIN", "gift") : null;
   }

   static List<Signal> homeContainer(String blockId, int occupiedSlots, int totalSlots, Set<String> itemIds) {
      ArrayList<Signal> out = new ArrayList<>(2);
      String block = normalize(blockId);
      if (itemIds != null && itemIds.stream().map(NarratorB2SignalDetector::normalize).anyMatch(PRECIOUS_BLOCKS::contains)) {
         out.add(signal(PRECIOUS_STORED, "HOME_STORAGE", block));
      }
      boolean chest = block.endsWith(":chest") || block.endsWith(":trapped_chest");
      if (chest && totalSlots > 0 && occupiedSlots * 10 >= totalSlots * 9) {
         out.add(signal(CHEST_ALMOST_FULL, "HOME_STORAGE", block));
      }
      return out;
   }

   private static Signal signal(String id, String source, String target) {
      return new Signal(id, source, target == null ? "" : target);
   }

   private static String normalize(String value) {
      return value == null ? "" : value.toLowerCase(Locale.ROOT);
   }
}
