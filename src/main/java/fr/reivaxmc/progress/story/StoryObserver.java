package fr.reivaxmc.progress.story;

import fr.reivaxmc.progress.ReivaxMCProgress;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

import java.util.Set;

/**
 * L'Observer : transforme des actions Minecraft en faits neutres, postés sur le Story Bus.
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
        BlockState state = event.getState();
        String blockId = normalize(key(state.getBlock()));
        Set<String> tags = state.is(BlockTags.LOGS) ? Set.of("minecraft:logs") : Set.of();
        StoryBus.post(new StoryFact("block_break", player, blockId, tags));
    }

    @SubscribeEvent
    public static void onCraft(PlayerEvent.ItemCraftedEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        StoryBus.post(new StoryFact("craft", player, key(event.getCrafting().getItem()), Set.of()));
    }

    @SubscribeEvent
    public static void onSmelt(PlayerEvent.ItemSmeltedEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        StoryBus.post(new StoryFact("smelt", player, key(event.getSmelting().getItem()), Set.of()));
    }

    @SubscribeEvent
    public static void onPlace(BlockEvent.EntityPlaceEvent event) {
        if (event.getLevel().isClientSide()) {
            return;
        }
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        BlockState state = event.getPlacedBlock();
        Set<String> tags = state.is(BlockTags.SAPLINGS) ? Set.of("minecraft:saplings") : Set.of();
        StoryBus.post(new StoryFact("place", player, key(state.getBlock()), tags));
    }

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        LivingEntity dead = event.getEntity();
        if (dead.level().isClientSide()) {
            return;
        }
        if (dead instanceof ServerPlayer player) {
            StoryBus.post(new StoryFact("death", player, "minecraft:player", Set.of()));
            return;
        }
        if (event.getSource().getEntity() instanceof ServerPlayer killer) {
            String entityId = EntityType.getKey(dead.getType()).toString();
            StoryBus.post(new StoryFact("kill", killer, entityId, Set.of()));
        }
    }

    private static String key(Block block) {
        return BuiltInRegistries.BLOCK.getKey(block).toString();
    }

    private static String key(Item item) {
        return BuiltInRegistries.ITEM.getKey(item).toString();
    }

    /** Ramène les variantes deepslate à leur minerai de base, pour un seul déclencheur. */
    private static String normalize(String blockId) {
        return switch (blockId) {
            case "minecraft:deepslate_coal_ore" -> "minecraft:coal_ore";
            case "minecraft:deepslate_iron_ore" -> "minecraft:iron_ore";
            case "minecraft:deepslate_diamond_ore" -> "minecraft:diamond_ore";
            case "minecraft:deepslate_gold_ore" -> "minecraft:gold_ore";
            case "minecraft:deepslate_copper_ore" -> "minecraft:copper_ore";
            default -> blockId;
        };
    }

    private StoryObserver() {}
}
