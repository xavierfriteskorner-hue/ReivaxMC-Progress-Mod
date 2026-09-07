# Migration 0.10.2 → 0.11.0

- Les mondes existants, la Borne, le Conseil, le Journal, les points et le Chapitre I sont conservés.
- Le Sanctuaire 0.10.2 est reconnu comme ancien par son marqueur ; il est remplacé une seule fois par la grande architecture 0.11.0.
- Le remplacement emploie `setBlock` et ne casse pas les blocs sous forme d'objets : fleurs, herbes et décor ne jonchent pas le sol.
- La Salle du Registre reste ouverte si sa Concordance a déjà été enregistrée.
- La Piste du Chapitre II possède sa propre sauvegarde versionnée.
- Les récompenses principales et personnelles sont idempotentes.
- Le protocole réseau passe à la version 15 : client et serveur doivent utiliser exactement le même JAR 0.11.0.

Il est toujours recommandé de conserver une sauvegarde du monde avant remplacement du JAR, comme pour toute mise à jour structurelle importante.
