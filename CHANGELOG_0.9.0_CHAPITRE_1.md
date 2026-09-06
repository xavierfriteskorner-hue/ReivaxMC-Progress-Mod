# REIVAX MC 0.9.0 — Chapitre I : Le Foyer emprunté

Cette version quitte la phase de construction du moteur et ouvre la première portion réellement jouable de la campagne.

## Campagne

- Démarrage automatique du Chapitre I après l’établissement de la Borne de Fondation.
- Reconnaissance d’un Foyer déjà construit : le joueur conserve sa liberté et ne recommence pas sa base.
- Mission d’installation acceptant trois catégories distinctes parmi repos, réserve, travail, lumière et identité.
- Choix permanent de la première doctrine de civilisation : **Protéger**, **Comprendre** ou **Partager**.
- Exploration guidée vers un Écho généré de façon déterministe autour du Foyer.
- Fragment de mémoire conservé par le joueur, retour automatique au Foyer, confrontation et conclusion propre au choix effectué.
- Séparation canonique des fonctions : la Borne administre la civilisation ; la Matrice, encore inaccessible, analysera plus tard les Fragments.
- Récompense commune de campagne et paquet matériel différent pour chaque doctrine.
- Chronologie, artefact, points d’Âge et points de Civilisation enregistrés séparément.

## SOLO et DUO

- Une seule campagne partagée au niveau du monde : les actions des deux joueurs contribuent au même chapitre.
- Confrontation adaptée au nombre de joueurs connectés.
- Récompense individuelle, y compris pour un partenaire rejoignant le monde après la conclusion.
- État intégralement persistant après sortie du monde ou redémarrage du jeu.
- Filet de sécurité contre la disparition d’un ennemi de mission lors d’un rechargement ou à cause d’un autre mod.

## Mise en scène

- Première fonction narrative concrète du Sanctuaire et de la Borne : révéler ce que le nouveau Foyer recouvre.
- Ton plus adulte sans retirer la liberté de construction.
- Premier « moment signature » court : révélation, alerte sonore et apparition de la menace dans une séquence exploitable en Short, sans système artificiel de capture.

## Développement et validation

- `/reivax dev chapter1 start` : crée si nécessaire une Borne de test et commence directement le chapitre.
- `/reivax dev chapter1 next` : prépare ou rejoint le contrôle suivant ; pendant la confrontation, résout également le combat pour un test express.
- `/reivax dev chapter1 status` : affiche l’état persistant partagé.
- `/reivax dev chapter1 reset` : réinitialise uniquement le Chapitre I.
- Nouvel auto-test `checkChapter1`, intégré à `check`.

## Architecture verrouillée pour la suite

- Sanctuaire conçu comme un édifice évolutif dont la Matrice est visible ou suggérée avant de devenir accessible.
- Ouverture future des salles par Concordances narratives plutôt que par une simple collection de clés.
- Livre défini comme registre portable incomplet et révisable.
- Deux progressions seulement : points d’Âge permanents et points de Civilisation gagnés/dépensables à la Borne.
