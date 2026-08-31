package fr.reivaxmc.progress.story;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

/**
 * Le metteur en scène. Il décide SI la Voix doit parler.
 * Il compare le fait observé aux déclencheurs déclarés dans les données, et ne joue une
 * intervention qu'une seule fois par monde. Une seule intervention par fait (anti-spam).
 */
public final class Director {

    public static void handle(StoryFact fact) {
        ServerPlayer actor = fact.actor();
        if (actor == null) {
            return;
        }
        MinecraftServer server = actor.getServer();
        if (server == null) {
            return;
        }
        CampaignSavedData memory = CampaignSavedData.get(server);

        for (PilotEvent event : NarratorData.all()) {
            Trigger trigger = event.trigger();
            if (trigger == null || !trigger.matches(fact)) {
                continue;
            }
            if (memory.hasFired(event.id())) {
                continue; // déjà vécu : la Voix ne se répète pas
            }
            memory.markFired(event.id());
            memory.addPoints(event.agePoints(), event.civScore());
            Voix.speak(actor, event);
            Reward.give(actor, event);
            return; // une seule intervention par fait
        }
    }

    private Director() {}
}
