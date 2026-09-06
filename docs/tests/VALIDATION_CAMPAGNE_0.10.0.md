# Validation unique — REIVAX MC 0.10.0

Ce contrôle remplace les validations séparées du moteur, du Chapitre I et des nouveaux systèmes. Les vrais dialogues restent masqués en mode DEV.

## Préparation

1. Installer `reivaxmc_progress-0.10.0.jar`.
2. Dans un monde de test, exécuter `/reivax dev on` puis `/reivax dev chapter1 start`.
3. Exécuter `/reivax dev chapter1 next` : les trois repères sont validés et les objets de doctrine sont fournis.

## Contrôle condensé SOLO

1. Présenter FER, LIVRE ou PAIN à la Borne. Le choix doit être confirmé immédiatement en SOLO.
2. Utiliser `/reivax dev chapter1 next`, examiner l'Écho, puis réutiliser `next` pour revenir au Foyer.
3. Vérifier que le Fragment reste dans l'inventaire, puis résoudre le combat avec `next`.
4. Ouvrir la Borne : son contour apparaît brièvement. Se rendre près de la lisière, puis la franchir dans les deux sens ; les deux messages de traversée doivent apparaître.
5. Onglet OPTIONS : vérifier le total et le solde de Civilisation. Acheter Lisière accordée, puis Ancrage étendu si le solde le permet ; le rayon affiché passe à 128 blocs.
6. Obtenir le Journal si nécessaire avec `/give @s reivaxmc_progress:destiny_book`, puis l'ouvrir. Vérifier les trois onglets, enregistrer une note privée puis une note partagée.
7. Quitter le monde, revenir et vérifier : chapitre terminé, Fragment conservé, achats conservés, notes conservées, récompense non redonnée.

## Contrôle DUO ultérieur — seulement quatre minutes

1. Dans un monde où le Chapitre I attend le choix, regrouper les deux joueurs près de la Borne.
2. Joueur A présente FER ; les deux écrans doivent annoncer le Conseil en attente.
3. Joueur B présente LIVRE ; les deux écrans doivent annoncer la Dissonance et rien ne progresse.
4. Joueur B présente FER ; le consensus est validé sur les deux écrans et l'Écho apparaît.
5. Chacun ouvre sa copie du Journal : la note partagée est commune, la note privée de l'autre reste invisible.

## Sanctuaire

Sur un monde neuf, observer seulement ces trois points lors du passage normal : les ailes sont déjà physiquement présentes derrière leurs sceaux, la Matrice est aperçue derrière le sceau occidental, et aucune pluie d'objets végétaux n'apparaît pendant la génération.

Si ces contrôles sont corrects, aucun nouveau test du socle A/B n'est requis pour les prochains chapitres.
