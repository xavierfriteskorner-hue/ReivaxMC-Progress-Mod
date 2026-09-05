# REIVAX MC 0.8.8 — B3, socle final du Narrateur

Cette version ferme la grande phase de validation technique du Narrateur de l’Âge I. Le développement peut ensuite revenir au scénario, aux quêtes et à la progression réelle.

## Ajouts

- 9 événements spécialisés supplémentaires, IDs `A1-165` à `A1-173` : tir raté, Enderman regardé, feu au Foyer, premier toit couvert, panneau nommé, ravin, objet précieux sauvé, long voyage avec un compagnon et errance avec une boussole.
- Textes distincts pour l’acteur et son partenaire sur les 9 événements.
- Détecteurs prudents, compatibles SOLO et DUO, avec seuils anti-faux-positifs.
- Commande `/reivax_b3` pour afficher l’état détaillé du lot.

## Corrections

- `Aube survécue` et `Lever dehors` reconnaissent maintenant une vraie transition nuit → aube **ou** nuit → jour, y compris quand une commande saute les quelques ticks de l’aube.
- `Sommeil refusé` compte désormais les nuits réellement terminées éveillé, et non le simple passage au début de la nuit.
- Le sommeil confirmé annule correctement le compteur de la nuit en cours.

## Outils de test

- `/reivax dev tp foundation` téléporte à la Borne déjà enregistrée sans réinitialiser l’histoire.
- `/reivax dev test b1time` prépare et valide rapidement l’aube survécue ainsi que la troisième nuit sans sommeil.
- `/reivax dev test return20` simule l’attente de vingt minutes puis ramène à la Borne enregistrée.
- `/reivax dev on` conserve le mode QA anti-spoil.

## État final du socle

- 97 événements sur 100 sont équipés de détecteurs robustes.
- 2 événements liés à la Matrice restent volontairement réservés au chapitre où elle sera introduite.
- L’échange avec un villageois reste actif pour Minecraft Vanilla, mais sa validation est reportée dans le modpack actuel car MineColonies supprime les villageois Vanilla classiques.
- Tous les auto-tests A1, A2, A3, A4, A5, B1, B2 et B3 passent, ainsi que la compilation complète NeoForge.
