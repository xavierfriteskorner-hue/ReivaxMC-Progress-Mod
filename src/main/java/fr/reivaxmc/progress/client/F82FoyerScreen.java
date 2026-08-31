package fr.reivaxmc.progress.client;

import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

public final class F82FoyerScreen extends Screen {
   private final String name;
   private final int radius;
   private final String founder;
   private final int day;
   private final String coords;
   private int tab = 0;

   public F82FoyerScreen(String var1) {
      super(Component.literal("Foyer principal"));
      String[] var2 = var1 == null ? new String[0] : var1.split("\\|", -1);
      this.name = var2.length > 0 && !var2[0].isBlank() ? var2[0] : "Premier Foyer";
      this.radius = var2.length > 1 ? parseInt(var2[1], 96) : 96;
      this.founder = var2.length > 2 && !var2[2].isBlank() ? var2[2] : "Fondateurs";
      this.day = var2.length > 3 ? parseInt(var2[3], 1) : 1;
      this.coords = var2.length > 4 && !var2[4].isBlank() ? var2[4] : "—";
   }

   public void renderBackground(GuiGraphics var1, int var2, int var3, float var4) {
      var1.fill(0, 0, this.width, this.height, -1207959552);
   }

   public void render(GuiGraphics var1, int var2, int var3, float var4) {
      this.renderBackground(var1, var2, var3, var4);
      int var5 = Math.min(760, this.width - 34);
      int var6 = Math.min(470, this.height - 30);
      int var7 = (this.width - var5) / 2;
      int var8 = (this.height - var6) / 2;
      var1.fill(var7, var8, var7 + var5, var8 + var6, -233367011);
      var1.fill(var7, var8, var7 + 5, var8 + var6, -2054356);
      var1.fill(var7 + 18, var8 + 54, var7 + var5 - 18, var8 + 55, 1430210122);
      var1.drawString(this.font, "FOYER PRINCIPAL", var7 + 24, var8 + 20, -1002190, false);
      var1.drawString(this.font, this.name, var7 + 24, var8 + 37, -724503, true);
      var1.drawString(this.font, "X", var7 + var5 - 28, var8 + 20, -4672341, false);
      String[] var9 = new String[]{"VUE D'ENSEMBLE", "TERRITOIRE", "JOURNAL", "OPTIONS"};
      int var10 = var7 + 22;
      int var11 = var8 + 70;
      byte var12 = 8;
      int var13 = (var5 - 44 - var12 * 3) / 4;

      for (int var14 = 0; var14 < var9.length; var14++) {
         int var15 = var10 + var14 * (var13 + var12);
         int var16 = var14 == this.tab ? -13025210 : -869979602;
         var1.fill(var15, var11, var15 + var13, var11 + 28, var16);
         if (var14 == this.tab) {
            var1.fill(var15, var11 + 26, var15 + var13, var11 + 28, -2054356);
         }

         var1.drawCenteredString(this.font, var9[var14], var15 + var13 / 2, var11 + 10, var14 == this.tab ? -461589 : -3619652);
      }

      int var20 = var7 + 30;
      int var21 = var8 + 122;
      int var22 = var5 - 60;
      if (this.tab == 0) {
         this.overview(var1, var20, var21, var22);
      } else if (this.tab == 1) {
         this.territory(var1, var20, var21, var22);
      } else if (this.tab == 2) {
         this.journal(var1, var20, var21, var22);
      } else {
         this.options(var1, var20, var21, var22);
      }

      short var17 = 150;
      int var18 = var7 + var5 - var17 - 24;
      int var19 = var8 + var6 - 44;
      var1.fill(var18, var19, var18 + var17, var19 + 28, -13486019);
      var1.drawCenteredString(this.font, "FERMER", var18 + var17 / 2, var19 + 10, -790554);
      super.render(var1, var2, var3, var4);
   }

   private void overview(GuiGraphics var1, int var2, int var3, int var4) {
      this.title(var1, "ANCRAGE DU FOYER", var2, var3);
      this.line(var1, "Nom", this.name, var2, var3 + 28);
      this.line(var1, "Fondé par", this.founder, var2, var3 + 50);
      this.line(var1, "Jour de fondation", "Jour " + this.day, var2, var3 + 72);
      this.line(var1, "Position de la Borne", this.coords, var2, var3 + 94);
      this.line(var1, "Territoire principal", "Rayon " + this.radius + " blocs", var2, var3 + 116);
      this.paragraph(
         var1,
         "Cette Borne est le point d'ancrage du Foyer. Les futures mécaniques de civilisation, d'habitants et d'événements pourront s'y rattacher.",
         var2,
         var3 + 150,
         var4
      );
   }

   private void territory(GuiGraphics var1, int var2, int var3, int var4) {
      this.title(var1, "TERRITOIRE PRINCIPAL", var2, var3);
      this.paragraph(
         var1,
         "La Borne définit actuellement un territoire de "
            + this.radius
            + " blocs de rayon autour du Foyer. Cette zone sert de référence narrative et pourra évoluer avec votre civilisation.",
         var2,
         var3 + 30,
         var4
      );
      this.box(var1, var2, var3 + 92, var4, 64, "RAYON ACTUEL", this.radius + " blocs");
      this.box(var1, var2, var3 + 166, var4, 64, "POINT D'ANCRAGE", this.coords);
   }

   private void journal(GuiGraphics var1, int var2, int var3, int var4) {
      this.title(var1, "JOURNAL DU FOYER", var2, var3);
      this.paragraph(
         var1,
         "Le Foyer a été établi au jour "
            + this.day
            + " par "
            + this.founder
            + ". Les événements propres à ce lieu pourront être regroupés ici au fil de la campagne.",
         var2,
         var3 + 30,
         var4
      );
      var1.fill(var2, var3 + 104, var2 + var4, var3 + 105, 1144997450);
      var1.drawString(this.font, "• Premier Foyer établi", var2 + 8, var3 + 124, -1646120, true);
      var1.drawString(this.font, "• Borne de Fondation liée", var2 + 8, var3 + 145, -3553860, true);
   }

   private void options(GuiGraphics var1, int var2, int var3, int var4) {
      this.title(var1, "OPTIONS DU FOYER", var2, var3);
      this.paragraph(
         var1,
         "Ce menu est maintenant une vraie interface persistante. Les fonctions ci-dessous seront activées progressivement sans remplacer la Borne.",
         var2,
         var3 + 30,
         var4
      );
      this.option(var1, var2, var3 + 92, var4, "RENOMMER LE FOYER", "À venir");
      this.option(var1, var2, var3 + 132, var4, "GESTION DU TERRITOIRE", "À venir");
      this.option(var1, var2, var3 + 172, var4, "HABITANTS & CIVILISATION", "À venir");
      this.option(var1, var2, var3 + 212, var4, "MIGRATION / NOUVEL ANCRAGE", "À venir");
   }

   private void title(GuiGraphics var1, String var2, int var3, int var4) {
      var1.drawString(this.font, var2, var3, var4, -1002190, false);
   }

   private void line(GuiGraphics var1, String var2, String var3, int var4, int var5) {
      var1.drawString(this.font, var2, var4, var5, -4343375, true);
      var1.drawString(this.font, var3, var4 + 175, var5, -856346, true);
   }

   private void box(GuiGraphics var1, int var2, int var3, int var4, int var5, String var6, String var7) {
      var1.fill(var2, var3, var2 + var4, var3 + var5, -1440602325);
      var1.fill(var2, var3, var2 + 3, var3 + var5, -3104453);
      var1.drawString(this.font, var6, var2 + 14, var3 + 13, -4277583, false);
      var1.drawString(this.font, var7, var2 + 14, var3 + 34, -724760, true);
   }

   private void option(GuiGraphics var1, int var2, int var3, int var4, String var5, String var6) {
      var1.fill(var2, var3, var2 + var4, var3 + 31, -1440339153);
      var1.drawString(this.font, var5, var2 + 12, var3 + 11, -1514534, true);
      int var7 = this.font.width(var6);
      var1.drawString(this.font, var6, var2 + var4 - var7 - 12, var3 + 11, -7369852, false);
   }

   private void paragraph(GuiGraphics var1, String var2, int var3, int var4, int var5) {
      List var6 = this.font.split(Component.literal(var2), var5);
      int var7 = var4;

      for (FormattedCharSequence var9 : var6) {
         var1.drawString(this.font, var9, var3, var7, -2895929, false);
         var7 += 14;
      }
   }

   public boolean mouseClicked(double var1, double var3, int var5) {
      int var6 = Math.min(760, this.width - 34);
      int var7 = Math.min(470, this.height - 30);
      int var8 = (this.width - var6) / 2;
      int var9 = (this.height - var7) / 2;
      if (this.hit(var1, var3, var8 + var6 - 40, var9 + 10, 30, 28)) {
         this.onClose();
         return true;
      } else {
         int var10 = var8 + 22;
         int var11 = var9 + 70;
         byte var12 = 8;
         int var13 = (var6 - 44 - var12 * 3) / 4;

         for (int var14 = 0; var14 < 4; var14++) {
            int var15 = var10 + var14 * (var13 + var12);
            if (this.hit(var1, var3, var15, var11, var13, 28)) {
               this.tab = var14;
               return true;
            }
         }

         short var17 = 150;
         int var18 = var8 + var6 - var17 - 24;
         int var16 = var9 + var7 - 44;
         if (this.hit(var1, var3, var18, var16, var17, 28)) {
            this.onClose();
            return true;
         } else {
            return super.mouseClicked(var1, var3, var5);
         }
      }
   }

   private boolean hit(double var1, double var3, int var5, int var6, int var7, int var8) {
      return var1 >= (double)var5 && var1 <= (double)(var5 + var7) && var3 >= (double)var6 && var3 <= (double)(var6 + var8);
   }

   private static int parseInt(String var0, int var1) {
      try {
         return Integer.parseInt(var0.trim());
      } catch (Throwable var3) {
         return var1;
      }
   }

   public boolean isPauseScreen() {
      return false;
   }
}
