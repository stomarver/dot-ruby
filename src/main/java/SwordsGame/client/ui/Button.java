package SwordsGame.client.ui;

import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_QUADS;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.lwjgl.opengl.GL11.glColor4f;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.glVertex2f;

public class Button {
    private static final float NORMAL_RGB = 96.0f / 255.0f;
    private static final float HOVER_RGB = 128.0f / 255.0f;
    private static final float ALPHA = 0.6f;

    private final Text text;
    private final float screenW;
    private final float screenH;

    public Button(Text text, int screenW, int screenH) {
        this.text = text;
        this.screenW = (float) screenW;
        this.screenH = (float) screenH;
    }


    public boolean contains(Anchor.TypeX ax,
                            Anchor.TypeY ay,
                            float x,
                            float y,
                            float width,
                            float height,
                            float cursorX,
                            float cursorY) {
        Anchor anchor = buildAnchor(ax, ay);
        float rx = anchor.x + x;
        float ry = anchor.y + y;

        if (anchor.tx == Anchor.TypeX.CENTER) rx -= width / 2f;
        else if (anchor.tx == Anchor.TypeX.RIGHT) rx -= width;

        if (anchor.ty == Anchor.TypeY.CENTER) ry -= height / 2f;
        else if (anchor.ty == Anchor.TypeY.BOTTOM) ry -= height;

        return contains(rx, ry, width, height, cursorX, cursorY);
    }

    public void draw(String label,
                     Anchor.TypeX ax,
                     Anchor.TypeY ay,
                     float x,
                     float y,
                     float width,
                     float height,
                     float textScale,
                     float cursorX,
                     float cursorY) {
        Anchor anchor = buildAnchor(ax, ay);

        float rx = anchor.x + x;
        float ry = anchor.y + y;

        if (anchor.tx == Anchor.TypeX.CENTER) rx -= width / 2f;
        else if (anchor.tx == Anchor.TypeX.RIGHT) rx -= width;

        if (anchor.ty == Anchor.TypeY.CENTER) ry -= height / 2f;
        else if (anchor.ty == Anchor.TypeY.BOTTOM) ry -= height;

        boolean hovered = contains(rx, ry, width, height, cursorX, cursorY);
        float rgb = hovered ? HOVER_RGB : NORMAL_RGB;

        glDisable(GL_TEXTURE_2D);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        glColor4f(rgb, rgb, rgb, ALPHA);
        glBegin(GL_QUADS);
        glVertex2f(rx, ry);
        glVertex2f(rx + width, ry);
        glVertex2f(rx + width, ry + height);
        glVertex2f(rx, ry + height);
        glEnd();

        glColor4f(1f, 1f, 1f, 1f);

        float centerX = rx + (width / 2f);
        float centerY = ry + (height / 2f);
        text.draw(label == null ? "" : label, new Anchor(Anchor.CENTER, Anchor.CENTER_Y, centerX, centerY), 0f, 0f, textScale);
    }

    private boolean contains(float x, float y, float width, float height, float mouseX, float mouseY) {
        return mouseX >= x && mouseX <= (x + width)
                && mouseY >= y && mouseY <= (y + height);
    }

    private Anchor buildAnchor(Anchor.TypeX ax, Anchor.TypeY ay) {
        float px = ax == Anchor.TypeX.LEFT ? 0f : ax == Anchor.TypeX.CENTER ? screenW / 2f : screenW;
        float py = ay == Anchor.TypeY.TOP ? 0f : ay == Anchor.TypeY.CENTER ? screenH / 2f : screenH;
        return new Anchor(ax, ay, px, py);
    }
}
