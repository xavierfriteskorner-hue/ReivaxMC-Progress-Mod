package fr.reivaxmc.progress.story;

import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedData.Factory;

/** Sauvegarde mondiale, partagée en SOLO/DUO, de La Dette du Foyer. */
public final class C110TrailData extends SavedData {
   public static final String DATA_NAME = "reivaxmc_chapter2_debt";
   public static final int SCHEMA_VERSION = 1;

   private String stage = C110TrailRules.LOCKED;
   private long stageTick;
   private String lastActor = "";
   private final BlockPos[] sites = {BlockPos.ZERO, BlockPos.ZERO, BlockPos.ZERO};
   private int placedMask;
   private int discoveredMask;
   private int witnessMask;
   private boolean censusSpawned;
   private boolean concordance;
   private boolean registryOpened;
   private boolean completed;
   private boolean optionalRewarded;
   private long bastionDay = -1L;
   private long solidarityDay = -1L;
   private final Set<String> rewardedPlayers = new HashSet<>();

   public static C110TrailData create() { return new C110TrailData(); }

   public static C110TrailData get(MinecraftServer server) {
      return server.overworld().getDataStorage().computeIfAbsent(
         new Factory<C110TrailData>(C110TrailData::create, C110TrailData::load, null), DATA_NAME);
   }

   public static C110TrailData load(CompoundTag tag, Provider provider) {
      C110TrailData data = new C110TrailData();
      data.stage = tag.getString("Stage");
      if (data.stage.isBlank()) data.stage = C110TrailRules.LOCKED;
      data.stageTick = tag.getLong("StageTick");
      data.lastActor = tag.getString("LastActor");
      for (int i = 0; i < 3; i++) data.sites[i] = new BlockPos(tag.getInt("SiteX" + i), tag.getInt("SiteY" + i), tag.getInt("SiteZ" + i));
      data.placedMask = tag.getInt("PlacedMask");
      data.discoveredMask = tag.getInt("DiscoveredMask");
      data.witnessMask = tag.getInt("WitnessMask");
      data.censusSpawned = tag.getBoolean("CensusSpawned");
      data.concordance = tag.getBoolean("Concordance");
      data.registryOpened = tag.getBoolean("RegistryOpened");
      data.completed = tag.getBoolean("Completed");
      data.optionalRewarded = tag.getBoolean("OptionalRewarded");
      data.bastionDay = tag.getLong("BastionDay");
      data.solidarityDay = tag.getLong("SolidarityDay");
      ListTag rewards = tag.getList("RewardedPlayers", 8);
      for (int i = 0; i < rewards.size(); i++) data.rewardedPlayers.add(rewards.getString(i));
      return data;
   }

   @Override public synchronized CompoundTag save(CompoundTag tag, Provider provider) {
      tag.putInt("SchemaVersion", SCHEMA_VERSION);
      tag.putString("Stage", stage);
      tag.putLong("StageTick", stageTick);
      tag.putString("LastActor", lastActor);
      for (int i = 0; i < 3; i++) {
         tag.putInt("SiteX" + i, sites[i].getX()); tag.putInt("SiteY" + i, sites[i].getY()); tag.putInt("SiteZ" + i, sites[i].getZ());
      }
      tag.putInt("PlacedMask", placedMask);
      tag.putInt("DiscoveredMask", discoveredMask);
      tag.putInt("WitnessMask", witnessMask);
      tag.putBoolean("CensusSpawned", censusSpawned);
      tag.putBoolean("Concordance", concordance);
      tag.putBoolean("RegistryOpened", registryOpened);
      tag.putBoolean("Completed", completed);
      tag.putBoolean("OptionalRewarded", optionalRewarded);
      tag.putLong("BastionDay", bastionDay);
      tag.putLong("SolidarityDay", solidarityDay);
      ListTag rewards = new ListTag();
      for (String id : rewardedPlayers) rewards.add(StringTag.valueOf(id));
      tag.put("RewardedPlayers", rewards);
      return tag;
   }

   public synchronized boolean offer(long tick) {
      if (!C110TrailRules.LOCKED.equals(stage)) return false;
      stage = C110TrailRules.OFFERED; stageTick = tick; setDirty(); return true;
   }

   public synchronized boolean accept(String actor, long tick) {
      if (!C110TrailRules.OFFERED.equals(stage)) return false;
      stage = C110TrailRules.SEEK_RIFTS; lastActor = safe(actor); stageTick = tick; setDirty(); return true;
   }

   public synchronized void placeSite(int index, BlockPos pos) {
      if (index < 0 || index >= 3) return;
      sites[index] = pos.immutable(); placedMask |= 1 << index; setDirty();
   }

   public synchronized boolean discover(int index, String actor, long tick) {
      if (index < 0 || index >= 3 || (placedMask & (1 << index)) == 0 || (discoveredMask & (1 << index)) != 0) return false;
      discoveredMask |= 1 << index; lastActor = safe(actor); stageTick = tick;
      if (C110TrailRules.SEEK_RIFTS.equals(stage) && C110TrailRules.count(discoveredMask) >= C110TrailRules.REQUIRED_RIFTS) stage = C110TrailRules.RETURN_FOYER;
      setDirty(); return true;
   }

   public synchronized boolean markOptionalRewarded() {
      if (optionalRewarded || C110TrailRules.count(discoveredMask) < 3) return false;
      optionalRewarded = true; setDirty(); return true;
   }

   public synchronized boolean beginCensus(String actor, long tick) {
      if (!C110TrailRules.RETURN_FOYER.equals(stage)) return false;
      stage = C110TrailRules.CENSUS; lastActor = safe(actor); stageTick = tick; censusSpawned = false; witnessMask = 0; setDirty(); return true;
   }

   public synchronized void censusSpawned() { censusSpawned = true; setDirty(); }

   public synchronized boolean observeWitness(int index, String actor, long tick) {
      if (!C110TrailRules.CENSUS.equals(stage) || index < 0 || index >= 8 || (witnessMask & (1 << index)) != 0) return false;
      witnessMask |= 1 << index; lastActor = safe(actor); stageTick = tick;
      if (Integer.bitCount(witnessMask) >= C110TrailRules.REQUIRED_WITNESSES) stage = C110TrailRules.RETURN_SANCTUARY;
      setDirty(); return true;
   }

   public synchronized boolean concord(String actor, long tick) {
      if (!C110TrailRules.RETURN_SANCTUARY.equals(stage)) return false;
      concordance = true; stage = C110TrailRules.REGISTRY; lastActor = safe(actor); stageTick = tick; setDirty(); return true;
   }

   public synchronized boolean openRegistry(String actor, long tick) {
      if (!C110TrailRules.REGISTRY.equals(stage)) return false;
      registryOpened = true; completed = true; stage = C110TrailRules.COMPLETE; lastActor = safe(actor); stageTick = tick; setDirty(); return true;
   }

   public synchronized boolean claimReward(String uuid) {
      if (!completed || uuid == null || uuid.isBlank() || !rewardedPlayers.add(uuid)) return false;
      setDirty(); return true;
   }

   public synchronized boolean useBastion(long day) { if (bastionDay == day) return false; bastionDay = day; setDirty(); return true; }
   public synchronized boolean useSolidarity(long day) { if (solidarityDay == day) return false; solidarityDay = day; setDirty(); return true; }

   public synchronized void reset() {
      stage = C110TrailRules.LOCKED; stageTick = 0L; lastActor = ""; placedMask = discoveredMask = witnessMask = 0;
      for (int i = 0; i < 3; i++) sites[i] = BlockPos.ZERO;
      censusSpawned = concordance = registryOpened = completed = optionalRewarded = false;
      bastionDay = solidarityDay = -1L; rewardedPlayers.clear(); setDirty();
   }

   public synchronized Snapshot snapshot() {
      return new Snapshot(stage, stageTick, lastActor, sites.clone(), placedMask, discoveredMask, witnessMask, censusSpawned,
         concordance, registryOpened, completed, optionalRewarded, Set.copyOf(rewardedPlayers));
   }

   private static String safe(String value) { return value == null ? "" : value; }

   public record Snapshot(String stage, long stageTick, String lastActor, BlockPos[] sites, int placedMask, int discoveredMask,
      int witnessMask, boolean censusSpawned, boolean concordance, boolean registryOpened, boolean completed,
      boolean optionalRewarded, Set<String> rewardedPlayers) {
      public int discoveredCount() { return C110TrailRules.count(discoveredMask); }
      public int witnessCount() { return Integer.bitCount(witnessMask); }
   }
}
