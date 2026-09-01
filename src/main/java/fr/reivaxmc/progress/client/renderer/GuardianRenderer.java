package fr.reivaxmc.progress.client.renderer;

import fr.reivaxmc.progress.client.model.GuardianModel;
import fr.reivaxmc.progress.entity.GuardianEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/**
 * Renderer GeckoLib générique. La couche emissive (AutoGlowingGeoLayer) est DÉSACTIVÉE pour l'instant :
 * on isole la cause du rendu tout noir (hypothèse = l'overlay emissif peignait le modèle en noir).
 * On la remettra une fois la texture de base confirmée à l'écran.
 */
public class GuardianRenderer<T extends GuardianEntity> extends GeoEntityRenderer<T> {
   public GuardianRenderer(EntityRendererProvider.Context context) {
      super(context, new GuardianModel<>());
   }
}
