# Implémentation 0.12.1 — Sanctuaire canonique

## Source d'autorité

`C110SanctuaryArchitecture` est désormais le seul constructeur appelé par la boucle narrative. `F94SanctuaryShell` reste un adaptateur de compatibilité, mais la boucle ne l'invoque plus en parallèle et l'ancien `buildSanctuary` n'est plus utilisé.

Le marqueur physique de version est placé en `(0,+16,-3)`. Sa nouvelle position force une migration unique des constructions 0.12.0 puis bloque toute reconstruction répétée.

## Plan invariant

- façade et première porte : `z+22` ;
- hall des deux Veilleurs : autour de `z+14` ;
- seconde porte : `z+7` ;
- porte intérieure : `z-7` ;
- Protecteur : `z-15` ;
- Borne de Fondation : `z-18` ;
- monolithes : `x-9/x+9, z+36` ;
- Registre oriental : `x+27, z-16` ;
- Matrice occidentale : `x-29, z-16` ;
- Galerie : `z-29` à `z-44`.

Les portes narratives ne sont pas déplacées : la nouvelle enveloppe est construite autour de leurs coordonnées historiques afin de préserver les sauvegardes, les interactions et les Pistes déjà codées.

## Persistance et migration

`CampaignSavedData` enregistre `SanctuaryLocated` et les coordonnées `SanctuaryX/Y/Z`. Pour une partie qui possède déjà `F8_SANCTUARY_BUILT`, l'ancien calcul est rejoué une dernière fois afin de retrouver le bâtiment existant. Une nouvelle partie utilise le contrôle d'emprise complet.

## Gardiens

Les anciens drapeaux `F8_SPAWNED_*` ne contrôlent plus la présence. Chaque poste est vérifié par son tag propre, avec une temporisation de cinq secondes. Un gardien vaincu ne revient jamais ; un gardien supprimé par un outil DEV ou perdu au chargement est recréé.

Les trois entités décoratives portant `reivax_sanctuary_presence` sont retirées pendant la migration. Seuls les six Veilleurs et le Protecteur du parcours réel subsistent.

## Sécurité du monde

Tous les blocs sont posés avec le drapeau `18`. Le nettoyage des objets est limité à l'emprise et aux entités très récentes. Le monument possède un socle continu, une voûte à double peau et des raccords de terrain limités à sept blocs autour du périmètre.

