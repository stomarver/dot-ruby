package SwordsGame.client.ui;

import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.lwjgl.opengl.GL11.glColor4f;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.glVertex2f;
import static org.lwjgl.opengl.GL11.GL_QUADS;

public class Button {
    private static final float NORMAL_RGB = 96.0f / 255.0f;
    private static final float HOVER_RGB = 128.0f / 255.0f;
    private static final float ALPHA = 0.6f;

    private final Text text;

    private final float x;
    private final float y;
    private final float width;
    private final float height;

    private float textScale;
    private boolean hovered;
    private String label;

    public Button(Text text, float x, float y, float width, float height, String label) {
        this.text = text;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.label = label == null ? "" : label;
        this.textScale = 1.0f;
    }

    public void setText(String label) {
        this.label = label == null ? "" : label;
    }

    public void setTextScale(float textScale) {
        this.textScale = Math.max(0.5f, textScale);
    }

    public void setHovered(boolean hovered) {
        this.hovered = hovered;
    }

    public boolean contains(float mouseX, float mouseY) {
        return mouseX >= x && mouseX <= (x + width)
                && mouseY >= y && mouseY <= (y + height);
    }

    public void render() {
        drawBody();
        drawLabel();
    }

    private void drawBody() {
        float rgb = hovered ? HOVER_RGB : NORMAL_RGB;

        glDisable(GL_TEXTURE_2D);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        glColor4f(rgb, rgb, rgb, ALPHA);
        glBegin(GL_QUADS);
        glVertex2f(x, y);
        glVertex2f(x + width, y);
        glVertex2f(x + width, y + height);
        glVertex2f(x, y + height);
        glEnd();

        glColor4f(1f, 1f, 1f, 1f);
    }

    private void drawLabel() {
        float centerX = x + (width / 2.0f) - 1.0f;
        float centerY = y + (height / 2.0f) - 1.0f;
        text.draw(label, Anchor.CENTER, Anchor.CENTER_Y, centerX, centerY, textScale);
    }
}
