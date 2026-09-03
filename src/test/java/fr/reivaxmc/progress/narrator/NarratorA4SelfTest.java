package fr.reivaxmc.progress.narrator;

import java.util.HashSet;
import java.util.List;

/** Auto-test autonome du lot A4, sans lancer Minecraft. */
public final class NarratorA4SelfTest {
   private NarratorA4SelfTest() {
   }

   public static void main(String[] args) {
      idsAreStableAndUnique();
      combatMappingsAreExact();
      damageAdapterSupportsNeoForge211();
      hazardsArePrecise();
      drowningRequiresRecovery();
      catalogContainsSoloDuoAndCondensationData();
      System.out.println("REIVAX A4 self-test: OK (12 dangers/combat SOLO + DUO)");
   }

   private static void idsAreStableAndUnique() {
      require(NarratorA4SignalDetector.ALL_IDS.size() == 12, "A4 doit suivre exactement 12 événements.");
      require(new HashSet<>(NarratorA4SignalDetector.ALL_IDS).size() == 12, "Les IDs A4 doivent être uniques.");
      require(
         NarratorA4SignalDetector.FIRST_CREEPER_KILL.equals("A1-078")
            && NarratorA4SignalDetector.FIRST_DEATH.equals("A1-084"),
         "Les deux pilotes 0.8.0 doivent conserver leur mémoire historique."
      );
   }

   private static void combatMappingsAreExact() {
      expectKill("minecraft:creeper", NarratorA4SignalDetector.FIRST_CREEPER_KILL);
      expectKill("minecraft:zombie", NarratorA4SignalDetector.FIRST_ZOMBIE_KILL);
      expectKill("minecraft:skeleton", NarratorA4SignalDetector.FIRST_SKELETON_KILL);
      expectKill("minecraft:spider", NarratorA4SignalDetector.FIRST_SPIDER_KILL);
      expectKill("minecraft:enderman", NarratorA4SignalDetector.FIRST_ENDERMAN_KILL);
      require(NarratorA4SignalDetector.killedByPlayer("minecraft:husk") == null, "Un Husk ne doit pas compter comme Zombie vanilla.");
      require(NarratorA4SignalDetector.killedByPlayer("modded:zombie") == null, "Un homonyme moddé ne doit pas créer de faux positif.");
   }

   private static void damageAdapterSupportsNeoForge211() {
      require(
         NarratorLegacy.a4DamageAmountForTest(new NeoForge211DamagePost(3.5F)) == 3.5F,
         "NeoForge 21.1.248 fournit la perte de vie via getNewDamage()."
      );
      require(
         NarratorLegacy.a4DamageAmountForTest(new FutureDamagePost(4.5F)) == 4.5F,
         "Le nom getHealthDamage() doit rester accepté pour les versions NeoForge plus récentes."
      );
   }

   private static void hazardsArePrecise() {
      List<NarratorA4SignalDetector.Signal> fall = damage("fall", "", 10.0F, 6.0F);
      require(has(fall, NarratorA4SignalDetector.FIRST_MAJOR_FALL), "Une chute avec dégâts doit être détectée.");
      require(!has(fall, NarratorA4SignalDetector.FIRST_NEAR_FATAL_FALL), "Une chute à cinq cœurs n'est pas presque mortelle.");
      require(damage("fall", "", 18.0F, 2.0F).isEmpty(), "Une petite chute ne doit pas compter comme chute importante.");

      List<NarratorA4SignalDetector.Signal> nearFatal = damage("fall", "", 4.0F, 16.0F);
      require(has(nearFatal, NarratorA4SignalDetector.FIRST_MAJOR_FALL), "La chute presque mortelle inclut la première chute.");
      require(has(nearFatal, NarratorA4SignalDetector.FIRST_NEAR_FATAL_FALL), "Deux cœurs après la chute doivent déclencher l'alerte forte.");

      require(has(damage("lava", "", 12.0F, 4.0F), NarratorA4SignalDetector.FIRST_LAVA_DAMAGE), "La lave doit être détectée.");
      require(!has(damage("lava", "", 12.0F, 4.0F), NarratorA4SignalDetector.FIRST_FIRE_DAMAGE), "La lave ne doit pas doubler l'événement feu.");
      require(has(damage("onFire", "", 18.0F, 2.0F), NarratorA4SignalDetector.FIRST_FIRE_DAMAGE), "Le feu doit être détecté.");
      require(
         has(damage("explosion", "minecraft:creeper", 8.0F, 12.0F), NarratorA4SignalDetector.FIRST_CREEPER_DAMAGE),
         "Seule une source Creeper exacte doit valider les dégâts de Creeper."
      );
      require(damage("explosion", "minecraft:tnt", 8.0F, 12.0F).isEmpty(), "La TNT ne doit pas compter comme Creeper.");
      require(damage("fall", "", 4.0F, 0.0F).isEmpty(), "Un dégât nul ne doit rien déclencher.");
   }

   private static void drowningRequiresRecovery() {
      require(NarratorA4SignalDetector.isDrowning("drown"), "La noyade doit armer le souvenir temporaire.");
      require(NarratorA4SignalDetector.drowningRecovered(true, 100, 300, 10.0F) == null, "L'air encore bas ne valide pas la survie.");
      require(NarratorA4SignalDetector.drowningRecovered(false, 300, 300, 10.0F) == null, "Sans noyade préalable, respirer ne valide rien.");
      require(
         NarratorA4SignalDetector.drowningRecovered(true, 200, 300, 10.0F).eventId().equals(NarratorA4SignalDetector.FIRST_DROWNING_SURVIVED),
         "La remontée à plus de la moitié d'air doit valider la noyade évitée."
      );
   }

   private static void catalogContainsSoloDuoAndCondensationData() {
      for (String id : NarratorA4SignalDetector.ALL_IDS) {
         require(NarratorLegacy.catalogHasEventForTest(id), "Événement absent du catalogue JSON : " + id);
         require(NarratorLegacy.catalogHasDuoTextForTest(id), "Texte partenaire absent : " + id);
      }
      require(
         "fall_danger".equals(NarratorLegacy.catalogCondenseGroupForTest(NarratorA4SignalDetector.FIRST_MAJOR_FALL))
            && "fall_danger".equals(NarratorLegacy.catalogCondenseGroupForTest(NarratorA4SignalDetector.FIRST_NEAR_FATAL_FALL)),
         "Les deux conséquences d'une même chute doivent partager un encadré."
      );
   }

   private static List<NarratorA4SignalDetector.Signal> damage(String cause, String sourceEntity, float health, float inflicted) {
      return NarratorA4SignalDetector.damagedPlayer(new NarratorA4SignalDetector.DamageObservation(cause, sourceEntity, health, inflicted));
   }

   private static void expectKill(String entityId, String eventId) {
      NarratorA4SignalDetector.Signal signal = NarratorA4SignalDetector.killedByPlayer(entityId);
      require(signal != null && eventId.equals(signal.eventId()), "Mauvais mapping de combat pour " + entityId);
   }

   private static boolean has(List<NarratorA4SignalDetector.Signal> signals, String eventId) {
      return signals.stream().anyMatch(signal -> eventId.equals(signal.eventId()));
   }

   private static void require(boolean condition, String message) {
      if (!condition) {
         throw new AssertionError(message);
      }
   }

   private record NeoForge211DamagePost(float amount) {
      public float getNewDamage() {
         return amount;
      }
   }

   private record FutureDamagePost(float amount) {
      public float getHealthDamage() {
         return amount;
      }
   }
}
