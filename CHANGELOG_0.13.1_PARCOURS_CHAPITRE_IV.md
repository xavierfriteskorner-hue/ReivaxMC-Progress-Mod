# 0.13.1 — Correctif du parcours du chapitre IV

## Consolidation des sauvegardes

- les quatre chapitres principaux possèdent maintenant un schéma de sauvegarde explicite ;
- le chargement répare les anciens états incomplets sans réinitialiser la progression narrative ;
- les participants SOLO/DUO des Chapitres III et IV sont récupérés depuis les traces déjà enregistrées lorsque l'ancienne liste manque ;
- les compteurs et masques invalides sont bornés afin d'éviter une piste impossible à terminer ;
- `/reivax dev auditstate` signale maintenant toute étape de chapitre inconnue ;
- les recherches et activations de gardiens sont limitées au volume du Sanctuaire au lieu de parcourir tout le monde ;
- un auto-test dédié rejoue les migrations depuis des données 0.9–0.13 minimales.

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
