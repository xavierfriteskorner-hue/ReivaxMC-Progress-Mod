package fr.reivaxmc.progress.client;

import fr.reivaxmc.progress.network.Alpha18FPayloads;
import fr.reivaxmc.progress.story.Alpha18FClientState;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.PacketDistributor;

public final class StoryLauncherScreen extends Screen {
   private boolean requestPending;
   private long requestAt;

   public StoryLauncherScreen() {
      super(Component.literal("REIVAX MC — Mode Histoire"));
   }

   public void render(GuiGraphics var1, int var2, int var3, float var4) {
      if (this.requestPending && System.currentTimeMillis() - this.requestAt > 60000L && !Alpha18FClientState.started) {
         this.requestPending = false;
      }

      var1.fill(0, 0, this.width, this.height, -385414899);
      var1.fill(0, 0, this.width, 2, -11511451);
      var1.fill(0, this.height - 1, this.width, this.height, -14670288);
      int var5 = this.width / 2;
      int var6 = Math.min(470, Math.max(300, this.width - 36));
      int var7 = Math.min(230, Math.max(176, this.height - 34));
      int var8 = var5 - var6 / 2;
      int var9 = (this.height - var7) / 2;
      var1.fill(var8 + 5, var9 + 6, var8 + var6 + 5, var9 + var7 + 6, -2013265920);
      var1.fill(var8, var9, var8 + var6, var9 + var7, -267710957);
      var1.fill(var8, var9, var8 + var6, var9 + 2, -9142903);
      var1.fill(var8, var9 + var7 - 1, var8 + var6, var9 + var7, -14143687);
      var1.drawCenteredString(this.font, "REIVAX MC", var5, var9 + 27, -855571);
      var1.drawCenteredString(this.font, "MODE HISTOIRE", var5, var9 + 50, -4998461);
      var1.fill(var5 - 64, var9 + 72, var5 + 64, var9 + 73, -12564396);
      var1.drawCenteredString(this.font, "L'aventure n'a pas encore commencé.", var5, var9 + 91, -2565153);
      var1.drawCenteredString(this.font, PartyUiState18F.modeLine(), var5, var9 + 108, -7169371);
      int var10 = Math.min(340, Math.max(250, var6 - 72));
      byte var11 = 34;
      int var12 = var5 - var10 / 2;
      int var13 = var9 + var7 - 70;
      boolean var14 = var2 >= var12 && var2 < var12 + var10 && var3 >= var13 && var3 < var13 + var11;
      long var15 = System.currentTimeMillis() / 55L % 48L;
      int var17 = (int)(var15 <= 24L ? var15 : 48L - var15);
      int var18 = 48 + var17 * 4;
      int var19 = Math.min(150, var18) << 24 | 8885153;
      var1.fill(var12 - 5, var13 - 5, var12 + var10 + 5, var13 + var11 + 5, var19);
      var1.fill(var12 - 2, var13 - 2, var12 + var10 + 2, var13 + var11 + 2, var14 ? -663980155 : -1018473118);
      var1.fill(var12, var13, var12 + var10, var13 + var11, var14 ? -14341067 : -15196634);
      var1.fill(var12, var13, var12 + var10, var13 + 1, var14 ? -2501432 : -6643028);
      String var20 = this.requestPending ? PartyUiState18F.pendingButtonLabel() : PartyUiState18F.idleButtonLabel();
      int var21 = this.requestPending ? -8550768 : (var14 ? -1 : -987416);
      var1.drawCenteredString(this.font, var20, var5, var13 + 13, var21);
      var1.drawCenteredString(this.font, "Une seule fois par monde", var5, var13 + var11 + 12, -9866626);
      var1.drawCenteredString(this.font, "Échap : revenir au jeu", var5, var9 + var7 - 13, -11577503);
   }

   public boolean mouseClicked(double var1, double var3, int var5) {
      if (var5 == 0 && !this.requestPending && !Alpha18FClientState.started) {
         int var6 = this.width / 2;
         int var7 = Math.min(470, Math.max(300, this.width - 36));
         int var8 = Math.min(230, Math.max(176, this.height - 34));
         int var9 = (this.height - var8) / 2;
         int var10 = Math.min(340, Math.max(250, var7 - 72));
         byte var11 = 34;
         int var12 = var6 - var10 / 2;
         int var13 = var9 + var8 - 70;
         if (var1 >= (double)var12 && var1 < (double)(var12 + var10) && var3 >= (double)var13 && var3 < (double)(var13 + var11)) {
            this.requestPending = true;
            this.requestAt = System.currentTimeMillis();
            PacketDistributor.sendToServer(new Alpha18FPayloads.StoryStartRequest(), new CustomPacketPayload[0]);
            return true;
         }
      }

      return super.mouseClicked(var1, var3, var5);
   }

   public boolean isPauseScreen() {
      return false;
   }
}
