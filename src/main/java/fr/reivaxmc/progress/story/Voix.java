package fr.reivaxmc.progress.story;

import fr.reivaxmc.progress.network.NarrationPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * La Voix des Origines : le rendu final d'une intervention, envoyé au client sous forme de panneau.
 * « Monde commun, action attribuée, réaction partagée » — l'acteur reçoit sa phrase,
 * les autres joueurs en reçoivent une variante à la troisième personne.
 */
public final class Voix {

    public static void speak(ServerPlayer actor, PilotEvent event) {
        MinecraftServer server = actor.getServer();
        if (server == null) {
            return;
        }
        String name = actor.getGameProfile().getName();

        NarrationPayload toActor = new NarrationPayload(
                nz(event.title()), nz(event.actorText()), event.agePoints());

        String otherRaw = event.otherText() != null ? event.otherText() : event.actorText();
        NarrationPayload toOthers = new NarrationPayload(
                nz(event.title()), nz(otherRaw).replace("{player}", name), event.agePoints());

        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
            PacketDistributor.sendToPlayer(p, p == actor ? toActor : toOthers);
        }
    }

    private static String nz(String s) {
        return s == null ? "" : s;
    }

    private Voix() {}
}
