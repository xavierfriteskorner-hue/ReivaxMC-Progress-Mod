package fr.reivaxmc.progress.network;

import fr.reivaxmc.progress.ReivaxMCProgress;
import fr.reivaxmc.progress.client.DestinyBookScreen;
import fr.reivaxmc.progress.client.FoundationPlacementScreen;
import fr.reivaxmc.progress.client.FoundationTransferScreen;
import fr.reivaxmc.progress.client.FragmentAltarScreen;
import fr.reivaxmc.progress.client.IntroCinematicScreen;
import fr.reivaxmc.progress.client.MatrixScreen;
import fr.reivaxmc.progress.client.MemorialScreen;
import fr.reivaxmc.progress.client.ReliquaryScreen;
import fr.reivaxmc.progress.client.V12RegistryScreen;
import fr.reivaxmc.progress.progression.CampaignSavedData;
import fr.reivaxmc.progress.progression.WorldStructures;
import fr.reivaxmc.progress.story.F8InteractionBridge;
import fr.reivaxmc.progress.story.F90SealGate;
import fr.reivaxmc.progress.story.F91FoyerChapterData;
import fr.reivaxmc.progress.story.F91FoyerChapterEngine;
import fr.reivaxmc.progress.story.F92JournalData;
import fr.reivaxmc.progress.story.F92FoyerBoundaryEngine;
import fr.reivaxmc.progress.story.F7NarrativeEngine;
import fr.reivaxmc.progress.story.C110TrailData;
import fr.reivaxmc.progress.story.C110TrailEngine;
import fr.reivaxmc.progress.story.V12Chapter3Engine;
import fr.reivaxmc.progress.story.V12TrailData;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class ProgressNetworking {
   private ProgressNetworking() {
   }

   public static void register(RegisterPayloadHandlersEvent e) {
      PayloadRegistrar r = e.registrar("16");
      r.playToClient(ProgressSyncPayload.TYPE, ProgressSyncPayload.CODEC, (p, c) -> c.enqueueWork(() -> ClientCampaignState.apply(p)));
      r.playToClient(
         SimplePayloads.StartIntro.TYPE,
         SimplePayloads.StartIntro.CODEC,
         (p, c) -> c.enqueueWork(() -> Minecraft.getInstance().setScreen(new IntroCinematicScreen()))
      );
      r.playToClient(
         SimplePayloads.OpenReliquary.TYPE,
         SimplePayloads.OpenReliquary.CODEC,
         (p, c) -> c.enqueueWork(() -> Minecraft.getInstance().setScreen(new ReliquaryScreen()))
      );
      r.playToClient(
         SimplePayloads.OpenFragment.TYPE,
         SimplePayloads.OpenFragment.CODEC,
         (p, c) -> c.enqueueWork(() -> Minecraft.getInstance().setScreen(new FragmentAltarScreen()))
      );
      r.playToClient(JournalPayloads.OpenJournal.TYPE, JournalPayloads.OpenJournal.CODEC,
         (p, c) -> c.enqueueWork(() -> Minecraft.getInstance().setScreen(new DestinyBookScreen(p.chronicle(), p.shared(), p.personal()))));
      r.playToClient(
         SimplePayloads.OpenMatrix.TYPE, SimplePayloads.OpenMatrix.CODEC, (p, c) -> c.enqueueWork(() -> Minecraft.getInstance().setScreen(new MatrixScreen()))
      );
      r.playToClient(
         PosPayloads.OpenFoundation.TYPE,
         PosPayloads.OpenFoundation.CODEC,
         (p, c) -> c.enqueueWork(() -> Minecraft.getInstance().setScreen(new FoundationPlacementScreen(p.x(), p.y(), p.z())))
      );
      r.playToClient(
         PosPayloads.OpenTransfer.TYPE,
         PosPayloads.OpenTransfer.CODEC,
         (p, c) -> c.enqueueWork(() -> Minecraft.getInstance().setScreen(new FoundationTransferScreen(p.x(), p.y(), p.z())))
      );
      r.playToClient(
         MemorialInfoPayload.TYPE, MemorialInfoPayload.CODEC, (p, c) -> c.enqueueWork(() -> Minecraft.getInstance().setScreen(new MemorialScreen(p.text())))
      );
      r.playToClient(V12Payloads.OpenRegistry.TYPE,V12Payloads.OpenRegistry.CODEC,(p,c)->c.enqueueWork(()->Minecraft.getInstance().setScreen(
         new V12RegistryScreen(p.stage(),p.perception(),p.census(),p.testimonies(),p.versions(),p.memory()))));
      r.playToServer(SimplePayloads.ClaimSeal.TYPE, SimplePayloads.ClaimSeal.CODEC, (p, c) -> c.enqueueWork(() -> {
            if (c.player() instanceof ServerPlayer sp) {
               claimSeal(sp);
            }
         }));
      r.playToServer(SimplePayloads.ClaimFragment.TYPE, SimplePayloads.ClaimFragment.CODEC, (p, c) -> c.enqueueWork(() -> {
            if (c.player() instanceof ServerPlayer sp) {
               claimFragment(sp);
            }
         }));
      r.playToServer(PosPayloads.ConfirmFoundation.TYPE, PosPayloads.ConfirmFoundation.CODEC, (p, c) -> c.enqueueWork(() -> {
            if (c.player() instanceof ServerPlayer sp) {
               confirmFoundation(sp, new BlockPos(p.x(), p.y(), p.z()));
            }
         }));
      r.playToServer(PosPayloads.ConfirmTransfer.TYPE, PosPayloads.ConfirmTransfer.CODEC, (p, c) -> c.enqueueWork(() -> {
            if (c.player() instanceof ServerPlayer sp) {
               confirmTransfer(sp, new BlockPos(p.x(), p.y(), p.z()));
            }
         }));
      r.playToServer(JournalPayloads.SaveNote.TYPE, JournalPayloads.SaveNote.CODEC, (p, c) -> c.enqueueWork(() -> {
            if (c.player() instanceof ServerPlayer sp) saveJournalNote(sp, p.text(), p.shared());
         }));
      r.playToServer(CivilizationPayloads.BuyUpgrade.TYPE, CivilizationPayloads.BuyUpgrade.CODEC, (p, c) -> c.enqueueWork(() -> {
            if (c.player() instanceof ServerPlayer sp) buyCivilizationUpgrade(sp, p.id());
         }));
      r.playToServer(CouncilPayloads.CastVote.TYPE, CouncilPayloads.CastVote.CODEC, (p, c) -> c.enqueueWork(() -> {
            if (c.player() instanceof ServerPlayer sp) F91FoyerChapterEngine.submitCouncilVote(sp, p.doctrine());
         }));
      r.playToServer(TrailPayloads.Follow.TYPE, TrailPayloads.Follow.CODEC, (p, c) -> c.enqueueWork(() -> {
            if (c.player() instanceof ServerPlayer sp) C110TrailEngine.follow(sp);
         }));
      r.playToServer(V12Payloads.Follow.TYPE,V12Payloads.Follow.CODEC,(p,c)->c.enqueueWork(()->{if(c.player() instanceof ServerPlayer sp)V12Chapter3Engine.follow(sp);}));
      r.playToServer(V12Payloads.Action.TYPE,V12Payloads.Action.CODEC,(p,c)->c.enqueueWork(()->{if(c.player() instanceof ServerPlayer sp){if("ACK".equals(p.action()))V12Chapter3Engine.acknowledge(sp);else if("VOTE".equals(p.action()))V12Chapter3Engine.vote(sp,p.value());}}));
      Alpha18FNetwork.register(e);
   }

   private static void claimSeal(ServerPlayer p) {
      MinecraftServer s = p.getServer();
      if (s != null) {
         CampaignSavedData d = CampaignSavedData.get(s);
         if (!d.reliquaryOpened() && p.blockPosition().closerThan(d.vestigePos(), 9.0)) {
            d.openReliquary();
            ItemStack st = new ItemStack((ItemLike)ReivaxMCProgress.ORIGIN_SEAL.get());
            F90SealGate.distributeSecondSeal(p);
            if (!p.getInventory().add(st)) {
               p.drop(st, false);
            }

            d.addTimeline(day(s), name(p), "Les Sceaux des Origines", name(p) + " a brisé le scellement du Reliquaire et récupéré le Sceau.");
            broadcast(
               s,
               d,
               "RELIQUARY",
               "SCEAUX DES ORIGINES",
               "Le scellement cède. Le Sceau repose désormais entre vos mains. Quelque chose en lui semble attendre d'être activé.",
               25
            );
         }
      }
   }

   private static void claimFragment(ServerPlayer p) {
      MinecraftServer s = p.getServer();
      if (s != null) {
         CampaignSavedData d = CampaignSavedData.get(s);
         if (!d.fragmentFound() && p.blockPosition().closerThan(d.altarPos(), 9.0)) {
            d.markFragmentFound();
            d.complete("FIRST_FRAGMENT", 70, 25);
            d.addArtifact(
               new CampaignSavedData.ArtifactRecord("fragment_01", "Éclat inconnu", name(p), p.getUUID().toString(), day(s), "Stèle brisée", "Inconnue")
            );
            d.addTimeline(day(s), name(p), "Premier Fragment", name(p) + " a retiré un Fragment inconnu de son réceptacle ancien.");
            ItemStack f = new ItemStack((ItemLike)ReivaxMCProgress.UNKNOWN_FRAGMENT.get());
            if (!p.getInventory().add(f)) {
               p.drop(f, false);
            }

            giveBeacon(p);
            p.serverLevel().playSound(null, p.blockPosition(), SoundEvents.RESPAWN_ANCHOR_CHARGE, SoundSource.PLAYERS, 1.0F, 1.1F);
            WorldStructures.revealMatrix(p.serverLevel(), d);
            broadcast(
               s,
               d,
               "FOUNDATION_GUIDE",
               "NOUVEL OBJET — BORNE DE FONDATION",
               "Le Fragment a réagi… et vous avez reçu une BORNE DE FONDATION.\n\nElle sert à définir votre FOYER PRINCIPAL : l'endroit où vous choisissez d'établir votre base.\n\nPlacez-la à l'emplacement choisi. Les 96 blocs autour deviendront votre TERRITOIRE PRINCIPAL.\n\nLa Résonance persiste encore dans cette chambre…",
               70
            );
            p.displayClientMessage(
               Component.literal("§6BORNE DE FONDATION reçue §7— choisissez votre base puis placez-la pour fonder votre Foyer principal."), false
            );
         }
      }
   }

   private static void confirmFoundation(ServerPlayer var0, BlockPos var1) {
      F8InteractionBridge.confirmFoundation(var0, var1);
   }

   private static void confirmTransfer(ServerPlayer p, BlockPos pos) {
      MinecraftServer s = p.getServer();
      if (s != null && p.blockPosition().closerThan(pos, 9.0)) {
         CampaignSavedData d = CampaignSavedData.get(s);
         if (d.foundationPlaced() && d.foundationPos().equals(pos) && p.serverLevel().getBlockState(pos).is((Block)ReivaxMCProgress.FOUNDATION_BEACON.get())) {
            String old = d.foundationName();
            CampaignSavedData.HistoricalSite h = d.abandonSettlement(day(s));
            p.serverLevel().setBlock(pos, ((Block)ReivaxMCProgress.MEMORIAL_PLAQUE.get()).defaultBlockState(), 3);
            giveBeacon(p);
            d.addTimeline(day(s), name(p), "Le Grand Départ", old + " est devenu un Site historique. La civilisation est désormais en migration.");
            broadcast(
               s,
               d,
               "MIGRATION",
               "SITE HISTORIQUE CRÉÉ",
               old
                  + " appartient désormais à l'histoire. Ce territoire ne pourra plus jamais redevenir votre Foyer principal. Trouvez un nouvel emplacement pour la Borne.",
               0
            );
         }
      }
   }

   private static boolean consumeBeacon(ServerPlayer p) {
      for (int i = 0; i < p.getInventory().getContainerSize(); i++) {
         ItemStack s = p.getInventory().getItem(i);
         if (s.is((Item)ReivaxMCProgress.FOUNDATION_BEACON_ITEM.get())) {
            s.shrink(1);
            return true;
         }
      }

      return p.isCreative();
   }

   private static void giveBeacon(ServerPlayer p) {
      ItemStack b = new ItemStack((ItemLike)ReivaxMCProgress.FOUNDATION_BEACON_ITEM.get());
      if (!p.getInventory().add(b)) {
         p.drop(b, false);
      }
   }

   private static String mission(CampaignSavedData d) {
      String var1 = d.stage();

      return switch (var1) {
         case "DORMANT" -> "Inspectez le Sanctuaire de l'Éveil et son Reliquaire.";
         case "SURVIVE_FIRST_NIGHT" -> "La nuit approche. Tenez jusqu'au lever du jour et observez ce monde : peut-être quelque chose répondra-t-il à votre présence.";
         case "FOLLOW_RESONANCE" -> "Une trace du passé a répondu à l'Éveil. Suivez la Résonance.";
         case "SEARCH_STELE" -> "Explorez le site de la Stèle brisée et trouvez l'origine de la Résonance.";
         case "FIND_MATRIX" -> "Le Fragment est à vous, mais la Résonance persiste dans la chambre…";
         case "BUILD_FIRST_HOME" -> "Choisissez votre base et placez la Borne de Fondation pour établir votre Foyer principal.";
         case "MIGRATION" -> "Votre civilisation est en migration. Replacez la Borne hors de tout Site historique.";
         case "CH1_SHAPE_FOYER" -> "Donnez une forme au Foyer avec trois repères de vie.";
         case "CH1_CHOOSE_PRIORITY" -> "Ouvrez le Conseil de la Borne et choisissez ce qui doit survivre en premier.";
         case "CH1_FIND_ECHO" -> "Suivez l'Écho apparu autour du Foyer.";
         case "CH1_RETURN_FRAGMENT" -> "Conservez le Fragment et revenez au cœur du Foyer ; seule la Matrice pourra l’analyser.";
         case "CH1_DEFEND_FOYER" -> "Défendez le Foyer contre ce que la mémoire a réveillé.";
         case "CH1_COMPLETE" -> "Le Foyer emprunté a livré sa mémoire. Développez librement votre civilisation.";
         case "CH2_OFFERED" -> "Une nouvelle Piste attend dans la Borne : La Dette du Foyer.";
         case "CH2_SEEK_RIFTS" -> "Suivez les Fêlures autour du Foyer. Deux concordances suffiront.";
         case "CH2_RETURN_FOYER" -> "Deux Fêlures concordent. Revenez à la Borne.";
         case "CH2_CENSUS" -> "Le Recensement a commencé. Approchez trois Veilleurs.";
         case "CH2_RETURN_SANCTUARY" -> "Gardez le Fragment et retournez au Sanctuaire.";
         case "CH2_REGISTRY" -> "La salle orientale est ouverte. Consultez le Registre des Absents.";
         case "CH2_COMPLETE" -> "La Dette du Foyer est inscrite. Le Sanctuaire garde encore des salles closes.";
         case "CH3_OFFERED" -> "Une nouvelle Piste attend dans la Borne : Les Noms retirés.";
         case "CH3_EIGHTH_LINE" -> "Consultez la huitième ligne du Registre des Absents.";
         case "CH3_COMPLETE" -> "Les Noms retirés sont inscrits. Votre politique de mémoire demeure.";
         default -> "Votre Premier Foyer est établi. Développez votre civilisation.";
      };
   }

   private static ProgressSyncPayload packet(ServerPlayer p, CampaignSavedData d, String event, String kind, String title, String detail, int gained) {
      BlockPos t = BlockPos.ZERO;
      if (d.stage().equals("FOLLOW_RESONANCE") || d.stage().equals("SEARCH_STELE")) {
         t = d.stelaPos();
      } else if (d.fragmentFound() && !d.matrixDiscovered()) {
         t = d.matrixPos();
      }

      return new ProgressSyncPayload(
         d.progress(),
         d.score(),
         d.introCompleted(),
         t.getX(),
         t.getY(),
         t.getZ(),
         d.stage(),
         mission(d),
         d.timelinePacket(),
         d.artifactsPacket(),
         event,
         kind,
         title,
         detail,
         gained,
         d.reliquaryOpened(),
         d.matrixDiscovered(),
         d.foundationPlaced(),
         d.migration(),
         d.foundationName(),
         d.territoryRadius(),
         d.historicalSites().size()
      );
   }

   public static void sync(ServerPlayer p, CampaignSavedData d) {
      PacketDistributor.sendToPlayer(p, packet(p, d, "", "", "", "", 0), new CustomPacketPayload[0]);
   }

   public static void syncAll(MinecraftServer s, CampaignSavedData d) {
      for (ServerPlayer p : s.getPlayerList().getPlayers()) {
         sync(p, d);
      }
   }

   public static void broadcast(MinecraftServer s, CampaignSavedData d, String event, String title, String detail, int gained) {
      broadcast(s, d, event, "STORY", title, detail, gained);
   }

   public static void broadcast(MinecraftServer s, CampaignSavedData d, String event, String kind, String title, String detail, int gained) {
      for (ServerPlayer p : s.getPlayerList().getPlayers()) {
         PacketDistributor.sendToPlayer(p, packet(p, d, event, kind, title, detail, gained), new CustomPacketPayload[0]);
      }
   }

   public static void openReliquary(ServerPlayer p) {
      PacketDistributor.sendToPlayer(p, new SimplePayloads.OpenReliquary(), new CustomPacketPayload[0]);
   }

   public static void openFragment(ServerPlayer p) {
      PacketDistributor.sendToPlayer(p, new SimplePayloads.OpenFragment(), new CustomPacketPayload[0]);
   }

   public static void openBook(ServerPlayer p) {
      MinecraftServer server = p.getServer();
      if (server == null) return;
      CampaignSavedData campaign = CampaignSavedData.get(server);
      F92JournalData journal = F92JournalData.get(server);
      PacketDistributor.sendToPlayer(p, new JournalPayloads.OpenJournal(campaign.timelinePacket(), journal.sharedPacket(),
         journal.personalPacket(p.getUUID().toString())), new CustomPacketPayload[0]);
   }

   private static void saveJournalNote(ServerPlayer player, String text, boolean shared) {
      MinecraftServer server = player.getServer();
      if (server == null || text == null || text.isBlank() || text.length() > 180) return;
      if (!player.getInventory().contains(new ItemStack((Item)ReivaxMCProgress.DESTINY_BOOK.get()))) return;
      F92JournalData journal = F92JournalData.get(server);
      boolean saved = shared
         ? journal.addShared(day(server), name(player), text)
         : journal.addPersonal(player.getUUID().toString(), text);
      if (saved) {
         player.displayClientMessage(Component.literal(shared ? "§6JOURNAL §8• §fPage partagée avec le Foyer." : "§6JOURNAL §8• §fNote conservée dans vos pages personnelles."), false);
         openBook(player);
      }
   }

   private static void buyCivilizationUpgrade(ServerPlayer player, String id) {
      MinecraftServer server = player.getServer();
      if (server == null) return;
      CampaignSavedData campaign = CampaignSavedData.get(server);
      if (!campaign.foundationPlaced() || !player.serverLevel().dimension().location().toString().equals(campaign.foundationDimension())
         || player.blockPosition().distSqr(campaign.foundationPos()) > 144.0) {
         player.displayClientMessage(Component.literal("§6BORNE §8• §fRevenez près de la Borne pour engager les points de Civilisation."), false);
         return;
      }
      int cost;
      String title;
      if ("LISIERE_ACCORDEE".equals(id)) {
         cost = 12;
         title = "Lisière accordée";
      } else if ("ANCRAGE_ETENDU".equals(id)) {
         cost = 25;
         title = "Ancrage étendu";
      } else if ("CARTOGRAPHIE_RESONANTE".equals(id)) {
         cost = 18;
         title = "Cartographie résonante";
      } else if ("VEILLE_LISIERE".equals(id)) {
         cost = 22;
         title = "Veille de la lisière";
      } else if ("TABLE_COMMUNE".equals(id)) {
         cost = 20;
         title = "Table commune";
      } else return;
      if (!campaign.buyCivilizationUpgrade(id, cost)) {
         player.displayClientMessage(Component.literal("§6CIVILISATION §8• §fAmélioration déjà acquise ou solde insuffisant."), false);
         openFoyerPanel(player);
         return;
      }
      campaign.addTimeline(day(server), name(player), title, name(player) + " a engagé " + cost + " points de Civilisation à la Borne.");
      broadcast(server, campaign, "CIV_UPGRADE_" + id + "|GROUP||CIV=0", "CIVILISATION", title.toUpperCase(),
         "La décision est inscrite dans le Foyer. Solde disponible : " + campaign.civilizationAvailable() + ".", 0);
      F92FoyerBoundaryEngine.reveal(player);
      syncAll(server, campaign);
      openFoyerPanel(player);
   }

   public static void openFoyerPanel(ServerPlayer player) {
      MinecraftServer server = player.getServer();
      if (server == null) return;
      CampaignSavedData campaign = CampaignSavedData.get(server);
      F91FoyerChapterData.Snapshot chapter = F91FoyerChapterData.get(server).snapshot();
      BlockPos pos = campaign.foundationPos();
      String data = campaign.foundationName() + "|" + campaign.territoryRadius() + "|" + campaign.foundationFounder() + "|"
         + campaign.foundationDay() + "|" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + "|"
         + campaign.civilizationTotal() + "|" + campaign.civilizationAvailable() + "|" + campaign.civilizationUpgradesPacket()
         + "|" + chapter.stage() + "|" + chapter.doctrine() + "|" + councilRoster(server, chapter)
         + "|" + C110TrailEngine.packet(C110TrailData.get(server).snapshot())
         + "|" + V12Chapter3Engine.packet(V12TrailData.get(server).snapshot());
      F7NarrativeEngine.pushUi(player, "F8_FOYER_PANEL", data);
   }

   /** Rafraîchit le même Conseil sur tous les écrans après chaque vote. */
   public static void refreshCouncilPanels(MinecraftServer server) {
      for (ServerPlayer member : server.getPlayerList().getPlayers()) openFoyerPanel(member);
   }

   private static String councilRoster(MinecraftServer server, F91FoyerChapterData.Snapshot chapter) {
      StringBuilder packet = new StringBuilder();
      java.util.Set<String> voters = chapter.councilVoters().isEmpty()
         ? server.getPlayerList().getPlayers().stream().map(player -> player.getUUID().toString()).collect(java.util.stream.Collectors.toSet())
         : chapter.councilVoters();
      for (String uuid : voters) {
         ServerPlayer online = server.getPlayerList().getPlayers().stream()
            .filter(player -> player.getUUID().toString().equals(uuid)).findFirst().orElse(null);
         if (packet.length() > 0) packet.append(';');
         packet.append(online == null ? "Membre absent" : online.getGameProfile().getName())
            .append('~').append(chapter.councilVotes().getOrDefault(uuid, ""));
      }
      return packet.toString();
   }

   public static void openMatrix(ServerPlayer p) {
      PacketDistributor.sendToPlayer(p, new SimplePayloads.OpenMatrix(), new CustomPacketPayload[0]);
   }

   public static void openFoundationPlacement(ServerPlayer p, BlockPos b) {
      PacketDistributor.sendToPlayer(p, new PosPayloads.OpenFoundation(b.getX(), b.getY(), b.getZ()), new CustomPacketPayload[0]);
   }

   public static void openTransfer(ServerPlayer p, BlockPos b) {
      PacketDistributor.sendToPlayer(p, new PosPayloads.OpenTransfer(b.getX(), b.getY(), b.getZ()), new CustomPacketPayload[0]);
   }

   public static void memorial(ServerPlayer p, CampaignSavedData.HistoricalSite h) {
      String t = "ICI SE TROUVAIT "
         + h.name().toUpperCase()
         + "\n\nC'est ici qu'une étape de votre civilisation prit racine.\n\nFondé par : "
         + h.founder()
         + "\nFondation : Jour "
         + h.foundedDay()
         + "\nAbandon : Jour "
         + h.abandonedDay()
         + "\nDurée : "
         + Math.max(0, h.abandonedDay() - h.foundedDay())
         + " jours\nÈre : Origines\n\nCe territoire appartient désormais définitivement à l'Histoire.";
      PacketDistributor.sendToPlayer(p, new MemorialInfoPayload(t), new CustomPacketPayload[0]);
   }

   private static String name(ServerPlayer p) {
      return p.getGameProfile().getName();
   }

   public static int day(MinecraftServer s) {
      return (int)(s.overworld().getDayTime() / 24000L) + 1;
   }
}
