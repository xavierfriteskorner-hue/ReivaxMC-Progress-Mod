package fr.reivaxmc.progress.client;

import fr.reivaxmc.progress.network.ClientCampaignState;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class ProgressScreen extends Screen {
   private static final String[] TABS = new String[]{"CAMPAGNE", "QUÊTES", "PROGRESSION", "RÉCOMPENSES", "CHRONOLOGIE", "ARCHIVES"};
   private int tab;
   private int px;
   private int py;
   private int pw;
   private int ph;
   private int tabY;
   private int tabH;

   public ProgressScreen() {
      super(Component.literal("ReivaxMC Progress"));
   }

   public void renderBackground(GuiGraphics g, int a, int b, float p) {
      g.fill(0, 0, this.width, this.height, -1879048192);
   }

   public void render(GuiGraphics g, int mx, int my, float p) {
      this.renderBackground(g, mx, my, p);
      this.pw = Math.min(920, this.width - 28);
      this.ph = Math.min(570, this.height - 28);
      this.px = (this.width - this.pw) / 2;
      this.py = (this.height - this.ph) / 2;
      g.fill(this.px + 5, this.py + 5, this.px + this.pw + 5, this.py + this.ph + 5, 1426063360);
      g.fill(this.px, this.py, this.px + this.pw, this.py + this.ph, -267710450);
      g.fill(this.px, this.py, this.px + 5, this.py + this.ph, -4023733);
      g.drawString(this.font, "REIVAXMC PROGRESS", this.px + 24, this.py + 19, -1849474, false);
      g.drawString(this.font, "ÂGE I • ORIGINES", this.px + this.pw - 145, this.py + 19, -9061227, false);
      this.tabY = this.py + 48;
      this.tabH = 28;
      int tw = (this.pw - 24) / TABS.length;

      for (int i = 0; i < TABS.length; i++) {
         int x = this.px + 12 + i * tw;
         boolean active = i == this.tab;
         g.fill(x, this.tabY, x + tw - 3, this.tabY + this.tabH, active ? -14271695 : -15459304);
         g.drawCenteredString(this.font, TABS[i], x + (tw - 3) / 2, this.tabY + 10, active ? -2047890 : -5658981);
      }

      int x = this.px + 28;
      int y = this.tabY + 40;
      int w = this.pw - 56;
      int h = this.ph - 102;
      switch (this.tab) {
         case 0:
            this.campaign(g, x, y, w, h);
            break;
         case 1:
            this.quests(g, x, y, w, h);
            break;
         case 2:
            this.progress(g, x, y, w, h);
            break;
         case 3:
            this.rewards(g, x, y, w, h);
            break;
         case 4:
            this.timeline(g, x, y, w, h);
            break;
         default:
            this.archives(g, x, y, w, h);
      }
   }

   private void campaign(GuiGraphics g, int x, int y, int w, int h) {
      this.title(g, "RETROUVER VOS ORIGINES", x, y);
      this.box(g, x, y + 30, w, 105);
      this.wrap(
         g,
         "Vous vous êtes éveillés dans un monde dont vous ignorez tout. Pourtant, des traces anciennes semblent reconnaître votre présence. Survivez, bâtissez et suivez ces traces pour découvrir qui vous êtes, d'où vous venez et ce qui s'est produit avant votre arrivée.",
         x + 16,
         y + 46,
         w - 32,
         15,
         -2567225
      );
      this.title(g, "MISSION ACTUELLE", x, y + 140);
      this.box(g, x, y + 165, w, 98);
      this.wrap(g, ClientCampaignState.mission, x + 16, y + 180, w - 32, 15, -1515312);
   }

   private void quests(GuiGraphics g, int x, int y, int w, int h) {
      this.title(g, "QUÊTE PRINCIPALE", x, y);
      this.box(g, x, y + 30, w, 105);
      this.wrap(g, ClientCampaignState.mission, x + 16, y + 46, w - 32, 15, -1515312);
      if (ReivaxMCProgressClient.hasTarget()) {
         this.box(g, x, y + 145, w, 70);
         g.drawString(this.font, "RÉSONANCE DES ORIGINES", x + 16, y + 160, -8795748, false);
         g.drawString(
            this.font,
            "Intensité : " + ReivaxMCProgressClient.resonanceStrength() + "   Direction : " + ReivaxMCProgressClient.resonanceArrow(),
            x + 16,
            y + 182,
            -2567225,
            false
         );
      }

      if ("BUILD_FIRST_HOME".equals(ClientCampaignState.stage) && !ClientCampaignState.matrixDiscovered && !ClientCampaignState.artifacts.isEmpty()) {
         this.title(g, "QUÊTE SECONDAIRE — UN RÉCEPTACLE VIDE", x, y + 215);
         this.box(g, x, y + 242, w, 68);
         this.wrap(
            g,
            "Une faible Résonance persiste parmi les ruines de la Stèle. Quelque chose d'autre semble avoir réagi au Fragment. Fouillez les environs si vous souhaitez comprendre ce signal.",
            x + 16,
            y + 257,
            w - 32,
            15,
            -4666437
         );
      }
   }

   private void progress(GuiGraphics g, int x, int y, int w, int h) {
      this.title(g, "CIVILISATION", x, y);
      this.box(g, x, y + 30, w, 150);
      String foyer = ClientCampaignState.foundationPlaced
         ? (ClientCampaignState.foundationName.isEmpty() ? "Foyer principal" : ClientCampaignState.foundationName)
         : "AUCUN";
      String[] a = new String[]{
         "Ère : Origines",
         "Foyer principal : " + foyer,
         "Territoire principal : "
            + (ClientCampaignState.foundationPlaced ? ClientCampaignState.territoryRadius + " blocs de rayon" : "Aucun territoire actif"),
         "État : " + (ClientCampaignState.migration ? "MIGRATION" : (ClientCampaignState.foundationPlaced ? "ÉTABLI" : "À FONDER")),
         "Sites historiques : " + ClientCampaignState.historicalCount,
         "Reliques enregistrées : " + this.count(ClientCampaignState.artifacts)
      };
      int yy = y + 48;

      for (String s : a) {
         g.drawString(this.font, s, x + 18, yy, -2304311, false);
         yy += 20;
      }

      this.title(g, "PROGRESSION DE L'ÂGE", x, y + 198);
      this.box(g, x, y + 225, w, 58);
      g.drawString(this.font, ClientCampaignState.progress + " / 1000 points", x + 18, y + 240, -2567225, false);
      this.bar(g, x + 18, y + 266, w - 36, (float)ClientCampaignState.progress / 1000.0F);
   }

   private void rewards(GuiGraphics g, int x, int y, int w, int h) {
      this.title(g, "RÉCOMPENSES DE L'ÂGE I", x, y);
      this.box(g, x, y + 30, w, 195);
      String[] a = new String[]{
         "200 pts   •   Premier palier",
         "400 pts   •   Développement du Foyer",
         "600 pts   •   Nouvelle étape de civilisation",
         "800 pts   •   Héritage des Origines",
         "1000 pts  •   Fin de l'Âge I"
      };
      int yy = y + 55;

      for (String s : a) {
         g.drawString(this.font, s, x + 20, yy, ClientCampaignState.progress >= Integer.parseInt(s.substring(0, s.indexOf(32))) ? -9192302 : -7697532, false);
         yy += 29;
      }

      g.drawString(
         this.font, "Les récompenses ne révéleront jamais une technologie que votre civilisation n'a pas encore découverte.", x + 18, y + 236, -5790823, false
      );
   }

   private void timeline(GuiGraphics g, int x, int y, int w, int h) {
      this.title(g, "CHRONOLOGIE DU MONDE", x, y);
      int yy = y + 35;
      if (ClientCampaignState.timeline.isEmpty()) {
         g.drawString(this.font, "Votre histoire n'a pas encore commencé.", x, yy, -7368309, false);
      } else {
         for (String line : ClientCampaignState.timeline.split("\\n")) {
            if (yy > y + h - 50) {
               break;
            }

            String[] p = line.split("¦", -1);
            if (p.length >= 4) {
               this.box(g, x, yy, w, 55);
               g.drawString(this.font, "Jour " + p[0] + " — " + p[2], x + 14, yy + 10, -2046857, false);
               Alpha15UiHelper.drawWrapped2(g, this.font, p[1] + " • " + p[3], x + 14, yy + 30, -4210504, false, w - 28);
               yy += 66;
            }
         }
      }
   }

   private void archives(GuiGraphics g, int x, int y, int w, int h) {
      this.title(g, "ARCHIVES DES ORIGINES", x, y);
      this.box(g, x, y + 30, w, 70);
      g.drawString(
         this.font,
         "Matrice des Origines : " + (ClientCampaignState.matrixDiscovered ? "DÉCOUVERTE" : "INCONNUE"),
         x + 18,
         y + 48,
         ClientCampaignState.matrixDiscovered ? -8927590 : -7762553,
         false
      );
      g.drawString(this.font, "Fragments reconnus : " + this.count(ClientCampaignState.artifacts), x + 18, y + 72, -2764603, false);
      int yy = y + 125;

      for (String line : ClientCampaignState.artifacts.split("\\n")) {
         if (!line.isEmpty()) {
            String[] p = line.split("¦", -1);
            if (p.length >= 6) {
               this.box(g, x, yy, w, 66);
               g.drawString(this.font, "◆ " + p[1], x + 16, yy + 11, -8927590, false);
               g.drawString(this.font, "Découvert par " + p[2] + " • Jour " + p[3] + " • " + p[4], x + 16, yy + 31, -3553860, false);
               g.drawString(this.font, "Signification : " + p[5], x + 16, yy + 48, -6710122, false);
               yy += 76;
            }
         }
      }
   }

   private int count(String s) {
      return s != null && !s.isEmpty() ? s.split("\\n").length : 0;
   }

   private void title(GuiGraphics g, String s, int x, int y) {
      g.drawString(this.font, s, x, y, -2047634, false);
   }

   private void box(GuiGraphics g, int x, int y, int w, int h) {
      g.fill(x, y, x + w, y + h, -15656939);
      g.fill(x, y, x + 3, y + h, -12684969);
   }

   private void bar(GuiGraphics g, int x, int y, int w, float f) {
      g.fill(x, y, x + w, y + 6, -15063263);
      g.fill(x, y, x + (int)((float)w * Math.max(0.0F, Math.min(1.0F, f))), y + 6, -11364753);
   }

   private void wrap(GuiGraphics g, String s, int x, int y, int max, int step, int color) {
      int yy = y;

      for (String para : s.split("\\n")) {
         String line = "";

         for (String q : para.split(" ")) {
            String n = line.isEmpty() ? q : line + " " + q;
            if (this.font.width(n) > max && !line.isEmpty()) {
               g.drawString(this.font, line, x, yy, color, false);
               yy += step;
               line = q;
            } else {
               line = n;
            }
         }

         if (!line.isEmpty()) {
            g.drawString(this.font, line, x, yy, color, false);
            yy += step;
         }

         yy += 3;
      }
   }

   public boolean mouseClicked(double mx, double my, int b) {
      if (b == 0 && my >= (double)this.tabY && my < (double)(this.tabY + this.tabH)) {
         int tw = (this.pw - 24) / TABS.length;
         int i = (int)((mx - (double)(this.px + 12)) / (double)tw);
         if (i >= 0 && i < TABS.length) {
            this.tab = i;
            return true;
         }
      }

      return super.mouseClicked(mx, my, b);
   }

   public boolean isPauseScreen() {
      return false;
   }
}
