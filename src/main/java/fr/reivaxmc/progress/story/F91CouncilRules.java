package fr.reivaxmc.progress.story;

import java.util.Map;
import java.util.Set;

/** Logique pure du Conseil, testable sans démarrer Minecraft. */
public final class F91CouncilRules {
   private F91CouncilRules() {
   }

   public static Result evaluate(Set<String> required, Map<String, String> votes) {
      if (required == null || required.isEmpty() || votes == null) return new Result(State.INVALID, "");
      if (!votes.keySet().containsAll(required)) return new Result(State.WAITING, "");
      String consensus = null;
      for (String voter : required) {
         String vote = votes.get(voter);
         if (vote == null || vote.isBlank()) return new Result(State.INVALID, "");
         if (consensus == null) consensus = vote;
         else if (!consensus.equals(vote)) return new Result(State.DISSONANCE, "");
      }
      return new Result(State.CONFIRMED, consensus == null ? "" : consensus);
   }

   public enum State { INVALID, WAITING, DISSONANCE, CONFIRMED }
   public record Result(State state, String doctrine) { }
}
