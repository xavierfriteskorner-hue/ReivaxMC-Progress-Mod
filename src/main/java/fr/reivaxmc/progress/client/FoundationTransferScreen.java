package fr.reivaxmc.progress.client;

import fr.reivaxmc.progress.network.PosPayloads;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.util.FormattedCharSequence;
import net.neoforged.neoforge.network.PacketDistributor;

public final class FoundationTransferScreen extends Screen {
   private final int px;
   private final int py;
   private final int pz;
   private long holdStart = -1L;

   public FoundationTransferScreen(int x, int y, int z) {
      super(Component.literal("Transférer le Foyer principal"));
      this.px = x;
      this.py = y;
      this.pz = z;
   }

   public void renderBackground(GuiGraphics g, int x, int y, float p) {
      g.fill(0, 0, this.width, this.height, -1073741824);
   }

   public void render(GuiGraphics g, int mx, int my, float p) {
      this.renderBackground(g, mx, my, p);
      int w = Math.min(640, this.width - 24);
      int h = Math.min(430, this.height - 16);
      int x = (this.width - w) / 2;
      int y = (this.height - h) / 2;
      g.fill(x, y, x + w, y + h, -266987750);
      g.fill(x, y, x + 5, y + h, -4832702);
      g.drawCenteredString(this.font, "TRANSFÉRER LE FOYER PRINCIPAL", this.width / 2, y + 18, -15449);
      g.drawCenteredString(this.font, "⚠ DÉCISION IRRÉVERSIBLE POUR CE TERRITOIRE", this.width / 2, y + 39, -29833);
      String text = "Vous êtes sur le point d'abandonner ce Foyer comme base principale de votre civilisation. Si vous continuez, la Borne sera récupérée et remplacée par une Stèle commémorative. Les 96 blocs autour deviendront définitivement un Site historique. Vous pourrez toujours visiter, construire et utiliser ce lieu, mais cette zone ne pourra PLUS JAMAIS devenir votre Foyer principal.";
      int yy = y + 70;

      for (FormattedCharSequence l : this.font.split(Component.literal(text), w - 58)) {
         if (yy > y + h - 100) {
            break;
         }

         g.drawString(this.font, l, x + 29, yy, -1383464, false);
         yy += 14;
      }

      int by = y + h - 46;
      int bw = (w - 74) / 2;
      this.btn(g, x + 24, by, bw, 30, "ANNULER", -12499382);
      long held = this.holdStart < 0L ? 0L : Util.getMillis() - this.holdStart;
      String label = this.holdStart < 0L ? "MAINTENIR 2 SEC POUR ABANDONNER" : "CONFIRMATION " + Math.min(100, (int)(held / 20L)) + "%";
      this.btn(g, x + 50 + bw, by, bw, 30, label, -7718856);
      if (this.holdStart >= 0L) {
         int pw = (int)((double)bw * Math.min(1.0, (double)held / 2000.0));
         g.fill(x + 50 + bw, by + 27, x + 50 + bw + pw, by + 30, -25992);
         if (held >= 2000L) {
            PacketDistributor.sendToServer(new PosPayloads.ConfirmTransfer(this.px, this.py, this.pz), new CustomPacketPayload[0]);
            this.holdStart = -1L;
            this.onClose();
         }
      }

      super.render(g, mx, my, p);
   }

   public boolean mouseClicked(double mx, double my, int b) {
      int w = Math.min(640, this.width - 24);
      int h = Math.min(430, this.height - 16);
      int x = (this.width - w) / 2;
      int y = (this.height - h) / 2;
      int by = y + h - 46;
      int bw = (w - 74) / 2;
      if (this.hit(mx, my, x + 24, by, bw, 30)) {
         this.onClose();
         return true;
      } else if (this.hit(mx, my, x + 50 + bw, by, bw, 30)) {
         this.holdStart = Util.getMillis();
         return true;
      } else {
         return super.mouseClicked(mx, my, b);
      }
   }

   public boolean mouseReleased(double mx, double my, int b) {
      if (this.holdStart >= 0L) {
         this.holdStart = -1L;
         return true;
      } else {
         return super.mouseReleased(mx, my, b);
      }
   }

   private void btn(GuiGraphics g, int x, int y, int w, int h, String t, int c) {
      g.fill(x, y, x + w, y + h, c);
      g.drawCenteredString(this.font, t, x + w / 2, y + 10, -659740);
   }

   private boolean hit(double mx, double my, int x, int y, int w, int h) {
      return mx >= (double)x && mx <= (double)(x + w) && my >= (double)y && my <= (double)(y + h);
   }

   public boolean isPauseScreen() {
      return false;
   }
}
