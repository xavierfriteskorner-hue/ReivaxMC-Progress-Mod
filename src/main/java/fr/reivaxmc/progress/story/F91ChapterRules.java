package fr.reivaxmc.progress.story;

import java.util.Locale;

/** Règles pures du Chapitre I, séparées du moteur Minecraft pour rester testables. */
public final class F91ChapterRules {
   public static final int REST = 1;
   public static final int STORAGE = 1 << 1;
   public static final int WORK = 1 << 2;
   public static final int LIGHT = 1 << 3;
   public static final int IDENTITY = 1 << 4;
   public static final int REQUIRED_HOME_SIGNALS = 3;

   public static final String LOCKED = "LOCKED";
   public static final String SHAPE_FOYER = "SHAPE_FOYER";
   public static final String CHOOSE_PRIORITY = "CHOOSE_PRIORITY";
   public static final String FIND_ECHO = "FIND_ECHO";
   public static final String RETURN_FRAGMENT = "RETURN_FRAGMENT";
   public static final String DEFEND_FOYER = "DEFEND_FOYER";
   public static final String COMPLETE = "COMPLETE";

   public static final String BASTION = "BASTION";
   public static final String MEMORY = "MEMORY";
   public static final String SOLIDARITY = "SOLIDARITY";

   private F91ChapterRules() {
   }

   public static int classifyBlock(String id) {
      String value = normalize(id);
      if (containsAny(value, "bed")) return REST;
      if (containsAny(value, "chest", "barrel", "shulker_box")) return STORAGE;
      if (containsAny(value, "crafting_table", "furnace", "smoker", "blast_furnace", "stonecutter", "anvil")) return WORK;
      if (containsAny(value, "torch", "lantern", "campfire", "candle")) return LIGHT;
      if (containsAny(value, "sign", "banner")) return IDENTITY;
      return 0;
   }

   public static String doctrineForItem(String id) {
      return switch (normalize(id)) {
         case "minecraft:iron_ingot" -> BASTION;
         case "minecraft:book" -> MEMORY;
         case "minecraft:bread" -> SOLIDARITY;
         default -> "";
      };
   }

   public static int signalCount(int mask) {
      return Integer.bitCount(mask & (REST | STORAGE | WORK | LIGHT | IDENTITY));
   }

   public static String signalLabel(int signal) {
      return switch (signal) {
         case REST -> "repos";
         case STORAGE -> "réserve";
         case WORK -> "travail";
         case LIGHT -> "lumière";
         case IDENTITY -> "identité";
         default -> "repère";
      };
   }

   public static String doctrineLabel(String doctrine) {
      return switch (doctrine) {
         case BASTION -> "PROTÉGER";
         case MEMORY -> "COMPRENDRE";
         case SOLIDARITY -> "PARTAGER";
         default -> "INDÉCISE";
      };
   }

   private static String normalize(String value) {
      return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
   }

   private static boolean containsAny(String value, String... needles) {
      for (String needle : needles) {
         if (value.contains(needle)) return true;
      }
      return false;
   }
}
