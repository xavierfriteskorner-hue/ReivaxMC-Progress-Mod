# Tests DUO différés — REIVAX MC 0.8.4 / lot A4

À exécuter plus tard avec Laeriss. Aucun de ces tests n'est requis pour valider la compilation ou le test SOLO de la 0.8.4.

## Préparation

1. Les deux joueurs rejoignent le même monde et démarrent l'histoire commune.
2. Utiliser `/reivax dev on` si vous souhaitez masquer les phrases pendant le contrôle.
3. Noter les points communs et l'état `/reivax_a4` avant les actions.

## Vérifications essentielles

- ReivaxMC tue un Zombie : l'événement n'est validé qu'une fois, les points communs ne sont ajoutés qu'une fois et Laeriss reçoit la version partenaire si le mode anti-spoil est désactivé.
- Laeriss tue ensuite un Squelette : le texte partenaire reçu par ReivaxMC doit nommer Laeriss comme acteur.
- Les deux joueurs réalisent rapidement deux combats différents : le Réalisateur ne doit jamais fusionner les actions de deux acteurs dans un même encadré.
- Un joueur subit une chute presque mortelle : les deux faits de chute peuvent partager un encadré, mais le partenaire ne reçoit qu'une seule progression commune de +15 Âge.
- Un joueur meurt : le partenaire reçoit la version DUO, tandis que la progression et la récompense ne sont pas doublées.

## Persistance

- Les deux joueurs se déconnectent puis reviennent : `/reivax_a4` conserve les mêmes validations.
- Répéter un événement avec l'autre joueur ne redonne aucun point : la mémoire est commune au monde.
