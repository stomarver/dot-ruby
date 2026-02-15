package SwordsGame.client.ui;

import static org.lwjgl.opengl.GL11.*;

public class Button {
    private final Text text;

    private float x;
    private float y;
    private float width;
    private float height;
    private float textScale;

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

    public void render() {
        drawBody();
        drawLabel();
    }

    private void drawBody() {
        glDisable(GL_TEXTURE_2D);

        glColor3f(0.08f, 0.08f, 0.08f);
        glBegin(GL_QUADS);
        glVertex2f(x, y);
        glVertex2f(x + width, y);
        glVertex2f(x + width, y + height);
        glVertex2f(x, y + height);
        glEnd();

        glColor3f(0.55f, 0.55f, 0.55f);
        glBegin(GL_LINE_LOOP);
        glVertex2f(x, y);
        glVertex2f(x + width, y);
        glVertex2f(x + width, y + height);
        glVertex2f(x, y + height);
        glEnd();

        glColor4f(1f, 1f, 1f, 1f);
    }

    private void drawLabel() {
        float centerX = x + (width / 2f);
        float centerY = y + (height / 2f);
        text.draw(label, Anchor.CENTER, Anchor.CENTER_Y, centerX, centerY, textScale);
    }
}
