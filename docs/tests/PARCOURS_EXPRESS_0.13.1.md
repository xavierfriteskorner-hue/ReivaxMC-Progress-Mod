# Parcours express de l'histoire — 0.13.1

Ce parcours sert à vérifier rapidement l'enchaînement visuel, narratif et technique du prologue jusqu'à la fin du Chapitre IV.

## Préparation

Utiliser un monde créatif neuf avec les commandes autorisées. Le parcours nettoie les états narratifs à l'arrêt, mais conserve les structures qu'il a construites : ne pas l'utiliser sur une sauvegarde de survie importante.

Une seule commande est nécessaire pour commencer :

```mcfunction
/reivax dev storytest start
```

Le contenu narratif réel est automatiquement rendu visible. À chaque point de contrôle, le jeu affiche ce qui doit être observé.

## Commandes

- `/reivax dev storytest next` : valider l'observation et préparer l'étape suivante.
- `/reivax dev storytest check` : afficher le point actuel et auditer les quatre chapitres.
- `/reivax dev storytest replay` : réafficher l'objectif de contrôle actuel.
- `/reivax dev storytest back` : revenir au contrôle précédent pendant le prologue ; dans un chapitre, redémarrer proprement ce chapitre.
- `/reivax dev storytest stop` : arrêter le parcours et nettoyer les états narratifs.
- `/reivax dev auditstate` : afficher directement les états I à IV et toute incohérence détectée.

## Chemin couvert

1. Trace inconnue.
2. Première Résonance nocturne.
3. Extérieur du Sanctuaire : entrée, monolithes et Veilleurs.
4. Fondation : pose de la Borne.
5. Chapitre I — Le Foyer emprunté.
6. Chapitre II — La Dette du Foyer.
7. Chapitre III — Les Noms retirés.
8. Chapitre IV — La Mémoire n'est pas la vérité.

Dans les chapitres, `next` fait avancer le moteur DEV d'une transition seulement. Il faut donc regarder l'objectif, la scène, les portes, les entités et l'interface avant de relancer la commande.

## Résultat attendu

À la fin, `/reivax dev storytest check` doit afficher :

- Chapitres I, II, III et IV sur `COMPLETE` ;
- `Actifs 0` ;
- `COHÉRENT` ;
- aucun Protecteur ou Veilleur dupliqué ;
- une seule nouvelle piste proposée à chaque transition.

Durée indicative en SOLO : 15 à 25 minutes.
