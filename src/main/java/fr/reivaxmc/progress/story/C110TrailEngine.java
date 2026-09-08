package fr.reivaxmc.progress.story;

import fr.reivaxmc.progress.ReivaxMCProgress;
import fr.reivaxmc.progress.entity.VeilleurEntity;
import fr.reivaxmc.progress.network.ProgressNetworking;
import fr.reivaxmc.progress.progression.CampaignSavedData;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.RightClickItem;
import net.neoforged.neoforge.event.tick.PlayerTickEvent.Post;

/**
 * Chapitre II — La Dette du Foyer.
 * Une Piste complète, facultative au départ, partagée par le monde et indépendante des autres mods.
 */
public final class C110TrailEngine {
   private static final String WATCHER_PREFIX = "reivax_ch2_watcher_";
   private static final String MAIN_REWARD = "CH2_DEBT_COMPLETE";
   private static final String OPTIONAL_REWARD = "CH2_THIRD_RIFT";

   private C110TrailEngine() {
   }

   public static void onLogin(PlayerLoggedInEvent event) {
      if (!(event.getEntity() instanceof ServerPlayer player)) return;
      repairSanctuary(player);
      migrateLegacySites(player);
      offerIfReady(player);
      restore(player);
   }

   public static void onPlayerTick(Post event) {
      if (!(event.getEntity() instanceof ServerPlayer player) || player.tickCount % 20 != 0) return;
      offerIfReady(player);
      MinecraftServer server = player.getServer();
      if (server == null) return;
      C110TrailData data = C110TrailData.get(server);
      C110TrailData.Snapshot state = data.snapshot();

      repairSanctuary(player);
      if (state.placedMask() != 0 && state.siteLayoutVersion() < 2)
         ensureSites(player.serverLevel(), CampaignSavedData.get(server), data);

      if (isRunning(state.stage())) ensureSites(player.serverLevel(), CampaignSavedData.get(server), data);
      if (C110TrailRules.RETURN_FOYER.equals(state.stage()) && atFoyer(player, 11.0)) beginCensus(player, data);
      if (C110TrailRules.CENSUS.equals(state.stage())) {
         ensureCensus(player.serverLevel(), CampaignSavedData.get(server), data);
         observeNearbyWatcher(player, data);
         guideToNearestWatcher(player, data);
      }
      if (C110TrailRules.RETURN_SANCTUARY.equals(state.stage())) tryConcordance(player, data);
      if (C110TrailRules.REGISTRY.equals(state.stage()) && player.blockPosition().closerThan(C110SanctuaryArchitecture.registryConsole(server), 8.0))
         player.displayClientMessage(Component.literal("§bREGISTRE DES ABSENTS §8• §fCliquez la console lumineuse pour inscrire la Concordance."), true);
      if (state.completed()) grantPersonalReward(player, data);
      applyDoctrine(player, data);
   }

   /** @return true si le chapitre a consommé le clic. */
   public static boolean onRightClickBlock(RightClickBlock event) {
      if (!(event.getEntity() instanceof ServerPlayer player) || !(event.getLevel() instanceof ServerLevel level)) return false;
      MinecraftServer server = player.getServer();
      if (server == null) return false;
      C110TrailData data = C110TrailData.get(server);
      C110TrailData.Snapshot state = data.snapshot();
      BlockPos pos = event.getPos();

      CampaignSavedData campaign = CampaignSavedData.get(server);
      if (!level.dimension().location().toString().equals(campaign.foundationDimension())) return false;

      for (int i = 0; i < 3; i++) {
         if ((state.placedMask() & (1 << i)) != 0 && pos.closerThan(state.sites()[i], i == 0 ? 4.5 : 3.5)) {
            consume(event);
            discoverRift(player, data, i);
            return true;
         }
      }

      if (C110TrailRules.RETURN_SANCTUARY.equals(state.stage()) && F8SanctuaryEngine.isReliquaryPos(server, pos) && !carriesFragment(player)) {
         consume(event);
         give(player, new ItemStack((Item)ReivaxMCProgress.UNKNOWN_FRAGMENT.get()));
         player.displayClientMessage(Component.literal("§3RELIQUAIRE §8• §fLe Fragment perdu revient à son porteur. Il n'a toujours pas été analysé."), false);
         return true;
      }

      if (C110TrailRules.REGISTRY.equals(state.stage()) && C110SanctuaryArchitecture.isRegistryConsole(server, pos)) {
         consume(event);
         completeAtRegistry(player, data);
         return true;
      }
      return false;
   }

   public static void onRightClickItem(RightClickItem event) {
      if (!(event.getEntity() instanceof ServerPlayer player) || !event.getItemStack().is((Item)ReivaxMCProgress.RESONANCE_COMPASS.get())) return;
      event.setCanceled(true);
      event.setCancellationResult(InteractionResult.SUCCESS);
      useCompass(player);
   }

   /** Appelé par l'onglet PISTES : suivre ne coûte rien et n'empêche aucune autre activité. */
   public static void follow(ServerPlayer player) {
      MinecraftServer server = player.getServer();
      if (server == null) return;
      CampaignSavedData campaign = CampaignSavedData.get(server);
      C110TrailData data = C110TrailData.get(server);
      if (!atFoyer(player, 12.0)) {
         player.displayClientMessage(Component.literal("§6PISTES §8• §fRevenez près de la Borne pour inscrire cette piste au Foyer."), false);
         return;
      }
      if (!data.accept(player.getGameProfile().getName(), player.serverLevel().getGameTime())) {
         ProgressNetworking.openFoyerPanel(player);
         return;
      }
      ensureSites(player.serverLevel(), campaign, data);
      campaign.stage("CH2_SEEK_RIFTS");
      campaign.addTimeline(ProgressNetworking.day(server), player.getGameProfile().getName(), "La Dette du Foyer",
         "Le Foyer a accepté de regarder au-delà de ses propres limites.");
      broadcast(server, campaign, "CH2_TRAIL_FOLLOWED", "PISTE SUIVIE — LA DETTE DU FOYER",
         "Trois Fêlures répondent autour du Foyer. Deux suffiront pour comprendre leur appel ; la troisième restera votre choix.", 0);
      sync(server);
   }

   public static String packet(C110TrailData.Snapshot state) {
      return state.stage() + "~" + C110TrailRules.title(state.stage()) + "~"
         + C110TrailRules.objective(state.stage(), state.discoveredCount(), state.witnessCount()) + "~"
         + state.discoveredCount() + "~" + state.witnessCount() + "~" + (state.completed() ? "1" : "0");
   }

   public static int devCommand(ServerPlayer player, String action) {
      MinecraftServer server = player.getServer();
      if (server == null) return 0;
      C110TrailData data = C110TrailData.get(server);
      CampaignSavedData campaign = CampaignSavedData.get(server);
      switch (action) {
         case "start" -> {
            clearWatchers(server);
            data.reset();
            data.offer(player.serverLevel().getGameTime());
            try { int[] o = F8SanctuaryEngine.target(server); C110SanctuaryArchitecture.build(server, o); } catch (Throwable ignored) {}
            if (campaign.foundationPlaced()) {
               player.teleportTo(player.serverLevel(), campaign.foundationPos().getX() + 2.5, campaign.foundationPos().getY() + 1.0,
                  campaign.foundationPos().getZ() + 2.5, player.getYRot(), player.getXRot());
               ProgressNetworking.openFoyerPanel(player);
               player.displayClientMessage(Component.literal("§6CHAPITRE II DEV §8• §aPiste prête dans la Borne, onglet PISTES."), false);
            } else {
               player.displayClientMessage(Component.literal("§cCHAPITRE II DEV §8• §fAucun Foyer : terminez d'abord /reivax dev chapter1 start."), false);
            }
         }
         case "next" -> devNext(player, data);
         case "status" -> sendStatus(player, data.snapshot());
         case "reset" -> {
            clearWatchers(server); data.reset(); campaign.stage("CH1_COMPLETE");
            player.displayClientMessage(Component.literal("§6CHAPITRE II DEV §8• §aRéinitialisé."), false);
         }
         default -> player.displayClientMessage(Component.literal("§6CHAPITRE II DEV §8• §fstart · next · status · reset"), false);
      }
      sync(server);
      return 1;
   }

   private static void offerIfReady(ServerPlayer player) {
      MinecraftServer server = player.getServer();
      if (server == null || !F91FoyerChapterData.get(server).snapshot().completed()) return;
      C110TrailData data = C110TrailData.get(server);
      if (!data.offer(player.serverLevel().getGameTime())) return;
      CampaignSavedData campaign = CampaignSavedData.get(server);
      campaign.stage("CH2_OFFERED");
      player.displayClientMessage(Component.literal("§6NOUVELLE PISTE §8• §fLa Dette du Foyer §8— §7consultez l'onglet PISTES de la Borne quand vous le souhaiterez."), false);
      ProgressNetworking.syncAll(server, campaign);
   }

   private static void restore(ServerPlayer player) {
      MinecraftServer server = player.getServer();
      if (server == null) return;
      C110TrailData.Snapshot state = C110TrailData.get(server).snapshot();
      if (isRunning(state.stage())) player.displayClientMessage(Component.literal("§6PISTE SUIVIE §8• §f" + C110TrailRules.title(state.stage())
         + " §8— §7" + C110TrailRules.objective(state.stage(), state.discoveredCount(), state.witnessCount())), false);
      if (state.completed()) grantPersonalReward(player, C110TrailData.get(server));
   }

   private static void ensureSites(ServerLevel level, CampaignSavedData campaign, C110TrailData data) {
      if (!campaign.foundationPlaced() || !level.dimension().location().toString().equals(campaign.foundationDimension())) return;
      C110TrailData.Snapshot state = data.snapshot();
      // Les Fêlures ne surgissent plus pratiquement sous la Borne : elles forment
      // une vraie courte exploration, dans trois directions très distinctes.
      int[][] offsets = {{142, 61}, {-176, 83}, {74, -214}};
      if (state.placedMask() != 0 && state.siteLayoutVersion() < 2) {
         BlockPos[] moved = state.sites().clone();
         for (int i = 0; i < 3; i++) {
            if ((state.placedMask() & (1 << i)) == 0) continue;
            cleanLegacySite(level, state.sites()[i], i);
            BlockPos base = campaign.foundationPos().offset(offsets[i][0], 0, offsets[i][1]);
            int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, base.getX(), base.getZ());
            moved[i] = new BlockPos(base.getX(), y, base.getZ());
            buildSite(level, moved[i], i);
         }
         data.migrateSites(moved, state.placedMask(), 2);
         state = data.snapshot();
      }
      for (int i = 0; i < 3; i++) {
         if ((state.placedMask() & (1 << i)) != 0) continue;
         BlockPos base = campaign.foundationPos().offset(offsets[i][0], 0, offsets[i][1]);
         int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, base.getX(), base.getZ());
         BlockPos site = new BlockPos(base.getX(), y, base.getZ());
         buildSite(level, site, i);
         data.placeSite(i, site);
      }
      data.siteLayoutVersion(2);
   }

   private static void migrateLegacySites(ServerPlayer player) {
      MinecraftServer server = player.getServer();
      if (server == null) return;
      C110TrailData data = C110TrailData.get(server);
      C110TrailData.Snapshot state = data.snapshot();
      if (state.placedMask() != 0 && state.siteLayoutVersion() < 2)
         ensureSites(player.serverLevel(), CampaignSavedData.get(server), data);
   }

   private static void buildSite(ServerLevel level, BlockPos site, int index) {
      if (index == 0) buildEchoStone(level, site);
      else if (index == 1) buildDoorlessThreshold(level, site);
      else buildTableOfAbsent(level, site);
   }

   /** Retire uniquement les blocs caractéristiques posés par l'ancienne Fêlure. */
   private static void cleanLegacySite(ServerLevel level, BlockPos p, int index) {
      int radius = index == 0 ? 3 : 2;
      int maxY = index == 0 ? 4 : 5;
      int depth = index == 2 ? 2 : 0;
      for (int x = -radius; x <= radius; x++) for (int y = -1; y <= maxY; y++) for (int z = -depth; z <= depth; z++) {
         BlockPos q = p.offset(x, y, z);
         Block block = level.getBlockState(q).getBlock();
         if (block == ReivaxMCProgress.ECHO_STONE.get() || block == ReivaxMCProgress.SANCTUARY_STONE.get()
            || block == ReivaxMCProgress.SANCTUARY_LUMEN.get() || block == Blocks.CHISELED_DEEPSLATE
            || block == Blocks.GILDED_BLACKSTONE || block == Blocks.POLISHED_BLACKSTONE_BRICKS
            || block == Blocks.DARK_OAK_SLAB || block == Blocks.DARK_OAK_FENCE
            || block == Blocks.DARK_OAK_STAIRS || block == Blocks.SOUL_LANTERN)
            level.setBlock(q, Blocks.AIR.defaultBlockState(), 18);
      }
   }

   private static void buildEchoStone(ServerLevel level, BlockPos p) {
      Block echo = (Block)ReivaxMCProgress.ECHO_STONE.get();
      for (int y=0;y<7;y++) {
         int width=(y==0||y==6)?1:2;
         for(int x=-width;x<=width;x++) level.setBlock(p.offset(x,y,0),(x==0&&y>=1&&y<=5?echo:Blocks.CHISELED_DEEPSLATE).defaultBlockState(),18);
      }
      for(int z=-2;z<=2;z++) for(int x=-4;x<=4;x++) if(Math.abs(x)+Math.abs(z)<=5)
         level.setBlock(p.offset(x,-1,z),(Math.abs(x)==3?Blocks.GILDED_BLACKSTONE:Blocks.POLISHED_BLACKSTONE_BRICKS).defaultBlockState(),18);
      for(int side:new int[]{-1,1}) {
         level.setBlock(p.offset(side*4,0,0),Blocks.CHISELED_POLISHED_BLACKSTONE.defaultBlockState(),18);
         level.setBlock(p.offset(side*4,1,0),Blocks.SOUL_LANTERN.defaultBlockState(),18);
      }
   }

   private static void buildDoorlessThreshold(ServerLevel level, BlockPos p) {
      for (int side : new int[]{-3,3}) for (int y=0;y<=7;y++) level.setBlock(p.offset(side,y,0),
         (y==3?(Block)ReivaxMCProgress.SANCTUARY_LUMEN.get():(Block)ReivaxMCProgress.SANCTUARY_STONE.get()).defaultBlockState(),18);
      for(int x=-3;x<=3;x++) level.setBlock(p.offset(x,7,0),((Block)ReivaxMCProgress.SANCTUARY_STONE.get()).defaultBlockState(),18);
      for(int x=-4;x<=4;x++) level.setBlock(p.offset(x,-1,0),Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState(),18);
      level.setBlock(p,((Block)ReivaxMCProgress.ECHO_STONE.get()).defaultBlockState(),18);
   }

   private static void buildTableOfAbsent(ServerLevel level, BlockPos p) {
      level.setBlock(p,((Block)ReivaxMCProgress.ECHO_STONE.get()).defaultBlockState(),18);
      for(int x=-3;x<=3;x++) level.setBlock(p.offset(x,1,0),Blocks.DARK_OAK_SLAB.defaultBlockState(),18);
      level.setBlock(p.offset(-3,0,0),Blocks.DARK_OAK_FENCE.defaultBlockState(),18);
      level.setBlock(p.offset(3,0,0),Blocks.DARK_OAK_FENCE.defaultBlockState(),18);
      for(int z:new int[]{-3,3}) for(int x=-3;x<=3;x+=2) level.setBlock(p.offset(x,0,z),Blocks.DARK_OAK_STAIRS.defaultBlockState(),18);
      level.setBlock(p.offset(0,2,0),Blocks.SOUL_LANTERN.defaultBlockState(),18);
   }

   private static void discoverRift(ServerPlayer player, C110TrailData data, int index) {
      C110TrailData.Snapshot before = data.snapshot();
      if (!data.discover(index, player.getGameProfile().getName(), player.serverLevel().getGameTime())) {
         player.displayClientMessage(Component.literal("§6FÊLURE §8• §7Ce lieu vous a déjà répondu."), false);
         return;
      }
      MinecraftServer server = player.getServer();
      if (server == null) return;
      CampaignSavedData campaign = CampaignSavedData.get(server);
      String[] titles = {"LA PIERRE DE L'ÉCHO", "LE SEUIL SANS PORTE", "LA TABLE DES ABSENTS"};
      String[] texts = {
         "Ses inscriptions s'allument avant votre main. Elles ne racontent pas le passé : elles vérifient que vous en avez un.",
         "Vous franchissez une frontière bâtie autour d'une absence. De chaque côté, le Foyer semble être ailleurs.",
         "Huit places. Sept noms effacés. La dernière ligne du registre n'a pas été écrite avec de l'encre."
      };
      int gained = index == 2 && before.discoveredCount() >= 2 ? 15 : 0;
      broadcast(server, campaign, "CH2_RIFT_" + (index + 1), titles[index], texts[index], gained);
      if (data.markOptionalRewarded() && campaign.complete(OPTIONAL_REWARD, 15, 5)) {
         campaign.addTimeline(ProgressNetworking.day(server), player.getGameProfile().getName(), "Les trois Fêlures",
            "Aucune trace n'a été laissée de côté, même lorsque l'histoire n'en exigeait que deux.");
      }
      C110TrailData.Snapshot after = data.snapshot();
      campaign.stage(switch (after.stage()) {
         case C110TrailRules.RETURN_FOYER -> "CH2_RETURN_FOYER";
         default -> "CH2_SEEK_RIFTS";
      });
      if (C110TrailRules.RETURN_FOYER.equals(after.stage())) player.displayClientMessage(Component.literal("§6PISTE §8• §fDeux Fêlures concordent. Revenez à la Borne."), false);
      sync(server);
   }

   private static void beginCensus(ServerPlayer player, C110TrailData data) {
      MinecraftServer server = player.getServer();
      if (server == null || !data.beginCensus(player.getGameProfile().getName(), player.serverLevel().getGameTime())) return;
      CampaignSavedData campaign = CampaignSavedData.get(server);
      campaign.stage("CH2_CENSUS");
      ensureCensus(player.serverLevel(), campaign, data);
      broadcast(server, campaign, "CH2_CENSUS_START", "LE RECENSEMENT",
         "Sept silhouettes apparaissent au-delà de la lisière. Une huitième se tient déjà du mauvais côté. Elles n'attaquent pas. Approchez-en trois.", 0);
      sync(server);
   }

   private static void ensureCensus(ServerLevel level, CampaignSavedData campaign, C110TrailData data) {
      if (!campaign.foundationPlaced()) return;
      BlockPos home = campaign.foundationPos();
      int r = campaign.territoryRadius() + 10;
      C110TrailData.Snapshot snapshot=data.snapshot();
      for (int i = 0; i < 8; i++) {
         if((snapshot.witnessMask()&(1<<i))!=0) continue;
         double angle = Math.PI * 2.0 * i / 7.0;
         int distance = i == 7 ? 17 : r;
         int x = home.getX() + (int)Math.round(Math.cos(angle) * distance);
         int z = home.getZ() + (int)Math.round(Math.sin(angle) * distance);
         int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
         BlockPos pos=new BlockPos(x,y,z);
         AABB area=new AABB(pos).inflate(4);
         int index=i;
         boolean exists=!level.getEntities((Entity)null,area,e->e.getTags().contains(WATCHER_PREFIX+index)).isEmpty();
         if(!exists) spawnWatcher(level,i,pos);
      }
      if(!snapshot.censusSpawned()) data.censusSpawned();
   }

   private static void spawnWatcher(ServerLevel level, int index, BlockPos pos) {
      VeilleurEntity watcher = (VeilleurEntity)ReivaxMCProgress.VEILLEUR.get().create(level);
      if (watcher == null) return;
      watcher.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, index * 51.0F, 0.0F);
      watcher.addTag(WATCHER_PREFIX + index);
      watcher.addTag("reivax_ch2_watcher");
      watcher.setNoAi(true);
      watcher.setInvulnerable(true);
      watcher.setSilent(true);
      watcher.setPersistenceRequired();
      level.addFreshEntity(watcher);
      level.sendParticles(ParticleTypes.SCULK_SOUL, pos.getX() + 0.5, pos.getY() + 1.2, pos.getZ() + 0.5, 18, 0.5, 1.0, 0.5, 0.02);
   }

   private static void observeNearbyWatcher(ServerPlayer player, C110TrailData data) {
      List<Entity> nearby = player.serverLevel().getEntities(player, player.getBoundingBox().inflate(5.5), e -> e.getTags().contains("reivax_ch2_watcher"));
      for (Entity entity : nearby) {
         for (int i = 0; i < 8; i++) if (entity.getTags().contains(WATCHER_PREFIX + i)) {
            if (!data.observeWitness(i, player.getGameProfile().getName(), player.serverLevel().getGameTime())) continue;
            player.serverLevel().sendParticles(ParticleTypes.REVERSE_PORTAL, entity.getX(), entity.getY() + 1.2, entity.getZ(), 32, 0.6, 1.1, 0.6, 0.04);
            C110TrailData.Snapshot state = data.snapshot();
            if (C110TrailRules.RETURN_SANCTUARY.equals(state.stage())) {
               MinecraftServer server = player.getServer();
               if (server == null) return;
               finishCensus(player, server);
            } else {
               player.displayClientMessage(Component.literal("§6VEILLEUR RECONNU §8• §f" + state.witnessCount() + "/3"), false);
            }
            return;
         }
      }
   }

   private static void guideToNearestWatcher(ServerPlayer player,C110TrailData data) {
      if(player.tickCount%100!=0) return;
      C110TrailData.Snapshot s=data.snapshot(); Entity nearest=null; double best=Double.MAX_VALUE;
      AABB search=new AABB(player.blockPosition()).inflate(180);
      for(Entity e:player.serverLevel().getEntities((Entity)null,search,e->e.getTags().contains("reivax_ch2_watcher"))) {
         double d=e.distanceToSqr(player); if(d<best){best=d;nearest=e;}
      }
      if(nearest==null) return;
      int dx=(int)Math.round(nearest.getX()-player.getX()), dz=(int)Math.round(nearest.getZ()-player.getZ());
      String dir=Math.abs(dx)>Math.abs(dz)?(dx>=0?"est":"ouest"):(dz>=0?"sud":"nord");
      int distance=(int)Math.sqrt(best);
      player.displayClientMessage(Component.literal("§6RECENSEMENT §8• §fProchain Veilleur : §e"+distance+" blocs vers le "+dir+" §8• §7"+s.witnessCount()+"/3"),true);
      double len=Math.max(1,Math.sqrt((double)dx*dx+(double)dz*dz));
      for(int i=2;i<=12;i+=2) player.serverLevel().sendParticles(ParticleTypes.SCULK_SOUL,player.getX()+dx/len*i,player.getY()+1.2,player.getZ()+dz/len*i,2,.15,.3,.15,.01);
   }

   private static void finishCensus(ServerPlayer player, MinecraftServer server) {
      clearWatchers(server);
      CampaignSavedData campaign = CampaignSavedData.get(server);
      campaign.stage("CH2_RETURN_SANCTUARY");
      broadcast(server, campaign, "CH2_CENSUS_END", "CEUX QUI MANQUENT",
         "Vous avez compté ceux que les murs ont laissés dehors. Vous n'avez jamais demandé qui manquait à l'intérieur.", 0);
      player.displayClientMessage(Component.literal("§6PISTE §8• §fGardez le Fragment sur vous et retournez au Sanctuaire."), false);
      sync(server);
   }

   private static void tryConcordance(ServerPlayer player, C110TrailData data) {
      MinecraftServer server = player.getServer();
      if (server == null || player.serverLevel() != server.overworld()) return;
      try {
         int[] origin = F8SanctuaryEngine.target(server);
         BlockPos threshold = C110SanctuaryArchitecture.eastThreshold(server);
         if (!player.blockPosition().closerThan(threshold, 8.0)) return;
         if (!carriesFragment(player)) {
            player.displayClientMessage(Component.literal("§3CONCORDANCE §8• §fLe Sanctuaire reconnaît votre vécu, mais le Fragment manque. Le Reliquaire peut le rappeler."), false);
            return;
         }
         if (!data.concord(player.getGameProfile().getName(), player.serverLevel().getGameTime())) return;
         C110SanctuaryArchitecture.openRegistry(server, origin);
         CampaignSavedData campaign = CampaignSavedData.get(server);
         campaign.stage("CH2_REGISTRY");
         broadcast(server, campaign, "CH2_CONCORDANCE", "PREMIÈRE CONCORDANCE",
            "Le Fragment, votre choix et ce que vous avez vécu résonnent ensemble. La Borne n'analyse rien : le Sanctuaire vient seulement d'ouvrir une mémoire.", 0);
         sync(server);
      } catch (Throwable error) {
         System.err.println("[REIVAX 0.11.0] concordance failed: " + error);
      }
   }

   private static void completeAtRegistry(ServerPlayer player, C110TrailData data) {
      MinecraftServer server = player.getServer();
      if (server == null || !data.openRegistry(player.getGameProfile().getName(), player.serverLevel().getGameTime())) return;
      CampaignSavedData campaign = CampaignSavedData.get(server);
      if (campaign.complete(MAIN_REWARD, 150, 32)) {
         campaign.addTimeline(ProgressNetworking.day(server), player.getGameProfile().getName(), "Le Registre des Absents",
            "La première salle condamnée du Sanctuaire a été rendue à la mémoire commune.");
      }
      campaign.stage("CH2_COMPLETE");
      broadcast(server, campaign, "CH2_COMPLETE", "LA DETTE DU FOYER",
         "Les noms avaient été remplacés par des nombres bien avant votre arrivée. Derrière le sceau occidental, la Matrice ouvre un œil — puis se tait.", 150);
      C110SanctuaryArchitecture.pulseMatrix(server);
      for (ServerPlayer member : server.getPlayerList().getPlayers()) grantPersonalReward(member, data);
      sync(server);
   }

   private static void grantPersonalReward(ServerPlayer player, C110TrailData data) {
      if (!data.claimReward(player.getUUID().toString())) return;
      give(player, new ItemStack((Item)ReivaxMCProgress.RESONANCE_COMPASS.get()));
      player.displayClientMessage(Component.literal("§bRÉCOMPENSE §8• §fBoussole de Résonance §7— clic droit : guider · accroupi + clic droit : changer de cible."), false);
   }

   private static void applyDoctrine(ServerPlayer player, C110TrailData data) {
      MinecraftServer server = player.getServer();
      if (server == null || player.tickCount % 200 != 0) return;
      F91FoyerChapterData.Snapshot chapter = F91FoyerChapterData.get(server).snapshot();
      CampaignSavedData campaign = CampaignSavedData.get(server);
      if (!chapter.completed() || !campaign.foundationPlaced() || !atFoyer(player, campaign.territoryRadius())) return;
      long day = player.serverLevel().getDayTime() / 24000L;
      if (F91ChapterRules.BASTION.equals(chapter.doctrine())) {
         List<Monster> threats = player.serverLevel().getEntitiesOfClass(Monster.class, player.getBoundingBox().inflate(36.0), Entity::isAlive);
         if (!threats.isEmpty() && data.useBastion(day)) {
            Monster first = threats.get(0);
            first.addEffect(new MobEffectInstance(MobEffects.GLOWING, 240, 0));
            first.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 160, 0));
            player.displayClientMessage(Component.literal("§6PROTECTION §8• §fLa lisière désigne la première menace du jour."), false);
         }
      } else if (F91ChapterRules.SOLIDARITY.equals(chapter.doctrine()) && player.getHealth() < player.getMaxHealth() * 0.5F && data.useSolidarity(day)) {
         for (ServerPlayer member : server.getPlayerList().getPlayers()) if (member.distanceToSqr(player) <= 256.0) member.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 120, 0));
         player.displayClientMessage(Component.literal("§6PARTAGE §8• §fLe Foyer refuse de vous laisser revenir seul et brisé."), false);
      }
   }

   private static void useCompass(ServerPlayer player) {
      MinecraftServer server = player.getServer();
      if (server == null) return;
      int mode = player.getPersistentData().getInt("ReivaxCompassMode");
      if (player.isShiftKeyDown()) {
         mode = (mode + 1) % 3;
         player.getPersistentData().putInt("ReivaxCompassMode", mode);
      }
      CampaignSavedData campaign = CampaignSavedData.get(server);
      C110TrailData.Snapshot trail = C110TrailData.get(server).snapshot();
      BlockPos target;
      String label;
      if (mode == 0) { target = campaign.foundationPos(); label = "FOYER"; }
      else if (mode == 1) {
         try { int[] o = F8SanctuaryEngine.target(server); target = new BlockPos(o[0], o[1], o[2]); }
         catch (Throwable ignored) { target = campaign.foundationPos(); }
         label = "SANCTUAIRE";
      } else {
         V12TrailData.Snapshot chapter3=V12TrailData.get(server).snapshot();
         target = (!V12TrailRules.LOCKED.equals(chapter3.stage())&&!V12TrailRules.OFFERED.equals(chapter3.stage())&&!V12TrailRules.COMPLETE.equals(chapter3.stage()))
            ? V12Chapter3Engine.currentTarget(server) : objectiveTarget(server, campaign, trail);
         label = "PISTE ACTUELLE";
      }
      int dx = target.getX() - player.getBlockX();
      int dz = target.getZ() - player.getBlockZ();
      int distance = (int)Math.sqrt((double)dx * dx + (double)dz * dz);
      String direction = Math.abs(dx) > Math.abs(dz) ? (dx >= 0 ? "est" : "ouest") : (dz >= 0 ? "sud" : "nord");
      boolean exact = F91ChapterRules.MEMORY.equals(F91FoyerChapterData.get(server).snapshot().doctrine()) || campaign.hasCivilizationUpgrade("CARTOGRAPHIE_RESONANTE");
      String guidance = exact ? target.getX() + ", " + target.getY() + ", " + target.getZ() + " · " + distance + " blocs"
         : "environ " + distance + " blocs vers le " + direction;
      player.displayClientMessage(Component.literal("§bBOUSSOLE — " + label + " §8• §f" + guidance), true);
      player.serverLevel().playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.8F, 1.25F);
   }

   private static BlockPos objectiveTarget(MinecraftServer server, CampaignSavedData campaign, C110TrailData.Snapshot trail) {
      if (C110TrailRules.SEEK_RIFTS.equals(trail.stage())) for (int i = 0; i < 3; i++) if ((trail.discoveredMask() & (1 << i)) == 0) return trail.sites()[i];
      if (C110TrailRules.RETURN_FOYER.equals(trail.stage()) || C110TrailRules.CENSUS.equals(trail.stage())) return campaign.foundationPos();
      try { int[] o = F8SanctuaryEngine.target(server); return new BlockPos(o[0], o[1], o[2]); } catch (Throwable ignored) { return campaign.foundationPos(); }
   }

   private static void devNext(ServerPlayer player, C110TrailData data) {
      C110TrailData.Snapshot s = data.snapshot();
      if (C110TrailRules.LOCKED.equals(s.stage())) data.offer(player.serverLevel().getGameTime());
      else if (C110TrailRules.OFFERED.equals(s.stage())) follow(player);
      else if (C110TrailRules.SEEK_RIFTS.equals(s.stage())) {
         ensureSites(player.serverLevel(), CampaignSavedData.get(player.getServer()), data);
         C110TrailData.Snapshot ready = data.snapshot();
         for (int i = 0; i < 3; i++) if ((ready.discoveredMask() & (1 << i)) == 0) {
            BlockPos p = ready.sites()[i];
            player.teleportTo(player.serverLevel(), p.getX() + 3.5, p.getY() + 1.0, p.getZ() + 3.5, player.getYRot(), player.getXRot());
            player.displayClientMessage(Component.literal("§6CHAPITRE II DEV §8• §fFêlure suivante : approchez et cliquez sa structure."), false);
            break;
         }
      } else if (C110TrailRules.RETURN_FOYER.equals(s.stage())) {
         CampaignSavedData campaign = CampaignSavedData.get(player.getServer());
         BlockPos p = campaign.foundationPos();
         player.teleportTo(player.serverLevel(), p.getX() + 2.5, p.getY() + 1.0, p.getZ() + 2.5, player.getYRot(), player.getXRot());
         beginCensus(player, data);
      }
      else if (C110TrailRules.CENSUS.equals(s.stage())) {
         for (int i = 0; i < 3; i++) data.observeWitness(i, player.getGameProfile().getName(), player.serverLevel().getGameTime());
         if (C110TrailRules.RETURN_SANCTUARY.equals(data.snapshot().stage())) {
            MinecraftServer server = player.getServer();
            if (server != null) finishCensus(player, server);
         }
      }
      else if (C110TrailRules.RETURN_SANCTUARY.equals(s.stage())) {
         if (!carriesFragment(player)) give(player, new ItemStack((Item)ReivaxMCProgress.UNKNOWN_FRAGMENT.get()));
         try { BlockPos p=C110SanctuaryArchitecture.eastThreshold(player.getServer()).offset(-6,0,0); player.teleportTo(player.serverLevel(),p.getX()+.5,p.getY(),p.getZ()+.5,270,0); } catch(Throwable ignored) {}
      } else if (C110TrailRules.REGISTRY.equals(s.stage())) {
         try { int[] o = F8SanctuaryEngine.target(player.getServer()); BlockPos p = new BlockPos(o[0] + 27, o[1] + 2, o[2] - 16);
            player.teleportTo(player.serverLevel(), p.getX() - 2.5, p.getY(), p.getZ() + 0.5, 0, 0);
            player.displayClientMessage(Component.literal("§6CHAPITRE II DEV §8• §fCliquez le Registre des Absents."), false);
         } catch (Throwable ignored) {}
      }
      sendStatus(player, data.snapshot());
   }

   private static void sendStatus(ServerPlayer player, C110TrailData.Snapshot state) {
      player.displayClientMessage(Component.literal("§6CHAPITRE II §8• §f" + state.stage() + " §8• §fFêlures " + state.discoveredCount() + "/3 §8• §fVeilleurs " + state.witnessCount() + "/3"), false);
   }

   private static boolean isRunning(String stage) { return !C110TrailRules.LOCKED.equals(stage) && !C110TrailRules.OFFERED.equals(stage); }
   private static void repairSanctuary(ServerPlayer player) {
      MinecraftServer server=player.getServer(); if(server==null) return;
      CampaignSavedData c=CampaignSavedData.get(server);
      if(!c.isCompleted(F94SanctuaryShell.BUILT)) return;
      try { int[] o=F8SanctuaryEngine.target(server); if(!C110SanctuaryArchitecture.isPresent(server,o)) C110SanctuaryArchitecture.build(server,o); }
      catch(Throwable ignored) {}
   }
   private static boolean atFoyer(ServerPlayer player, double radius) {
      MinecraftServer server = player.getServer(); if (server == null) return false;
      CampaignSavedData c = CampaignSavedData.get(server);
      return c.foundationPlaced() && player.serverLevel().dimension().location().toString().equals(c.foundationDimension()) && player.blockPosition().closerThan(c.foundationPos(), radius);
   }
   private static boolean carriesFragment(ServerPlayer player) { return player.getInventory().contains(new ItemStack((Item)ReivaxMCProgress.UNKNOWN_FRAGMENT.get())); }
   private static void give(ServerPlayer player, ItemStack stack) { if (!player.getInventory().add(stack)) player.drop(stack, false); }
   private static void consume(RightClickBlock event) { event.setCanceled(true); event.setCancellationResult(InteractionResult.SUCCESS); }
   private static void clearWatchers(MinecraftServer server) {
      server.getCommands().performPrefixedCommand(server.createCommandSourceStack().withSuppressedOutput(), "kill @e[tag=reivax_ch2_watcher]");
   }
   private static void broadcast(MinecraftServer server, CampaignSavedData campaign, String id, String title, String text, int age) {
      ProgressNetworking.broadcast(server, campaign, id + "|GROUP||CIV=0", "STORY", title, text, age);
   }
   private static void sync(MinecraftServer server) { ProgressNetworking.syncAll(server, CampaignSavedData.get(server)); }
}
