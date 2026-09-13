# Validation unique Minecraft — 0.13.1

Durée visée : 20 à 30 minutes. Un monde neuf convient désormais au parcours DEV ; un monde 0.12.2 reste utile uniquement pour vérifier la migration.

## Préparation condensée

1. Installer `reivaxmc_progress-0.13.1.jar` et retirer l'ancien JAR.
2. Entrer dans un monde neuf et exécuter `/reivax dev on`.
3. Exécuter `/reivax dev goto foundation`, puis poser la Borne donnée à l'endroit voulu.
4. Exécuter `/reivax dev chapter4 start`. Cette commande prépare automatiquement le Sanctuaire, ses anciennes portes, la Galerie, le premier Fragment et la Boussole.
5. Faire un clic droit sur la Borne, ouvrir **Pistes**, puis choisir **Suivre cette Piste**.
6. Le jeu doit vous téléporter automatiquement **dans la Galerie des Absents**, face aux trois témoignages. Ne lancez aucune autre commande.

Dans ce raccourci, les anciennes portes sont volontairement ouvertes et les combats des chapitres I à III sont considérés comme terminés. Il ne doit rester aucun Veilleur ni Protecteur.

Sur un monde où la Borne est déjà posée, reprendre directement à l'étape 4.

## Parcours à vérifier

### Galerie

- Les trois consoles cyan du mur du fond sont visibles et ouvrent de grands écrans lisibles.
- Le bouton conserve une seule fois chaque version.
- En DUO, le bloc « Ce que vous seul percevez » doit différer sur au moins une capture.
- Après 3/3, un message explique d'utiliser la Boussole ; un clic droit doit viser les Archives brisées sans changer son mode.

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

- `0.13.1 validée`, ou
- l'étape, ce qui est affiché, ce qui était attendu et une capture si le défaut est visuel.
