# Implémentation technique — 0.13.0

## Architecture

- `V13Chapter4Rules` contient les étapes, objectifs et règles pures de Concordance.
- `V13Chapter4Data` sauvegarde l'état mondial, les témoignages, les marges, le site, les participants et les lectures reconnues.
- `V13Chapter4Engine` gère l'offre différée, les interactions, la construction des Archives, la Boussole, les retours d'objets, l'analyse et les commandes DEV.
- `V13Payloads`, `V13WitnessScreen` et `V13MatrixScreen` séparent les lectures communes des perceptions propres à chaque écran.
- `chapter4_memory_truth.json` décrit le parcours pour la future généralisation du moteur de Pistes.

Le protocole réseau passe de 16 à 17.

## Étapes persistantes

`LOCKED → OFFERED → TESTIMONIES → SEEK_FRAGMENT → RETURN_GALLERY → STRONG_CONCORDANCE → MATRIX_READY → REVELATION → COMPLETE`

Chaque mutation appelle `setDirty`. Les récompenses du chapitre et des deux marges utilisent les reçus idempotents de `CampaignSavedData.complete`.

## Protection des objets

L'analyse vérifie la présence du Fragment inconnu et du Fragment stratifié près de la Galerie ou de la Matrice. Elle ne retire aucun `ItemStack`. Si un exemplaire n'existe plus chez aucun joueur connecté, le premier pupitre peut le restituer ; si un partenaire le porte ailleurs, aucune copie n'est créée.

## Monde et structure

Les Archives brisées sont générées relativement à la Borne, sans biome ni mod externe requis. Leur plateforme reçoit des supports jusqu'à huit blocs sous les voies porteuses. L'intérieur est dégagé avant la pose des murs afin d'éviter arbres et blocs flottants.

La porte de Matrice possède le reçu `F130_MATRIX_OPEN`. `C110SanctuaryArchitecture` respecte ce reçu lors d'une reconstruction et la rouvre au chargement d'un chapitre déjà avancé.

## Compatibilité

- Aucun lien dur avec MineColonies ou un autre mod de contenu.
- Les mondes 0.12.2 conservent leur Sanctuaire et obtiennent le chapitre après la fin du Chapitre III.
- Le Fragment stratifié réutilise provisoirement la texture du Fragment inconnu, mais possède un identifiant, un nom et un modèle distincts.

## Vérifications automatiques

`checkChapter4` vérifie les fenêtres SOLO/DUO, l'exigence de deux joueurs distincts, tous les objectifs et les trois branches de politique de mémoire. La tâche `check` exécute ce test avec les douze familles déjà existantes.

