package fr.reivaxmc.progress.network;

import fr.reivaxmc.progress.story.Alpha18FClientState;
import fr.reivaxmc.progress.story.DuoStartGate18F;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class Alpha18FNetwork {
   private Alpha18FNetwork() {
   }

   @SubscribeEvent
   public static void register(RegisterPayloadHandlersEvent var0) {
      PayloadRegistrar var1 = var0.registrar("13");
      var1.playToServer(Alpha18FPayloads.StoryStartRequest.TYPE, Alpha18FPayloads.StoryStartRequest.CODEC, (var0x, var1x) -> var1x.enqueueWork(() -> {
            if (var1x.player() instanceof ServerPlayer var1xx) {
               DuoStartGate18F.requestStart(var1xx);
            }
         }));
      var1.playToClient(
         Alpha18FPayloads.StoryStatus.TYPE,
         Alpha18FPayloads.StoryStatus.CODEC,
         (var0x, var1x) -> var1x.enqueueWork(() -> Alpha18FClientState.update(var0x.managed(), var0x.available(), var0x.started()))
      );
   }
}
