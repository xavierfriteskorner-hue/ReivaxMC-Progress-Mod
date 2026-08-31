package fr.reivaxmc.progress.story;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public record StoryFact(String factId, String factType, String topicKey, String actorId, long observedAt, Map<String, String> context) {
   public StoryFact(String factId, String factType, String topicKey, String actorId, long observedAt, Map<String, String> context) {
      factId = require(factId, "factId");
      factType = require(factType, "factType");
      topicKey = require(topicKey, "topicKey");
      actorId = actorId == null ? "" : actorId;
      context = Collections.unmodifiableMap(new LinkedHashMap(context == null ? Map.of() : context));
      this.factId = factId;
      this.factType = factType;
      this.topicKey = topicKey;
      this.actorId = actorId;
      this.observedAt = observedAt;
      this.context = context;
   }

   public static StoryFact create(String var0, String var1, String var2, Map<String, String> var3) {
      return new StoryFact(UUID.randomUUID().toString(), var0, var1, var2, System.currentTimeMillis(), var3);
   }

   public static StoryFact identified(String var0, String var1, String var2, String var3, Map<String, String> var4) {
      return new StoryFact(var0, var1, var2, var3, System.currentTimeMillis(), var4);
   }

   public String value(String var1) {
      return this.context.get(var1);
   }

   private static String require(String var0, String var1) {
      Objects.requireNonNull(var0, var1);
      if (var0.isBlank()) {
         throw new IllegalArgumentException(var1 + " cannot be blank");
      } else {
         return var0;
      }
   }
}
