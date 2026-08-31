package fr.reivaxmc.progress.story;

import fr.reivaxmc.progress.block.Alpha18FContent;
import fr.reivaxmc.progress.network.CompatPacketSender18F;
import fr.reivaxmc.progress.network.SimplePayloads;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent.Post;

@EventBusSubscriber(
   modid = "reivaxmc_progress"
)
public final class Alpha18FOpeningEvents {
   private Alpha18FOpeningEvents() {
   }

   @SubscribeEvent
   public static void login(PlayerLoggedInEvent var0) {
      try {
         if (!(Alpha18Probe.invokeNoArg(var0, "getEntity") instanceof ServerPlayer var2)) {
            return;
         }

         MinecraftServer var3 = var2.getServer();
         if (var3 == null) {
            return;
         }

         StoryStartStateData18F var4 = StoryModeGate18F.state(var3);
         StoryStartStateData18F.Snapshot var5 = var4.snapshot();
         if (var5.managed() && var5.started() && !var5.tracePlaced()) {
            var4.restartInterruptedIntro(var3.overworld().getGameTime());
            var5 = var4.snapshot();
            StoryOpening18F.sendStatus(var2, var4);
            CompatPacketSender18F.sendToPlayer(var2, new SimplePayloads.StartIntro());
            if (Alpha18FContent.OPENING_SOUND != null) {
               var2.playNotifySound(Alpha18FContent.OPENING_SOUND, SoundSource.MASTER, 0.82F, 1.0F);
            }

            Alpha18Probe.sendSystemMessage(var2, "§6[REIVAX Alpha 18F.6.7] §eIntroduction reprise après interruption.");
         } else {
            StoryOpening18F.sendStatus(var2, var4);
         }

         if (var5.managed() && !var5.started()) {
            Alpha18Probe.sendSystemMessage(
               var2, "§6[REIVAX Alpha 18F.6.7] §aMODE HISTOIRE prêt §7— appuyez sur §f3 du pavé numérique §7pour ouvrir REIVAX et lancer l'histoire."
            );
         } else if (var5.managed() && var5.tracePlaced() && !var5.traceExamined()) {
            Alpha18Probe.sendSystemMessage(var2, "§6[REIVAX Alpha 18F.6.7] §aMODE HISTOIRE actif §7— approchez-vous de la Trace et faites §fCLIC DROIT§7.");
         } else if (var5.managed() && var5.traceExamined()) {
            Alpha18Probe.sendSystemMessage(var2, "§6[REIVAX Alpha 18F.6.7] §aTrace examinée §7— état retrouvé depuis le monde.");
         }
      } catch (Throwable var6) {
         System.err.println("[REIVAX Alpha18F.6.7] login status failed: " + var6);
      }
   }

   @SubscribeEvent
   public static void serverTick(Post var0) {
      MinecraftServer var1 = var0.getServer();
      if (var1 != null) {
         StoryOpening18F.tick(var1);
         if (var1.getTickCount() % 40 == 0) {
            try {
               StoryStartStateData18F var2 = StoryModeGate18F.state(var1);

               for (ServerPlayer var4 : var1.getPlayerList().getPlayers()) {
                  StoryOpening18F.sendStatus(var4, var2);
               }
            } catch (Throwable var5) {
               System.err.println("[REIVAX Alpha18F.6.7] periodic status sync failed: " + var5);
            }
         }
      }
   }
}
