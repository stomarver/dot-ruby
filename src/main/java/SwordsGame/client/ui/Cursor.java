package SwordsGame.client.ui;

import SwordsGame.client.graphics.TexLoad;
import SwordsGame.client.assets.Paths;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.*;

public class Cursor {
    private final TexLoad.Texture texture;
    private float x, y;
    private static final float BASE_SIZE_PIXELS = 16f;

    public Cursor() {
        this.texture = TexLoad.loadTexture(Paths.UI_CURSOR, false);
        if (this.texture != null) {
            glBindTexture(GL_TEXTURE_2D, this.texture.id);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, 0);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, 0);
            glBindTexture(GL_TEXTURE_2D, 0);
        }
    }

    public void updatePosition(float mouseX, float mouseY) {
        this.x = mouseX;
        this.y = mouseY;
    }

    public void render(float sizeVirtualUnits) {
        if (texture == null) return;

        glPushAttrib(GL_ENABLE_BIT | GL_COLOR_BUFFER_BIT);

        glDisable(GL_DEPTH_TEST);
        glDisable(GL_LIGHTING);
        glEnable(GL_TEXTURE_2D);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        glBindTexture(GL_TEXTURE_2D, texture.id);
        glColor4f(1.0f, 1.0f, 1.0f, 1.0f);

        float size = Math.max(1f, Math.round(sizeVirtualUnits));
        float rx = (float) Math.floor(x);
        float ry = (float) Math.floor(y);

        glBegin(GL_QUADS);
        glTexCoord2f(0, 0); glVertex2f(rx, ry);
        glTexCoord2f(1, 0); glVertex2f(rx + size, ry);
        glTexCoord2f(1, 1); glVertex2f(rx + size, ry + size);
        glTexCoord2f(0, 1); glVertex2f(rx, ry + size);
        glEnd();

        glBindTexture(GL_TEXTURE_2D, 0);

        glPopAttrib();
    }

    public float getBaseSizePixels() {
        return BASE_SIZE_PIXELS;
    }

    public void destroy() {
        if (texture != null) {
            TexLoad.deleteTexture(texture.id);
        }
    }
}
