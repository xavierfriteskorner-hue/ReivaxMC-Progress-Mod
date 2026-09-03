package fr.reivaxmc.progress.narrator;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Détecteur pur du lot A4 : dangers subis et premiers combats.
 *
 * <p>Les identifiants Minecraft et les valeurs de santé lui sont fournis par
 * l'adaptateur NeoForge. Il ne dépend donc ni de Minecraft, ni du Brain, ni du
 * HUD et peut être testé sans lancer le jeu.</p>
 */
final class NarratorA4SignalDetector {
   // Deux pilotes 0.8.0 sont conservés pour ne pas rejouer/doubler leur mémoire.
   static final String FIRST_CREEPER_KILL = "A1-078";
   static final String FIRST_DEATH = "A1-084";

   static final String FIRST_CREEPER_DAMAGE = "A1-117";
   static final String FIRST_MAJOR_FALL = "A1-118";
   static final String FIRST_NEAR_FATAL_FALL = "A1-119";
   static final String FIRST_DROWNING_SURVIVED = "A1-120";
   static final String FIRST_FIRE_DAMAGE = "A1-121";
   static final String FIRST_LAVA_DAMAGE = "A1-122";
   static final String FIRST_ZOMBIE_KILL = "A1-123";
   static final String FIRST_SKELETON_KILL = "A1-124";
   static final String FIRST_SPIDER_KILL = "A1-125";
   static final String FIRST_ENDERMAN_KILL = "A1-126";

   static final List<String> ALL_IDS = List.of(
      FIRST_CREEPER_KILL,
      FIRST_CREEPER_DAMAGE,
      FIRST_DEATH,
      FIRST_MAJOR_FALL,
      FIRST_NEAR_FATAL_FALL,
      FIRST_DROWNING_SURVIVED,
      FIRST_FIRE_DAMAGE,
      FIRST_LAVA_DAMAGE,
      FIRST_ZOMBIE_KILL,
      FIRST_SKELETON_KILL,
      FIRST_SPIDER_KILL,
      FIRST_ENDERMAN_KILL
   );

   private NarratorA4SignalDetector() {
   }

   record Signal(String eventId, String source, String targetId) {
   }

   record DamageObservation(String causeId, String sourceEntityId, float healthAfter, float healthDamage) {
      DamageObservation {
         causeId = normalize(causeId);
         sourceEntityId = normalize(sourceEntityId);
      }
   }

   static Signal playerDeath() {
      return signal(FIRST_DEATH, "DEATH", "minecraft:player");
   }

   static Signal killedByPlayer(String victimId) {
      String id = normalize(victimId);
      return switch (id) {
         case "minecraft:creeper" -> signal(FIRST_CREEPER_KILL, "KILL", id);
         case "minecraft:zombie" -> signal(FIRST_ZOMBIE_KILL, "KILL", id);
         case "minecraft:skeleton" -> signal(FIRST_SKELETON_KILL, "KILL", id);
         case "minecraft:spider" -> signal(FIRST_SPIDER_KILL, "KILL", id);
         case "minecraft:enderman" -> signal(FIRST_ENDERMAN_KILL, "KILL", id);
         default -> null;
      };
   }

   static List<Signal> damagedPlayer(DamageObservation observation) {
      if (observation == null || observation.healthDamage() <= 0.0F) {
         return List.of();
      }

      ArrayList<Signal> signals = new ArrayList<>();
      String cause = observation.causeId();
      String sourceEntity = observation.sourceEntityId();

      if ("minecraft:creeper".equals(sourceEntity)) {
         signals.add(signal(FIRST_CREEPER_DAMAGE, "DAMAGE", sourceEntity));
      }

      if (isFall(cause)) {
         if (observation.healthDamage() >= 6.0F) {
            signals.add(signal(FIRST_MAJOR_FALL, "DAMAGE", cause));
         }
         if (observation.healthAfter() > 0.0F && observation.healthAfter() <= 4.0F) {
            signals.add(signal(FIRST_NEAR_FATAL_FALL, "DAMAGE", cause));
         }
      } else if (isLava(cause)) {
         signals.add(signal(FIRST_LAVA_DAMAGE, "DAMAGE", cause));
      } else if (isFire(cause)) {
         signals.add(signal(FIRST_FIRE_DAMAGE, "DAMAGE", cause));
      }

      return List.copyOf(signals);
   }

   static boolean isDrowning(String causeId) {
      String cause = normalize(causeId).replace("_", "");
      return cause.equals("drown") || cause.equals("drowning");
   }

   static Signal drowningRecovered(boolean dangerArmed, int air, int maxAir, float health) {
      if (!dangerArmed || health <= 0.0F || maxAir <= 0 || air <= maxAir / 2) {
         return null;
      }
      return signal(FIRST_DROWNING_SURVIVED, "RECOVERY", "minecraft:drowning");
   }

   private static boolean isFall(String causeId) {
      String cause = normalize(causeId).replace("_", "");
      return cause.equals("fall") || cause.equals("stalagmite");
   }

   private static boolean isLava(String causeId) {
      return normalize(causeId).replace("_", "").equals("lava");
   }

   private static boolean isFire(String causeId) {
      String cause = normalize(causeId).replace("_", "");
      return cause.equals("infire") || cause.equals("onfire");
   }

   private static Signal signal(String eventId, String source, String targetId) {
      return new Signal(eventId, source, targetId);
   }

   private static String normalize(String id) {
      return id == null ? "" : id.trim().toLowerCase(Locale.ROOT);
   }
}
