# REIVAX MC 0.8.6 — Lot B1 monde, survie et exploration

## Résumé

- Activation ou formalisation de 24 événements de classe B.
- Conservation des identifiants historiques de la culture, de l'aube, du village, du demi-cœur et de la foudre.
- Ajout de 19 événements A1-135 à A1-153.
- Détecteurs communs SOLO + DUO, textes partenaires prêts pour chaque événement.
- Passage de 50 à 74 événements robustes actifs sur les 76 spécifiés.
- Les deux événements Matrice restent volontairement désactivés jusqu'au chapitre narratif prévu.

## Détection

- Échantillonnage du monde limité à une fois par seconde et uniquement par joueur connecté.
- Distance cumulée persistante ; les téléportations supérieures à 32 blocs par échantillon sont ignorées.
- Biomes, hauteur, profondeur, village, météo, faim et inventaire détectés sans scan global du monde.
- Sommeil confirmé au réveil à l'aube ; refus de dormir confirmé après trois nuits distinctes.
- Consommation confirmée par l'événement de fin d'utilisation de l'objet.
- Objet précieux confirmé par un drop volontaire non annulé.
- Nom d'animal confirmé au tick suivant l'utilisation de l'étiquette.
- Compagnon blessé limité aux animaux apprivoisés dont le propriétaire est un joueur REIVAX connecté.

## Brain, Réalisateur et HUD

- Tous les événements passent par la chaîne Détecteur → Brain → Réalisateur → HUD.
- Groupes de condensation ajoutés pour exploration, survie, nourriture, profondeur, météo, inventaire et compagnons.
- Récompenses affichées explicitement en points d'Âge et de Civilisation selon le catalogue.
- Nouvelle commande de diagnostic `/reivax_b1` avec détail 24/24 et rappel 74/76.
- `/reivax17_reset` efface aussi les compteurs techniques B1 afin de rendre les tests reproductibles.

## Validation automatique

- `checkB1Signals` couvre les 24 identifiants, les cas positifs, les principaux faux positifs, les textes DUO et la condensation.
- `clean check build` réussi avec les auto-tests A1, A2, A3, A4, A5 et B1.

## Compatibilité A5 conservée

- Validation utilisateur A5 : 15/16 en jeu et persistance confirmée après reconnexion.
- Échange avec villageois : test reporté, car MineColonies supprime les villageois vanilla classiques du modpack utilisé. L'événement reste disponible pour les mondes compatibles et n'est pas accordé artificiellement.
