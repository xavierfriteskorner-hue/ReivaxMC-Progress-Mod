package fr.reivaxmc.progress.item;

import fr.reivaxmc.progress.network.ProgressNetworking;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Le Livre du Destin des Origines. Clic droit (en main) = ouvre l'écran de lecture
 * (DestinyBookScreen), côté client, via le paquet OpenBook. Sans ça, le livre récupéré
 * sur le pupitre du Sanctuaire ne pouvait pas être ouvert.
 */
public final class DestinyBookItem extends Item {
   public DestinyBookItem(Properties p) {
      super(p);
   }

   @Override
   public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
      ItemStack stack = player.getItemInHand(hand);
      if (!level.isClientSide && player instanceof ServerPlayer sp) {
         ProgressNetworking.openBook(sp);
      }

      return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
   }
}
