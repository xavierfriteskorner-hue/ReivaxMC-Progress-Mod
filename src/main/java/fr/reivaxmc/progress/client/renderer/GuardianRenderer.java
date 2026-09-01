package fr.reivaxmc.progress.client.renderer;

import fr.reivaxmc.progress.client.model.GuardianModel;
import fr.reivaxmc.progress.entity.GuardianEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

/**
 * Renderer GeckoLib générique + couche emissive (yeux / gravures / noyau qui brillent).
 * L'emissive utilise le fichier `<texture>_glowmask.png` (nom EXIGÉ par GeckoLib 4 ; avant
 * je l'avais nommé `_glowing` -> introuvable -> modèle rendu tout noir).
 */
public class GuardianRenderer<T extends GuardianEntity> extends GeoEntityRenderer<T> {
   public GuardianRenderer(EntityRendererProvider.Context context) {
      super(context, new GuardianModel<>());
      this.addRenderLayer(new AutoGlowingGeoLayer<>(this));
   }
}
