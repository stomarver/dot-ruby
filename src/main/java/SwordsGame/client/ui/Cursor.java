package SwordsGame.client.ui;

import SwordsGame.client.graphics.TextureLoader;
import SwordsGame.client.assets.Paths;

import static org.lwjgl.opengl.GL11.*;

public class Cursor {
    private final TextureLoader.Texture texture;
    private float x, y;
    private final int width = 16;
    private final int height = 16;

    public Cursor() {
        this.texture = TextureLoader.loadTexture(Paths.UI_CURSOR, true);
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
        glColor4f(1.0f, 1.0f, 1.0f, 1.0f);

        glBegin(GL_QUADS);
        glTexCoord2f(0, 0); glVertex2f(x, y);
        glTexCoord2f(1, 0); glVertex2f(x + width, y);
        glTexCoord2f(1, 1); glVertex2f(x + width, y + height);
        glTexCoord2f(0, 1); glVertex2f(x, y + height);
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
