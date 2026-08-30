package fr.reivaxmc.progress;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

/**
 * Point d'entrée du mod ReivaxMC Progress.
 *
 * Reconstruction propre (Alpha 18) : « On construit le moteur une fois. Ensuite, on nourrit le monde. »
 * Ce fichier est volontairement minimal : il sert d'abord à valider la chaîne
 * code -> compilation cloud -> jar téléchargeable -> test en jeu.
 */
@Mod(ReivaxMCProgress.MODID)
public class ReivaxMCProgress {

    public static final String MODID = "reivaxmc_progress";

    private static final Logger LOGGER = LogUtils.getLogger();

    public ReivaxMCProgress(IEventBus modEventBus) {
        LOGGER.info("[ReivaxMC Progress] Reconstruction propre chargee. La Voix se souvient de ce que vous avez oublie.");
    }
}
