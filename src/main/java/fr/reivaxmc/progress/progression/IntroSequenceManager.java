package fr.reivaxmc.progress.progression;

import fr.reivaxmc.progress.ReivaxMCProgress;
import fr.reivaxmc.progress.network.ProgressNetworking;
import fr.reivaxmc.progress.network.SimplePayloads;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.network.PacketDistributor;

public final class IntroSequenceManager {
   private static final Map<UUID, IntroSequenceManager.Frozen> FROZEN = new HashMap<>();
   private static MinecraftServer activeServer;

   private IntroSequenceManager() {
   }

   public static MinecraftServer server() {
      return activeServer;
   }

   public static void remember(MinecraftServer s) {
      activeServer = s;
   }

   public static void requestStart(ServerPlayer p) {
      MinecraftServer s = p.getServer();
      if (s != null) {
         remember(s);
         CampaignSavedData d = CampaignSavedData.get(s);
         if (!d.introCompleted() && !d.introRunning()) {
            int slot = -1;

            for (int i = 0; i < p.getInventory().getContainerSize(); i++) {
               if (p.getInventory().getItem(i).is((Item)ReivaxMCProgress.ORIGIN_SEAL.get())) {
                  slot = i;
                  break;
               }
            }

            if (slot >= 0) {
               ItemStack st = p.getInventory().getItem(slot);
               st.shrink(1);
               long now = s.overworld().getGameTime();
               d.startIntro(now + 340L);
               d.addTimeline(
                  ProgressNetworking.day(s),
                  p.getGameProfile().getName(),
                  "L'Éveil",
                  p.getGameProfile().getName() + " a activé le Sceau des Origines. Quelque chose s'est réveillé dans le monde."
               );

               for (ServerPlayer q : s.getPlayerList().getPlayers()) {
                  FROZEN.put(q.getUUID(), new IntroSequenceManager.Frozen(q.getX(), q.getY(), q.getZ(), q.getYRot(), q.getXRot()));
                  PacketDistributor.sendToPlayer(q, new SimplePayloads.StartIntro(), new CustomPacketPayload[0]);
               }
            }
         }
      }
   }

   public static void tick(MinecraftServer s) {
      remember(s);
      CampaignSavedData d = CampaignSavedData.get(s);
      if (d.introRunning()) {
         long now = s.overworld().getGameTime();
         long left = d.introEndTick() - now;

         for (ServerPlayer p : s.getPlayerList().getPlayers()) {
            IntroSequenceManager.Frozen f = FROZEN.computeIfAbsent(
               p.getUUID(), u -> new IntroSequenceManager.Frozen(p.getX(), p.getY(), p.getZ(), p.getYRot(), p.getXRot())
            );
            p.teleportTo(f.x, f.y, f.z);
            p.setYRot(f.yaw);
            p.setXRot(f.pitch);
            p.setDeltaMovement(0.0, 0.0, 0.0);
         }

         if (left == 315L || left == 245L || left == 175L || left == 85L) {
            lightning(s.overworld(), d.vestigePos());
         }

         if (now >= d.introEndTick()) {
            d.finishIntro(now);
            FROZEN.clear();

            for (ServerPlayer p : s.getPlayerList().getPlayers()) {
               giveBook(p, d);
            }

            ProgressNetworking.broadcast(
               s,
               d,
               "AWAKENING",
               "RETROUVEZ VOS ORIGINES",
               "Vous ignorez qui vous êtes, d'où vous venez et pourquoi ce monde porte déjà vos traces. Survivez. Observez. Retrouvez vos Origines.",
               40
            );
         }
      }
   }

   private static void lightning(ServerLevel l, BlockPos c) {
      for (int i = 0; i < 6; i++) {
         LightningBolt bolt = (LightningBolt)EntityType.LIGHTNING_BOLT.create(l);
         if (bolt != null) {
            bolt.moveTo((double)(c.getX() + l.random.nextInt(31) - 15), (double)(c.getY() + 8), (double)(c.getZ() + l.random.nextInt(31) - 15));
            bolt.setVisualOnly(true);
            l.addFreshEntity(bolt);
         }
      }
   }

   public static void giveBook(ServerPlayer p, CampaignSavedData d) {
      if (!d.hasBook(p.getUUID())) {
         ItemStack b = new ItemStack((ItemLike)ReivaxMCProgress.DESTINY_BOOK.get());
         if (!p.getInventory().add(b)) {
            p.drop(b, false);
         }

         d.markBook(p.getUUID());
      }
   }

   private static record Frozen(double x, double y, double z, float yaw, float pitch) {
   }
}
