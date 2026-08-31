package fr.reivaxmc.progress.client;

import com.mojang.blaze3d.platform.InputConstants.Type;
import fr.reivaxmc.progress.narrator.CompactHudHook;
import fr.reivaxmc.progress.narrator.NarratorClientHook;
import fr.reivaxmc.progress.network.ClientCampaignState;
import fr.reivaxmc.progress.story.Alpha18FClientState;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent.Post;

@EventBusSubscriber(
   modid = "reivaxmc_progress",
   value = {Dist.CLIENT},
   bus = Bus.MOD
)
public final class ReivaxMCProgressClient {
   public static final KeyMapping OPEN = new KeyMapping("key.reivaxmc_progress.open", Type.KEYSYM, 323, "key.categories.reivaxmc_progress");

   @SubscribeEvent
   public static void keys(RegisterKeyMappingsEvent e) {
      e.register(OPEN);
   }

   @SubscribeEvent
   public static void layers(RegisterGuiLayersEvent e) {
      e.registerAboveAll(ResourceLocation.fromNamespaceAndPath("reivaxmc_progress", "campaign_hud"), ReivaxMCProgressClient::hud);
   }

   public static boolean hasTarget() {
      return ClientCampaignState.targetX != 0 || ClientCampaignState.targetY != 0 || ClientCampaignState.targetZ != 0;
   }

   public static String resonanceArrow() {
      Minecraft m = Minecraft.getInstance();
      if (m.player != null && hasTarget()) {
         double dx = (double)ClientCampaignState.targetX + 0.5 - m.player.getX();
         double dz = (double)ClientCampaignState.targetZ + 0.5 - m.player.getZ();
         double target = Math.toDegrees(Math.atan2(-dx, dz));
         double delta = (double)Mth.wrapDegrees((float)(target - (double)m.player.getYRot()));
         if (Math.abs(delta) < 35.0) {
            return "▲";
         } else if (Math.abs(delta) > 145.0) {
            return "▼";
         } else {
            return delta < 0.0 ? "◀" : "▶";
         }
      } else {
         return "";
      }
   }

   public static String resonanceStrength() {
      Minecraft m = Minecraft.getInstance();
      if (m.player != null && hasTarget()) {
         double dx = (double)ClientCampaignState.targetX + 0.5 - m.player.getX();
         double dz = (double)ClientCampaignState.targetZ + 0.5 - m.player.getZ();
         double d = Math.sqrt(dx * dx + dz * dz);
         if (d < 35.0) {
            return "TOUT PRÈS";
         } else if (d < 70.0) {
            return "TRÈS FORTE";
         } else if (d < 110.0) {
            return "FORTE";
         } else {
            return d < 160.0 ? "PERCEPTIBLE" : "FAIBLE";
         }
      } else {
         return "INACTIVE";
      }
   }

   private static void hud(GuiGraphics var0, DeltaTracker var1) {
      CompactHudHook.render(var0, var1);
   }

   private static void notification(GuiGraphics var0, Minecraft var1) {
      NarratorClientHook.render(var0, var1);
   }

   @EventBusSubscriber(
      modid = "reivaxmc_progress",
      value = {Dist.CLIENT}
   )
   public static final class Game {
      private Game() {
      }

      @SubscribeEvent
      public static void tick(Post var0) {
         Minecraft var1 = Minecraft.getInstance();
         if (!Alpha18FClientState.managed && var1.screen instanceof IntroCinematicScreen && ClientCampaignState.introCompleted) {
            var1.setScreen(new DestinyBookScreen());
         }

         while (ReivaxMCProgressClient.OPEN.consumeClick()) {
            if (var1.player != null) {
               if (Alpha18FClientState.managed) {
                  toggleReivax(var1, !Alpha18FClientState.started);
               } else if (!ClientCampaignState.introCompleted) {
                  Alpha18FClientState.update(true, true, false);
                  toggleReivax(var1, true);
               } else {
                  toggleReivax(var1, false);
               }
            }
         }
      }

      private static void toggleReivax(Minecraft var0, boolean var1) {
         if (var0.screen instanceof StoryLauncherScreen || var0.screen instanceof ProgressScreen) {
            var0.setScreen(null);
         } else if (var0.screen == null) {
            var0.setScreen((Screen)(var1 ? new StoryLauncherScreen() : new ProgressScreen()));
         }
      }
   }
}
