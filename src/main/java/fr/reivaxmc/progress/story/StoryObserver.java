package fr.reivaxmc.progress.story;

import fr.reivaxmc.progress.ReivaxMCProgress;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;

/**
 * L'Observer : il transforme des actions Minecraft en faits neutres et les poste sur le Story Bus.
 * Il ne choisit aucun texte et ne donne aucun point — il constate, c'est tout.
 */
@EventBusSubscriber(modid = ReivaxMCProgress.MODID)
public final class StoryObserver {

    @SubscribeEvent
    public static void onBlockBroken(BlockEvent.BreakEvent event) {
        if (event.getLevel().isClientSide()) {
            return;
        }
        if (!(event.getPlayer() instanceof ServerPlayer player)) {
            return;
        }
        String factKey = classify(event.getState());
        if (factKey != null) {
            StoryBus.post(new StoryFact(factKey, player));
        }
    }

    /** Traduit le bloc cassé en fait normalisé (ou null si ça ne nous intéresse pas). */
    private static String classify(BlockState state) {
        if (state.is(BlockTags.LOGS)) {
            return "mine_wood";
        }
        if (state.is(Blocks.STONE)) {
            return "mine_stone";
        }
        if (state.is(Blocks.COAL_ORE) || state.is(Blocks.DEEPSLATE_COAL_ORE)) {
            return "mine_coal";
        }
        if (state.is(Blocks.IRON_ORE) || state.is(Blocks.DEEPSLATE_IRON_ORE)) {
            return "mine_iron";
        }
        return null;
    }

    private StoryObserver() {}
}
