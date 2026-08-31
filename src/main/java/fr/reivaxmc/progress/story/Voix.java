package fr.reivaxmc.progress.story;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

/**
 * La Voix des Origines : le rendu final d'une intervention.
 * « Monde commun, action attribuée, réaction partagée » — l'acteur entend sa phrase,
 * les autres joueurs en voient une variante à la troisième personne.
 */
public final class Voix {

    public static void speak(ServerPlayer actor, PilotEvent event) {
        MinecraftServer server = actor.getServer();
        if (server == null) {
            return;
        }
        Component actorLine = style(event.actorText());
        String otherRaw = event.otherText() != null ? event.otherText() : event.actorText();
        Component otherLine = style(replacePlayer(otherRaw, actor.getGameProfile().getName()));

        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
            p.displayClientMessage(p == actor ? actorLine : otherLine, false);
        }
    }

    private static String replacePlayer(String text, String name) {
        return text == null ? "" : text.replace("{player}", name);
    }

    private static Component style(String text) {
        return Component.literal(text == null ? "" : text)
                .withStyle(ChatFormatting.GOLD, ChatFormatting.ITALIC);
    }

    private Voix() {}
}
