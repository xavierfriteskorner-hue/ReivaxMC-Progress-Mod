package fr.reivaxmc.progress.client;

import fr.reivaxmc.progress.network.PosPayloads;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.util.FormattedCharSequence;
import net.neoforged.neoforge.network.PacketDistributor;

public final class FoundationPlacementScreen extends Screen {
   private final int px;
   private final int py;
   private final int pz;

   public FoundationPlacementScreen(int x, int y, int z) {
      super(Component.literal("Établir le Foyer principal"));
      this.px = x;
      this.py = y;
      this.pz = z;
   }

   public void renderBackground(GuiGraphics g, int x, int y, float p) {
      g.fill(0, 0, this.width, this.height, -1207959552);
   }

   public void render(GuiGraphics g, int mx, int my, float p) {
      this.renderBackground(g, mx, my, p);
      int w = Math.min(600, this.width - 30);
      int h = Math.min(390, this.height - 24);
      int x = (this.width - w) / 2;
      int y = (this.height - h) / 2;
      g.fill(x, y, x + w, y + h, -267052515);
      g.fill(x, y, x + 5, y + h, -1919929);
      g.drawCenteredString(this.font, "ÉTABLIR LE FOYER PRINCIPAL ICI ?", this.width / 2, y + 22, -1520522);
      String text = "Cette Borne définira votre base principale. Les 96 blocs de rayon autour de cet emplacement seront enregistrés comme votre Territoire principal. Certaines quêtes, événements et futures mécaniques utiliseront cette zone comme référence.";
      int yy = y + 60;

      for (FormattedCharSequence l : this.font.split(Component.literal(text), w - 60)) {
         g.drawString(this.font, l, x + 30, yy, -1580328, false);
         yy += 14;
      }

      int by = y + h - 48;
      int bw = (w - 72) / 2;
      this.btn(g, x + 24, by, bw, 30, "ANNULER", -12499382);
      this.btn(g, x + 48 + bw, by, bw, 30, "FONDER ICI", -8690645);
      super.render(g, mx, my, p);
   }

   public boolean mouseClicked(double mx, double my, int b) {
      int w = Math.min(600, this.width - 30);
      int h = Math.min(390, this.height - 24);
      int x = (this.width - w) / 2;
      int y = (this.height - h) / 2;
      int by = y + h - 48;
      int bw = (w - 72) / 2;
      if (this.hit(mx, my, x + 24, by, bw, 30)) {
         this.onClose();
         return true;
      } else if (this.hit(mx, my, x + 48 + bw, by, bw, 30)) {
         PacketDistributor.sendToServer(new PosPayloads.ConfirmFoundation(this.px, this.py, this.pz), new CustomPacketPayload[0]);
         this.onClose();
         return true;
      } else {
         return super.mouseClicked(mx, my, b);
      }
   }

   private void btn(GuiGraphics g, int x, int y, int w, int h, String t, int c) {
      g.fill(x, y, x + w, y + h, c);
      g.drawCenteredString(this.font, t, x + w / 2, y + 10, -790815);
   }

   private boolean hit(double mx, double my, int x, int y, int w, int h) {
      return mx >= (double)x && mx <= (double)(x + w) && my >= (double)y && my <= (double)(y + h);
   }

   public boolean isPauseScreen() {
      return false;
   }
}
