package fr.reivaxmc.progress.narrator;

import java.io.InputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public final class NarratorLegacy {
   private static final String KV = "__N17KV__";
   private static final String FLAG = "__N17FLAG__";
   private static final long SOURCE_HINT_MS = 8000L;
   private static final long NOTIFICATION_GAP_MS = 6400L;
   private static final long QUIET_AFTER_MS = 180000L;
   private static final long QUIET_COOLDOWN_MS = 900000L;
   private static final Map<Object, NarratorLegacy.ServerState> SERVERS = Collections.synchronizedMap(new WeakHashMap<>());
   private static final Map<String, NarratorLegacy.PlayerState> PLAYERS = new ConcurrentHashMap<>();
   private static final Map<String, NarratorLegacy.EventDef> CATALOG = loadCatalog();
   private static volatile boolean listenersTried = false;
   private static volatile boolean debugPlayerTickSeen = false;
   private static volatile boolean debugInventorySeen = false;

   private NarratorLegacy() {
   }

   public static void onLogin(Object var0) {
      try {
         Object var1 = call(var0, "getEntity");
         if (!isServerPlayer(var1)) {
            return;
         }

         ensurePlayer(var1);
         touch(serverOf(var1));
      } catch (Throwable var2) {
         soft("login", var2);
      }
   }

   public static void onServerTick(Object var0) {
      try {
         Object var1 = call(var0, "getServer");
         if (var1 == null) {
            return;
         }

         NarratorLegacy.ServerState var2 = state(var1);
         Object var3 = campaignData(var1);
         if (var3 == null) {
            return;
         }

         bridgeStoryState(var1, var3, var2);
         processQueue(var1, var3, var2);
         maybeQuietWindow(var1, var3, var2);
      } catch (Throwable var4) {
         soft("serverTick", var4);
      }
   }

   public static void onPlayerTick(Object var0) {
      try {
         if (!debugPlayerTickSeen) {
            debugPlayerTickSeen = true;
            System.out.println("[REIVAX EVENT DEBUG] NarratorLegacy.onPlayerTick atteint; catalogue=" + CATALOG.size());
         }
         Object var1 = call(var0, "getEntity");
         if (!isServerPlayer(var1)) {
            return;
         }

         Object var2 = serverOf(var1);
         if (var2 == null) {
            return;
         }

         NarratorLegacy.PlayerState var3 = ensurePlayer(var1);
         NarratorLegacy.ServerState var4 = state(var2);
         float var5 = asFloat(callQuiet(var1, "getHealth"), var3.lastHealth);
         if (!Float.isNaN(var3.lastHealth) && var5 < var3.lastHealth) {
            var4.lastFactAt = now();
            inspectDamageContext(var1, var3, var5);
         }

         if (Math.abs(var5 - 1.0F) < 0.001F && (Float.isNaN(var3.lastHealth) || var3.lastHealth > 1.0F)) {
            trigger(var1, "A1-087", "UNKNOWN", null, Map.of("health", var5));
         }

         var3.lastHealth = var5;
         // Inventory deltas are the generic sensor used by several pilot events
         // (wood, stone, coal, iron, diamond). Scan at 2 Hz: responsive enough
         // for narration while avoiding a full inventory walk every game tick.
         if (now() - var3.lastInventoryScanAt >= 500L) {
            var3.lastInventoryScanAt = now();
            Object campaign = campaignData(var2);
            if (campaign != null) {
               inventoryScan(var1, campaign);
            }
         }
         observeHomeDistance(var1, var3);
         observeDuoDistance(var1);
         observeRecentGift(var1, var3);
      } catch (Throwable var6) {
         soft("playerTick", var6);
      }
   }

   public static void inventoryScan(Object var0, Object var1) {
      try {
         if (!isServerPlayer(var0) || var1 == null) {
            return;
         }

         NarratorLegacy.PlayerState var2 = ensurePlayer(var0);
         Map<String, Integer> var3 = inventoryCounts(var0);
         if (!debugInventorySeen) {
            debugInventorySeen = true;
            System.out.println("[REIVAX EVENT DEBUG] inventoryScan atteint; catalogue=" + CATALOG.size() + "; items=" + var3.size());
         }
         if (!var2.inventoryInitialized) {
            var2.inventoryInitialized = true;
            mirrorLegacy(var1, "first_wood", "A1-001");
            mirrorLegacy(var1, "first_stone", "A1-005");
            mirrorLegacy(var1, "first_coal", "A1-007");
            mirrorLegacy(var1, "first_iron", "A1-009");
         }

         HashSet<String> var4 = new HashSet<>();
         var4.addAll(var3.keySet());
         var4.addAll(var2.itemCounts.keySet());

         for (String var6 : var4) {
            int var7 = var2.itemCounts.getOrDefault(var6, 0);
            int var8 = var3.getOrDefault(var6, 0);
            if (var8 > var7) {
               int var9 = var8 - var7;
               if (!consumeNarratorCredit(var2, var6, var9)) {
                  NarratorLegacy.SourceHint var10 = var2.hints.get(var6);
                  String var11 = sourceFor(var0, var10);
                  var2.hints.remove(var6);
                  HashMap var12 = new HashMap();
                  var12.put("item", var6);
                  var12.put("delta", var9);
                  if (isWood(var6)) {
                     trigger(var0, "A1-001", var11, var10, var12);
                  }

                  if (var6.equals("minecraft:stone") || var6.equals("minecraft:cobblestone")) {
                     trigger(var0, "A1-005", var11, var10, var12);
                  }

                  if (var6.equals("minecraft:coal") || var6.equals("minecraft:charcoal")) {
                     trigger(var0, "A1-007", var11, var10, var12);
                  }

                  if (var6.equals("minecraft:raw_iron")) {
                     trigger(var0, "A1-009", var11, var10, var12);
                  }

                  if (var6.equals("minecraft:diamond")) {
                     trigger(var0, "A1-018", var11, var10, var12);
                  }
               }
            }
         }

         var2.itemCounts.clear();
         var2.itemCounts.putAll(var3);
      } catch (Throwable var13) {
         soft("inventoryScan", var13);
      }
   }

   public static void onBlockPlaced(Object var0) {
      try {
         Object var1 = callQuiet(var0, "getEntity");
         if (!isServerPlayer(var1)) {
            return;
         }

         Object var2 = callQuiet(var0, "getPlacedBlock");
         Object var3 = callQuiet(var0, "getPos");
         String var4 = blockId(var2);
         NarratorLegacy.ServerState var5 = state(serverOf(var1));
         var5.lastFactAt = now();
         String var6 = positionKey(var1, var3);
         if (isContainerBlock(var4) && var6 != null) {
            var5.playerPlacedContainers.add(var6);
         }

         if (var4.contains("origin_matrix")) {
            var5.lastMatrixActorUuid = uuid(var1);
         }

         if (isCropBlock(var4)) {
            trigger(var1, "A1-024", "PLACED", null, Map.of("block", var4));
         }

         Object var7 = campaignData(serverOf(var1));
         if (var7 != null && insideHome(var7, var1, var3)) {
            long var8 = kvInc(var7, "home_blocks", 1L);
            if (var8 >= 500L) {
               trigger(var1, "A1-060", "PLACED", null, Map.of("count", var8));
            }
         }
      } catch (Throwable var10) {
         soft("blockPlaced", var10);
      }
   }

   public static void onBlockBroken(Object var0) {
      try {
         Object var1 = callQuiet(var0, "getPlayer");
         if (!isServerPlayer(var1)) {
            return;
         }

         Object var2 = callQuiet(var0, "getState");
         String var3 = blockId(var2);
         NarratorLegacy.PlayerState var4 = ensurePlayer(var1);
         state(serverOf(var1)).lastFactAt = now();
         if (var3.contains("diamond_ore")) {
            var4.hints.put("minecraft:diamond", new NarratorLegacy.SourceHint("MINED", now(), null));
         }

         if (var3.contains("coal_ore")) {
            var4.hints.put("minecraft:coal", new NarratorLegacy.SourceHint("MINED", now(), null));
         }

         if (var3.contains("iron_ore")) {
            var4.hints.put("minecraft:raw_iron", new NarratorLegacy.SourceHint("MINED", now(), null));
         }

         if (!var3.endsWith("_log") && !var3.endsWith("_stem") && !var3.endsWith("_hyphae") && var3.endsWith("_wood")) {
         }
      } catch (Throwable var5) {
         soft("blockBroken", var5);
      }
   }

   public static void onRightClickBlock(Object var0) {
      try {
         Object var1 = callQuiet(var0, "getEntity");
         if (!isServerPlayer(var1)) {
            return;
         }

         Object var2 = callQuiet(var0, "getLevel");
         Object var3 = callQuiet(var0, "getPos");
         Object var4 = var2 != null && var3 != null ? callQuiet(var2, "getBlockState", var3) : null;
         String var5 = blockId(var4);
         NarratorLegacy.PlayerState var6 = ensurePlayer(var1);
         state(serverOf(var1)).lastFactAt = now();
         if (isContainerBlock(var5)) {
            String var7 = positionKey(var1, var3);
            String var8 = var7 != null && state(serverOf(var1)).playerPlacedContainers.contains(var7) ? "PLAYER_STORAGE" : "UNKNOWN_CONTAINER";
            var6.containerHint = new NarratorLegacy.SourceHint(var8, now(), null);
         }

         if (var5.endsWith(":origin_matrix") || var5.contains("origin_matrix")) {
            var6.lastMatrixInteractionAt = now();
            state(serverOf(var1)).lastMatrixActorUuid = uuid(var1);
         }
      } catch (Throwable var9) {
         soft("rightClickBlock", var9);
      }
   }

   public static void onRightClickItem(Object var0) {
      try {
         Object var1 = callQuiet(var0, "getEntity");
         if (isServerPlayer(var1)) {
            state(serverOf(var1)).lastFactAt = now();
         }
      } catch (Throwable var2) {
      }
   }

   public static void onCommands(Object var0) {
      try {
         registerSimpleCommand(var0, "reivax17_info", var0x -> debugInfo(var0x));
         registerSimpleCommand(var0, "reivax17_reset", var0x -> resetPilots(var0x));
         registerSimpleCommand(var0, "reivax17_half", var0x -> trigger(var0x, "A1-087", "UNKNOWN", null, Map.of("debug", true)));
         registerSimpleCommand(var0, "reivax17_lightning", var0x -> trigger(var0x, "A1-090", "UNKNOWN", null, Map.of("debug", true)));
         registerSimpleCommand(var0, "reivax17_village", var0x -> trigger(var0x, "A1-040", "UNKNOWN", null, Map.of("debug", true)));
         registerSimpleCommand(
            var0,
            "reivax17_quiet",
            var0x -> enqueueObservation(var0x, "OBS-PILOT-02", "", "Je constate que rien d’intéressant ne se produit actuellement. Continuez.")
         );
      } catch (Throwable var2) {
         soft("commands", var2);
      }
   }

   private static synchronized void ensureDynamicListeners() {
      if (!listenersTried) {
         listenersTried = true;

         try {
            Class var0 = Class.forName("net.neoforged.neoforge.common.NeoForge");
            Object var1 = var0.getField("EVENT_BUS").get(null);
            addDynamic(var1, "net.neoforged.neoforge.event.entity.player.PlayerEvent$ItemCraftedEvent", NarratorLegacy::onItemCrafted);
            addDynamic(var1, "net.neoforged.neoforge.event.entity.player.PlayerEvent$ItemSmeltedEvent", NarratorLegacy::onItemSmelted);
            addDynamic(var1, "net.neoforged.neoforge.event.entity.living.LivingDeathEvent", NarratorLegacy::onLivingDeath);
            addDynamic(var1, "net.neoforged.neoforge.event.entity.living.AnimalTameEvent", NarratorLegacy::onAnimalTame);
            addDynamic(var1, "net.neoforged.neoforge.event.entity.item.ItemTossEvent", NarratorLegacy::onItemToss);
            addDynamic(var1, "net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent$Post", NarratorLegacy::onItemPickup);
         } catch (Throwable var2) {
            soft("dynamicListeners", var2);
         }
      }
   }

   private static void addDynamic(Object var0, String var1, Consumer<Object> var2) {
      try {
         Class var3 = Class.forName(var1);
         Method var4 = null;

         for (Method var8 : var0.getClass().getMethods()) {
            if (var8.getName().equals("addListener")) {
               Class[] var9 = var8.getParameterTypes();
               boolean var10 = false;
               boolean var11 = false;

               for (Class var15 : var9) {
                  if (var15 == Class.class) {
                     var10 = true;
                  }

                  if (Consumer.class.isAssignableFrom(var15)) {
                     var11 = true;
                  }
               }

               if (var10 && var11) {
                  var4 = var8;
                  break;
               }
            }
         }

         if (var4 == null) {
            return;
         }

         Object[] var17 = new Object[var4.getParameterCount()];
         Class[] var18 = var4.getParameterTypes();

         for (int var19 = 0; var19 < var18.length; var19++) {
            if (var18[var19] == Class.class) {
               var17[var19] = var3;
            } else if (Consumer.class.isAssignableFrom(var18[var19])) {
               var17[var19] = var2;
            } else if (var18[var19] == boolean.class) {
               var17[var19] = false;
            } else if (!var18[var19].isEnum()) {
               var17[var19] = null;
            } else {
               Object var20 = var18[var19].getEnumConstants()[0];

               for (Object var24 : var18[var19].getEnumConstants()) {
                  if (String.valueOf(var24).equals("NORMAL")) {
                     var20 = var24;
                  }
               }

               var17[var19] = var20;
            }
         }

         var4.invoke(var0, var17);
      } catch (Throwable var16) {
      }
   }

   public static void onItemCrafted(Object var0) {
      try {
         Object var1 = callQuiet(var0, "getEntity");
         if (!isServerPlayer(var1)) {
            return;
         }

         Object var2 = callQuiet(var0, "getCrafting");
         if (var2 == null) {
            var2 = callQuiet(var0, "getItemStack");
         }

         String var3 = stackItemId(var2);
         NarratorLegacy.PlayerState var4 = ensurePlayer(var1);
         if (var3 != null) {
            var4.hints.put(var3, new NarratorLegacy.SourceHint("CRAFTED", now(), null));
         }

         state(serverOf(var1)).lastFactAt = now();
         if ("minecraft:crafting_table".equals(var3)) {
            trigger(var1, "A1-003", "CRAFTED", null, Map.of("item", var3));
         }
      } catch (Throwable var5) {
         soft("crafted", var5);
      }
   }

   public static void onItemSmelted(Object var0) {
      try {
         Object var1 = callQuiet(var0, "getEntity");
         if (!isServerPlayer(var1)) {
            return;
         }

         Object var2 = callQuiet(var0, "getSmelting");
         if (var2 == null) {
            var2 = callQuiet(var0, "getItemStack");
         }

         String var3 = stackItemId(var2);
         if (var3 != null) {
            ensurePlayer(var1).hints.put(var3, new NarratorLegacy.SourceHint("SMELTED", now(), null));
         }

         state(serverOf(var1)).lastFactAt = now();
         if (var3 != null && var3.endsWith("_ingot")) {
            trigger(var1, "A1-010", "SMELTED", null, Map.of("item", var3));
         }
      } catch (Throwable var4) {
         soft("smelted", var4);
      }
   }

   public static void onLivingDeath(Object var0) {
      try {
         Object var1 = callQuiet(var0, "getEntity");
         String var2 = entityId(var1);
         Object var3 = callQuiet(var0, "getSource");
         Object var4 = var3 == null ? null : callQuiet(var3, "getEntity");
         if (isServerPlayer(var1)) {
            state(serverOf(var1)).lastFactAt = now();
            trigger(var1, "A1-084", "DEATH", null, Map.of("cause", damageId(var3)));
            return;
         }

         if (isServerPlayer(var4) && "minecraft:creeper".equals(var2)) {
            state(serverOf(var4)).lastFactAt = now();
            trigger(var4, "A1-078", "KILL", null, Map.of("entity", var2));
         }
      } catch (Throwable var6) {
         soft("livingDeath", var6);
      }
   }

   public static void onAnimalTame(Object var0) {
      try {
         Object var1 = callQuiet(var0, "getTamer");
         if (!isServerPlayer(var1)) {
            var1 = callQuiet(var0, "getPlayer");
         }

         if (!isServerPlayer(var1)) {
            return;
         }

         state(serverOf(var1)).lastFactAt = now();
         trigger(var1, "A1-073", "TAME", null, Map.of());
      } catch (Throwable var2) {
         soft("animalTame", var2);
      }
   }

   public static void onItemToss(Object var0) {
      try {
         Object var1 = callQuiet(var0, "getPlayer");
         if (!isServerPlayer(var1)) {
            return;
         }

         Object var2 = callQuiet(var0, "getEntity");
         Object var3 = var2 == null ? null : callQuiet(var2, "getItem");
         String var4 = stackItemId(var3);
         NarratorLegacy.PlayerState var5 = ensurePlayer(var1);
         state(serverOf(var1)).lastFactAt = now();
         long var6 = now() - var5.recentGiftAt;
         if (var4 != null && var5.recentGiftItem != null && var4.equals(var5.recentGiftItem) && !var5.giftCallbackDone && var6 > 1500L && var6 < 30000L) {
            var5.giftCallbackDone = true;
            enqueueObservation(var1, "OBS-PILOT-01", "…JE VOIS.", "Je vois. Je prendrai cela comme une réponse.");
         }
      } catch (Throwable var8) {
         soft("itemToss", var8);
      }
   }

   public static void onItemPickup(Object var0) {
      try {
         Object var1 = callQuiet(var0, "getPlayer");
         if (!isServerPlayer(var1)) {
            return;
         }

         Object var2 = callQuiet(var0, "getOriginalStack");
         String var3 = stackItemId(var2);
         if (var3 == null) {
            return;
         }

         NarratorLegacy.PlayerState var4 = ensurePlayer(var1);
         NarratorLegacy.SourceHint var5 = var4.hints.get(var3);
         if (var5 == null || !"MINED".equals(var5.source) || now() - var5.at >= 2000L) {
            var4.hints.put(var3, new NarratorLegacy.SourceHint("WORLD_PICKUP", now(), null));
         }

         state(serverOf(var1)).lastFactAt = now();
      } catch (Throwable var6) {
         soft("itemPickup", var6);
      }
   }

   private static void inspectDamageContext(Object var0, NarratorLegacy.PlayerState var1, float var2) {
      try {
         Object var3 = callQuiet(var0, "getLastDamageSource");
         String var4 = damageId(var3);
         Object var5 = var3 == null ? null : callQuiet(var3, "getDirectEntity");
         Object var6 = var3 == null ? null : callQuiet(var3, "getEntity");
         String var7 = entityId(var6 != null ? var6 : var5);
         if (var4.contains("lightning")) {
            trigger(var0, "A1-090", "DAMAGE", null, Map.of("cause", var4));
         }

         if (var4.contains("explosion") || "minecraft:creeper".equals(var7)) {
            Object var8 = campaignData(serverOf(var0));
            if (var8 != null && insideHome(var8, var0, callQuiet(var0, "blockPosition"))) {
               trigger(var0, "A1-083", "DAMAGE", null, Map.of("cause", var4, "entity", var7));
            }
         }
      } catch (Throwable var9) {
      }
   }

   private static void observeHomeDistance(Object var0, NarratorLegacy.PlayerState var1) {
      try {
         Object var2 = serverOf(var0);
         Object var3 = campaignData(var2);
         if (var3 == null || !asBool(callQuiet(var3, "foundationPlaced"))) {
            return;
         }

         Object var4 = callQuiet(var3, "foundationPos");
         if (var4 == null) {
            return;
         }

         String var5 = String.valueOf(callQuiet(var3, "foundationDimension"));
         String var6 = dimensionId(var0);
         if (!Objects.equals(var5, var6)) {
            var1.away = true;
            var1.maxAway = Math.max(var1.maxAway, 2500.0);
            return;
         }

         double var7 = distance(callQuiet(var0, "blockPosition"), var4);
         if (var7 >= 250.0) {
            trigger(var0, "A1-043", "LOCATION", null, Map.of("distance", (int)var7));
         }

         int var9 = asInt(callQuiet(var3, "territoryRadius"), 96);
         if (var7 > (double)var9) {
            var1.away = true;
            var1.maxAway = Math.max(var1.maxAway, var7);
         } else if (var1.away) {
            if (var1.maxAway >= 1000.0) {
               trigger(var0, "A1-047", "RETURN", null, Map.of("max_distance", (int)var1.maxAway));
            }

            var1.away = false;
            var1.maxAway = 0.0;
         }
      } catch (Throwable var10) {
      }
   }

   private static void observeDuoDistance(Object var0) {
      try {
         Object var1 = serverOf(var0);
         List var2 = players(var1);
         if (var2.size() < 2) {
            return;
         }

         String var3 = uuid(var0);

         for (Object var5 : var2) {
            if (var5 != var0 && !Objects.equals(uuid(var5), var3) && Objects.equals(dimensionId(var0), dimensionId(var5))) {
               double var6 = distance(callQuiet(var0, "blockPosition"), callQuiet(var5, "blockPosition"));
               if (var6 >= 1000.0) {
                  HashMap var8 = new HashMap();
                  var8.put("other", var5);
                  var8.put("distance", (int)var6);
                  trigger(var0, "A1-049", "DISTANCE", null, var8);
                  return;
               }
            }
         }
      } catch (Throwable var9) {
      }
   }

   private static void bridgeStoryState(Object var0, Object var1, NarratorLegacy.ServerState var2) {
      try {
         boolean var3 = asBool(callQuiet(var1, "foundationPlaced"));
         boolean var4 = asBool(callQuiet(var1, "matrixInstalled"));
         boolean var5 = asBool(callQuiet(var1, "stelaPlaced"));
         boolean var6 = asBool(callQuiet(var1, "stelaDiscovered"));
         boolean var7 = asBool(callQuiet(var1, "matrixDiscovered"));
         boolean var8 = asBool(callQuiet(var1, "nightSeen"));
         if (!var2.storyInitialized) {
            var2.storyInitialized = true;
            var2.foundation = var3;
            var2.matrix = var4;
            var2.stelaPlaced = var5;
            var2.stela = var6;
            var2.matrixDisc = var7;
            var2.nightSeen = var8;
            return;
         }

         Object var9 = firstPlayer(var0);
         if (!var2.foundation && var3) {
            Object var10 = foundationActor(var1, var0);
            if (var10 != null) {
               var9 = var10;
            }

            if (var9 != null) {
               trigger(var9, "A1-051", "STORY", null, Map.of());
            }
         }

         if (!var2.matrix && var4) {
            Object var12 = playerByUuid(var0, var2.lastMatrixActorUuid);
            if (var12 != null) {
               var9 = var12;
            }

            if (var9 != null) {
               trigger(var9, "A1-066", "STORY", null, Map.of());
            }
         }

         if (var8 && !asBool(callQuiet(var1, "isCompleted", "A1-031")) && isDawn(var0) && var9 != null) {
            trigger(var9, "A1-031", "STORY", null, Map.of());
            var2.nextDeliveryAt = Math.max(var2.nextDeliveryAt, now() + 6500L);
         }

         if (!var2.stelaPlaced && var5) {
            silentComplete(var1, "A1-096", 20, 5);
            var2.nextDeliveryAt = Math.max(var2.nextDeliveryAt, now() + 6500L);
         }

         if (!var2.stela && var6) {
            silentComplete(var1, "A1-097", 25, 5);
         }

         if (!var2.matrixDisc && var7) {
            silentComplete(var1, "A1-099", 25, 5);
         }

         var2.foundation = var3;
         var2.matrix = var4;
         var2.stelaPlaced = var5;
         var2.stela = var6;
         var2.matrixDisc = var7;
         var2.nightSeen = var8;
      } catch (Throwable var11) {
      }
   }

   private static boolean isDawn(Object var0) {
      try {
         Object var1 = call(var0, "overworld");
         long var2 = ((Number)call(var1, "getDayTime")).longValue();
         long var4 = var2 % 24000L;
         return var2 > 12000L && var4 < 700L;
      } catch (Throwable var6) {
         return false;
      }
   }

   private static Object foundationActor(Object var0, Object var1) {
      try {
         Object var2 = fieldQuiet(var0, "foundationFounderUuid");
         return playerByUuid(var1, var2 == null ? null : String.valueOf(var2));
      } catch (Throwable var3) {
         return null;
      }
   }

   private static Object playerByUuid(Object var0, String var1) {
      if (var1 != null && !var1.isBlank()) {
         for (Object var3 : players(var0)) {
            if (var1.equals(uuid(var3))) {
               return var3;
            }
         }

         return null;
      } else {
         return null;
      }
   }

   private static void maybeQuietWindow(Object var0, Object var1, NarratorLegacy.ServerState var2) {
      long var3 = now();
      if (asBool(callQuiet(var1, "introCompleted"))) {
         if (var2.queue.isEmpty() && var3 - var2.lastFactAt >= 180000L && var3 - var2.lastDeliveredAt >= 180000L && var3 - var2.lastQuietAt >= 900000L) {
            Object var5 = firstPlayer(var0);
            if (var5 != null) {
               var2.lastQuietAt = var3;
               enqueueObservation(var5, "OBS-PILOT-02", "", "Je constate que rien d’intéressant ne se produit actuellement. Continuez.");
            }
         }
      }
   }

   private static void trigger(Object var0, String var1, String var2, NarratorLegacy.SourceHint var3, Map<String, Object> var4) {
      try {
         System.out.println("[REIVAX EVENT DEBUG] tentative trigger " + var1 + " source=" + var2);
         NarratorLegacy.EventDef var5 = CATALOG.get(var1);
         if (var5 == null) {
            System.out.println("[REIVAX EVENT DEBUG] REFUS " + var1 + ": absent du catalogue (taille=" + CATALOG.size() + ")");
            return;
         }

         Object var6 = serverOf(var0);
         Object var7 = campaignData(var6);
         if (var6 == null || var7 == null) {
            System.out.println("[REIVAX EVENT DEBUG] REFUS " + var1 + ": serveur ou CampaignSavedData introuvable");
            return;
         }

         // Alpha 18F has its own canonical story-start state. CampaignSavedData.introCompleted()
         // belongs to the older awakening intro and is NOT toggled by the current
         // "Commencer l'histoire" button. Using it here made every A1 event look as if
         // the story had never started, even though StoryOpening18F had started it.
         boolean varStoryStarted = storyStarted(var6);
         System.out.println("[REIVAX EVENT DEBUG] gate histoire " + var1 + ": started=" + varStoryStarted);
         if (var1.startsWith("A1-") && !varStoryStarted && !"STORY".equals(var2)) {
            System.out.println("[REIVAX EVENT DEBUG] IGNORE " + var1 + ": histoire non commencée (StoryStartState18F.started=false)");
            return;
         }

         if (asBool(callQuiet(var7, "isCompleted", var1))) {
            System.out.println("[REIVAX EVENT DEBUG] REFUS " + var1 + ": déjà complété");
            return;
         }

         NarratorLegacy.ServerState var8 = state(var6);

         for (NarratorLegacy.Pending var10 : var8.queue) {
            if (var10.id.equals(var1)) {
               return;
            }
         }

         String var16 = var5.actorText;
         String var18 = var5.otherText;
         if ("A1-018".equals(var1)) {
            float var11 = asFloat(callQuiet(var0, "getHealth"), 20.0F);
            if (var11 <= 8.0F && var5.lowHealthText != null) {
               var16 = var5.lowHealthText;
            } else if (var2 != null && var5.sourceVariants.containsKey(var2)) {
               var16 = var5.sourceVariants.get(var2);
            }

            if ("NATURAL_CONTAINER".equals(var2) && var5.otherContainerText != null) {
               var18 = var5.otherContainerText;
            }
         }

         if ("A1-049".equals(var1) && asInt(var4.get("distance"), 0) >= 2500 && var5.longDistanceText != null) {
            var16 = var5.longDistanceText;
            var18 = var5.longDistanceText;
         }

         Object var20 = var4.get("other");
         String var12 = name(var0);
         var16 = replaceVars(var16, var12, var20 == null ? "votre compagnon" : name(var20));
         var18 = replaceVars(var18, var12, "{recipient}");
         NarratorLegacy.Pending var13 = new NarratorLegacy.Pending(var1, var0, var5, var16, var18, now(), var2, var4);
         NarratorLegacy.ServerState var14 = state(var6);
         var14.lastFactAt = now();
         var14.queue.add(var13);
         System.out.println("[REIVAX EVENT DEBUG] ACCEPTÉ " + var1 + " -> file narrateur");
      } catch (Throwable var15) {
         soft("trigger " + var1, var15);
      }
   }

   private static void enqueueObservation(Object var0, String var1, String var2, String var3) {
      try {
         NarratorLegacy.EventDef var4 = new NarratorLegacy.EventDef();
         var4.id = var1;
         var4.title = var2;
         var4.actorText = var3;
         var4.otherText = var3;
         var4.kind = "NARRATOR_WHISPER";
         var4.priority = 15;
         var4.agePoints = 0;
         var4.civScore = 0;
         state(serverOf(var0)).queue.add(new NarratorLegacy.Pending(var1, var0, var4, var3, var3, now(), "OBSERVATION", Map.of()));
      } catch (Throwable var5) {
      }
   }

   private static void processQueue(Object var0, Object var1, NarratorLegacy.ServerState var2) {
      long var3 = now();
      if (var3 >= var2.nextDeliveryAt && !var2.queue.isEmpty()) {
         NarratorLegacy.Pending var5 = null;

         for (NarratorLegacy.Pending var7 : var2.queue) {
            if (var5 == null || var7.def.priority > var5.def.priority) {
               var5 = var7;
            }
         }

         if (var5 != null) {
            var2.queue.remove(var5);
            if (var5.def.priority >= 30 || var3 - var5.createdAt <= 120000L) {
               System.out.println("[REIVAX EVENT DEBUG] DEFILE " + var5.id + " -> deliver");
               deliver(var0, var1, var5);
               var2.lastDeliveredAt = var3;
               var2.nextDeliveryAt = var3 + 6400L;
            } else {
               System.out.println("[REIVAX EVENT DEBUG] DROP " + var5.id + ": TTL dépassé");
            }
         }
      }
   }

   private static void deliver(Object var0, Object var1, NarratorLegacy.Pending var2) {
      if (!var2.id.startsWith("A1-") || asBool(callQuiet(var1, "complete", var2.id, var2.def.agePoints, var2.def.civScore))) {
         if (var2.id.startsWith("A1-")) {
            addTimeline(var1, var0, var2.actor, var2.def.title, var2.actorText);
         }

         List var3 = players(var0);
         String var4 = uuid(var2.actor);
         String var5 = name(var2.actor);

         for (Object var7 : var3) {
            boolean var8 = Objects.equals(uuid(var7), var4);
            String var9 = var8 ? var2.actorText : var2.otherText;
            var9 = var9.replace("{recipient}", name(var7));
            if ("A1-084".equals(var2.id)
               && !var8
               && sameDimension(var7, var2.actor)
               && distance(callQuiet(var7, "blockPosition"), callQuiet(var2.actor, "blockPosition")) < 32.0) {
               var9 = "Vous avez assisté à cela. Je suppose que vous saurez choisir vos mots.";
            }

            sendIndividual(var7, var1, var2.id, var2.def.kind, var2.def.title, var9, var2.def.agePoints);
         }

         if (var2.def.rewardItem != null && !var2.def.rewardItem.isBlank()) {
            reward(var2.actor, var2.def);
         }
      }
   }

   private static void reward(Object var0, NarratorLegacy.EventDef var1) {
      try {
         Object var2 = itemById(var1.rewardItem);
         if (var2 == null) {
            return;
         }

         Object var3 = newItemStack(var2, var1.rewardCount);
         if (var3 == null) {
            return;
         }

         if (var1.rewardName != null && !var1.rewardName.isBlank()) {
            setCustomName(var3, var1.rewardName);
         }

         boolean var10000;
         label31: {
            NarratorLegacy.PlayerState var4 = ensurePlayer(var0);
            var4.rewardCredit.merge(var1.rewardItem, var1.rewardCount, Integer::sum);
            var4.recentGiftItem = var1.rewardItem;
            var4.recentGiftAt = now();
            var4.giftCallbackDone = false;
            if (callQuiet(var0, "addItem", var3) instanceof Boolean var7 && var7) {
               var10000 = true;
               break label31;
            }

            var10000 = false;
         }

         boolean var6 = var10000;
         if (!var6) {
            callQuiet(var0, "drop", var3, false);
         }
      } catch (Throwable var8) {
         soft("reward", var8);
      }
   }

   private static void observeRecentGift(Object var0, NarratorLegacy.PlayerState var1) {
      if (var1.recentGiftItem != null) {
         long var2 = now() - var1.recentGiftAt;
         if (var2 > 120000L && !var1.giftCallbackDone) {
            try {
               if (inventoryCounts(var0).getOrDefault(var1.recentGiftItem, 0) > 0) {
                  var1.giftCallbackDone = true;
                  enqueueObservation(var0, "CB-PILOT-01", "VOUS L’AVEZ CONSERVÉE.", "Vous l’avez conservée. Je ne m’attendais pas à cela.");
               }
            } catch (Throwable var5) {
            }
         }

         if (var2 > 600000L) {
            var1.recentGiftItem = null;
            var1.giftCallbackDone = false;
         }
      }
   }

   private static void sendIndividual(Object var0, Object var1, String var2, String var3, String var4, String var5, int var6) {
      try {
         Class var7 = Class.forName("fr.reivaxmc.progress.network.ProgressNetworking");
         Method var8 = null;

         for (Method var12 : var7.getDeclaredMethods()) {
            if (var12.getName().equals("packet") && var12.getParameterCount() == 7) {
               var8 = var12;
               break;
            }
         }

         if (var8 == null) {
            return;
         }

         var8.setAccessible(true);
         Object var18 = var8.invoke(null, var0, var1, var2, var3, var4 == null ? "" : var4, var5 == null ? "" : var5, var6);
         System.out.println("[REIVAX EVENT DEBUG] PACKET construit " + var2 + " kind=" + var3 + " joueur=" + name(var0));
         Class var19 = Class.forName("net.neoforged.neoforge.network.PacketDistributor");
         Class var20 = Class.forName("net.minecraft.network.protocol.common.custom.CustomPacketPayload");
         Object var21 = Array.newInstance(var20, 0);

         for (Method var16 : var19.getMethods()) {
            if (var16.getName().equals("sendToPlayer") && var16.getParameterCount() == 3) {
               var16.invoke(null, var0, var18, var21);
               System.out.println("[REIVAX EVENT DEBUG] PACKET envoyé " + var2 + " -> " + name(var0));
               break;
            }
         }
      } catch (Throwable var17) {
         soft("send", var17);
      }
   }

   private static void registerSimpleCommand(Object var0, String var1, NarratorLegacy.PlayerAction var2) throws Exception {
      Object var3 = call(var0, "getDispatcher");
      Class var4 = Class.forName("net.minecraft.commands.Commands");
      Object var5 = callStatic(var4, "literal", var1);
      Class var6 = Class.forName("com.mojang.brigadier.Command");
      Object var7 = Proxy.newProxyInstance(var6.getClassLoader(), new Class[]{var6}, (var2x, var3x, var4x) -> {
         if (var3x.getDeclaringClass() == Object.class) {
            String var8 = var3x.getName();

            return switch (var8) {
               case "hashCode" -> System.identityHashCode(var2x);
               case "equals" -> var2x == (var4x != null && var4x.length != 0 ? var4x[0] : null);
               case "toString" -> "ReivaxMCNarratorCommand[" + var1 + "]";
               default -> null;
            };
         } else if (var3x.getName().equals("run")) {
            Object var5x = var4x[0];
            Object var6x = call(var5x, "getSource");
            Object var7x = call(var6x, "getPlayerOrException");
            var2.run(var7x);
            return 1;
         } else {
            return null;
         }
      });
      call(var5, "executes", var7);
      call(var3, "register", var5);
   }

   private static void debugInfo(Object var0) {
      try {
         NarratorLegacy.ServerState var1 = state(serverOf(var0));
         Object var2 = campaignData(serverOf(var0));
         message(
            var0,
            "§6Narrateur A17 §7— file: §f"
               + var1.queue.size()
               + " §7| progression: §f"
               + callQuiet(var2, "progress")
               + " §7| civilisation: §f"
               + callQuiet(var2, "score")
         );
      } catch (Throwable var3) {
      }
   }

   private static void resetPilots(Object var0) {
      try {
         Object var1 = campaignData(serverOf(var0));
         Set var2 = doneSet(var1);
         int var3 = 0;
         int var4 = 0;

         for (NarratorLegacy.EventDef var6 : CATALOG.values()) {
            if (var2.remove(var6.id)) {
               var3 += var6.agePoints;
               var4 += var6.civScore;
            }
         }

         setIntField(var1, "progress", Math.max(0, asInt(fieldQuiet(var1, "progress"), 0) - var3));
         setIntField(var1, "score", Math.max(0, asInt(fieldQuiet(var1, "score"), 0) - var4));
         callQuiet(var1, "setDirty");
         PLAYERS.remove(uuid(var0));
         state(serverOf(var0)).queue.clear();
         message(var0, "§aPilotes Alpha 17 réinitialisés.");
      } catch (Throwable var7) {
         message(var0, "§cReset impossible: " + var7.getClass().getSimpleName());
      }
   }

   private static Set<String> doneSet(Object var0) throws Exception {
      Field var1 = findField(var0.getClass(), "done");
      var1.setAccessible(true);
      return (Set<String>)var1.get(var0);
   }

   private static long kvInc(Object var0, String var1, long var2) {
      try {
         long var4 = kvGet(var0, var1) + var2;
         kvSet(var0, var1, var4);
         return var4;
      } catch (Throwable var6) {
         return var2;
      }
   }

   private static long kvGet(Object var0, String var1) throws Exception {
      String var2 = "__N17KV__" + var1 + "=";

      for (String var4 : doneSet(var0)) {
         if (var4.startsWith(var2)) {
            return Long.parseLong(var4.substring(var2.length()));
         }
      }

      return 0L;
   }

   private static void kvSet(Object var0, String var1, long var2) throws Exception {
      Set<String> var4 = doneSet(var0);
      String var5 = "__N17KV__" + var1 + "=";
      var4.removeIf(var1x -> var1x.startsWith(var5));
      var4.add(var5 + var2);
      callQuiet(var0, "setDirty");
   }

   private static void mirrorLegacy(Object var0, String var1, String var2) {
      try {
         if (asBool(callQuiet(var0, "isCompleted", var1)) && !asBool(callQuiet(var0, "isCompleted", var2))) {
            callQuiet(var0, "complete", var2, 0, 0);
         }
      } catch (Throwable var4) {
      }
   }

   private static void silentComplete(Object var0, String var1, int var2, int var3) {
      try {
         if (!asBool(callQuiet(var0, "isCompleted", var1))) {
            callQuiet(var0, "complete", var1, var2, var3);
         }
      } catch (Throwable var5) {
      }
   }

   private static String sourceFor(Object var0, NarratorLegacy.SourceHint var1) {
      if (var1 != null) {
         long var2 = "MINED".equals(var1.source) ? 2000L : 8000L;
         if (now() - var1.at < var2) {
            return var1.source;
         }
      }

      NarratorLegacy.PlayerState var4 = ensurePlayer(var0);
      if (var4.containerHint != null && now() - var4.containerHint.at < 8000L) {
         return var4.containerHint.source;
      } else {
         return isCreative(var0) ? "CREATIVE" : "UNKNOWN";
      }
   }

   private static boolean consumeNarratorCredit(NarratorLegacy.PlayerState var0, String var1, int var2) {
      Integer var3 = var0.rewardCredit.get(var1);
      if (var3 != null && var3 > 0) {
         int var4 = var3 - var2;
         if (var4 <= 0) {
            var0.rewardCredit.remove(var1);
         } else {
            var0.rewardCredit.put(var1, var4);
         }

         return var3 >= var2;
      } else {
         return false;
      }
   }

   private static boolean insideHome(Object var0, Object var1, Object var2) {
      try {
         return asBool(callQuiet(var0, "isInsideMainTerritory", dimensionId(var1), var2));
      } catch (Throwable var4) {
         return false;
      }
   }

   private static boolean sameDimension(Object var0, Object var1) {
      return Objects.equals(dimensionId(var0), dimensionId(var1));
   }

   private static double distance(Object var0, Object var1) {
      try {
         double var2 = (double)(asInt(call(var0, "getX"), 0) - asInt(call(var1, "getX"), 0));
         double var4 = (double)(asInt(call(var0, "getY"), 0) - asInt(call(var1, "getY"), 0));
         double var6 = (double)(asInt(call(var0, "getZ"), 0) - asInt(call(var1, "getZ"), 0));
         return Math.sqrt(var2 * var2 + var4 * var4 + var6 * var6);
      } catch (Throwable var8) {
         return 0.0;
      }
   }

   private static String positionKey(Object var0, Object var1) {
      try {
         return dimensionId(var0) + ":" + call(var1, "getX") + ":" + call(var1, "getY") + ":" + call(var1, "getZ");
      } catch (Throwable var3) {
         return null;
      }
   }

   private static boolean isContainerBlock(String var0) {
      return var0 != null && (var0.endsWith(":chest") || var0.endsWith(":trapped_chest") || var0.endsWith(":barrel") || var0.contains("shulker_box"));
   }

   private static boolean isCropBlock(String var0) {
      return var0 != null
         && (
            var0.endsWith(":wheat")
               || var0.endsWith(":carrots")
               || var0.endsWith(":potatoes")
               || var0.endsWith(":beetroots")
               || var0.endsWith(":melon_stem")
               || var0.endsWith(":pumpkin_stem")
               || var0.endsWith(":nether_wart")
               || var0.endsWith(":cocoa")
         );
   }

   private static boolean isWood(String var0) {
      return var0 != null
         && (var0.endsWith("_log") || var0.endsWith("_wood") || var0.endsWith("_stem") || var0.endsWith("_hyphae") || var0.equals("minecraft:bamboo_block"));
   }

   private static boolean isCreative(Object var0) {
      try {
         Object var1 = callQuiet(var0, "getAbilities");
         Object var2 = fieldQuiet(var1, "instabuild");
         return asBool(var2);
      } catch (Throwable var3) {
         return false;
      }
   }

   private static String dimensionId(Object var0) {
      try {
         Object var1 = call(var0, "serverLevel");
         Object var2 = call(var1, "dimension");
         Object var3 = call(var2, "location");
         return String.valueOf(var3);
      } catch (Throwable var4) {
         return "unknown";
      }
   }

   private static String damageId(Object var0) {
      if (var0 == null) {
         return "unknown";
      } else {
         try {
            Object var1 = callQuiet(var0, "getMsgId");
            if (var1 != null) {
               return String.valueOf(var1);
            }
         } catch (Throwable var2) {
         }

         return String.valueOf(var0).toLowerCase(Locale.ROOT);
      }
   }

   private static Map<String, Integer> inventoryCounts(Object var0) throws Exception {
      HashMap<String, Integer> var1 = new HashMap<>();
      Object var2 = call(var0, "getInventory");
      int var3 = asInt(call(var2, "getContainerSize"), 0);

      for (int var4 = 0; var4 < var3; var4++) {
         Object var5 = call(var2, "getItem", var4);
         if (var5 != null && !asBool(callQuiet(var5, "isEmpty"))) {
            String var6 = stackItemId(var5);
            if (var6 != null) {
               var1.merge(var6, asInt(callQuiet(var5, "getCount"), 1), Integer::sum);
            }
         }
      }

      return var1;
   }

   private static String stackItemId(Object var0) {
      try {
         return var0 == null ? null : itemId(call(var0, "getItem"));
      } catch (Throwable var2) {
         return null;
      }
   }

   private static String itemId(Object var0) {
      return registryId("ITEM", var0);
   }

   private static String blockId(Object var0) {
      try {
         return var0 == null ? "" : registryId("BLOCK", call(var0, "getBlock"));
      } catch (Throwable var2) {
         return "";
      }
   }

   private static String entityId(Object var0) {
      try {
         return var0 == null ? "" : registryId("ENTITY_TYPE", call(var0, "getType"));
      } catch (Throwable var2) {
         return "";
      }
   }

   private static String registryId(String var0, Object var1) {
      try {
         Class var2 = Class.forName("net.minecraft.core.registries.BuiltInRegistries");
         Object var3 = var2.getField(var0).get(null);
         Object var4 = call(var3, "getKey", var1);
         return String.valueOf(var4);
      } catch (Throwable var5) {
         return String.valueOf(var1);
      }
   }

   private static Object itemById(String var0) {
      try {
         String var1 = var0.substring(var0.indexOf(58) + 1).toUpperCase(Locale.ROOT);
         return Class.forName("net.minecraft.world.item.Items").getField(var1).get(null);
      } catch (Throwable var2) {
         return null;
      }
   }

   private static Object newItemStack(Object var0, int var1) {
      try {
         Class var2 = Class.forName("net.minecraft.world.item.ItemStack");

         for (Constructor var6 : var2.getConstructors()) {
            Class[] var7 = var6.getParameterTypes();
            if (var7.length == 2 && var7[1] == int.class && compatible(var7[0], var0)) {
               return var6.newInstance(var0, var1);
            }
         }

         for (Constructor var12 : var2.getConstructors()) {
            if (var12.getParameterCount() == 1 && compatible(var12.getParameterTypes()[0], var0)) {
               Object var13 = var12.newInstance(var0);
               callQuiet(var13, "setCount", var1);
               return var13;
            }
         }
      } catch (Throwable var8) {
      }

      return null;
   }

   private static void setCustomName(Object var0, String var1) {
      try {
         Class var2 = Class.forName("net.minecraft.core.component.DataComponents");
         Object var3 = var2.getField("CUSTOM_NAME").get(null);
         Class var4 = Class.forName("net.minecraft.network.chat.Component");
         Object var5 = callStatic(var4, "literal", var1);
         callQuiet(var0, "set", var3, var5);
      } catch (Throwable var6) {
      }
   }

   private static void addTimeline(Object var0, Object var1, Object var2, String var3, String var4) {
      try {
         int var5 = day(var1);
         callQuiet(var0, "addTimeline", var5, name(var2), var3, var4);
      } catch (Throwable var6) {
      }
   }

   private static int day(Object var0) {
      try {
         Object var1 = call(var0, "overworld");
         long var2 = ((Number)call(var1, "getDayTime")).longValue();
         return (int)(var2 / 24000L) + 1;
      } catch (Throwable var4) {
         return 1;
      }
   }

   private static String name(Object var0) {
      try {
         Object var1 = callQuiet(var0, "getGameProfile");
         Object var2 = var1 == null ? null : callQuiet(var1, "getName");
         if (var2 != null) {
            return String.valueOf(var2);
         }
      } catch (Throwable var3) {
      }

      return "Joueur";
   }

   private static String uuid(Object var0) {
      try {
         return String.valueOf(call(var0, "getUUID"));
      } catch (Throwable var2) {
         return String.valueOf(System.identityHashCode(var0));
      }
   }

   private static Object serverOf(Object var0) {
      return callQuiet(var0, "getServer");
   }

   private static boolean isServerPlayer(Object var0) {
      return var0 != null && var0.getClass().getName().equals("net.minecraft.server.level.ServerPlayer")
         || var0 != null && isAssignableName(var0.getClass(), "net.minecraft.server.level.ServerPlayer");
   }

   private static boolean isAssignableName(Class<?> var0, String var1) {
      while (var0 != null) {
         if (var0.getName().equals(var1)) {
            return true;
         }

         var0 = var0.getSuperclass();
      }

      return false;
   }

   private static NarratorLegacy.PlayerState ensurePlayer(Object var0) {
      return PLAYERS.computeIfAbsent(uuid(var0), var0x -> new NarratorLegacy.PlayerState());
   }

   private static NarratorLegacy.ServerState state(Object var0) {
      return SERVERS.computeIfAbsent(var0, var0x -> new NarratorLegacy.ServerState());
   }

   private static void touch(Object var0) {
      if (var0 != null) {
         state(var0).lastFactAt = now();
      }
   }

   private static List<?> players(Object var0) {
      try {
         Object var1 = call(var0, "getPlayerList");
         return call(var1, "getPlayers") instanceof List var3 ? var3 : List.of();
      } catch (Throwable var4) {
         return List.of();
      }
   }

   private static Object firstPlayer(Object var0) {
      List var1 = players(var0);
      return var1.isEmpty() ? null : var1.get(0);
   }

   private static Object campaignData(Object var0) {
      try {
         return callStatic(Class.forName("fr.reivaxmc.progress.progression.CampaignSavedData"), "get", var0);
      } catch (Throwable var2) {
         return null;
      }
   }

   private static String replaceVars(String var0, String var1, String var2) {
      return var0 == null ? "" : var0.replace("{player}", var1).replace("{other_player}", var2);
   }

   private static long now() {
      return System.currentTimeMillis();
   }

   private static Map<String, NarratorLegacy.EventDef> loadCatalog() {
      LinkedHashMap var0 = new LinkedHashMap();

      try {
         LinkedHashMap var13;
         try (InputStream var1 = NarratorLegacy.class.getResourceAsStream("/data/reivaxmc_progress/narrator/age1_pilots.json")) {
            if (var1 != null) {
               String var2 = new String(var1.readAllBytes(), StandardCharsets.UTF_8);
               if (new NarratorLegacy.Json(var2).parse() instanceof Map var4) {
                  Object var6 = var4.get("events");
                  if (var6 instanceof List) {
                     for (Object var7 : (List)var6) {
                        if (var7 instanceof Map) {
                           Map var8 = (Map)var7;
                           NarratorLegacy.EventDef var9 = NarratorLegacy.EventDef.from(var8);
                           if (var9.id != null && !var0.containsKey(var9.id)) {
                              var0.put(var9.id, var9);
                           }
                        }
                     }
                  }
               }

               return var0;
            }

            var13 = var0;
         }

         return var13;
      } catch (Throwable var12) {
         System.err.println("[ReivaxMC Narrator] catalog load failed: " + var12);
         return var0;
      }
   }

   private static Object call(Object var0, String var1, Object... var2) throws Exception {
      if (var0 == null) {
         throw new NullPointerException(var1);
      } else {
         Method var3 = findMethod(var0.getClass(), var1, var2);
         if (var3 == null) {
            throw new NoSuchMethodException(var0.getClass() + "." + var1);
         } else {
            var3.setAccessible(true);
            return var3.invoke(var0, var2);
         }
      }
   }

   private static boolean storyStarted(Object var0) {
      if (var0 == null) {
         return false;
      }

      try {
         Class<?> var1 = Class.forName("fr.reivaxmc.progress.story.StoryModeGate18F");
         Object var2 = callStatic(var1, "state", var0);
         Object var3 = callQuiet(var2, "snapshot");
         return asBool(callQuiet(var3, "started"));
      } catch (Throwable var4) {
         soft("storyStarted18F", var4);
         return false;
      }
   }

   private static Object callQuiet(Object var0, String var1, Object... var2) {
      try {
         return call(var0, var1, var2);
      } catch (Throwable var4) {
         return null;
      }
   }

   private static Object callStatic(Class<?> var0, String var1, Object... var2) throws Exception {
      Method var3 = findMethod(var0, var1, var2);
      if (var3 == null) {
         throw new NoSuchMethodException(var0 + "." + var1);
      } else {
         var3.setAccessible(true);
         return var3.invoke(null, var2);
      }
   }

   private static Method findMethod(Class<?> var0, String var1, Object[] var2) {
      for (Class var3 = var0; var3 != null; var3 = var3.getSuperclass()) {
         for (Method var7 : var3.getDeclaredMethods()) {
            if (var7.getName().equals(var1) && var7.getParameterCount() == var2.length && paramsOk(var7.getParameterTypes(), var2)) {
               return var7;
            }
         }
      }

      for (Method var11 : var0.getMethods()) {
         if (var11.getName().equals(var1) && var11.getParameterCount() == var2.length && paramsOk(var11.getParameterTypes(), var2)) {
            return var11;
         }
      }

      return null;
   }

   private static boolean paramsOk(Class<?>[] var0, Object[] var1) {
      for (int var2 = 0; var2 < var0.length; var2++) {
         if (!compatible(var0[var2], var1[var2])) {
            return false;
         }
      }

      return true;
   }

   private static boolean compatible(Class<?> var0, Object var1) {
      if (var1 == null) {
         return !var0.isPrimitive();
      } else if (var0.isInstance(var1)) {
         return true;
      } else {
         return !var0.isPrimitive()
            ? var0.isAssignableFrom(var1.getClass())
            : var0 == int.class && var1 instanceof Integer
               || var0 == long.class && var1 instanceof Long
               || var0 == boolean.class && var1 instanceof Boolean
               || var0 == float.class && var1 instanceof Float
               || var0 == double.class && var1 instanceof Double
               || var0 == short.class && var1 instanceof Short
               || var0 == byte.class && var1 instanceof Byte
               || var0 == char.class && var1 instanceof Character;
      }
   }

   private static Field findField(Class<?> var0, String var1) throws NoSuchFieldException {
      for (Class var2 = var0; var2 != null; var2 = var2.getSuperclass()) {
         try {
            return var2.getDeclaredField(var1);
         } catch (NoSuchFieldException var4) {
         }
      }

      throw new NoSuchFieldException(var1);
   }

   private static Object fieldQuiet(Object var0, String var1) {
      try {
         Field var2 = findField(var0.getClass(), var1);
         var2.setAccessible(true);
         return var2.get(var0);
      } catch (Throwable var3) {
         return null;
      }
   }

   private static void setIntField(Object var0, String var1, int var2) throws Exception {
      Field var3 = findField(var0.getClass(), var1);
      var3.setAccessible(true);
      var3.setInt(var0, var2);
   }

   private static boolean asBool(Object var0) {
      if (var0 instanceof Boolean var1 && var1) {
         return true;
      }

      return false;
   }

   private static int asInt(Object var0, int var1) {
      return var0 instanceof Number var2 ? var2.intValue() : var1;
   }

   private static float asFloat(Object var0, float var1) {
      return var0 instanceof Number var2 ? var2.floatValue() : var1;
   }

   private static void message(Object var0, String var1) {
      try {
         Class var2 = Class.forName("net.minecraft.network.chat.Component");
         Object var3 = callStatic(var2, "literal", var1);
         callQuiet(var0, "displayClientMessage", var3, false);
      } catch (Throwable var4) {
      }
   }

   private static void soft(String var0, Throwable var1) {
      System.err.println("[ReivaxMC Narrator A17] " + var0 + ": " + root(var1));
   }

   private static String root(Throwable var0) {
      while (var0 instanceof InvocationTargetException var1 && var1.getCause() != null) {
         var0 = var1.getCause();
      }

      return var0.getClass().getSimpleName() + ": " + var0.getMessage();
   }

   private static final class EventDef {
      String id;
      String title = "";
      String actorText = "";
      String otherText = "";
      String kind = "NARRATOR_ACHIEVEMENT";
      String rewardItem;
      String rewardName;
      String lowHealthText;
      String otherContainerText;
      String longDistanceText;
      int priority = 60;
      int agePoints;
      int civScore;
      int rewardCount = 1;
      final Map<String, String> sourceVariants = new HashMap<>();

      static NarratorLegacy.EventDef from(Map<?, ?> var0) {
         NarratorLegacy.EventDef var1 = new NarratorLegacy.EventDef();
         var1.id = str(var0, "id");
         var1.title = str(var0, "title");
         var1.actorText = str(var0, "actor_text");
         var1.otherText = str(var0, "other_text");
         var1.kind = str(var0, "kind");
         if (var1.kind.isBlank()) {
            var1.kind = "NARRATOR_ACHIEVEMENT";
         }

         var1.priority = num(var0, "priority", 60);
         var1.agePoints = num(var0, "age_points", 0);
         var1.civScore = num(var0, "civ_score", 0);
         var1.rewardItem = str(var0, "reward_item");
         var1.rewardName = str(var0, "reward_name");
         var1.rewardCount = num(var0, "reward_count", 1);
         var1.lowHealthText = str(var0, "low_health_text");
         var1.otherContainerText = str(var0, "other_container_text");
         var1.longDistanceText = str(var0, "long_distance_text");
         if (var0.get("source_variants") instanceof Map<?, ?> var3) {
            for (Entry var5 : var3.entrySet()) {
               var1.sourceVariants.put(String.valueOf(var5.getKey()), String.valueOf(var5.getValue()));
            }
         }

         return var1;
      }

      private static String str(Map<?, ?> var0, String var1) {
         Object var2 = var0.get(var1);
         return var2 == null ? "" : String.valueOf(var2);
      }

      private static int num(Map<?, ?> var0, String var1, int var2) {
         return var0.get(var1) instanceof Number var4 ? var4.intValue() : var2;
      }
   }

   private static final class Json {
      final String s;
      int i;

      Json(String var1) {
         this.s = var1;
      }

      Object parse() {
         this.ws();
         return this.val();
      }

      Object val() {
         this.ws();
         if (this.i >= this.s.length()) {
            return null;
         } else {
            char var1 = this.s.charAt(this.i);
            if (var1 == '{') {
               return this.obj();
            } else if (var1 == '[') {
               return this.arr();
            } else if (var1 == '"') {
               return this.str();
            } else if (var1 == 't' && this.s.startsWith("true", this.i)) {
               this.i += 4;
               return true;
            } else if (var1 == 'f' && this.s.startsWith("false", this.i)) {
               this.i += 5;
               return false;
            } else if (var1 == 'n' && this.s.startsWith("null", this.i)) {
               this.i += 4;
               return null;
            } else {
               return this.num();
            }
         }
      }

      Map<String, Object> obj() {
         LinkedHashMap var1 = new LinkedHashMap();
         this.i++;
         this.ws();
         if (this.peek('}')) {
            this.i++;
            return var1;
         } else {
            while (this.i < this.s.length()) {
               String var2 = this.str();
               this.ws();
               this.i++;
               Object var3 = this.val();
               var1.put(var2, var3);
               this.ws();
               if (this.peek('}')) {
                  this.i++;
                  break;
               }

               this.i++;
               this.ws();
            }

            return var1;
         }
      }

      List<Object> arr() {
         ArrayList var1 = new ArrayList();
         this.i++;
         this.ws();
         if (this.peek(']')) {
            this.i++;
            return var1;
         } else {
            while (this.i < this.s.length()) {
               var1.add(this.val());
               this.ws();
               if (this.peek(']')) {
                  this.i++;
                  break;
               }

               this.i++;
            }

            return var1;
         }
      }

      String str() {
         StringBuilder var1 = new StringBuilder();
         this.i++;

         while (this.i < this.s.length()) {
            char var2 = this.s.charAt(this.i++);
            if (var2 == '"') {
               break;
            }

            if (var2 == '\\' && this.i < this.s.length()) {
               char var3 = this.s.charAt(this.i++);
               switch (var3) {
                  case '"':
                     var1.append('"');
                     break;
                  case '\\':
                     var1.append('\\');
                     break;
                  case 'n':
                     var1.append('\n');
                     break;
                  case 'r':
                     var1.append('\r');
                     break;
                  case 't':
                     var1.append('\t');
                     break;
                  case 'u':
                     int var4 = Integer.parseInt(this.s.substring(this.i, this.i + 4), 16);
                     this.i += 4;
                     var1.append((char)var4);
                     break;
                  default:
                     var1.append(var3);
               }
            } else {
               var1.append(var2);
            }
         }

         return var1.toString();
      }

      Number num() {
         int var1 = this.i;

         while (this.i < this.s.length() && "-+0123456789.eE".indexOf(this.s.charAt(this.i)) >= 0) {
            this.i++;
         }

         String var2 = this.s.substring(var1, this.i);

         try {
            return !var2.contains(".") && !var2.contains("e") && !var2.contains("E") ? (double)Long.parseLong(var2) : Double.parseDouble(var2);
         } catch (Exception var4) {
            return 0;
         }
      }

      void ws() {
         while (this.i < this.s.length() && Character.isWhitespace(this.s.charAt(this.i))) {
            this.i++;
         }
      }

      boolean peek(char var1) {
         return this.i < this.s.length() && this.s.charAt(this.i) == var1;
      }
   }

   private static record Pending(
      String id, Object actor, NarratorLegacy.EventDef def, String actorText, String otherText, long createdAt, String source, Map<String, Object> context
   ) {
   }

   private interface PlayerAction {
      void run(Object var1) throws Exception;
   }

   private static final class PlayerState {
      boolean inventoryInitialized;
      boolean away;
      boolean giftCallbackDone;
      float lastHealth = Float.NaN;
      double maxAway;
      long lastMatrixInteractionAt;
      long recentGiftAt;
      long lastInventoryScanAt;
      String recentGiftItem;
      NarratorLegacy.SourceHint containerHint;
      final Map<String, Integer> itemCounts = new HashMap<>();
      final Map<String, Integer> rewardCredit = new HashMap<>();
      final Map<String, NarratorLegacy.SourceHint> hints = new HashMap<>();
   }

   private static final class ServerState {
      final Deque<NarratorLegacy.Pending> queue = new ArrayDeque<>();
      final Set<String> playerPlacedContainers = ConcurrentHashMap.newKeySet();
      long lastFactAt = NarratorLegacy.now();
      long lastDeliveredAt;
      long nextDeliveryAt;
      long lastQuietAt;
      String lastMatrixActorUuid;
      boolean storyInitialized;
      boolean foundation;
      boolean matrix;
      boolean stelaPlaced;
      boolean stela;
      boolean matrixDisc;
      boolean nightSeen;
   }

   private static record SourceHint(String source, long at, String actor) {
   }
}
