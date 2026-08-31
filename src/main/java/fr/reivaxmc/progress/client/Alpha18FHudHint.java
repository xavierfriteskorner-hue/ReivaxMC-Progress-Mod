package fr.reivaxmc.progress.client;

import fr.reivaxmc.progress.story.Alpha18FClientState;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import net.minecraft.client.gui.GuiGraphics;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent.Post;

@EventBusSubscriber(
   modid = "reivaxmc_progress",
   value = {Dist.CLIENT}
)
public final class Alpha18FHudHint {
   private Alpha18FHudHint() {
   }

   @SubscribeEvent
   public static void render(Post var0) {
      if (Alpha18FClientState.managed) {
         try {
            Object var1 = Class.forName("net.minecraft.client.Minecraft").getMethod("getInstance").invoke(null);
            if (var1 == null || field(var1, "player") == null) {
               return;
            }

            if (field(var1, "screen") != null) {
               return;
            }

            GuiGraphics var2 = var0.getGuiGraphics();
            Object var3 = field(var1, "font");
            if (var2 == null || var3 == null) {
               return;
            }

            String var4 = Alpha18FClientState.started ? "[ NUM 3 ]  Ouvrir REIVAX" : "[ NUM 3 ]  Ouvrir REIVAX  ·  Lancer l'histoire";
            int var5 = number(call(var2, "guiWidth"), 320);
            int var6 = number(call(var3, "width", var4), 190);
            int var7 = Math.max(8, var5 - var6 - 20);
            byte var8 = 12;
            call(var2, "fill", var7 - 7, var8 - 5, var7 + var6 + 7, var8 + 14, -1475737078);
            call(var2, "fill", var7 - 7, var8 - 5, var7 - 4, var8 + 14, -3561396);
            call(var2, "drawString", var3, var4, var7, Integer.valueOf(var8), -659224, true);
         } catch (Throwable var9) {
         }
      }
   }

   private static int number(Object var0, int var1) {
      return var0 instanceof Number var2 ? var2.intValue() : var1;
   }

   private static Object field(Object var0, String var1) throws Exception {
      for (Class var2 = var0.getClass(); var2 != null; var2 = var2.getSuperclass()) {
         try {
            Field var3 = var2.getDeclaredField(var1);
            var3.setAccessible(true);
            return var3.get(var0);
         } catch (NoSuchFieldException var4) {
         }
      }

      throw new NoSuchFieldException(var1);
   }

   private static Object call(Object var0, String var1, Object... var2) throws Exception {
      for (Method var6 : var0.getClass().getMethods()) {
         if (var6.getName().equals(var1) && var6.getParameterCount() == var2.length) {
            try {
               return var6.invoke(var0, var2);
            } catch (IllegalArgumentException var9) {
            }
         }
      }

      for (Method var13 : var0.getClass().getDeclaredMethods()) {
         if (var13.getName().equals(var1) && var13.getParameterCount() == var2.length) {
            try {
               var13.setAccessible(true);
               return var13.invoke(var0, var2);
            } catch (IllegalArgumentException var8) {
            }
         }
      }

      throw new NoSuchMethodException(var1 + "/" + var2.length);
   }
}
