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

Le socle du Narrateur a été stabilisé en **0.8.8** avec 97/100 événements robustes. La version **0.10.2** ouvre la campagne jouable avec le Chapitre I, **Le Foyer emprunté**, et pose ses systèmes durables : Conseil visuel synchronisé SOLO/DUO, frontière de Résonance renforcée, Journal intime hérité, points de Civilisation dépensables et Sanctuaire complet à salles scellées. L'interface de la Borne reste désormais lisible quelle que soit l'échelle de GUI.

Les événements du Narrateur ne sont pas du contenu jetable : ils forment le vocabulaire réutilisable de la campagne, des missions, des réactions de la Voix et des futurs moments de civilisation.

Pour valider le lot sans rejouer le prologue, utilisez `/reivax dev on`, puis `/reivax dev chapter1 start`. La commande `/reivax dev chapter1 next` permet ensuite de rejoindre chaque contrôle important. Une seule procédure condensée se trouve dans `docs/tests/VALIDATION_CAMPAGNE_0.10.0.md`.

Règle d'or : *on construit le moteur une fois, ensuite on nourrit le monde.*
