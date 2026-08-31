package fr.reivaxmc.progress.story;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class F8InteractionBridge {
   private F8InteractionBridge() {
   }

   public static void onPlaced(Object var0) {
      if (var0 != null) {
         try {
            Object var1 = F8SanctuaryEngine.invokeNoArg(var0, "getEntity");
            if (var1 == null) {
               return;
            }

            Object var2 = server(var1);
            if (var2 == null) {
               return;
            }

            Object var3 = F8SanctuaryEngine.invokeNoArg(var0, "getPos");
            if (F8SanctuaryEngine.isSanctuaryProtectedPos(var2, var3)) {
               cancel(var0);
               message(var1, "§6SANCTUAIRE §8• §fLa matière du Sanctuaire refuse toute modification.");
               return;
            }

            Object var4 = F8SanctuaryEngine.invokeNoArg(var0, "getPlacedBlock");
            Object var5 = F8SanctuaryEngine.invokeNoArg(var4, "getBlock");
            if (var5 != F8SanctuaryEngine.foundationBeaconBlock()) {
               return;
            }

            cancel(var0);
            Object var6 = F8SanctuaryEngine.campaign(var2);
            if (!F8SanctuaryEngine.completed(var6, "F8_FOUNDATION_BEACON_RECOVERED")) {
               message(var1, "§cCette Borne n'a pas encore été obtenue dans l'histoire.");
               return;
            }

            if (F8SanctuaryEngine.boolInvoke(var6, "foundationPlaced")) {
               message(var1, "§6FOYER §8• §fUn Foyer principal est déjà actif.");
               return;
            }

            if (F8SanctuaryEngine.tooCloseToSanctuary(var2, var3)) {
               message(var1, "§6BORNE §8• §fChoisissez un lieu plus éloigné du Sanctuaire pour établir votre propre Foyer.");
               F7NarrativeEngine.routeStoryMessage(
                  var1, "§6OBJECTIF PRINCIPAL §8• §fÉloignez-vous du Sanctuaire puis placez la Borne à l'endroit où vous voulez vivre.", true
               );
               return;
            }

            F8SanctuaryEngine.callStatic("fr.reivaxmc.progress.network.ProgressNetworking", "openFoundationPlacement", var1, var3);
         } catch (Throwable var7) {
            System.err.println("[REIVAX Alpha 18F.8.4] placement bridge failed: " + var7.getClass().getSimpleName() + ": " + var7.getMessage());
         }
      }
   }

   public static void confirmFoundation(Object var0, Object var1) {
      if (var0 != null && var1 != null) {
         try {
            Object var2 = server(var0);
            if (var2 == null || F8SanctuaryEngine.distance2(var0, var1) > 81.0) {
               return;
            }

            Object var3 = F8SanctuaryEngine.campaign(var2);
            if (!F8SanctuaryEngine.completed(var3, "F8_FOUNDATION_BEACON_RECOVERED")) {
               return;
            }

            if (F8SanctuaryEngine.boolInvoke(var3, "foundationPlaced")) {
               message(var0, "§6FOYER §8• §fUn Foyer principal est déjà actif.");
               return;
            }

            if (F8SanctuaryEngine.tooCloseToSanctuary(var2, var1)) {
               message(var0, "§6BORNE §8• §fCet emplacement est encore trop proche du Sanctuaire.");
               return;
            }

            if (!consumeBeacon(var0)) {
               message(var0, "§cVous ne possédez plus la Borne de Fondation.");
               return;
            }

            Object var4 = F8SanctuaryEngine.invokeNoArg(var0, "serverLevel");
            F8SanctuaryEngine.setBlock(
               var4, F8SanctuaryEngine.posX(var1), F8SanctuaryEngine.posY(var1), F8SanctuaryEngine.posZ(var1), F8SanctuaryEngine.foundationBeaconBlock()
            );
            Object var5 = F8SanctuaryEngine.invokeNoArg(var4, "dimension");
            Object var6 = F8SanctuaryEngine.invokeNoArg(var5, "location");
            String var7 = String.valueOf(var6);
            String var8 = F8SanctuaryEngine.playerName(var0);
            Object var9 = F8SanctuaryEngine.invokeNoArg(var0, "getUUID");
            UUID var10 = var9 instanceof UUID var11 ? var11 : UUID.fromString(String.valueOf(var9));
            int var15 = F8SanctuaryEngine.worldDay(var2);
            long var12 = F8SanctuaryEngine.number(F8SanctuaryEngine.invokeNoArg(var4, "getGameTime")).longValue();
            F8SanctuaryEngine.invoke(var3, "foundSettlement", var1, var7, var8, var10, var15, var12);
            F8SanctuaryEngine.onFoundationEstablished(var2, var0);
            syncAll(var2, var3);
         } catch (Throwable var14) {
            System.err.println("[REIVAX Alpha 18F.8.4] confirmFoundation failed: " + var14.getClass().getSimpleName() + ": " + var14.getMessage());
         }
      }
   }

   public static void onRightClickBlock(Object var0) {
      if (!F90SealGate.tryHandle(var0)) {
         if (var0 != null) {
            try {
               Object var1 = F8SanctuaryEngine.invokeNoArg(var0, "getLevel");
               if (F8SanctuaryEngine.invokeNoArg(var1, "isClientSide") instanceof Boolean var3 && var3) {
                  return;
               }

               Object var17 = F8SanctuaryEngine.invokeNoArg(var0, "getEntity");
               Object var4 = server(var17);
               if (var4 == null) {
                  return;
               }

               Object var5 = F8SanctuaryEngine.invokeNoArg(var0, "getPos");
               Object var6 = F8SanctuaryEngine.invoke(var1, "getBlockState", var5);
               Object var7 = F8SanctuaryEngine.invokeNoArg(var6, "getBlock");
               Object var8 = F8SanctuaryEngine.campaign(var4);
               if (F8SanctuaryEngine.isSealStelePos(var4, var5)) {
                  cancel(var0);
                  if (!F8SanctuaryEngine.completed(var8, "F8_SANCTUARY_DISCOVERED")) {
                     message(var17, "§6STÈLE §8• §fLa matière reste inerte.");
                     return;
                  }

                  if (F8SanctuaryEngine.completed(var8, "F84_SEAL_INSERTED")) {
                     message(var17, "§6STÈLE §8• §fLe Sceau est déjà enchâssé dans la Stèle.");
                     return;
                  }

                  List var20 = F8SanctuaryEngine.players(var4);
                  int var22 = F8SanctuaryEngine.countNear(var20, F8SanctuaryEngine.posX(var5), F8SanctuaryEngine.posY(var5), F8SanctuaryEngine.posZ(var5), 30.0);
                  if (var22 < var20.size()) {
                     message(var17, "§6STÈLE §8• §fRegroupez-vous devant le seuil avant d'insérer le Sceau · " + var22 + "/" + var20.size() + ".");
                     return;
                  }

                  Object var23 = null;

                  try {
                     var23 = F8SanctuaryEngine.invokeNoArg(var0, "getItemStack");
                  } catch (Throwable var15) {
                  }

                  Object var24 = var23 == null ? null : F8SanctuaryEngine.invokeNoArg(var23, "getItem");
                  if (var24 != F8SanctuaryEngine.originSealItem()) {
                     message(var17, "§6STÈLE §8• §fTenez le Sceau des Origines en main puis faites CLIC DROIT.");
                     return;
                  }

                  F8SanctuaryEngine.invoke(var23, "shrink", 1);
                  F8SanctuaryEngine.onSealInserted(var4, var17);
                  syncAll(var4, var8);
                  return;
               }

               if (F8SanctuaryEngine.isBookPedestalPos(var4, var5)) {
                  cancel(var0);
                  if (!F8SanctuaryEngine.completed(var8, "F82_FOUNDATION_GUARDS_CLEARED")) {
                     message(var17, "§6LIVRE §8• §fLes Protecteurs vous empêchent encore d'atteindre le pupitre.");
                     return;
                  }

                  if (F8SanctuaryEngine.completed(var8, "F84_BOOK_RECOVERED")) {
                     message(var17, "§6LIVRE §8• §fLe pupitre est désormais vide.");
                     return;
                  }

                  giveItem(var17, F8SanctuaryEngine.destinyBookItem(), 1);
                  F8SanctuaryEngine.onBookRecovered(var4, var17);
                  F7NarrativeEngine.routeStoryMessage(
                     var17, "§6OBJECTIF PRINCIPAL §8• §fOuvrez le Reliquaire du Sanctuaire · il se trouve à gauche de la chambre.", true
                  );
                  syncAll(var4, var8);
                  return;
               }

               if (F8SanctuaryEngine.isReliquaryPos(var4, var5)) {
                  cancel(var0);
                  if (!F8SanctuaryEngine.completed(var8, "F82_FOUNDATION_GUARDS_CLEARED")) {
                     message(var17, "§6RELIQUAIRE §8• §fLe mécanisme est verrouillé tant que les Protecteurs sont actifs.");
                     return;
                  }

                  if (!F8SanctuaryEngine.completed(var8, "F84_BOOK_RECOVERED")) {
                     message(var17, "§6RELIQUAIRE §8• §fQuelque chose manque encore dans la chambre. Examinez le Livre ancien.");
                     return;
                  }

                  if (F8SanctuaryEngine.completed(var8, "F84_RELIQUARY_CLAIMED")) {
                     message(var17, "§6RELIQUAIRE §8• §fLe Reliquaire est vide.");
                     return;
                  }

                  for (Object var21 : F8SanctuaryEngine.players(var4)) {
                     giveVanilla(var21, "BREAD", 4);
                     giveVanilla(var21, "COAL", 4);
                     giveVanilla(var21, "IRON_INGOT", 2);
                  }

                  giveVanilla(var17, "GOLDEN_APPLE", 1);
                  F8SanctuaryEngine.onReliquaryClaimed(var4, var17);
                  F7NarrativeEngine.routeStoryMessage(
                     var17, "§6OBJECTIF PRINCIPAL §8• §fRécupérez la Borne de Fondation · faites CLIC DROIT sur la Borne au sommet de l'autel.", true
                  );
                  syncAll(var4, var8);
                  return;
               }

               if (var7 != F8SanctuaryEngine.foundationBeaconBlock()) {
                  return;
               }

               if (F8SanctuaryEngine.boolInvoke(var8, "foundationPlaced") && !F8SanctuaryEngine.isSanctuaryBeaconPos(var4, var5)) {
                  cancel(var0);
                  Object var18 = F8SanctuaryEngine.invokeNoArg(var8, "foundationPos");
                  String var10 = String.valueOf(F8SanctuaryEngine.invokeNoArg(var8, "foundationName"));
                  int var11 = F8SanctuaryEngine.number(F8SanctuaryEngine.invokeNoArg(var8, "territoryRadius")).intValue();
                  String var12 = String.valueOf(F8SanctuaryEngine.fieldValue(var8, "foundationFounder"));
                  int var13 = F8SanctuaryEngine.number(F8SanctuaryEngine.fieldValue(var8, "foundationDay")).intValue();
                  String var14 = F8SanctuaryEngine.posX(var18) + ", " + F8SanctuaryEngine.posY(var18) + ", " + F8SanctuaryEngine.posZ(var18);
                  F7NarrativeEngine.pushUi(var17, "F8_FOYER_PANEL", var10 + "|" + var11 + "|" + var12 + "|" + var13 + "|" + var14);
                  return;
               }

               if (!F8SanctuaryEngine.isSanctuaryBeaconPos(var4, var5)) {
                  return;
               }

               cancel(var0);
               int var9 = (F8SanctuaryEngine.completed(var8, "F82_FOUNDATION_GUARD_1_DEFEATED") ? 1 : 0)
                  + (F8SanctuaryEngine.completed(var8, "F82_FOUNDATION_GUARD_2_DEFEATED") ? 1 : 0);
               if (var9 < 2) {
                  message(var17, "§6BORNE §8• §fLa Borne reste protégée tant que les Gardiens de Fondation sont actifs.");
                  F7NarrativeEngine.routeStoryMessage(var17, "§6OBJECTIF PRINCIPAL §8• §fNeutralisez les Gardiens de Fondation · " + var9 + "/2.", true);
                  return;
               }

               if (F8SanctuaryEngine.completed(var8, "F8_FOUNDATION_BEACON_RECOVERED")) {
                  return;
               }

               if (!F8SanctuaryEngine.completed(var8, "F84_BOOK_RECOVERED")) {
                  message(var17, "§6BORNE §8• §fAvant de partir, examinez le Livre ancien conservé dans la chambre.");
                  return;
               }

               if (!F8SanctuaryEngine.completed(var8, "F84_RELIQUARY_CLAIMED")) {
                  message(var17, "§6BORNE §8• §fLe Reliquaire vient de se déverrouiller. Examinez-le avant de retirer la Borne.");
                  return;
               }

               F8SanctuaryEngine.setBlock(var1, F8SanctuaryEngine.posX(var5), F8SanctuaryEngine.posY(var5), F8SanctuaryEngine.posZ(var5), "AIR");
               giveBeacon(var17);
               F8SanctuaryEngine.onBeaconRecovered(var4, var17);
               syncAll(var4, var8);
            } catch (Throwable var16) {
               System.err.println("[REIVAX Alpha 18F.8.4] right-click bridge failed: " + var16.getClass().getSimpleName() + ": " + var16.getMessage());
            }
         }
      }
   }

   public static void onBreak(Object var0) {
      if (var0 != null) {
         try {
            Object var1 = F8SanctuaryEngine.invokeNoArg(var0, "getPlayer");
            Object var2 = server(var1);
            if (var2 == null) {
               return;
            }

            Object var3 = F8SanctuaryEngine.invokeNoArg(var0, "getPos");
            if (F8SanctuaryEngine.isSanctuaryProtectedPos(var2, var3)) {
               cancel(var0);
               message(var1, "§6SANCTUAIRE §8• §fCette structure ne peut pas être altérée.");
               return;
            }

            Object var4 = F8SanctuaryEngine.invokeNoArg(var0, "getState");
            Object var5 = F8SanctuaryEngine.invokeNoArg(var4, "getBlock");
            if (var5 != F8SanctuaryEngine.foundationBeaconBlock()) {
               return;
            }

            Object var6 = F8SanctuaryEngine.campaign(var2);
            boolean var7 = false;
            if (!F8SanctuaryEngine.completed(var6, "F8_FOUNDATION_BEACON_RECOVERED") && F8SanctuaryEngine.isSanctuaryBeaconPos(var2, var3)) {
               var7 = true;
            }

            if (F8SanctuaryEngine.boolInvoke(var6, "foundationPlaced")) {
               Object var8 = F8SanctuaryEngine.invokeNoArg(var6, "foundationPos");
               if (samePos(var8, var3)) {
                  var7 = true;
               }
            }

            if (var7) {
               cancel(var0);
               message(var1, "§6BORNE §8• §fLa Borne est un élément narratif protégé · utilisez CLIC DROIT.");
            }
         } catch (Throwable var9) {
            System.err.println("[REIVAX Alpha 18F.8.4] break protection failed: " + var9.getClass().getSimpleName());
         }
      }
   }

   public static void onLivingDeath(Object var0) {
      if (var0 != null) {
         try {
            Object var1 = F8SanctuaryEngine.invokeNoArg(var0, "getEntity");
            if (F8SanctuaryEngine.invokeNoArg(var1, "getTags") instanceof Set var3 && var3.contains("reivax_f8_guardian")) {
               Object var4 = server(var1);
               if (var4 == null) {
                  return;
               }

               String var5;
               if (var3.contains("reivax_f83_fg2") || var3.contains("reivax_f82_fg2")) {
                  var5 = "fg2";
               } else if (var3.contains("reivax_f83_fg1") || var3.contains("reivax_f82_fg1")) {
                  var5 = "fg1";
               } else if (!var3.contains("reivax_f83_w2") && !var3.contains("reivax_f82_w2") && !var3.contains("reivax_f8_g2")) {
                  var5 = "g1";
               } else {
                  var5 = "g2";
               }

               Object var6 = null;
               Object var7 = null;

               try {
                  var6 = F8SanctuaryEngine.invokeNoArg(var0, "getSource");
                  var7 = F8SanctuaryEngine.invokeNoArg(var6, "getEntity");
               } catch (Throwable var9) {
               }

               F8SanctuaryEngine.onGuardianDefeated(var4, var5, var7);
               return;
            }
         } catch (Throwable var10) {
            System.err.println("[REIVAX Alpha 18F.8.4] death bridge failed: " + var10.getClass().getSimpleName());
         }
      }
   }

   private static boolean consumeBeacon(Object var0) throws Exception {
      Object var1 = F8SanctuaryEngine.invokeNoArg(var0, "getInventory");
      int var2 = F8SanctuaryEngine.number(F8SanctuaryEngine.invokeNoArg(var1, "getContainerSize")).intValue();
      Object var3 = F8SanctuaryEngine.foundationBeaconItem();

      for (int var4 = 0; var4 < var2; var4++) {
         Object var5 = F8SanctuaryEngine.invoke(var1, "getItem", var4);
         Object var6 = F8SanctuaryEngine.invokeNoArg(var5, "getItem");
         if (var6 == var3) {
            F8SanctuaryEngine.invoke(var5, "shrink", 1);
            return true;
         }
      }

      return false;
   }

   private static void giveBeacon(Object var0) throws Exception {
      Object var1 = F8SanctuaryEngine.foundationBeaconItem();
      Object var2 = F8SanctuaryEngine.invokeNoArg(var1, "getDefaultInstance");
      Object var3 = F8SanctuaryEngine.invokeNoArg(var0, "getInventory");
      if (F8SanctuaryEngine.invoke(var3, "add", var2) instanceof Boolean var5 && !var5) {
         try {
            F8SanctuaryEngine.invoke(var0, "drop", var2, false);
         } catch (Throwable var7) {
         }
      }
   }

   private static void giveItem(Object var0, Object var1, int var2) throws Exception {
      Object var3 = F8SanctuaryEngine.invokeNoArg(var1, "getDefaultInstance");
      if (var2 > 1) {
         F8SanctuaryEngine.invoke(var3, "setCount", var2);
      }

      Object var4 = F8SanctuaryEngine.invokeNoArg(var0, "getInventory");
      if (F8SanctuaryEngine.invoke(var4, "add", var3) instanceof Boolean var6 && !var6) {
         try {
            F8SanctuaryEngine.invoke(var0, "drop", var3, false);
         } catch (Throwable var8) {
         }
      }
   }

   private static void giveVanilla(Object var0, String var1, int var2) throws Exception {
      Object var3 = F8SanctuaryEngine.staticField("net.minecraft.world.item.Items", var1);
      giveItem(var0, var3, var2);
   }

   private static Object server(Object var0) {
      try {
         return F8SanctuaryEngine.invokeNoArg(var0, "getServer");
      } catch (Throwable var4) {
         try {
            Object var2 = F8SanctuaryEngine.invokeNoArg(var0, "level");
            return F8SanctuaryEngine.invokeNoArg(var2, "getServer");
         } catch (Throwable var3) {
            return null;
         }
      }
   }

   private static void syncAll(Object var0, Object var1) {
      try {
         for (Object var4 : F8SanctuaryEngine.players(var0)) {
            try {
               F8SanctuaryEngine.callStatic("fr.reivaxmc.progress.network.ProgressNetworking", "sync", var4, var1);
            } catch (Throwable var6) {
            }
         }
      } catch (Throwable var7) {
      }
   }

   private static void message(Object var0, String var1) {
      F7NarrativeEngine.routeStoryMessage(var0, var1, false);
   }

   private static boolean samePos(Object var0, Object var1) throws Exception {
      return F8SanctuaryEngine.posX(var0) == F8SanctuaryEngine.posX(var1)
         && F8SanctuaryEngine.posY(var0) == F8SanctuaryEngine.posY(var1)
         && F8SanctuaryEngine.posZ(var0) == F8SanctuaryEngine.posZ(var1);
   }

   private static void cancel(Object var0) {
      try {
         F8SanctuaryEngine.invoke(var0, "setCanceled", true);
      } catch (Throwable var2) {
      }
   }
}
