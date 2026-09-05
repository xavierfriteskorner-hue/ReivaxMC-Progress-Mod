# REIVAX MC 0.8.6 — Checklist DUO du lot B1

Cette checklist est prête mais son exécution est reportée jusqu'à la disponibilité de Laeriss. Le code SOLO et DUO est déjà commun : il ne faudra pas réadapter les détecteurs plus tard.

## Préparation

1. Les deux joueurs rejoignent le même monde et commencent l'histoire REIVAX.
2. Activer le masque anti-spoiler avec `/reivax dev on` si souhaité.
3. Utiliser `/reivax_b1` pour suivre les validations.

## Test court représentatif

- [ ] ReivaxMC plante une culture : ReivaxMC reçoit le texte acteur et Laeriss le texte partenaire.
- [ ] Laeriss découvre un village ou un océan : Laeriss devient l'actrice de l'événement et ReivaxMC reçoit la version partenaire.
- [ ] Un joueur mange une pomme pendant que l'autre déclenche un repas cuit : les messages sont mis en file sans se recouvrir.
- [ ] Déclencher deux événements d'un même groupe à quelques secondes d'intervalle : l'encadré condensé ne doit pas dépasser trois éléments.
- [ ] Nommer un animal près de l'autre joueur : chacun reçoit le bon point de vue narratif.
- [ ] Blesser à moins de 50 % un compagnon appartenant à ReivaxMC puis un compagnon appartenant à Laeriss : le propriétaire correct doit être crédité.
- [ ] Jeter un diamant : le joueur qui le jette est bien l'acteur, jamais son partenaire.
- [ ] Quitter et rejoindre le monde avec un seul joueur puis avec les deux : aucune validation parasite ne doit apparaître.
- [ ] Fermer complètement le serveur ou le monde, relancer, puis vérifier `/reivax_b1` : le total doit persister.

## Résultat attendu

Les points ne sont accordés qu'une fois au monde partagé. Le joueur responsable reçoit le texte acteur ; son partenaire reçoit le texte DUO. Aucune présence du second joueur n'est requise pour que le même détecteur fonctionne en SOLO.
