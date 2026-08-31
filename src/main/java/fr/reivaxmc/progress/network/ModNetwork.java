package fr.reivaxmc.progress.network;

import fr.reivaxmc.progress.ReivaxMCProgress;
import fr.reivaxmc.progress.client.ClientNarrationHandler;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * Déclare les paquets réseau du mod. Le paquet de narration ne va que vers le client ;
 * son traitement client est appelé paresseusement (jamais chargé côté serveur).
 */
@EventBusSubscriber(modid = ReivaxMCProgress.MODID, bus = EventBusSubscriber.Bus.MOD)
public final class ModNetwork {

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToClient(
                NarrationPayload.TYPE,
                NarrationPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> ClientNarrationHandler.receive(payload))
        );
    }

    private ModNetwork() {}
}
