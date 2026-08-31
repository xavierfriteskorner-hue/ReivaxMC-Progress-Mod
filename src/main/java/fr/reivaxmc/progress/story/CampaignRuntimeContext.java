package fr.reivaxmc.progress.story;

import java.util.function.Supplier;

public final class CampaignRuntimeContext {
   private static final ThreadLocal<Object> SERVER = new ThreadLocal<>();

   private CampaignRuntimeContext() {
   }

   public static Object currentServer() {
      return SERVER.get();
   }

   public static <T> T withServer(Object var0, Supplier<T> var1) {
      Object var2 = SERVER.get();
      if (var0 == null) {
         SERVER.remove();
      } else {
         SERVER.set(var0);
      }

      Object var3;
      try {
         var3 = var1.get();
      } finally {
         if (var2 == null) {
            SERVER.remove();
         } else {
            SERVER.set(var2);
         }
      }

      return (T)var3;
   }
}
