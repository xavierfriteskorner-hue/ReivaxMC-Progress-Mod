# Tests SOLO — REIVAX MC 0.8.3 / lot A3

## But du test

Valider en une seule session les 16 nouveaux événements de progression matérielle, la lisibilité des points et leur sauvegarde.

## Préparation

1. Utiliser de préférence une copie de monde de test avec les commandes autorisées.
2. Démarrer l'histoire si elle ne l'est pas encore.
3. Noter le pourcentage d'Âge avant le test.
4. Taper `/reivax_a3` : le résultat attendu au départ est `0/16` dans un monde neuf ou réinitialisé.

## Ressources et objets — 12 événements rapides

Exécuter une commande, attendre la Voix, puis passer à la suivante :

```text
/give @s minecraft:bucket 1
/give @s minecraft:water_bucket 1
/give @s minecraft:lava_bucket 1
/give @s minecraft:bow 1
/give @s minecraft:emerald 1
/give @s minecraft:copper_ingot 1
/give @s minecraft:lapis_lazuli 1
/give @s minecraft:gold_ingot 1
/give @s minecraft:redstone 1
/give @s minecraft:amethyst_shard 1
/give @s minecraft:obsidian 1
/give @s minecraft:diamond_pickaxe 1
```

Résultat attendu : une phrase de la Voix par découverte, sauf lorsque 2 ou 3 découvertes compatibles très rapprochées sont volontairement réunies dans un seul encadré.

## Équipement — 4 événements

Tenir un outil en fer :

```text
/give @s minecraft:iron_pickaxe 1
```

Placer la pioche en fer dans la main principale. Puis équiper l'armure :

```text
/item replace entity @s armor.head with minecraft:iron_helmet
/item replace entity @s armor.chest with minecraft:iron_chestplate
/item replace entity @s armor.legs with minecraft:iron_leggings
/item replace entity @s armor.feet with minecraft:iron_boots
/item replace entity @s weapon.offhand with minecraft:shield
```

Résultat attendu : outil en fer, première armure, armure complète et premier bouclier sont validés.

## Contrôles finaux

- `/reivax_a3` affiche `16/16` et toutes les lignes sont vertes.
- Le gain total du lot est de **+102 points d'Âge** et **0 point de Civilisation**.
- Le HUD écrit clairement `+X ÂGE`. Il n'affiche plus le vague `+X PTS` ni un `+0 CIVILISATION`.
- Reprendre un objet déjà validé ou retirer/remettre une armure ne redonne aucun point.
- Sortir du monde puis revenir : `/reivax_a3` reste à `16/16`.
- Fermer complètement Minecraft, relancer et revenir : `/reivax_a3` reste à `16/16` et le pourcentage d'Âge reste identique.

## Test facultatif du regroupement

Prendre très vite 2 ou 3 matériaux compatibles depuis l'inventaire créatif. Ils peuvent apparaître dans un seul encadré `PROGRESSION RAPIDE`, limité à trois phrases. S'ils ont été obtenus trop lentement, ils restent affichés l'un après l'autre : les deux comportements sont corrects.

