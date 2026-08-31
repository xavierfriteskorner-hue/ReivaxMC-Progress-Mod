package fr.reivaxmc.progress.story;

public final class StoryModeGate18F {
   private StoryModeGate18F() {
   }

   public static StoryStartStateData18F state(Object var0) {
      try {
         StoryStartStateData18F var1 = StoryStartStateData18F.getForServer(var0);
         var1.forceManaged();
         return var1;
      } catch (Throwable var2) {
         throw new IllegalStateException("Cannot resolve story mode", var2);
      }
   }

   public static boolean isManagedServer(Object var0) {
      try {
         return state(var0).snapshot().managed();
      } catch (Throwable var2) {
         return true;
      }
   }

   public static boolean allowLegacy(Object var0) {
      return false;
   }
}
