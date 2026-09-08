package fr.reivaxmc.progress.story;

public final class V12TrailRulesSelfTest {
   public static void main(String[] args){
      check(!V12TrailRules.crossComplete("a",100,"",0,1),"un seul pupitre ne suffit pas");
      check(V12TrailRules.crossComplete("a",100,"a",599,1),"solo dans 25 secondes");
      check(!V12TrailRules.crossComplete("a",100,"a",601,1),"solo hors fenêtre");
      check(V12TrailRules.crossComplete("a",100,"b",339,2),"duo distinct dans 12 secondes");
      check(!V12TrailRules.crossComplete("a",100,"a",120,2),"duo exige deux personnes");
      check(!V12TrailRules.crossComplete("a",100,"b",341,2),"duo hors fenêtre");
      for(String stage:new String[]{V12TrailRules.OFFERED,V12TrailRules.EIGHTH_LINE,V12TrailRules.PROCESSION_LOCATE,V12TrailRules.PROCESSION_OBSERVE,V12TrailRules.HOUSE_LOCATE,V12TrailRules.HOUSE_INSPECT,V12TrailRules.RETURN_REGISTRY,V12TrailRules.CROSS_CALL,V12TrailRules.MEMORY_COUNCIL,V12TrailRules.COMPLETE})
         check(!V12TrailRules.objective(stage,2,3).isBlank(),"objectif absent pour "+stage);
      System.out.println("V12TrailRulesSelfTest OK");
   }
   private static void check(boolean ok,String msg){if(!ok)throw new AssertionError(msg);}
}
