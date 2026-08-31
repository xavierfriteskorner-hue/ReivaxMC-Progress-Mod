package fr.reivaxmc.progress.client;

import fr.reivaxmc.progress.ReivaxMCProgress;
import fr.reivaxmc.progress.network.SimplePayloads;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.network.PacketDistributor;

public final class FragmentAltarScreen extends Screen {
   public FragmentAltarScreen() {
      super(Component.literal("Réceptacle ancien"));
   }

   public void renderBackground(GuiGraphics g, int x, int y, float p) {
      g.fill(0, 0, this.width, this.height, -1073741824);
   }

   public void render(GuiGraphics g, int mx, int my, float p) {
      this.renderBackground(g, mx, my, p);
      int w = Math.min(480, this.width - 40);
      int h = 270;
      int x = (this.width - w) / 2;
      int y = (this.height - h) / 2;
      g.fill(x, y, x + w, y + h, -267776752);
      g.fill(x, y, x + 4, y + h, -11818622);
      g.drawCenteredString(this.font, "RÉCEPTACLE DE LA STÈLE", this.width / 2, y + 24, -1586833);
      g.drawCenteredString(this.font, "La pierre semble avoir été taillée pour conserver cet objet précis.", this.width / 2, y + 50, -4606807);
      int sx = this.width / 2 - 22;
      int sy = y + 88;
      g.fill(sx - 5, sy - 5, sx + 49, sy + 49, -15657195);
      g.fill(sx, sy, sx + 44, sy + 44, -14273233);
      g.renderItem(new ItemStack((ItemLike)ReivaxMCProgress.UNKNOWN_FRAGMENT.get()), sx + 6, sy + 6);
      int bx = this.width / 2 - 115;
      int by = y + h - 55;
      g.fill(bx, by, bx + 230, by + 32, -12226723);
      g.drawCenteredString(this.font, "RETIRER LE FRAGMENT", this.width / 2, by + 11, -725280);
      super.render(g, mx, my, p);
   }

   public boolean mouseClicked(double mx, double my, int b) {
      int h = 270;
      int y = (this.height - h) / 2;
      int bx = this.width / 2 - 115;
      int by = y + h - 55;
      if (mx >= (double)bx && mx <= (double)(bx + 230) && my >= (double)by && my <= (double)(by + 32)) {
         PacketDistributor.sendToServer(new SimplePayloads.ClaimFragment(), new CustomPacketPayload[0]);
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
