package fr.reivaxmc.progress.story;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedData.Factory;

/** État partagé SOLO/DUO et persistant du Chapitre I — Le Foyer emprunté. */
public final class F91FoyerChapterData extends SavedData {
   public static final String DATA_NAME = "reivaxmc_chapter1_foyer";
   public static final int SCHEMA_VERSION = 2;

   private String stage = F91ChapterRules.LOCKED;
   private int homeSignals;
   private String doctrine = "";
   private final Set<String> councilVoters = new HashSet<>();
   private final Map<String, String> councilVotes = new HashMap<>();
   private String lastActor = "";
   private String echoDimension = "minecraft:overworld";
   private int echoX;
   private int echoY;
   private int echoZ;
   private boolean echoPlaced;
   private boolean echoExamined;
   private int witnessTarget;
   private int witnessesDefeated;
   private long stageTick;
   private long lastSpawnTick;
   private boolean completed;
   private final Set<String> rewardedPlayers = new HashSet<>();

   public static F91FoyerChapterData create() {
      return new F91FoyerChapterData();
   }

   public static F91FoyerChapterData get(MinecraftServer server) {
      return server.overworld().getDataStorage().computeIfAbsent(
         new Factory<F91FoyerChapterData>(F91FoyerChapterData::create, F91FoyerChapterData::load, null), DATA_NAME
      );
   }

   public static F91FoyerChapterData load(CompoundTag tag, Provider provider) {
      F91FoyerChapterData data = new F91FoyerChapterData();
      data.stage = tag.getString("Stage");
      if (data.stage.isBlank()) data.stage = F91ChapterRules.LOCKED;
      data.homeSignals = tag.getInt("HomeSignals");
      data.doctrine = tag.getString("Doctrine");
      ListTag voters = tag.getList("CouncilVoters", 8);
      for (int i = 0; i < voters.size(); i++) data.councilVoters.add(voters.getString(i));
      ListTag votes = tag.getList("CouncilVotes", 8);
      for (int i = 0; i < votes.size(); i++) {
         String encoded = votes.getString(i);
         int separator = encoded.indexOf('|');
         if (separator > 0) data.councilVotes.put(encoded.substring(0, separator), encoded.substring(separator + 1));
      }
      data.lastActor = tag.getString("LastActor");
      data.echoDimension = tag.getString("EchoDimension");
      if (data.echoDimension.isBlank()) data.echoDimension = "minecraft:overworld";
      data.echoX = tag.getInt("EchoX");
      data.echoY = tag.getInt("EchoY");
      data.echoZ = tag.getInt("EchoZ");
      data.echoPlaced = tag.getBoolean("EchoPlaced");
      data.echoExamined = tag.getBoolean("EchoExamined");
      data.witnessTarget = tag.getInt("WitnessTarget");
      data.witnessesDefeated = tag.getInt("WitnessesDefeated");
      data.stageTick = tag.getLong("StageTick");
      data.lastSpawnTick = tag.getLong("LastSpawnTick");
      data.completed = tag.getBoolean("Completed");
      ListTag rewards = tag.getList("RewardedPlayers", 8);
      for (int i = 0; i < rewards.size(); i++) data.rewardedPlayers.add(rewards.getString(i));
      return data;
   }

   @Override
   public synchronized CompoundTag save(CompoundTag tag, Provider provider) {
      tag.putInt("SchemaVersion", SCHEMA_VERSION);
      tag.putString("Stage", stage);
      tag.putInt("HomeSignals", homeSignals);
      tag.putString("Doctrine", doctrine);
      ListTag voters = new ListTag();
      for (String id : councilVoters) voters.add(StringTag.valueOf(id));
      tag.put("CouncilVoters", voters);
      ListTag votes = new ListTag();
      for (Map.Entry<String, String> vote : councilVotes.entrySet()) votes.add(StringTag.valueOf(vote.getKey() + "|" + vote.getValue()));
      tag.put("CouncilVotes", votes);
      tag.putString("LastActor", lastActor);
      tag.putString("EchoDimension", echoDimension);
      tag.putInt("EchoX", echoX);
      tag.putInt("EchoY", echoY);
      tag.putInt("EchoZ", echoZ);
      tag.putBoolean("EchoPlaced", echoPlaced);
      tag.putBoolean("EchoExamined", echoExamined);
      tag.putInt("WitnessTarget", witnessTarget);
      tag.putInt("WitnessesDefeated", witnessesDefeated);
      tag.putLong("StageTick", stageTick);
      tag.putLong("LastSpawnTick", lastSpawnTick);
      tag.putBoolean("Completed", completed);
      ListTag rewards = new ListTag();
      for (String id : rewardedPlayers) rewards.add(StringTag.valueOf(id));
      tag.put("RewardedPlayers", rewards);
      return tag;
   }

   public synchronized boolean begin(long tick, String actor) {
      if (!F91ChapterRules.LOCKED.equals(stage)) return false;
      stage = F91ChapterRules.SHAPE_FOYER;
      stageTick = tick;
      lastActor = safe(actor);
      setDirty();
      return true;
   }

   public synchronized boolean addHomeSignal(int signal, String actor, long tick) {
      if (!F91ChapterRules.SHAPE_FOYER.equals(stage) || signal == 0 || (homeSignals & signal) != 0) return false;
      homeSignals |= signal;
      lastActor = safe(actor);
      stageTick = tick;
      if (F91ChapterRules.signalCount(homeSignals) >= F91ChapterRules.REQUIRED_HOME_SIGNALS) {
         stage = F91ChapterRules.CHOOSE_PRIORITY;
      }
      setDirty();
      return true;
   }

   public synchronized CouncilResult vote(String value, String voterUuid, String actor, Set<String> requiredVoters, long tick) {
      if (!F91ChapterRules.CHOOSE_PRIORITY.equals(stage) || value == null || value.isBlank()
         || voterUuid == null || voterUuid.isBlank() || requiredVoters == null || requiredVoters.isEmpty()) return CouncilResult.INVALID;
      if (councilVoters.isEmpty()) councilVoters.addAll(requiredVoters);
      if (!councilVoters.contains(voterUuid)) return CouncilResult.INVALID;
      councilVotes.put(voterUuid, value);
      lastActor = safe(actor);
      stageTick = tick;
      F91CouncilRules.Result result = F91CouncilRules.evaluate(councilVoters, councilVotes);
      if (result.state() == F91CouncilRules.State.WAITING) {
         setDirty();
         return CouncilResult.WAITING;
      }
      if (result.state() == F91CouncilRules.State.DISSONANCE) {
         setDirty();
         return CouncilResult.DISSONANCE;
      }
      if (result.state() != F91CouncilRules.State.CONFIRMED) return CouncilResult.INVALID;
      doctrine = result.doctrine();
      councilVotes.clear();
      councilVoters.clear();
      stage = F91ChapterRules.FIND_ECHO;
      setDirty();
      return CouncilResult.CONFIRMED;
   }

   public synchronized void placeEcho(BlockPos pos, String dimension, long tick) {
      echoX = pos.getX();
      echoY = pos.getY();
      echoZ = pos.getZ();
      echoDimension = safe(dimension);
      echoPlaced = true;
      stageTick = tick;
      setDirty();
   }

   public synchronized boolean examineEcho(String actor, long tick) {
      if (!F91ChapterRules.FIND_ECHO.equals(stage) || !echoPlaced || echoExamined) return false;
      echoExamined = true;
      lastActor = safe(actor);
      stage = F91ChapterRules.RETURN_FRAGMENT;
      stageTick = tick;
      setDirty();
      return true;
   }

   public synchronized boolean beginDefense(int target, String actor, long tick) {
      if (!F91ChapterRules.RETURN_FRAGMENT.equals(stage)) return false;
      witnessTarget = Math.max(1, target);
      witnessesDefeated = 0;
      lastActor = safe(actor);
      stage = F91ChapterRules.DEFEND_FOYER;
      stageTick = tick;
      lastSpawnTick = 0L;
      setDirty();
      return true;
   }

   public synchronized boolean markWitnessDefeated(String actor, long tick) {
      if (!F91ChapterRules.DEFEND_FOYER.equals(stage)) return false;
      witnessesDefeated = Math.min(witnessTarget, witnessesDefeated + 1);
      lastActor = safe(actor);
      stageTick = tick;
      if (witnessesDefeated >= witnessTarget) {
         completed = true;
         stage = F91ChapterRules.COMPLETE;
      }
      setDirty();
      return true;
   }

   public synchronized void markSpawn(long tick) {
      lastSpawnTick = tick;
      setDirty();
   }

   public synchronized boolean claimReward(String uuid) {
      if (!completed || uuid == null || uuid.isBlank() || !rewardedPlayers.add(uuid)) return false;
      setDirty();
      return true;
   }

   public synchronized void reset() {
      stage = F91ChapterRules.LOCKED;
      homeSignals = 0;
      doctrine = "";
      councilVoters.clear();
      councilVotes.clear();
      lastActor = "";
      echoDimension = "minecraft:overworld";
      echoX = echoY = echoZ = 0;
      echoPlaced = false;
      echoExamined = false;
      witnessTarget = 0;
      witnessesDefeated = 0;
      stageTick = 0L;
      lastSpawnTick = 0L;
      completed = false;
      rewardedPlayers.clear();
      setDirty();
   }

   /**
    * État terminal cohérent réservé aux profils DEV. Il permet de préparer un
    * chapitre ultérieur sans laisser le Chapitre I actif en arrière-plan.
    */
   public synchronized void devComplete(long tick, String actor, String chosenDoctrine) {
      stage = F91ChapterRules.COMPLETE;
      homeSignals = F91ChapterRules.REST | F91ChapterRules.STORAGE | F91ChapterRules.WORK;
      doctrine = Set.of(F91ChapterRules.BASTION, F91ChapterRules.MEMORY, F91ChapterRules.SOLIDARITY).contains(chosenDoctrine)
         ? chosenDoctrine : F91ChapterRules.MEMORY;
      councilVoters.clear();
      councilVotes.clear();
      lastActor = safe(actor);
      echoPlaced = true;
      echoExamined = true;
      witnessTarget = 1;
      witnessesDefeated = 1;
      stageTick = Math.max(0L, tick);
      lastSpawnTick = stageTick;
      completed = true;
      setDirty();
   }

   public synchronized Snapshot snapshot() {
      return new Snapshot(stage, homeSignals, doctrine, Set.copyOf(councilVoters), Map.copyOf(councilVotes), lastActor, echoDimension, new BlockPos(echoX, echoY, echoZ), echoPlaced, echoExamined,
         witnessTarget, witnessesDefeated, stageTick, lastSpawnTick, completed, Set.copyOf(rewardedPlayers));
   }

   private static String safe(String value) {
      return value == null ? "" : value;
   }

   public enum CouncilResult { INVALID, WAITING, DISSONANCE, CONFIRMED }

   public record Snapshot(String stage, int homeSignals, String doctrine, Set<String> councilVoters, Map<String, String> councilVotes,
      String lastActor, String echoDimension, BlockPos echoPos,
      boolean echoPlaced, boolean echoExamined, int witnessTarget, int witnessesDefeated, long stageTick, long lastSpawnTick,
      boolean completed, Set<String> rewardedPlayers) {
   }
}
