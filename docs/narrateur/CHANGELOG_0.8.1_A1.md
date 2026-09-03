# REIVAX MC 0.8.1 — sous-lot A1

Date de préparation : 2026-09-03

## Livré

- Détecteur pur de cinq transitions internes du Story Bus.
- Passage des événements `A1-051`, `A1-066`, `A1-096`, `A1-097` et `A1-099` par le Brain, le Réalisateur puis le HUD.
- Suppression de la validation silencieuse des trois événements des Origines concernés.
- Conservation de la déduplication persistante, des points, des récompenses et de la file narrative existante.
- Commande de diagnostic en lecture seule `/reivax_a1`.
- Auto-test Java intégré à la tâche Gradle `check` via `checkA1Signals`.
- Checklist de validation SOLO dédiée.
- Bible et Catalogue maîtres synchronisés en V1.8 avec journal de modifications.

## Limites volontaires

- Aucun détecteur A2/A3 n’est ajouté dans ce lot.
- Aucun changement de texte ou de définition JSON.
- Aucun changement de comportement DUO ; sa validation attend la disponibilité de Laeriss.
- Le premier instantané d’une sauvegarde déjà avancée ne rejoue pas rétroactivement les transitions passées.

## Validation

- Compilation Java autonome : passée.
- Auto-test des transitions et du Brain : passé.
- Contrôle du catalogue JSON : 5/5 événements présents.
- Catalogue XLSX : 25 feuilles rendues, aucune erreur de formule détectée.
- À faire : build NeoForge complet puis checklist SOLO en jeu.
