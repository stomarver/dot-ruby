package SwordsGame.client.ui;

import static org.lwjgl.opengl.GL11.*;

public class Dialog {
    private static final float BORDER_THICKNESS = 2.0f;
    private static final float BORDER_R = 0.75f;
    private static final float BORDER_G = 0.75f;
    private static final float BORDER_B = 0.75f;
    private static final float FILL_R = 0.5f;
    private static final float FILL_G = 0.5f;
    private static final float FILL_B = 0.5f;
    private static final float FILL_A = 0.65f;

    private final int screenW;
    private final int screenH;

    private boolean visible;
    private Anchor.TypeX anchorX = Anchor.LEFT;
    private Anchor.TypeY anchorY = Anchor.TOP;
    private float offsetX;
    private float offsetY;
    private float width = 260;
    private float height = 120;

    public Dialog(int screenW, int screenH) {
        this.screenW = screenW;
        this.screenH = screenH;
    }

    public void show(Anchor.TypeX ax, Anchor.TypeY ay, float x, float y, float width, float height) {
        this.anchorX = ax == null ? Anchor.LEFT : ax;
        this.anchorY = ay == null ? Anchor.TOP : ay;
        this.offsetX = x;
        this.offsetY = y;
        this.width = Math.max(1f, width);
        this.height = Math.max(1f, height);
        this.visible = true;
    }

    public void toggle(Anchor.TypeX ax, Anchor.TypeY ay, float x, float y, float width, float height) {
        if (visible) {
            visible = false;
        } else {
            show(ax, ay, x, y, width, height);
        }
    }

    public void hide() {
        this.visible = false;
    }

    public void render() {
        if (!visible) return;

        Rect r = resolveRect(anchorX, anchorY, offsetX, offsetY, width, height);

        glDisable(GL_TEXTURE_2D);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        glColor4f(FILL_R, FILL_G, FILL_B, FILL_A);
        glBegin(GL_QUADS);
        glVertex2f(r.x, r.y);
        glVertex2f(r.x + r.w, r.y);
        glVertex2f(r.x + r.w, r.y + r.h);
        glVertex2f(r.x, r.y + r.h);
        glEnd();

        glColor4f(BORDER_R, BORDER_G, BORDER_B, 1.0f);
        drawBorder(r.x, r.y, r.w, r.h, BORDER_THICKNESS);

        glColor4f(1f, 1f, 1f, 1f);
    }

    private Rect resolveRect(Anchor.TypeX ax, Anchor.TypeY ay, float x, float y, float w, float h) {
        float baseX = (ax == Anchor.TypeX.LEFT) ? 0 : (ax == Anchor.TypeX.CENTER ? screenW / 2f : screenW);
        float baseY = (ay == Anchor.TypeY.TOP) ? 0 : (ay == Anchor.TypeY.CENTER ? screenH / 2f : screenH);

        float left = baseX + x;
        float top = baseY + y;

        if (ax == Anchor.TypeX.CENTER) left -= w / 2f;
        else if (ax == Anchor.TypeX.RIGHT) left -= w;

        if (ay == Anchor.TypeY.CENTER) top -= h / 2f;
        else if (ay == Anchor.TypeY.BOTTOM) top -= h;

        return new Rect(left, top, w, h);
    }

    private void drawBorder(float x, float y, float w, float h, float t) {
        glBegin(GL_QUADS);
        glVertex2f(x, y); glVertex2f(x + w, y); glVertex2f(x + w, y + t); glVertex2f(x, y + t);
        glVertex2f(x, y + h - t); glVertex2f(x + w, y + h - t); glVertex2f(x + w, y + h); glVertex2f(x, y + h);
        glVertex2f(x, y); glVertex2f(x + t, y); glVertex2f(x + t, y + h); glVertex2f(x, y + h);
        glVertex2f(x + w - t, y); glVertex2f(x + w, y); glVertex2f(x + w, y + h); glVertex2f(x + w - t, y + h);
        glEnd();
    }

    private record Rect(float x, float y, float w, float h) {}
}
