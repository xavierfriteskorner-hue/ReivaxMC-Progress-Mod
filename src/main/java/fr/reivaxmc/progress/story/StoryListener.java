package fr.reivaxmc.progress.story;

import java.util.List;

@FunctionalInterface
public interface StoryListener {
   List<TopicDecision> onFact(StoryFact var1);
}
