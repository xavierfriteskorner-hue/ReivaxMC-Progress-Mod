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
import fr.reivaxmc.progress.network.TrailPayloads;
import fr.reivaxmc.progress.network.V12Payloads;

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
   private final String trailStage;
   private final String trailTitle;
   private final String trailObjective;
   private final int trailRifts;
   private final int trailWitnesses;
   private final boolean trailCompleted;
   private final String chapter3Stage,chapter3Title,chapter3Objective;
   private final int chapter3Traces,chapter3Witnesses;
   private final boolean chapter3Completed;
   private Button boundaryUpgrade;
   private Button territoryUpgrade;
   private Button mapUpgrade,watchUpgrade,tableUpgrade;
   private Button councilConfirm;
   private Button followTrail;
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
      String[] trail = var2.length > 11 ? var2[11].split("~", -1) : new String[0];
      this.trailStage = trail.length > 0 ? trail[0] : "LOCKED";
      this.trailTitle = trail.length > 1 ? trail[1] : "La Dette du Foyer";
      this.trailObjective = trail.length > 2 ? trail[2] : "Aucune Piste n'est actuellement suivie.";
      this.trailRifts = trail.length > 3 ? parseInt(trail[3], 0) : 0;
      this.trailWitnesses = trail.length > 4 ? parseInt(trail[4], 0) : 0;
      this.trailCompleted = trail.length > 5 && "1".equals(trail[5]);
      String[] third=var2.length>12?var2[12].split("~",-1):new String[0];
      this.chapter3Stage=third.length>0?third[0]:"LOCKED";this.chapter3Title=third.length>1?third[1]:"Les Noms retirés";
      this.chapter3Objective=third.length>2?third[2]:"La mémoire n'a pas encore ouvert cette Piste.";
      this.chapter3Traces=third.length>3?parseInt(third[3],0):0;this.chapter3Witnesses=third.length>4?parseInt(third[4],0):0;this.chapter3Completed=third.length>5&&"1".equals(third[5]);
      if (councilOpen()) this.tab = 2;
      else if ("OFFERED".equals(this.trailStage)||"OFFERED".equals(this.chapter3Stage)) this.tab = 3;
   }

   @Override
   protected void init() {
      int panelWidth = Math.min(760, this.width - 24);
      int panelHeight = Math.min(430, this.height - 16);
      int panelX = (this.width - panelWidth) / 2;
      int panelY = (this.height - panelHeight) / 2;
      int contentY = panelY + 84;
      int right = panelX + panelWidth - 18;
      boundaryUpgrade = addRenderableWidget(Button.builder(Component.literal(hasUpgrade("LISIERE_ACCORDEE") ? "ACQUIS" : "12 POINTS"),
         button -> buy("LISIERE_ACCORDEE")).bounds(right - 96, contentY + 52, 96, 20).build());
      territoryUpgrade = addRenderableWidget(Button.builder(Component.literal(hasUpgrade("ANCRAGE_ETENDU") ? "ACQUIS" : "25 POINTS"),
         button -> buy("ANCRAGE_ETENDU")).bounds(right - 96, contentY + 90, 96, 20).build());
      mapUpgrade=addRenderableWidget(Button.builder(Component.literal(hasUpgrade("CARTOGRAPHIE_RESONANTE")?"ACQUIS":"18 POINTS"),bbutton->buy("CARTOGRAPHIE_RESONANTE")).bounds(right-96,contentY+128,96,20).build());
      watchUpgrade=addRenderableWidget(Button.builder(Component.literal(hasUpgrade("VEILLE_LISIERE")?"ACQUIS":"22 POINTS"),button->buy("VEILLE_LISIERE")).bounds(right-96,contentY+166,96,20).build());
      tableUpgrade=addRenderableWidget(Button.builder(Component.literal(hasUpgrade("TABLE_COMMUNE")?"ACQUIS":"20 POINTS"),button->buy("TABLE_COMMUNE")).bounds(right-96,contentY+204,96,20).build());
      councilConfirm = addRenderableWidget(Button.builder(Component.literal("CONFIRMER CE CHOIX"), button -> castVote())
         .bounds(panelX + (panelWidth - 190) / 2, panelY + panelHeight - 58, 190, 20).build());
      followTrail = addRenderableWidget(Button.builder(Component.literal("SUIVRE CETTE PISTE"), button -> followTrail())
         .bounds(panelX + (panelWidth - 190) / 2, panelY + panelHeight - 58, 190, 20).build());
      updateUpgradeButtons();
      updateCouncilButton();
      updateTrailButton();
   }

   private void buy(String id) {
      PacketDistributor.sendToServer(new CivilizationPayloads.BuyUpgrade(id));
   }

   private boolean hasUpgrade(String id) {
      for (String value : upgrades.split(",")) if (id.equals(value)) return true;
      return false;
   }

   private void updateUpgradeButtons() {
      boolean visible = tab == 5;
      if (boundaryUpgrade != null) {
         boundaryUpgrade.visible = visible;
         boundaryUpgrade.active = visible && !hasUpgrade("LISIERE_ACCORDEE") && civilizationAvailable >= 12;
      }
      if (territoryUpgrade != null) {
         territoryUpgrade.visible = visible;
         territoryUpgrade.active = visible && !hasUpgrade("ANCRAGE_ETENDU") && civilizationAvailable >= 25;
      }
      upgradeButton(mapUpgrade,"CARTOGRAPHIE_RESONANTE",18,visible);upgradeButton(watchUpgrade,"VEILLE_LISIERE",22,visible);upgradeButton(tableUpgrade,"TABLE_COMMUNE",20,visible);
   }
   private void upgradeButton(Button b,String id,int cost,boolean visible){if(b!=null){b.visible=visible;b.active=visible&&!hasUpgrade(id)&&civilizationAvailable>=cost;}}

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

   private void followTrail() {
      if(showChapter3())PacketDistributor.sendToServer(new V12Payloads.Follow());else PacketDistributor.sendToServer(new TrailPayloads.Follow());
      if (followTrail != null) {
         followTrail.active = false;
         followTrail.setMessage(Component.literal("PISTE INSCRITE…"));
      }
   }

   private void updateTrailButton() {
      if (followTrail == null) return;
      followTrail.visible = tab == 3 && (showChapter3()?"OFFERED".equals(chapter3Stage):"OFFERED".equals(trailStage));
      followTrail.active = followTrail.visible;
   }

   public void renderBackground(GuiGraphics var1, int var2, int var3, float var4) {
      var1.fill(0, 0, this.width, this.height, -1207959552);
   }

   public void render(GuiGraphics var1, int var2, int var3, float var4) {
      this.renderBackground(var1, var2, var3, var4);
      int var5 = Math.min(760, this.width - 24);
      int var6 = Math.min(430, this.height - 16);
      int var7 = (this.width - var5) / 2;
      int var8 = (this.height - var6) / 2;
      var1.fill(var7, var8, var7 + var5, var8 + var6, -233367011);
      var1.fill(var7, var8, var7 + 5, var8 + var6, -2054356);
      var1.fill(var7 + 14, var8 + 40, var7 + var5 - 14, var8 + 41, 1430210122);
      var1.drawString(this.font, "FOYER PRINCIPAL", var7 + 18, var8 + 10, -1002190, false);
      var1.drawString(this.font, this.name, var7 + 18, var8 + 25, -724503, true);
      var1.drawString(this.font, "X", var7 + var5 - 24, var8 + 12, -4672341, false);
      String[] var9 = new String[]{"FOYER", "TERRITOIRE", "CONSEIL", "PISTES", "JOURNAL", "OPTIONS"};
      int var10 = var7 + 14;
      int var11 = var8 + 48;
      byte var12 = 4;
      int var13 = (var5 - 44 - var12 * 5) / 6;

      for (int var14 = 0; var14 < var9.length; var14++) {
         int var15 = var10 + var14 * (var13 + var12);
         int var16 = var14 == this.tab ? -13025210 : -869979602;
         var1.fill(var15, var11, var15 + var13, var11 + 24, var16);
         if (var14 == this.tab) {
            var1.fill(var15, var11 + 22, var15 + var13, var11 + 24, -2054356);
         }

         var1.drawCenteredString(this.font, var9[var14], var15 + var13 / 2, var11 + 8, var14 == this.tab ? -461589 : -3619652);
      }

      int var20 = var7 + 18;
      int var21 = var8 + 84;
      int var22 = var5 - 36;
      // Toujours à l'échelle native : le contenu reste aussi lisible que les onglets.
      if (this.tab == 0) {
         this.overview(var1, var20, var21, var22);
      } else if (this.tab == 1) {
         this.territory(var1, var20, var21, var22);
      } else if (this.tab == 2) {
         this.council(var1, var20, var21, var22);
      } else if (this.tab == 3) {
         this.trails(var1, var20, var21, var22);
      } else if (this.tab == 4) {
         this.journal(var1, var20, var21, var22);
      } else {
         this.options(var1, var20, var21, var22);
      }

      short var17 = 150;
      int var18 = var7 + var5 - var17 - 24;
      int var19 = var8 + var6 - 30;
      var1.fill(var18, var19, var18 + var17, var19 + 20, -13486019);
      var1.drawCenteredString(this.font, "FERMER", var18 + var17 / 2, var19 + 6, -790554);
      super.render(var1, var2, var3, var4);
   }

   private void overview(GuiGraphics var1, int var2, int var3, int var4) {
      this.title(var1, "ANCRAGE DU FOYER", var2, var3);
      this.line(var1, "Nom", this.name, var2, var3 + 20);
      this.line(var1, "Fondé par", this.founder, var2, var3 + 38);
      this.line(var1, "Jour de fondation", "Jour " + this.day, var2, var3 + 56);
      this.line(var1, "Position de la Borne", this.coords, var2, var3 + 74);
      this.line(var1, "Territoire principal", "Rayon " + this.radius + " blocs", var2, var3 + 92);
      this.paragraph(
         var1,
         "Cette Borne est le point d'ancrage du Foyer. Les futures mécaniques de civilisation, d'habitants et d'événements pourront s'y rattacher.",
         var2,
         var3 + 118,
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
         var3 + 20,
         var4
      );
      this.box(var1, var2, var3 + 58, var4, 42, "RAYON ACTUEL", this.radius + " blocs");
      this.box(var1, var2, var3 + 108, var4, 42, "POINT D'ANCRAGE", this.coords);
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

      this.paragraph(graphics, "Choisissez ce qui doit survivre en premier. Aucun objet n'est requis ni consommé.", x, y + 18, width);
      String[] ids = {"BASTION", "MEMORY", "SOLIDARITY"};
      int gap = 8;
      int cardWidth = (width - gap * 2) / 3;
      int cardY = y + 48;
      for (int i = 0; i < ids.length; i++) {
         int cardX = x + i * (cardWidth + gap);
         boolean selected = ids[i].equals(selectedDoctrine);
         graphics.fill(cardX, cardY, cardX + cardWidth, cardY + 54, selected ? -1140655540 : -1440602325);
         graphics.fill(cardX, cardY, cardX + (selected ? 4 : 2), cardY + 54, selected ? -2054356 : -3104453);
         graphics.drawCenteredString(this.font, doctrineLabel(ids[i]), cardX + cardWidth / 2, cardY + 8, selected ? -461589 : -1514534);
         this.paragraph(graphics, doctrineDescription(ids[i]), cardX + 8, cardY + 25, cardWidth - 16);
      }

      graphics.drawString(this.font, "VOIX DU CONSEIL", x, y + 112, -4277583, false);
      int lineY = y + 128;
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
            lineY += 14;
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

   private void trails(GuiGraphics graphics, int x, int y, int width) {
      this.title(graphics, "PISTES DU FOYER", x, y);
      if(showChapter3()){
         int color=chapter3Completed?-5713253:-461589;graphics.fill(x,y+28,x+width,y+116,-1440602325);graphics.fill(x,y+28,x+4,y+116,color);
         graphics.drawString(this.font,chapter3Title.toUpperCase(),x+14,y+40,color,false);this.paragraph(graphics,chapter3Objective,x+14,y+60,width-28);
         String status="Traces de vie : "+chapter3Traces+"/4"+(chapter3Witnesses>0?"   •   Procession : "+chapter3Witnesses+"/6":"");graphics.drawString(this.font,status,x+14,y+98,-2895929,false);
         this.paragraph(graphics,"Cette Piste reste libre : elle attend votre curiosité, jamais votre obéissance.",x,y+138,width);return;
      }
      if ("LOCKED".equals(trailStage)) {
         this.paragraph(graphics, "Aucune Piste ne répond encore. Continuez à vivre : le monde n'exige pas que vous abandonniez tout pour lui.", x, y + 28, width);
         return;
      }
      int color = trailCompleted ? -5713253 : -461589;
      graphics.fill(x, y + 28, x + width, y + 116, -1440602325);
      graphics.fill(x, y + 28, x + 4, y + 116, color);
      graphics.drawString(this.font, trailTitle.toUpperCase(), x + 14, y + 40, color, false);
      this.paragraph(graphics, trailObjective, x + 14, y + 60, width - 28);
      String status = "Fêlures : " + trailRifts + "/3" + (trailWitnesses > 0 ? "   •   Veilleurs : " + trailWitnesses + "/3" : "");
      graphics.drawString(this.font, status, x + 14, y + 98, -2895929, false);
      this.paragraph(graphics,
         "Une Piste peut être suivie sans bloquer l'exploration, la construction ou les autres projets du Foyer.", x, y + 138, width);
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
         var3 + 20,
         var4
      );
      var1.fill(var2, var3 + 72, var2 + var4, var3 + 73, 1144997450);
      var1.drawString(this.font, "• Premier Foyer établi", var2 + 8, var3 + 88, -1646120, true);
      var1.drawString(this.font, "• Borne de Fondation liée", var2 + 8, var3 + 106, -3553860, true);
   }

   private void options(GuiGraphics var1, int var2, int var3, int var4) {
      this.title(var1, "DÉCISIONS DE CIVILISATION", var2, var3);
      this.paragraph(
         var1,
         "Points gagnés : " + civilizationTotal + "   •   disponibles : " + civilizationAvailable
            + ". Les points d'Âge racontent votre progression ; seuls les points de Civilisation se dépensent ici.",
         var2,
         var3 + 18,
         var4
      );
      this.option(var1, var2, var3 + 52, var4, "LISIÈRE ACCORDÉE", hasUpgrade("LISIERE_ACCORDEE") ? "Résonance stabilisée" : "Contour perçu plus tôt");
      this.option(var1, var2, var3 + 90, var4, "ANCRAGE ÉTENDU", hasUpgrade("ANCRAGE_ETENDU") ? "Rayon 128 blocs" : "96 → 128 blocs");
      this.option(var1,var2,var3+128,var4,"CARTOGRAPHIE RÉSONANTE",hasUpgrade("CARTOGRAPHIE_RESONANTE")?"Coordonnées révélées":"Cibles exactes de la Boussole");
      this.option(var1,var2,var3+166,var4,"VEILLE DE LA LISIÈRE",hasUpgrade("VEILLE_LISIERE")?"Alerte active":"Menaces signalées au Foyer");
      this.option(var1,var2,var3+204,var4,"TABLE COMMUNE",hasUpgrade("TABLE_COMMUNE")?"Foyer partagé":"Soutien mutuel renforcé");
   }

   private boolean showChapter3(){return trailCompleted&&!"LOCKED".equals(chapter3Stage);}

   private void title(GuiGraphics var1, String var2, int var3, int var4) {
      var1.drawString(this.font, var2, var3, var4, -1002190, false);
   }

   private void line(GuiGraphics var1, String var2, String var3, int var4, int var5) {
      var1.drawString(this.font, var2, var4, var5, -4343375, true);
      var1.drawString(this.font, var3, var4 + 132, var5, -856346, true);
   }

   private void box(GuiGraphics var1, int var2, int var3, int var4, int var5, String var6, String var7) {
      var1.fill(var2, var3, var2 + var4, var3 + var5, -1440602325);
      var1.fill(var2, var3, var2 + 3, var3 + var5, -3104453);
      var1.drawString(this.font, var6, var2 + 12, var3 + 8, -4277583, false);
      var1.drawString(this.font, var7, var2 + 12, var3 + 24, -724760, true);
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
      int var6 = Math.min(760, this.width - 24);
      int var7 = Math.min(430, this.height - 16);
      int var8 = (this.width - var6) / 2;
      int var9 = (this.height - var7) / 2;
      if (this.hit(var1, var3, var8 + var6 - 40, var9 + 10, 30, 28)) {
         this.onClose();
         return true;
      } else {
         int var10 = var8 + 14;
         int var11 = var9 + 48;
         byte var12 = 4;
         int var13 = (var6 - 44 - var12 * 5) / 6;

         for (int var14 = 0; var14 < 6; var14++) {
            int var15 = var10 + var14 * (var13 + var12);
            if (this.hit(var1, var3, var15, var11, var13, 24)) {
               this.tab = var14;
               updateUpgradeButtons();
               updateCouncilButton();
               updateTrailButton();
               return true;
            }
         }

         if (tab == 2 && councilOpen()) {
            double localX = var1 - (var8 + 18);
            double localY = var3 - (var9 + 84);
            int contentWidth = var6 - 36;
            int gap = 8;
            int cardWidth = (contentWidth - gap * 2) / 3;
            String[] ids = {"BASTION", "MEMORY", "SOLIDARITY"};
            for (int i = 0; i < ids.length; i++) {
               int cardX = i * (cardWidth + gap);
               if (hit(localX, localY, cardX, 48, cardWidth, 54)) {
                  selectedDoctrine = ids[i];
                  voteSent = false;
                  updateCouncilButton();
                  return true;
               }
            }
         }

         short var17 = 150;
         int var18 = var8 + var6 - var17 - 24;
         int var16 = var9 + var7 - 30;
         if (this.hit(var1, var3, var18, var16, var17, 20)) {
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
