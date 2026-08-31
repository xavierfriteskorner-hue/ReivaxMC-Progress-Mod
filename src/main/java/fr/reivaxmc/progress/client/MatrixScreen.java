package fr.reivaxmc.progress.client;

import fr.reivaxmc.progress.network.ClientCampaignState;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class MatrixScreen extends Screen {
   public MatrixScreen() {
      super(Component.literal("Matrice des Origines"));
   }

   public void renderBackground(GuiGraphics g, int x, int y, float p) {
      g.fill(0, 0, this.width, this.height, -1442840576);
   }

   public void render(GuiGraphics g, int mx, int my, float partial) {
      this.renderBackground(g, mx, my, partial);
      int w = Math.min(560, this.width - 40);
      int h = Math.min(360, this.height - 40);
      int x = (this.width - w) / 2;
      int y = (this.height - h) / 2;
      g.fill(x, y, x + w, y + h, -267776499);
      g.fill(x, y, x + 4, y + h, -11690122);
      g.drawCenteredString(this.font, "MATRICE DES ORIGINES", this.width / 2, y + 22, -2309772);
      g.drawCenteredString(this.font, "Registre mondial des reliques retrouvées", this.width / 2, y + 42, -6377049);
      String a = ClientCampaignState.artifacts;
      int yy = y + 78;
      if (a.isEmpty()) {
         g.drawCenteredString(this.font, "[ ? ]   [ ? ]   [ ? ]   [ ? ]   [ ? ]", this.width / 2, yy, -10919331);
         g.drawCenteredString(this.font, "Aucun Fragment n'a encore été reconnu.", this.width / 2, yy + 35, -5725286);
      } else {
         for (String line : a.split("\\n")) {
            String[] q = line.split("¦", -1);
            if (q.length >= 6) {
               g.fill(x + 28, yy - 8, x + w - 28, yy + 54, -15656939);
               g.drawString(this.font, "◆  " + q[1], x + 42, yy, -2705045, false);
               g.drawString(this.font, "Découvert par : " + q[2] + "  •  Jour " + q[3], x + 42, yy + 17, -2501947, false);
               g.drawString(this.font, "Lieu : " + q[4] + "  •  Signification : " + q[5], x + 42, yy + 34, -3094084, false);
               yy += 70;
               if (yy > y + h - 70) {
                  break;
               }
            }
         }
      }

      super.render(g, mx, my, partial);
   }

   public boolean isPauseScreen() {
      return false;
   }
}
