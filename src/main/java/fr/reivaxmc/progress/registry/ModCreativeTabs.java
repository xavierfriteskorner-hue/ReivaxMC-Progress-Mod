package fr.reivaxmc.progress.registry;

import fr.reivaxmc.progress.ReivaxMCProgress;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

/**
 * L'onglet créatif de la Maison ReivaxMC : un seul endroit où retrouver le contenu du mod.
 */
public final class ModCreativeTabs {

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ReivaxMCProgress.MODID);

    public static final Supplier<CreativeModeTab> ORIGINS_TAB = CREATIVE_MODE_TABS.register("origins",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.reivaxmc_progress.origins"))
                    .icon(() -> new ItemStack(ModItems.ORIGIN_SEAL.get()))
                    .displayItems((params, output) -> {
                        output.accept(ModItems.ORIGIN_SEAL.get());
                        output.accept(ModItems.UNKNOWN_FRAGMENT.get());
                        output.accept(ModItems.DESTINY_OF_ORIGINS.get());
                    })
                    .build());

    private ModCreativeTabs() {}
}
