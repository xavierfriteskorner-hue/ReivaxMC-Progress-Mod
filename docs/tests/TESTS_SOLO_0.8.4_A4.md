# Tests SOLO — REIVAX MC 0.8.4 / lot A4

## But du test

Valider rapidement 12 événements de dangers et combats, leurs points, l'anti-doublon et la persistance. Deux événements utilisent les anciens pilotes 0.8.0 : Creeper tué et première mort.

## Préparation anti-spoil

1. Utiliser une copie de monde de test avec les commandes activées.
2. Démarrer l'histoire si nécessaire.
3. Taper `/reivax dev on` : les phrases de la Voix et l'historique sont masqués, mais les événements et les points continuent de fonctionner.
4. Taper `/gamerule keepInventory true` puis passer en Survie avec `/gamemode survival`.
5. Noter les points d'Âge et taper `/reivax_a4` avant de commencer. Un ancien monde peut déjà avoir validé Creeper tué ou première mort.

## Combats — 5 validations rapides

Faire apparaître chaque créature immobile puis lui attribuer un coup de joueur :

```text
/summon minecraft:creeper ~ ~ ~3 {NoAI:1b,Silent:1b,PersistenceRequired:1b}
/damage @s 1 minecraft:explosion by @e[type=minecraft:creeper,limit=1,sort=nearest]
/damage @e[type=minecraft:creeper,limit=1,sort=nearest] 100 minecraft:player_attack by @s

/summon minecraft:zombie ~ ~ ~3 {NoAI:1b,Silent:1b}
/damage @e[type=minecraft:zombie,limit=1,sort=nearest] 100 minecraft:player_attack by @s

/summon minecraft:skeleton ~ ~ ~3 {NoAI:1b,Silent:1b}
/damage @e[type=minecraft:skeleton,limit=1,sort=nearest] 100 minecraft:player_attack by @s

/summon minecraft:spider ~ ~ ~3 {NoAI:1b,Silent:1b}
/damage @e[type=minecraft:spider,limit=1,sort=nearest] 100 minecraft:player_attack by @s

/summon minecraft:enderman ~ ~ ~3 {NoAI:1b,Silent:1b}
/damage @e[type=minecraft:enderman,limit=1,sort=nearest] 100 minecraft:player_attack by @s
```

Le premier `/damage` sur le joueur valide les dégâts de Creeper ; le second tue le Creeper avec le joueur comme responsable. Les quatre autres commandes de dégâts valident Zombie, Squelette, Araignée et Enderman.

## Dangers — 6 validations rapides

```text
/damage @s 1 minecraft:drown
/damage @s 1 minecraft:on_fire
/damage @s 1 minecraft:lava
/tp @s ~ ~20 ~
```

- Le dégât `drown`, suivi du retour automatique à un air normal, valide la noyade évitée.
- Le feu et la lave doivent produire deux souvenirs distincts. Ils peuvent partager un encadré si les phrases sont visibles et si les actions sont très rapprochées.
- La téléportation 20 blocs au-dessus d'un sol plat doit laisser le joueur vivant à environ 1,5 cœur sans armure ni effet : chute importante et chute presque mortelle sont validées ensemble. Elles peuvent apparaître dans un seul encadré `PROGRESSION RAPIDE` avec +15 Âge.

Si la chute tue le joueur à cause du relief ou d'un état de santé trop bas, se soigner et recommencer depuis un sol plat ; la première mort peut alors être déjà validée.

## Première mort — 1 validation

```text
/kill @s
```

Réapparaître puis attendre quelques secondes. Cette validation conserve l'ancien ID A1-084 afin de ne jamais rejouer une première mort déjà mémorisée.

## Contrôles finaux

- `/reivax_a4` affiche `12/12` et toutes les lignes sont vertes.
- Sur un monde où A4 était à `0/12`, le total est **+79 points d'Âge** et **+1 point de Civilisation**.
- Si Creeper tué ou première mort étaient déjà verts avant le test, leurs anciens points ne sont pas redonnés : comparer le gain uniquement aux événements gris du départ.
- Répéter le Zombie ou le dégât de lave : aucun nouveau point et aucun second souvenir.
- Sortir du monde, revenir et taper `/reivax_a4` : le résultat reste `12/12`.
- Fermer complètement Minecraft, relancer et vérifier encore : compteur et points restent identiques.

## Contrôle facultatif des faux positifs

- Tuer un Husk ne doit pas valider Zombie.
- Tuer une Araignée venimeuse ne doit pas valider Araignée.
- Subir une explosion de TNT ne doit pas valider Dégâts Creeper.
- Faire une petite chute d'un ou deux cœurs ne doit pas valider Chute importante.
