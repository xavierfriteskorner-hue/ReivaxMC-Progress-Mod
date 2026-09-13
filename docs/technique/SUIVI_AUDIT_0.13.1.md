# Suivi de stabilisation après audit — 0.13.1

Ce document transforme l'audit global du 12 septembre 2026 en liste de contrôle. Il ne remplace ni la Bible narrative ni les procédures de test Minecraft.

## Baseline actuelle

- branche de référence : `main` ;
- commit du JAR courant : `f96cef2` ;
- fichier : `reivaxmc_progress-0.13.1-stabilisation.jar` ;
- SHA-256 : `DD3BAB545EC9AB4EDFBEF9276397812F677D246739907E225D92BE4BA25E05EB` ;
- compilation : réussie sous Java 21 / NeoForge 21.1.248 ;
- vérifications : 15 auto-tests autonomes réussis.

Le JAR reste une candidate locale tant que le parcours Minecraft n'a pas reçu le verdict humain demandé dans `docs/tests/VALIDATION_UNIQUE_0.13.1.md`.

## Corrigé depuis l'audit

- [x] une seule baseline de code sur `main` ;
- [x] coordinateur central des Chapitres I à IV ;
- [x] impossibilité de lancer deux chapitres principaux simultanément par le flux normal ;
- [x] profils DEV atomiques et parcours express global ;
- [x] commande `/reivax dev auditstate` et détection des étapes inconnues ;
- [x] schémas et migrations conservatrices du socle de campagne et des Chapitres I à IV ;
- [x] restauration des participants historiques des Chapitres III et IV quand leurs listes manquent ;
- [x] recherches de Veilleurs/Protecteur bornées au Sanctuaire ;
- [x] guide de validation renommé conformément à sa version réelle.

## À valider en jeu avant de figer la 0.13.1

- [ ] monde neuf : prologue puis Chapitres I à IV sans commande DEV ;
- [ ] parcours express complet avec `/reivax dev storytest start` puis `next` ;
- [ ] fermeture et réouverture du monde à chaque frontière de chapitre ;
- [ ] trois redémarrages près du Sanctuaire sans duplication de gardien ;
- [ ] migration d'une copie de monde 0.12.2 ou 0.13.0 ;
- [ ] parcours SOLO puis DUO du Chapitre IV ;
- [ ] confirmation visuelle des portes, objets uniques, objectifs et scènes.

## Bloquants avant la reprise du Chapitre V / de la 0.14.0

1. Définir la règle de groupe DUO lors d'une déconnexion et d'une reconnexion. Aucun choix automatique ne doit être inventé silencieusement.
2. Séparer le pourcentage narratif de l'Âge I du score et des points de Civilisation, afin que 100 % ne dépende que du Sceau final.
3. Ajouter au moins un test d'intégration avec monde/serveur couvrant sauvegarde, rechargement et enchaînement interchapitres.
4. Réconcilier la Bible, le catalogue maître, les JSON de Pistes et le code ; décider explicitement si les Pistes V2 sont du runtime ou seulement de la conception.
5. Réintégrer la candidate 0.14.0 uniquement depuis cette baseline stabilisée, puis tester ses héritages en SOLO et en DUO.

## Dette non bloquante

- découper progressivement les grandes classes héritées ;
- remplacer les `catch(Throwable)` silencieux par des erreurs ciblées ;
- rattacher les états d'interface au monde ou au joueur ;
- ajouter le Gradle Wrapper et simplifier la livraison GitHub ;
- clarifier les différents compteurs du Narrateur dans la documentation publique.

## Règle de reprise

Le prochain développement narratif ne doit pas commencer tant que les cinq bloquants ci-dessus ne sont pas soit résolus, soit tranchés explicitement. Les défauts purement décoratifs du Sanctuaire restent reportés : sa structure actuelle est considérée comme une base validable, pas comme une version artistique définitive.
