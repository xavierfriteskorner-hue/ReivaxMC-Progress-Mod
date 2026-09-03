# REIVAX MC 0.8.4 — dangers et combats A4

## Contenu ajouté

- 12 événements fiables suivis dans `/reivax_a4`.
- 2 pilotes existants consolidés sans casser leur mémoire : premier Creeper tué et première mort.
- 10 raccordements nouveaux : dégâts de Creeper, chute, chute presque mortelle, noyade évitée, feu, lave, Zombie, Squelette, Araignée et Enderman.
- Textes destinés à l'acteur et au partenaire pour chaque événement : SOLO et DUO sont développés ensemble.

## Détection et mémoire

- Les victimes sont reconnues par leur identifiant vanilla exact : aucun monstre moddé homonyme ne valide un événement.
- Les dégâts sont observés après leur application réelle ; un dégât annulé ou nul ne compte pas.
- La lave et le feu restent deux souvenirs distincts sans double validation sur un même dégât de lave.
- La noyade évitée demande d'abord de subir un dégât de noyade, puis de retrouver plus de la moitié de son air en restant vivant.
- La mémoire des événements reste persistante dans la sauvegarde et empêche tout doublon après reconnexion ou redémarrage.

## Réalisateur et HUD

- Une chute presque mortelle valide deux faits mais les réunit dans un seul encadré `PROGRESSION RAPIDE`.
- Les points additionnés restent affichés explicitement : jusqu'à +79 Âge sur une sauvegarde neuve et +1 Civilisation pour le pilote historique du premier Creeper tué.
- Les combats rapprochés peuvent être condensés, sans mélanger deux joueurs ni dépasser trois phrases.
- Le mode `/reivax dev on` continue de masquer les phrases afin de tester sans se spoiler.

## Validation technique

- Auto-tests A1, A2, A3 et A4 prévus dans le build GitHub.
- Test A4 autonome : 12 IDs, filtres exacts, cas négatifs, récupération après noyade, textes DUO et condensation des chutes.
- Compilation NeoForge complète à confirmer par GitHub Actions après le push.

## Tests en jeu demandés

- Utiliser un monde de test avec les commandes activées et `/reivax dev on` si les phrases doivent rester cachées.
- Provoquer les événements, vérifier `/reivax_a4`, les points d'Âge puis sortir/revenir dans le monde.
- Le test réel DUO reste reporté jusqu'à la disponibilité de Laeriss ; aucun second joueur n'est requis pour valider le lot SOLO.
