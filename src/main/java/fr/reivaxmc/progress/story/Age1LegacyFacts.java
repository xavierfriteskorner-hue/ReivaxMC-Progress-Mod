package fr.reivaxmc.progress.story;

import java.util.Set;

public final class Age1LegacyFacts {
   public static final String RESONANCE = "AGE1_RESONANCE_MANIFESTED";
   public static final String STELA = "AGE1_STELA_DISCOVERED";
   public static final String FRAGMENT = "AGE1_FRAGMENT_FOUND";
   public static final String MATRIX = "AGE1_MATRIX_RECOGNIZED";
   public static final String FIRST_PAGE = "AGE1_FIRST_PAGE_RECORDED";
   public static final String FOYER = "AGE1_FOYER_FOUNDED";
   public static final String RESPONSE = "AGE1_DISTANT_RESPONSE";
   private static final Set<String> ALL = Set.of(
      "AGE1_RESONANCE_MANIFESTED",
      "AGE1_STELA_DISCOVERED",
      "AGE1_FRAGMENT_FOUND",
      "AGE1_MATRIX_RECOGNIZED",
      "AGE1_FIRST_PAGE_RECORDED",
      "AGE1_FOYER_FOUNDED",
      "AGE1_DISTANT_RESPONSE"
   );

   private Age1LegacyFacts() {
   }

   public static boolean isAge1(String var0) {
      return var0 != null && ALL.contains(var0);
   }

   public static String topicFor(String var0) {
      return "TOPIC_" + var0;
   }
}
