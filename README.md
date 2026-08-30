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

On avance par **tranches courtes et testables**. On ne casse jamais ce qui marche sans l'avoir remplacé. Règle d'or : *on construit le moteur une fois, ensuite on nourrit le monde.*
