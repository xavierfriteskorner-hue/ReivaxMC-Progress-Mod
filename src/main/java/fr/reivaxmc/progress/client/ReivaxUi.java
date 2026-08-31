package fr.reivaxmc.progress.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

/**
 * Utilitaire d'affichage des visuels ReivaxMC (cadre, emblème, cinématique, onglets, Voix).
 * Tout passe par UNE seule méthode blit (part) : si la signature devait changer, un seul point à ajuster.
 */
public final class ReivaxUi {

    private static ResourceLocation gui(String n) {
        return ResourceLocation.fromNamespaceAndPath("reivaxmc_progress", "textures/gui/" + n);
    }

    public static final ResourceLocation PANEL = gui("panel.png");            // 512x512
    public static final ResourceLocation EMBLEM = gui("emblem.png");          // 256x256
    public static final ResourceLocation VOICE = gui("voice_panel.png");      // 768x256
    public static final ResourceLocation CINE_TITLE = gui("cine_title.png");  // 1280x720
    public static final ResourceLocation CINE_REVEIL = gui("cine_reveil.png");
    public static final ResourceLocation CINE_RESONANCE = gui("cine_resonance.png");
    public static final ResourceLocation[] TABS = {
            gui("tab_campagne.png"), gui("tab_quetes.png"), gui("tab_progression.png"),
            gui("tab_recompenses.png"), gui("tab_chronologie.png"), gui("tab_archives.png")
    };

    /** Dessine une région source (su,sv,sw,sh) d'une texture (texW,texH), étirée dans (dx,dy,dw,dh). */
    public static void part(GuiGraphics g, ResourceLocation tex, int dx, int dy, int dw, int dh,
                            int su, int sv, int sw, int sh, int texW, int texH) {
        if (dw <= 0 || dh <= 0) {
            return;
        }
        RenderSystem.enableBlend();
        g.blit(tex, dx, dy, dw, dh, (float) su, (float) sv, sw, sh, texW, texH);
    }

    /** Dessine une texture entière étirée dans (x,y,w,h). */
    public static void tex(GuiGraphics g, ResourceLocation t, int x, int y, int w, int h, int texW, int texH) {
        part(g, t, x, y, w, h, 0, 0, texW, texH, texW, texH);
    }

    /** Applique une teinte/opacité (r=g=b=1, a=alpha) pour un fondu, puis à remettre à 1 après. */
    public static void alpha(GuiGraphics g, float a) {
        g.setColor(1.0F, 1.0F, 1.0F, Math.max(0.0F, Math.min(1.0F, a)));
    }

    public static void resetColor(GuiGraphics g) {
        g.setColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    /**
     * Cadre 9-tranches (coins fixes, bords étirés, centre laissé transparent).
     * @param texSize taille de la texture carrée (ex. 512)
     * @param corner  taille du coin en pixels source (ex. 96)
     */
    public static void frame(GuiGraphics g, ResourceLocation t, int x, int y, int w, int h, int texSize, int corner) {
        int c = corner;
        int mid = texSize - 2 * c;
        // Coins
        part(g, t, x, y, c, c, 0, 0, c, c, texSize, texSize);
        part(g, t, x + w - c, y, c, c, texSize - c, 0, c, c, texSize, texSize);
        part(g, t, x, y + h - c, c, c, 0, texSize - c, c, c, texSize, texSize);
        part(g, t, x + w - c, y + h - c, c, c, texSize - c, texSize - c, c, c, texSize, texSize);
        // Bords
        part(g, t, x + c, y, w - 2 * c, c, c, 0, mid, c, texSize, texSize);
        part(g, t, x + c, y + h - c, w - 2 * c, c, c, texSize - c, mid, c, texSize, texSize);
        part(g, t, x, y + c, c, h - 2 * c, 0, c, c, mid, texSize, texSize);
        part(g, t, x + w - c, y + c, c, h - 2 * c, texSize - c, c, c, mid, texSize, texSize);
    }

    private ReivaxUi() {}
}
