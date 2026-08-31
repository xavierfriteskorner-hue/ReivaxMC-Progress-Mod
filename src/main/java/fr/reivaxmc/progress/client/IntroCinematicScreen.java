package fr.reivaxmc.progress.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class IntroCinematicScreen extends Screen {
   private final long start = System.currentTimeMillis();
   private static final long DURATION = 26000L;

   public IntroCinematicScreen() {
      super(Component.literal("REIVAX MC — Origines"));
   }

   public void renderBackground(GuiGraphics var1, int var2, int var3, float var4) {
      var1.fill(0, 0, this.width, this.height, -16579579);
   }

   public void render(GuiGraphics var1, int var2, int var3, float var4) {
      long var5 = System.currentTimeMillis() - this.start;
      int var7 = this.width / 2;
      int var8 = this.height / 2;
      int var9 = 4 + (int)(5.0 * (0.5 + 0.5 * Math.sin((double)var5 * 5.2E-4)));
      int var10 = 0xFF000000 | 3 + var9 << 16 | 5 + var9 << 8 | 7 + var9;
      var1.fill(0, 0, this.width, this.height, var10);
      int var11 = Math.max(12, this.height / 14);
      var1.fill(0, 0, this.width, var11, -402653184);
      var1.fill(0, this.height - var11, this.width, this.height, -402653184);
      var1.fill(0, var11, 8, this.height - var11, -1711276032);
      var1.fill(this.width - 8, var11, this.width, this.height - var11, -1711276032);
      double var12 = Math.min(1.0, Math.max(0.0, (double)(var5 - 1800L) / 5000.0));

      for (int var14 = 0; var14 < 15; var14++) {
         double var15 = (double)var5 * (9.0E-5 + (double)var14 * 4.8E-6) + (double)var14 * 1.337;
         int var17 = (int)(Math.sin(var15) * (double)this.width * (0.12 + (double)var14 * 0.012) * var12);
         int var18 = (int)(Math.cos(var15 * 1.43) * 13.0 * var12);
         int var19 = 24 + var14 * 17 % Math.max(30, this.height - var11 * 2 - 30);
         int var20 = 8 + (int)(23.0 * var12 * (0.5 + 0.5 * Math.sin(var15 * 2.1)));
         int var21 = var14 % 4 == 0 ? 13223096 : 10397608;
         int var22 = Math.min(70, var20) << 24 | var21;
         int var23 = var7 + var17 + (var14 % 3 - 1) * 7;
         var1.fill(var23, var8 - var19 / 2 + var18, var23 + 1, var8 + var19 / 2 + var18, var22);
      }

      int var24 = (int)(Math.sin((double)var5 * 0.00122) * 9.0 * var12);
      int var25 = (int)(Math.sin((double)var5 * 8.3E-4 + 1.8) * 13.0 * var12);
      var1.fill(var7 - 2 + var24, var8 - 88, var7 + var24, var8 + 88, 887809756);
      var1.fill(var7 + 7 - var25, var8 - 58, var7 + 9 - var25, var8 + 58, 614377384);
      var1.fill(var7 - 42 + var25 / 2, var8 - 1, var7 + 38 + var24 / 2, var8 + 1, 416731848);
      this.flash(var1, var5, 3650L, 170L, 753460442);
      this.flash(var1, var5, 10400L, 220L, 819847647);
      this.flash(var1, var5, 18150L, 250L, 653323491);
      this.flash(var1, var5, 24400L, 320L, 955576809);
      if (var5 >= 2800L && var5 < 7200L) {
         this.text(var1, "QUELQUE CHOSE A CHANGÉ.", var8 - 5, fade(var5, 2800L, 900L, 7200L, 900L), 15328732);
      } else if (var5 >= 8800L && var5 < 15100L) {
         this.text(var1, "Le monde n'est plus tout à fait silencieux.", var8 - 5, fade(var5, 8800L, 1100L, 15100L, 1000L), 14210768);
      } else if (var5 >= 15700L && var5 < 19600L) {
         this.text(var1, "Quelque chose s'est souvenu de vous.", var8 - 5, fade(var5, 15700L, 900L, 19600L, 900L), 15657697);
      } else if (var5 >= 19800L && var5 < 25500L) {
         int var16 = fade(var5, 19800L, 900L, 25500L, 1000L);
         int var26 = Math.max(0, Math.min(255, var16 / 2));
         int var27 = var26 << 24 | 13223096;
         var1.fill(var7 - 72, var8 - 38, var7 + 72, var8 - 37, var27);
         this.text(var1, "ÂGE I", var8 - 20, var16, 13223356);
         this.text(var1, "ORIGINES", var8 + 3, var16, 15986662);
      }

      if (var5 >= 26000L) {
         Minecraft.getInstance().setScreen(null);
      }
   }

   private void flash(GuiGraphics var1, long var2, long var4, long var6, int var8) {
      long var9 = Math.abs(var2 - var4);
      if (var9 < var6) {
         int var11 = var8 >>> 24 & 0xFF;
         double var12 = 1.0 - (double)var9 / (double)var6;
         int var14 = (int)((double)var11 * var12);
         var1.fill(0, 0, this.width, this.height, var14 << 24 | var8 & 16777215);
      }
   }

   private void text(GuiGraphics var1, String var2, int var3, int var4, int var5) {
      if (var4 > 0) {
         int var6 = Math.min(255, var4) << 24 | var5 & 16777215;
         var1.drawCenteredString(this.font, var2, this.width / 2, var3, var6);
      }
   }

   private static int fade(long var0, long var2, long var4, long var6, long var8) {
      double var10 = 1.0;
      if (var0 < var2 + var4) {
         var10 = Math.max(0.0, Math.min(1.0, (double)(var0 - var2) / (double)var4));
      }

      if (var0 > var6 - var8) {
         var10 = Math.min(var10, Math.max(0.0, (double)(var6 - var0) / (double)var8));
      }

      return (int)(255.0 * var10);
   }

   public boolean shouldCloseOnEsc() {
      return false;
   }

   public boolean isPauseScreen() {
      return false;
   }
}
