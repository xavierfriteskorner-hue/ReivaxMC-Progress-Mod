package fr.reivaxmc.progress.story;

import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedData.Factory;

public final class CampaignStateData18 extends SavedData {
   public static final String DATA_NAME = "reivaxmc_campaign18";
   public static final int SCHEMA_VERSION = 2;
   public static final String CHAPTER_BOOTSTRAP = "TECH_BOOTSTRAP";
   public static final String CHAPTER_PERSISTENCE = "TECH_PERSISTENCE";
   public static final String CHAPTER_VALIDATED = "TECH_VALIDATED";
   public static final String CHAPTER_AGE1_RESONANCE = "AGE1_RESONANCE";
   public static final String CHAPTER_AGE1_TRACE = "AGE1_FIRST_TRACE";
   public static final String CHAPTER_AGE1_FOYER = "AGE1_FIRST_HOME";
   public static final String CHAPTER_AGE1_RESPONSE = "AGE1_RESPONSE";
   public static final String OBJECTIVE_OBSERVE = "OBSERVE_REAL_ACTIONS";
   public static final String OBJECTIVE_RECONNECT = "RECONNECT_AND_RELOAD";
   public static final String OBJECTIVE_DONE = "ALPHA18C_VALIDATED";
   public static final String OBJECTIVE_FOLLOW_RESONANCE = "FOLLOW_RESONANCE";
   public static final String OBJECTIVE_DISCOVER_TRACE = "DISCOVER_FIRST_TRACE";
   public static final String OBJECTIVE_ESTABLISH_FOYER = "ESTABLISH_FIRST_HOME";
   public static final String OBJECTIVE_WAIT_RESPONSE = "WAIT_FOR_RESPONSE";
   public static final String OBJECTIVE_AGE1_DONE = "AGE1_FIRST_PAGE_COMPLETE";
   private int schemaVersion = 2;
   private int age = 1;
   private String chapter = "TECH_BOOTSTRAP";
   private String mainObjective = "OBSERVE_REAL_ACTIONS";
   private boolean gateCraft;
   private boolean gateBreak;
   private boolean gatePlace;
   private boolean persistenceArmed;
   private boolean persistenceValidated;
   private boolean age1Resonance;
   private boolean age1Stela;
   private boolean age1Fragment;
   private boolean age1Matrix;
   private boolean age1FirstPage;
   private boolean age1Foyer;
   private boolean age1Response;
   private String lastActorId = "";
   private long updatedAt;
   private int revision;
   private transient boolean loadedFromDisk;

   public static CampaignStateData18 create() {
      return new CampaignStateData18();
   }

   public static CampaignStateData18 get(MinecraftServer var0) {
      return (CampaignStateData18)var0.overworld()
         .getDataStorage()
         .computeIfAbsent(new Factory(CampaignStateData18::create, CampaignStateData18::load, null), "reivaxmc_campaign18");
   }

   public static CampaignStateData18 getForServer(Object var0) {
      if (var0 instanceof MinecraftServer var1) {
         return get(var1);
      } else {
         throw new IllegalArgumentException("Expected MinecraftServer, got " + (var0 == null ? "null" : var0.getClass().getName()));
      }
   }

   public static CampaignStateData18 load(CompoundTag var0, Provider var1) {
      CampaignStateData18 var2 = new CampaignStateData18();
      var2.schemaVersion = Math.max(1, var0.getInt("SchemaVersion"));
      var2.age = Math.max(1, var0.getInt("Age"));
      String var3 = var0.getString("Chapter");
      if (!var3.isBlank()) {
         var2.chapter = var3;
      }

      String var4 = var0.getString("MainObjective");
      if (!var4.isBlank()) {
         var2.mainObjective = var4;
      }

      var2.gateCraft = var0.getBoolean("GateCraft");
      var2.gateBreak = var0.getBoolean("GateBreak");
      var2.gatePlace = var0.getBoolean("GatePlace");
      var2.persistenceArmed = var0.getBoolean("PersistenceArmed");
      var2.persistenceValidated = var0.getBoolean("PersistenceValidated");
      var2.age1Resonance = var0.getBoolean("Age1Resonance");
      var2.age1Stela = var0.getBoolean("Age1Stela");
      var2.age1Fragment = var0.getBoolean("Age1Fragment");
      var2.age1Matrix = var0.getBoolean("Age1Matrix");
      var2.age1FirstPage = var0.getBoolean("Age1FirstPage");
      var2.age1Foyer = var0.getBoolean("Age1Foyer");
      var2.age1Response = var0.getBoolean("Age1Response");
      var2.lastActorId = var0.getString("LastActorId");
      var2.updatedAt = var0.getLong("UpdatedAt");
      var2.revision = var0.getInt("Revision");
      var2.loadedFromDisk = true;
      return var2;
   }

   public synchronized CompoundTag save(CompoundTag var1, Provider var2) {
      var1.putInt("SchemaVersion", 2);
      var1.putInt("Age", this.age);
      var1.putString("Chapter", this.chapter);
      var1.putString("MainObjective", this.mainObjective);
      var1.putBoolean("GateCraft", this.gateCraft);
      var1.putBoolean("GateBreak", this.gateBreak);
      var1.putBoolean("GatePlace", this.gatePlace);
      var1.putBoolean("PersistenceArmed", this.persistenceArmed);
      var1.putBoolean("PersistenceValidated", this.persistenceValidated);
      var1.putBoolean("Age1Resonance", this.age1Resonance);
      var1.putBoolean("Age1Stela", this.age1Stela);
      var1.putBoolean("Age1Fragment", this.age1Fragment);
      var1.putBoolean("Age1Matrix", this.age1Matrix);
      var1.putBoolean("Age1FirstPage", this.age1FirstPage);
      var1.putBoolean("Age1Foyer", this.age1Foyer);
      var1.putBoolean("Age1Response", this.age1Response);
      var1.putString("LastActorId", this.lastActorId == null ? "" : this.lastActorId);
      var1.putLong("UpdatedAt", this.updatedAt);
      var1.putInt("Revision", this.revision);
      return var1;
   }

   public synchronized boolean acceptFact(StoryFact var1) {
      if (var1 == null) {
         return false;
      } else if (Age1LegacyFacts.isAge1(var1.factType())) {
         return this.acceptAge1Fact(var1);
      } else if (this.persistenceValidated) {
         return false;
      } else {
         boolean var2 = false;
         String var3 = var1.factType();
         switch (var3) {
            case "ITEM_CRAFTED":
               if (!this.gateCraft) {
                  this.gateCraft = true;
                  var2 = true;
               }
               break;
            case "BLOCK_BROKEN":
               if (!this.gateBreak) {
                  this.gateBreak = true;
                  var2 = true;
               }
               break;
            case "BLOCK_PLACED":
               if (!this.gatePlace) {
                  this.gatePlace = true;
                  var2 = true;
               }
               break;
            default:
               return false;
         }

         if (!var2) {
            return false;
         } else {
            this.touch(var1.actorId());
            if (this.gateCount() == 3) {
               this.persistenceArmed = true;
               this.chapter = "TECH_PERSISTENCE";
               this.mainObjective = "RECONNECT_AND_RELOAD";
            }

            this.setDirty();
            return true;
         }
      }
   }

   public synchronized boolean acceptAge1Fact(StoryFact var1) {
      if (var1 != null && Age1LegacyFacts.isAge1(var1.factType())) {
         String var3 = var1.factType();

         boolean var2 = switch (var3) {
            case "AGE1_RESONANCE_MANIFESTED" -> this.setIfFalse(0);
            case "AGE1_STELA_DISCOVERED" -> this.setIfFalse(1);
            case "AGE1_FRAGMENT_FOUND" -> this.setIfFalse(2);
            case "AGE1_MATRIX_RECOGNIZED" -> this.setIfFalse(3);
            case "AGE1_FIRST_PAGE_RECORDED" -> this.setIfFalse(4);
            case "AGE1_FOYER_FOUNDED" -> this.setIfFalse(5);
            case "AGE1_DISTANT_RESPONSE" -> this.setIfFalse(6);
            default -> false;
         };
         if (!var2) {
            return false;
         } else {
            this.touch(var1.actorId());
            this.recalcAge1Route();
            this.setDirty();
            return true;
         }
      } else {
         return false;
      }
   }

   private boolean setIfFalse(int var1) {
      return switch (var1) {
         case 0 -> {
            if (this.age1Resonance) {
               yield false;
            } else {
               this.age1Resonance = true;
               yield true;
            }
         }
         case 1 -> {
            if (this.age1Stela) {
               yield false;
            } else {
               this.age1Stela = true;
               yield true;
            }
         }
         case 2 -> {
            if (this.age1Fragment) {
               yield false;
            } else {
               this.age1Fragment = true;
               yield true;
            }
         }
         case 3 -> {
            if (this.age1Matrix) {
               yield false;
            } else {
               this.age1Matrix = true;
               yield true;
            }
         }
         case 4 -> {
            if (this.age1FirstPage) {
               yield false;
            } else {
               this.age1FirstPage = true;
               yield true;
            }
         }
         case 5 -> {
            if (this.age1Foyer) {
               yield false;
            } else {
               this.age1Foyer = true;
               yield true;
            }
         }
         case 6 -> {
            if (this.age1Response) {
               yield false;
            } else {
               this.age1Response = true;
               yield true;
            }
         }
         default -> false;
      };
   }

   private void recalcAge1Route() {
      this.age = 1;
      if (this.age1Response) {
         this.chapter = "AGE1_RESPONSE";
         this.mainObjective = "AGE1_FIRST_PAGE_COMPLETE";
      } else if (this.age1Foyer) {
         this.chapter = "AGE1_FIRST_HOME";
         this.mainObjective = "WAIT_FOR_RESPONSE";
      } else if (this.age1FirstPage || this.age1Matrix || this.age1Fragment || this.age1Stela) {
         this.chapter = "AGE1_FIRST_TRACE";
         this.mainObjective = this.age1FirstPage ? "ESTABLISH_FIRST_HOME" : "DISCOVER_FIRST_TRACE";
      } else if (this.age1Resonance) {
         this.chapter = "AGE1_RESONANCE";
         this.mainObjective = "FOLLOW_RESONANCE";
      }
   }

   private void touch(String var1) {
      this.lastActorId = var1 == null ? "" : var1;
      this.updatedAt = System.currentTimeMillis();
      this.revision++;
   }

   public synchronized boolean validateReloadIfEligible() {
      if (this.loadedFromDisk && this.persistenceArmed && !this.persistenceValidated && this.gateCount() == 3) {
         this.persistenceValidated = true;
         if (this.age1GateCount() == 0) {
            this.chapter = "TECH_VALIDATED";
            this.mainObjective = "ALPHA18C_VALIDATED";
         }

         this.updatedAt = System.currentTimeMillis();
         this.revision++;
         this.setDirty();
         return true;
      } else {
         return false;
      }
   }

   public synchronized int gateCount() {
      int var1 = 0;
      if (this.gateCraft) {
         var1++;
      }

      if (this.gateBreak) {
         var1++;
      }

      if (this.gatePlace) {
         var1++;
      }

      return var1;
   }

   public synchronized int age1GateCount() {
      int var1 = 0;
      if (this.age1Resonance) {
         var1++;
      }

      if (this.age1Stela) {
         var1++;
      }

      if (this.age1Fragment) {
         var1++;
      }

      if (this.age1Matrix) {
         var1++;
      }

      if (this.age1FirstPage) {
         var1++;
      }

      if (this.age1Foyer) {
         var1++;
      }

      if (this.age1Response) {
         var1++;
      }

      return var1;
   }

   public synchronized CampaignStateData18.Snapshot snapshot() {
      return new CampaignStateData18.Snapshot(
         this.age,
         this.chapter,
         this.mainObjective,
         this.gateCount(),
         this.age1GateCount(),
         this.persistenceArmed,
         this.persistenceValidated,
         this.loadedFromDisk,
         this.revision,
         this.lastActorId
      );
   }

   public static record Snapshot(
      int age,
      String chapter,
      String mainObjective,
      int technicalGates,
      int age1Gates,
      boolean persistenceArmed,
      boolean persistenceValidated,
      boolean loadedFromDisk,
      int revision,
      String lastActorId
   ) {
   }
}
