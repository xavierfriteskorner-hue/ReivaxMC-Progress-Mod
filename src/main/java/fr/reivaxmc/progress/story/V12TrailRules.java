package fr.reivaxmc.progress.story;

/** Transitions pures du Chapitre III — Les Noms retirés. */
public final class V12TrailRules {
   public static final String LOCKED="LOCKED", OFFERED="OFFERED", EIGHTH_LINE="EIGHTH_LINE";
   public static final String PROCESSION_LOCATE="PROCESSION_LOCATE", PROCESSION_OBSERVE="PROCESSION_OBSERVE";
   public static final String HOUSE_LOCATE="HOUSE_LOCATE", HOUSE_INSPECT="HOUSE_INSPECT", RETURN_REGISTRY="RETURN_REGISTRY";
   public static final String CROSS_CALL="CROSS_CALL", MEMORY_COUNCIL="MEMORY_COUNCIL", COMPLETE="COMPLETE";
   private V12TrailRules() {}

   public static String title(){return "Les Noms retirés";}
   public static String objective(String stage,int traces,int witnesses){return switch(stage){
      case LOCKED->"La mémoire n'a pas encore ouvert cette Piste.";
      case OFFERED->"Une ligne supplémentaire est apparue dans le Registre. Suivez cette Piste quand vous le souhaitez.";
      case EIGHTH_LINE->"Consultez le Registre des Absents et reconnaissez ce que vous seul avez lu.";
      case PROCESSION_LOCATE->"La Boussole perçoit une procession hors du Foyer.";
      case PROCESSION_OBSERVE->"Suivez les silhouettes sans les dépasser ni les frapper · "+witnesses+"/6 effacées.";
      case HOUSE_LOCATE->"Suivez la trace jusqu'à la maison numérotée.";
      case HOUSE_INSPECT->"Examinez les quatre traces de vie dans la maison · "+traces+"/4.";
      case RETURN_REGISTRY->"Rapportez au Registre ce que la maison conservait.";
      case CROSS_CALL->"Dans la Galerie, faites répondre les deux pupitres de Concordance.";
      case MEMORY_COUNCIL->"Le Registre attend la décision commune du Foyer sur les noms retirés.";
      case COMPLETE->"Cette Piste est achevée. La décision demeure dans le Registre.";
      default->"La Piste cherche encore sa forme.";
   };}

   public static boolean crossComplete(String leftPlayer,long leftTick,String rightPlayer,long rightTick,int participants){
      if(leftTick<=0||rightTick<=0) return false;
      long window=participants>1?240L:500L;
      if(Math.abs(leftTick-rightTick)>window) return false;
      return participants<=1||!leftPlayer.equals(rightPlayer);
   }
}
