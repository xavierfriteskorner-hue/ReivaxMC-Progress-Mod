# Validation joueur unique — 0.11.0

Cette validation remplace un nouveau marathon B1/B2/B3. Durée visée : 10 à 15 minutes sur votre monde de test possédant déjà un Foyer.

## Préparation

1. Installer le JAR 0.11.0 côté client et serveur.
2. Faire une copie du monde.
3. Entrer `/reivax dev on`, puis `/reivax dev qa on` pour éviter les révélations longues dans le chat.
4. Entrer `/reivax dev chapter2 start`.

## Contrôles

1. La Borne s'ouvre sur **PISTES** et le texte est lisible. Cliquer **SUIVRE CETTE PISTE**.
2. Lancer `/reivax dev chapter2 next` : la première Fêlure est rejointe. Vérifier la grande Pierre de l'Écho sombre aux runes cyan/or, puis cliquer sa structure.
3. Relancer `next`, cliquer la seconde Fêlure. Vérifier que 2/2 suffit et que la troisième n'est pas obligatoire.
4. Relancer `next` : retour au Foyer et déclenchement du Recensement. Vérifier les Veilleurs immobiles et l'effet visible sur les deux écrans en DUO.
5. Relancer `next` pour condenser les trois rencontres, puis encore `next` pour rejoindre le Sanctuaire avec le Fragment.
6. Vérifier le Sanctuaire agrandi : deux monolithes verticaux à l'entrée, trois logements circulaires par monolithe, ailes et profondeurs déjà bâties.
7. Attendre une seconde : la Salle du Registre orientale s'ouvre. Relancer `next` si nécessaire pour être placé devant le Registre, puis cliquer le bloc.
8. Vérifier la récompense, le bref réveil de la Matrice derrière la salle occidentale et la fin de la Piste.
9. Se déconnecter/reconnecter : l'état doit rester achevé et chaque joueur doit posséder sa Boussole une seule fois.
10. Avec la Boussole : clic accroupi pour changer Foyer/Sanctuaire/Piste, clic normal pour obtenir la direction.

## Verdict attendu

- Aucun ancien progrès perdu.
- Aucun amas de fleurs ou blocs sous forme d'items autour du Sanctuaire.
- Piste et événements partagés, récompense personnelle non dupliquée.
- Troisième Fêlure réellement facultative.
- Aucune dépendance à MineColonies.
