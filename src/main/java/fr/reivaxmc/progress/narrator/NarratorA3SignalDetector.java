package fr.reivaxmc.progress.narrator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Détecteur pur du lot accéléré A3 : progression matérielle et équipement.
 *
 * <p>Il ne connaît ni Minecraft, ni le Brain, ni le HUD. Les adaptateurs du
 * Narrateur lui transmettent uniquement des identifiants déjà normalisés.</p>
 */
final class NarratorA3SignalDetector {
   static final String FIRST_BUCKET = "A1-101";
   static final String FIRST_WATER_BUCKET = "A1-102";
   static final String FIRST_LAVA_BUCKET = "A1-103";
   static final String FIRST_ARMOR = "A1-104";
   static final String FULL_ARMOR = "A1-105";
   static final String FIRST_SHIELD = "A1-106";
   static final String FIRST_BOW = "A1-107";
   static final String FIRST_EMERALD = "A1-108";
   static final String FIRST_COPPER = "A1-109";
   static final String FIRST_LAPIS = "A1-110";
   static final String FIRST_GOLD = "A1-111";
   static final String FIRST_REDSTONE = "A1-112";
   static final String FIRST_AMETHYST = "A1-113";
   static final String FIRST_OBSIDIAN = "A1-114";
   static final String FIRST_IRON_TOOL = "A1-115";
   static final String FIRST_DIAMOND_PICKAXE = "A1-116";

   static final List<String> ALL_IDS = List.of(
      FIRST_BUCKET,
      FIRST_WATER_BUCKET,
      FIRST_LAVA_BUCKET,
      FIRST_ARMOR,
      FULL_ARMOR,
      FIRST_SHIELD,
      FIRST_BOW,
      FIRST_EMERALD,
      FIRST_COPPER,
      FIRST_LAPIS,
      FIRST_GOLD,
      FIRST_REDSTONE,
      FIRST_AMETHYST,
      FIRST_OBSIDIAN,
      FIRST_IRON_TOOL,
      FIRST_DIAMOND_PICKAXE
   );

   private static final Set<String> IRON_TOOLS = Set.of(
      "minecraft:iron_pickaxe",
      "minecraft:iron_axe",
      "minecraft:iron_shovel",
      "minecraft:iron_hoe"
   );

   private NarratorA3SignalDetector() {
   }

   record Signal(String eventId, String source, String targetId) {
   }

   record Equipment(Set<String> armorItems, String mainHandId, String offHandId) {
      Equipment {
         armorItems = armorItems == null ? Set.of() : Set.copyOf(armorItems);
         mainHandId = normalize(mainHandId);
         offHandId = normalize(offHandId);
      }
   }

   static Signal inventoryIncrease(String itemId) {
      String id = normalize(itemId);
      return switch (id) {
         case "minecraft:bucket" -> signal(FIRST_BUCKET, "inventory", id);
         case "minecraft:water_bucket" -> signal(FIRST_WATER_BUCKET, "inventory", id);
         case "minecraft:lava_bucket" -> signal(FIRST_LAVA_BUCKET, "inventory", id);
         case "minecraft:bow" -> signal(FIRST_BOW, "inventory", id);
         case "minecraft:emerald" -> signal(FIRST_EMERALD, "inventory", id);
         case "minecraft:raw_copper", "minecraft:copper_ingot" -> signal(FIRST_COPPER, "inventory", id);
         case "minecraft:lapis_lazuli" -> signal(FIRST_LAPIS, "inventory", id);
         case "minecraft:raw_gold", "minecraft:gold_ingot" -> signal(FIRST_GOLD, "inventory", id);
         case "minecraft:redstone" -> signal(FIRST_REDSTONE, "inventory", id);
         case "minecraft:amethyst_shard" -> signal(FIRST_AMETHYST, "inventory", id);
         case "minecraft:obsidian" -> signal(FIRST_OBSIDIAN, "inventory", id);
         case "minecraft:diamond_pickaxe" -> signal(FIRST_DIAMOND_PICKAXE, "inventory", id);
         default -> null;
      };
   }

   static List<Signal> equipment(Equipment equipment) {
      if (equipment == null) {
         return List.of();
      }

      ArrayList<Signal> signals = new ArrayList<>();
      HashSet<String> armor = new HashSet<>();
      for (String raw : equipment.armorItems()) {
         String id = normalize(raw);
         if (isVanillaArmor(id)) {
            armor.add(id);
         }
      }

      if (!armor.isEmpty()) {
         signals.add(signal(FIRST_ARMOR, "equipment", armor.iterator().next()));
      }
      if (armor.size() >= 4) {
         signals.add(signal(FULL_ARMOR, "equipment", "minecraft:full_armor"));
      }
      if ("minecraft:shield".equals(equipment.mainHandId()) || "minecraft:shield".equals(equipment.offHandId())) {
         signals.add(signal(FIRST_SHIELD, "equipment", "minecraft:shield"));
      }
      if (IRON_TOOLS.contains(equipment.mainHandId())) {
         signals.add(signal(FIRST_IRON_TOOL, "equipment", equipment.mainHandId()));
      }
      return List.copyOf(signals);
   }

   private static boolean isVanillaArmor(String id) {
      return id.startsWith("minecraft:")
         && (id.endsWith("_helmet") || id.endsWith("_chestplate") || id.endsWith("_leggings") || id.endsWith("_boots"));
   }

   private static Signal signal(String eventId, String source, String targetId) {
      return new Signal(eventId, source, targetId);
   }

   private static String normalize(String id) {
      return id == null ? "" : id.trim().toLowerCase(Locale.ROOT);
   }
}

