# REIVAX MC 0.8.5 — monde vivant A5

## Résultat du lot robuste

- 16 événements A5 sont désormais raccordés à l'architecture Détecteur → Brain → Réalisateur → HUD.
- Le total atteint 50 événements robustes jouables sur les 52 spécifiés.
- Les 2 événements restants concernent la Matrice. Ils restent volontairement inactifs jusqu'au chapitre prévu afin de ne pas introduire cet élément trop tôt.
- Les huit événements historiques A5 conservent leurs IDs, leur mémoire et leurs points existants.

## Événements A5

- Fondations : premier bois, établi crafté, première pierre, charbon, fer brut, première cuisson et premier diamant.
- Monde vivant : premier apprivoisement, premier animal nourri et première reproduction animale.
- Mobilité : premier cheval monté et premier bateau utilisé.
- Village : premier échange villageois et première cloche réellement sonnée.
- Orientation : première carte et première boussole acquises.

## Détection robuste

- Les ressources sont reconnues par des identifiants vanilla exacts ; un objet moddé portant un nom ressemblant ne valide pas l'événement.
- La première cuisson accepte désormais toute vraie sortie de four, pas seulement un lingot.
- Le nourrissage est validé au tick suivant uniquement si l'animal a réellement accepté la nourriture et attribue l'action au joueur.
- La reproduction demande un enfant créé et un joueur responsable.
- Monter et descendre sont séparés : seule la montée sur un cheval ou un bateau compte.
- Le commerce utilise l'événement serveur de transaction réussie.
- Le clic sur une cloche ne suffit pas : son état de vibration est contrôlé avant validation.

## Réalisateur, HUD et DUO

- Chaque événement possède un texte acteur et un texte partenaire afin que le DUO soit prêt techniquement en même temps que le SOLO.
- Les événements proches peuvent être regroupés par famille : fondations, animaux, voyage ou village.
- Les points affichés distinguent toujours Âge et Civilisation.
- `/reivax dev on` masque les phrases narratives sans désactiver la détection, les points, la mémoire ou les diagnostics.
- `/reivax_a5` affiche les 16 cases A5 et rappelle que les 2 événements Matrice sont reportés.

## Validation technique

- Compilation locale complète avec Gradle 9.2.1 et NeoForge 21.1.248 : réussie.
- Auto-tests A1, A2, A3, A4 et A5 : réussis.
- L'auto-test A5 couvre les 16 IDs, les filtres exacts, les cas négatifs, les confirmations anti-faux-positifs, les textes DUO et la condensation.

