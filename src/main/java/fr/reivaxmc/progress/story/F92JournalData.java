package fr.reivaxmc.progress.story;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedData.Factory;

/** Notes du Journal : une partie intime par joueur et une partie volontairement partagée. */
public final class F92JournalData extends SavedData {
   public static final String DATA_NAME = "reivaxmc_inherited_journal";
   private final Map<String, List<String>> personal = new HashMap<>();
   private final List<String> shared = new ArrayList<>();

   public static F92JournalData get(MinecraftServer server) {
      return server.overworld().getDataStorage().computeIfAbsent(
         new Factory<F92JournalData>(F92JournalData::new, F92JournalData::load, null), DATA_NAME);
   }

   private static F92JournalData load(CompoundTag tag, Provider provider) {
      F92JournalData data = new F92JournalData();
      ListTag personal = tag.getList("Personal", 8);
      for (int i = 0; i < personal.size(); i++) {
         String encoded = personal.getString(i);
         int separator = encoded.indexOf('¦');
         if (separator > 0) data.personal.computeIfAbsent(encoded.substring(0, separator), ignored -> new ArrayList<>()).add(encoded.substring(separator + 1));
      }
      ListTag shared = tag.getList("Shared", 8);
      for (int i = 0; i < shared.size(); i++) data.shared.add(shared.getString(i));
      return data;
   }

   @Override
   public synchronized CompoundTag save(CompoundTag tag, Provider provider) {
      ListTag personalTag = new ListTag();
      for (Map.Entry<String, List<String>> entry : personal.entrySet()) {
         for (String note : entry.getValue()) personalTag.add(StringTag.valueOf(entry.getKey() + "¦" + note));
      }
      tag.put("Personal", personalTag);
      ListTag sharedTag = new ListTag();
      for (String note : shared) sharedTag.add(StringTag.valueOf(note));
      tag.put("Shared", sharedTag);
      return tag;
   }

   public synchronized boolean addPersonal(String uuid, String text) {
      String clean = clean(text);
      if (uuid == null || uuid.isBlank() || clean.isBlank()) return false;
      List<String> notes = personal.computeIfAbsent(uuid, ignored -> new ArrayList<>());
      notes.add(0, clean);
      while (notes.size() > 20) notes.remove(notes.size() - 1);
      setDirty();
      return true;
   }

   public synchronized boolean addShared(int day, String author, String text) {
      String clean = clean(text);
      if (clean.isBlank()) return false;
      shared.add(0, day + "¦" + clean(author) + "¦" + clean);
      while (shared.size() > 40) shared.remove(shared.size() - 1);
      setDirty();
      return true;
   }

   public synchronized String personalPacket(String uuid) {
      return String.join("\n", personal.getOrDefault(uuid, List.of()));
   }

   public synchronized String sharedPacket() {
      return String.join("\n", shared);
   }

   private static String clean(String value) {
      if (value == null) return "";
      String clean = value.replace('¦', ' ').replace('\n', ' ').replace('\r', ' ').trim();
      return clean.length() > 180 ? clean.substring(0, 180) : clean;
   }
}
