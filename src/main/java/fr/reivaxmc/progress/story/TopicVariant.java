package fr.reivaxmc.progress.story;

import java.util.Objects;
import java.util.function.Predicate;

public record TopicVariant(String variantId, String topicKey, int priority, Predicate<StoryFact> eligibility, String debugText) {
   public TopicVariant(String variantId, String topicKey, int priority, Predicate<StoryFact> eligibility, String debugText) {
      Objects.requireNonNull(variantId, "variantId");
      Objects.requireNonNull(topicKey, "topicKey");
      eligibility = eligibility == null ? var0 -> true : eligibility;
      debugText = debugText == null ? "" : debugText;
      this.variantId = variantId;
      this.topicKey = topicKey;
      this.priority = priority;
      this.eligibility = eligibility;
      this.debugText = debugText;
   }

   public boolean matches(StoryFact var1) {
      return this.topicKey.equals(var1.topicKey()) && this.eligibility.test(var1);
   }
}
