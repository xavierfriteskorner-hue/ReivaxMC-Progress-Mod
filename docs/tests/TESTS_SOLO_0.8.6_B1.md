# REIVAX MC 0.8.6 — Checklist SOLO du lot B1

## Avant de commencer

1. Créer un monde de test avec les commandes autorisées et commencer l'histoire REIVAX.
2. Pour éviter les spoilers narratifs : `/reivax dev on`.
3. Pour suivre les validations : `/reivax_b1`.
4. Les commandes servent uniquement à accélérer les conditions. Les détecteurs restent les vrais détecteurs de jeu.

## Départ, temps et survie

- [ ] **Première culture** — labourer avec une houe, planter du blé, des carottes, des pommes de terre ou des betteraves.
- [ ] **Survivre jusqu'à l'aube** — `/time set 14000`, attendre 2 secondes, puis `/time set 23000` sans dormir.
- [ ] **500 blocs parcourus** — courir, voler ou naviguer réellement sur 500 blocs. `/effect give @s minecraft:speed 120 4 true` accélère le test. Une téléportation est volontairement ignorée.
- [ ] **Demi-cœur** — en Survie, `/attribute @s minecraft:generic.max_health base set 1`, attendre l'encadré, puis restaurer avec `/attribute @s minecraft:generic.max_health base set 20`.
- [ ] **Foudre** — dehors, `/summon minecraft:lightning_bolt ~ ~ ~`.
- [ ] **Première nuit dormie** — `/time set night`, dormir normalement dans un lit jusqu'au matin.
- [ ] **Refus de dormir** — ne pas dormir et provoquer trois passages distincts en nuit : `/time set 14000`, attendre 2 secondes, puis alterner quatre fois `/time add 12000` en attendant 2 secondes entre chaque commande.
- [ ] **Première faim critique** — `/effect give @s minecraft:hunger 90 20 true`, puis sprinter jusqu'à deux gigots ou moins.

## Nourriture et inventaire

- [ ] **Première pomme mangée** — `/give @s minecraft:apple`, perdre un peu de faim puis manger la pomme.
- [ ] **Premier repas cuit** — `/give @s minecraft:cooked_beef`, perdre un peu de faim puis le manger.
- [ ] **Inventaire complètement plein** — `/clear @s`, puis `/give @s minecraft:dirt 2304`; vérifier que les 36 cases principales sont occupées.
- [ ] **Jeter un objet précieux** — `/give @s minecraft:diamond`, puis le jeter volontairement avec la touche de drop.

## Monde et exploration

- [ ] **Village découvert** — utiliser `/locate structure #minecraft:village`, se téléporter près des coordonnées puis entrer dans la zone. Avec certains modpacks supprimant les villages vanilla, noter le test comme compatibilité reportée.
- [ ] **Premier océan** — `/locate biome minecraft:ocean`, puis se rendre aux coordonnées.
- [ ] **Premier sommet élevé** — passer en Créatif et voler dehors au-dessus de Y=160.
- [ ] **Première neige** — `/locate biome minecraft:snowy_plains`, puis se rendre aux coordonnées.
- [ ] **Premier désert** — `/locate biome minecraft:desert`, puis se rendre aux coordonnées.
- [ ] **Première jungle** — `/locate biome minecraft:jungle`, puis se rendre aux coordonnées.
- [ ] **Première grotte profonde** — en Créatif ou Spectateur, descendre sous Y=0 avec un plafond au-dessus de soi.
- [ ] **Sous Y=-40** — descendre jusqu'à Y=-40 ou plus bas dans l'Overworld.
- [ ] **Premier orage** — dehors, `/weather thunder 60`.
- [ ] **Premier lever de soleil observé dehors** — dehors et éveillé, `/time set 14000`, attendre 2 secondes, puis `/time set 23000`.

## Animaux et compagnons

- [ ] **Premier nom donné à un animal** — renommer une étiquette dans une enclume, puis l'utiliser sur un animal encore sans nom.
- [ ] **Compagnon blessé** — apprivoiser un loup ou un chat, puis lui faire perdre assez de vie pour passer à 50 % ou moins sans le tuer.

## Persistance et non-régression

- [ ] Noter le total `/reivax_b1`, quitter le monde, revenir : le même total doit rester affiché.
- [ ] Fermer complètement Minecraft, relancer le monde : le total doit encore être identique.
- [ ] Reproduire un événement déjà validé : aucun point supplémentaire et aucune seconde validation.
- [ ] Vérifier que les événements Matrice ne sont toujours pas actifs.

## Résultat attendu

`/reivax_b1` doit finir à **24/24**. Le compteur global doit indiquer **74/76 événements robustes actifs**, les deux événements Matrice restant reportés au chapitre prévu.
