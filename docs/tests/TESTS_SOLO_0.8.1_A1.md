# REIVAX MC 0.8.1 — checklist SOLO du sous-lot A1

## Préparation

- [ ] Utiliser de préférence un nouveau monde SOLO de test, ou une sauvegarde de test copiée.
- [ ] Installer la version 0.8.1 et vérifier qu’elle démarre sans erreur.
- [ ] Ne pas connecter Laeriss : le DUO est volontairement hors périmètre de ce lot.
- [ ] Exécuter `/reivax_a1` et noter le compteur initial. Sur un monde neuf, l’objectif est `0/5`.

> Une sauvegarde déjà avancée peut contenir certains identifiants A1 mémorisés par la 0.8.0. Pour tester les cinq transitions de bout en bout, employer un monde neuf.

## Test nominal des cinq signaux

Pour chaque étape, vérifier dans cet ordre : transition de l’histoire, phrase de La Voix dans le HUD, progression/points éventuels, puis compteur `/reivax_a1`.

- [ ] Provoquer la première Résonance : `A1-096` doit être livré une seule fois, avec +20 points d’Âge et +5 civilisation.
- [ ] Découvrir ou activer la Stèle : `A1-097` doit être livré une seule fois, avec +25 points d’Âge et +5 civilisation.
- [ ] Atteindre la reconnaissance de la Matrice : `A1-099` doit être livré une seule fois, avec +25 points d’Âge et +5 civilisation.
- [ ] Valider le premier foyer : `A1-051` doit être livré une seule fois, avec +25 points d’Âge, +5 civilisation et la Pierre du Premier Foyer.
- [ ] Installer la Matrice : `A1-066` doit être livré une seule fois, avec +20 points d’Âge, +4 civilisation et deux Veilleuses de la Matrice.
- [ ] À la fin, `/reivax_a1` doit afficher `5/5`.
- [ ] Sur un monde neuf ayant uniquement validé ces cinq événements, le gain cumulé attendu est +115 points d’Âge et +24 civilisation.

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
