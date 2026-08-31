package fr.reivaxmc.progress.story;

import java.util.LinkedHashSet;
import java.util.Set;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedData.Factory;

public final class Age1BridgeStateData18 extends SavedData {
   public static final String DATA_NAME = "reivaxmc_age1_bridge18";
   public static final int SCHEMA_VERSION = 1;
   private int schemaVersion = 1;
   private final LinkedHashSet<String> emitted = new LinkedHashSet<>();
   private int revision;
   private long updatedAt;
   private transient boolean loadedFromDisk;

   public static Age1BridgeStateData18 create() {
      return new Age1BridgeStateData18();
   }

   public static Age1BridgeStateData18 get(MinecraftServer var0) {
      return (Age1BridgeStateData18)var0.overworld()
         .getDataStorage()
         .computeIfAbsent(new Factory(Age1BridgeStateData18::create, Age1BridgeStateData18::load, null), "reivaxmc_age1_bridge18");
   }

   public static Age1BridgeStateData18 getForServer(Object var0) {
      if (var0 instanceof MinecraftServer var1) {
         return get(var1);
      } else {
         throw new IllegalArgumentException("Expected MinecraftServer, got " + (var0 == null ? "null" : var0.getClass().getName()));
      }
   }

   public static Age1BridgeStateData18 load(CompoundTag var0, Provider var1) {
      Age1BridgeStateData18 var2 = new Age1BridgeStateData18();
      var2.schemaVersion = Math.max(1, var0.getInt("SchemaVersion"));
      var2.revision = Math.max(0, var0.getInt("Revision"));
      var2.updatedAt = var0.getLong("UpdatedAt");
      int var3 = Math.max(0, var0.getInt("EmittedCount"));

      for (int var4 = 0; var4 < var3; var4++) {
         String var5 = var0.getString("Emitted" + var4);
         if (!var5.isBlank()) {
            var2.emitted.add(var5);
         }
      }

      var2.loadedFromDisk = true;
      return var2;
   }

   public synchronized CompoundTag save(CompoundTag var1, Provider var2) {
      var1.putInt("SchemaVersion", this.schemaVersion);
      var1.putInt("Revision", this.revision);
      var1.putLong("UpdatedAt", this.updatedAt);
      var1.putInt("EmittedCount", this.emitted.size());
      int var3 = 0;

      for (String var5 : this.emitted) {
         var1.putString("Emitted" + var3++, var5);
      }

      return var1;
   }

   public synchronized boolean has(String var1) {
      return this.emitted.contains(var1);
   }

   public synchronized boolean markEmitted(String var1) {
      if (var1 != null && !var1.isBlank() && !this.emitted.contains(var1)) {
         this.emitted.add(var1);
         this.revision++;
         this.updatedAt = System.currentTimeMillis();
         this.setDirty();
         return true;
      } else {
         return false;
      }
   }

   public synchronized Age1BridgeStateData18.Snapshot snapshot() {
      return new Age1BridgeStateData18.Snapshot(this.emitted.size(), this.revision, this.updatedAt, this.loadedFromDisk, Set.copyOf(this.emitted));
   }

   public static record Snapshot(int emitted, int revision, long updatedAt, boolean loadedFromDisk, Set<String> keys) {
   }
}
