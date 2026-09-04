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
   private static final long NOTIFICATION_GAP_MIN_MS = 4300L;
   private static final long NOTIFICATION_GAP_MAX_MS = 9000L;
   private static final int MAX_QUEUE_SIZE = 24;
   private static final long QUIET_AFTER_MS = 180000L;
   private static final long QUIET_COOLDOWN_MS = 900000L;
   private static final Map<Object, NarratorLegacy.ServerState> SERVERS = Collections.synchronizedMap(new WeakHashMap<>());
   private static final Map<String, NarratorLegacy.PlayerState> PLAYERS = new ConcurrentHashMap<>();
   private static final Map<String, NarratorLegacy.EventDef> CATALOG = loadCatalog();
   private static volatile boolean listenersTried = false;

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

         observeDrowningRecovery(var1, var3);
         observePendingA5Interactions(var1, var3);

         var3.lastHealth = var5;
          // Inventory deltas are the generic sensor used by several robust events
          // (wood, stone, coal, iron, diamond, stick). Scan at 2 Hz: responsive enough
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
                  NarratorA5SignalDetector.Signal a5 = NarratorA5SignalDetector.inventoryIncrease(var6);
                  if (a5 != null) {
                     trigger(var0, a5.eventId(), var11, var10, Map.of("item", a5.targetId(), "delta", var9));
                  }

                   NarratorA2SignalDetector.Signal a2 = NarratorA2SignalDetector.inventoryIncrease(var6);
                   if (a2 != null) {
                      trigger(var0, a2.eventId(), a2.source(), var10, Map.of("item", a2.targetId(), "delta", var9));
                   }

                   NarratorA3SignalDetector.Signal a3 = NarratorA3SignalDetector.inventoryIncrease(var6);
                   if (a3 != null) {
                      trigger(var0, a3.eventId(), a3.source(), var10, Map.of("item", a3.targetId(), "delta", var9));
                   }
                }
            }
         }

         var2.itemCounts.clear();
         var2.itemCounts.putAll(var3);
         observeA3Equipment(var0);
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

          NarratorA2SignalDetector.Signal a2 = NarratorA2SignalDetector.blockPlaced(var4);
          if (a2 != null) {
             trigger(var1, a2.eventId(), a2.source(), null, Map.of("block", a2.targetId()));
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

         if ("minecraft:bell".equals(var5)) {
            var6.pendingBellLevel = var2;
            var6.pendingBellPos = var3;
            var6.pendingBellAt = now();
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
         registerSimpleCommand(var0, "reivax_brain", var0x -> debugBrain(var0x));
         registerSimpleCommand(var0, "reivax_a1", var0x -> debugStorySignalsA1(var0x));
         registerSimpleCommand(var0, "reivax_a2", var0x -> debugSignalsA2(var0x));
         registerSimpleCommand(var0, "reivax_a3", var0x -> debugSignalsA3(var0x));
         registerSimpleCommand(var0, "reivax_a4", var0x -> debugSignalsA4(var0x));
         registerSimpleCommand(var0, "reivax_a5", var0x -> debugSignalsA5(var0x));
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
         NarratorA5SignalDetector.Signal signal = NarratorA5SignalDetector.itemCrafted(var3);
         if (signal != null) {
            trigger(var1, signal.eventId(), signal.source(), null, Map.of("item", signal.targetId()));
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
         NarratorA5SignalDetector.Signal signal = NarratorA5SignalDetector.itemSmelted(var3);
         if (signal != null) {
            trigger(var1, signal.eventId(), signal.source(), null, Map.of("item", signal.targetId()));
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
            ensurePlayer(var1).drowningDanger = false;
            NarratorA4SignalDetector.Signal death = NarratorA4SignalDetector.playerDeath();
            trigger(var1, death.eventId(), death.source(), null, Map.of("cause", damageId(var3)));
            return;
         }

         if (isServerPlayer(var4)) {
            state(serverOf(var4)).lastFactAt = now();
            NarratorA4SignalDetector.Signal kill = NarratorA4SignalDetector.killedByPlayer(var2);
            if (kill != null) {
               trigger(var4, kill.eventId(), kill.source(), null, Map.of("entity", kill.targetId()));
            }
         }
      } catch (Throwable var6) {
         soft("livingDeath", var6);
      }
   }

   public static void onLivingDamage(Object event) {
      try {
         Object player = callQuiet(event, "getEntity");
         if (!isServerPlayer(player)) {
            return;
         }

         Object source = callQuiet(event, "getSource");
         Object sourceEntity = source == null ? null : callQuiet(source, "getEntity");
         if (sourceEntity == null && source != null) {
            sourceEntity = callQuiet(source, "getDirectEntity");
         }

         String causeId = damageId(source);
         String sourceEntityId = entityId(sourceEntity);
         float healthAfter = asFloat(callQuiet(player, "getHealth"), 20.0F);
         float healthDamage = a4DamageAmount(event);
         NarratorLegacy.PlayerState playerState = ensurePlayer(player);
         state(serverOf(player)).lastFactAt = now();

         if (NarratorA4SignalDetector.isDrowning(causeId) && healthDamage > 0.0F) {
            playerState.drowningDanger = true;
         }

         NarratorA4SignalDetector.DamageObservation observation = new NarratorA4SignalDetector.DamageObservation(
            causeId,
            sourceEntityId,
            healthAfter,
            healthDamage
         );
         for (NarratorA4SignalDetector.Signal signal : NarratorA4SignalDetector.damagedPlayer(observation)) {
            trigger(
               player,
               signal.eventId(),
               signal.source(),
               null,
               Map.of("cause", causeId, "entity", sourceEntityId, "health", healthAfter, "damage", healthDamage)
            );
         }
      } catch (Throwable error) {
         soft("livingDamage", error);
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
         NarratorA5SignalDetector.Signal signal = NarratorA5SignalDetector.animalTamed(true);
         if (signal != null) {
            trigger(var1, signal.eventId(), signal.source(), null, Map.of());
         }
      } catch (Throwable var2) {
         soft("animalTame", var2);
      }
   }

   /**
    * L'interaction est un pré-événement : elle arme seulement une vérification.
    * Le tick suivant confirme que l'animal a réellement accepté la nourriture.
    */
   public static void onEntityInteract(Object event) {
      try {
         Object player = callQuiet(event, "getEntity");
         Object target = callQuiet(event, "getTarget");
         Object stack = callQuiet(event, "getItemStack");
         if (!isServerPlayer(player) || target == null || stack == null) {
            return;
         }

         boolean food = asBool(callQuiet(target, "isFood", stack));
         boolean adult = asInt(callQuiet(target, "getAge"), -1) == 0;
         boolean canLove = asBool(callQuiet(target, "canFallInLove"));
         if (food && adult && canLove) {
            PlayerState state = ensurePlayer(player);
            state.pendingFedAnimal = target;
            state.pendingFedAnimalAt = now();
         }
      } catch (Throwable error) {
         soft("entityInteractA5", error);
      }
   }

   public static void onAnimalBred(Object event) {
      try {
         if (asBool(callQuiet(event, "isCanceled"))) {
            return;
         }

         Object player = callQuiet(event, "getCausedByPlayer");
         Object child = callQuiet(event, "getChild");
         if (!isServerPlayer(player)) {
            return;
         }

         NarratorA5SignalDetector.Signal signal = NarratorA5SignalDetector.animalBred(true, child != null);
         if (signal != null) {
            state(serverOf(player)).lastFactAt = now();
            trigger(player, signal.eventId(), signal.source(), null, Map.of("entity", entityId(child)));
         }
      } catch (Throwable error) {
         soft("animalBredA5", error);
      }
   }

   public static void onEntityMounted(Object event) {
      try {
         if (asBool(callQuiet(event, "isCanceled"))) {
            return;
         }

         Object player = callQuiet(event, "getEntityMounting");
         if (!isServerPlayer(player)) {
            return;
         }

         Object mounted = callQuiet(event, "getEntityBeingMounted");
         String mountedId = entityId(mounted);
         NarratorA5SignalDetector.Signal signal = NarratorA5SignalDetector.entityMounted(
            mountedId,
            asBool(callQuiet(event, "isMounting"))
         );
         if (signal != null) {
            state(serverOf(player)).lastFactAt = now();
            trigger(player, signal.eventId(), signal.source(), null, Map.of("entity", signal.targetId()));
         }
      } catch (Throwable error) {
         soft("entityMountedA5", error);
      }
   }

   public static void onVillagerTrade(Object event) {
      try {
         Object player = callQuiet(event, "getEntity");
         if (!isServerPlayer(player)) {
            return;
         }

         NarratorA5SignalDetector.Signal signal = NarratorA5SignalDetector.villagerTrade(true);
         if (signal != null) {
            state(serverOf(player)).lastFactAt = now();
            trigger(player, signal.eventId(), signal.source(), null, Map.of());
         }
      } catch (Throwable error) {
         soft("villagerTradeA5", error);
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
         boolean var4 = currentAge1Resonance(var0);
         boolean var5 = asBool(callQuiet(var1, "isCompleted", "F84_SEAL_INSERTED"));
         boolean var6 = asBool(callQuiet(var1, "nightSeen"));
         NarratorStorySignalDetector.Snapshot currentStory = new NarratorStorySignalDetector.Snapshot(var3, var4, var5);
         if (!var2.storyInitialized) {
            var2.storyInitialized = true;
            rememberStorySnapshot(var2, currentStory);
            var2.nightSeen = var6;
            return;
         }

         NarratorStorySignalDetector.Snapshot previousStory = new NarratorStorySignalDetector.Snapshot(
            var2.foundation,
            var2.stelaPlaced,
            var2.stela
         );
         Object var7 = firstPlayer(var0);
         for (NarratorStorySignalDetector.Signal signal : NarratorStorySignalDetector.detect(previousStory, currentStory)) {
            Object actor = storySignalActor(signal.eventId(), var0, var1, var7);
            if (actor != null) {
               trigger(actor, signal.eventId(), "story_bus", null, Map.of("story_state", signal.storyState()));
               if (NarratorStorySignalDetector.FIRST_RESONANCE.equals(signal.eventId())) {
                  var2.nextDeliveryAt = Math.max(var2.nextDeliveryAt, now() + 6500L);
               }
            }
         }

         if (var6 && !asBool(callQuiet(var1, "isCompleted", "A1-031")) && isDawn(var0) && var7 != null) {
            trigger(var7, "A1-031", "STORY", null, Map.of());
            var2.nextDeliveryAt = Math.max(var2.nextDeliveryAt, now() + 6500L);
         }

         rememberStorySnapshot(var2, currentStory);
         var2.nightSeen = var6;
      } catch (Throwable var8) {
         soft("storySignalsA1", var8);
      }
   }

   private static Object storySignalActor(
      String eventId,
      Object server,
      Object campaign,
      Object fallbackActor
   ) {
      if (NarratorStorySignalDetector.FIRST_HOME.equals(eventId)) {
         Object founder = foundationActor(campaign, server);
         return founder == null ? fallbackActor : founder;
      }
      return fallbackActor;
   }

   private static boolean currentAge1Resonance(Object server) {
      try {
         Object data = callStatic(Class.forName("fr.reivaxmc.progress.story.CampaignStateData18"), "getForServer", server);
         return asBool(fieldQuiet(data, "age1Resonance"));
      } catch (Throwable error) {
         soft("storyResonanceA1", error);
         return false;
      }
   }

   private static void rememberStorySnapshot(
      NarratorLegacy.ServerState serverState,
      NarratorStorySignalDetector.Snapshot snapshot
   ) {
      serverState.foundation = snapshot.firstHome();
      serverState.stelaPlaced = snapshot.firstResonance();
      serverState.stela = snapshot.stelaActivated();
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
      // Même gate canonique que les événements A1 : l'ancien introCompleted()
      // n'est plus le démarrage réel de l'histoire depuis Alpha 18F.
      if (storyStarted(var0)) {
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
         NarratorLegacy.EventDef var5 = CATALOG.get(var1);
         if (var5 == null) {
            return;
         }

         Object var6 = serverOf(var0);
         Object var7 = campaignData(var6);
         if (var6 == null || var7 == null) {
            return;
         }

         // Alpha 18F has its own canonical story-start state. CampaignSavedData.introCompleted()
         // belongs to the older awakening intro and is NOT toggled by the current
         // "Commencer l'histoire" button. Using it here made every A1 event look as if
         // the story had never started, even though StoryOpening18F had started it.
         boolean varStoryStarted = storyStarted(var6);
         boolean varStorySource = "STORY".equals(var2) || "story_bus".equalsIgnoreCase(var2);
         if (var1.startsWith("A1-") && !varStoryStarted && !varStorySource) {
            return;
         }

         if (asBool(callQuiet(var7, "isCompleted", var1))) {
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
         if (var20 == null) {
            var20 = firstOtherPlayer(var6, var0);
         }

         // 0.8.0 — le texte n'est plus seulement lié au trigger. Le cerveau reçoit
         // un instantané du monde, de la relation et de la mémoire persistante puis
         // choisit la meilleure variante data-driven. Sans variante compatible, le
         // texte 0.7.x reste strictement inchangé : le nouveau cerveau est additif.
         NarratorContextBrain.Snapshot brain = buildBrainContext(var0, var6, var7, var8, var5, var2, var4, var20);
         NarratorContextBrain.Choice choice = NarratorContextBrain.choose(
            var5.contextVariants,
            brain,
            var16,
            var18,
            var1 + "|" + uuid(var0) + "|" + brain.intValue("total_narrations")
         );
         var16 = choice.actorText();
         var18 = choice.otherText();

         String var12 = name(var0);
         var16 = replaceVars(var16, var12, var20 == null ? "votre compagnon" : name(var20));
         var18 = replaceVars(var18, var12, "{recipient}");
         NarratorLegacy.Pending var13 = new NarratorLegacy.Pending(var1, var0, var5, var16, var18, now(), var2, var4);
         NarratorLegacy.ServerState var14 = state(var6);
         var14.lastFactAt = now();
         if (var5.priority >= 90) {
            var14.queue.removeIf(pending -> pending.def.priority <= 55);
         }
         var14.queue.add(var13);
         trimQueue(var14);
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
            if (NarratorBurstPolicy.shouldWait(var5.def.condenseGroup, var5.createdAt, var3)) {
               return;
            }

            ArrayList<NarratorLegacy.Pending> batch = new ArrayList<>();
            batch.add(var5);
            if (!var5.def.condenseGroup.isBlank() && !hasReward(var5.def)) {
               String anchorActor = uuid(var5.actor);
               for (NarratorLegacy.Pending candidate : var2.queue) {
                  if (candidate == var5 || batch.size() >= NarratorBurstPolicy.MAX_EVENTS_PER_PANEL) {
                     continue;
                  }
                  if (NarratorBurstPolicy.canJoin(
                     var5.def.condenseGroup,
                     candidate.def.condenseGroup,
                     anchorActor,
                     uuid(candidate.actor),
                     var5.createdAt,
                     candidate.createdAt,
                     hasReward(candidate.def)
                  )) {
                     batch.add(candidate);
                  }
               }
            }

            batch.sort((left, right) -> {
               int byTime = Long.compare(left.createdAt, right.createdAt);
               return byTime != 0 ? byTime : left.id.compareTo(right.id);
            });
            var2.queue.removeAll(batch);
            if (var5.def.priority >= 30 || var3 - var5.createdAt <= 120000L) {
               if (batch.size() > 1) {
                  deliverBatch(var0, var1, batch);
               } else {
                  deliver(var0, var1, var5);
               }
               var2.lastDeliveredAt = var3;
               var2.nextDeliveryAt = var3 + deliveryGapMs(var5);
            } else {
            }
         }
      }
   }

   private static boolean hasReward(NarratorLegacy.EventDef def) {
      return def.rewardItem != null && !def.rewardItem.isBlank() && def.rewardCount > 0;
   }

   private static long deliveryGapMs(NarratorLegacy.Pending pending) {
      int chars = Math.max(pending.actorText == null ? 0 : pending.actorText.length(), pending.otherText == null ? 0 : pending.otherText.length());
      long gap = 2500L + chars * 42L;
      if (pending.def.priority >= 90) {
         gap += 900L;
      } else if (pending.def.priority <= 55) {
         gap -= 400L;
      }

      return Math.max(NOTIFICATION_GAP_MIN_MS, Math.min(NOTIFICATION_GAP_MAX_MS, gap));
   }

   private static void trimQueue(NarratorLegacy.ServerState state) {
      while (state.queue.size() > MAX_QUEUE_SIZE) {
         NarratorLegacy.Pending weakest = null;
         for (NarratorLegacy.Pending candidate : state.queue) {
            if (weakest == null
               || candidate.def.priority < weakest.def.priority
               || candidate.def.priority == weakest.def.priority && candidate.createdAt < weakest.createdAt) {
               weakest = candidate;
            }
         }

         if (weakest == null) {
            break;
         }
         state.queue.remove(weakest);
      }
   }

   private static void deliver(Object var0, Object var1, NarratorLegacy.Pending var2) {
      if (!var2.id.startsWith("A1-") || asBool(callQuiet(var1, "complete", var2.id, var2.def.agePoints, var2.def.civScore))) {
         if (var2.id.startsWith("A1-")) {
            addTimeline(var1, var0, var2.actor, var2.def.title, var2.actorText);
         }

         // La mémoire n'est enregistrée qu'une fois l'événement réellement validé.
         // Elle survit au redémarrage via CampaignSavedData et nourrira les variantes
         // des événements suivants.
         recordNarrationMemory(var1, var2);

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

            // Le client reçoit aussi le rôle du destinataire. Le vrai ID serveur reste intact :
            // cela permet au HUD d'indiquer clairement qui a reçu la récompense en DUO.
            String clientEvent = var2.id + (var8 ? "|SELF|" : "|OTHER|") + var5 + "|CIV=" + var2.def.civScore;
            sendIndividual(var7, var1, clientEvent, var2.def.kind, var2.def.title, var9, var2.def.agePoints);
         }

         if (var2.def.rewardItem != null && !var2.def.rewardItem.isBlank()) {
            reward(var2.actor, var2.def);
         }
      }
   }

   private static void deliverBatch(Object server, Object campaign, List<NarratorLegacy.Pending> requested) {
      ArrayList<NarratorLegacy.Pending> accepted = new ArrayList<>();
      int agePoints = 0;
      int civPoints = 0;
      for (NarratorLegacy.Pending pending : requested) {
         if (!pending.id.startsWith("A1-") || asBool(callQuiet(campaign, "complete", pending.id, pending.def.agePoints, pending.def.civScore))) {
            accepted.add(pending);
            agePoints += pending.def.agePoints;
            civPoints += pending.def.civScore;
            if (pending.id.startsWith("A1-")) {
               addTimeline(campaign, server, pending.actor, pending.def.title, pending.actorText);
            }
            recordNarrationMemory(campaign, pending);
         }
      }

      if (accepted.isEmpty()) {
         return;
      }

      NarratorLegacy.Pending anchor = accepted.getFirst();
      String actorUuid = uuid(anchor.actor);
      String actorName = name(anchor.actor);
      String kind = accepted.stream().map(pending -> pending.def.kind).anyMatch("NARRATOR_ANNOUNCEMENT"::equals)
         ? "NARRATOR_ANNOUNCEMENT"
         : "NARRATOR_ACHIEVEMENT";

      for (Object recipient : players(server)) {
         boolean actor = Objects.equals(uuid(recipient), actorUuid);
         ArrayList<String> texts = new ArrayList<>();
         for (NarratorLegacy.Pending pending : accepted) {
            String text = actor ? pending.actorText : pending.otherText;
            texts.add(text.replace("{recipient}", name(recipient)));
         }
         String combined = NarratorBurstPolicy.combine(texts);
         String clientEvent = "BATCH:" + anchor.id + (actor ? "|SELF|" : "|OTHER|") + actorName + "|CIV=" + civPoints;
         sendIndividual(recipient, campaign, clientEvent, kind, "PROGRESSION RAPIDE", combined, agePoints);
      }
   }

   public static String rewardSummary(String eventId) {
      if (eventId == null || eventId.isBlank()) {
         return "";
      }

      String baseId = eventId;
      int sep = baseId.indexOf('|');
      if (sep >= 0) {
         baseId = baseId.substring(0, sep);
      }

      NarratorLegacy.EventDef def = CATALOG.get(baseId);
      if (def == null || def.rewardItem == null || def.rewardItem.isBlank() || def.rewardCount <= 0) {
         return "";
      }

      String label = def.rewardLabel;
      if (label == null || label.isBlank()) {
         label = def.rewardName;
      }
      if (label == null || label.isBlank()) {
         int colon = def.rewardItem.indexOf(':');
         label = (colon >= 0 ? def.rewardItem.substring(colon + 1) : def.rewardItem).replace('_', ' ').toUpperCase(Locale.ROOT);
      }

      return def.rewardCount + " " + label;
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
         Class var19 = Class.forName("net.neoforged.neoforge.network.PacketDistributor");
         Class var20 = Class.forName("net.minecraft.network.protocol.common.custom.CustomPacketPayload");
         Object var21 = Array.newInstance(var20, 0);

         for (Method var16 : var19.getMethods()) {
            if (var16.getName().equals("sendToPlayer") && var16.getParameterCount() == 3) {
               var16.invoke(null, var0, var18, var21);
               break;
            }
         }
      } catch (Throwable var17) {
         soft("send", var17);
      }
   }

   /**
    * API stable pour les futurs choix narratifs : les autres systèmes du mod peuvent
    * faire évoluer la relation avec La Voix sans connaître le stockage interne.
    */
   public static void rememberDecision(Object player, String decisionTag, int trustDelta, int defianceDelta) {
      try {
         if (!isServerPlayer(player)) {
            return;
         }

         Object campaign = campaignData(serverOf(player));
         if (campaign == null) {
            return;
         }

         adjustBrainAxis(campaign, actorBrainKey(player, "trust"), trustDelta);
         adjustBrainAxis(campaign, actorBrainKey(player, "defiance"), defianceDelta);
         if (decisionTag != null && !decisionTag.isBlank()) {
            brainInc(campaign, "brain.decision." + normalizeBrainTag(decisionTag), 1L);
         }
      } catch (Throwable error) {
         soft("brainDecision", error);
      }
   }

   /** Permet aux futurs âges d'ajouter une mémoire contextuelle sans nouveau schéma Java. */
   public static void rememberFact(Object player, String tag, int amount) {
      try {
         if (!isServerPlayer(player) || tag == null || tag.isBlank() || amount == 0) {
            return;
         }
         Object campaign = campaignData(serverOf(player));
         if (campaign == null) {
            return;
         }
         String normalized = normalizeBrainTag(tag);
         brainInc(campaign, "brain.tag." + normalized, amount);
         brainInc(campaign, actorBrainKey(player, "tag." + normalized), amount);
      } catch (Throwable error) {
         soft("brainFact", error);
      }
   }

   private static NarratorContextBrain.Snapshot buildBrainContext(
      Object actor,
      Object server,
      Object campaign,
      NarratorLegacy.ServerState serverState,
      NarratorLegacy.EventDef def,
      String source,
      Map<String, Object> eventContext,
      Object other
   ) {
      HashMap<String, Object> values = new HashMap<>();
      float health = asFloat(callQuiet(actor, "getHealth"), 20.0F);
      float maxHealth = asFloat(callQuiet(actor, "getMaxHealth"), 20.0F);
      if (maxHealth <= 0.0F) {
         maxHealth = 20.0F;
      }

      values.put("source", source == null ? "UNKNOWN" : source);
      values.put("health", health);
      values.put("max_health", maxHealth);
      values.put("health_ratio", health / maxHealth);
      values.put("actor_name", name(actor));
      values.put("dimension", dimensionId(actor));
      values.put("day_phase", dayPhase(actor));
      values.put("queue_pressure", serverState == null ? 0 : serverState.queue.size());
      values.put("progress", asInt(fieldQuiet(campaign, "progress"), 0));
      values.put("civ_score", asInt(fieldQuiet(campaign, "score"), 0));
      values.put("total_narrations", (int)Math.min(Integer.MAX_VALUE, brainGet(campaign, "brain.total")));
      values.put("familiarity", (int)Math.min(Integer.MAX_VALUE, brainGet(campaign, actorBrainKey(actor, "heard"))));
      values.put("trust", (int)Math.min(Integer.MAX_VALUE, brainGet(campaign, actorBrainKey(actor, "trust"))));
      values.put("defiance", (int)Math.min(Integer.MAX_VALUE, brainGet(campaign, actorBrainKey(actor, "defiance"))));

      boolean otherOnline = other != null && isServerPlayer(other);
      boolean sameDim = otherOnline && sameDimension(actor, other);
      double otherDistance = sameDim ? distance(callQuiet(actor, "blockPosition"), callQuiet(other, "blockPosition")) : 999999.0;
      values.put("other_online", otherOnline);
      values.put("other_same_dimension", sameDim);
      values.put("other_nearby", sameDim && otherDistance <= 48.0);
      values.put("distance_to_other", (int)Math.min(Integer.MAX_VALUE, Math.round(otherDistance)));
      values.put("other_name", otherOnline ? name(other) : "");

      if (eventContext != null) {
         for (Entry<String, Object> entry : eventContext.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof Number || value instanceof Boolean || value instanceof String) {
               values.putIfAbsent(entry.getKey(), value);
            }
         }
      }

      // Les compteurs de tags sont chargés uniquement si une variante de cet événement
      // les demande. Même avec des milliers d'événements, on ne lit donc pas toute la mémoire.
      for (String required : NarratorContextBrain.requiredContextKeys(def.contextVariants)) {
         if (required.startsWith("actor_tag_")) {
            String tag = normalizeBrainTag(required.substring("actor_tag_".length()));
            values.put(required, (int)Math.min(Integer.MAX_VALUE, brainGet(campaign, actorBrainKey(actor, "tag." + tag))));
         } else if (required.startsWith("global_tag_")) {
            String tag = normalizeBrainTag(required.substring("global_tag_".length()));
            values.put(required, (int)Math.min(Integer.MAX_VALUE, brainGet(campaign, "brain.tag." + tag)));
         }
      }

      return new NarratorContextBrain.Snapshot(values);
   }

   private static void recordNarrationMemory(Object campaign, NarratorLegacy.Pending pending) {
      try {
         if (campaign == null || pending == null || pending.actor == null) {
            return;
         }

         brainInc(campaign, "brain.total", 1L);
         brainInc(campaign, actorBrainKey(pending.actor, "heard"), 1L);
         for (String rawTag : pending.def.memoryTags) {
            String tag = normalizeBrainTag(rawTag);
            if (tag.isBlank()) {
               continue;
            }
            brainInc(campaign, "brain.tag." + tag, 1L);
            brainInc(campaign, actorBrainKey(pending.actor, "tag." + tag), 1L);
         }
      } catch (Throwable error) {
         soft("brainMemory", error);
      }
   }

   private static Object firstOtherPlayer(Object server, Object actor) {
      String actorUuid = uuid(actor);
      for (Object candidate : players(server)) {
         if (candidate != actor && !Objects.equals(actorUuid, uuid(candidate))) {
            return candidate;
         }
      }
      return null;
   }

   private static String dayPhase(Object player) {
      try {
         Object level = callQuiet(player, "serverLevel");
         Object raw = callQuiet(level, "getDayTime");
         long time = raw instanceof Number number ? Math.floorMod(number.longValue(), 24000L) : 0L;
         if (time < 1000L || time >= 23000L) {
            return "DAWN";
         }
         if (time < 12000L) {
            return "DAY";
         }
         if (time < 13500L) {
            return "DUSK";
         }
         return "NIGHT";
      } catch (Throwable ignored) {
         return "UNKNOWN";
      }
   }

   private static long brainGet(Object campaign, String key) {
      try {
         return kvGet(campaign, key);
      } catch (Throwable ignored) {
         return 0L;
      }
   }

   private static long brainInc(Object campaign, String key, long delta) {
      try {
         return kvInc(campaign, key, delta);
      } catch (Throwable ignored) {
         return 0L;
      }
   }

   private static void adjustBrainAxis(Object campaign, String key, int delta) throws Exception {
      long current = kvGet(campaign, key);
      long next = Math.max(0L, Math.min(100L, current + delta));
      kvSet(campaign, key, next);
   }

   private static String actorBrainKey(Object player, String metric) {
      return "brain.actor." + uuid(player) + "." + metric;
   }

   private static String normalizeBrainTag(String tag) {
      if (tag == null) {
         return "";
      }
      String normalized = tag.trim().toLowerCase(Locale.ROOT).replace(' ', '_');
      StringBuilder out = new StringBuilder(normalized.length());
      for (int i = 0; i < normalized.length(); i++) {
         char c = normalized.charAt(i);
         if (Character.isLetterOrDigit(c) || c == '_' || c == '-' || c == '.') {
            out.append(c);
         }
      }
      return out.toString();
   }

   private static void debugBrain(Object player) {
      try {
         Object campaign = campaignData(serverOf(player));
         if (campaign == null) {
            message(player, "§cCerveau Narrateur indisponible.");
            return;
         }
         int heard = (int)Math.min(Integer.MAX_VALUE, brainGet(campaign, actorBrainKey(player, "heard")));
         long trust = brainGet(campaign, actorBrainKey(player, "trust"));
         long defiance = brainGet(campaign, actorBrainKey(player, "defiance"));
         long danger = brainGet(campaign, actorBrainKey(player, "tag.danger"));
         long origins = brainGet(campaign, actorBrainKey(player, "tag.origins"));
         long total = brainGet(campaign, "brain.total");
         message(
            player,
            "§6La Voix 0.8 §7— état: §f" + NarratorContextBrain.familiarityStage(heard)
               + " §7| familiarité: §f" + heard
               + " §7| confiance: §f" + trust
               + " §7| défiance: §f" + defiance
               + " §7| danger: §f" + danger
               + " §7| Origines: §f" + origins
               + " §7| mémoires monde: §f" + total
         );
      } catch (Throwable error) {
         message(player, "§cLecture du cerveau impossible: " + error.getClass().getSimpleName());
      }
   }

   private static void debugStorySignalsA1(Object player) {
      try {
         Object campaign = campaignData(serverOf(player));
         if (campaign == null) {
            message(player, "§cSignaux A1 indisponibles.");
            return;
         }

         String[] ids = {
            NarratorStorySignalDetector.FIRST_RESONANCE,
            NarratorStorySignalDetector.STELA_DISCOVERED,
            NarratorStorySignalDetector.FIRST_HOME
         };
         String[] labels = {"Résonance", "Stèles du seuil", "Foyer/Borne"};
         int completed = 0;
         StringBuilder details = new StringBuilder();
         for (int index = 0; index < ids.length; index++) {
            boolean done = asBool(callQuiet(campaign, "isCompleted", ids[index]));
            if (done) {
               completed++;
            }
            if (index > 0) {
               details.append(" §8| ");
            }
            details.append(done ? "§a" : "§7").append(labels[index]).append(done ? " ✓" : " —");
         }
         message(player, "§6A1 actuel §7— §f" + completed + "/3 §8| " + details + " §8| §7Matrice reportée");
      } catch (Throwable error) {
         message(player, "§cLecture A1 impossible: " + error.getClass().getSimpleName());
      }
   }

   private static void debugSignalsA2(Object player) {
      try {
         Object campaign = campaignData(serverOf(player));
         if (campaign == null) {
            message(player, "§cSignaux A2 indisponibles.");
            return;
         }

         String[] ids = {
            NarratorA2SignalDetector.FIRST_STICK,
            NarratorA2SignalDetector.FIRST_CHEST,
            NarratorA2SignalDetector.FIRST_BED
         };
         String[] labels = {"Bâton", "Coffre posé", "Lit posé"};
         int completed = 0;
         StringBuilder details = new StringBuilder();
         for (int index = 0; index < ids.length; index++) {
            boolean done = asBool(callQuiet(campaign, "isCompleted", ids[index]));
            if (done) {
               completed++;
            }
            if (index > 0) {
               details.append(" §8| ");
            }
            details.append(done ? "§a" : "§7").append(labels[index]).append(done ? " ✓" : " —");
         }
         message(player, "§6A2 outils et foyer §7— §f" + completed + "/3 §8| " + details + " §8| §bDUO prêt, test reporté");
      } catch (Throwable error) {
         message(player, "§cLecture A2 impossible: " + error.getClass().getSimpleName());
      }
   }

   private static void debugSignalsA3(Object player) {
      try {
         Object campaign = campaignData(serverOf(player));
         if (campaign == null) {
            message(player, "§cSignaux A3 indisponibles.");
            return;
         }

         String[] labels = {
            "Seau", "Eau", "Lave", "Armure", "Armure complète", "Bouclier", "Arc", "Émeraude",
            "Cuivre", "Lapis", "Or", "Redstone", "Améthyste", "Obsidienne", "Outil fer", "Pioche diamant"
         };
         int completed = 0;
         StringBuilder resources = new StringBuilder();
         StringBuilder equipment = new StringBuilder();
         for (int index = 0; index < NarratorA3SignalDetector.ALL_IDS.size(); index++) {
            boolean done = asBool(callQuiet(campaign, "isCompleted", NarratorA3SignalDetector.ALL_IDS.get(index)));
            if (done) {
               completed++;
            }
            StringBuilder target = index < 3 || index >= 7 && index <= 13 ? resources : equipment;
            if (!target.isEmpty()) {
               target.append(" §8| ");
            }
            target.append(done ? "§a" : "§7").append(labels[index]).append(done ? " ✓" : " —");
         }

         message(player, "§6A3 progression matérielle §7— §f" + completed + "/16 §8| §bSOLO + DUO prêts");
         message(player, "§6Ressources §8| " + resources);
         message(player, "§6Équipement §8| " + equipment);
      } catch (Throwable error) {
         message(player, "§cLecture A3 impossible: " + error.getClass().getSimpleName());
      }
   }

   private static void debugSignalsA4(Object player) {
      try {
         Object campaign = campaignData(serverOf(player));
         if (campaign == null) {
            message(player, "§cSignaux A4 indisponibles.");
            return;
         }

         String[] labels = {
            "Creeper tué", "Dégâts Creeper", "Première mort", "Chute", "Chute critique", "Noyade évitée",
            "Feu", "Lave", "Zombie", "Squelette", "Araignée", "Enderman"
         };
         int completed = 0;
         StringBuilder dangers = new StringBuilder();
         StringBuilder combats = new StringBuilder();
         for (int index = 0; index < NarratorA4SignalDetector.ALL_IDS.size(); index++) {
            boolean done = asBool(callQuiet(campaign, "isCompleted", NarratorA4SignalDetector.ALL_IDS.get(index)));
            if (done) {
               completed++;
            }
            StringBuilder target = index <= 7 ? dangers : combats;
            if (!target.isEmpty()) {
               target.append(" §8| ");
            }
            target.append(done ? "§a" : "§7").append(labels[index]).append(done ? " ✓" : " —");
         }

         message(player, "§6A4 dangers et combats §7— §f" + completed + "/12 §8| §bSOLO + DUO prêts");
         message(player, "§6Dangers §8| " + dangers);
         message(player, "§6Combats §8| " + combats);
      } catch (Throwable error) {
         message(player, "§cLecture A4 impossible: " + error.getClass().getSimpleName());
      }
   }

   private static void debugSignalsA5(Object player) {
      try {
         Object campaign = campaignData(serverOf(player));
         if (campaign == null) {
            message(player, "§cSignaux A5 indisponibles.");
            return;
         }

         String[] labels = {
            "Bois", "Établi", "Pierre", "Charbon", "Fer brut", "Cuisson", "Diamant", "Apprivoisement",
            "Animal nourri", "Reproduction", "Cheval", "Bateau", "Échange", "Cloche", "Carte", "Boussole"
         };
         int completed = 0;
         StringBuilder foundations = new StringBuilder();
         StringBuilder lifeAndTravel = new StringBuilder();
         StringBuilder villageAndOrientation = new StringBuilder();
         for (int index = 0; index < NarratorA5SignalDetector.ALL_IDS.size(); index++) {
            boolean done = asBool(callQuiet(campaign, "isCompleted", NarratorA5SignalDetector.ALL_IDS.get(index)));
            if (done) {
               completed++;
            }
            StringBuilder target = index < 8 ? foundations : index < 12 ? lifeAndTravel : villageAndOrientation;
            if (!target.isEmpty()) {
               target.append(" §8| ");
            }
            target.append(done ? "§a" : "§7").append(labels[index]).append(done ? " ✓" : " —");
         }

         message(player, "§6A5 monde vivant §7— §f" + completed + "/16 §8| §bSOLO + DUO prêts");
         message(player, "§6Fondations §8| " + foundations);
         message(player, "§6Vie et mobilité §8| " + lifeAndTravel);
         message(player, "§6Village et orientation §8| " + villageAndOrientation);
         message(player, "§650/52 événements robustes actifs §8| §7les 2 événements Matrice restent reportés au chapitre prévu");
      } catch (Throwable error) {
         message(player, "§cLecture A5 impossible: " + error.getClass().getSimpleName());
      }
   }

   static boolean catalogHasEventForTest(String eventId) {
      return CATALOG.containsKey(eventId);
   }

   /**
    * NeoForge 21.1.248 (Minecraft 1.21.1) exposes the final health loss as
    * getNewDamage(). Later NeoForge releases renamed it getHealthDamage().
    * Keep both names here so the detector never silently receives zero again.
    */
   private static float a4DamageAmount(Object event) {
      Object amount = callQuiet(event, "getNewDamage");
      if (amount == null) {
         amount = callQuiet(event, "getHealthDamage");
      }
      return asFloat(amount, 0.0F);
   }

   static float a4DamageAmountForTest(Object event) {
      return a4DamageAmount(event);
   }

   static boolean catalogHasDuoTextForTest(String eventId) {
      NarratorLegacy.EventDef def = CATALOG.get(eventId);
      return def != null && def.otherText != null && !def.otherText.isBlank();
   }

   static String catalogCondenseGroupForTest(String eventId) {
      NarratorLegacy.EventDef def = CATALOG.get(eventId);
      return def == null ? "" : def.condenseGroup;
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
         // Le reset pilote doit également rendre les tests du cerveau reproductibles.
         doneSet(var1).removeIf(value -> value.startsWith(KV + "brain."));
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

   private static void observeA3Equipment(Object player) {
      try {
         HashSet<String> armor = new HashSet<>();
         Object rawArmor = callQuiet(player, "getArmorSlots");
         if (rawArmor instanceof Iterable<?> slots) {
            for (Object stack : slots) {
               if (stack != null && !asBool(callQuiet(stack, "isEmpty"))) {
                  String id = stackItemId(stack);
                  if (id != null && !id.isBlank()) {
                     armor.add(id);
                  }
               }
            }
         }

         String mainHand = stackItemId(callQuiet(player, "getMainHandItem"));
         String offHand = stackItemId(callQuiet(player, "getOffhandItem"));
         NarratorA3SignalDetector.Equipment snapshot = new NarratorA3SignalDetector.Equipment(armor, mainHand, offHand);
         for (NarratorA3SignalDetector.Signal signal : NarratorA3SignalDetector.equipment(snapshot)) {
            trigger(
               player,
               signal.eventId(),
               signal.source(),
               null,
               Map.of("equipment", signal.targetId(), "armor_slots", armor.size())
            );
         }
      } catch (Throwable error) {
         soft("a3Equipment", error);
      }
   }

   private static void observePendingA5Interactions(Object player, NarratorLegacy.PlayerState playerState) {
      long current = now();

      if (playerState.pendingFedAnimal != null) {
         Object animal = playerState.pendingFedAnimal;
         if (current - playerState.pendingFedAnimalAt > 2000L) {
            playerState.pendingFedAnimal = null;
         } else {
            boolean accepted = asBool(callQuiet(animal, "isInLove"));
            Object loveCause = callQuiet(animal, "getLoveCause");
            boolean credited = loveCause != null && Objects.equals(uuid(loveCause), uuid(player));
            NarratorA5SignalDetector.Signal signal = NarratorA5SignalDetector.animalFed(accepted, credited);
            if (signal != null) {
               playerState.pendingFedAnimal = null;
               state(serverOf(player)).lastFactAt = current;
               trigger(player, signal.eventId(), signal.source(), null, Map.of("entity", entityId(animal)));
            }
         }
      }

      if (playerState.pendingBellPos != null) {
         if (current - playerState.pendingBellAt > 2000L) {
            playerState.pendingBellLevel = null;
            playerState.pendingBellPos = null;
         } else {
            Object level = playerState.pendingBellLevel;
            Object pos = playerState.pendingBellPos;
            Object blockState = level == null ? null : callQuiet(level, "getBlockState", pos);
            Object blockEntity = level == null ? null : callQuiet(level, "getBlockEntity", pos);
            boolean ringing = asBool(callQuiet(blockEntity, "isShaking")) || asBool(fieldQuiet(blockEntity, "shaking"));
            NarratorA5SignalDetector.Signal signal = NarratorA5SignalDetector.villageBell(blockId(blockState), ringing);
            if (signal != null) {
               playerState.pendingBellLevel = null;
               playerState.pendingBellPos = null;
               state(serverOf(player)).lastFactAt = current;
               trigger(player, signal.eventId(), signal.source(), null, Map.of("block", signal.targetId()));
            }
         }
      }
   }

   private static void observeDrowningRecovery(Object player, NarratorLegacy.PlayerState state) {
      if (!state.drowningDanger) {
         return;
      }

      int air = asInt(callQuiet(player, "getAirSupply"), -1);
      int maxAir = asInt(callQuiet(player, "getMaxAirSupply"), 300);
      float health = asFloat(callQuiet(player, "getHealth"), 0.0F);
      NarratorA4SignalDetector.Signal signal = NarratorA4SignalDetector.drowningRecovered(state.drowningDanger, air, maxAir, health);
      if (signal != null) {
         state.drowningDanger = false;
         trigger(player, signal.eventId(), signal.source(), null, Map.of("air", air, "max_air", maxAir, "health", health));
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
      String rewardLabel;
      String condenseGroup = "";
      String lowHealthText;
      String otherContainerText;
      String longDistanceText;
      int priority = 60;
      int agePoints;
      int civScore;
      int rewardCount = 1;
      final Map<String, String> sourceVariants = new HashMap<>();
      final List<String> memoryTags = new ArrayList<>();
      final List<NarratorContextBrain.Variant> contextVariants = new ArrayList<>();

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
         var1.rewardLabel = str(var0, "reward_label");
         var1.condenseGroup = str(var0, "condense_group");
         var1.rewardCount = num(var0, "reward_count", 1);
         var1.lowHealthText = str(var0, "low_health_text");
         var1.otherContainerText = str(var0, "other_container_text");
         var1.longDistanceText = str(var0, "long_distance_text");
         if (var0.get("source_variants") instanceof Map<?, ?> var3) {
            for (Entry var5 : var3.entrySet()) {
               var1.sourceVariants.put(String.valueOf(var5.getKey()), String.valueOf(var5.getValue()));
            }
         }

         if (var0.get("memory_tags") instanceof List<?> tags) {
            for (Object tag : tags) {
               String value = String.valueOf(tag).trim();
               if (!value.isBlank()) {
                  var1.memoryTags.add(value);
               }
            }
         }

         if (var0.get("context_variants") instanceof List<?> variants) {
            for (Object rawVariant : variants) {
               if (rawVariant instanceof Map<?, ?> variantMap) {
                  NarratorContextBrain.Variant variant = NarratorContextBrain.fromMap(variantMap);
                  if (variant != null) {
                     var1.contextVariants.add(variant);
                  }
               }
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
      boolean drowningDanger;
      boolean giftCallbackDone;
      float lastHealth = Float.NaN;
      double maxAway;
      long lastMatrixInteractionAt;
      long recentGiftAt;
      long lastInventoryScanAt;
      long pendingFedAnimalAt;
      long pendingBellAt;
      String recentGiftItem;
      Object pendingFedAnimal;
      Object pendingBellLevel;
      Object pendingBellPos;
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
