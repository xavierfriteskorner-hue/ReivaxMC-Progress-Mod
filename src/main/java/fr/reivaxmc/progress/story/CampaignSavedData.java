package fr.reivaxmc.progress.story;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashSet;
import java.util.Set;

/**
 * La mémoire persistante de la campagne, au niveau du monde.
 * Pour l'instant : la liste des interventions déjà déclenchées (pour ne jamais les rejouer).
 * Stockée sur l'Overworld (jamais déchargé) car c'est une vérité globale du monde.
 */
public class CampaignSavedData extends SavedData {

    private static final String NAME = "reivaxmc_campaign";

    private final Set<String> firedEvents = new HashSet<>();

    public static CampaignSavedData create() {
        return new CampaignSavedData();
    }

    public static CampaignSavedData load(CompoundTag tag, HolderLookup.Provider provider) {
        CampaignSavedData data = create();
        ListTag list = tag.getList("fired_events", Tag.TAG_STRING);
        for (int i = 0; i < list.size(); i++) {
            data.firedEvents.add(list.getString(i));
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag list = new ListTag();
        for (String s : firedEvents) {
            list.add(StringTag.valueOf(s));
        }
        tag.put("fired_events", list);
        return tag;
    }

    public boolean hasFired(String id) {
        return firedEvents.contains(id);
    }

    /** Marque un événement comme déclenché. Renvoie true si c'était la première fois. */
    public boolean markFired(String id) {
        boolean added = firedEvents.add(id);
        if (added) {
            setDirty();
        }
        return added;
    }

    public static CampaignSavedData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(
                new Factory<>(CampaignSavedData::create, CampaignSavedData::load), NAME);
    }
}
