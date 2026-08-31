package fr.reivaxmc.progress.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class DestinyBookScreen extends Screen {
   private int page;
   private static final String[][] P = new String[][]{
      {
            "LE DESTIN DES ORIGINES",
            "Vous vous êtes éveillés dans un monde qui ne vous attendait pas.",
            "Vous ignorez d'où vous venez. Vous ignorez pourquoi vous êtes ici.",
            "Mais ce monde porte déjà des traces de votre passé."
      },
      {
            "L'ÉVEIL",
            "Le Sceau a répondu à votre présence.",
            "Ce qui dort dans ce monde n'est pas un hasard.",
            "Les réponses existent. Mais vous n'avez pas encore les moyens de les atteindre."
      },
      {"VOTRE DESTIN", "Survivez. Bâtissez. Explorez. Comprenez.", "Chaque trace retrouvée vous rapprochera de la vérité.", "RETROUVEZ VOS ORIGINES."}
   };

   public DestinyBookScreen() {
      super(Component.literal("Le Destin des Origines"));
   }

   public void renderBackground(GuiGraphics g, int x, int y, float p) {
      g.fill(0, 0, this.width, this.height, -1442840576);
   }

   public void render(GuiGraphics g, int mx, int my, float p) {
      this.renderBackground(g, mx, my, p);
      int w = Math.min(440, this.width - 40);
      int h = Math.min(300, this.height - 40);
      int x = (this.width - w) / 2;
      int y = (this.height - h) / 2;
      g.fill(x, y, x + w, y + h, -267118828);
      g.fill(x, y, x + 4, y + h, -3760051);
      g.drawCenteredString(this.font, P[this.page][0], this.width / 2, y + 28, -1916296);
      int yy = y + 72;

      for (int i = 1; i < P[this.page].length; i++) {
         this.wrap(g, P[this.page][i], x + 36, yy, w - 72);
         yy += 52;
      }

      g.drawCenteredString(this.font, "‹   PAGE " + (this.page + 1) + " / 3   ›", this.width / 2, y + h - 28, -4806783);
   }

   public boolean mouseClicked(double mx, double my, int b) {
      if (b == 0) {
         if (mx < (double)(this.width / 2)) {
            this.page = Math.max(0, this.page - 1);
         } else {
            this.page = Math.min(2, this.page + 1);
         }

         return true;
      } else {
         return super.mouseClicked(mx, my, b);
      }
   }

   private void wrap(GuiGraphics g, String s, int x, int y, int max) {
      String[] w = s.split(" ");
      String line = "";
      int yy = y;

      for (String q : w) {
         String n = line.isEmpty() ? q : line + " " + q;
         if (this.font.width(n) > max) {
            g.drawString(this.font, line, x, yy, -1383467, false);
            yy += 13;
            line = q;
         } else {
            line = n;
         }
      }

      if (!line.isEmpty()) {
         g.drawString(this.font, line, x, yy, -1383467, false);
      }
   }

   public boolean isPauseScreen() {
      return false;
   }
}
