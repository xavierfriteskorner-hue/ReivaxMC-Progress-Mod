# Validation unique Minecraft — 0.13.0

Durée visée : 20 à 30 minutes. Le monde de test 0.12.2 est préférable pour vérifier la migration.

## Préparation condensée

1. Installer `reivaxmc_progress-0.13.0.jar` et retirer l'ancien JAR.
2. Entrer dans le monde testé en 0.12.2.
3. Exécuter `/reivax dev on`.
4. Exécuter `/reivax dev chapter4 start`.
5. Revenir près de la Borne et ouvrir l'onglet **Pistes**.
6. Choisir **Suivre cette Piste**.

Si la Borne est trop loin : `/reivax dev goto foundation`. Pour le Sanctuaire : `/reivax dev goto sanctuary`, qui place le joueur dehors sur l'allée.

## Parcours à vérifier

### Galerie

- Les trois nouvelles consoles sont visibles et ouvrent de grands écrans lisibles.
- Le bouton conserve une seule fois chaque version.
- En DUO, le bloc « Ce que vous seul percevez » doit différer sur au moins une capture.
- Après 3/3, la Boussole en mode **Piste actuelle** vise les Archives brisées.

### Archives brisées

- La ruine touche correctement le terrain et ne flotte pas.
- Aucun arbre ni bloc ne coupe son volume intérieur.
- Le réceptacle central donne exactement un Fragment stratifié.
- Les deux traces latérales restent facultatives et paient chacune une seule fois.

### Retour et Concordance

- Revenir à la Galerie avec les deux Fragments.
- SOLO : activer les deux pupitres en moins de 25 secondes.
- DUO : chaque joueur active un pupitre en moins de 12 secondes.
- La porte occidentale s'ouvre sans bloc dans le passage.
- Quitter puis recharger : la porte reste ouverte.

### Matrice

- Cliquer le cœur cyan ouvre la nouvelle interface.
- L'écran annonce que les objets ne seront pas cédés.
- Après **Analyser**, les deux Fragments sont toujours présents.
- La conséquence affichée correspond au choix du Chapitre III.
- En DUO, chaque personne clique **Confronter ma version** ; la première seule ne termine pas le chapitre.
- La conclusion affiche une signature personnelle antérieure à l'arrivée.

## Accélération DEV

`/reivax dev chapter4 next` avance d'un grand acte. Utiliser `/reivax dev chapter4 status` entre deux actes. Cette voie vérifie les transitions, pas les distances ni la mise en scène.

## Verdict à renvoyer

- `0.13.0 validée`, ou
- l'étape, ce qui est affiché, ce qui était attendu et une capture si le défaut est visuel.

