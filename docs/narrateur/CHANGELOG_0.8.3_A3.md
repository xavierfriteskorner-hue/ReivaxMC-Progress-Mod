# REIVAX MC 0.8.3 — lot accéléré A3

## Contenu ajouté

- 16 événements robustes supplémentaires : seaux, ressources, armure, bouclier, arc et outils.
- Détection précise à partir de l'inventaire et de l'équipement réellement porté ou tenu.
- Textes SOLO et variantes DUO pour chaque nouvel événement.
- Commande de contrôle `/reivax_a3` avec progression détaillée sur 16.

## Améliorations du Réalisateur et du HUD

- Les gains sont désormais nommés clairement : `ÂGE` et, lorsqu'il existe, `CIVILISATION`.
- Jusqu'à trois petits événements compatibles et très rapprochés peuvent partager un encadré `PROGRESSION RAPIDE`.
- Le regroupement conserve séparément chaque souvenir, chaque validation et chaque gain de points.
- Les événements importants, récompensés, de thèmes différents ou réalisés par deux acteurs différents restent séparés.
- La file d'attente a été agrandie pour absorber les séries de découvertes sans en perdre.

## Validation technique

- Auto-tests A1, A2 et A3 passés.
- Catalogue JSON validé.
- Compilation autonome des classes du Narrateur et du HUD réussie.
- Compilation NeoForge complète à confirmer par GitHub Actions après le push.

## État fonctionnel

- A1 : validé SOLO.
- A2 : validé SOLO, y compris file d'attente et redémarrage complet du jeu.
- A3 : prêt pour test SOLO.
- DUO : développé en parallèle, test réel reporté jusqu'à la disponibilité de Laeriss.

