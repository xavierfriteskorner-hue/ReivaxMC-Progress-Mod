package fr.reivaxmc.progress.progression;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedData.Factory;

public final class CampaignSavedData extends SavedData {
   public static final String DATA_NAME = "reivaxmc_campaign";
   public static final int MAIN_TERRITORY_RADIUS = 96;
   private int progress;
   private int score;
   private String stage = "DORMANT";
   private boolean introRunning;
   private boolean introCompleted;
   private boolean vestigePlaced;
   private boolean reliquaryOpened;
   private boolean stelaPlaced;
   private boolean stelaDiscovered;
   private boolean fragmentFound;
   private boolean matrixDiscovered;
   private boolean matrixInstalled;
   private boolean foundationPlaced;
   private boolean migration;
   private boolean aftermathPlayed;
   private boolean nightSeen;
   private long introEndTick;
   private long introCompletedAt;
   private long foundationAt;
   private int vestigeX;
   private int vestigeY;
   private int vestigeZ;
   private int stelaX;
   private int stelaY;
   private int stelaZ;
   private int altarX;
   private int altarY;
   private int altarZ;
   private int matrixX;
   private int matrixY;
   private int matrixZ;
   private int foundationX;
   private int foundationY;
   private int foundationZ;
   private int settlementIndex;
   private String foundationDimension = "minecraft:overworld";
   private String foundationName = "";
   private String foundationFounder = "";
   private String foundationFounderUuid = "";
   private int foundationDay;
   private final Set<String> done = new HashSet<>();
   private final Set<String> bookRecipients = new HashSet<>();
   private final List<String> timeline = new ArrayList<>();
   private final List<CampaignSavedData.HistoricalSite> historicalSites = new ArrayList<>();
   private final List<CampaignSavedData.ArtifactRecord> artifacts = new ArrayList<>();

   public static CampaignSavedData create() {
      return new CampaignSavedData();
   }

   public static CampaignSavedData get(MinecraftServer s) {
      return (CampaignSavedData)s.overworld()
         .getDataStorage()
         .computeIfAbsent(new Factory(CampaignSavedData::create, CampaignSavedData::load, null), "reivaxmc_campaign");
   }

   public static CampaignSavedData load(CompoundTag t, Provider p) {
      CampaignSavedData d = new CampaignSavedData();
      d.progress = t.getInt("Progress");
      d.score = t.getInt("Score");
      d.stage = t.getString("Stage");
      d.introRunning = t.getBoolean("IntroRunning");
      d.introCompleted = t.getBoolean("IntroCompleted");
      d.vestigePlaced = t.getBoolean("VestigePlaced");
      d.reliquaryOpened = t.getBoolean("ReliquaryOpened");
      d.stelaPlaced = t.getBoolean("StelaPlaced");
      d.stelaDiscovered = t.getBoolean("StelaDiscovered");
      d.fragmentFound = t.getBoolean("FragmentFound");
      d.matrixDiscovered = t.getBoolean("MatrixDiscovered");
      d.matrixInstalled = t.getBoolean("MatrixInstalled");
      d.foundationPlaced = t.getBoolean("FoundationPlaced");
      d.migration = t.getBoolean("Migration");
      d.aftermathPlayed = t.getBoolean("AftermathPlayed");
      d.nightSeen = t.getBoolean("NightSeen");
      d.introEndTick = t.getLong("IntroEndTick");
      d.introCompletedAt = t.getLong("IntroCompletedAt");
      d.foundationAt = t.getLong("FoundationAt");
      d.vestigeX = t.getInt("VestigeX");
      d.vestigeY = t.getInt("VestigeY");
      d.vestigeZ = t.getInt("VestigeZ");
      d.stelaX = t.getInt("StelaX");
      d.stelaY = t.getInt("StelaY");
      d.stelaZ = t.getInt("StelaZ");
      d.altarX = t.getInt("AltarX");
      d.altarY = t.getInt("AltarY");
      d.altarZ = t.getInt("AltarZ");
      d.matrixX = t.getInt("MatrixX");
      d.matrixY = t.getInt("MatrixY");
      d.matrixZ = t.getInt("MatrixZ");
      d.foundationX = t.getInt("FoundationX");
      d.foundationY = t.getInt("FoundationY");
      d.foundationZ = t.getInt("FoundationZ");
      d.settlementIndex = t.getInt("SettlementIndex");
      d.foundationDimension = t.getString("FoundationDimension");
      d.foundationName = t.getString("FoundationName");
      d.foundationFounder = t.getString("FoundationFounder");
      d.foundationFounderUuid = t.getString("FoundationFounderUuid");
      d.foundationDay = t.getInt("FoundationDay");
      readStrings(t, "Done", d.done);
      readStrings(t, "BookRecipients", d.bookRecipients);
      readStrings(t, "Timeline", d.timeline);
      ListTag hs = t.getList("HistoricalSites", 10);

      for (int i = 0; i < hs.size(); i++) {
         CompoundTag h = hs.getCompound(i);
         d.historicalSites
            .add(
               new CampaignSavedData.HistoricalSite(
                  h.getString("Name"),
                  h.getString("Dim"),
                  new BlockPos(h.getInt("X"), h.getInt("Y"), h.getInt("Z")),
                  h.getInt("Radius"),
                  h.getString("Founder"),
                  h.getString("Uuid"),
                  h.getInt("Founded"),
                  h.getInt("Abandoned"),
                  h.getInt("Index")
               )
            );
      }

      ListTag as = t.getList("Artifacts", 10);

      for (int i = 0; i < as.size(); i++) {
         CompoundTag a = as.getCompound(i);
         d.artifacts
            .add(
               new CampaignSavedData.ArtifactRecord(
                  a.getString("Id"),
                  a.getString("Name"),
                  a.getString("Who"),
                  a.getString("Uuid"),
                  a.getInt("Day"),
                  a.getString("Place"),
                  a.getString("Meaning")
               )
            );
      }

      return d;
   }

   public CompoundTag save(CompoundTag t, Provider p) {
      t.putInt("Progress", this.progress);
      t.putInt("Score", this.score);
      t.putString("Stage", this.stage);
      t.putBoolean("IntroRunning", this.introRunning);
      t.putBoolean("IntroCompleted", this.introCompleted);
      t.putBoolean("VestigePlaced", this.vestigePlaced);
      t.putBoolean("ReliquaryOpened", this.reliquaryOpened);
      t.putBoolean("StelaPlaced", this.stelaPlaced);
      t.putBoolean("StelaDiscovered", this.stelaDiscovered);
      t.putBoolean("FragmentFound", this.fragmentFound);
      t.putBoolean("MatrixDiscovered", this.matrixDiscovered);
      t.putBoolean("MatrixInstalled", this.matrixInstalled);
      t.putBoolean("FoundationPlaced", this.foundationPlaced);
      t.putBoolean("Migration", this.migration);
      t.putBoolean("AftermathPlayed", this.aftermathPlayed);
      t.putBoolean("NightSeen", this.nightSeen);
      t.putLong("IntroEndTick", this.introEndTick);
      t.putLong("IntroCompletedAt", this.introCompletedAt);
      t.putLong("FoundationAt", this.foundationAt);
      t.putInt("VestigeX", this.vestigeX);
      t.putInt("VestigeY", this.vestigeY);
      t.putInt("VestigeZ", this.vestigeZ);
      t.putInt("StelaX", this.stelaX);
      t.putInt("StelaY", this.stelaY);
      t.putInt("StelaZ", this.stelaZ);
      t.putInt("AltarX", this.altarX);
      t.putInt("AltarY", this.altarY);
      t.putInt("AltarZ", this.altarZ);
      t.putInt("MatrixX", this.matrixX);
      t.putInt("MatrixY", this.matrixY);
      t.putInt("MatrixZ", this.matrixZ);
      t.putInt("FoundationX", this.foundationX);
      t.putInt("FoundationY", this.foundationY);
      t.putInt("FoundationZ", this.foundationZ);
      t.putInt("SettlementIndex", this.settlementIndex);
      t.putString("FoundationDimension", this.foundationDimension);
      t.putString("FoundationName", this.foundationName);
      t.putString("FoundationFounder", this.foundationFounder);
      t.putString("FoundationFounderUuid", this.foundationFounderUuid);
      t.putInt("FoundationDay", this.foundationDay);
      t.put("Done", writeStrings(this.done));
      t.put("BookRecipients", writeStrings(this.bookRecipients));
      t.put("Timeline", writeStrings(this.timeline));
      ListTag hs = new ListTag();

      for (CampaignSavedData.HistoricalSite h : this.historicalSites) {
         CompoundTag q = new CompoundTag();
         q.putString("Name", h.name);
         q.putString("Dim", h.dimension);
         q.putInt("X", h.pos.getX());
         q.putInt("Y", h.pos.getY());
         q.putInt("Z", h.pos.getZ());
         q.putInt("Radius", h.radius);
         q.putString("Founder", h.founder);
         q.putString("Uuid", h.founderUuid);
         q.putInt("Founded", h.foundedDay);
         q.putInt("Abandoned", h.abandonedDay);
         q.putInt("Index", h.index);
         hs.add(q);
      }

      t.put("HistoricalSites", hs);
      ListTag as = new ListTag();

      for (CampaignSavedData.ArtifactRecord a : this.artifacts) {
         CompoundTag q = new CompoundTag();
         q.putString("Id", a.id);
         q.putString("Name", a.name);
         q.putString("Who", a.discoverer);
         q.putString("Uuid", a.uuid);
         q.putInt("Day", a.day);
         q.putString("Place", a.place);
         q.putString("Meaning", a.meaning);
         as.add(q);
      }

      t.put("Artifacts", as);
      return t;
   }

   private static void readStrings(CompoundTag t, String k, Collection<String> o) {
      ListTag l = t.getList(k, 8);

      for (int i = 0; i < l.size(); i++) {
         o.add(l.getString(i));
      }
   }

   private static ListTag writeStrings(Collection<String> c) {
      ListTag l = new ListTag();

      for (String s : c) {
         l.add(StringTag.valueOf(s));
      }

      return l;
   }

   public boolean complete(String id, int pts, int civ) {
      if (!this.done.add(id)) {
         return false;
      } else {
         this.progress += pts;
         this.score += civ;
         this.setDirty();
         return true;
      }
   }

   public boolean isCompleted(String id) {
      return this.done.contains(id);
   }

   public int progress() {
      return this.progress;
   }

   public int score() {
      return this.score;
   }

   public String stage() {
      return this.stage;
   }

   public void stage(String s) {
      this.stage = s;
      this.setDirty();
   }

   public boolean introRunning() {
      return this.introRunning;
   }

   public boolean introCompleted() {
      return this.introCompleted;
   }

   public void startIntro(long end) {
      this.introRunning = true;
      this.introEndTick = end;
      this.setDirty();
   }

   public long introEndTick() {
      return this.introEndTick;
   }

   public void finishIntro(long t) {
      this.introRunning = false;
      this.introCompleted = true;
      this.introCompletedAt = t;
      this.stage = "SURVIVE_FIRST_NIGHT";
      this.complete("AWAKENING", 40, 20);
      this.setDirty();
   }

   public long introCompletedAt() {
      return this.introCompletedAt;
   }

   public boolean vestigePlaced() {
      return this.vestigePlaced;
   }

   public BlockPos vestigePos() {
      return new BlockPos(this.vestigeX, this.vestigeY, this.vestigeZ);
   }

   public void setVestige(BlockPos p) {
      this.vestigePlaced = true;
      this.vestigeX = p.getX();
      this.vestigeY = p.getY();
      this.vestigeZ = p.getZ();
      this.setDirty();
   }

   public boolean reliquaryOpened() {
      return this.reliquaryOpened;
   }

   public void openReliquary() {
      this.reliquaryOpened = true;
      this.setDirty();
   }

   public boolean stelaPlaced() {
      return this.stelaPlaced;
   }

   public BlockPos stelaPos() {
      return new BlockPos(this.stelaX, this.stelaY, this.stelaZ);
   }

   public void setStela(BlockPos p) {
      this.stelaPlaced = true;
      this.stelaX = p.getX();
      this.stelaY = p.getY();
      this.stelaZ = p.getZ();
      this.stage = "FOLLOW_RESONANCE";
      this.setDirty();
   }

   public boolean stelaDiscovered() {
      return this.stelaDiscovered;
   }

   public void discoverStela() {
      this.stelaDiscovered = true;
      this.stage = "SEARCH_STELE";
      this.setDirty();
   }

   public BlockPos altarPos() {
      return new BlockPos(this.altarX, this.altarY, this.altarZ);
   }

   public void setAltar(BlockPos p) {
      this.altarX = p.getX();
      this.altarY = p.getY();
      this.altarZ = p.getZ();
      this.setDirty();
   }

   public boolean fragmentFound() {
      return this.fragmentFound;
   }

   public void markFragmentFound() {
      this.fragmentFound = true;
      this.stage = "BUILD_FIRST_HOME";
      this.setDirty();
   }

   public void setMatrixPos(BlockPos p) {
      this.matrixX = p.getX();
      this.matrixY = p.getY();
      this.matrixZ = p.getZ();
      this.setDirty();
   }

   public boolean matrixDiscovered() {
      return this.matrixDiscovered;
   }

   public BlockPos matrixPos() {
      return new BlockPos(this.matrixX, this.matrixY, this.matrixZ);
   }

   public void discoverMatrix() {
      this.matrixDiscovered = true;
      this.stage = "BUILD_FIRST_HOME";
      this.setDirty();
   }

   public boolean matrixInstalled() {
      return this.matrixInstalled;
   }

   public void matrixInstalled(BlockPos p) {
      this.matrixInstalled = true;
      this.matrixX = p.getX();
      this.matrixY = p.getY();
      this.matrixZ = p.getZ();
      this.setDirty();
   }

   public boolean nightSeen() {
      return this.nightSeen;
   }

   public void markNightSeen() {
      this.nightSeen = true;
      this.setDirty();
   }

   public boolean foundationPlaced() {
      return this.foundationPlaced;
   }

   public boolean migration() {
      return this.migration;
   }

   public BlockPos foundationPos() {
      return new BlockPos(this.foundationX, this.foundationY, this.foundationZ);
   }

   public String foundationDimension() {
      return this.foundationDimension;
   }

   public String foundationName() {
      return this.foundationName;
   }

   public int territoryRadius() {
      return 96;
   }

   public void foundSettlement(BlockPos p, String dim, String who, UUID uuid, int day, long time) {
      this.settlementIndex++;
      this.foundationPlaced = true;
      this.migration = false;
      this.foundationX = p.getX();
      this.foundationY = p.getY();
      this.foundationZ = p.getZ();
      this.foundationDimension = dim;
      this.foundationFounder = who;
      this.foundationFounderUuid = uuid.toString();
      this.foundationDay = day;
      this.foundationAt = time;
      this.foundationName = this.settlementIndex == 1 ? "Premier Foyer" : "Foyer principal #" + this.settlementIndex;
      this.stage = "AFTER_FOUNDATION";
      this.complete("FIRST_HOME", 90, 50);
      this.setDirty();
   }

   public CampaignSavedData.HistoricalSite abandonSettlement(int day) {
      CampaignSavedData.HistoricalSite h = new CampaignSavedData.HistoricalSite(
         this.foundationName,
         this.foundationDimension,
         this.foundationPos(),
         96,
         this.foundationFounder,
         this.foundationFounderUuid,
         this.foundationDay,
         day,
         this.settlementIndex
      );
      this.historicalSites.add(h);
      this.foundationPlaced = false;
      this.migration = true;
      this.foundationName = "";
      this.stage = "MIGRATION";
      this.setDirty();
      return h;
   }

   private static long d2(int x, int z, int a, int b) {
      long dx = (long)x - (long)a;
      long dz = (long)z - (long)b;
      return dx * dx + dz * dz;
   }

   public boolean isInsideHistorical(String dim, BlockPos p) {
      for (CampaignSavedData.HistoricalSite h : this.historicalSites) {
         if (h.dimension.equals(dim) && d2(p.getX(), p.getZ(), h.pos.getX(), h.pos.getZ()) <= (long)h.radius * (long)h.radius) {
            return true;
         }
      }

      return false;
   }

   public boolean isInsideMainTerritory(String dim, BlockPos p) {
      return this.foundationPlaced && this.foundationDimension.equals(dim) && d2(p.getX(), p.getZ(), this.foundationX, this.foundationZ) <= 9216L;
   }

   public List<CampaignSavedData.HistoricalSite> historicalSites() {
      return Collections.unmodifiableList(this.historicalSites);
   }

   public List<CampaignSavedData.ArtifactRecord> artifacts() {
      return Collections.unmodifiableList(this.artifacts);
   }

   public boolean hasArtifact(String id) {
      return this.artifacts.stream().anyMatch(a -> a.id.equals(id));
   }

   public void addArtifact(CampaignSavedData.ArtifactRecord a) {
      if (!this.hasArtifact(a.id)) {
         this.artifacts.add(a);
         this.setDirty();
      }
   }

   public boolean aftermathPlayed() {
      return this.aftermathPlayed;
   }

   public void markAftermathPlayed() {
      this.aftermathPlayed = true;
      this.setDirty();
   }

   public long foundationAt() {
      return this.foundationAt;
   }

   public boolean hasBook(UUID u) {
      return this.bookRecipients.contains(u.toString());
   }

   public void markBook(UUID u) {
      this.bookRecipients.add(u.toString());
      this.setDirty();
   }

   public void addTimeline(int day, String who, String title, String detail) {
      this.timeline.add(0, day + "¦" + who + "¦" + title + "¦" + detail);

      while (this.timeline.size() > 80) {
         this.timeline.remove(this.timeline.size() - 1);
      }

      this.setDirty();
   }

   public String timelinePacket() {
      return String.join("\n", this.timeline);
   }

   public String artifactsPacket() {
      StringBuilder b = new StringBuilder();

      for (CampaignSavedData.ArtifactRecord a : this.artifacts) {
         if (b.length() > 0) {
            b.append('\n');
         }

         b.append(a.id)
            .append('¦')
            .append(a.name)
            .append('¦')
            .append(a.discoverer)
            .append('¦')
            .append(a.day)
            .append('¦')
            .append(a.place)
            .append('¦')
            .append(a.meaning);
      }

      return b.toString();
   }

   public static record ArtifactRecord(String id, String name, String discoverer, String uuid, int day, String place, String meaning) {
   }

   public static record HistoricalSite(
      String name, String dimension, BlockPos pos, int radius, String founder, String founderUuid, int foundedDay, int abandonedDay, int index
   ) {
   }
}
