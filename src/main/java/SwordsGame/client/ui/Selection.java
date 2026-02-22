package SwordsGame.client.ui;

import static org.lwjgl.opengl.GL11.*;

public class Selection {
    private final BoxSelectionMath boxSelectionMath = new BoxSelectionMath();
    private boolean active;

    public void update(float mouseX, float mouseY, boolean mouseHeld,
                       float minX, float minY, float maxX, float maxY) {
        float clampedX = boxSelectionMath.clamp(mouseX, minX, maxX);
        float clampedY = boxSelectionMath.clamp(mouseY, minY, maxY);

        if (mouseHeld) {
            if (!active) {
                if (!boxSelectionMath.isInside(mouseX, mouseY, minX, minY, maxX, maxY)) {
                    return;
                }
                active = true;
                boxSelectionMath.begin(clampedX, clampedY);
            }
            boxSelectionMath.moveEnd(clampedX, clampedY);
        } else {
            active = false;
        }
    }

    public boolean isActive() {
        return active;
    }


    public void render(float borderThickness) {
        if (!active) {
            return;
        }

        float minX = boxSelectionMath.minX();
        float minY = boxSelectionMath.minY();
        float maxX = boxSelectionMath.maxX();
        float maxY = boxSelectionMath.maxY();

        if (!boxSelectionMath.hasVisibleArea()) {
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
