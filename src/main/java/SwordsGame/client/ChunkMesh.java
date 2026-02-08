package SwordsGame.client;

import java.util.Map;

import static org.lwjgl.opengl.GL11.*;

public class ChunkMesh {
    private final Map<Integer, MeshBuffer> opaque;
    private final Map<Integer, MeshBuffer> transparent;
    private final Map<Integer, MeshBuffer> emissive;
    private final Map<Integer, MeshBuffer> xray;

    public ChunkMesh(Map<Integer, MeshBuffer> opaque, Map<Integer, MeshBuffer> transparent, Map<Integer, MeshBuffer> emissive,
                     Map<Integer, MeshBuffer> xray) {
        this.opaque = opaque;
        this.transparent = transparent;
        this.emissive = emissive;
        this.xray = xray;
    }

    public void render() {
        render(true);
    }

    public void render(boolean useColorArray) {
        renderBuffers(opaque, useColorArray);
        renderEmissive(useColorArray);
        renderTransparent(useColorArray);
        renderXray(useColorArray);
    }

    public void destroy() {
        destroyBuffers(opaque);
        destroyBuffers(transparent);
        destroyBuffers(emissive);
        destroyBuffers(xray);
    }

    private void renderBuffers(Map<Integer, MeshBuffer> buffers, boolean useColor) {
        for (Map.Entry<Integer, MeshBuffer> entry : buffers.entrySet()) {
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

    private void renderXray(boolean useColor) {
        if (xray.isEmpty()) return;
        glDisable(GL_CULL_FACE);
        glDisable(GL_DEPTH_TEST);
        glDisable(GL_LIGHTING);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glDepthMask(false);
        renderBuffers(xray, useColor);
        glDepthMask(true);
        glDisable(GL_BLEND);
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_LIGHTING);
        glEnable(GL_CULL_FACE);
    }

    private void destroyBuffers(Map<Integer, MeshBuffer> buffers) {
        for (MeshBuffer buffer : buffers.values()) {
            buffer.destroy();
        }
        buffers.clear();
    }
}
