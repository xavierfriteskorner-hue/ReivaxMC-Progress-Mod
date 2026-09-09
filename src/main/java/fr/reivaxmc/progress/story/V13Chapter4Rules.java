package fr.reivaxmc.progress.story;

/** Transitions pures du Chapitre IV — La Mémoire n'est pas la vérité. */
public final class V13Chapter4Rules {
   public static final String LOCKED="LOCKED",OFFERED="OFFERED",TESTIMONIES="TESTIMONIES";
   public static final String SEEK_FRAGMENT="SEEK_FRAGMENT",RETURN_GALLERY="RETURN_GALLERY";
   public static final String STRONG_CONCORDANCE="STRONG_CONCORDANCE",MATRIX_READY="MATRIX_READY";
   public static final String REVELATION="REVELATION",COMPLETE="COMPLETE";
   private V13Chapter4Rules(){}

   public static String title(){return "La Mémoire n'est pas la vérité";}
   public static String objective(String stage,int testimonies,int acknowledgements){return switch(stage){
      case LOCKED->"La Galerie n'a pas encore rendu cette Piste visible.";
      case OFFERED->"Trois témoignages incompatibles attendent dans la Galerie du Sanctuaire.";
      case TESTIMONIES->"Écoutez et reconnaissez les trois versions conservées par la Galerie · "+testimonies+"/3.";
      case SEEK_FRAGMENT->"La Boussole suit une seconde signature jusqu'aux Archives brisées.";
      case RETURN_GALLERY->"Rapportez les deux signatures de Fragment aux pupitres de la Galerie.";
      case STRONG_CONCORDANCE->"Faites répondre les deux pupitres avec les deux Fragments à proximité.";
      case MATRIX_READY->"La chambre occidentale est ouverte. Présentez les Fragments à la Matrice.";
      case REVELATION->"Lisez ce que la Matrice a reconnu et confrontez vos versions · "+acknowledgements+" lecture(s).";
      case COMPLETE->"La Matrice conserve désormais la contradiction — et une signature impossible.";
      default->"La Piste cherche encore sa forme.";
   };}

   public static boolean concordanceComplete(String left,long leftTick,String right,long rightTick,int participants){
      if(leftTick<=0||rightTick<=0||Math.abs(leftTick-rightTick)>(participants>1?240L:500L))return false;
      return participants<=1||!left.equals(right);
   }

   public static String policyConsequence(String policy){return switch(policy){
      case "RESTORE_NAMES"->"La Matrice privilégie les voix humaines. Deux noms reviennent — mais ils appartiennent peut-être à la même vie recomposée.";
      case "KEEP_BOTH"->"La Matrice superpose les traces. Les coutures du montage deviennent visibles, avec les anciennes catégories toujours attachées aux personnes.";
      default->"La Matrice respecte les blancs. Aucune fausse identité n'est créée, mais une liaison décisive demeure impossible à prouver.";
   };}
}
