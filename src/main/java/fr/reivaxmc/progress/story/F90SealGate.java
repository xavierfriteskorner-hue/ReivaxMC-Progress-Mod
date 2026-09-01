package fr.reivaxmc.progress.story;

import java.lang.reflect.Method;
import java.util.List;

public final class F90SealGate {
   private F90SealGate() {
   }

   public static boolean tryHandle(Object var0) {
      if (var0 == null) {
         return false;
      } else {
         try {
            Object var1 = F8SanctuaryEngine.invokeNoArg(var0, "getEntity");
            if (var1 == null) {
               return false;
            } else {
               Object var2 = F8SanctuaryEngine.invokeNoArg(var1, "getServer");
               if (var2 == null) {
                  return false;
               } else {
                  Object var3 = F8SanctuaryEngine.invokeNoArg(var0, "getPos");
                  int var4 = F90Sanctuary.receptacleSide(var2, var3);
                  if (var4 == 0) {
                     return false;
                  } else {
                     cancel(var0);
                     Object var5 = F8SanctuaryEngine.campaign(var2);
                     if (!F8SanctuaryEngine.completed(var5, "F8_SANCTUARY_DISCOVERED")) {
                        msg(var1, "§6RÉCEPTACLE §8· §fLe dispositif reste inerte.");
                        return true;
                     } else if (!F8SanctuaryEngine.frontCleared(var5)) {
                        // Obligatoire : tuer les 2 Veilleurs de l'entrée avant de pouvoir insérer les Sceaux.
                        msg(var1, "§6RÉCEPTACLE §8· §fLes 2 Veilleurs de l'entrée gardent encore les stèles · neutralisez-les d'abord.");
                        return true;
                     } else {
                        String var6 = var4 < 0 ? "F90_LEFT_SEAL" : "F90_RIGHT_SEAL";
                        if (F8SanctuaryEngine.completed(var5, var6)) {
                           msg(var1, "§6RÉCEPTACLE §8· §fUn Sceau est déjà installé dans cette borne.");
                           return true;
                        } else {
                           Object var7 = null;

                           try {
                              var7 = F8SanctuaryEngine.invokeNoArg(var0, "getItemStack");
                           } catch (Throwable var12) {
                           }

                           Object var8 = var7 == null ? null : F8SanctuaryEngine.invokeNoArg(var7, "getItem");
                           if (var8 != F8SanctuaryEngine.originSealItem()) {
                              msg(
                                 var1,
                                 "§6RÉCEPTACLE §8· §fLa cavité reprend la forme du Sceau. Tenez un Sceau des Origines en main et faites CLIC DROIT sur la borne."
                              );
                              return true;
                           } else {
                              F8SanctuaryEngine.invoke(var7, "shrink", 1);
                              F8SanctuaryEngine.complete(var5, var6);
                              F90Sanctuary.activateVisual(var2, var4);
                              List var9 = F8SanctuaryEngine.players(var2);
                              boolean var10 = F8SanctuaryEngine.completed(var5, "F90_LEFT_SEAL");
                              boolean var11 = F8SanctuaryEngine.completed(var5, "F90_RIGHT_SEAL");
                              if (var10 && var11) {
                                 F8SanctuaryEngine.history(var9, "§8Les deux Sceaux sont désormais enchâssés. Le seuil du Sanctuaire répond.");
                                 F8SanctuaryEngine.onSealInserted(var2, var1);
                              } else {
                                 F8SanctuaryEngine.objective(var9, "Activez le second Réceptacle des Sceaux, de l'autre côté de l'entrée.");
                                 msg(var1, "§6SANCTUAIRE §8· §fLa première borne s'éveille. Une seconde attend de l'autre côté du seuil.");
                              }

                              return true;
                           }
                        }
                     }
                  }
               }
            }
         } catch (Throwable var13) {
            System.err.println("[REIVAX F9 seal gate] " + var13.getClass().getSimpleName() + ": " + var13.getMessage());
            return false;
         }
      }
   }

   public static void distributeSecondSeal(Object var0) {
      if (var0 != null) {
         try {
            Object var1 = F8SanctuaryEngine.invokeNoArg(var0, "getServer");
            List var2 = var1 == null ? List.of() : F8SanctuaryEngine.players(var1);
            Object var3 = var0;
            if (var2.size() > 1) {
               for (Object var5 : var2) {
                  if (var5 != null && var5 != var0) {
                     var3 = var5;
                     break;
                  }
               }
            }

            give(var3);
            if (var3 == var0) {
               msg(var0, "§6SCEAUX DES ORIGINES §8· §fLa Trace a libéré deux Sceaux jumeaux. Les deux seront nécessaires.");
            } else {
               msg(var0, "§6SCEAUX DES ORIGINES §8· §fDeux Sceaux jumeaux ont été libérés. Le second a rejoint votre partenaire.");
               msg(var3, "§6SCEAU DES ORIGINES §8· §fUn second Sceau vous a rejoint. Il répond au premier.");
            }
         } catch (Throwable var6) {
            System.err.println("[REIVAX F9 seals] " + var6.getMessage());
         }
      }
   }

   public static void giveDevSecondSeal(Object var0) {
      distributeSecondSeal(var0);
   }

   private static void give(Object var0) throws Exception {
      Class var1 = Class.forName("fr.reivaxmc.progress.story.F81DevTools");
      Method var2 = var1.getDeclaredMethod("giveItem", Object.class, String.class);
      var2.setAccessible(true);
      var2.invoke(null, var0, "ORIGIN_SEAL");
   }

   private static void msg(Object var0, String var1) {
      try {
         F7NarrativeEngine.routeStoryMessage(var0, var1, false);
      } catch (Throwable var3) {
      }
   }

   private static void cancel(Object var0) {
      try {
         F8SanctuaryEngine.invoke(var0, "setCanceled", Boolean.TRUE);
      } catch (Throwable var3) {
      }

      try {
         Object var1 = F8SanctuaryEngine.staticField("net.minecraft.world.InteractionResult", "SUCCESS");
         F8SanctuaryEngine.invoke(var0, "setCancellationResult", var1);
      } catch (Throwable var2) {
      }
   }
}
