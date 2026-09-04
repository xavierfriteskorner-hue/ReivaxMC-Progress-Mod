# Tests SOLO rapides — REIVAX MC 0.8.5 A5

## Préparation

1. Créer un monde de test neuf avec les commandes autorisées et commencer l'histoire.
2. Facultatif : taper `/reivax dev on` pour ne pas lire les phrases et éviter les spoilers.
3. Taper `/reivax_a5` : le compteur doit commencer à `0/16` dans ce nouveau monde.

## 1 — Fondations et orientation, très rapide

Exécuter successivement :

```text
/give @s minecraft:oak_log
/give @s minecraft:cobblestone
/give @s minecraft:coal
/give @s minecraft:raw_iron
/give @s minecraft:diamond
/give @s minecraft:map
/give @s minecraft:compass
```

- Transformer le bois en planches puis fabriquer réellement un établi : l'établi donné directement ne doit pas suffire.
- Faire cuire n'importe quel objet dans un four, par exemple une pomme de terre ou du fer brut.
- Attendre la fin des encadrés puis taper `/reivax_a5` : les 7 ressources/orientation, l'établi et la cuisson doivent être verts.

## 2 — Animaux, en une seule petite zone

```text
/summon minecraft:cow ~2 ~ ~
/summon minecraft:cow ~4 ~ ~
/give @s minecraft:wheat 2
/summon minecraft:wolf ~6 ~ ~
/give @s minecraft:bone 64
```

- Donner un blé à chaque vache : `Animal nourri` doit se valider, puis `Reproduction` quand le bébé apparaît.
- Apprivoiser réellement le loup avec les os : `Apprivoisement` doit se valider.
- Une simple tentative ratée ne doit pas ajouter de point.

## 3 — Cheval et bateau

```text
/summon minecraft:horse ~2 ~ ~ {Tame:1b}
/give @s minecraft:oak_boat
```

- Monter sur le cheval apprivoisé : `Cheval` devient vert.
- Poser le bateau sur l'eau et monter dedans : `Bateau` devient vert.
- Descendre puis remonter ne doit pas redonner de points.

## 4 — Village

- Trouver ou invoquer un villageois, lui faire un véritable échange : `Échange` devient vert.
- Placer une cloche avec `/setblock ~2 ~ ~ minecraft:bell`, puis faire clic droit dessus : `Cloche` devient vert.
- Casser la cloche ou cliquer dans le vide ne doit rien valider.

## 5 — Vérification finale et persistance

1. Taper `/reivax_a5` : résultat attendu `16/16`.
2. Sur un monde neuf, le lot A5 complet ajoute au maximum `+119 Âge` et `+19 Civilisation` avec les valeurs historiques conservées.
3. Sortir du monde, revenir, puis retaper `/reivax_a5` : le résultat doit rester `16/16` sans nouveau point ni nouvelle phrase.
4. Fermer complètement Minecraft, relancer le jeu et refaire la même vérification.

Le résultat global attendu après validation des lots A1 à A5 est `50/52 événements robustes actifs`. Les deux cases Matrice ne sont pas des échecs : elles sont volontairement reportées.

