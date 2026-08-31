package fr.reivaxmc.progress.story;

import java.util.List;

public final class ArchiveEngine18 implements StoryListener {
   @Override
   public List<TopicDecision> onFact(StoryFact var1) {
      if (var1 != null && isMemoryFact(var1.factType())) {
         Object var2 = CampaignRuntimeContext.currentServer();
         if (var2 == null) {
            return List.of();
         } else {
            try {
               ArchiveStateData18.getForServer(var2).acceptFact(var1);
            } catch (Throwable var4) {
               System.err.println("[REIVAX Alpha18E] Archive Engine failed: " + var4);
            }

            return List.of();
         }
      } else {
         return List.of();
      }
   }

   private static boolean isMemoryFact(String var0) {
      return "ITEM_CRAFTED".equals(var0) || "BLOCK_BROKEN".equals(var0) || "BLOCK_PLACED".equals(var0) || Age1LegacyFacts.isAge1(var0);
   }
}
