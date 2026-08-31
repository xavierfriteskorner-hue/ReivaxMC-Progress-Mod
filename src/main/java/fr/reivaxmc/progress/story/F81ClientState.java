package fr.reivaxmc.progress.story;

import java.lang.reflect.Field;

public final class F81ClientState {
   private static volatile boolean qaMask;
   private static String realTimeline;
   private static String realArtifacts;
   private static String maskedTimeline;
   private static String maskedArtifacts;

   private F81ClientState() {
   }

   public static boolean qaMask() {
      return qaMask;
   }

   public static synchronized void setQaMask(boolean var0) {
      if (qaMask == var0) {
         if (var0) {
            enforceMask();
         }
      } else {
         qaMask = var0;
         if (var0) {
            captureCurrentAsReal();
            enforceMask();
         } else {
            restoreReal();
         }
      }
   }

   public static synchronized void enforceMask() {
      if (qaMask) {
         try {
            Class var0 = Class.forName("fr.reivaxmc.progress.network.ClientCampaignState");
            String var1 = string(getStatic(var0, "timeline"));
            String var2 = string(getStatic(var0, "artifacts"));
            if (maskedTimeline == null || !var1.equals(maskedTimeline)) {
               realTimeline = var1;
            }

            if (maskedArtifacts == null || !var2.equals(maskedArtifacts)) {
               realArtifacts = var2;
            }

            maskedTimeline = maskTimeline(realTimeline);
            maskedArtifacts = maskArtifacts(realArtifacts);
            setStatic(var0, "timeline", maskedTimeline);
            setStatic(var0, "artifacts", maskedArtifacts);
         } catch (Throwable var3) {
         }
      }
   }

   private static void captureCurrentAsReal() {
      try {
         Class var0 = Class.forName("fr.reivaxmc.progress.network.ClientCampaignState");
         String var1 = string(getStatic(var0, "timeline"));
         String var2 = string(getStatic(var0, "artifacts"));
         if (maskedTimeline == null || !var1.equals(maskedTimeline)) {
            realTimeline = var1;
         }

         if (maskedArtifacts == null || !var2.equals(maskedArtifacts)) {
            realArtifacts = var2;
         }
      } catch (Throwable var3) {
      }
   }

   private static void restoreReal() {
      try {
         Class var0 = Class.forName("fr.reivaxmc.progress.network.ClientCampaignState");
         if (realTimeline != null) {
            setStatic(var0, "timeline", realTimeline);
         }

         if (realArtifacts != null) {
            setStatic(var0, "artifacts", realArtifacts);
         }

         maskedTimeline = null;
         maskedArtifacts = null;
      } catch (Throwable var1) {
      }
   }

   private static String maskTimeline(String var0) {
      if (var0 != null && !var0.isBlank()) {
         StringBuilder var1 = new StringBuilder();
         int var2 = 0;

         for (String var6 : var0.split("\\n")) {
            if (var6 != null && !var6.isBlank()) {
               String[] var7 = var6.split("¦", -1);
               String var8 = var7.length > 0 && !var7[0].isBlank() ? var7[0] : "?";
               if (var1.length() > 0) {
                  var1.append('\n');
               }

               var1.append(var8)
                  .append('¦')
                  .append("QA")
                  .append('¦')
                  .append("ÉVÉNEMENT NARRATIF ")
                  .append(++var2)
                  .append('¦')
                  .append("Contenu masqué pendant le mode QA anti-spoil.");
            }
         }

         return var1.toString();
      } else {
         return "";
      }
   }

   private static String maskArtifacts(String var0) {
      if (var0 != null && !var0.isBlank()) {
         StringBuilder var1 = new StringBuilder();
         int var2 = 0;

         for (String var6 : var0.split("\\n")) {
            if (var6 != null && !var6.isBlank()) {
               String[] var7 = var6.split("¦", -1);
               String var8 = var7.length > 3 && !var7[3].isBlank() ? var7[3] : "?";
               if (var1.length() > 0) {
                  var1.append('\n');
               }

               var1.append("qa_")
                  .append(var2)
                  .append('¦')
                  .append("ARCHIVE MASQUÉE ")
                  .append(++var2)
                  .append('¦')
                  .append("QA")
                  .append('¦')
                  .append(var8)
                  .append('¦')
                  .append("—")
                  .append('¦')
                  .append("Contenu masqué pendant le mode QA anti-spoil.");
            }
         }

         return var1.toString();
      } else {
         return "";
      }
   }

   private static Object getStatic(Class<?> var0, String var1) throws Exception {
      Field var2 = var0.getField(var1);
      return var2.get(null);
   }

   private static void setStatic(Class<?> var0, String var1, Object var2) throws Exception {
      Field var3 = var0.getField(var1);
      var3.set(null, var2);
   }

   private static String string(Object var0) {
      return var0 == null ? "" : String.valueOf(var0);
   }
}
