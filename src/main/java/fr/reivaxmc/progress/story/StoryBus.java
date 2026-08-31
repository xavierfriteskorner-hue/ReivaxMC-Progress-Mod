package fr.reivaxmc.progress.story;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class StoryBus {
   private final CopyOnWriteArrayList<StoryListener> listeners = new CopyOnWriteArrayList<>();

   public void subscribe(StoryListener var1) {
      if (var1 != null && !this.listeners.contains(var1)) {
         this.listeners.add(var1);
      }
   }

   public List<TopicDecision> publish(StoryFact var1) {
      if (var1 == null) {
         return List.of();
      } else {
         ArrayList var2 = new ArrayList();

         for (StoryListener var4 : this.listeners) {
            List var5 = var4.onFact(var1);
            if (var5 != null && !var5.isEmpty()) {
               var2.addAll(var5);
            }
         }

         return List.copyOf(var2);
      }
   }

   public int listenerCount() {
      return this.listeners.size();
   }
}
