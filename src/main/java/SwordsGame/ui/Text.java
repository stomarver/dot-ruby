package SwordsGame.ui;

import SwordsGame.client.graphics.Font;

import static org.lwjgl.opengl.GL11.*;
import java.util.Random;

public class Text {
    private final Font font;
    private final float screenW, screenH;

    public enum Shake { NONE, SLOW, MEDIUM, FAST }
    public enum Wave { NONE, SLOW, MEDIUM, FAST }
    public enum Crit { NONE, SLOW, MEDIUM, FAST }

    public Text(Font f, int w, int h) {
        this.font = f;
        this.screenW = (float)w;
        this.screenH = (float)h;
    }

    public void draw(String t, Anchor.TypeX ax, Anchor.TypeX ay, float x, float y, float s) {
        draw(t, ax, toTypeY(ay), x, y, s);
    }

    public void draw(String t, Anchor.TypeX ax, Anchor.TypeX ay, float x, float y, float s, Wave wv) {
        draw(t, ax, toTypeY(ay), x, y, s, wv);
    }

    public void draw(String t, Anchor.TypeX ax, Anchor.TypeY ay, float x, float y, float s) {
        drawInternal(t, buildAnchor(ax, ay), x, y, s, true, Shake.NONE, Wave.NONE, Crit.NONE, 0);
    }

    public void draw(String t, Anchor.TypeX ax, Anchor.TypeY ay, float x, float y, float s, Wave wv) {
        drawInternal(t, buildAnchor(ax, ay), x, y, s, true, Shake.NONE, wv, Crit.NONE, 0);
    }

    public void draw(String t, Anchor a, float x, float y, float s) {
        drawInternal(t, a, x, y, s, true, Shake.NONE, Wave.NONE, Crit.NONE, 0);
    }

    private void drawInternal(String txt, Anchor a, float ox, float oy, float scale,
                              boolean shad, Shake shk, Wave wav, Crit crt, float spc) {
        if (txt == null || txt.isEmpty()) return;

        float[] startCol = new float[4];
        glGetFloatv(GL_CURRENT_COLOR, startCol);
        float currentAlpha = startCol[3];

        float s = Math.max(0.1f, scale) * 2f;
        float h = font.getCharHeight() * s, gap = h * 0.5f, step = h + gap;
        String[] lines = txt.split("\n");
        float totalH = (lines.length * h) + ((lines.length - 1) * gap);

        float sy = a.y + oy;
        if (a.ty == Anchor.TypeY.CENTER) sy -= totalH / 2f;
        else if (a.ty == Anchor.TypeY.BOTTOM) sy -= totalH;

        float shadowOffset = scale * 2f;

        for (int i = 0; i < lines.length; i++) {
            float cy = sy + (i * step);
            float lw = calcW(lines[i], scale, spc);
            float rx = a.x + ox;

            if (a.tx == Anchor.TypeX.CENTER) rx -= lw / 2f;
            else if (a.tx == Anchor.TypeX.RIGHT) rx -= lw;

            float cry = 0;
            if (crt != null && crt != Crit.NONE) {
                long t = System.currentTimeMillis(), p = (crt == Crit.SLOW ? 1600 : crt == Crit.MEDIUM ? 800 : 400);
                double c = (double)(t % p) / p;
                cry = (c < 0.25 || (c >= 0.5 && c < 0.75)) ? -s/2f : (c >= 0.25 && c < 0.5) ? -s : 0;
            }

            if (shad) {
                glColor4f(0, 0, 0, currentAlpha * 0.5f);
                drIn(lines[i], rx + shadowOffset, cy + cry + shadowOffset, s, shk, wav, spc, true, currentAlpha);
            }

            glColor4f(startCol[0], startCol[1], startCol[2], currentAlpha);
            drIn(lines[i], rx, cy + cry, s, shk, wav, spc, false, currentAlpha);
        }

        glColor4f(startCol[0], startCol[1], startCol[2], startCol[3]);
    }

    private void drIn(String t, float x, float y, float s, Shake sh, Wave wv, float sp, boolean isShadow, float alpha) {
        glEnable(GL_TEXTURE_2D);
        glBindTexture(GL_TEXTURE_2D, font.getTextureID());
        glBegin(GL_QUADS);
        long tm = System.currentTimeMillis();
        float cx = x;

        for (int i = 0; i < t.length(); i++) {
            char c = t.charAt(i);
            if (c == '^' && i + 1 < t.length() && Character.isDigit(t.charAt(i + 1))) {
                if (!isShadow) chCol(t.charAt(i + 1), alpha);
                i++; continue;
            }

            Font.CharData d = font.getCharData(c);
            Font.DiacriticData di = font.getDiacriticData(c);
            if (di != null) d = font.getCharData(di.baseChar);
            if (d == null && c != ' ') continue;

            if (c == ' ') {
                cx += ((font.getCharData(' ') != null ? font.getCharData(' ').advance : 4f) + sp) * s;
                continue;
            }

            float tx = cx, ty = y;
            if (sh != null && sh != Shake.NONE) {
                long spd = (sh == Shake.FAST ? 16 : sh == Shake.MEDIUM ? 40 : 80);
                Random r = new Random((tm / spd) + i * 31L);
                tx += (r.nextInt(3) - 1) * (s / 2f);
                ty += (r.nextInt(3) - 1) * (s / 2f);
            }
            if (wv != null && wv != Wave.NONE) {
                double ws = (wv == Wave.FAST ? 0.01 : wv == Wave.MEDIUM ? 0.005 : 0.002);
                ty += (float)(Math.sin(tm * ws + i * 0.5) * 3f * (s / 2f));
            }

            drQ(d, tx, ty, s);
            if (di != null) drDQ(di, tx, ty, s);
            cx += (d.advance + sp) * s;
        }
        glEnd();
    }

    private void chCol(char code, float alpha) {
        glEnd();
        switch (code) {
            case '1': glColor4f(1.0f, 0.3f, 0.3f, alpha); break;
            case '2': glColor4f(0.2f, 1.0f, 0.2f, alpha); break;
            case '3': glColor4f(0.4f, 0.4f, 1.0f, alpha); break;
            case '4': glColor4f(1.0f, 1.0f, 0.0f, alpha); break;
            case '5': glColor4f(1.0f, 0.0f, 1.0f, alpha); break;
            default:  glColor4f(1.0f, 1.0f, 1.0f, alpha); break;
        }
        glBegin(GL_QUADS);
    }

    public float calcW(String ln, float s, float sp) {
        float w = 0, sc = s * 2f;
        for (int i = 0; i < ln.length(); i++) {
            char c = ln.charAt(i);
            if (c == '^' && i + 1 < ln.length() && Character.isDigit(ln.charAt(i + 1))) {
                i++; continue;
            }
            Font.CharData d = font.getCharData(c);
            float adv = (c == ' ') ? (font.getCharData(' ') != null ? font.getCharData(' ').advance : 4f) : (d != null ? d.advance : 0);
            w += (adv + sp) * sc;
        }
        return w;
    }

    private Anchor buildAnchor(Anchor.TypeX ax, Anchor.TypeY ay) {
        float bx = (ax == Anchor.TypeX.LEFT) ? 0 : (ax == Anchor.TypeX.CENTER ? screenW / 2f : screenW);
        float by = (ay == Anchor.TypeY.TOP) ? 0 : (ay == Anchor.TypeY.CENTER ? screenH / 2f : screenH);
        return new Anchor(ax, ay, bx, by);
    }

    private Anchor.TypeY toTypeY(Anchor.TypeX type) {
        return type == Anchor.TypeX.CENTER ? Anchor.TypeY.CENTER : Anchor.TypeY.TOP;
    }

    private void drQ(Font.CharData d, float x, float y, float s) {
        float u1 = (float)d.x/font.getTextureWidth(), v1 = (float)d.y/font.getTextureHeight();
        float u2 = (float)(d.x+font.getCharWidth())/font.getTextureWidth(), v2 = (float)(d.y+font.getCharHeight())/font.getTextureHeight();
        glTexCoord2f(u1, v1); glVertex2f(x, y);
        glTexCoord2f(u2, v1); glVertex2f(x+font.getCharWidth()*s, y);
        glTexCoord2f(u2, v2); glVertex2f(x+font.getCharWidth()*s, y+font.getCharHeight()*s);
        glTexCoord2f(u1, v2); glVertex2f(x, y+font.getCharHeight()*s);
    }

    private void drDQ(Font.DiacriticData d, float x, float y, float s) {
        float u1 = (float)d.diacriticX/font.getTextureWidth(), v1 = (float)d.diacriticY/font.getTextureHeight();
        float u2 = (float)(d.diacriticX+d.diacriticWidth)/font.getTextureWidth(), v2 = (float)(d.diacriticY+d.diacriticHeight)/font.getTextureHeight();
        float dx = x + (font.getCharWidth()-d.diacriticWidth)/2f*s - (s/2f), dy = y + d.offsetY*s;
        glTexCoord2f(u1, v1); glVertex2f(dx, dy);
        glTexCoord2f(u2, v1); glVertex2f(dx+d.diacriticWidth*s, dy);
        glTexCoord2f(u2, v2); glVertex2f(dx+d.diacriticWidth*s, dy+d.diacriticHeight*s);
        glTexCoord2f(u1, v2); glVertex2f(dx, dy+d.diacriticHeight*s);
    }
}
