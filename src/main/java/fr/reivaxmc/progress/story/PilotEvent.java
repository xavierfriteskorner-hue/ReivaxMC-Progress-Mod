package fr.reivaxmc.progress.story;

/**
 * Une intervention du narrateur, telle que décrite dans les données (age1_pilots.json).
 * Le texte vient des données, jamais du code — on pourra l'éditer sans toucher au moteur.
 */
public record PilotEvent(
        String id,
        String title,
        String actorText,
        String otherText,
        int agePoints,
        int civScore
) {}
