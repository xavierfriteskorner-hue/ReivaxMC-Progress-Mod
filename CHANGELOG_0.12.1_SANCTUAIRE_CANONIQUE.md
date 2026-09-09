# 0.12.1 — Le Sanctuaire canonique

Cette version est un lot correctif exclusivement consacré au Sanctuaire. Elle ne fait pas avancer le chapitre III et ne modifie pas ses révélations.

## Architecture

- remplacement des deux anciens constructeurs concurrents par une seule architecture canonique ;
- reconstruction automatique, une seule fois, des Sanctuaires issus de la 0.12.0 ;
- nef voûtée à double peau, murs fermés, socle continu et suppression des accès accidentels ;
- façade monumentale, portique lisible, allée extérieure et deux monolithes verticaux symétriques ;
- ailes du Registre et de la Matrice, Galerie et quatre seuils futurs présents dès l'origine ;
- décoration intérieure, éclairages, nervures, autels, jardins latéraux et contreforts ;
- raccords en terrasses vers le relief au lieu des grands rectangles d'herbe suspendus ;
- pose sans butin et nettoyage strictement local des objets créés pendant la génération.

## Parcours et créatures

- deux Veilleurs devant l'entrée ;
- deux Veilleurs dans le hall ;
- deux Veilleurs facultatifs dans les jardins latéraux ;
- un Protecteur dans la chambre de Fondation ;
- réparation automatique de chaque gardien manquant tant qu'il n'a pas été vaincu ;
- suppression des faux figurants invulnérables ajoutés en 0.12.0 ;
- ouverture de la seconde porte uniquement après la chute des deux Veilleurs du hall.

## Placement et outils DEV

- sélection d'un terrain fondée sur quinze mesures couvrant toute l'emprise du Sanctuaire ;
- rejet des sites traversés par l'eau et forte pénalisation des reliefs trop brutaux ;
- position enregistrée définitivement dans la sauvegarde pour empêcher tout déplacement au redémarrage ;
- compatibilité : un ancien monde retrouve d'abord ses coordonnées 0.12.0 avant de les enregistrer ;
- `/reivax dev goto sanctuary` construit/répare si nécessaire puis téléporte réellement sur l'allée extérieure.
