package fr.reivaxmc.progress.story;

import java.util.List;
import java.util.Map;

public final class StoryRuntime {
   public static final String CORE_TOPIC = "SYSTEM_ALPHA18_CORE";
   public static final String PILOT_CRAFT = "PILOT_REAL_CRAFT";
   public static final String PILOT_BREAK = "PILOT_REAL_BREAK";
   public static final String PILOT_PLACE = "PILOT_REAL_PLACE";
   private static final StoryBus BUS = new StoryBus();
   private static final TopicEngine TOPICS = new TopicEngine();
   private static final CampaignEngine18 CAMPAIGN = new CampaignEngine18();
   private static final ArchiveEngine18 ARCHIVES = new ArchiveEngine18();
   private static final ChronicleEngine18 CHRONICLE = new ChronicleEngine18();

   private StoryRuntime() {
   }

   private static void registerPilot(String var0, String var1) {
      TOPICS.register(new TopicVariant(var1 + ".fallback", var0, 10, var0x -> true, "generic"));
      TOPICS.register(new TopicVariant(var1 + ".contextual", var0, 100, var0x -> {
         String var1x = var0x.value("subject");
         return var1x != null && !var1x.isBlank() && !"unknown".equals(var1x);
      }, "contextual"));
   }

   private static void registerAge1(String var0) {
      String var1 = Age1LegacyFacts.topicFor(var0);
      TOPICS.register(new TopicVariant("alpha18e.age1." + var0.toLowerCase(), var1, 100, var0x -> true, "legacy-routed"));
   }

   public static List<TopicDecision> publish(StoryFact var0) {
      return BUS.publish(var0);
   }

   public static long candidateCount(String var0) {
      return TOPICS.candidateCount(var0);
   }

   public static StoryRuntime.SelfTestResult selfTest(String var0) {
      StoryFact var1 = StoryFact.create("ALPHA18_SELFTEST", "SYSTEM_ALPHA18_CORE", var0, Map.of("source", "LOGIN_PROBE"));
      List var2 = publish(var1);
      List var3 = publish(var1);
      boolean var4 = var2.size() == 1;
      boolean var5 = var4 && "alpha18.core.preferred".equals(((TopicDecision)var2.getFirst()).variantId());
      boolean var6 = var3.isEmpty();
      boolean var7 = TOPICS.candidateCount("SYSTEM_ALPHA18_CORE") == 2L;
      boolean var8 = BUS.listenerCount() == 4;
      return new StoryRuntime.SelfTestResult(
         var4 && var5 && var6 && var7 && var8,
         var2.isEmpty() ? "none" : ((TopicDecision)var2.getFirst()).variantId(),
         TOPICS.candidateCount("SYSTEM_ALPHA18_CORE"),
         var3.size(),
         BUS.listenerCount()
      );
   }

   static {
      TOPICS.register(new TopicVariant("alpha18.core.fallback", "SYSTEM_ALPHA18_CORE", 10, var0 -> true, "fallback"));
      TOPICS.register(new TopicVariant("alpha18.core.preferred", "SYSTEM_ALPHA18_CORE", 100, var0 -> "LOGIN_PROBE".equals(var0.value("source")), "preferred"));
      registerPilot("PILOT_REAL_CRAFT", "alpha18b.craft");
      registerPilot("PILOT_REAL_BREAK", "alpha18b.break");
      registerPilot("PILOT_REAL_PLACE", "alpha18b.place");
      registerAge1("AGE1_RESONANCE_MANIFESTED");
      registerAge1("AGE1_STELA_DISCOVERED");
      registerAge1("AGE1_FRAGMENT_FOUND");
      registerAge1("AGE1_MATRIX_RECOGNIZED");
      registerAge1("AGE1_FIRST_PAGE_RECORDED");
      registerAge1("AGE1_FOYER_FOUNDED");
      registerAge1("AGE1_DISTANT_RESPONSE");
      BUS.subscribe(TOPICS);
      BUS.subscribe(CAMPAIGN);
      BUS.subscribe(ARCHIVES);
      BUS.subscribe(CHRONICLE);
   }

   public static record SelfTestResult(boolean ok, String winner, long candidateCount, int duplicateDecisionCount, int listenerCount) {
   }
}
