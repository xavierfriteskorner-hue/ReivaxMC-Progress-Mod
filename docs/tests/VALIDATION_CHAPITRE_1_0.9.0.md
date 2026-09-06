# Validation condensée — Chapitre I, REIVAX MC 0.9.0

Ce contrôle remplace toute nouvelle série de dizaines d’événements. Il dure environ cinq minutes et masque les véritables dialogues.

## Préparation

1. Installer `reivaxmc_progress-0.9.0.jar` dans un monde de test.
2. Exécuter `/reivax dev on`. Le mode QA anti-spoil est activé automatiquement.
3. Exécuter `/reivax dev chapter1 start`.
   - Si ce monde ne possède aucune Borne, une Borne de test apparaît quatre blocs devant le joueur.
   - Le prologue Trace → Sanctuaire → Matrice est ignoré.

## Parcours express obligatoire

1. Exécuter `/reivax dev chapter1 next`.
   - Les trois repères de Foyer sont validés.
   - Un lingot de fer, un livre et un pain sont fournis.
2. Choisir librement un seul de ces objets, le tenir en main et faire clic droit sur la Borne :
   - fer = Protéger ;
   - livre = Comprendre ;
   - pain = Partager.
3. Exécuter `/reivax dev chapter1 next` pour rejoindre l’Écho, puis faire clic droit sur la pierre placée devant le joueur.
4. Exécuter de nouveau `/reivax dev chapter1 next` : le Fragment est fourni et le joueur revient au Foyer.
5. Attendre une seconde. Le retour est reconnu automatiquement et le Fragment doit rester dans l’inventaire : la Borne ne l’analyse pas.
6. Soit combattre normalement le ou les Témoins, soit exécuter `/reivax dev chapter1 next` pour résoudre immédiatement le combat.
7. Exécuter `/reivax dev chapter1 status` et vérifier `COMPLETE`.

## Trois vérifications finales

- Le Fragment est toujours conservé, et la récompense contient une Pierre des Absents ainsi qu’un paquet correspondant au choix effectué.
- Quitter le monde, revenir, puis vérifier que l’objectif reste postérieur au Chapitre I et que la récompense n’est pas redonnée.
- En DUO ultérieurement : le second joueur doit voir le même état de chapitre et recevoir sa propre récompense s’il ne l’a jamais réclamée.

Si ces trois points sont corrects, le chapitre est validé. Aucun nouveau test du socle B1/B2/B3 n’est demandé.
