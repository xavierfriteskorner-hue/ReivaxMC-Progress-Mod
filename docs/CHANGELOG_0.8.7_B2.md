# REIVAX MC 0.8.7 — Lot B2 Foyer et corrélations DUO

## Résultat

- 15 événements B2 couverts : 4 historiques consolidés et 11 nouveaux.
- 85 événements robustes du catalogue technique sont maintenant actifs.
- La Matrice reste strictement reportée au chapitre scénaristique prévu.
- Le même pipeline reste utilisé : Détecteur → Brain → Réalisateur → HUD.

## Foyer — SOLO et DUO

- Éloignement à 250 blocs du Foyer (`A1-043`).
- Retour après une excursion d'au moins 1 000 blocs (`A1-047`).
- Explosion subie dans le territoire du Foyer (`A1-083`).
- Première porte posée dans le territoire (`A1-154`).
- Retour au Foyer de nuit après un véritable éloignement (`A1-155`).
- Retour après plus de vingt minutes d'absence (`A1-156`).
- Premier bloc de diamant, d'émeraude ou de netherite rangé dans un stockage du Foyer (`A1-163`).
- Coffre du Foyer rempli à au moins 90 % de ses cases (`A1-164`).

L'absence, sa durée et la distance maximale sont sauvegardées avec le monde. Une sortie complète du jeu pendant un voyage ne remet donc pas le suivi à zéro.

## Corrélations DUO prêtes à tester plus tard

- Partenaires séparés de 500 blocs (`A1-157`) puis de 1 000 blocs (`A1-049`).
- Retrouvailles à moins de 20 blocs après séparation (`A1-158`).
- Mort d'un joueur à moins de 32 blocs de l'autre (`A1-159`).
- Deux joueurs simultanément à 25 % de vie ou moins (`A1-160`).
- Même événement découvert par les deux joueurs en moins de 10 secondes (`A1-161`).
- Objet réellement jeté par un joueur puis ramassé par l'autre en moins de 15 secondes (`A1-162`).

Les calculs DUO sont centralisés côté serveur : une seule décision est prise et chacun reçoit son texte adapté.

## Sécurité et performances

- Distances et santé vérifiées une fois par seconde, pas à chaque image.
- Aucun scan global du monde.
- Les coffres sont inspectés localement et seulement après interaction au Foyer.
- Le cadeau DUO exige le même objet au sol, pas seulement le même type d'objet.
- Les événements de regard, de forme de toit, de maison en feu et de compagnon voyageur restent reportés : ils demandent un détecteur spécialisé pour éviter les faux positifs.

## Outils et validation

- Nouvelle commande `/reivax_b2` : état détaillé des 15 événements.
- Nouvel auto-test `checkB2Signals` : seuils, anti-faux-positifs, catalogue SOLO/DUO et condensation.
- Compilation complète `clean check build` validée avec les auto-tests A1 à B2.

