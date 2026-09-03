# REIVAX MC 0.8.1a — checklist SOLO du sous-lot A1 corrigé

## Préparation

- [ ] Utiliser de préférence un nouveau monde SOLO de test, ou une sauvegarde de test copiée.
- [ ] Installer la version 0.8.1a et vérifier qu’elle démarre sans erreur.
- [ ] Ne pas connecter Laeriss : le DUO est volontairement hors périmètre de ce lot.
- [ ] Exécuter `/reivax_a1` et noter le compteur initial. Sur un monde neuf, l’objectif est `0/3`.

> Une sauvegarde déjà avancée peut avoir dépassé certains jalons. Pour tester les trois transitions de bout en bout, employer un monde neuf.

## Test nominal des trois signaux actuels

Pour chaque étape, vérifier dans cet ordre : transition de l’histoire, phrase de La Voix dans le HUD, progression/points éventuels, puis compteur `/reivax_a1`.

- [ ] Provoquer la première Résonance : `A1-096` doit être livré une seule fois, avec +20 points d’Âge et +5 civilisation.
- [ ] Insérer les Sceaux dans les Stèles du seuil du Sanctuaire : `A1-097` doit être livré une seule fois, avec +25 points d’Âge et +5 civilisation.
- [ ] Valider le premier foyer : `A1-051` doit être livré une seule fois, avec +25 points d’Âge, +5 civilisation et la Pierre du Premier Foyer.
- [ ] À la fin, `/reivax_a1` doit afficher `3/3` et préciser que la Matrice est reportée.
- [ ] Sur un monde neuf ayant uniquement validé ces trois événements, le gain cumulé attendu est +70 points d’Âge et +15 civilisation.

Les événements `A1-066` (Matrice installée) et `A1-099` (Matrice reconnue) ne doivent rien déclencher dans cette version. Ils restent réservés au futur chapitre de la Matrice.

## Anti-doublon et persistance

- [ ] Après chaque signal, attendre la livraison HUD avant de quitter le monde.
- [ ] Recharger le monde : le compteur `/reivax_a1` doit rester identique.
- [ ] Répéter une action déjà validée : aucune seconde phrase de première fois, aucun second gain, aucune seconde récompense.
- [ ] Vérifier avec `/reivax_brain` que la mémoire n’augmente qu’après une livraison acceptée.
- [ ] Quitter complètement le jeu, le relancer et recharger le monde : le compteur doit encore rester identique.

## Régression 0.8.0

- [ ] Vérifier un ancien pilote simple, par exemple premier bois ou premier charbon.
- [ ] Vérifier que les variantes contextuelles et le HUD adaptatif restent fonctionnels.
- [ ] Vérifier que points et récompenses ne sont distribués qu’une fois.
- [ ] Vérifier que la mémoire du Brain survit au rechargement.

## Critères d’arrêt

Le test est en échec si une transition ne produit aucun passage par le HUD, si une phrase/prime se répète, si le compteur change au simple rechargement, ou si une erreur serveur apparaît. Conserver alors `latest.log` et noter l’étape exacte. Le test DUO reste reporté jusqu’à la disponibilité de Laeriss.
