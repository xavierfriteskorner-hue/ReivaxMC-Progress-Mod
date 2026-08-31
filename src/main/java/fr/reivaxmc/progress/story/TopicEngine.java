package fr.reivaxmc.progress.story;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

public final class TopicEngine implements StoryListener {
   private static final int MAX_TRANSIENT_MUTEX = 4096;
   private static final Comparator<TopicVariant> ORDER = Comparator.comparingInt(TopicVariant::priority).reversed().thenComparing(TopicVariant::variantId);
   private final List<TopicVariant> variants = new ArrayList<>();
   private final LinkedHashMap<String, Boolean> resolvedMutex = new LinkedHashMap<String, Boolean>(256, 0.75F, true) {
      @Override
      protected boolean removeEldestEntry(Entry<String, Boolean> var1) {
         return this.size() > 4096;
      }
   };

   public synchronized void register(TopicVariant var1) {
      if (var1 != null) {
         this.variants.add(var1);
         this.variants.sort(ORDER);
      }
   }

   @Override
   public List<TopicDecision> onFact(StoryFact var1) {
      if (var1 == null) {
         return List.of();
      } else {
         String var2 = var1.factId() + "|" + var1.topicKey();
         synchronized (this.resolvedMutex) {
            if (this.resolvedMutex.containsKey(var2)) {
               return List.of();
            }
         }

         TopicVariant var12 = null;
         synchronized (this) {
            for (TopicVariant var6 : this.variants) {
               if (var6.matches(var1)) {
                  var12 = var6;
                  break;
               }
            }
         }

         if (var12 == null) {
            return List.of();
         } else {
            synchronized (this.resolvedMutex) {
               if (this.resolvedMutex.containsKey(var2)) {
                  return List.of();
               }

               this.resolvedMutex.put(var2, Boolean.TRUE);
            }

            return List.of(new TopicDecision(var1.factId(), var1.topicKey(), var12.variantId(), var12.priority(), var12.debugText()));
         }
      }
   }

   public synchronized long candidateCount(String var1) {
      return this.variants.stream().filter(var1x -> var1x.topicKey().equals(var1)).count();
   }

   public int resolvedMutexCount() {
      synchronized (this.resolvedMutex) {
         return this.resolvedMutex.size();
      }
   }
}
