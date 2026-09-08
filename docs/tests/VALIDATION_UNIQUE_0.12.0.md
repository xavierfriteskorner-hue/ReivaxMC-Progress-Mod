# Validation unique 0.12.0

Cette validation remplace toute répétition de B1/B2. Utiliser une copie du monde déjà arrivée après `La Dette du Foyer`.

## Préparation

1. Installer uniquement le JAR 0.12.0 et conserver une sauvegarde du monde.
2. Entrer dans le monde : le Sanctuaire 0.11.0 doit être reconstruit une seule fois et les anciennes Fêlures doivent être déplacées loin du Foyer sans perdre leur validation. Attendre la fin du chargement.
3. Vérifier de l'extérieur : aucune grande toiture plate, entrée dégagée, deux monolithes visibles sur le parvis, un creux circulaire sur chacun, aucun arbre/bloc suspendu dans le bâtiment.
4. Vérifier à l'intérieur : aucun objet de fleur/graine au sol ; deux Veilleurs silencieux dans la nef ; un Protecteur dormant dans l'aile occidentale ; aucune décoration devant une porte future.

## Corrections du chapitre II — 3 minutes

1. Activer `/reivax dev on`, puis `/reivax dev chapter2 start`.
2. Avec `/reivax dev chapter2 next`, contrôler que les Fêlures sont de vraies destinations éloignées et que la Pierre de l'Écho porte une grande inscription lumineuse.
3. Continuer jusqu'au Recensement : le HUD indique régulièrement direction et distance du prochain Veilleur. Trois approches doivent suffire.
4. Continuer : le joueur arrive devant la porte orientale et non dans sa collision.
5. Cliquer le Registre : sa face doit représenter clairement un livre lumineux ; l'impulsion de la Matrice doit traverser la nef, produire son et particules autour du joueur.

## Chapitre III — 12 à 15 minutes

1. Entrer `/reivax dev chapter3 start`. La Borne s'ouvre sur `PISTES` et affiche `Les Noms retirés`.
2. Cliquer `SUIVRE CETTE PISTE`, aller au Registre et l'ouvrir. La huitième ligne doit être grande et lisible. En DUO, comparer les deux écrans : les noms lus diffèrent.
3. Chaque joueur clique `JE L'AI VUE`. Le premier attend le second ; aucune validation solitaire en DUO.
4. Utiliser `/reivax dev chapter3 next` pour rejoindre la Procession. Approcher naturellement : six silhouettes apparaissent, puis disparaissent l'une après l'autre. Elles ne combattent pas.
5. `next` rejoint la Maison numérotée. Cliquer les quatre traces : couvert, mesure, cache, lit. Le compteur atteint 4/4 quel que soit l'ordre.
6. Facultatif : cliquer la marque sombre en hauteur sur le mur arrière. Le texte cru apparaît et donne 25 Âge / 8 Civilisation une seule fois.
7. `next` revient devant le Registre. Cliquer : la Galerie s'ouvre sans téléporter dans un mur.
8. Activer les deux pupitres. SOLO : deux clics en moins de 25 s. DUO : un joueur différent sur chaque pupitre en moins de 12 s. L'échec doit rester recommençable.
9. Ouvrir le Registre, onglet `MÉMOIRE`, puis choisir. En DUO, voter différemment : rien ne se tranche. Revoter de manière identique après réinitialisation du test ou avec la même option lors du premier passage.
10. Vérifier la récompense 180 Âge / 40 Civilisation, la Chronique commune et la note personnelle du Journal.

## Persistance — 2 minutes

1. Quitter puis rouvrir le monde à deux étapes différentes.
2. Vérifier que la Piste, les traces, lectures, portes, décision, récompenses et Sanctuaire restent identiques.
3. Vérifier que le Sanctuaire ne se reconstruit pas à chaque connexion et qu'aucun objet nouveau n'apparaît au sol.

## Commandes utiles

- `/reivax dev chapter3 status` : état détaillé.
- `/reivax dev chapter3 next` : prochain point de contrôle sûr.
- `/reivax dev chapter3 reset` : réinitialise uniquement le chapitre III.
- La Boussole, mode `PISTE ACTUELLE`, guide vers la Procession, la Maison ou le Registre selon l'étape.
