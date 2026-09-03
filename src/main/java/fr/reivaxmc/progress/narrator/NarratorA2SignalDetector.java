package fr.reivaxmc.progress.narrator;

import java.util.Locale;

/**
 * Détecteur pur du sous-lot A2 : un item simple et deux poses de blocs.
 *
 * <p>Cette classe ne connaît ni le Brain, ni les textes, ni le HUD. Elle
 * transforme seulement une observation Minecraft déjà filtrée en signal
 * neutre. La mémoire persistante et l'anti-doublon restent appliqués lors de
 * l'acceptation par le pipeline Narrateur.</p>
 */
final class NarratorA2SignalDetector {
   static final String FIRST_STICK = "A1-026";
   static final String FIRST_CHEST = "A1-027";
   static final String FIRST_BED = "A1-029";

   private NarratorA2SignalDetector() {
   }

   record Signal(String eventId, String source, String targetId) {
   }

   static Signal inventoryIncrease(String itemId) {
      String normalized = normalize(itemId);
      if ("minecraft:stick".equals(normalized)) {
         return new Signal(FIRST_STICK, "inventory", normalized);
      }
      return null;
   }

   static Signal blockPlaced(String blockId) {
      String normalized = normalize(blockId);
      if ("minecraft:chest".equals(normalized) || "minecraft:trapped_chest".equals(normalized)) {
         return new Signal(FIRST_CHEST, "block_placed", normalized);
      }
      if (normalized.startsWith("minecraft:") && normalized.endsWith("_bed")) {
         return new Signal(FIRST_BED, "block_placed", normalized);
      }
      return null;
   }

   private static String normalize(String id) {
      return id == null ? "" : id.trim().toLowerCase(Locale.ROOT);
   }
}
