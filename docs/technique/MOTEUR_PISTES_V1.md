# Moteur de Pistes v1

## Contrat joueur

Une Piste est une possibilité narrative, jamais une injonction permanente. Elle possède un titre, un objectif lisible, un état partagé et des contributions. La Borne sert à la consulter et à la suivre ; le monde reste jouable normalement pendant tout son déroulement.

## États

`LOCKED → OFFERED → SEEK_RIFTS → RETURN_FOYER → CENSUS → RETURN_SANCTUARY → REGISTRY → COMPLETE`

La sauvegarde `reivaxmc_chapter2_debt` conserve sites, découvertes, Veilleurs reconnus, Concordance, récompenses et délais quotidiens. Tout état critique est côté serveur.

## Principes d'extension

- Ajouter une Piste ne doit pas nécessiter un nouveau moteur d'événements.
- Les objectifs peuvent consommer les faits du Narrateur existant sans dupliquer leurs détecteurs.
- Les étapes principales ne dépendent pas d'un autre mod.
- Les récompenses mondiales utilisent les identifiants idempotents de campagne ; les objets personnels utilisent les UUID.
- Le protocole d'interface transporte un résumé compact séparé par `~` dans le panneau de la Borne.
- Les structures utilisent des positions déterministes autour du Foyer et ne génèrent pas de butin lors du remplacement de terrain.

## Doctrine

- Protection : première menace proche du Foyer chaque jour révélée et affaiblie.
- Compréhension : coordonnées et distance exactes dans la Boussole ; base prête pour des annotations supplémentaires du Journal.
- Partage : premier retour quotidien à faible santé soutient les membres proches.

## Compatibilité

Le moteur est Vanilla + REIVAX. MineColonies peut fournir plus tard des variantes contextuelles, mais l'absence de villageois Vanilla ou de colonie ne bloque aucune Piste.
