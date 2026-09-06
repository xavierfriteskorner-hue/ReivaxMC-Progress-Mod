package fr.reivaxmc.progress.client;

import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.neoforged.neoforge.network.PacketDistributor;
import fr.reivaxmc.progress.network.CivilizationPayloads;
import fr.reivaxmc.progress.network.CouncilPayloads;

public final class F82FoyerScreen extends Screen {
   private final String name;
   private final int radius;
   private final String founder;
   private final int day;
   private final String coords;
   private final int civilizationTotal;
   private final int civilizationAvailable;
   private final String upgrades;
   private final String chapterStage;
   private final String doctrine;
   private final String councilRoster;
   private Button boundaryUpgrade;
   private Button territoryUpgrade;
   private Button councilConfirm;
   private String selectedDoctrine = "";
   private boolean voteSent;
   private int tab = 0;

   public F82FoyerScreen(String var1) {
      super(Component.literal("Foyer principal"));
      String[] var2 = var1 == null ? new String[0] : var1.split("\\|", -1);
      this.name = var2.length > 0 && !var2[0].isBlank() ? var2[0] : "Premier Foyer";
      this.radius = var2.length > 1 ? parseInt(var2[1], 96) : 96;
      this.founder = var2.length > 2 && !var2[2].isBlank() ? var2[2] : "Fondateurs";
      this.day = var2.length > 3 ? parseInt(var2[3], 1) : 1;
      this.coords = var2.length > 4 && !var2[4].isBlank() ? var2[4] : "—";
      this.civilizationTotal = var2.length > 5 ? parseInt(var2[5], 0) : 0;
      this.civilizationAvailable = var2.length > 6 ? parseInt(var2[6], this.civilizationTotal) : this.civilizationTotal;
      this.upgrades = var2.length > 7 ? var2[7] : "";
      this.chapterStage = var2.length > 8 ? var2[8] : "";
      this.doctrine = var2.length > 9 ? var2[9] : "";
      this.councilRoster = var2.length > 10 ? var2[10] : "";
      if (councilOpen()) this.tab = 2;
   }

   @Override
   protected void init() {
      int panelWidth = Math.min(760, this.width - 34);
      int panelHeight = Math.min(470, this.height - 30);
      int panelX = (this.width - panelWidth) / 2;
      int panelY = (this.height - panelHeight) / 2;
      int right = panelX + panelWidth - 42;
      boundaryUpgrade = addRenderableWidget(Button.builder(Component.literal(hasUpgrade("LISIERE_ACCORDEE") ? "ACQUIS" : "12 POINTS"),
         button -> buy("LISIERE_ACCORDEE")).bounds(right - 106, panelY + 211, 106, 22).build());
      territoryUpgrade = addRenderableWidget(Button.builder(Component.literal(hasUpgrade("ANCRAGE_ETENDU") ? "ACQUIS" : "25 POINTS"),
         button -> buy("ANCRAGE_ETENDU")).bounds(right - 106, panelY + 257, 106, 22).build());
      councilConfirm = addRenderableWidget(Button.builder(Component.literal("CONFIRMER CE CHOIX"), button -> castVote())
         .bounds(panelX + (panelWidth - 210) / 2, panelY + panelHeight - 78, 210, 22).build());
      updateUpgradeButtons();
      updateCouncilButton();
   }

   private void buy(String id) {
      PacketDistributor.sendToServer(new CivilizationPayloads.BuyUpgrade(id));
   }

   private boolean hasUpgrade(String id) {
      for (String value : upgrades.split(",")) if (id.equals(value)) return true;
      return false;
   }

   private void updateUpgradeButtons() {
      boolean visible = tab == 4;
      if (boundaryUpgrade != null) {
         boundaryUpgrade.visible = visible;
         boundaryUpgrade.active = visible && !hasUpgrade("LISIERE_ACCORDEE") && civilizationAvailable >= 12;
      }
      if (territoryUpgrade != null) {
         territoryUpgrade.visible = visible;
         territoryUpgrade.active = visible && !hasUpgrade("ANCRAGE_ETENDU") && civilizationAvailable >= 25;
      }
   }

   private boolean councilOpen() {
      return "CHOOSE_PRIORITY".equals(chapterStage);
   }

   private void castVote() {
      if (!councilOpen() || selectedDoctrine.isBlank() || voteSent) return;
      voteSent = true;
      updateCouncilButton();
      PacketDistributor.sendToServer(new CouncilPayloads.CastVote(selectedDoctrine));
   }

   private void updateCouncilButton() {
      if (councilConfirm == null) return;
      councilConfirm.visible = tab == 2 && councilOpen();
      councilConfirm.active = councilConfirm.visible && !selectedDoctrine.isBlank() && !voteSent;
      councilConfirm.setMessage(Component.literal(voteSent ? "VOTE TRANSMIS…" : "CONFIRMER CE CHOIX"));
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
      String[] var9 = new String[]{"VUE D'ENSEMBLE", "TERRITOIRE", "CONSEIL", "JOURNAL", "OPTIONS"};
      int var10 = var7 + 22;
      int var11 = var8 + 70;
      byte var12 = 8;
      int var13 = (var5 - 44 - var12 * 4) / 5;

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
      // Contenu mis à l'échelle pour tenir entre les onglets et le bouton FERMER, quel que soit le GUI
      // scale : plus d'encadré qui déborde par le bas ni de chevauchement avec FERMER.
      float var23 = Math.min(1.0F, (float)(var6 - 172) / 260.0F);
      int var24 = (int)((float)var22 / var23);
      var1.pose().pushPose();
      var1.pose().translate((double)var20, (double)var21, 0.0);
      var1.pose().scale(var23, var23, 1.0F);
      if (this.tab == 0) {
         this.overview(var1, 0, 0, var24);
      } else if (this.tab == 1) {
         this.territory(var1, 0, 0, var24);
      } else if (this.tab == 2) {
         this.council(var1, 0, 0, var24);
      } else if (this.tab == 3) {
         this.journal(var1, 0, 0, var24);
      } else {
         this.options(var1, 0, 0, var24);
      }

      var1.pose().popPose();
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

   private void council(GuiGraphics graphics, int x, int y, int width) {
      this.title(graphics, "CONSEIL DU FOYER", x, y);
      if (!councilOpen()) {
         String status = doctrine.isBlank()
            ? "La Borne n'appelle encore aucune décision. Le Conseil s'ouvrira lorsque le Foyer aura pris forme."
            : "Priorité inscrite : " + doctrineLabel(doctrine) + ". Cette décision appartient désormais à l'histoire du Foyer.";
         this.paragraph(graphics, status, x, y + 32, width);
         return;
      }

      this.paragraph(graphics, "Choisissez ce qui doit survivre en premier. Aucun objet n'est requis ou consommé.", x, y + 24, width);
      String[] ids = {"BASTION", "MEMORY", "SOLIDARITY"};
      int gap = 8;
      int cardWidth = (width - gap * 2) / 3;
      int cardY = y + 58;
      for (int i = 0; i < ids.length; i++) {
         int cardX = x + i * (cardWidth + gap);
         boolean selected = ids[i].equals(selectedDoctrine);
         graphics.fill(cardX, cardY, cardX + cardWidth, cardY + 76, selected ? -1140655540 : -1440602325);
         graphics.fill(cardX, cardY, cardX + (selected ? 4 : 2), cardY + 76, selected ? -2054356 : -3104453);
         graphics.drawCenteredString(this.font, doctrineLabel(ids[i]), cardX + cardWidth / 2, cardY + 12, selected ? -461589 : -1514534);
         this.paragraph(graphics, doctrineDescription(ids[i]), cardX + 9, cardY + 34, cardWidth - 18);
      }

      graphics.drawString(this.font, "VOIX DU CONSEIL", x, y + 151, -4277583, false);
      int lineY = y + 172;
      if (councilRoster.isBlank()) {
         graphics.drawString(this.font, "En attente des membres du Foyer…", x + 8, lineY, -7369852, false);
      } else {
         for (String entry : councilRoster.split(";")) {
            String[] parts = entry.split("~", -1);
            String member = parts.length > 0 ? parts[0] : "Membre";
            String vote = parts.length > 1 ? parts[1] : "";
            String value = vote.isBlank() ? "EN ATTENTE" : doctrineLabel(vote);
            graphics.drawString(this.font, "• " + member, x + 8, lineY, -2895929, false);
            graphics.drawString(this.font, value, x + width - this.font.width(value) - 8, lineY, vote.isBlank() ? -7369852 : -461589, false);
            lineY += 17;
         }
      }
   }

   private static String doctrineLabel(String id) {
      return switch (id) {
         case "BASTION" -> "PROTECTION";
         case "MEMORY" -> "COMPRÉHENSION";
         case "SOLIDARITY" -> "PARTAGE";
         default -> "—";
      };
   }

   private static String doctrineDescription(String id) {
      return switch (id) {
         case "BASTION" -> "Tenir. Défendre. Préserver les murs.";
         case "MEMORY" -> "Chercher. Comprendre. Préserver la mémoire.";
         default -> "Nourrir. Unir. Préserver les autres.";
      };
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
      this.title(var1, "DÉCISIONS DE CIVILISATION", var2, var3);
      this.paragraph(
         var1,
         "Points gagnés : " + civilizationTotal + "   •   disponibles : " + civilizationAvailable
            + ". Les points d'Âge racontent votre progression ; seuls les points de Civilisation se dépensent ici.",
         var2,
         var3 + 30,
         var4
      );
      this.option(var1, var2, var3 + 92, var4, "LISIÈRE ACCORDÉE", hasUpgrade("LISIERE_ACCORDEE") ? "Résonance stabilisée" : "Contour perçu plus tôt");
      this.option(var1, var2, var3 + 138, var4, "ANCRAGE ÉTENDU", hasUpgrade("ANCRAGE_ETENDU") ? "Rayon 128 blocs" : "96 → 128 blocs");
      this.paragraph(var1, "Ces améliorations modifient le Foyer et restent acquises. Elles n'augmentent pas simplement les dégâts : elles changent la manière d'habiter le territoire.", var2, var3 + 194, var4);
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
      List<FormattedCharSequence> var6 = this.font.split(Component.literal(var2), var5);
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
         int var13 = (var6 - 44 - var12 * 4) / 5;

         for (int var14 = 0; var14 < 5; var14++) {
            int var15 = var10 + var14 * (var13 + var12);
            if (this.hit(var1, var3, var15, var11, var13, 28)) {
               this.tab = var14;
               updateUpgradeButtons();
               updateCouncilButton();
               return true;
            }
         }

         if (tab == 2 && councilOpen()) {
            float scale = Math.min(1.0F, (float)(var7 - 172) / 260.0F);
            double localX = (var1 - (var8 + 30)) / scale;
            double localY = (var3 - (var9 + 122)) / scale;
            int contentWidth = (int)((var6 - 60) / scale);
            int gap = 8;
            int cardWidth = (contentWidth - gap * 2) / 3;
            String[] ids = {"BASTION", "MEMORY", "SOLIDARITY"};
            for (int i = 0; i < ids.length; i++) {
               int cardX = i * (cardWidth + gap);
               if (hit(localX, localY, cardX, 58, cardWidth, 76)) {
                  selectedDoctrine = ids[i];
                  voteSent = false;
                  updateCouncilButton();
                  return true;
               }
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
