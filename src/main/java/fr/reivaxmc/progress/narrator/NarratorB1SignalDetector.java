package fr.reivaxmc.progress.narrator;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Détecteurs purs du lot B1. Cette classe ne dépend pas de Minecraft : les hooks
 * traduisent le monde en valeurs simples, puis le Brain reçoit uniquement un signal
 * confirmé. Les mêmes signaux servent en SOLO et en DUO.
 */
final class NarratorB1SignalDetector {
   static final String FIRST_CROP = "A1-024";
   static final String SURVIVE_DAWN = "A1-031";
   static final String VILLAGE_DISCOVERED = "A1-040";
   static final String HALF_HEART = "A1-087";
   static final String LIGHTNING = "A1-090";
   static final String TRAVEL_500 = "A1-135";
   static final String FIRST_SLEEP = "A1-136";
   static final String REFUSED_SLEEP = "A1-137";
   static final String FIRST_APPLE = "A1-138";
   static final String FIRST_COOKED_MEAL = "A1-139";
   static final String CRITICAL_HUNGER = "A1-140";
   static final String FIRST_OCEAN = "A1-141";
   static final String FIRST_SUMMIT = "A1-142";
   static final String FIRST_SNOW = "A1-143";
   static final String FIRST_DESERT = "A1-144";
   static final String FIRST_JUNGLE = "A1-145";
   static final String FIRST_DEEP_CAVE = "A1-146";
   static final String BELOW_MINUS_40 = "A1-147";
   static final String FIRST_THUNDERSTORM = "A1-148";
   static final String SUNRISE_OUTDOORS = "A1-149";
   static final String INVENTORY_FULL = "A1-150";
   static final String PRECIOUS_TOSS = "A1-151";
   static final String NAMED_ANIMAL = "A1-152";
   static final String COMPANION_HURT = "A1-153";

   static final List<String> ALL_IDS = List.of(
      FIRST_CROP, SURVIVE_DAWN, VILLAGE_DISCOVERED, TRAVEL_500, HALF_HEART, LIGHTNING,
      FIRST_SLEEP, REFUSED_SLEEP, FIRST_APPLE, FIRST_COOKED_MEAL, CRITICAL_HUNGER,
      FIRST_OCEAN, FIRST_SUMMIT, FIRST_SNOW, FIRST_DESERT, FIRST_JUNGLE,
      FIRST_DEEP_CAVE, BELOW_MINUS_40, FIRST_THUNDERSTORM, SUNRISE_OUTDOORS,
      INVENTORY_FULL, PRECIOUS_TOSS, NAMED_ANIMAL, COMPANION_HURT
   );

   private static final Set<String> CROPS = Set.of(
      "minecraft:wheat", "minecraft:carrots", "minecraft:potatoes", "minecraft:beetroots",
      "minecraft:melon_stem", "minecraft:pumpkin_stem", "minecraft:nether_wart", "minecraft:cocoa"
   );
   private static final Set<String> COOKED_FOOD = Set.of(
      "minecraft:baked_potato", "minecraft:bread", "minecraft:cooked_beef", "minecraft:cooked_chicken",
      "minecraft:cooked_cod", "minecraft:cooked_mutton", "minecraft:cooked_porkchop",
      "minecraft:cooked_rabbit", "minecraft:cooked_salmon", "minecraft:rabbit_stew",
      "minecraft:mushroom_stew", "minecraft:beetroot_soup", "minecraft:pumpkin_pie"
   );
   private static final Set<String> PRECIOUS_ITEMS = Set.of(
      "minecraft:diamond", "minecraft:netherite_ingot", "minecraft:enchanted_golden_apple",
      "minecraft:elytra", "minecraft:nether_star", "minecraft:dragon_egg", "minecraft:trident",
      "minecraft:heart_of_the_sea", "minecraft:recovery_compass"
   );

   private NarratorB1SignalDetector() {
   }

   record Signal(String eventId, String source, String targetId) {
   }

   static Signal cropPlaced(String blockId) {
      return CROPS.contains(normalize(blockId)) ? signal(FIRST_CROP, "PLACED", blockId) : null;
   }

   static Signal dawn(boolean nightSeen, boolean dawn) {
      return nightSeen && dawn ? signal(SURVIVE_DAWN, "STORY", "dawn") : null;
   }

   static Signal village(boolean insideVillage) {
      return insideVillage ? signal(VILLAGE_DISCOVERED, "LOCATION", "village") : null;
   }

   static Signal travelled(double cumulativeBlocks) {
      return cumulativeBlocks >= 500.0 ? signal(TRAVEL_500, "DISTANCE", "500") : null;
   }

   static Signal halfHeart(float previousHealth, float currentHealth) {
      return previousHealth > 1.0F && currentHealth > 0.0F && currentHealth <= 1.0F
         ? signal(HALF_HEART, "DANGER", "half_heart") : null;
   }

   static Signal lightning(boolean playerStruck, boolean canceled) {
      return playerStruck && !canceled ? signal(LIGHTNING, "LIGHTNING", "player") : null;
   }

   static Signal sleepConfirmed(boolean wakePending, boolean dawn) {
      return wakePending && dawn ? signal(FIRST_SLEEP, "SLEEP", "bed") : null;
   }

   static Signal refusedSleep(int consecutiveNightsAwake) {
      return consecutiveNightsAwake >= 3 ? signal(REFUSED_SLEEP, "TIME", "three_nights") : null;
   }

   static List<Signal> consumed(String itemId) {
      String id = normalize(itemId);
      ArrayList<Signal> out = new ArrayList<>(1);
      if ("minecraft:apple".equals(id)) {
         out.add(signal(FIRST_APPLE, "CONSUMED", id));
      } else if (COOKED_FOOD.contains(id)) {
         out.add(signal(FIRST_COOKED_MEAL, "CONSUMED", id));
      }
      return out;
   }

   static Signal criticalHunger(int previousFood, int currentFood) {
      return previousFood > 2 && currentFood <= 2 ? signal(CRITICAL_HUNGER, "HUNGER", String.valueOf(currentFood)) : null;
   }

   static List<Signal> environment(String biomeId, int y, boolean canSeeSky, boolean overworld) {
      ArrayList<Signal> out = new ArrayList<>();
      if (!overworld) {
         return out;
      }
      String biome = normalize(biomeId);
      if (biome.contains("ocean")) {
         out.add(signal(FIRST_OCEAN, "BIOME", biome));
      }
      if (canSeeSky && y >= 160) {
         out.add(signal(FIRST_SUMMIT, "LOCATION", String.valueOf(y)));
      }
      if (biome.contains("snow") || biome.contains("frozen") || biome.contains("ice_spikes")) {
         out.add(signal(FIRST_SNOW, "BIOME", biome));
      }
      if (biome.contains("desert") || biome.contains("badlands")) {
         out.add(signal(FIRST_DESERT, "BIOME", biome));
      }
      if (biome.contains("jungle")) {
         out.add(signal(FIRST_JUNGLE, "BIOME", biome));
      }
      if (!canSeeSky && y < 0) {
         out.add(signal(FIRST_DEEP_CAVE, "LOCATION", String.valueOf(y)));
      }
      if (y <= -40) {
         out.add(signal(BELOW_MINUS_40, "LOCATION", String.valueOf(y)));
      }
      return out;
   }

   static Signal thunderstorm(boolean thundering, boolean outdoors) {
      return thundering && outdoors ? signal(FIRST_THUNDERSTORM, "WEATHER", "thunder") : null;
   }

   static Signal sunrise(String previousPhase, String currentPhase, boolean outdoors, boolean awake) {
      return "NIGHT".equals(previousPhase) && "DAWN".equals(currentPhase) && outdoors && awake
         ? signal(SUNRISE_OUTDOORS, "TIME", "sunrise") : null;
   }

   static Signal inventoryFull(boolean wasFull, boolean isFull) {
      return !wasFull && isFull ? signal(INVENTORY_FULL, "INVENTORY", "full") : null;
   }

   static Signal preciousToss(String itemId, boolean canceled) {
      String id = normalize(itemId);
      return !canceled && PRECIOUS_ITEMS.contains(id) ? signal(PRECIOUS_TOSS, "TOSSED", id) : null;
   }

   static Signal animalNamed(boolean pending, boolean wasUnnamed, boolean nowNamed, boolean ownedByPlayer) {
      return pending && wasUnnamed && nowNamed && ownedByPlayer ? signal(NAMED_ANIMAL, "INTERACT", "named_pet") : null;
   }

   static Signal companionHurt(boolean ownedByPlayer, float damage, float healthAfter, float maxHealth) {
      return ownedByPlayer && damage > 0.0F && healthAfter > 0.0F && maxHealth > 0.0F && healthAfter / maxHealth <= 0.5F
         ? signal(COMPANION_HURT, "DANGER", "companion") : null;
   }

   private static Signal signal(String id, String source, String target) {
      return new Signal(id, source, target == null ? "" : target);
   }

   private static String normalize(String value) {
      return value == null ? "" : value.toLowerCase(Locale.ROOT);
   }
}
