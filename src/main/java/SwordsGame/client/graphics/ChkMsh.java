package SwordsGame.client.graphics;

import java.util.Map;

import static org.lwjgl.opengl.GL11.*;

public class ChkMsh {
    private final Map<Integer, MshBuf> opaque;
    private final Map<Integer, MshBuf> transparent;
    private final Map<Integer, MshBuf> emissive;

    public ChkMsh(Map<Integer, MshBuf> opaque, Map<Integer, MshBuf> transparent, Map<Integer, MshBuf> emissive) {
        this.opaque = opaque;
        this.transparent = transparent;
        this.emissive = emissive;
    }

    public void render() {
        render(true);
    }

    public void render(boolean useColorArray) {
        renderBuffers(opaque, useColorArray);
        renderEmissive(useColorArray);
        renderTransparent(useColorArray);
    }

    public void destroy() {
        destroyBuffers(opaque);
        destroyBuffers(transparent);
        destroyBuffers(emissive);
    }

    private void renderBuffers(Map<Integer, MshBuf> buffers, boolean useColor) {
        for (Map.Entry<Integer, MshBuf> entry : buffers.entrySet()) {
            glBindTexture(GL_TEXTURE_2D, entry.getKey());
            entry.getValue().render(useColor);
        }
    }

    private void renderEmissive(boolean useColor) {
        if (emissive.isEmpty()) return;
        glDisable(GL_LIGHTING);
        renderBuffers(emissive, useColor);
        glEnable(GL_LIGHTING);
    }

    private void renderTransparent(boolean useColor) {
        if (transparent.isEmpty()) return;
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glDepthMask(false);
        renderBuffers(transparent, useColor);
        glDepthMask(true);
        glDisable(GL_BLEND);
    }

    private void destroyBuffers(Map<Integer, MshBuf> buffers) {
        for (MshBuf buffer : buffers.values()) {
            buffer.destroy();
        }
        buffers.clear();
    }
}
