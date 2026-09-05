# REIVAX MC 0.8.7 — Checklist DUO du lot B2

Ce test est préparé mais reporté jusqu'à la disponibilité de Laeriss. Utiliser un monde de test avec histoire commencée et `/reivax dev on` si vous voulez éviter les spoilers.

## Distance et retrouvailles

1. Les deux joueurs restent dans la même dimension.
2. Éloignez-vous à 500 blocs : **Duo 500** doit passer au vert une seule fois.
3. Continuez jusqu'à 1 000 blocs : **Duo 1000** doit passer au vert une seule fois.
4. Revenez à moins de 20 blocs : **Retrouvailles** doit passer au vert.

## Danger partagé

1. Placez-vous à moins de 32 blocs l'un de l'autre.
2. Avec une commande de dégâts ou des monstres, descendez chacun à 5 cœurs ou moins : **Deux faibles** doit passer au vert.
3. Faites mourir l'un des joueurs à moins de 32 blocs de l'autre : **Mort proche** doit passer au vert.
4. Vérifiez que les deux joueurs reçoivent un encadré adapté, sans double attribution de points.

## Découverte partagée

1. Choisissez un événement encore gris et simple à provoquer pour les deux joueurs, par exemple une première pomme mangée dans un monde de test neuf.
2. Provoquez-le chez les deux joueurs à moins de dix secondes d'intervalle.
3. **Découverte partagée** doit passer au vert une seule fois.

## Cadeau réel

1. Le premier joueur jette un objet avec la touche de drop.
2. Le second ramasse exactement cet objet dans les quinze secondes.
3. **Cadeau partenaire** doit passer au vert.
4. Contrôle négatif : un objet déjà au sol depuis longtemps ou jeté puis repris par son propriétaire ne doit pas compter.

## Persistance DUO

Quittez complètement les deux clients, relancez le monde, reconnectez les deux joueurs et vérifiez `/reivax_b2`. Tous les ✓ doivent rester et aucun jalon ne doit être recréé à la connexion.

