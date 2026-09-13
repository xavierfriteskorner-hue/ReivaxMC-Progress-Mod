# 0.13.1 — Correctif du parcours du chapitre IV

## Stabilisation globale de la campagne

- ajout d'un coordinateur unique pour les Chapitres I à IV ;
- blocage automatique du démarrage d'un chapitre si un autre chapitre principal est actif ;
- profils DEV atomiques : les chapitres précédents sont terminés proprement et les suivants sont verrouillés ;
- ajout de `/reivax dev auditstate` pour détecter immédiatement les chevauchements et prérequis incohérents ;
- ajout du parcours guidé `/reivax dev storytest start` jusqu'à la fin du Chapitre IV ;
- ajout d'un auto-test dédié aux invariants de campagne.

- La téléportation DEV au Sanctuaire ne réinitialise plus la campagne lorsque le chapitre IV est préparé.
- **Suivre cette Piste** en mode DEV conduit directement dans la Galerie des Absents.
- Les portes héritées sont ouvertes explicitement puisque les chapitres I à III sont sautés.
- Les anciens Veilleurs et Protecteurs sont supprimés du parcours du chapitre IV.
- La détection des gardiens est devenue directe et fiable ; un seul exemplaire peut exister par poste.
- Les doublons provenant d'un ancien monde défectueux sont supprimés automatiquement.
- La Boussole de Résonance est fournie et réglée sur la Piste actuelle.
- Les transitions Galerie → Archives → Galerie donnent désormais une consigne explicite.
