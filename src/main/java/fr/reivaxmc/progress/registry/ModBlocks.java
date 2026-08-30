package fr.reivaxmc.progress.registry;

import fr.reivaxmc.progress.ReivaxMCProgress;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Les blocs du mod. Reconstruction propre : on réutilise les modèles et textures d'origine
 * (formes sculptées), on les enregistre proprement et on leur redonne des noms cohérents.
 *
 * Pour l'instant ce sont des blocs statiques (ils se posent et s'affichent correctement).
 * Leur comportement spécial — interfaces, Matrice qui lit une Trace, etc. — reviendra
 * avec le moteur, dans un prochain incrément.
 */
public final class ModBlocks {

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ReivaxMCProgress.MODID);

    public static final DeferredBlock<Block> FOUNDATION_BEACON = register("foundation_beacon");
    public static final DeferredBlock<Block> ORIGIN_MATRIX     = register("origin_matrix");
    public static final DeferredBlock<Block> STORY_TRACE       = register("story_trace");
    public static final DeferredBlock<Block> ORIGIN_RELIQUARY  = register("origin_reliquary");
    public static final DeferredBlock<Block> FRAGMENT_ALTAR    = register("fragment_altar");
    public static final DeferredBlock<Block> MEMORIAL_PLAQUE   = register("memorial_plaque");

    /** Enregistre un bloc simple + son objet-bloc (ce qu'on tient en main). */
    private static DeferredBlock<Block> register(String name) {
        DeferredBlock<Block> block = BLOCKS.registerSimpleBlock(name,
                BlockBehaviour.Properties.of().strength(3.0f).noOcclusion());
        ModItems.ITEMS.registerSimpleBlockItem(block);
        return block;
    }

    private ModBlocks() {}
}
