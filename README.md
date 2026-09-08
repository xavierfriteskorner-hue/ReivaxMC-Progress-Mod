# ReivaxMC Progress

Campagne coopérative narrative pour **Minecraft 1.21.1 / NeoForge 21.1.248 / Java 21**.
Deux joueurs bâtissent une civilisation à travers 7 Âges pendant qu'un mystère ancien — les Origines — réagit à leurs choix. *« La Voix ne connaît pas votre avenir. Elle se souvient de quelque chose que vous avez oublié. »*

---

## À quoi sert ce dépôt

C'est la **reconstruction propre** du mod (Alpha 18), repartie de la conception validée et des acquis de la version précédente. Le code est écrit ici, puis **compilé automatiquement dans le cloud** (GitHub Actions) à chaque envoi. Le fichier du mod (`.jar`) prêt à jouer se télécharge dans l'onglet **Actions**.

## Comment récupérer le mod compilé

1. Onglet **Actions** du dépôt → dernier build vert ✅
2. Section **Artifacts** en bas → télécharger **ReivaxMC-Progress-mod**
3. Dézipper, mettre le `.jar` dans le dossier `mods` de l'instance Minecraft
4. Tester, puis remonter le `latest.log` + une capture en cas de souci

## Principe de développement

Le socle du Narrateur a été stabilisé en **0.8.8** avec 97/100 événements robustes. La version **0.12.0** livre trois chapitres jouables, dont **Les Noms retirés**, une refonte semi-enfouie du Sanctuaire, le Registre interactif, la Galerie de Concordance, des décisions de mémoire synchronisées SOLO/DUO et cinq décisions de Civilisation réellement dépensables.

Les événements du Narrateur ne sont pas du contenu jetable : ils forment le vocabulaire réutilisable de la campagne, des Pistes, des réactions de la Voix et des futurs moments de civilisation.

Pour valider le gros lot sans rejouer le prologue, utilisez `/reivax dev on`, puis `/reivax dev chapter3 start`. La commande `/reivax dev chapter3 next` rejoint chaque contrôle important. La procédure condensée se trouve dans `docs/tests/VALIDATION_UNIQUE_0.12.0.md`.

Règle d'or : *on construit le moteur une fois, ensuite on nourrit le monde.*
