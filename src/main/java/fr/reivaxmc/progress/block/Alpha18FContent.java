package fr.reivaxmc.progress.block;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import net.neoforged.neoforge.registries.RegisterEvent;

@EventBusSubscriber(
   modid = "reivaxmc_progress",
   bus = Bus.MOD
)
public final class Alpha18FContent {
   public static final ResourceLocation TRACE_ID = ResourceLocation.fromNamespaceAndPath("reivaxmc_progress", "story_trace");
   public static final ResourceLocation OPENING_SOUND_ID = ResourceLocation.fromNamespaceAndPath("reivaxmc_progress", "story_opening");
   public static final ResourceLocation TRACE_APPEAR_SOUND_ID = ResourceLocation.fromNamespaceAndPath("reivaxmc_progress", "trace_appear");
   public static final ResourceLocation TRACE_INTERACT_SOUND_ID = ResourceLocation.fromNamespaceAndPath("reivaxmc_progress", "trace_interact");
   public static Block TRACE_BLOCK;
   public static SoundEvent OPENING_SOUND;
   public static SoundEvent TRACE_APPEAR_SOUND;
   public static SoundEvent TRACE_INTERACT_SOUND;

   private Alpha18FContent() {
   }

   @SubscribeEvent
   public static void register(RegisterEvent var0) {
      var0.register(Registries.BLOCK, TRACE_ID, () -> {
         Block var0x = new Block(Properties.of().strength(-1.0F, 3600000.0F).sound(SoundType.AMETHYST).noOcclusion().lightLevel(var0xx -> 8));
         TRACE_BLOCK = var0x;
         return var0x;
      });
      var0.register(Registries.SOUND_EVENT, OPENING_SOUND_ID, () -> {
         OPENING_SOUND = SoundEvent.createVariableRangeEvent(OPENING_SOUND_ID);
         return OPENING_SOUND;
      });
      var0.register(Registries.SOUND_EVENT, TRACE_APPEAR_SOUND_ID, () -> {
         TRACE_APPEAR_SOUND = SoundEvent.createVariableRangeEvent(TRACE_APPEAR_SOUND_ID);
         return TRACE_APPEAR_SOUND;
      });
      var0.register(Registries.SOUND_EVENT, TRACE_INTERACT_SOUND_ID, () -> {
         TRACE_INTERACT_SOUND = SoundEvent.createVariableRangeEvent(TRACE_INTERACT_SOUND_ID);
         return TRACE_INTERACT_SOUND;
      });
   }
}
