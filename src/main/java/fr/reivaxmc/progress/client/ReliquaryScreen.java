package fr.reivaxmc.progress.client;

import fr.reivaxmc.progress.ReivaxMCProgress;
import fr.reivaxmc.progress.network.ClientCampaignState;
import fr.reivaxmc.progress.network.SimplePayloads;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.network.PacketDistributor;

public final class ReliquaryScreen extends Screen {
   public ReliquaryScreen() {
      super(Component.literal("Reliquaire des Origines"));
   }

   public void renderBackground(GuiGraphics g, int x, int y, float p) {
      g.fill(0, 0, this.width, this.height, -1442840576);
   }

   public void render(GuiGraphics g, int mx, int my, float p) {
      this.renderBackground(g, mx, my, p);
      int w = 360;
      int h = 220;
      int x = (this.width - w) / 2;
      int y = (this.height - h) / 2;
      g.fill(x, y, x + w, y + h, -267776756);
      g.fill(x, y, x + 4, y + h, -3761594);
      g.fill(x + w - 4, y, x + w, y + h, -9284050);
      g.drawCenteredString(this.font, "RELIQUAIRE DES ORIGINES", this.width / 2, y + 25, -1851025);
      g.drawCenteredString(
         this.font,
         ClientCampaignState.reliquaryOpened ? "Le scellement a été brisé." : "Une seule cavité repose au centre du Reliquaire.",
         this.width / 2,
         y + 50,
         -4607066
      );
      int sx = this.width / 2 - 18;
      int sy = y + 86;
      g.fill(sx - 3, sy - 3, sx + 39, sy + 39, -9743312);
      g.fill(sx, sy, sx + 36, sy + 36, -15394281);
      if (!ClientCampaignState.reliquaryOpened) {
         g.renderItem(new ItemStack((ItemLike)ReivaxMCProgress.ORIGIN_SEAL.get()), sx + 10, sy + 10);
         g.drawCenteredString(this.font, "SCEAU DES ORIGINES", this.width / 2, y + 139, -2707107);
         g.drawCenteredString(this.font, "Cliquez sur le Sceau pour le retirer.", this.width / 2, y + 165, -1514795);
      } else {
         g.drawCenteredString(this.font, "VIDE", this.width / 2, y + 101, -9343902);
      }
   }

   public boolean mouseClicked(double mx, double my, int b) {
      int y = (this.height - 220) / 2;
      int sx = this.width / 2 - 18;
      int sy = y + 86;
      if (b == 0 && !ClientCampaignState.reliquaryOpened && mx >= (double)sx && mx < (double)(sx + 36) && my >= (double)sy && my < (double)(sy + 36)) {
         PacketDistributor.sendToServer(new SimplePayloads.ClaimSeal(), new CustomPacketPayload[0]);
         this.onClose();
         return true;
      } else {
         return super.mouseClicked(mx, my, b);
      }
   }

   public boolean isPauseScreen() {
      return false;
   }
}
