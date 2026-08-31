package fr.reivaxmc.progress.story;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * La remise d'une récompense décrite par une intervention (reward_item / reward_count).
 * Simple et sûr : on donne l'objet au joueur, le surplus tombe à ses pieds.
 */
public final class Reward {

    public static void give(ServerPlayer player, PilotEvent event) {
        if (event.rewardItem() == null || event.rewardCount() <= 0) {
            return;
        }
        ResourceLocation id = ResourceLocation.tryParse(event.rewardItem());
        if (id == null) {
            return;
        }
        Item item = BuiltInRegistries.ITEM.get(id);
        if (item == Items.AIR) {
            return;
        }
        ItemStack stack = new ItemStack(item, event.rewardCount());
        player.getInventory().placeItemBackInInventory(stack);
    }

    private Reward() {}
}
