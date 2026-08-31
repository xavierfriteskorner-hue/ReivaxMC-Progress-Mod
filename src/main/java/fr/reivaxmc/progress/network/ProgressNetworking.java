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
import fr.reivaxmc.progress.progression.CampaignSavedData;
import fr.reivaxmc.progress.progression.WorldStructures;
import fr.reivaxmc.progress.story.F8InteractionBridge;
import fr.reivaxmc.progress.story.F90SealGate;
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
      PayloadRegistrar r = e.registrar("13");
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
      r.playToClient(
         SimplePayloads.OpenBook.TYPE, SimplePayloads.OpenBook.CODEC, (p, c) -> c.enqueueWork(() -> Minecraft.getInstance().setScreen(new DestinyBookScreen()))
      );
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
      PacketDistributor.sendToPlayer(p, new SimplePayloads.OpenBook(), new CustomPacketPayload[0]);
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
