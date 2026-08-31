package fr.reivaxmc.progress.story;

/**
 * Le Story Bus : un point de passage unique entre « ce qui est observé » et « ce qui en est fait ».
 * Volontairement mince pour l'instant, mais c'est la couture sur laquelle brancheront demain
 * la Campagne, les Archives, la Chronologie, la Progression…
 */
public final class StoryBus {

    public static void post(StoryFact fact) {
        Director.handle(fact);
    }

    private StoryBus() {}
}
