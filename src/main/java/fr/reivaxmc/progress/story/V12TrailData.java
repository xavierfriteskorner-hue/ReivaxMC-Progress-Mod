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

/** Sauvegarde mondiale et idempotente du Chapitre III. */
public final class V12TrailData extends SavedData {
   public static final String DATA_NAME="reivaxmc_chapter3_names";
   private String stage=V12TrailRules.LOCKED, actor="", policy="";
   private long stageTick, leftTick, rightTick;
   private BlockPos procession=BlockPos.ZERO, house=BlockPos.ZERO;
   private boolean sitesBuilt, optionalFound, rewarded;
   private int traceMask, vanished;
   private String leftPlayer="",rightPlayer="";
   private final Set<String> participants=new HashSet<>(), acknowledgements=new HashSet<>();
   private final Map<String,String> perceptions=new HashMap<>(), votes=new HashMap<>();

   public static V12TrailData get(MinecraftServer s){return s.overworld().getDataStorage().computeIfAbsent(
      new SavedData.Factory<V12TrailData>(V12TrailData::new,V12TrailData::load,null),DATA_NAME);}
   public static V12TrailData load(CompoundTag t,Provider p){
      V12TrailData d=new V12TrailData(); d.stage=t.getString("Stage"); if(d.stage.isBlank())d.stage=V12TrailRules.LOCKED;
      d.actor=t.getString("Actor"); d.policy=t.getString("Policy"); d.stageTick=t.getLong("StageTick"); d.leftTick=t.getLong("LeftTick"); d.rightTick=t.getLong("RightTick");
      d.procession=new BlockPos(t.getInt("PX"),t.getInt("PY"),t.getInt("PZ")); d.house=new BlockPos(t.getInt("HX"),t.getInt("HY"),t.getInt("HZ"));
      d.sitesBuilt=t.getBoolean("SitesBuilt"); d.optionalFound=t.getBoolean("OptionalFound"); d.rewarded=t.getBoolean("Rewarded"); d.traceMask=t.getInt("TraceMask"); d.vanished=t.getInt("Vanished");
      d.leftPlayer=t.getString("LeftPlayer"); d.rightPlayer=t.getString("RightPlayer");
      readSet(t,"Participants",d.participants); readSet(t,"Acks",d.acknowledgements); readMap(t,"Perceptions",d.perceptions); readMap(t,"Votes",d.votes); return d;
   }
   @Override public synchronized CompoundTag save(CompoundTag t,Provider p){
      t.putString("Stage",stage);t.putString("Actor",actor);t.putString("Policy",policy);t.putLong("StageTick",stageTick);t.putLong("LeftTick",leftTick);t.putLong("RightTick",rightTick);
      t.putInt("PX",procession.getX());t.putInt("PY",procession.getY());t.putInt("PZ",procession.getZ());t.putInt("HX",house.getX());t.putInt("HY",house.getY());t.putInt("HZ",house.getZ());
      t.putBoolean("SitesBuilt",sitesBuilt);t.putBoolean("OptionalFound",optionalFound);t.putBoolean("Rewarded",rewarded);t.putInt("TraceMask",traceMask);t.putInt("Vanished",vanished);
      t.putString("LeftPlayer",leftPlayer);t.putString("RightPlayer",rightPlayer);writeSet(t,"Participants",participants);writeSet(t,"Acks",acknowledgements);writeMap(t,"Perceptions",perceptions);writeMap(t,"Votes",votes);return t;
   }
   private static void readSet(CompoundTag t,String k,Set<String> out){ListTag l=t.getList(k,8);for(int i=0;i<l.size();i++)out.add(l.getString(i));}
   private static void writeSet(CompoundTag t,String k,Set<String> in){ListTag l=new ListTag();for(String s:in)l.add(StringTag.valueOf(s));t.put(k,l);}
   private static void readMap(CompoundTag t,String k,Map<String,String> out){ListTag l=t.getList(k,10);for(int i=0;i<l.size();i++){CompoundTag e=l.getCompound(i);out.put(e.getString("K"),e.getString("V"));}}
   private static void writeMap(CompoundTag t,String k,Map<String,String> in){ListTag l=new ListTag();in.forEach((a,b)->{CompoundTag e=new CompoundTag();e.putString("K",a);e.putString("V",b);l.add(e);});t.put(k,l);}

   public synchronized boolean offer(long tick){if(!V12TrailRules.LOCKED.equals(stage))return false;stage=V12TrailRules.OFFERED;stageTick=tick;setDirty();return true;}
   public synchronized boolean accept(String who,long tick,Map<String,String> names){if(!V12TrailRules.OFFERED.equals(stage))return false;stage=V12TrailRules.EIGHTH_LINE;actor=who;stageTick=tick;participants.clear();participants.addAll(names.keySet());perceptions.clear();perceptions.putAll(names);setDirty();return true;}
   public synchronized boolean acknowledge(String id,long tick){if(!V12TrailRules.EIGHTH_LINE.equals(stage)||!participants.contains(id))return false;acknowledgements.add(id);if(acknowledgements.containsAll(participants)){stage=V12TrailRules.PROCESSION_LOCATE;stageTick=tick;}setDirty();return true;}
   public synchronized void sites(BlockPos p,BlockPos h){procession=p;house=h;sitesBuilt=true;setDirty();}
   public synchronized boolean beginProcession(long tick){if(!V12TrailRules.PROCESSION_LOCATE.equals(stage))return false;stage=V12TrailRules.PROCESSION_OBSERVE;stageTick=tick;vanished=0;setDirty();return true;}
   public synchronized boolean vanish(int count,long tick){if(!V12TrailRules.PROCESSION_OBSERVE.equals(stage)||count<=vanished)return false;vanished=Math.min(6,count);if(vanished>=6){stage=V12TrailRules.HOUSE_LOCATE;stageTick=tick;}setDirty();return true;}
   public synchronized boolean enterHouse(long tick){if(!V12TrailRules.HOUSE_LOCATE.equals(stage))return false;stage=V12TrailRules.HOUSE_INSPECT;stageTick=tick;setDirty();return true;}
   public synchronized boolean inspect(int i,long tick){if(!V12TrailRules.HOUSE_INSPECT.equals(stage)||i<0||i>3||(traceMask&(1<<i))!=0)return false;traceMask|=1<<i;if(Integer.bitCount(traceMask)>=4){stage=V12TrailRules.RETURN_REGISTRY;stageTick=tick;}setDirty();return true;}
   public synchronized boolean returnRegistry(long tick){if(!V12TrailRules.RETURN_REGISTRY.equals(stage))return false;stage=V12TrailRules.CROSS_CALL;stageTick=tick;setDirty();return true;}
   public synchronized boolean activateLectern(boolean left,String player,long tick){if(!V12TrailRules.CROSS_CALL.equals(stage))return false;if(left){leftPlayer=player;leftTick=tick;}else{rightPlayer=player;rightTick=tick;}if(V12TrailRules.crossComplete(leftPlayer,leftTick,rightPlayer,rightTick,participants.size())){stage=V12TrailRules.MEMORY_COUNCIL;stageTick=tick;}setDirty();return true;}
   public synchronized boolean vote(String id,String choice,long tick){if(!V12TrailRules.MEMORY_COUNCIL.equals(stage)||!participants.contains(id)||!(choice.equals("RESTORE_NAMES")||choice.equals("KEEP_BOTH")||choice.equals("LEAVE_BLANKS")))return false;votes.put(id,choice);if(votes.keySet().containsAll(participants)&&new HashSet<>(votes.values()).size()==1){policy=choice;stage=V12TrailRules.COMPLETE;stageTick=tick;}setDirty();return true;}
   public synchronized boolean reward(){if(!V12TrailRules.COMPLETE.equals(stage)||rewarded)return false;rewarded=true;setDirty();return true;}
   public synchronized void optional(){if(!optionalFound){optionalFound=true;setDirty();}}
   public synchronized void reset(){stage=V12TrailRules.LOCKED;actor=policy=leftPlayer=rightPlayer="";stageTick=leftTick=rightTick=0;procession=house=BlockPos.ZERO;sitesBuilt=optionalFound=rewarded=false;traceMask=vanished=0;participants.clear();acknowledgements.clear();perceptions.clear();votes.clear();setDirty();}
   /** État terminal cohérent réservé aux profils DEV et aux tests accélérés. */
   public synchronized void devComplete(long tick,String who,String chosenPolicy){
      stage=V12TrailRules.COMPLETE;actor=who==null?"":who;policy=switch(chosenPolicy){case"RESTORE_NAMES","KEEP_BOTH","LEAVE_BLANKS"->chosenPolicy;default->"KEEP_BOTH";};
      stageTick=Math.max(0L,tick);traceMask=0b1111;vanished=6;sitesBuilt=true;leftPlayer=rightPlayer=actor;leftTick=stageTick;rightTick=stageTick;
      participants.clear();if(!actor.isBlank())participants.add(actor);acknowledgements.clear();acknowledgements.addAll(participants);votes.clear();for(String id:participants)votes.put(id,policy);rewarded=true;setDirty();
   }
   public synchronized Snapshot snapshot(){return new Snapshot(stage,stageTick,procession,house,sitesBuilt,traceMask,vanished,Set.copyOf(participants),Set.copyOf(acknowledgements),Map.copyOf(perceptions),Map.copyOf(votes),policy,optionalFound,rewarded);}
   public record Snapshot(String stage,long stageTick,BlockPos procession,BlockPos house,boolean sitesBuilt,int traceMask,int vanished,Set<String> participants,Set<String> acknowledgements,Map<String,String> perceptions,Map<String,String> votes,String policy,boolean optionalFound,boolean rewarded){public int traceCount(){return Integer.bitCount(traceMask);}}
}
