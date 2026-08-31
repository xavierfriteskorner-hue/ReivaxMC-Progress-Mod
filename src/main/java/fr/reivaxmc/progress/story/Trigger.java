package fr.reivaxmc.progress.story;

/**
 * La condition qui relie un fait observé à une intervention.
 * Décrite dans les données, jamais en dur.
 *
 * @param type  le type de fait attendu ("block_break", "craft"…)
 * @param key   la dimension à comparer : "block", "item", "entity", "tag" — ou null (type seul, ex. "death")
 * @param value la valeur attendue (ex. "minecraft:stone"), ou null
 */
public record Trigger(String type, String key, String value) {

    public boolean matches(StoryFact fact) {
        if (!type.equals(fact.type())) {
            return false;
        }
        if (key == null || value == null) {
            return true; // condition sur le type seul
        }
        if ("tag".equals(key)) {
            return fact.tags().contains(value);
        }
        // "block" / "item" / "entity" : on compare au sujet du fait
        return value.equals(fact.subject());
    }
}
