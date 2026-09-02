package fr.reivaxmc.progress.narrator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Cerveau contextuel data-driven de La Voix.
 *
 * <p>Le moteur Minecraft reste dans {@link NarratorLegacy}. Cette classe ne connait
 * aucune classe Minecraft : elle reçoit seulement un instantané de contexte et choisit
 * la variante narrative la plus pertinente. Cela permet de faire grossir le catalogue
 * sans transformer chaque nouvelle intervention en nouveau code Java.</p>
 *
 * <p>Les conditions JSON supportées sont volontairement génériques : égalité par
 * défaut, listes ("l'une de ces valeurs"), ainsi que les suffixes _gte, _lte, _gt,
 * _lt et _neq. Exemples : familiarity_gte, health_ratio_lte, source, other_nearby.</p>
 */
public final class NarratorContextBrain {
   private NarratorContextBrain() {
   }

   public record Snapshot(Map<String, Object> values) {
      public Snapshot {
         values = values == null ? Map.of() : Collections.unmodifiableMap(values);
      }

      public Object get(String key) {
         return values.get(key);
      }

      public int intValue(String key) {
         Object value = get(key);
         return value instanceof Number number ? number.intValue() : 0;
      }
   }

   public record Variant(
      String id,
      int priority,
      Map<String, Object> when,
      String actorText,
      String otherText,
      String tone
   ) {
      public Variant {
         id = id == null ? "" : id;
         when = when == null ? Map.of() : Collections.unmodifiableMap(when);
         actorText = actorText == null ? "" : actorText;
         otherText = otherText == null ? "" : otherText;
         tone = tone == null ? "" : tone;
      }
   }

   public record Choice(String actorText, String otherText, String variantId, String tone, int score) {
      public boolean contextual() {
         return variantId != null && !variantId.isBlank();
      }
   }

   /** Choisit la variante la plus spécifique puis la plus prioritaire. */
   public static Choice choose(List<Variant> variants, Snapshot snapshot, String fallbackActor, String fallbackOther, String seed) {
      if (variants == null || variants.isEmpty()) {
         return new Choice(fallbackActor, fallbackOther, "", "", Integer.MIN_VALUE);
      }

      Variant best = null;
      int bestScore = Integer.MIN_VALUE;
      long bestTie = Long.MIN_VALUE;
      for (Variant variant : variants) {
         if (!matches(variant, snapshot)) {
            continue;
         }

         int specificity = variant.when().size();
         int score = specificity * 100 + variant.priority();
         long tie = stableTie(seed, variant.id());
         if (best == null || score > bestScore || score == bestScore && tie > bestTie) {
            best = variant;
            bestScore = score;
            bestTie = tie;
         }
      }

      if (best == null) {
         return new Choice(fallbackActor, fallbackOther, "", "", Integer.MIN_VALUE);
      }

      String actor = best.actorText().isBlank() ? fallbackActor : best.actorText();
      String other = best.otherText().isBlank() ? fallbackOther : best.otherText();
      return new Choice(actor, other, best.id(), best.tone(), bestScore);
   }

   public static Set<String> requiredContextKeys(List<Variant> variants) {
      if (variants == null || variants.isEmpty()) {
         return Set.of();
      }

      LinkedHashSet<String> keys = new LinkedHashSet<>();
      for (Variant variant : variants) {
         for (String condition : variant.when().keySet()) {
            keys.add(baseKey(condition));
         }
      }
      return Collections.unmodifiableSet(keys);
   }

   public static String familiarityStage(int heard) {
      if (heard >= 45) {
         return "FAMILIAR";
      }
      if (heard >= 20) {
         return "ATTENTIVE";
      }
      if (heard >= 8) {
         return "OBSERVANT";
      }
      return "DISTANT";
   }

   @SuppressWarnings("unchecked")
   public static Variant fromMap(Map<?, ?> raw) {
      if (raw == null) {
         return null;
      }

      String id = string(raw.get("id"));
      int priority = number(raw.get("priority"), 0);
      String actorText = string(raw.get("actor_text"));
      String otherText = string(raw.get("other_text"));
      String tone = string(raw.get("tone"));
      Map<String, Object> when = Map.of();
      Object whenRaw = raw.get("when");
      if (whenRaw instanceof Map<?, ?> map) {
         java.util.LinkedHashMap<String, Object> copy = new java.util.LinkedHashMap<>();
         for (Map.Entry<?, ?> entry : map.entrySet()) {
            copy.put(String.valueOf(entry.getKey()), entry.getValue());
         }
         when = copy;
      }
      return new Variant(id, priority, when, actorText, otherText, tone);
   }

   private static boolean matches(Variant variant, Snapshot snapshot) {
      for (Map.Entry<String, Object> condition : variant.when().entrySet()) {
         String rawKey = condition.getKey();
         String key = baseKey(rawKey);
         Object actual = snapshot.get(key);
         Object expected = condition.getValue();
         if (!conditionMatches(rawKey, actual, expected)) {
            return false;
         }
      }
      return true;
   }

   private static boolean conditionMatches(String rawKey, Object actual, Object expected) {
      if (rawKey.endsWith("_gte")) {
         return numeric(actual) >= numeric(expected);
      }
      if (rawKey.endsWith("_lte")) {
         return numeric(actual) <= numeric(expected);
      }
      if (rawKey.endsWith("_gt")) {
         return numeric(actual) > numeric(expected);
      }
      if (rawKey.endsWith("_lt")) {
         return numeric(actual) < numeric(expected);
      }
      if (rawKey.endsWith("_neq")) {
         return !equalValue(actual, expected);
      }

      if (expected instanceof List<?> list) {
         for (Object candidate : list) {
            if (equalValue(actual, candidate)) {
               return true;
            }
         }
         return false;
      }
      return equalValue(actual, expected);
   }

   private static boolean equalValue(Object actual, Object expected) {
      if (actual instanceof Number || expected instanceof Number) {
         return Double.compare(numeric(actual), numeric(expected)) == 0;
      }
      if (actual instanceof Boolean || expected instanceof Boolean) {
         return bool(actual) == bool(expected);
      }
      if (actual == null || expected == null) {
         return Objects.equals(actual, expected);
      }
      return String.valueOf(actual).equalsIgnoreCase(String.valueOf(expected));
   }

   public static String baseKey(String conditionKey) {
      if (conditionKey == null) {
         return "";
      }
      for (String suffix : List.of("_gte", "_lte", "_neq", "_gt", "_lt")) {
         if (conditionKey.endsWith(suffix)) {
            return conditionKey.substring(0, conditionKey.length() - suffix.length());
         }
      }
      return conditionKey;
   }

   private static double numeric(Object value) {
      if (value instanceof Number number) {
         return number.doubleValue();
      }
      if (value == null) {
         return 0.0;
      }
      try {
         return Double.parseDouble(String.valueOf(value));
      } catch (NumberFormatException ignored) {
         return 0.0;
      }
   }

   private static boolean bool(Object value) {
      if (value instanceof Boolean bool) {
         return bool;
      }
      return value != null && Boolean.parseBoolean(String.valueOf(value));
   }

   private static String string(Object value) {
      return value == null ? "" : String.valueOf(value);
   }

   private static int number(Object value, int fallback) {
      return value instanceof Number number ? number.intValue() : fallback;
   }

   private static long stableTie(String seed, String variantId) {
      String value = (seed == null ? "" : seed) + "|" + (variantId == null ? "" : variantId);
      long hash = 1125899906842597L;
      for (int i = 0; i < value.length(); i++) {
         hash = 31L * hash + value.charAt(i);
      }
      return hash;
   }
}
