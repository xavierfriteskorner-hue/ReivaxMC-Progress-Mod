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

/** Sauvegarde mondiale, reprenable et idempotente du Chapitre IV. */
public final class V13Chapter4Data extends SavedData {
   public static final String DATA_NAME="reivaxmc_chapter4_memory";
   private String stage=V13Chapter4Rules.LOCKED,leftPlayer="",rightPlayer="";
   private long stageTick,leftTick,rightTick;
   private int testimonyMask,optionalMask;
   private BlockPos archive=BlockPos.ZERO;
   private boolean siteBuilt,fragmentClaimed,rewarded;
   private final Set<String> participants=new HashSet<>(),acknowledgements=new HashSet<>();

   public static V13Chapter4Data get(MinecraftServer s){return s.overworld().getDataStorage().computeIfAbsent(
      new SavedData.Factory<V13Chapter4Data>(V13Chapter4Data::new,V13Chapter4Data::load,null),DATA_NAME);}
   public static V13Chapter4Data load(CompoundTag t,Provider p){V13Chapter4Data d=new V13Chapter4Data();d.stage=t.getString("Stage");if(d.stage.isBlank())d.stage=V13Chapter4Rules.LOCKED;
      d.stageTick=t.getLong("StageTick");d.leftTick=t.getLong("LeftTick");d.rightTick=t.getLong("RightTick");d.leftPlayer=t.getString("LeftPlayer");d.rightPlayer=t.getString("RightPlayer");
      d.testimonyMask=t.getInt("TestimonyMask");d.optionalMask=t.getInt("OptionalMask");d.archive=new BlockPos(t.getInt("AX"),t.getInt("AY"),t.getInt("AZ"));d.siteBuilt=t.getBoolean("SiteBuilt");d.fragmentClaimed=t.getBoolean("FragmentClaimed");d.rewarded=t.getBoolean("Rewarded");
      readSet(t,"Participants",d.participants);readSet(t,"Acknowledgements",d.acknowledgements);return d;}
   @Override public synchronized CompoundTag save(CompoundTag t,Provider p){t.putString("Stage",stage);t.putLong("StageTick",stageTick);t.putLong("LeftTick",leftTick);t.putLong("RightTick",rightTick);t.putString("LeftPlayer",leftPlayer);t.putString("RightPlayer",rightPlayer);
      t.putInt("TestimonyMask",testimonyMask);t.putInt("OptionalMask",optionalMask);t.putInt("AX",archive.getX());t.putInt("AY",archive.getY());t.putInt("AZ",archive.getZ());t.putBoolean("SiteBuilt",siteBuilt);t.putBoolean("FragmentClaimed",fragmentClaimed);t.putBoolean("Rewarded",rewarded);writeSet(t,"Participants",participants);writeSet(t,"Acknowledgements",acknowledgements);return t;}
   private static void readSet(CompoundTag t,String k,Set<String> out){ListTag l=t.getList(k,8);for(int i=0;i<l.size();i++)out.add(l.getString(i));}
   private static void writeSet(CompoundTag t,String k,Set<String> in){ListTag l=new ListTag();for(String s:in)l.add(StringTag.valueOf(s));t.put(k,l);}

   public synchronized boolean offer(long tick){if(!V13Chapter4Rules.LOCKED.equals(stage))return false;stage=V13Chapter4Rules.OFFERED;stageTick=tick;setDirty();return true;}
   public synchronized boolean accept(Set<String> players,long tick){if(!V13Chapter4Rules.OFFERED.equals(stage))return false;stage=V13Chapter4Rules.TESTIMONIES;stageTick=tick;participants.clear();participants.addAll(players);setDirty();return true;}
   public synchronized boolean testimony(int index,long tick){if(!V13Chapter4Rules.TESTIMONIES.equals(stage)||index<0||index>2||(testimonyMask&(1<<index))!=0)return false;testimonyMask|=1<<index;if(Integer.bitCount(testimonyMask)==3){stage=V13Chapter4Rules.SEEK_FRAGMENT;stageTick=tick;}setDirty();return true;}
   public synchronized void archive(BlockPos pos){archive=pos;siteBuilt=true;setDirty();}
   public synchronized boolean optional(int index){if(index<0||index>1||(optionalMask&(1<<index))!=0)return false;optionalMask|=1<<index;setDirty();return true;}
   public synchronized boolean claim(long tick){if(!V13Chapter4Rules.SEEK_FRAGMENT.equals(stage)||fragmentClaimed)return false;fragmentClaimed=true;stage=V13Chapter4Rules.RETURN_GALLERY;stageTick=tick;setDirty();return true;}
   public synchronized boolean lectern(boolean left,String player,long tick){if(!(V13Chapter4Rules.RETURN_GALLERY.equals(stage)||V13Chapter4Rules.STRONG_CONCORDANCE.equals(stage)))return false;stage=V13Chapter4Rules.STRONG_CONCORDANCE;if(left){leftPlayer=player;leftTick=tick;}else{rightPlayer=player;rightTick=tick;}if(V13Chapter4Rules.concordanceComplete(leftPlayer,leftTick,rightPlayer,rightTick,participants.size())){stage=V13Chapter4Rules.MATRIX_READY;stageTick=tick;}setDirty();return true;}
   public synchronized boolean analyze(long tick){if(!V13Chapter4Rules.MATRIX_READY.equals(stage))return false;stage=V13Chapter4Rules.REVELATION;stageTick=tick;acknowledgements.clear();setDirty();return true;}
   public synchronized boolean acknowledge(String id,long tick){if(!V13Chapter4Rules.REVELATION.equals(stage)||!participants.contains(id))return false;acknowledgements.add(id);if(acknowledgements.containsAll(participants)){stage=V13Chapter4Rules.COMPLETE;stageTick=tick;}setDirty();return true;}
   public synchronized boolean reward(){if(!V13Chapter4Rules.COMPLETE.equals(stage)||rewarded)return false;rewarded=true;setDirty();return true;}
   public synchronized void reset(){stage=V13Chapter4Rules.LOCKED;leftPlayer=rightPlayer="";stageTick=leftTick=rightTick=0;testimonyMask=optionalMask=0;archive=BlockPos.ZERO;siteBuilt=fragmentClaimed=rewarded=false;participants.clear();acknowledgements.clear();setDirty();}
   public synchronized Snapshot snapshot(){return new Snapshot(stage,stageTick,testimonyMask,optionalMask,archive,siteBuilt,fragmentClaimed,Set.copyOf(participants),Set.copyOf(acknowledgements),rewarded);}
   public record Snapshot(String stage,long stageTick,int testimonyMask,int optionalMask,BlockPos archive,boolean siteBuilt,boolean fragmentClaimed,Set<String> participants,Set<String> acknowledgements,boolean rewarded){public int testimonies(){return Integer.bitCount(testimonyMask);}public int optionals(){return Integer.bitCount(optionalMask);}}
}
