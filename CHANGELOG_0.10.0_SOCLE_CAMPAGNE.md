# REIVAX MC 0.10.0 — Socle de campagne

Cette version termine proprement le travail commencé en 0.9.0 avant d'ouvrir les chapitres suivants.

## Chapitre I et SOLO/DUO

- Le Fragment trouvé dans « Le Foyer emprunté » reste dans l'inventaire : la Borne ne l'analyse et ne le consomme jamais.
- Le choix de doctrine passe désormais par le Conseil du Foyer.
- En SOLO, un choix valide est confirmé immédiatement.
- En DUO, les deux joueurs doivent être regroupés près de la Borne et voter chacun sur leur écran.
- Deux votes différents déclenchent une Dissonance commune ; aucun hôte, premier clic ou tirage aléatoire ne tranche.
- Une déconnexion conserve le Conseil en attente et empêche toute décision derrière le partenaire.

## Foyer vivant

- Une frontière diégétique remplace toute ligne permanente : arc de Résonance à proximité, impulsion à la traversée, contour révélé temporairement par la Borne.
- Les points d'Âge restent cumulatifs et non dépensables.
- Les points de Civilisation conservent un total historique et gagnent un solde dépensable.
- Deux premières décisions de Borne sont fonctionnelles : Lisière accordée et Ancrage étendu (rayon 96 → 128).

## Journal intime hérité

- Le Livre ancien devient un Journal dont le propriétaire initial reste inconnu.
- Pages héritées ambiguës, chronique automatique commune, notes personnelles privées.
- Chaque joueur peut conserver une note pour lui ou la partager volontairement avec le Foyer.
- En DUO, les deux objets lisent la même chronique tandis que les pages intimes restent séparées.

## Sanctuaire durable

- Les deux ailes des futurs chapitres sont construites avec le Sanctuaire initial puis maintenues derrière des sceaux de Concordance.
- La Matrice existe physiquement derrière le sceau occidental ; elle reste visible mais inaccessible.
- La zone protégée couvre désormais l'intégralité des ailes.
- La pose structurelle n'envoie plus de mises à jour destructrices à la végétation voisine, ce qui évite les milliers de fleurs et d'herbes abandonnées au sol.
- Les mondes de test existants reçoivent une migration unique des ailes manquantes, sans reconstruire le corps central.

## Validation

- Tous les auto-tests A1 à B3 et Chapitre I passent.
- Le test Chapitre I vérifie désormais explicitement l'attente DUO, la Dissonance et le consensus.
- Une seule validation en jeu est demandée dans `docs/tests/VALIDATION_CAMPAGNE_0.10.0.md`.
