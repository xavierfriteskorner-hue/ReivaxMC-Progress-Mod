package fr.reivaxmc.progress.story;

/**
 * Une intervention du narrateur, telle que décrite dans les données (age1_pilots.json).
 * Texte, points et récompense viennent des données — on les édite sans toucher au moteur.
 */
public record PilotEvent(
        String id,
        String title,
        String actorText,
        String otherText,
        int agePoints,
        int civScore,
        Trigger trigger,
        String rewardItem,
        int rewardCount
) {}
