package SwordsGame.client.ui;

import static org.lwjgl.opengl.GL11.*;

public class SelBox {
    private boolean active;
    private float startX;
    private float startY;
    private float endX;
    private float endY;

    public void update(float mouseX, float mouseY, boolean mouseHeld, SelArea area) {
        float clampedX = area.clampX(mouseX);
        float clampedY = area.clampY(mouseY);

        if (mouseHeld) {
            if (!active) {
                if (!area.contains(mouseX, mouseY)) {
                    return;
                }
                active = true;
                startX = clampedX;
                startY = clampedY;
            }
            endX = clampedX;
            endY = clampedY;
        } else {
            active = false;
        }
    }

    public boolean isActive() {
        return active;
    }

    private float snap(float value) {
        return (float) Math.floor(value);
    }

    public void render(float borderThickness) {
        if (!active) {
            return;
        }

        float minX = snap(Math.min(startX, endX));
        float minY = snap(Math.min(startY, endY));
        float maxX = snap(Math.max(startX, endX));
        float maxY = snap(Math.max(startY, endY));

        if (maxX - minX < 1f || maxY - minY < 1f) {
            return;
        }

        float thickness = Math.max(0.1f, borderThickness);

        glDisable(GL_TEXTURE_2D);
        glDisable(GL_LINE_SMOOTH);
        glColor3f(1f, 1f, 1f);

        glBegin(GL_QUADS);
        glVertex2f(minX, minY);
        glVertex2f(maxX, minY);
        glVertex2f(maxX, minY + thickness);
        glVertex2f(minX, minY + thickness);

        glVertex2f(minX, maxY - thickness);
        glVertex2f(maxX, maxY - thickness);
        glVertex2f(maxX, maxY);
        glVertex2f(minX, maxY);

        glVertex2f(minX, minY + thickness);
        glVertex2f(minX + thickness, minY + thickness);
        glVertex2f(minX + thickness, maxY - thickness);
        glVertex2f(minX, maxY - thickness);

        glVertex2f(maxX - thickness, minY + thickness);
        glVertex2f(maxX, minY + thickness);
        glVertex2f(maxX, maxY - thickness);
        glVertex2f(maxX - thickness, maxY - thickness);
        glEnd();

        glColor4f(1f, 1f, 1f, 1f);
    }
}
