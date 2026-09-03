# REIVAX MC 0.8.2 — checklist DUO du sous-lot A2

**Statut : préparée dans le code, test en jeu reporté jusqu'à la disponibilité de Laeriss.**

## Principe

Les trois événements A2 possèdent déjà deux textes : un pour la personne qui agit et un pour son partenaire. Le routage DUO existant est réutilisé ; aucune seconde version du système ne sera nécessaire plus tard.

## Test à effectuer avec Laeriss

- [ ] Les deux joueurs sont connectés avant la première action.
- [ ] Vérifier d'abord `/reivax_a2` sur un monde de test neuf ou réinitialisé : `0/3`.
- [ ] Le joueur A obtient son premier bâton : il reçoit le texte acteur ; le joueur B reçoit le texte partenaire avec le nom du joueur A.
- [ ] Le joueur A pose son premier coffre : même vérification acteur/partenaire.
- [ ] Le joueur A pose son premier lit : même vérification acteur/partenaire.
- [ ] Le compteur atteint `3/3` une seule fois pour le monde partagé et le gain cumulé reste `+12` Âge, `+2` civilisation.
- [ ] Répéter les trois actions : aucun doublon pour aucun joueur.
- [ ] Quitter puis rejoindre le monde : le compteur reste `3/3`.
- [ ] Sur une autre copie de test, inverser les rôles afin que Laeriss déclenche les trois événements.

## Résultat attendu

Le DUO ajoute uniquement une présentation adaptée au partenaire. Il ne crée ni double progression, ni double mémoire, ni double récompense.

