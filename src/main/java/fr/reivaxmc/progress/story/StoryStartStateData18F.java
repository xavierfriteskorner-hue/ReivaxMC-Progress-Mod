package fr.reivaxmc.progress.story;

import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedData.Factory;

public final class StoryStartStateData18F extends SavedData {
   public static final String DATA_NAME = "reivaxmc_story_start18f";
   public static final int SCHEMA_VERSION = 2;
   private int schemaVersion = 2;
   private boolean modeInitialized;
   private boolean managed;
   private boolean started;
   private boolean tracePlaced;
   private boolean traceExamined;
   private long startTick;
   private long traceExaminedTick;
   private String startedBy = "";
   private String startedByName = "";
   private String traceExaminedBy = "";
   private int traceX;
   private int traceY;
   private int traceZ;
   private int revision;
   private transient boolean loadedFromDisk;

   public static StoryStartStateData18F create() {
      return new StoryStartStateData18F();
   }

   public static StoryStartStateData18F get(MinecraftServer var0) {
      return (StoryStartStateData18F)var0.overworld()
         .getDataStorage()
         .computeIfAbsent(new Factory<StoryStartStateData18F>(StoryStartStateData18F::create, StoryStartStateData18F::load, null), "reivaxmc_story_start18f");
   }

   public static StoryStartStateData18F getForServer(Object var0) {
      if (var0 instanceof MinecraftServer var1) {
         return get(var1);
      } else {
         throw new IllegalArgumentException("Expected MinecraftServer");
      }
   }

   public static StoryStartStateData18F load(CompoundTag var0, Provider var1) {
      StoryStartStateData18F var2 = new StoryStartStateData18F();
      var2.schemaVersion = Math.max(1, var0.getInt("SchemaVersion"));
      var2.modeInitialized = var0.getBoolean("ModeInitialized");
      var2.managed = var0.getBoolean("Managed");
      var2.started = var0.getBoolean("Started");
      var2.tracePlaced = var0.getBoolean("TracePlaced");
      var2.traceExamined = var0.getBoolean("TraceExamined");
      var2.startTick = var0.getLong("StartTick");
      var2.traceExaminedTick = var0.getLong("TraceExaminedTick");
      var2.startedBy = var0.getString("StartedBy");
      var2.startedByName = var0.getString("StartedByName");
      var2.traceExaminedBy = var0.getString("TraceExaminedBy");
      var2.traceX = var0.getInt("TraceX");
      var2.traceY = var0.getInt("TraceY");
      var2.traceZ = var0.getInt("TraceZ");
      var2.revision = var0.getInt("Revision");
      var2.loadedFromDisk = true;
      return var2;
   }

   public synchronized CompoundTag save(CompoundTag var1, Provider var2) {
      var1.putInt("SchemaVersion", 2);
      var1.putBoolean("ModeInitialized", this.modeInitialized);
      var1.putBoolean("Managed", this.managed);
      var1.putBoolean("Started", this.started);
      var1.putBoolean("TracePlaced", this.tracePlaced);
      var1.putBoolean("TraceExamined", this.traceExamined);
      var1.putLong("StartTick", this.startTick);
      var1.putLong("TraceExaminedTick", this.traceExaminedTick);
      var1.putString("StartedBy", this.startedBy == null ? "" : this.startedBy);
      var1.putString("StartedByName", this.startedByName == null ? "" : this.startedByName);
      var1.putString("TraceExaminedBy", this.traceExaminedBy == null ? "" : this.traceExaminedBy);
      var1.putInt("TraceX", this.traceX);
      var1.putInt("TraceY", this.traceY);
      var1.putInt("TraceZ", this.traceZ);
      var1.putInt("Revision", this.revision);
      return var1;
   }

   public synchronized void initializeMode(boolean var1) {
      if (!this.modeInitialized) {
         this.modeInitialized = true;
         this.managed = var1;
         this.revision++;
         this.setDirty();
      }
   }

   public synchronized void forceManaged() {
      if (!this.modeInitialized || !this.managed) {
         this.modeInitialized = true;
         this.managed = true;
         this.revision++;
         this.setDirty();
      }
   }

   public synchronized boolean markStarted(long var1, String var3, String var4, int var5, int var6, int var7) {
      if (this.managed && !this.started) {
         this.started = true;
         this.startTick = var1;
         this.startedBy = var3 == null ? "" : var3;
         this.startedByName = var4 == null ? "" : var4;
         this.traceX = var5;
         this.traceY = var6;
         this.traceZ = var7;
         this.revision++;
         this.setDirty();
         return true;
      } else {
         return false;
      }
   }

   public synchronized boolean markTracePlaced(int var1, int var2, int var3) {
      if (this.started && !this.tracePlaced) {
         this.tracePlaced = true;
         this.traceX = var1;
         this.traceY = var2;
         this.traceZ = var3;
         this.revision++;
         this.setDirty();
         return true;
      } else {
         return false;
      }
   }

   public synchronized boolean markTraceExamined(long var1, String var3) {
      if (this.started && this.tracePlaced && !this.traceExamined) {
         this.traceExamined = true;
         this.traceExaminedTick = var1;
         this.traceExaminedBy = var3 == null ? "" : var3;
         this.revision++;
         this.setDirty();
         return true;
      } else {
         return false;
      }
   }

   public synchronized boolean restartInterruptedIntro(long var1) {
      if (this.managed && this.started && !this.tracePlaced) {
         this.startTick = var1;
         this.revision++;
         this.setDirty();
         return true;
      } else {
         return false;
      }
   }

   public synchronized StoryStartStateData18F.Snapshot snapshot() {
      return new StoryStartStateData18F.Snapshot(
         this.modeInitialized,
         this.managed,
         this.started,
         this.tracePlaced,
         this.traceExamined,
         this.startTick,
         this.traceExaminedTick,
         this.startedBy,
         this.startedByName,
         this.traceExaminedBy,
         this.traceX,
         this.traceY,
         this.traceZ,
         this.revision,
         this.loadedFromDisk
      );
   }

   public static record Snapshot(
      boolean modeInitialized,
      boolean managed,
      boolean started,
      boolean tracePlaced,
      boolean traceExamined,
      long startTick,
      long traceExaminedTick,
      String startedBy,
      String startedByName,
      String traceExaminedBy,
      int traceX,
      int traceY,
      int traceZ,
      int revision,
      boolean loadedFromDisk
   ) {
   }
}
