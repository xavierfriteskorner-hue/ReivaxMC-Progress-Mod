# REIVAX MC 0.8.1a — correctif du sous-lot A1

Date de préparation : 2026-09-03

## Livré

- Détecteur pur de trois transitions appartenant réellement à l’histoire actuelle.
- Passage de `A1-096` lors de la première Résonance, de `A1-097` lors de l’activation des Stèles du seuil et de `A1-051` lors de la fondation du Foyer par la Borne.
- Report explicite de `A1-066` et `A1-099` jusqu’au futur chapitre de la Matrice.
- Conservation de la déduplication persistante, des points, des récompenses et de la file narrative existante.
- Commande de diagnostic en lecture seule `/reivax_a1`, affichant trois jalons actifs et la Matrice reportée.
- Auto-test Java intégré à la tâche Gradle `check` via `checkA1Signals`.
- Correction Gradle 9 : la tâche `test` accepte l’absence de tests JUnit, tandis que l’auto-test A1 reste obligatoire.
- Checklist de validation SOLO dédiée.
- Bible et Catalogue maîtres synchronisés en V1.8 avec journal de modifications.

## Limites volontaires

- Aucun détecteur A2/A3 n’est ajouté dans ce lot.
- Aucun changement de texte ou de définition JSON ; les entrées de Matrice restent disponibles pour leur futur chapitre.
- Aucun changement de comportement DUO ; sa validation attend la disponibilité de Laeriss.
- Le premier instantané d’une sauvegarde déjà avancée ne rejoue pas rétroactivement les transitions passées.

## Validation

- Compilation Java autonome : passée.
- Auto-test des transitions et du Brain : passé.
- Contrôle du catalogue JSON : trois événements actifs et deux entrées futures toujours présentes.
- Erreur du build 0.8.1 identifiée : compilation principale réussie, auto-test A1 réussi, puis échec de la tâche JUnit vide.
- Compilation Java autonome du correctif : passée.
- À faire : nouveau build NeoForge GitHub puis checklist SOLO en jeu.
