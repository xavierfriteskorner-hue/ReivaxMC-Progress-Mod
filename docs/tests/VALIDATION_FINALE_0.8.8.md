# Validation finale condensée — REIVAX MC 0.8.8

Cette validation remplace les longues checklists précédentes. Elle vérifie l’intégration en jeu ; les seuils et les cas négatifs sont déjà contrôlés automatiquement à la compilation.

## Préparation

1. Installer le JAR `reivaxmc_progress-0.8.8.jar`.
2. Ouvrir le monde de test où une Borne de Fondation est déjà enregistrée.
3. Exécuter `/reivax dev on` pour masquer les vrais dialogues.
4. Exécuter `/reivax_b3` et prendre une capture de l’état initial.

## Contrôle rapide obligatoire

1. Exécuter `/reivax dev test b1time`.
   - Attendre deux secondes.
   - Vérifier dans `/reivax_b1` que `Aube survécue` et `Sommeil refusé` sont verts.
2. Exécuter `/reivax dev test return20`.
   - Vérifier que le joueur est téléporté près de la Borne enregistrée.
   - Vérifier dans `/reivax_b2` que `Retour 20 min` est vert.
3. Tirer une flèche volontairement dans un bloc.
   - Vérifier l’encadré QA puis `Tir raté ✓` dans `/reivax_b3`.
4. Poser un panneau, écrire au moins un mot et valider l’écran du panneau.
   - Vérifier `Panneau nommé ✓` dans `/reivax_b3`.
5. Dans le territoire du Foyer, entrer sous un toit déjà construit. Si le Foyer possède au moins 32 blocs enregistrés, vérifier `Toit fermé ✓`.

## Contrôles opportunistes, non bloquants

Ces événements seront validés naturellement en jouant et n’exigent plus une séance artificielle dédiée : regarder un Enderman, découvrir un ravin profond, récupérer un objet précieux en danger, voyager 500 blocs avec un animal apprivoisé et errer 600 blocs avec une boussole loin du Foyer.

Le feu au Foyer est volontairement non obligatoire pendant ce test : ne pas incendier une vraie base pour valider un encadré.

## Persistance

1. Quitter le monde puis revenir.
2. Exécuter `/reivax_b1`, `/reivax_b2` et `/reivax_b3`.
3. Vérifier que les validations restent vertes et qu’aucun dialogue narratif réel n’a été révélé par le mode QA.

Si ces étapes sont bonnes, la phase « socle du Narrateur » est terminée. La prochaine version doit avancer le contenu scénaristique.
