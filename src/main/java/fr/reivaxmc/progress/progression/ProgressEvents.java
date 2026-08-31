package fr.reivaxmc.progress.progression;

import fr.reivaxmc.progress.story.F81DevTools;
import fr.reivaxmc.progress.story.F8InteractionBridge;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.item.ItemTossEvent;
import net.neoforged.neoforge.event.entity.living.AnimalTameEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.ItemCraftedEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.ItemSmeltedEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.RightClickItem;
import net.neoforged.neoforge.event.level.BlockEvent.BreakEvent;
import net.neoforged.neoforge.event.level.BlockEvent.EntityPlaceEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent.Post;

public final class ProgressEvents {
   @SubscribeEvent
   public void login(PlayerLoggedInEvent var1) {
      F81DevTools.onLogin(var1);
   }

   @SubscribeEvent
   public void rightClickBlock(RightClickBlock var1) {
      F8InteractionBridge.onRightClickBlock(var1);
   }

   @SubscribeEvent
   public void rightClickItem(RightClickItem var1) {
   }

   @SubscribeEvent
   public void serverTick(Post var1) {
   }

   @SubscribeEvent
   public void playerTick(net.neoforged.neoforge.event.tick.PlayerTickEvent.Post var1) {
   }

   @SubscribeEvent
   public void placed(EntityPlaceEvent var1) {
      F8InteractionBridge.onPlaced(var1);
   }

   @SubscribeEvent
   public void broken(BreakEvent var1) {
      F8InteractionBridge.onBreak(var1);
   }

   @SubscribeEvent
   public void commands(RegisterCommandsEvent var1) {
      F81DevTools.onRegisterCommands(var1);
   }

   @SubscribeEvent
   public void narratorCrafted(ItemCraftedEvent var1) {
   }

   @SubscribeEvent
   public void narratorSmelted(ItemSmeltedEvent var1) {
   }

   @SubscribeEvent
   public void narratorLivingDeath(LivingDeathEvent var1) {
      F8InteractionBridge.onLivingDeath(var1);
   }

   @SubscribeEvent
   public void narratorAnimalTame(AnimalTameEvent var1) {
   }

   @SubscribeEvent
   public void narratorItemToss(ItemTossEvent var1) {
   }

   @SubscribeEvent
   public void narratorItemPickup(net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent.Post var1) {
   }
}
