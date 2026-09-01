package fr.reivaxmc.progress.client.model;

import fr.reivaxmc.progress.entity.GuardianEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

/** Modèle GeckoLib générique : résout les ressources selon le nom du gardien (veilleur/protecteur). */
public class GuardianModel<T extends GuardianEntity> extends GeoModel<T> {
   private static ResourceLocation rl(String path) {
      return ResourceLocation.fromNamespaceAndPath("reivaxmc_progress", path);
   }

   @Override
   public ResourceLocation getModelResource(T entity) {
      return rl("geo/" + entity.geoName() + ".geo.json");
   }

   @Override
   public ResourceLocation getTextureResource(T entity) {
      return rl("textures/entity/" + entity.geoName() + ".png");
   }

   @Override
   public ResourceLocation getAnimationResource(T entity) {
      return rl("animations/" + entity.geoName() + ".animation.json");
   }
}
