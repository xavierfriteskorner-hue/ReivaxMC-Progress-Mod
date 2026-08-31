package fr.reivaxmc.progress.story;

import fr.reivaxmc.progress.block.Alpha18FContent;
import fr.reivaxmc.progress.network.Alpha18FPayloads;
import fr.reivaxmc.progress.network.CompatPacketSender18F;
import fr.reivaxmc.progress.network.SimplePayloads;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.levelgen.Heightmap.Types;

public final class StoryOpening18F {
   public static final long TRACE_DELAY_TICKS = 520L;
   public static final double DUO_RADIUS = 32.0;

   private StoryOpening18F() {
   }

   public static void requestStart(ServerPlayer var0) {
      try {
         MinecraftServer var1 = var0.getServer();
         if (var1 == null) {
            return;
         }

         StoryStartStateData18F var2 = StoryModeGate18F.state(var1);
         StoryStartStateData18F.Snapshot var3 = var2.snapshot();
         if (!var3.managed()) {
            message(var0, "§cCe monde contient déjà une campagne Alpha 17 avancée. Le nouveau départ est désactivé ici.", false);
            return;
         }

         if (var3.started()) {
            message(var0, "§7L'histoire a déjà commencé dans ce monde.", false);
            sendStatus(var0, var2);
            return;
         }

         List<ServerPlayer> var4 = participants(var1, var0);
         List var5 = var1.getPlayerList().getPlayers();
         if (var5.size() >= 2 && var4.size() < 2) {
            message(var0, "§6REIVAX §7— rapprochez-vous de Laeriss (moins de 32 blocs) avant de commencer l'histoire.", false);
            return;
         }

         ServerLevel var6 = var1.overworld();
         int[] var7 = targetPosition(var6, var0);
         String var8 = var0.getUUID().toString();
         String var9 = playerName(var0);
         long var10 = var6.getGameTime();
         if (!var2.markStarted(var10, var8, var9, var7[0], var7[1], var7[2])) {
            return;
         }

         for (ServerPlayer var13 : var4) {
            sendStatus(var13, var2);
            CompatPacketSender18F.sendToPlayer(var13, new SimplePayloads.StartIntro());
            if (Alpha18FContent.OPENING_SOUND != null) {
               var13.playNotifySound(Alpha18FContent.OPENING_SOUND, SoundSource.MASTER, 0.82F, 1.0F);
            }
         }

         StoryFact var16 = StoryFact.identified(
            "AGE1_OPENING:START", "AGE1_STORY_STARTED", "AGE1.OPENING.START", var8, Map.of("mode", "18F6", "participants", Integer.toString(var4.size()))
         );
         CampaignRuntimeContext.withServer(var1, () -> StoryRuntime.publish(var16));
         System.out.println("[REIVAX Alpha18F.6] Story started by " + var9 + " participants=" + var4.size());
      } catch (Throwable var15) {
         System.err.println("[REIVAX Alpha18F.6] start failed: " + var15);

         try {
            message(var0, "§cREIVAX — impossible de démarrer l'histoire. Consulte latest.log.", false);
         } catch (Throwable var14) {
         }
      }
   }

   public static void tick(MinecraftServer var0) {
      F7NarrativeEngine.tick(var0);

      try {
         StoryStartStateData18F var1 = StoryModeGate18F.state(var0);
         StoryStartStateData18F.Snapshot var2 = var1.snapshot();
         TracePlacementCompat18F.ensureGiantTrace(var0, var2, Alpha18FContent.TRACE_BLOCK);
         if (!var2.managed() || !var2.started()) {
            return;
         }

         ServerLevel var3 = var0.overworld();
         long var4 = var3.getGameTime() - var2.startTick();
         if (!var2.tracePlaced() && var4 >= 520L) {
            placeTrace(var0, var1, var2);
            var2 = var1.snapshot();
         }

         if (!var2.tracePlaced()) {
            return;
         }

         if (var0.getTickCount() % 10 == 0) {
            double var6 = (double)var2.traceX() + 0.5;
            double var8 = (double)var2.traceY() + 1.15;
            double var10 = (double)var2.traceZ() + 0.5;
            var3.sendParticles(ParticleTypes.WHITE_ASH, var6, var8, var10, var2.traceExamined() ? 4 : 2, 0.72, 0.95, 0.72, 0.004);
         }

         if (var0.getTickCount() % 24 == 0) {
            double var13 = (double)var2.traceX() + 0.5;
            double var15 = (double)var2.traceY() + 1.25;
            double var16 = (double)var2.traceZ() + 0.5;
            var3.sendParticles(ParticleTypes.ELECTRIC_SPARK, var13, var15, var16, var2.traceExamined() ? 3 : 1, 0.48, 0.82, 0.48, 0.018);
         }

         if (var0.getTickCount() % 40 == 0) {
            for (ServerPlayer var7 : var0.getPlayerList().getPlayers()) {
               if (!var2.traceExamined()) {
                  message(var7, "§6OBJECTIF PRINCIPAL §8— §fExaminez la Trace §8· §fCLIC DROIT §7pour interagir", true);
               } else {
                  message(var7, F7NarrativeEngine.legacyObjective(), true);
               }
            }
         }
      } catch (Throwable var12) {
         System.err.println("[REIVAX Alpha18F.6] tick failed: " + var12);
      }
   }

   public static void sendStatus(ServerPlayer var0, StoryStartStateData18F var1) {
      StoryStartStateData18F.Snapshot var2 = var1.snapshot();
      CompatPacketSender18F.sendToPlayer(var0, new Alpha18FPayloads.StoryStatus(var2.managed(), var2.managed() && !var2.started(), var2.started()));
   }

   private static void placeTrace(MinecraftServer var0, StoryStartStateData18F var1, StoryStartStateData18F.Snapshot var2) {
      ServerLevel var3 = var0.overworld();
      int var4 = var2.traceX();
      int var5 = var2.traceZ();
      int var6 = var3.getHeight(Types.MOTION_BLOCKING_NO_LEAVES, var4, var5);
      if (Math.abs(var6 - var2.traceY()) > 12) {
         var6 = var2.traceY();
      }

      BlockPos var7 = new BlockPos(var4, var6, var5);
      if (Alpha18FContent.TRACE_BLOCK == null) {
         throw new IllegalStateException("story_trace block not registered");
      } else {
         var3.setBlock(var7, Alpha18FContent.TRACE_BLOCK.defaultBlockState(), 3);
         TracePlacementCompat18F.buildGiantTrace(var3, var7, Alpha18FContent.TRACE_BLOCK);
         var1.markTracePlaced(var4, var6, var5);
         var3.sendParticles(ParticleTypes.WHITE_ASH, (double)var4 + 0.5, (double)var6 + 1.0, (double)var5 + 0.5, 34, 1.35, 1.45, 1.35, 0.015);
         var3.sendParticles(ParticleTypes.ELECTRIC_SPARK, (double)var4 + 0.5, (double)var6 + 1.15, (double)var5 + 0.5, 18, 0.85, 1.15, 0.85, 0.045);
         String var8 = var2.startedBy() != null && !var2.startedBy().isBlank() ? var2.startedBy() : "WORLD";
         StoryFact var9 = StoryFact.identified(
            "AGE1_OPENING:TRACE_APPEARED",
            "AGE1_TRACE_APPEARED",
            "AGE1.OPENING.TRACE",
            var8,
            Map.of("x", Integer.toString(var4), "y", Integer.toString(var6), "z", Integer.toString(var5))
         );
         CampaignRuntimeContext.withServer(var0, () -> StoryRuntime.publish(var9));

         for (ServerPlayer var11 : var0.getPlayerList().getPlayers()) {
            if (Alpha18FContent.TRACE_APPEAR_SOUND != null) {
               var11.playNotifySound(Alpha18FContent.TRACE_APPEAR_SOUND, SoundSource.MASTER, 0.95F, 1.0F);
            }

            message(var11, "§6OBJECTIF PRINCIPAL §8— §fExaminez la Trace inconnue.", false);
            message(var11, "§7Approchez-vous puis faites §fCLIC DROIT §7sur elle pour interagir.", false);
            message(var11, "§6OBJECTIF PRINCIPAL §8— §fExaminez la Trace §8· §fCLIC DROIT §7pour interagir", true);
         }

         System.out.println("[REIVAX Alpha18F.6] Trace placed at " + var4 + "," + var6 + "," + var5);
      }
   }

   private static int[] targetPosition(ServerLevel var0, ServerPlayer var1) {
      return TracePlacementCompat18F.findLandTarget(var0, var1);
   }

   private static List<ServerPlayer> participants(MinecraftServer var0, ServerPlayer var1) {
      ArrayList var2 = new ArrayList();

      for (ServerPlayer var4 : var0.getPlayerList().getPlayers()) {
         if (distanceSq(var4, var1) <= 1024.0) {
            var2.add(var4);
         }
      }

      if (var2.isEmpty()) {
         var2.add(var1);
      }

      return var2;
   }

   private static double distanceSq(ServerPlayer var0, ServerPlayer var1) {
      double var2 = var0.getX() - var1.getX();
      double var4 = var0.getY() - var1.getY();
      double var6 = var0.getZ() - var1.getZ();
      return var2 * var2 + var4 * var4 + var6 * var6;
   }

   private static String playerName(ServerPlayer var0) {
      try {
         return var0.getGameProfile().getName();
      } catch (Throwable var2) {
         return var0.getUUID().toString();
      }
   }

   public static void message(ServerPlayer var0, String var1, boolean var2) {
      F7NarrativeEngine.routeStoryMessage(var0, var1, var2);
   }
}
