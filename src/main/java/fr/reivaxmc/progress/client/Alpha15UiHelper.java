package fr.reivaxmc.progress.client;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

public final class Alpha15UiHelper {
   private Alpha15UiHelper() {
   }

   public static int drawWrapped2(GuiGraphics var0, Font var1, String var2, int var3, int var4, int var5, boolean var6, int var7) {
      if (var2 == null) {
         var2 = "";
      }

      int var8 = var2.indexOf(" · ");
      if (var8 > 0) {
         String var9 = var2.substring(0, var8);
         String var10 = var2.substring(var8 + 3);
         if (var10.startsWith(var9 + " ")) {
            var2 = var9 + " · " + var10.substring(var9.length() + 1);
         }
      }

      String var14 = var1.plainSubstrByWidth(var2, var7);
      if (var14 == null) {
         var14 = "";
      }

      int var15 = var14.length();
      if (var15 < var2.length()) {
         int var11 = var14.lastIndexOf(32);
         if (var11 > Math.max(8, var14.length() / 2)) {
            var14 = var14.substring(0, var11);
            var15 = var11 + 1;
         }
      }

      int var16 = var0.drawString(var1, var14, var3, var4, var5, var6);
      if (var15 < var2.length()) {
         String var12 = var2.substring(Math.min(var15, var2.length())).trim();
         String var13 = var1.plainSubstrByWidth(var12, var7);
         if (var13.length() < var12.length() && var13.length() > 1) {
            var13 = var13.substring(0, var13.length() - 1) + "…";
         }

         var0.drawString(var1, var13, var3, var4 + 12, var5, var6);
      }

      return var16;
   }
}
