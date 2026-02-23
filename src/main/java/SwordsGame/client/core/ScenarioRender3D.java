package SwordsGame.client.core;

import SwordsGame.client.Camera;
import SwordsGame.client.World;
import SwordsGame.client.graphics.Renderer;
import SwordsGame.server.ChunkManager;

import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glPushMatrix;

public final class ScenarioRender3D {
    private ScenarioRender3D() {
    }

    public static void render(Window window,
                              Renderer renderer,
                              Camera camera,
                              World world,
                              ChunkManager chunkManager,
                              boolean showChunkBounds) {
        renderer.setup3D(window);

        glPushMatrix();
        camera.applyTransformations();
        renderer.applySunLight();

        world.render(chunkManager, camera);
        if (showChunkBounds) {
            world.renderChunkBounds(chunkManager, camera);
        }

        glPopMatrix();

        renderer.applyScreenSpaceFog(window);
        renderer.setup2D(window);
    }
}
