package fr.reivaxmc.progress.story;

import net.minecraft.server.level.ServerPlayer;

/**
 * Un fait observé, normalisé. L'Observer n'écrit jamais de texte ni ne donne de récompense :
 * il constate simplement « ceci vient de se produire, causé par ce joueur ».
 *
 * @param factKey clé stable du fait (ex. "mine_wood")
 * @param actor   le joueur à l'origine du fait
 */
public record StoryFact(String factKey, ServerPlayer actor) {}
