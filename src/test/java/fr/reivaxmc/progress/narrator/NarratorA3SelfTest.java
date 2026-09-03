package fr.reivaxmc.progress.narrator;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Auto-test autonome du lot accéléré A3, sans lancer Minecraft. */
public final class NarratorA3SelfTest {
   private NarratorA3SelfTest() {
   }

   public static void main(String[] args) {
      idsAreUnique();
      inventoryMappingsAreExact();
      equipmentMappingsAreExact();
      burstPolicyIsConservative();
      pointLabelsAreExplicit();
      catalogContainsSoloDuoAndCondensationData();
      System.out.println("REIVAX A3 self-test: OK (16 événements + HUD + condensation)");
   }

   private static void idsAreUnique() {
      require(NarratorA3SignalDetector.ALL_IDS.size() == 16, "A3 doit contenir exactement 16 événements.");
      require(new HashSet<>(NarratorA3SignalDetector.ALL_IDS).size() == 16, "Les IDs A3 doivent être uniques.");
   }

   private static void inventoryMappingsAreExact() {
      expect("minecraft:bucket", NarratorA3SignalDetector.FIRST_BUCKET);
      expect("minecraft:water_bucket", NarratorA3SignalDetector.FIRST_WATER_BUCKET);
      expect("minecraft:lava_bucket", NarratorA3SignalDetector.FIRST_LAVA_BUCKET);
      expect("minecraft:bow", NarratorA3SignalDetector.FIRST_BOW);
      expect("minecraft:emerald", NarratorA3SignalDetector.FIRST_EMERALD);
      expect("minecraft:raw_copper", NarratorA3SignalDetector.FIRST_COPPER);
      expect("minecraft:copper_ingot", NarratorA3SignalDetector.FIRST_COPPER);
      expect("minecraft:lapis_lazuli", NarratorA3SignalDetector.FIRST_LAPIS);
      expect("minecraft:raw_gold", NarratorA3SignalDetector.FIRST_GOLD);
      expect("minecraft:gold_ingot", NarratorA3SignalDetector.FIRST_GOLD);
      expect("minecraft:redstone", NarratorA3SignalDetector.FIRST_REDSTONE);
      expect("minecraft:amethyst_shard", NarratorA3SignalDetector.FIRST_AMETHYST);
      expect("minecraft:obsidian", NarratorA3SignalDetector.FIRST_OBSIDIAN);
      expect("minecraft:diamond_pickaxe", NarratorA3SignalDetector.FIRST_DIAMOND_PICKAXE);
      require(NarratorA3SignalDetector.inventoryIncrease("minecraft:crossbow") == null, "Une arbalète ne doit pas compter comme arc.");
      require(NarratorA3SignalDetector.inventoryIncrease("modded:obsidian") == null, "Un item moddé homonyme ne doit pas compter.");
   }

   private static void equipmentMappingsAreExact() {
      List<NarratorA3SignalDetector.Signal> onePiece = NarratorA3SignalDetector.equipment(
         new NarratorA3SignalDetector.Equipment(Set.of("minecraft:iron_helmet"), "minecraft:air", "minecraft:air")
      );
      require(has(onePiece, NarratorA3SignalDetector.FIRST_ARMOR), "Une pièce portée doit déclencher la première armure.");
      require(!has(onePiece, NarratorA3SignalDetector.FULL_ARMOR), "Une pièce ne doit pas compter comme armure complète.");

      Set<String> full = Set.of(
         "minecraft:iron_helmet", "minecraft:iron_chestplate", "minecraft:iron_leggings", "minecraft:iron_boots"
      );
      List<NarratorA3SignalDetector.Signal> fullSignals = NarratorA3SignalDetector.equipment(
         new NarratorA3SignalDetector.Equipment(full, "minecraft:iron_pickaxe", "minecraft:shield")
      );
      require(has(fullSignals, NarratorA3SignalDetector.FIRST_ARMOR), "Une armure complète inclut la première armure.");
      require(has(fullSignals, NarratorA3SignalDetector.FULL_ARMOR), "Quatre pièces portées doivent déclencher l'armure complète.");
      require(has(fullSignals, NarratorA3SignalDetector.FIRST_SHIELD), "Le bouclier en main secondaire doit être détecté.");
      require(has(fullSignals, NarratorA3SignalDetector.FIRST_IRON_TOOL), "L'outil en fer tenu doit être détecté.");

      List<NarratorA3SignalDetector.Signal> fake = NarratorA3SignalDetector.equipment(
         new NarratorA3SignalDetector.Equipment(Set.of("modded:iron_helmet"), "minecraft:iron_sword", "minecraft:air")
      );
      require(fake.isEmpty(), "Armure moddé homonyme et épée en fer ne doivent pas créer de faux positif.");
   }

   private static void burstPolicyIsConservative() {
      require(NarratorBurstPolicy.shouldWait("materials", 1000L, 2000L), "Un groupe récent doit attendre brièvement.");
      require(!NarratorBurstPolicy.shouldWait("materials", 1000L, 2600L), "Le délai ne doit pas bloquer indéfiniment.");
      require(
         NarratorBurstPolicy.canJoin("materials", "materials", "actor", "actor", 1000L, 3000L, false),
         "Deux petits événements compatibles doivent pouvoir partager un encadré."
      );
      require(!NarratorBurstPolicy.canJoin("materials", "equipment", "actor", "actor", 1000L, 1200L, false), "Deux thèmes différents restent séparés.");
      require(!NarratorBurstPolicy.canJoin("materials", "materials", "actor", "other", 1000L, 1200L, false), "Deux acteurs différents restent séparés.");
      require(!NarratorBurstPolicy.canJoin("materials", "materials", "actor", "actor", 1000L, 1200L, true), "Un événement avec récompense reste séparé.");
      require(
         NarratorBurstPolicy.combine(List.of("Un", "Deux", "Trois", "Quatre")).equals("Un  •  Deux  •  Trois"),
         "Un encadré condensé doit rester limité à trois phrases."
      );
   }

   private static void pointLabelsAreExplicit() {
      require("+2 ÂGE".equals(NarratorHudText.pointsLine(2, 0)), "Les points d'Âge doivent être nommés.");
      require(
         "+5 ÂGE   ·   +1 CIVILISATION".equals(NarratorHudText.pointsLine(5, 1)),
         "Âge et civilisation doivent être distingués."
      );
      require("+3 CIVILISATION".equals(NarratorHudText.pointsLine(0, 3)), "La civilisation seule doit rester lisible.");
      require(NarratorHudText.pointsLine(0, 0).isBlank(), "Aucun +0 ne doit être affiché.");
   }

   private static void catalogContainsSoloDuoAndCondensationData() {
      require(
         "home_setup".equals(NarratorLegacy.catalogCondenseGroupForTest("A1-027"))
            && "home_setup".equals(NarratorLegacy.catalogCondenseGroupForTest("A1-029")),
         "Le coffre et le lit doivent pouvoir partager un encadré lorsqu'ils sont posés presque ensemble."
      );
      for (String id : NarratorA3SignalDetector.ALL_IDS) {
         require(NarratorLegacy.catalogHasEventForTest(id), "Événement absent du catalogue JSON : " + id);
         require(NarratorLegacy.catalogHasDuoTextForTest(id), "Texte partenaire absent : " + id);
         require(!NarratorLegacy.catalogCondenseGroupForTest(id).isBlank(), "Groupe de condensation absent : " + id);
      }
   }

   private static void expect(String itemId, String eventId) {
      NarratorA3SignalDetector.Signal signal = NarratorA3SignalDetector.inventoryIncrease(itemId);
      require(signal != null && eventId.equals(signal.eventId()), "Mauvais mapping pour " + itemId);
   }

   private static boolean has(List<NarratorA3SignalDetector.Signal> signals, String eventId) {
      return signals.stream().anyMatch(signal -> eventId.equals(signal.eventId()));
   }

   private static void require(boolean condition, String message) {
      if (!condition) {
         throw new AssertionError(message);
      }
   }
}
