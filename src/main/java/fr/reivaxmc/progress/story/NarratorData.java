package fr.reivaxmc.progress.story;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import fr.reivaxmc.progress.ReivaxMCProgress;
import org.slf4j.Logger;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Charge les interventions du narrateur depuis les données embarquées du mod.
 * Approche « data-driven » : le texte (et les déclencheurs) vivent dans le JSON, pas dans le code.
 */
public final class NarratorData {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String PATH = "/data/reivaxmc_progress/narrator/age1_pilots.json";
    private static final Map<String, PilotEvent> EVENTS = new LinkedHashMap<>();

    /** Lit le fichier une fois. Ne lève jamais d'exception vers le jeu : au pire, la Voix reste muette. */
    public static void load() {
        EVENTS.clear();
        try (InputStream in = ReivaxMCProgress.class.getResourceAsStream(PATH)) {
            if (in == null) {
                LOGGER.warn("[ReivaxMC] Donnees narrateur introuvables : {}", PATH);
                return;
            }
            JsonObject root = JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
            JsonArray events = root.getAsJsonArray("events");
            int withTrigger = 0;
            for (JsonElement el : events) {
                JsonObject o = el.getAsJsonObject();
                String id = str(o, "id");
                if (id == null) {
                    continue;
                }
                Trigger trigger = parseTrigger(o);
                if (trigger != null) {
                    withTrigger++;
                }
                EVENTS.put(id, new PilotEvent(
                        id,
                        str(o, "title"),
                        str(o, "actor_text"),
                        str(o, "other_text"),
                        intOr(o, "age_points", 0),
                        intOr(o, "civ_score", 0),
                        trigger,
                        str(o, "reward_item"),
                        intOr(o, "reward_count", 0)
                ));
            }
            LOGGER.info("[ReivaxMC] Narrateur charge : {} interventions ({} branchees a un declencheur).",
                    EVENTS.size(), withTrigger);
        } catch (Exception e) {
            LOGGER.error("[ReivaxMC] Echec du chargement des donnees narrateur.", e);
        }
    }

    private static Trigger parseTrigger(JsonObject o) {
        if (!o.has("trigger") || !o.get("trigger").isJsonObject()) {
            return null;
        }
        JsonObject t = o.getAsJsonObject("trigger");
        String type = str(t, "type");
        if (type == null) {
            return null;
        }
        for (String key : new String[]{"block", "item", "entity", "tag"}) {
            if (t.has(key)) {
                return new Trigger(type, key, str(t, key));
            }
        }
        return new Trigger(type, null, null);
    }

    public static PilotEvent get(String id) {
        return EVENTS.get(id);
    }

    public static Collection<PilotEvent> all() {
        return EVENTS.values();
    }

    public static int count() {
        return EVENTS.size();
    }

    private static String str(JsonObject o, String key) {
        return o.has(key) && !o.get(key).isJsonNull() ? o.get(key).getAsString() : null;
    }

    private static int intOr(JsonObject o, String key, int fallback) {
        return o.has(key) && !o.get(key).isJsonNull() ? o.get(key).getAsInt() : fallback;
    }

    private NarratorData() {}
}
