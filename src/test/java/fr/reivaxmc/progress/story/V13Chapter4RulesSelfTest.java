package fr.reivaxmc.progress.story;

public final class V13Chapter4RulesSelfTest {
   public static void main(String[] args){
      check(V13Chapter4Rules.concordanceComplete("a",100,"a",599,1),"solo dans la fenêtre");
      check(!V13Chapter4Rules.concordanceComplete("a",100,"a",601,1),"solo hors fenêtre");
      check(V13Chapter4Rules.concordanceComplete("a",100,"b",339,2),"duo distinct");
      check(!V13Chapter4Rules.concordanceComplete("a",100,"a",120,2),"duo exige deux personnes");
      check(!V13Chapter4Rules.concordanceComplete("a",100,"b",341,2),"duo hors fenêtre");
      for(String stage:new String[]{V13Chapter4Rules.OFFERED,V13Chapter4Rules.TESTIMONIES,V13Chapter4Rules.SEEK_FRAGMENT,V13Chapter4Rules.RETURN_GALLERY,V13Chapter4Rules.STRONG_CONCORDANCE,V13Chapter4Rules.MATRIX_READY,V13Chapter4Rules.REVELATION,V13Chapter4Rules.COMPLETE})check(!V13Chapter4Rules.objective(stage,2,1).isBlank(),"objectif absent "+stage);
      check(V13Chapter4Rules.policyConsequence("RESTORE_NAMES").contains("noms"),"branche restitution");
      check(V13Chapter4Rules.policyConsequence("KEEP_BOTH").contains("coutures"),"branche deux traces");
      check(V13Chapter4Rules.policyConsequence("LEAVE_BLANKS").contains("blancs"),"branche blancs");
      System.out.println("V13Chapter4RulesSelfTest OK");
   }
   private static void check(boolean ok,String message){if(!ok)throw new AssertionError(message);}
}
