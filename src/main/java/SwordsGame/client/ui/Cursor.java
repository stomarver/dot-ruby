package SwordsGame.client.ui;

import SwordsGame.client.TextureLoader;
import SwordsGame.client.assets.Paths;

import static org.lwjgl.opengl.GL11.*;

public class Cursor {
    private final TextureLoader.Texture texture;
    private float x, y;
    private final int width = 16;
    private final int height = 16;
    private boolean hasTarget;

    public Cursor() {
        this.texture = TextureLoader.loadTexture(Paths.UI_CURSOR, true);
    }

    public void setHasTarget(boolean hasTarget) {
        this.hasTarget = hasTarget;
    }

    public void updatePosition(float mouseX, float mouseY) {
        this.x = mouseX;
        this.y = mouseY;
    }

    public void render() {
        if (texture == null) return;

        glPushAttrib(GL_ENABLE_BIT | GL_COLOR_BUFFER_BIT);

        glDisable(GL_DEPTH_TEST);
        glDisable(GL_LIGHTING);
        glEnable(GL_TEXTURE_2D);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        glBindTexture(GL_TEXTURE_2D, texture.id);
        if (hasTarget) {
            glColor4f(0.3f, 0.9f, 1.0f, 0.9f);
        } else {
            glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        }

        float size = hasTarget ? 18.0f : 16.0f;
        float half = size / 2.0f;
        float drawX = x - half;
        float drawY = y - half;

        glBegin(GL_QUADS);
        glTexCoord2f(0, 0); glVertex2f(drawX, drawY);
        glTexCoord2f(1, 0); glVertex2f(drawX + size, drawY);
        glTexCoord2f(1, 1); glVertex2f(drawX + size, drawY + size);
        glTexCoord2f(0, 1); glVertex2f(drawX, drawY + size);
        glEnd();

        glBindTexture(GL_TEXTURE_2D, 0);

        glPopAttrib();
    }

    public void destroy() {
        if (texture != null) {
            TextureLoader.deleteTexture(texture.id);
        }
    }
}
