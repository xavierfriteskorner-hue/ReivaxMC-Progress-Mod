# REIVAX MC 0.8.2 — sous-lot A2 SOLO + DUO

Date de préparation : 2026-09-03

## Livré

- Ajout de trois événements robustes : premier bâton (`A1-026`), premier coffre posé (`A1-027`) et premier lit posé (`A1-029`).
- Passage complet par l'architecture Détecteur → Brain → Réalisateur → HUD.
- Détection légère : inventaire déjà surveillé pour le bâton, événement direct de pose pour le coffre et le lit.
- Textes acteur et partenaire intégrés pour chaque événement afin de construire SOLO et DUO ensemble.
- Déduplication persistante par monde et conservation de la mémoire du Brain.
- Commande de diagnostic en lecture seule `/reivax_a2` avec compteur `0/3` à `3/3`.
- Auto-test Java A2 branché à la tâche Gradle `check`, en plus de l'auto-test A1 existant.
- Checklists séparées : SOLO immédiatement testable ; DUO préparé mais test en jeu reporté.
- Bible et Catalogue maîtres synchronisés en V1.9 avec journal de modifications.

## Validation technique

- Catalogue JSON lisible : passé.
- Compilation Java autonome des composants concernés : passée.
- Auto-test A1 de non-régression : passé.
- Auto-test A2 des détections, faux positifs et textes DUO : passé.
- Build NeoForge complet : à confirmer par GitHub Actions après le push.
- Validation SOLO en jeu : à effectuer avec la checklist 0.8.2.
- Validation DUO en jeu : volontairement reportée jusqu'à la disponibilité de Laeriss.

## Règle adoptée pour la suite

Les futurs événements seront pensés et codés pour SOLO et DUO dans le même lot. Seule la date des tests en jeu diffère : SOLO au fil de l'eau, DUO lors d'une session avec les deux joueurs.

