package fr.reivaxmc.progress.story;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.Map;

/**
 * Le metteur en scène. Il décide SI la Voix doit parler.
 * Ici : il relie un fait observé à une intervention du narrateur, et ne la joue qu'une seule
 * fois par monde (grâce à la mémoire persistante). Aucune récompense n'est dupliquée.
 */
public final class Director {

    /** Association fait observé -> intervention pilote. Le câblage est ici ; le texte, dans les données. */
    private static final Map<String, String> FACT_TO_EVENT = Map.of(
            "mine_wood", "A1-001",
            "mine_stone", "A1-005",
            "mine_coal", "A1-007",
            "mine_iron", "A1-009"
    );

    public static void handle(StoryFact fact) {
        ServerPlayer actor = fact.actor();
        if (actor == null) {
            return;
        }
        MinecraftServer server = actor.getServer();
        if (server == null) {
            return;
        }
        String eventId = FACT_TO_EVENT.get(fact.factKey());
        if (eventId == null) {
            return;
        }
        PilotEvent event = NarratorData.get(eventId);
        if (event == null) {
            return;
        }
        CampaignSavedData memory = CampaignSavedData.get(server);
        // markFired renvoie true seulement la première fois : la Voix ne se répète jamais.
        if (memory.markFired(eventId)) {
            Voix.speak(actor, event);
        }
    }

    private Director() {}
}
