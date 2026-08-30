package fr.reivaxmc.progress;

import com.mojang.logging.LogUtils;
import fr.reivaxmc.progress.registry.ModBlocks;
import fr.reivaxmc.progress.registry.ModCreativeTabs;
import fr.reivaxmc.progress.registry.ModItems;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

/**
 * Point d'entrée du mod ReivaxMC Progress.
 *
 * Reconstruction propre (Alpha 18) : « On construit le moteur une fois. Ensuite, on nourrit le monde. »
 */
@Mod(ReivaxMCProgress.MODID)
public class ReivaxMCProgress {

    public static final String MODID = "reivaxmc_progress";

    private static final Logger LOGGER = LogUtils.getLogger();

    public ReivaxMCProgress(IEventBus modEventBus) {
        // Enregistrement du contenu du mod sur le bus d'événements.
        // Les blocs d'abord (ils déclarent aussi leurs objets-blocs dans ModItems.ITEMS).
        ModBlocks.BLOCKS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModCreativeTabs.CREATIVE_MODE_TABS.register(modEventBus);

        LOGGER.info("[ReivaxMC Progress] Reconstruction propre chargee. La Voix se souvient de ce que vous avez oublie.");
    }
}
