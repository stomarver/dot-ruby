package SwordsGame.client.ui;

import static org.lwjgl.opengl.GL11.*;

public class Selection {
    private boolean active;
    private float startX;
    private float startY;
    private float endX;
    private float endY;

    public void update(float mouseX, float mouseY, boolean mouseHeld) {
        if (mouseHeld) {
            if (!active) {
                active = true;
                startX = mouseX;
                startY = mouseY;
            }
            endX = mouseX;
            endY = mouseY;
        } else {
            active = false;
        }
    }

    public void render() {
        if (!active) {
            return;
        }

        float minX = Math.min(startX, endX);
        float minY = Math.min(startY, endY);
        float maxX = Math.max(startX, endX);
        float maxY = Math.max(startY, endY);

        if (maxX - minX < 1f || maxY - minY < 1f) {
            return;
        }

        glDisable(GL_TEXTURE_2D);
        glColor3f(1f, 1f, 1f);
        glLineWidth(1f);

        glBegin(GL_LINE_LOOP);
        glVertex2f(minX, minY);
        glVertex2f(maxX, minY);
        glVertex2f(maxX, maxY);
        glVertex2f(minX, maxY);
        glEnd();

        glColor4f(1f, 1f, 1f, 1f);
    }
}
