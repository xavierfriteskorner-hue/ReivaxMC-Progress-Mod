package fr.reivaxmc.progress.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * Cinématique d'ouverture — désormais habillée avec les fonds peints (réveil → résonance → carte-titre),
 * en fondus enchaînés, avec les répliques d'origine et des bandes cinéma.
 */
public final class IntroCinematicScreen extends Screen {

    private final long start = System.currentTimeMillis();
    private static final long DURATION = 26000L;

    public IntroCinematicScreen() {
        super(Component.literal("REIVAX MC — Origines"));
    }

    @Override
    public void renderBackground(GuiGraphics g, int mx, int my, float pt) {
        g.fill(0, 0, this.width, this.height, 0xFF05070A);
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        long t = System.currentTimeMillis() - this.start;
        g.fill(0, 0, this.width, this.height, 0xFF05070A);

        // Fonds animés (fondus enchaînés)
        drawBg(g, ReivaxUi.CINE_REVEIL, phase(t, 0, 2000, 8000, 10000));
        drawBg(g, ReivaxUi.CINE_RESONANCE, phase(t, 8000, 10000, 17000, 19000));
        drawBg(g, ReivaxUi.CINE_TITLE, phase(t, 17000, 19000, 26000, 26000));

        // Bandes cinéma
        int bar = Math.max(14, this.height / 12);
        g.fill(0, 0, this.width, bar, 0xC0000000);
        g.fill(0, this.height - bar, this.width, this.height, 0xC0000000);

        int cy = this.height / 2;
        if (t >= 2800 && t < 7200) {
            text(g, "QUELQUE CHOSE A CHANGÉ.", cy + 44, fade(t, 2800, 900, 7200, 900), 0xE9DCC0);
        } else if (t >= 8800 && t < 15100) {
            text(g, "Le monde n'est plus tout à fait silencieux.", cy + 44, fade(t, 8800, 1100, 15100, 1000), 0xD8E0F0);
        } else if (t >= 15700 && t < 19600) {
            text(g, "Quelque chose s'est souvenu de vous.", cy + 44, fade(t, 15700, 900, 19600, 900), 0xEFE7E1);
        }

        if (t >= 19800 && t < 25800) {
            int a = fade(t, 19800, 1000, 25800, 1200);
            titleText(g, "ÂGE I", cy - 8, a, 0xC9A84C);
            titleText(g, "ORIGINES", cy + 22, a, 0xF5F0E8);
        }

        if (t >= DURATION) {
            Minecraft.getInstance().setScreen(null);
        }
    }

    private void drawBg(GuiGraphics g, ResourceLocation tex, float a) {
        if (a <= 0.01F) {
            return;
        }
        ReivaxUi.alpha(g, a);
        ReivaxUi.tex(g, tex, 0, 0, this.width, this.height, 1280, 720);
        ReivaxUi.resetColor(g);
    }

    /** Opacité d'un plan avec fondu d'entrée et de sortie. */
    private static float phase(long t, long inStart, long inEnd, long outStart, long outEnd) {
        if (t < inStart || t > outEnd) {
            return 0F;
        }
        if (t < inEnd) {
            return (float) (t - inStart) / (inEnd - inStart);
        }
        if (t <= outStart || outEnd == outStart) {
            return 1F;
        }
        return Math.max(0F, 1F - (float) (t - outStart) / (outEnd - outStart));
    }

    private void text(GuiGraphics g, String s, int y, int alpha, int rgb) {
        if (alpha <= 0) {
            return;
        }
        g.drawCenteredString(this.font, s, this.width / 2, y, (Math.min(255, alpha) << 24) | (rgb & 0xFFFFFF));
    }

    private void titleText(GuiGraphics g, String s, int y, int alpha, int rgb) {
        if (alpha <= 0) {
            return;
        }
        g.pose().pushPose();
        g.pose().translate(this.width / 2.0, y, 0.0);
        g.pose().scale(2.0F, 2.0F, 1.0F);
        g.drawString(this.font, s, -this.font.width(s) / 2, 0, (Math.min(255, alpha) << 24) | (rgb & 0xFFFFFF), true);
        g.pose().popPose();
    }

    private static int fade(long t, long inStart, long inDur, long outStart, long outDur) {
        double a = 1.0;
        if (t < inStart + inDur) {
            a = Math.max(0.0, Math.min(1.0, (double) (t - inStart) / inDur));
        }
        if (t > outStart - outDur) {
            a = Math.min(a, Math.max(0.0, (double) (outStart - t) / outDur));
        }
        return (int) (255.0 * a);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
