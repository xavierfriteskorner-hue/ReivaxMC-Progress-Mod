package fr.reivaxmc.progress;

import fr.reivaxmc.progress.block.FoundationBeaconBlock;
import fr.reivaxmc.progress.block.FragmentAltarBlock;
import fr.reivaxmc.progress.block.MemorialPlaqueBlock;
import fr.reivaxmc.progress.block.OriginMatrixBlock;
import fr.reivaxmc.progress.block.OriginReliquaryBlock;
import fr.reivaxmc.progress.network.ProgressNetworking;
import fr.reivaxmc.progress.progression.ProgressEvents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredRegister.Blocks;
import net.neoforged.neoforge.registries.DeferredRegister.Items;

@Mod("reivaxmc_progress")
public final class ReivaxMCProgress {
   public static final String MODID = "reivaxmc_progress";
   public static final Blocks BLOCKS = DeferredRegister.createBlocks("reivaxmc_progress");
   public static final Items ITEMS = DeferredRegister.createItems("reivaxmc_progress");
   public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, "reivaxmc_progress");
   public static final DeferredBlock<Block> FOUNDATION_BEACON = BLOCKS.register(
      "foundation_beacon", () -> new FoundationBeaconBlock(Properties.of().strength(4.0F, 8.0F).sound(SoundType.COPPER).noOcclusion().lightLevel(s -> 4))
   );
   public static final DeferredItem<BlockItem> FOUNDATION_BEACON_ITEM = ITEMS.registerSimpleBlockItem(
      "foundation_beacon", FOUNDATION_BEACON, new net.minecraft.world.item.Item.Properties().stacksTo(1).rarity(Rarity.EPIC)
   );
   public static final DeferredBlock<Block> ORIGIN_RELIQUARY = BLOCKS.register(
      "origin_reliquary",
      () -> new OriginReliquaryBlock(Properties.of().strength(-1.0F, 3600000.0F).sound(SoundType.DEEPSLATE).noOcclusion().lightLevel(s -> 8))
   );
   public static final DeferredItem<BlockItem> ORIGIN_RELIQUARY_ITEM = ITEMS.registerSimpleBlockItem(
      "origin_reliquary", ORIGIN_RELIQUARY, new net.minecraft.world.item.Item.Properties().stacksTo(1).rarity(Rarity.EPIC)
   );
   public static final DeferredBlock<Block> ORIGIN_MATRIX = BLOCKS.register(
      "origin_matrix", () -> new OriginMatrixBlock(Properties.of().strength(3.0F, 6.0F).sound(SoundType.AMETHYST).noOcclusion().lightLevel(s -> 5))
   );
   public static final DeferredItem<BlockItem> ORIGIN_MATRIX_ITEM = ITEMS.registerSimpleBlockItem(
      "origin_matrix", ORIGIN_MATRIX, new net.minecraft.world.item.Item.Properties().stacksTo(1).rarity(Rarity.EPIC)
   );
   public static final DeferredBlock<Block> MEMORIAL_PLAQUE = BLOCKS.register(
      "memorial_plaque",
      () -> new MemorialPlaqueBlock(Properties.of().strength(-1.0F, 3600000.0F).sound(SoundType.POLISHED_DEEPSLATE).noOcclusion().lightLevel(s -> 2))
   );
   public static final DeferredItem<BlockItem> MEMORIAL_PLAQUE_ITEM = ITEMS.registerSimpleBlockItem(
      "memorial_plaque", MEMORIAL_PLAQUE, new net.minecraft.world.item.Item.Properties().stacksTo(1).rarity(Rarity.RARE)
   );
   public static final DeferredBlock<Block> FRAGMENT_ALTAR = BLOCKS.register(
      "fragment_altar",
      () -> new FragmentAltarBlock(Properties.of().strength(-1.0F, 3600000.0F).sound(SoundType.GILDED_BLACKSTONE).noOcclusion().lightLevel(s -> 6))
   );
   public static final DeferredItem<BlockItem> FRAGMENT_ALTAR_ITEM = ITEMS.registerSimpleBlockItem(
      "fragment_altar", FRAGMENT_ALTAR, new net.minecraft.world.item.Item.Properties().stacksTo(1).rarity(Rarity.EPIC)
   );
   public static final DeferredItem<Item> ORIGIN_SEAL = ITEMS.register(
      "origin_seal", () -> new Item(new net.minecraft.world.item.Item.Properties().stacksTo(1).rarity(Rarity.EPIC))
   );
   public static final DeferredItem<Item> DESTINY_BOOK = ITEMS.register(
      "destiny_of_origins", () -> new Item(new net.minecraft.world.item.Item.Properties().stacksTo(1).rarity(Rarity.RARE))
   );
   public static final DeferredItem<Item> UNKNOWN_FRAGMENT = ITEMS.register(
      "unknown_fragment", () -> new Item(new net.minecraft.world.item.Item.Properties().stacksTo(1).rarity(Rarity.EPIC))
   );
   public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TAB = TABS.register(
      "main",
      () -> CreativeModeTab.builder()
            .title(Component.literal("ReivaxMC Progress"))
            .withTabsBefore(new ResourceKey[]{CreativeModeTabs.FUNCTIONAL_BLOCKS})
            .icon(() -> ((Item)ORIGIN_SEAL.get()).getDefaultInstance())
            .displayItems((p, o) -> {
               o.accept((ItemLike)ORIGIN_SEAL.get());
               o.accept((ItemLike)DESTINY_BOOK.get());
               o.accept((ItemLike)UNKNOWN_FRAGMENT.get());
               o.accept((ItemLike)FOUNDATION_BEACON_ITEM.get());
               o.accept((ItemLike)ORIGIN_MATRIX_ITEM.get());
            })
            .build()
   );

   public ReivaxMCProgress(IEventBus bus, ModContainer c) {
      BLOCKS.register(bus);
      ITEMS.register(bus);
      TABS.register(bus);
      bus.addListener(ProgressNetworking::register);
      NeoForge.EVENT_BUS.register(new ProgressEvents());
   }
}
