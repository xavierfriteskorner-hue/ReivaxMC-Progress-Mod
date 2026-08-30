package fr.reivaxmc.progress.registry;

import fr.reivaxmc.progress.ReivaxMCProgress;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Les objets du mod. Reconstruction propre : chaque objet est déclaré une fois,
 * proprement, avec sa texture et son nom canonique.
 */
public final class ModItems {

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ReivaxMCProgress.MODID);

    // Le Sceau des Origines — premier signe que le monde reconnaît quelque chose chez les joueurs.
    public static final DeferredItem<Item> ORIGIN_SEAL = ITEMS.registerSimpleItem(
            "origin_seal", new Item.Properties().rarity(Rarity.RARE));

    // Le Fragment inconnu — la pièce manquante d'un Site des Origines.
    public static final DeferredItem<Item> UNKNOWN_FRAGMENT = ITEMS.registerSimpleItem(
            "unknown_fragment", new Item.Properties());

    // Le Destin des Origines — le carnet de la campagne.
    public static final DeferredItem<Item> DESTINY_OF_ORIGINS = ITEMS.registerSimpleItem(
            "destiny_of_origins", new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));

    private ModItems() {}
}
