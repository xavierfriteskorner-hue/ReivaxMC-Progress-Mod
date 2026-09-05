package fr.reivaxmc.progress.progression;

import fr.reivaxmc.progress.story.F81DevTools;
import fr.reivaxmc.progress.narrator.NarratorEngine;
import fr.reivaxmc.progress.story.F8InteractionBridge;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.EntityMountEvent;
import net.neoforged.neoforge.event.entity.EntityStruckByLightningEvent;
import net.neoforged.neoforge.event.entity.item.ItemTossEvent;
import net.neoforged.neoforge.event.entity.living.AnimalTameEvent;
import net.neoforged.neoforge.event.entity.living.BabyEntitySpawnEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.ItemCraftedEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.ItemSmeltedEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.EntityInteract;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.RightClickItem;
import net.neoforged.neoforge.event.entity.player.TradeWithVillagerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerWakeUpEvent;
import net.neoforged.neoforge.event.level.BlockEvent.BreakEvent;
import net.neoforged.neoforge.event.level.BlockEvent.EntityPlaceEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent.Post;

public final class ProgressEvents {
   @SubscribeEvent
   public void login(PlayerLoggedInEvent var1) {
      F81DevTools.onLogin(var1);
      NarratorEngine.onLogin(var1);
   }

   @SubscribeEvent
   public void rightClickBlock(RightClickBlock var1) {
      F8InteractionBridge.onRightClickBlock(var1);
      NarratorEngine.onRightClickBlock(var1);
   }

   @SubscribeEvent
   public void rightClickItem(RightClickItem var1) {
      NarratorEngine.onRightClickItem(var1);
   }

   @SubscribeEvent
   public void serverTick(Post var1) {
      NarratorEngine.onServerTick(var1);
   }

   @SubscribeEvent
   public void playerTick(net.neoforged.neoforge.event.tick.PlayerTickEvent.Post var1) {
      NarratorEngine.onPlayerTick(var1);
   }

   @SubscribeEvent
   public void placed(EntityPlaceEvent var1) {
      F8InteractionBridge.onPlaced(var1);
      NarratorEngine.onBlockPlaced(var1);
   }

   @SubscribeEvent
   public void broken(BreakEvent var1) {
      F8InteractionBridge.onBreak(var1);
      NarratorEngine.onBlockBroken(var1);
   }

   @SubscribeEvent
   public void commands(RegisterCommandsEvent var1) {
      F81DevTools.onRegisterCommands(var1);
      NarratorEngine.onCommands(var1);
   }

   @SubscribeEvent
   public void narratorCrafted(ItemCraftedEvent var1) {
      NarratorEngine.onItemCrafted(var1);
   }

   @SubscribeEvent
   public void narratorSmelted(ItemSmeltedEvent var1) {
      NarratorEngine.onItemSmelted(var1);
   }

   @SubscribeEvent
   public void narratorLivingDeath(LivingDeathEvent var1) {
      F8InteractionBridge.onLivingDeath(var1);
      NarratorEngine.onLivingDeath(var1);
   }

   @SubscribeEvent
   public void narratorLivingDamage(LivingDamageEvent.Post var1) {
      NarratorEngine.onLivingDamage(var1);
   }

   @SubscribeEvent
   public void narratorAnimalTame(AnimalTameEvent var1) {
      NarratorEngine.onAnimalTame(var1);
   }

   @SubscribeEvent
   public void narratorAnimalInteract(EntityInteract event) {
      NarratorEngine.onEntityInteract(event);
   }

   @SubscribeEvent(priority = EventPriority.LOWEST)
   public void narratorAnimalBred(BabyEntitySpawnEvent event) {
      NarratorEngine.onAnimalBred(event);
   }

   @SubscribeEvent(priority = EventPriority.LOWEST)
   public void narratorEntityMounted(EntityMountEvent event) {
      NarratorEngine.onEntityMounted(event);
   }

   @SubscribeEvent
   public void narratorVillagerTrade(TradeWithVillagerEvent event) {
      NarratorEngine.onVillagerTrade(event);
   }

   @SubscribeEvent(priority = EventPriority.LOWEST)
   public void narratorItemToss(ItemTossEvent var1) {
      NarratorEngine.onItemToss(var1);
   }

   @SubscribeEvent
   public void narratorItemConsumed(LivingEntityUseItemEvent.Finish event) {
      NarratorEngine.onItemConsumed(event);
   }

   @SubscribeEvent
   public void narratorPlayerWakeUp(PlayerWakeUpEvent event) {
      NarratorEngine.onPlayerWakeUp(event);
   }

   @SubscribeEvent(priority = EventPriority.LOWEST)
   public void narratorLightning(EntityStruckByLightningEvent event) {
      NarratorEngine.onEntityStruckByLightning(event);
   }

   @SubscribeEvent
   public void narratorItemPickup(net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent.Post var1) {
      NarratorEngine.onItemPickup(var1);
   }
}
