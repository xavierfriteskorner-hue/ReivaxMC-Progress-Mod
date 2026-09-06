package fr.reivaxmc.progress.client;

import fr.reivaxmc.progress.network.JournalPayloads;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

/** Journal intime hérité : mémoire inconnue, chronique commune et pages propres à chaque joueur. */
public final class DestinyBookScreen extends Screen {
   private final String chronicle;
   private final String shared;
   private final String personal;
   private int tab;
   private EditBox note;
   private Button privateButton;
   private Button shareButton;

   public DestinyBookScreen() {
      this("", "", "");
   }

   public DestinyBookScreen(String chronicle, String shared, String personal) {
      super(Component.literal("Journal hérité"));
      this.chronicle = chronicle == null ? "" : chronicle;
      this.shared = shared == null ? "" : shared;
      this.personal = personal == null ? "" : personal;
   }

   @Override
   protected void init() {
      int w = Math.min(660, this.width - 40);
      int h = Math.min(410, this.height - 40);
      int x = (this.width - w) / 2;
      int y = (this.height - h) / 2;
      note = new EditBox(this.font, x + 28, y + h - 76, w - 296, 20, Component.literal("Écrire une note"));
      note.setMaxLength(180);
      note.setHint(Component.literal("Votre note personnelle…"));
      addRenderableWidget(note);
      privateButton = addRenderableWidget(Button.builder(Component.literal("CONSERVER"), button -> submit(false)).bounds(x + w - 252, y + h - 76, 104, 20).build());
      shareButton = addRenderableWidget(Button.builder(Component.literal("PARTAGER"), button -> submit(true)).bounds(x + w - 140, y + h - 76, 104, 20).build());
      updateWidgets();
   }

   private void submit(boolean share) {
      String text = note.getValue().trim();
      if (text.isBlank()) return;
      PacketDistributor.sendToServer(new JournalPayloads.SaveNote(text, share));
   }

   private void updateWidgets() {
      boolean visible = tab == 2;
      if (note != null) note.setVisible(visible);
      if (privateButton != null) privateButton.visible = visible;
      if (shareButton != null) shareButton.visible = visible;
   }

   @Override public void renderBackground(GuiGraphics g, int x, int y, float p) { g.fill(0, 0, width, height, -1442840576); }

   @Override
   public void render(GuiGraphics g, int mx, int my, float partial) {
      renderBackground(g, mx, my, partial);
      int w = Math.min(660, width - 40);
      int h = Math.min(410, height - 40);
      int x = (width - w) / 2;
      int y = (height - h) / 2;
      g.fill(x, y, x + w, y + h, -267118828);
      g.fill(x, y, x + 5, y + h, -3760051);
      g.drawString(font, "JOURNAL INTIME", x + 24, y + 20, -1916296, false);
      g.drawString(font, "Propriétaire d'origine : inconnu", x + 24, y + 38, -6645094, false);
      String[] tabs = {"PAGES HÉRITÉES", "CHRONIQUE COMMUNE", "MES PAGES"};
      int tabWidth = (w - 48) / 3;
      for (int i = 0; i < tabs.length; i++) {
         int tx = x + 24 + i * tabWidth;
         g.fill(tx, y + 62, tx + tabWidth - 6, y + 88, i == tab ? -13025210 : -869979602);
         if (i == tab) g.fill(tx, y + 86, tx + tabWidth - 6, y + 88, -2054356);
         g.drawCenteredString(font, tabs[i], tx + (tabWidth - 6) / 2, y + 71, i == tab ? -461589 : -3619652);
      }
      int contentY = y + 110;
      if (tab == 0) inherited(g, x + 30, contentY, w - 60);
      else if (tab == 1) entries(g, merge(shared, chronicle), x + 30, contentY, w - 60, false);
      else entries(g, personal, x + 30, contentY, w - 60, true);
      super.render(g, mx, my, partial);
   }

   private void inherited(GuiGraphics g, int x, int y, int width) {
      g.drawString(font, "« Je ne sais plus depuis combien de temps cette salle attend. »", x, y, -1383467, false);
      wrap(g, "L'encre ancienne change parfois lorsque personne ne regarde. Certaines pages semblent parler de vous avant même votre arrivée.", x, y + 28, width, -2895929);
      wrap(g, "Les mots suivants sont plus récents, mais vous ne vous souvenez pas les avoir écrits : survivre ne suffit pas ; il faudra choisir ce que votre civilisation refuse de devenir.", x, y + 84, width, -2895929);
      g.drawString(font, "La signature a été arrachée.", x, y + 154, -724503, true);
   }

   private String merge(String first, String second) {
      return first.isBlank() ? second : second.isBlank() ? first : first + "\n" + second;
   }

   private void entries(GuiGraphics g, String data, int x, int y, int width, boolean own) {
      if (data.isBlank()) {
         wrap(g, own ? "Ces pages vous appartiennent encore. Écrivez sans partager, ou confiez volontairement une note au Foyer."
            : "La chronique est encore silencieuse.", x, y, width, -5725286);
         return;
      }
      String[] lines = data.split("\n");
      int shown = 0;
      for (String line : lines) {
         if (line.isBlank() || shown >= 6) continue;
         String[] fields = line.split("¦", -1);
         String text = own ? line : formatEntry(fields, line);
         g.fill(x, y - 4, x + width, y + 29, -1440602325);
         wrap(g, "• " + text, x + 8, y + 4, width - 16, -1646120);
         y += 38;
         shown++;
      }
   }

   private String formatEntry(String[] fields, String fallback) {
      if (fields.length >= 4) return "Jour " + fields[0] + " — " + fields[1] + " : " + fields[2];
      if (fields.length >= 3) return "Jour " + fields[0] + " — " + fields[1] + " : " + fields[2];
      return fallback;
   }

   private void wrap(GuiGraphics g, String text, int x, int y, int max, int color) {
      for (var line : font.split(Component.literal(text), max)) {
         g.drawString(font, line, x, y, color, false);
         y += 13;
      }
   }

   @Override
   public boolean mouseClicked(double mx, double my, int button) {
      int w = Math.min(660, width - 40);
      int h = Math.min(410, height - 40);
      int x = (width - w) / 2;
      int y = (height - h) / 2;
      int tabWidth = (w - 48) / 3;
      if (button == 0 && my >= y + 62 && my <= y + 88) {
         for (int i = 0; i < 3; i++) if (mx >= x + 24 + i * tabWidth && mx <= x + 24 + (i + 1) * tabWidth - 6) {
            tab = i;
            updateWidgets();
            return true;
         }
      }
      return super.mouseClicked(mx, my, button);
   }

   @Override public boolean isPauseScreen() { return false; }
}
