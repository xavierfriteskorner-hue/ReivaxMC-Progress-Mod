package fr.reivaxmc.progress.story;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedData.Factory;

public final class ArchiveStateData18 extends SavedData {
   public static final String DATA_NAME = "reivaxmc_archives18";
   public static final int SCHEMA_VERSION = 1;
   private int schemaVersion = 1;
   private final LinkedHashMap<String, ArchiveStateData18.ArchiveEntry> entries = new LinkedHashMap<>();
   private int revision;
   private long updatedAt;
   private transient boolean loadedFromDisk;

   public static ArchiveStateData18 create() {
      return new ArchiveStateData18();
   }

   public static ArchiveStateData18 get(MinecraftServer var0) {
      return (ArchiveStateData18)var0.overworld()
         .getDataStorage()
         .computeIfAbsent(new Factory(ArchiveStateData18::create, ArchiveStateData18::load, null), "reivaxmc_archives18");
   }

   public static ArchiveStateData18 getForServer(Object var0) {
      if (var0 instanceof MinecraftServer var1) {
         return get(var1);
      } else {
         throw new IllegalArgumentException("Expected MinecraftServer, got " + (var0 == null ? "null" : var0.getClass().getName()));
      }
   }

   public static ArchiveStateData18 load(CompoundTag var0, Provider var1) {
      ArchiveStateData18 var2 = new ArchiveStateData18();
      var2.schemaVersion = Math.max(1, var0.getInt("SchemaVersion"));
      var2.revision = Math.max(0, var0.getInt("Revision"));
      var2.updatedAt = var0.getLong("UpdatedAt");
      int var3 = Math.max(0, var0.getInt("EntryCount"));

      for (int var4 = 0; var4 < var3; var4++) {
         String var5 = "Entry" + var4 + ".";
         String var6 = var0.getString(var5 + "ArchiveKey");
         if (!var6.isBlank()) {
            ArchiveStateData18.ArchiveEntry var7 = new ArchiveStateData18.ArchiveEntry(
               var6,
               var0.getString(var5 + "FactType"),
               var0.getString(var5 + "Subject"),
               var0.getString(var5 + "ActorId"),
               var0.getLong(var5 + "DiscoveredAt"),
               Math.max(1, var0.getInt(var5 + "InterpretationVersion")),
               var0.getString(var5 + "InterpretationKey")
            );
            var2.entries.putIfAbsent(var6, var7);
         }
      }

      var2.loadedFromDisk = true;
      return var2;
   }

   public synchronized CompoundTag save(CompoundTag var1, Provider var2) {
      var1.putInt("SchemaVersion", this.schemaVersion);
      var1.putInt("Revision", this.revision);
      var1.putLong("UpdatedAt", this.updatedAt);
      var1.putInt("EntryCount", this.entries.size());
      int var3 = 0;

      for (ArchiveStateData18.ArchiveEntry var5 : this.entries.values()) {
         String var6 = "Entry" + var3++ + ".";
         var1.putString(var6 + "ArchiveKey", var5.archiveKey());
         var1.putString(var6 + "FactType", safe(var5.factType()));
         var1.putString(var6 + "Subject", safe(var5.subject()));
         var1.putString(var6 + "ActorId", safe(var5.actorId()));
         var1.putLong(var6 + "DiscoveredAt", var5.discoveredAt());
         var1.putInt(var6 + "InterpretationVersion", Math.max(1, var5.interpretationVersion()));
         var1.putString(var6 + "InterpretationKey", safe(var5.interpretationKey()));
      }

      return var1;
   }

   public synchronized boolean acceptFact(StoryFact var1) {
      String var2 = keyForFact(var1 == null ? null : var1.factType());
      if (var2 != null && !this.entries.containsKey(var2)) {
         ArchiveStateData18.ArchiveEntry var3 = new ArchiveStateData18.ArchiveEntry(
            var2,
            var1.factType(),
            safe(var1.value("subject")),
            safe(var1.actorId()),
            var1.observedAt(),
            1,
            Age1LegacyFacts.isAge1(var1.factType()) ? "AGE1_OBSERVED_FACT" : "TECH_OBSERVED_ACTION"
         );
         this.entries.put(var2, var3);
         this.revision++;
         this.updatedAt = System.currentTimeMillis();
         this.setDirty();
         return true;
      } else {
         return false;
      }
   }

   public synchronized boolean updateInterpretation(String var1, int var2, String var3) {
      ArchiveStateData18.ArchiveEntry var4 = this.entries.get(var1);
      if (var4 != null && var2 > var4.interpretationVersion()) {
         this.entries
            .put(
               var1,
               new ArchiveStateData18.ArchiveEntry(var4.archiveKey(), var4.factType(), var4.subject(), var4.actorId(), var4.discoveredAt(), var2, safe(var3))
            );
         this.revision++;
         this.updatedAt = System.currentTimeMillis();
         this.setDirty();
         return true;
      } else {
         return false;
      }
   }

   public synchronized ArchiveStateData18.ArchiveEntry entry(String var1) {
      return this.entries.get(var1);
   }

   public synchronized ArchiveStateData18.Snapshot snapshot() {
      return new ArchiveStateData18.Snapshot(
         this.entries.size(), this.revision, this.updatedAt, this.loadedFromDisk, List.copyOf(new ArrayList<>(this.entries.keySet()))
      );
   }

   public synchronized boolean hasPilotSet() {
      return this.entries.containsKey("TECH_FIRST_CRAFT") && this.entries.containsKey("TECH_FIRST_BREAK") && this.entries.containsKey("TECH_FIRST_PLACE");
   }

   private static String keyForFact(String var0) {
      if (var0 == null) {
         return null;
      } else {
         return switch (var0) {
            case "ITEM_CRAFTED" -> "TECH_FIRST_CRAFT";
            case "BLOCK_BROKEN" -> "TECH_FIRST_BREAK";
            case "BLOCK_PLACED" -> "TECH_FIRST_PLACE";
            case "AGE1_RESONANCE_MANIFESTED" -> "AGE1_RESONANCE";
            case "AGE1_STELA_DISCOVERED" -> "AGE1_STELA";
            case "AGE1_FRAGMENT_FOUND" -> "AGE1_FRAGMENT";
            case "AGE1_MATRIX_RECOGNIZED" -> "AGE1_MATRIX";
            case "AGE1_FIRST_PAGE_RECORDED" -> "AGE1_FIRST_PAGE";
            case "AGE1_FOYER_FOUNDED" -> "AGE1_FOYER";
            case "AGE1_DISTANT_RESPONSE" -> "AGE1_RESPONSE";
            default -> null;
         };
      }
   }

   private static String safe(String var0) {
      return var0 == null ? "" : var0;
   }

   public static record ArchiveEntry(
      String archiveKey, String factType, String subject, String actorId, long discoveredAt, int interpretationVersion, String interpretationKey
   ) {
   }

   public static record Snapshot(int facts, int revision, long updatedAt, boolean loadedFromDisk, List<String> keys) {
   }
}
