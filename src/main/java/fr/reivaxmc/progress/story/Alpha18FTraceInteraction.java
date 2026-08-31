package fr.reivaxmc.progress.story;

import fr.reivaxmc.progress.block.Alpha18FContent;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.RightClickBlock;

@EventBusSubscriber(
   modid = "reivaxmc_progress"
)
public final class Alpha18FTraceInteraction {
   private Alpha18FTraceInteraction() {
   }

   @SubscribeEvent
   public static void rightClick(RightClickBlock var0) {
      if (var0.getEntity() instanceof ServerPlayer var1) {
         if (var0.getLevel() instanceof ServerLevel var15) {
            BlockPos var16 = var0.getPos();
            if (Alpha18FContent.TRACE_BLOCK != null && var15.getBlockState(var16).getBlock() == Alpha18FContent.TRACE_BLOCK) {
               MinecraftServer var4 = var1.getServer();
               if (var4 != null) {
                  StoryStartStateData18F var5 = StoryModeGate18F.state(var4);
                  StoryStartStateData18F.Snapshot var6 = var5.snapshot();
                  if (var6.managed() && var6.started() && var6.tracePlaced()) {
                     var0.setCancellationResult(InteractionResult.SUCCESS);
                     var0.setCanceled(true);
                     if (var6.traceExamined()) {
                        StoryOpening18F.message(var1, "§7La Trace continue de vibrer sous votre main.", true);
                     } else if (var5.markTraceExamined(var15.getGameTime(), var1.getUUID().toString())) {
                        double var7 = (double)var16.getX() + 0.5;
                        double var9 = (double)var16.getY() + 1.15;
                        double var11 = (double)var16.getZ() + 0.5;
                        var15.sendParticles(ParticleTypes.ELECTRIC_SPARK, var7, var9, var11, 42, 1.0, 1.35, 1.0, 0.085);
                        var15.sendParticles(ParticleTypes.END_ROD, var7, var9, var11, 20, 0.62, 1.05, 0.62, 0.035);
                        var15.sendParticles(ParticleTypes.WHITE_ASH, var7, var9, var11, 26, 1.25, 1.35, 1.25, 0.018);

                        for (ServerPlayer var14 : var4.getPlayerList().getPlayers()) {
                           if (Alpha18FContent.TRACE_INTERACT_SOUND != null) {
                              var14.playNotifySound(Alpha18FContent.TRACE_INTERACT_SOUND, SoundSource.MASTER, 0.95F, 1.0F);
                           }

                           StoryOpening18F.message(var14, "§6TRACE §8• §fLe contact a déclenché une réaction.", false);
                           StoryOpening18F.message(var14, "§6OBJECTIF MIS À JOUR §8• §fRestez près de la Trace. Ne vous éloignez pas.", false);
                           StoryOpening18F.message(var14, "§6OBJECTIF PRINCIPAL §8• §fRestez près de la Trace · observez ce qui se produit.", true);
                        }

                        StoryFact var17 = StoryFact.identified(
                           "AGE1_OPENING:TRACE_EXAMINED",
                           "AGE1_TRACE_EXAMINED",
                           "AGE1.OPENING.TRACE.EXAMINED",
                           var1.getUUID().toString(),
                           Map.of("subject", "story_trace", "position", var16.getX() + "," + var16.getY() + "," + var16.getZ())
                        );
                        CampaignRuntimeContext.withServer(var4, () -> StoryRuntime.publish(var17));
                        System.out.println("[REIVAX Alpha18F.6] Trace examined by " + var1.getGameProfile().getName());
                     }
                  }
               }
            }
         }
      }
   }
}
