# REIVAX MC 0.8.2 — checklist SOLO du sous-lot A2

## Préparation rapide

- [ ] Utiliser de préférence un monde de test neuf ou une copie de sauvegarde.
- [ ] Démarrer l'histoire normalement : les événements du Narrateur restent protégés par le démarrage de la campagne.
- [ ] Exécuter `/reivax_a2`. Sur un monde neuf, le compteur attendu est `0/3`.
- [ ] Entre deux actions, attendre que la phrase du HUD soit livrée. La file narrative peut demander jusqu'à environ 10 secondes.

## Trois tests rapides

1. **Premier bâton — A1-026**
   - Exécuter `/give @s minecraft:stick 1`.
   - Attendre environ une seconde, puis la livraison du HUD.
   - Vérifier la phrase de La Voix, `+2` points d'Âge et `/reivax_a2` à `1/3`.

2. **Premier coffre posé — A1-027**
   - Exécuter `/give @s minecraft:chest 1`.
   - Poser le coffre au sol : l'avoir seulement dans l'inventaire ne suffit pas.
   - Vérifier la phrase de La Voix, `+5` points d'Âge, `+1` civilisation et `/reivax_a2` à `2/3`.

3. **Premier lit posé — A1-029**
   - Exécuter `/give @s minecraft:red_bed 1`.
   - Poser le lit au sol : dormir n'est pas nécessaire.
   - Vérifier la phrase de La Voix, `+5` points d'Âge, `+1` civilisation et `/reivax_a2` à `3/3`.

Le gain cumulé attendu pour les trois événements est **+12 points d'Âge et +2 civilisation**.

## Anti-doublon et persistance

- [ ] Donner un second bâton, puis poser un second coffre et un second lit : aucun nouveau texte et aucun nouveau point ne doivent être accordés.
- [ ] Exécuter `/reivax_brain` : la mémoire ne doit augmenter que lors de la première livraison acceptée de chaque événement.
- [ ] Quitter le monde puis le recharger : `/reivax_a2` doit rester à `3/3`.
- [ ] Fermer complètement Minecraft, le relancer et recharger le monde : `/reivax_a2` doit encore rester à `3/3`.
- [ ] Exécuter `/reivax_a1` pour vérifier que le sous-lot précédent reste intact.

## Critères d'arrêt

Le test échoue si une action ne produit rien, si un événement est accordé deux fois, si les points changent au simple rechargement ou si une erreur serveur apparaît. Conserver alors `latest.log` et noter l'étape exacte.

