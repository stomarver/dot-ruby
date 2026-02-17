package SwordsGame.client.ui;

import static org.lwjgl.opengl.GL11.*;

public class Selection {
    private boolean active;
    private float startX;
    private float startY;
    private float endX;
    private float endY;

    public void update(float mouseX, float mouseY, boolean mouseHeld,
                       float minX, float minY, float maxX, float maxY) {
        float clampedX = clamp(mouseX, minX, maxX);
        float clampedY = clamp(mouseY, minY, maxY);

        if (mouseHeld) {
            if (!active) {
                if (!isInside(mouseX, mouseY, minX, minY, maxX, maxY)) {
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

    private boolean isInside(float x, float y, float minX, float minY, float maxX, float maxY) {
        return x >= minX && x <= maxX && y >= minY && y <= maxY;
    }

    private float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    private float snapToVirtualPixel(float value) {
        return (float) Math.floor(value);
    }

    public void render(float borderThickness) {
        if (!active) {
            return;
        }

        float minX = snapToVirtualPixel(Math.min(startX, endX));
        float minY = snapToVirtualPixel(Math.min(startY, endY));
        float maxX = snapToVirtualPixel(Math.max(startX, endX));
        float maxY = snapToVirtualPixel(Math.max(startY, endY));

        if (maxX - minX < 1f || maxY - minY < 1f) {
            return;
        }

        float thickness = Math.max(0.1f, borderThickness);

        glDisable(GL_TEXTURE_2D);
        glDisable(GL_LINE_SMOOTH);
        glColor3f(1f, 1f, 1f);

        glBegin(GL_QUADS);
        // Top edge
        glVertex2f(minX, minY);
        glVertex2f(maxX, minY);
        glVertex2f(maxX, minY + thickness);
        glVertex2f(minX, minY + thickness);

        // Bottom edge
        glVertex2f(minX, maxY - thickness);
        glVertex2f(maxX, maxY - thickness);
        glVertex2f(maxX, maxY);
        glVertex2f(minX, maxY);

        // Left edge
        glVertex2f(minX, minY + thickness);
        glVertex2f(minX + thickness, minY + thickness);
        glVertex2f(minX + thickness, maxY - thickness);
        glVertex2f(minX, maxY - thickness);

        // Right edge
        glVertex2f(maxX - thickness, minY + thickness);
        glVertex2f(maxX, minY + thickness);
        glVertex2f(maxX, maxY - thickness);
        glVertex2f(maxX - thickness, maxY - thickness);
        glEnd();

        glColor4f(1f, 1f, 1f, 1f);
    }
}
