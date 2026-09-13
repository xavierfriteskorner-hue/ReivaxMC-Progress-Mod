package fr.reivaxmc.progress.story;

import fr.reivaxmc.progress.ReivaxMCProgress;
import fr.reivaxmc.progress.network.ProgressNetworking;
import fr.reivaxmc.progress.progression.CampaignSavedData;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.neoforged.neoforge.event.level.BlockEvent.EntityPlaceEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent.Post;

/**
 * Chapitre I — Le Foyer emprunté.
 *
 * Le chapitre est volontairement autonome : il dépend uniquement de Minecraft et
 * des objets REIVAX. Son état est commun au monde, donc chaque action SOLO ou DUO
 * contribue à la même campagne sans imposer que les deux joueurs soient connectés.
 */
public final class F91FoyerChapterEngine {
   private static final String WITNESS_TAG = "reivax_ch1_witness";
   private static final String[] HOME_MILESTONES = {"CH1_HOME", "CH1_DOCTRINE", "CH1_ECHO", "CH1_RETURN", "CH1_COMPLETE"};

   private F91FoyerChapterEngine() {
   }

   public static void onLogin(PlayerLoggedInEvent event) {
      if (event.getEntity() instanceof ServerPlayer player) {
         ensureStarted(player);
         restoreFor(player);
      }
   }

   public static void onPlayerTick(Post event) {
      if (!(event.getEntity() instanceof ServerPlayer player) || player.tickCount % 20 != 0) return;
      ensureStarted(player);
      F91FoyerChapterData data = F91FoyerChapterData.get(player.getServer());
      F91FoyerChapterData.Snapshot state = data.snapshot();
      if (F91ChapterRules.SHAPE_FOYER.equals(state.stage()) && state.homeSignals() == 0) {
         scanExistingHome(player, data);
      }
      if (F91ChapterRules.FIND_ECHO.equals(state.stage()) && state.echoPlaced() && player.tickCount % 100 == 0) {
         sendEchoGuidance(player, state);
      }
      if (F91ChapterRules.RETURN_FRAGMENT.equals(state.stage()) && carriesUnknownFragment(player) && isBackAtFoyer(player)) {
         beginDefense(player, data);
      }
      if (F91ChapterRules.DEFEND_FOYER.equals(state.stage()) && player.tickCount % 100 == 0) {
         ensureWitnessPresence(player, data);
      }
      if (state.completed()) grantReward(player, data, state.doctrine());
   }

   public static void onBlockPlaced(EntityPlaceEvent event) {
      if (!(event.getEntity() instanceof ServerPlayer player)) return;
      MinecraftServer server = player.getServer();
      if (server == null) return;
      CampaignSavedData campaign = CampaignSavedData.get(server);
      F91FoyerChapterData data = F91FoyerChapterData.get(server);
      if (!campaign.foundationPlaced() || !campaign.isInsideMainTerritory(player.serverLevel().dimension().location().toString(), event.getPos())) return;
      if (!F91ChapterRules.SHAPE_FOYER.equals(data.snapshot().stage())) return;
      String id = BuiltInRegistries.BLOCK.getKey(event.getPlacedBlock().getBlock()).toString();
      registerHomeSignal(server, player, data, F91ChapterRules.classifyBlock(id));
   }

   /** @return true lorsque le clic appartient au chapitre et ne doit pas être retraité par F8. */
   public static boolean onRightClickBlock(RightClickBlock event) {
      if (!(event.getEntity() instanceof ServerPlayer player) || !(event.getLevel() instanceof ServerLevel level)) return false;
      MinecraftServer server = player.getServer();
      if (server == null) return false;
      CampaignSavedData campaign = CampaignSavedData.get(server);
      F91FoyerChapterData data = F91FoyerChapterData.get(server);
      F91FoyerChapterData.Snapshot state = data.snapshot();
      BlockPos pos = event.getPos();

      if (state.echoPlaced() && state.echoPos().equals(pos) && sameDimension(level, state.echoDimension())) {
         consume(event);
         if (F91ChapterRules.FIND_ECHO.equals(state.stage())) investigateEcho(player, data);
         else player.displayClientMessage(Component.literal("§6ÉCHO §8• §7La pierre a déjà livré ce qu'elle conservait."), false);
         return true;
      }

      return false;
   }

   public static void onLivingDeath(LivingDeathEvent event) {
      Entity entity = event.getEntity();
      if (!entity.getTags().contains(WITNESS_TAG) || !(entity.level() instanceof ServerLevel level)) return;
      MinecraftServer server = level.getServer();
      F91FoyerChapterData data = F91FoyerChapterData.get(server);
      String actor = event.getSource().getEntity() instanceof ServerPlayer player ? player.getGameProfile().getName() : "le Foyer";
      if (!data.markWitnessDefeated(actor, level.getGameTime())) return;
      F91FoyerChapterData.Snapshot state = data.snapshot();
      if (state.completed()) completeChapter(server, data, state, actor);
      else broadcastObjective(server, "Défendez le Foyer · Témoins neutralisés " + state.witnessesDefeated() + "/" + state.witnessTarget() + ".");
   }

   /** Démarrage immédiat appelé à la pose de la Borne, le tick restant le filet de sécurité. */
   public static void onFoundationEstablished(MinecraftServer server, ServerPlayer founder) {
      startChapter(server, founder, F91FoyerChapterData.get(server));
   }

   /** Vote demandé depuis l'interface de la Borne. Aucun objet n'est requis ni consommé. */
   public static void submitCouncilVote(ServerPlayer player, String doctrine) {
      MinecraftServer server = player.getServer();
      if (server == null || !Set.of(F91ChapterRules.BASTION, F91ChapterRules.MEMORY, F91ChapterRules.SOLIDARITY).contains(doctrine)) return;
      CampaignSavedData campaign = CampaignSavedData.get(server);
      F91FoyerChapterData data = F91FoyerChapterData.get(server);
      if (!F91ChapterRules.CHOOSE_PRIORITY.equals(data.snapshot().stage())) {
         player.displayClientMessage(Component.literal("§6CONSEIL DU FOYER §8• §fAucune décision n'attend actuellement votre voix."), false);
         ProgressNetworking.openFoyerPanel(player);
         return;
      }
      if (!campaign.foundationPlaced()
         || !player.serverLevel().dimension().location().toString().equals(campaign.foundationDimension())
         || player.blockPosition().distSqr(campaign.foundationPos()) > 144.0) {
         player.displayClientMessage(Component.literal("§6CONSEIL DU FOYER §8• §fRevenez près de la Borne pour voter."), false);
         return;
      }
      chooseDoctrine(player, data, doctrine);
   }

   public static int devCommand(ServerPlayer player, String action) {
      MinecraftServer server = player.getServer();
      if (server == null) return 0;
      F91FoyerChapterData data = F91FoyerChapterData.get(server);
      try {
         switch (action) {
            case "start" -> {
               clearChapterWorld(server, data.snapshot());
               data.reset();
               ensureDevFoundation(player);
               startChapter(server, player, data);
               player.displayClientMessage(Component.literal("§6CHAPITRE I DEV §8• §aDépart prêt, prologue ignoré."), false);
            }
            case "next" -> devNext(player, data);
            case "status" -> sendStatus(player, data.snapshot());
            case "reset" -> {
               clearChapterWorld(server, data.snapshot());
               data.reset();
               CampaignSavedData.get(server).stage("AFTER_FOUNDATION");
               broadcastObjective(server, "Chapitre I réinitialisé · /reivax dev chapter1 start pour recommencer.");
               player.displayClientMessage(Component.literal("§6CHAPITRE I DEV §8• §aÉtat narratif réinitialisé."), false);
            }
            default -> player.displayClientMessage(Component.literal("§6CHAPITRE I DEV §8• §fstart · next · status · reset"), false);
         }
         return 1;
      } catch (Throwable error) {
         player.displayClientMessage(Component.literal("§cChapitre I DEV impossible : " + error.getClass().getSimpleName()), false);
         System.err.println("[REIVAX 0.9.0] chapter DEV failed: " + error);
         return 0;
      }
   }

   private static void ensureStarted(ServerPlayer player) {
      MinecraftServer server = player.getServer();
      if (server == null) return;
      CampaignSavedData campaign = CampaignSavedData.get(server);
      F91FoyerChapterData data = F91FoyerChapterData.get(server);
      if (campaign.foundationPlaced() && F91ChapterRules.LOCKED.equals(data.snapshot().stage())
         && CampaignCoordinator.canStart(server, CampaignCoordinator.CHAPTER_1)) startChapter(server, player, data);
   }

   private static void startChapter(MinecraftServer server, ServerPlayer actor, F91FoyerChapterData data) {
      CampaignSavedData campaign = CampaignSavedData.get(server);
      if (!campaign.foundationPlaced() || !data.begin(server.overworld().getGameTime(), actor.getGameProfile().getName())) return;
      campaign.stage("CH1_SHAPE_FOYER");
      campaign.addTimeline(day(server), actor.getGameProfile().getName(), "Le Foyer emprunté", "La Borne a reconnu une mémoire enfouie sous le nouveau Foyer.");
      for (ServerPlayer player : players(server)) F7NarrativeEngine.pushUi(player, "F8_MILESTONE", "CHAPITRE I — LE FOYER EMPRUNTÉ");
      broadcastVoice(server, "CH1_OPENING", "Vous appelez cela votre premier Foyer. La Borne, elle, ne l'appelle pas le premier.", 0, 0);
      broadcastObjective(server, homeObjective(0));
      scanExistingHome(actor, data);
   }

   private static void scanExistingHome(ServerPlayer actor, F91FoyerChapterData data) {
      MinecraftServer server = actor.getServer();
      if (server == null) return;
      CampaignSavedData campaign = CampaignSavedData.get(server);
      ServerLevel level = levelFor(server, campaign.foundationDimension());
      if (level == null) return;
      BlockPos home = campaign.foundationPos();
      int found = 0;
      for (int dx = -12; dx <= 12 && F91ChapterRules.signalCount(found) < 3; dx++) {
         for (int dy = -5; dy <= 8 && F91ChapterRules.signalCount(found) < 3; dy++) {
            for (int dz = -12; dz <= 12 && F91ChapterRules.signalCount(found) < 3; dz++) {
               String id = BuiltInRegistries.BLOCK.getKey(level.getBlockState(home.offset(dx, dy, dz)).getBlock()).toString();
               found |= F91ChapterRules.classifyBlock(id);
            }
         }
      }
      for (int signal : new int[]{F91ChapterRules.REST, F91ChapterRules.STORAGE, F91ChapterRules.WORK, F91ChapterRules.LIGHT, F91ChapterRules.IDENTITY}) {
         if ((found & signal) != 0) registerHomeSignal(server, actor, data, signal);
      }
   }

   private static void registerHomeSignal(MinecraftServer server, ServerPlayer actor, F91FoyerChapterData data, int signal) {
      if (signal == 0 || !data.addHomeSignal(signal, actor.getGameProfile().getName(), actor.serverLevel().getGameTime())) return;
      F91FoyerChapterData.Snapshot state = data.snapshot();
      int count = F91ChapterRules.signalCount(state.homeSignals());
      if (F91ChapterRules.CHOOSE_PRIORITY.equals(state.stage())) {
         CampaignSavedData.get(server).stage("CH1_CHOOSE_PRIORITY");
         milestone(server, "CH1_HOME", "UN LIEU QUI VOUS RESSEMBLE", "Trois repères suffisent. Un Foyer n'est pas ce qui vous protège du monde : c'est ce que vous refusez de lui abandonner.", 20, 4);
         broadcastObjective(server, "Choisissez ce qui doit survivre en premier · ouvrez le Conseil depuis la Borne.");
      } else {
         broadcastObjective(server, homeObjective(count));
         actor.displayClientMessage(Component.literal("§6FOYER §8• §a" + F91ChapterRules.signalLabel(signal) + " reconnu §7(" + count + "/3)"), false);
      }
   }

   private static void chooseDoctrine(ServerPlayer actor, F91FoyerChapterData data, String doctrine) {
      MinecraftServer server = actor.getServer();
      if (server == null) return;
      CampaignSavedData campaign = CampaignSavedData.get(server);
      List<ServerPlayer> council = players(server);
      F91FoyerChapterData.Snapshot before = data.snapshot();
      Set<String> required = before.councilVoters().isEmpty()
         ? council.stream().map(player -> player.getUUID().toString()).collect(Collectors.toSet())
         : before.councilVoters();
      if (required.size() > 1 && !councilReady(council, campaign, required)) {
         actor.displayClientMessage(Component.literal("§6CONSEIL DU FOYER §8• §fRegroupez-vous tous près de la Borne avant de décider."), false);
         for (ServerPlayer player : council) {
            if (!player.getUUID().equals(actor.getUUID())) player.displayClientMessage(Component.literal("§6CONSEIL DU FOYER §8• §fVotre partenaire vous attend près de la Borne."), false);
         }
         return;
      }
      F91FoyerChapterData.CouncilResult result = data.vote(doctrine, actor.getUUID().toString(), actor.getGameProfile().getName(), required,
         actor.serverLevel().getGameTime());
      if (result == F91FoyerChapterData.CouncilResult.INVALID) return;
      if (result == F91FoyerChapterData.CouncilResult.WAITING) {
         ProgressNetworking.broadcast(server, campaign, "CH1_COUNCIL_WAIT|GROUP||CIV=0", "NARRATOR_GUIDANCE", "CONSEIL DU FOYER",
            actor.getGameProfile().getName() + " propose « " + F91ChapterRules.doctrineLabel(doctrine) + " ». L'autre voix doit maintenant se prononcer.", 0);
         broadcastObjective(server, "Conseil en cours · chaque membre ouvre la Borne et confirme sa priorité.");
         ProgressNetworking.refreshCouncilPanels(server);
         return;
      }
      if (result == F91FoyerChapterData.CouncilResult.DISSONANCE) {
         ProgressNetworking.broadcast(server, campaign, "CH1_COUNCIL_DISSONANCE|GROUP||CIV=0", "NARRATOR_WHISPER", "DISSONANCE",
            "Vos priorités se contredisent. La Borne ne choisira pas à votre place : l'un de vous doit répondre de nouveau.", 0);
         broadcastObjective(server, "Dissonance · modifiez vos votes dans le Conseil jusqu'à partager la même priorité.");
         ProgressNetworking.refreshCouncilPanels(server);
         return;
      }
      if (result != F91FoyerChapterData.CouncilResult.CONFIRMED) return;
      CampaignSavedData.get(server).stage("CH1_FIND_ECHO");
      for (ServerPlayer player : council) NarratorEngineBridge.rememberChoice(player, doctrine);
      String line = switch (doctrine) {
         case F91ChapterRules.BASTION -> "Vous avez choisi que les murs tiennent. Souvenez-vous seulement de ceux qu'ils laisseront dehors.";
         case F91ChapterRules.MEMORY -> "Vous avez choisi de comprendre. Le savoir éclaire. Il sait aussi très bien brûler.";
         default -> "Vous avez choisi de partager. C'est une force coûteuse : elle exige de compter les absents.";
      };
      milestone(server, "CH1_DOCTRINE", "PRIORITÉ — " + F91ChapterRules.doctrineLabel(doctrine), line, 10, 5);
      placeEcho(server, data);
      broadcastObjective(server, "Suivez l'Écho apparu autour du Foyer · la Résonance vous indiquera sa direction.");
      ProgressNetworking.refreshCouncilPanels(server);
   }

   private static boolean councilReady(List<ServerPlayer> players, CampaignSavedData campaign, Set<String> required) {
      if (players.size() < required.size()) return false;
      for (String uuid : required) {
         ServerPlayer player = players.stream().filter(candidate -> candidate.getUUID().toString().equals(uuid)).findFirst().orElse(null);
         if (player == null || !player.serverLevel().dimension().location().toString().equals(campaign.foundationDimension())
            || player.blockPosition().distSqr(campaign.foundationPos()) > 400.0) return false;
      }
      return true;
   }

   private static void placeEcho(MinecraftServer server, F91FoyerChapterData data) {
      CampaignSavedData campaign = CampaignSavedData.get(server);
      ServerLevel level = levelFor(server, campaign.foundationDimension());
      if (level == null) level = server.overworld();
      BlockPos home = campaign.foundationPos();
      long seed = level.getSeed() ^ home.asLong();
      int start = Math.floorMod(Long.hashCode(seed), 8);
      int[][] directions = {{1,0},{1,1},{0,1},{-1,1},{-1,0},{-1,-1},{0,-1},{1,-1}};
      BlockPos echo = null;
      for (int attempt = 0; attempt < 24 && echo == null; attempt++) {
         int[] direction = directions[(start + attempt) & 7];
         int distance = 48 + (attempt % 6) * 5;
         int x = home.getX() + direction[0] * distance + (attempt % 3) * 3;
         int z = home.getZ() + direction[1] * distance - (attempt % 4) * 2;
         int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
         BlockPos candidate = new BlockPos(x, y, z);
         if (level.getBlockState(candidate).isAir() && !level.getBlockState(candidate.below()).isAir() && level.getFluidState(candidate.below()).isEmpty()) echo = candidate;
      }
      if (echo == null) echo = home.offset(18, 1, 0);
      buildEchoMonolith(level, echo);
      data.placeEcho(echo, level.dimension().location().toString(), level.getGameTime());
      level.playSound(null, echo, SoundEvents.RESPAWN_ANCHOR_SET_SPAWN, SoundSource.BLOCKS, 1.2F, 0.55F);
   }

   private static void investigateEcho(ServerPlayer actor, F91FoyerChapterData data) {
      MinecraftServer server = actor.getServer();
      if (server == null || !data.examineEcho(actor.getGameProfile().getName(), actor.serverLevel().getGameTime())) return;
      CampaignSavedData campaign = CampaignSavedData.get(server);
      campaign.stage("CH1_RETURN_FRAGMENT");
      campaign.addArtifact(new CampaignSavedData.ArtifactRecord("foyer_emprunte_01", "Mémoire du Foyer emprunté", actor.getGameProfile().getName(),
         actor.getUUID().toString(), day(server), "Aux abords du Premier Foyer", "Des marques ont été gravées depuis l'intérieur."));
      give(actor, new ItemStack((Item)ReivaxMCProgress.UNKNOWN_FRAGMENT.get()));
      actor.serverLevel().sendParticles(net.minecraft.core.particles.ParticleTypes.SCULK_SOUL,
         actor.getX(), actor.getY() + 1.0, actor.getZ(), 24, 0.7, 0.9, 0.7, 0.025);
      milestone(server, "CH1_ECHO", "LA MÉMOIRE SOUS LA TERRE",
         "Des marques couvrent l'intérieur de la pierre. Pas des outils. Des ongles. Quelqu'un a essayé de sortir. Ce Foyer n'est pas neuf : il vous a été prêté.", 25, 5);
      broadcastObjective(server, "Conservez le Fragment et retournez au cœur du Foyer · seule la Matrice pourra un jour l'analyser.");
      for (ServerPlayer player : players(server)) F7NarrativeEngine.pushUi(player, "F8_GUIDANCE", "");
   }

   private static void beginDefense(ServerPlayer actor, F91FoyerChapterData data) {
      MinecraftServer server = actor.getServer();
      if (server == null) return;
      int target = players(server).size() >= 2 ? 2 : 1;
      if (!data.beginDefense(target, actor.getGameProfile().getName(), actor.serverLevel().getGameTime())) return;
      CampaignSavedData.get(server).stage("CH1_DEFEND_FOYER");
      milestone(server, "CH1_RETURN", "CE QUI REVIENT AVEC VOUS",
         "La Borne enregistre votre retour, mais elle ne sait pas lire le Fragment. Quelque chose d'autre, pourtant, l'a reconnu et vient le réclamer.", 20, 6);
      spawnWitnesses(server, data, target);
      broadcastObjective(server, "Défendez le Foyer · neutralisez " + target + (target > 1 ? " Témoins attirés" : " Témoin attiré") + " par la mémoire.");
   }

   private static void spawnWitnesses(MinecraftServer server, F91FoyerChapterData data, int count) {
      CampaignSavedData campaign = CampaignSavedData.get(server);
      ServerLevel level = levelFor(server, campaign.foundationDimension());
      if (level == null) level = server.overworld();
      BlockPos home = campaign.foundationPos();
      for (int i = 0; i < count; i++) {
         int x = home.getX() + (i == 0 ? 10 : -10);
         int z = home.getZ() + (i == 0 ? 7 : -7);
         int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
         String command = "execute in " + level.dimension().location() + " run summon reivaxmc_progress:veilleur " + (x + 0.5) + " " + y + " " + (z + 0.5)
            + " {Tags:[\"" + WITNESS_TAG + "\"],PersistenceRequired:1b}";
         server.getCommands().performPrefixedCommand(server.createCommandSourceStack().withSuppressedOutput(), command);
      }
      data.markSpawn(level.getGameTime());
      level.playSound(null, home, SoundEvents.SCULK_SHRIEKER_SHRIEK, SoundSource.HOSTILE, 1.25F, 0.62F);
   }

   /** Filet de sécurité : une entité supprimée par un autre mod ou un rechargement ne bloque jamais la campagne. */
   private static void ensureWitnessPresence(ServerPlayer player, F91FoyerChapterData data) {
      MinecraftServer server = player.getServer();
      if (server == null) return;
      F91FoyerChapterData.Snapshot state = data.snapshot();
      if (!F91ChapterRules.DEFEND_FOYER.equals(state.stage())) return;
      CampaignSavedData campaign = CampaignSavedData.get(server);
      ServerLevel level = levelFor(server, campaign.foundationDimension());
      if (level == null) return;
      int remaining = Math.max(0, state.witnessTarget() - state.witnessesDefeated());
      if (remaining == 0 || level.getGameTime() - state.lastSpawnTick() < 200L) return;
      AABB area = new AABB(campaign.foundationPos()).inflate(64.0);
      int present = level.getEntities((Entity)null, area, entity -> entity.getTags().contains(WITNESS_TAG)).size();
      if (present < remaining) {
         spawnWitnesses(server, data, remaining - present);
         broadcastObjective(server, "La mémoire refuse de disparaître · " + remaining + (remaining > 1 ? " Témoins restent" : " Témoin reste") + " à neutraliser.");
      }
   }

   private static void completeChapter(MinecraftServer server, F91FoyerChapterData data, F91FoyerChapterData.Snapshot state, String actor) {
      CampaignSavedData campaign = CampaignSavedData.get(server);
      campaign.stage("CH1_COMPLETE");
      String reflection = switch (state.doctrine()) {
         case F91ChapterRules.BASTION -> "Les précédents avaient eux aussi choisi de protéger. Leur échec ne condamne pas les murs. Il rappelle seulement qu'aucun mur ne choisit qui mérite d'être sauvé.";
         case F91ChapterRules.MEMORY -> "Les précédents avaient eux aussi choisi de comprendre. Ils ont tout consigné, sauf le moment où savoir ne suffisait plus.";
         default -> "Les précédents avaient eux aussi choisi de partager. À la fin, ils ont partagé jusqu'à leur faim. Une vertu n'abolit jamais son prix.";
      };
      campaign.addTimeline(day(server), actor, "Le prix d'un Foyer", "Le Foyer a survécu à son premier Témoin. Priorité conservée : " + F91ChapterRules.doctrineLabel(state.doctrine()) + ".");
      milestone(server, "CH1_COMPLETE", "CHAPITRE I TERMINÉ — LE FOYER EMPRUNTÉ", reflection + " La question n'est pas de savoir si votre choix est pur. Seulement ce que vous accepterez d'en payer.", 40, 10);
      broadcastObjective(server, "Développez librement votre Foyer · la prochaine Résonance viendra lorsque votre civilisation sera prête.");
      for (ServerPlayer player : players(server)) grantReward(player, data, state.doctrine());
   }

   private static void grantReward(ServerPlayer player, F91FoyerChapterData data, String doctrine) {
      if (!data.claimReward(player.getUUID().toString())) return;
      give(player, new ItemStack((Item)ReivaxMCProgress.ECHO_STONE_ITEM.get()));
      switch (doctrine) {
         case F91ChapterRules.BASTION -> {
            give(player, new ItemStack(Items.SHIELD));
            give(player, new ItemStack(Items.IRON_INGOT, 4));
         }
         case F91ChapterRules.MEMORY -> {
            give(player, new ItemStack(Items.COMPASS));
            give(player, new ItemStack(Items.BOOK, 3));
         }
         default -> {
            give(player, new ItemStack(Items.BREAD, 12));
            give(player, new ItemStack(Items.GOLDEN_APPLE));
         }
      }
      player.displayClientMessage(Component.literal("§6RÉCOMPENSE §8• §fPierre des Absents + paquet « " + F91ChapterRules.doctrineLabel(doctrine) + " »"), false);
   }

   private static void restoreFor(ServerPlayer player) {
      MinecraftServer server = player.getServer();
      if (server == null) return;
      F91FoyerChapterData data = F91FoyerChapterData.get(server);
      F91FoyerChapterData.Snapshot state = data.snapshot();
      if (F91ChapterRules.LOCKED.equals(state.stage())) return;
      F7NarrativeEngine.routeStoryMessage(player, "§6OBJECTIF PRINCIPAL §8• §f" + objectiveFor(state), true);
      if (state.completed()) grantReward(player, data, state.doctrine());
   }

   private static String objectiveFor(F91FoyerChapterData.Snapshot state) {
      return switch (state.stage()) {
         case F91ChapterRules.SHAPE_FOYER -> homeObjective(F91ChapterRules.signalCount(state.homeSignals()));
         case F91ChapterRules.CHOOSE_PRIORITY -> state.councilVoters().isEmpty()
            ? "Choisissez ce qui doit survivre en premier · ouvrez le Conseil depuis la Borne."
            : "Conseil du Foyer en cours · chaque membre doit confirmer son choix dans la Borne.";
         case F91ChapterRules.FIND_ECHO -> "Suivez l'Écho autour du Foyer · la Résonance indique sa direction.";
         case F91ChapterRules.RETURN_FRAGMENT -> "Conservez le Fragment et revenez au cœur du Foyer · la Matrice demeure inaccessible.";
         case F91ChapterRules.DEFEND_FOYER -> "Défendez le Foyer · Témoins neutralisés " + state.witnessesDefeated() + "/" + state.witnessTarget() + ".";
         default -> "Développez librement votre Foyer · la prochaine Résonance viendra lorsque votre civilisation sera prête.";
      };
   }

   private static String homeObjective(int count) {
      return "Donnez une forme au Foyer · installez 3 repères parmi repos, réserve, travail, lumière et identité · " + count + "/3.";
   }

   private static void sendEchoGuidance(ServerPlayer player, F91FoyerChapterData.Snapshot state) {
      if (!sameDimension(player.serverLevel(), state.echoDimension())) {
         F7NarrativeEngine.pushUi(player, "F8_GUIDANCE", "ÉCHO · retournez dans le monde du Foyer");
         return;
      }
      BlockPos here = player.blockPosition();
      BlockPos target = state.echoPos();
      double distance = Math.sqrt(here.distSqr(target));
      String direction = cardinal(target.getX() - here.getX(), target.getZ() - here.getZ());
      F7NarrativeEngine.pushUi(player, "F8_GUIDANCE", "ÉCHO · " + direction + "  " + Math.round(distance) + " m  ·  " + (distance < 7.0 ? "À PORTÉE" : "RÉSONANCE"));
   }

   private static String cardinal(int dx, int dz) {
      String ns = dz < -4 ? "N" : dz > 4 ? "S" : "";
      String ew = dx > 4 ? "E" : dx < -4 ? "O" : "";
      return (ns + ew).isBlank() ? "ICI" : ns + ew;
   }

   private static boolean carriesUnknownFragment(ServerPlayer player) {
      return player.getInventory().contains(new ItemStack((Item)ReivaxMCProgress.UNKNOWN_FRAGMENT.get()));
   }

   private static boolean isBackAtFoyer(ServerPlayer player) {
      MinecraftServer server = player.getServer();
      if (server == null) return false;
      CampaignSavedData campaign = CampaignSavedData.get(server);
      return campaign.foundationPlaced()
         && player.serverLevel().dimension().location().toString().equals(campaign.foundationDimension())
         && player.blockPosition().distSqr(campaign.foundationPos()) <= 324.0;
   }

   private static void milestone(MinecraftServer server, String id, String title, String detail, int age, int civilization) {
      CampaignSavedData campaign = CampaignSavedData.get(server);
      boolean awarded = campaign.complete(id, age, civilization);
      ProgressNetworking.broadcast(server, campaign, id + "|GROUP||CIV=" + (awarded ? civilization : 0), "NARRATOR_ACHIEVEMENT", title, detail, awarded ? age : 0);
      ProgressNetworking.syncAll(server, campaign);
   }

   private static void broadcastVoice(MinecraftServer server, String id, String detail, int age, int civilization) {
      CampaignSavedData campaign = CampaignSavedData.get(server);
      ProgressNetworking.broadcast(server, campaign, id + "|GROUP||CIV=" + civilization, "NARRATOR_WHISPER", "LA VOIX", detail, age);
   }

   private static void broadcastObjective(MinecraftServer server, String objective) {
      String value = "§6OBJECTIF PRINCIPAL §8• §f" + objective;
      F7NarrativeEngine.setCurrentObjective(value);
      for (ServerPlayer player : players(server)) F7NarrativeEngine.routeStoryMessage(player, value, true);
   }

   private static void give(ServerPlayer player, ItemStack stack) {
      if (!player.getInventory().add(stack)) player.drop(stack, false);
   }

   private static void consume(RightClickBlock event) {
      event.setCancellationResult(InteractionResult.SUCCESS);
      event.setCanceled(true);
   }

   private static List<ServerPlayer> players(MinecraftServer server) {
      return server.getPlayerList().getPlayers();
   }

   private static int day(MinecraftServer server) {
      return (int)(server.overworld().getDayTime() / 24000L) + 1;
   }

   private static boolean sameDimension(ServerLevel level, String dimension) {
      return level.dimension().location().toString().equals(dimension);
   }

   private static ServerLevel levelFor(MinecraftServer server, String dimension) {
      try {
         ResourceLocation id = ResourceLocation.parse(dimension);
         return server.getLevel(ResourceKey.create(Registries.DIMENSION, id));
      } catch (Throwable ignored) {
         return server.overworld();
      }
   }

   private static void ensureDevFoundation(ServerPlayer player) {
      MinecraftServer server = player.getServer();
      CampaignSavedData campaign = CampaignSavedData.get(server);
      if (campaign.foundationPlaced()) return;
      ServerLevel level = player.serverLevel();
      BlockPos raw = player.blockPosition().relative(player.getDirection(), 4);
      int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, raw.getX(), raw.getZ());
      BlockPos pos = new BlockPos(raw.getX(), y, raw.getZ());
      level.setBlock(pos, ((Block)ReivaxMCProgress.FOUNDATION_BEACON.get()).defaultBlockState(), 3);
      campaign.complete("F8_FOUNDATION_BEACON_RECOVERED", 0, 0);
      campaign.foundSettlement(pos, level.dimension().location().toString(), player.getGameProfile().getName(), player.getUUID(), day(server), level.getGameTime());
      ProgressNetworking.syncAll(server, campaign);
   }

   private static void devNext(ServerPlayer player, F91FoyerChapterData data) {
      MinecraftServer server = player.getServer();
      F91FoyerChapterData.Snapshot state = data.snapshot();
      switch (state.stage()) {
         case F91ChapterRules.LOCKED -> {
            ensureDevFoundation(player);
            startChapter(server, player, data);
         }
         case F91ChapterRules.SHAPE_FOYER -> {
            registerHomeSignal(server, player, data, F91ChapterRules.REST);
            registerHomeSignal(server, player, data, F91ChapterRules.STORAGE);
            registerHomeSignal(server, player, data, F91ChapterRules.WORK);
            ProgressNetworking.openFoyerPanel(player);
            player.displayClientMessage(Component.literal("§6CHAPITRE I DEV §8• §fLe Conseil est ouvert dans la Borne : choisissez puis confirmez votre priorité."), false);
         }
         case F91ChapterRules.CHOOSE_PRIORITY -> ProgressNetworking.openFoyerPanel(player);
         case F91ChapterRules.FIND_ECHO -> {
            BlockPos echo = state.echoPos();
            server.getCommands().performPrefixedCommand(server.createCommandSourceStack().withSuppressedOutput(),
               "execute in " + state.echoDimension() + " run tp " + player.getGameProfile().getName() + " " + echo.getX() + " " + (echo.getY() + 2) + " " + echo.getZ());
            player.displayClientMessage(Component.literal("§6CHAPITRE I DEV §8• §fInspectez la Pierre devant vous par CLIC DROIT."), false);
         }
         case F91ChapterRules.RETURN_FRAGMENT -> {
            CampaignSavedData campaign = CampaignSavedData.get(server);
            BlockPos home = campaign.foundationPos();
            server.getCommands().performPrefixedCommand(server.createCommandSourceStack().withSuppressedOutput(),
               "execute in " + campaign.foundationDimension() + " run tp " + player.getGameProfile().getName() + " " + home.getX() + " " + (home.getY() + 2) + " " + home.getZ());
            player.displayClientMessage(Component.literal("§6CHAPITRE I DEV §8• §fLe retour sera reconnu automatiquement ; le Fragment restera dans votre inventaire."), false);
         }
         case F91ChapterRules.DEFEND_FOYER -> {
            String dimension = CampaignSavedData.get(server).foundationDimension();
            server.getCommands().performPrefixedCommand(server.createCommandSourceStack().withSuppressedOutput(),
               "execute in " + dimension + " run kill @e[tag=" + WITNESS_TAG + "]");
            player.displayClientMessage(Component.literal("§6CHAPITRE I DEV §8• §aCombat résolu pour le contrôle express."), false);
         }
         default -> grantReward(player, data, state.doctrine());
      }
      sendStatus(player, data.snapshot());
   }

   private static void sendStatus(ServerPlayer player, F91FoyerChapterData.Snapshot state) {
      player.displayClientMessage(Component.literal("§6CHAPITRE I §8• §f" + state.stage() + " §8• §fFoyer "
         + F91ChapterRules.signalCount(state.homeSignals()) + "/3 §8• §fChoix " + (state.doctrine().isBlank() ? "—" : F91ChapterRules.doctrineLabel(state.doctrine()))
         + " §8• §fConseil " + state.councilVotes().size() + "/" + state.councilVoters().size()
         + " §8• §fTémoins " + state.witnessesDefeated() + "/" + state.witnessTarget()), false);
   }

   private static void clearChapterWorld(MinecraftServer server, F91FoyerChapterData.Snapshot state) {
      if (state.echoPlaced()) {
         ServerLevel level = levelFor(server, state.echoDimension());
         if (level != null && level.getBlockState(state.echoPos()).is((Block)ReivaxMCProgress.ECHO_STONE.get())) {
            for (int y = -1; y <= 3; y++) {
               for (int x = -1; x <= 1; x++) level.setBlock(state.echoPos().offset(x, y, 0), Blocks.AIR.defaultBlockState(), 3);
            }
         }
      }
      String dimension = CampaignSavedData.get(server).foundationDimension();
      server.getCommands().performPrefixedCommand(server.createCommandSourceStack().withSuppressedOutput(),
         "execute in " + dimension + " run kill @e[tag=" + WITNESS_TAG + "]");
   }

   /** Une vraie silhouette monumentale, construite sans entité ni chute d'objets. */
   private static void buildEchoMonolith(ServerLevel level, BlockPos core) {
      Block echo = (Block)ReivaxMCProgress.ECHO_STONE.get();
      for (int y = 0; y <= 2; y++) level.setBlock(core.above(y), echo.defaultBlockState(), 3);
      for (int y = 0; y <= 2; y++) {
         level.setBlock(core.offset(-1, y, 0), Blocks.CHISELED_DEEPSLATE.defaultBlockState(), 3);
         level.setBlock(core.offset(1, y, 0), Blocks.CHISELED_DEEPSLATE.defaultBlockState(), 3);
      }
      level.setBlock(core.offset(0, 3, 0), Blocks.LIGHTNING_ROD.defaultBlockState(), 3);
      level.setBlock(core.offset(-1, -1, 0), Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState(), 3);
      level.setBlock(core.offset(0, -1, 0), Blocks.GILDED_BLACKSTONE.defaultBlockState(), 3);
      level.setBlock(core.offset(1, -1, 0), Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState(), 3);
   }

   /** Petit pont isolé pour ne pas exposer le stockage interne du Narrateur. */
   private static final class NarratorEngineBridge {
      private static void rememberChoice(ServerPlayer player, String doctrine) {
         int trust = F91ChapterRules.SOLIDARITY.equals(doctrine) ? 2 : 1;
         int defiance = F91ChapterRules.BASTION.equals(doctrine) ? 1 : 0;
         fr.reivaxmc.progress.narrator.NarratorEngine.rememberDecision(player, "CH1_DOCTRINE_" + doctrine, trust, defiance);
         fr.reivaxmc.progress.narrator.NarratorEngine.rememberFact(player, "CH1_DOCTRINE_" + doctrine, 1);
      }
   }
}
