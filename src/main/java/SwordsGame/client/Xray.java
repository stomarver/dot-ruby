package SwordsGame.client;

import java.util.Map;

import static org.lwjgl.opengl.GL11.*;

public final class Xray {
    private Xray() {
    }

    public static void render(Map<Integer, MeshBuffer> buffers, boolean useColor) {
        if (buffers.isEmpty()) return;
        glDisable(GL_CULL_FACE);
        glDisable(GL_DEPTH_TEST);
        glDisable(GL_LIGHTING);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glDepthMask(false);
        for (Map.Entry<Integer, MeshBuffer> entry : buffers.entrySet()) {
            glBindTexture(GL_TEXTURE_2D, entry.getKey());
            entry.getValue().render(useColor);
        }
        glDepthMask(true);
        glDisable(GL_BLEND);
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_LIGHTING);
        glEnable(GL_CULL_FACE);
    }
}
