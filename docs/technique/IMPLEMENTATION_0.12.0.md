# Implémentation 0.12.0

## Autorité et sauvegarde

`V12TrailData` est l'unique autorité mondiale du chapitre. Les perceptions sont indexées par UUID mais les transitions restent partagées. Toutes les actions sont idempotentes : double clic, reconnexion et rediffusion réseau ne redonnent ni progression ni récompense.

## Flux

`LOCKED → OFFERED → EIGHTH_LINE → PROCESSION_LOCATE → PROCESSION_OBSERVE → HOUSE_LOCATE → HOUSE_INSPECT → RETURN_REGISTRY → CROSS_CALL → MEMORY_COUNCIL → COMPLETE`.

Le moteur spécial pilote la mise en scène, tandis que les trois fichiers JSON embarqués dans `data/reivaxmc_progress/trails` établissent le format V2 destiné aux prochaines Pistes. Les salles ont des ancres logiques fixes indépendantes de leur décoration.

## SOLO et DUO

- La liste des participants est figée à l'inscription de la Piste.
- La huitième ligne est personnelle ; le Journal intime conserve seulement la perception du joueur.
- La Chronique commune ne révèle pas les deux noms.
- Appel croisé : 500 ticks en SOLO, 240 ticks et deux UUID distincts en DUO.
- Conseil : toutes les voix sont requises et une divergence reste en attente.

## Migration du Sanctuaire

Le marqueur `F120_SANCTUARY_REFORGED` empêche toute reconstruction répétée. Les blocs sont écrits avec le drapeau 18. Le nettoyage d'entités est limité au volume du Sanctuaire et aux objets âgés de moins de 200 ticks. Les positions canoniques du Reliquaire et du Livre sont préservées ; les anciennes portes `z+22`, `z+7` et `z-7` correspondent toujours à de vrais seuils.

La disposition des trois Fêlures passe au schéma `2` : les anciens sites 0.11.0 sont nettoyés bloc par bloc, reconstruits plus loin et leurs états découverts ou facultatifs restent inchangés.
