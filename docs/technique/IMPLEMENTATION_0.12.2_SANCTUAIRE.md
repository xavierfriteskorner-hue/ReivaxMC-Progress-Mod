# Implémentation 0.12.2 — Finition du Sanctuaire

## Principe

`C110SanctuaryArchitecture` reste l'unique constructeur autoritaire. La finition est séparée en deux passes appelées après la structure fonctionnelle :

- `roofscape` construit le couronnement extérieur ;
- `interior` distribue la décoration par salle.

Les portes, les consoles et les objets narratifs sont construits avant ces passes. Les emplacements sensibles ont été exclus des boucles décoratives.

## Migration

Le marqueur devient `F122_SANCTUARY_DECORATED`. Le bloc physique de détection se trouve en `(0,+20,-3)`, à l'intérieur de la tour-lanterne scellée. Un Sanctuaire 0.12.1 échoue donc volontairement à `isPresent`, est reconstruit une fois, puis devient stable.

## Autel de Fondation

La Borne reste à sa coordonnée narrative historique `(0,+4,-18)`. Son support occupe les niveaux `+1`, `+2` et `+3`. L'encoche orientée vers l'entrée évite de placer un bloc dans la colonne du Protecteur historique à `z=-15`.

## Contraintes conservées

- Porte du seuil : `z=+22`.
- Porte du hall : `z=+7`.
- Chambre de Fondation : `z=-7`.
- Registre : seuil latéral est autour de `(18,-16)`.
- Matrice : seuil latéral ouest autour de `(-18,-16)`.
- Galerie : seuil autour de `(23,-29)`.
- Hauteur maximale de la finition : `y+23`, dans la zone protégée.

## Choix de conception

La décoration se concentre sur les bords, les murs et les plafonds. Les centres des salles restent lisibles pour deux joueurs en vidéo et suffisamment libres pour les combats. Chaque espace possède néanmoins un vocabulaire distinct : archives, résonance, mémoire, fondation ou défense.

