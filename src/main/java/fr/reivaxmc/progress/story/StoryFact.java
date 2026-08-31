package fr.reivaxmc.progress.story;

import net.minecraft.server.level.ServerPlayer;

import java.util.Set;

/**
 * Un fait observé, normalisé. L'Observer constate ; il ne raconte rien et ne récompense rien.
 *
 * @param type    le genre de fait ("block_break", "craft", "smelt", "place", "kill", "death"…)
 * @param actor   le joueur à l'origine du fait
 * @param subject l'objet du fait (id de bloc / d'objet / d'entité, ex. "minecraft:stone"), ou null
 * @param tags    étiquettes utiles au fait (ex. "minecraft:logs"), jamais null
 */
public record StoryFact(String type, ServerPlayer actor, String subject, Set<String> tags) {

    public StoryFact {
        if (tags == null) {
            tags = Set.of();
        }
    }
}
