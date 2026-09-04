package fr.reivaxmc.progress.narrator;

import java.util.List;
import java.util.Locale;

/**
 * Détecteur pur du lot A5 : fondations, animaux, mobilité et vie de village.
 *
 * <p>Les adaptateurs NeoForge confirment les actions réelles puis transmettent
 * ici uniquement des identifiants normalisés. Le détecteur ne connaît ni le
 * stockage, ni le Brain, ni le Réalisateur, ni le HUD.</p>
 */
final class NarratorA5SignalDetector {
   static final String FIRST_WOOD = "A1-001";
   static final String FIRST_CRAFTING_TABLE = "A1-003";
   static final String FIRST_STONE = "A1-005";
   static final String FIRST_COAL = "A1-007";
   static final String FIRST_RAW_IRON = "A1-009";
   static final String FIRST_SMELTING = "A1-010";
   static final String FIRST_DIAMOND = "A1-018";
   static final String FIRST_TAME = "A1-073";
   static final String FIRST_ANIMAL_FED = "A1-127";
   static final String FIRST_ANIMAL_BRED = "A1-128";
   static final String FIRST_HORSE_MOUNTED = "A1-129";
   static final String FIRST_BOAT_MOUNTED = "A1-130";
   static final String FIRST_VILLAGER_TRADE = "A1-131";
   static final String FIRST_VILLAGE_BELL = "A1-132";
   static final String FIRST_MAP = "A1-133";
   static final String FIRST_COMPASS = "A1-134";

   static final List<String> ALL_IDS = List.of(
      FIRST_WOOD,
      FIRST_CRAFTING_TABLE,
      FIRST_STONE,
      FIRST_COAL,
      FIRST_RAW_IRON,
      FIRST_SMELTING,
      FIRST_DIAMOND,
      FIRST_TAME,
      FIRST_ANIMAL_FED,
      FIRST_ANIMAL_BRED,
      FIRST_HORSE_MOUNTED,
      FIRST_BOAT_MOUNTED,
      FIRST_VILLAGER_TRADE,
      FIRST_VILLAGE_BELL,
      FIRST_MAP,
      FIRST_COMPASS
   );

   private NarratorA5SignalDetector() {
   }

   record Signal(String eventId, String source, String targetId) {
   }

   static Signal inventoryIncrease(String itemId) {
      String id = normalize(itemId);
      if (isVanillaWood(id)) {
         return signal(FIRST_WOOD, "inventory", id);
      }
      return switch (id) {
         case "minecraft:stone", "minecraft:cobblestone" -> signal(FIRST_STONE, "inventory", id);
         case "minecraft:coal", "minecraft:charcoal" -> signal(FIRST_COAL, "inventory", id);
         case "minecraft:raw_iron" -> signal(FIRST_RAW_IRON, "inventory", id);
         case "minecraft:diamond" -> signal(FIRST_DIAMOND, "inventory", id);
         case "minecraft:map", "minecraft:filled_map" -> signal(FIRST_MAP, "inventory", id);
         case "minecraft:compass" -> signal(FIRST_COMPASS, "inventory", id);
         default -> null;
      };
   }

   static Signal itemCrafted(String itemId) {
      String id = normalize(itemId);
      return "minecraft:crafting_table".equals(id) ? signal(FIRST_CRAFTING_TABLE, "crafted", id) : null;
   }

   static Signal itemSmelted(String itemId) {
      String id = normalize(itemId);
      return id.isBlank() || "minecraft:air".equals(id) ? null : signal(FIRST_SMELTING, "smelted", id);
   }

   static Signal animalTamed(boolean serverPlayer) {
      return serverPlayer ? signal(FIRST_TAME, "tame", "minecraft:animal") : null;
   }

   static Signal animalFed(boolean foodAccepted, boolean creditedToPlayer) {
      return foodAccepted && creditedToPlayer ? signal(FIRST_ANIMAL_FED, "animal_feed", "minecraft:animal") : null;
   }

   static Signal animalBred(boolean playerCaused, boolean childCreated) {
      return playerCaused && childCreated ? signal(FIRST_ANIMAL_BRED, "animal_breeding", "minecraft:baby_animal") : null;
   }

   static Signal entityMounted(String entityId, boolean mounting) {
      if (!mounting) {
         return null;
      }
      String id = normalize(entityId);
      if ("minecraft:horse".equals(id)) {
         return signal(FIRST_HORSE_MOUNTED, "mount", id);
      }
      if ("minecraft:boat".equals(id) || "minecraft:chest_boat".equals(id) || isVanillaBoatVariant(id)) {
         return signal(FIRST_BOAT_MOUNTED, "mount", id);
      }
      return null;
   }

   static Signal villagerTrade(boolean completedOnServer) {
      return completedOnServer ? signal(FIRST_VILLAGER_TRADE, "villager_trade", "minecraft:villager") : null;
   }

   static Signal villageBell(String blockId, boolean ringingConfirmed) {
      String id = normalize(blockId);
      return ringingConfirmed && "minecraft:bell".equals(id) ? signal(FIRST_VILLAGE_BELL, "bell", id) : null;
   }

   private static boolean isVanillaWood(String id) {
      return id.startsWith("minecraft:")
         && (id.endsWith("_log") || id.endsWith("_wood") || id.endsWith("_stem") || id.endsWith("_hyphae")
            || "minecraft:bamboo_block".equals(id));
   }

   private static boolean isVanillaBoatVariant(String id) {
      return id.startsWith("minecraft:") && (id.endsWith("_boat") || id.endsWith("_raft"));
   }

   private static Signal signal(String eventId, String source, String targetId) {
      return new Signal(eventId, source, targetId);
   }

   private static String normalize(String id) {
      return id == null ? "" : id.trim().toLowerCase(Locale.ROOT);
   }
}
