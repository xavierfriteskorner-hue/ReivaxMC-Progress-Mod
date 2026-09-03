# Tests DUO différés — REIVAX MC 0.8.3 / lot A3

Le code DUO est développé en même temps que le SOLO. Cette fiche sera utilisée lorsque Laeriss sera disponible ; elle ne bloque pas la validation SOLO.

## Vérifications à faire à deux

1. Les deux joueurs entrent dans le même monde et l'histoire est démarrée.
2. Le joueur A obtient un des 16 objets ou équipements A3.
3. Le joueur A reçoit la phrase qui lui est adressée ; le joueur B reçoit la variante partenaire qui nomme le joueur A.
4. Les points sont ajoutés une seule fois à la progression commune.
5. Le joueur B obtient ensuite le même objet : aucun doublon, aucun point supplémentaire.
6. Refaire avec plusieurs objets rapides : seuls des événements compatibles du même acteur peuvent être regroupés.
7. Reconnexion des deux joueurs puis redémarrage complet du serveur : `/reivax_a3` reste inchangé.

## Résultat attendu

- Une progression partagée et persistante.
- Des textes différents pour l'acteur et son partenaire.
- Aucun double comptage lorsque les deux joueurs accomplissent le même événement.
- Aucun mélange entre les actions de deux joueurs dans un même encadré condensé.

