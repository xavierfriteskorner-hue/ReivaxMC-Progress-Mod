package fr.reivaxmc.progress.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

public final class MemorialScreen extends Screen {
   private final String text;

   public MemorialScreen(String t) {
      super(Component.literal("Site historique"));
      this.text = t;
   }

   public void renderBackground(GuiGraphics g, int x, int y, float p) {
      g.fill(0, 0, this.width, this.height, -1207959552);
   }

   public void render(GuiGraphics g, int mx, int my, float p) {
      this.renderBackground(g, mx, my, p);
      int w = Math.min(520, this.width - 30);
      int h = Math.min(360, this.height - 30);
      int x = (this.width - w) / 2;
      int y = (this.height - h) / 2;
      g.fill(x, y, x + w, y + h, -267184360);
      g.fill(x, y, x + 4, y + h, -3102120);
      int yy = y + 28;

      for (String para : this.text.split("\\n")) {
         for (FormattedCharSequence l : this.font.split(Component.literal(para), w - 54)) {
            g.drawCenteredString(this.font, l, this.width / 2, yy, -1514792);
            yy += 14;
         }

         yy += para.isEmpty() ? 8 : 2;
      }

      super.render(g, mx, my, p);
   }

   public boolean isPauseScreen() {
      return false;
   }
}
