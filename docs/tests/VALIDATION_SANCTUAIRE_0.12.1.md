# Validation condensée — Sanctuaire 0.12.1

Une seule session de 10 à 15 minutes suffit. Tester de préférence dans une copie du monde.

## Départ

1. Installer uniquement `reivaxmc_progress-0.12.1.jar`.
2. Entrer dans le monde puis saisir `/reivax dev on`.
3. Saisir `/reivax dev goto sanctuary`.

Résultat attendu : téléportation immédiate sur l'allée, face au Sanctuaire. Le bâtiment se trouve au même endroit dans un monde 0.12.0 et sa reconstruction ne se répète pas.

## Contrôle visuel rapide

- façade reconnaissable, sans apparence de cube ;
- deux grands monolithes à gauche et à droite, chacun avec une cavité circulaire visible ;
- aucune fleur ni aucun bloc en objet sur le sol ;
- aucun bloc isolé dans les airs ;
- aucun trou permettant de contourner une porte ;
- salles futures fermées, sans décoration placée devant leur seuil ;
- intérieur éclairé et décoré, avec nef, nervures, piliers et chambre distinctes.

## Parcours des gardiens

1. Approcher de la façade : deux Veilleurs s'éveillent devant l'entrée.
2. Les neutraliser : la porte reste fermée tant que les deux Sceaux ne sont pas insérés.
3. Insérer un Sceau dans chacun des deux monolithes : la première porte s'ouvre.
4. Entrer : deux Veilleurs attendent dans le hall.
5. Les neutraliser : la seconde porte s'ouvre seulement à cet instant.
6. Avancer et se regrouper devant la porte intérieure : elle s'ouvre progressivement.
7. Vérifier la présence du Protecteur devant la Borne de Fondation.

Les deux autres Veilleurs peuvent être aperçus dans les jardins latéraux ; ils sont facultatifs et ne bloquent aucune porte.

## Persistance

Quitter puis rouvrir le monde. Refaire `/reivax dev goto sanctuary` : la commande doit ramener exactement au même monument, sans duplication et sans disparition des gardiens encore vivants.

