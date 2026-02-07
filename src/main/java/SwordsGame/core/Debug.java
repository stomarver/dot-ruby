package SwordsGame.core;

import SwordsGame.client.*;
import SwordsGame.client.assets.TexturePaths;
import SwordsGame.client.blocks.Registry;
import SwordsGame.server.ChunkManager;
import SwordsGame.server.functions.Explosion;
import SwordsGame.ui.Cursor;
import SwordsGame.ui.HUD;
import SwordsGame.utils.Discord;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

public class Debug {
    private Window window;
    private Renderer renderer;
    private Font font;
    private HUD hud;
    private Cursor cursor;
    private World world;
    private Camera camera;
    private ChunkManager chunkManager;

    private double lastExplosionTime = 0;
    private final double explosionCooldown = 0.2;

    public static void main(String[] args) {
        new Debug().start();
    }

    public void start() {
        window = new Window("SwordsGame");
        window.create();
        renderer = new Renderer();

        chunkManager = new ChunkManager();
        world = new World();
        camera = new Camera();

        Discord.init();
        Registry.init();

        font = new Font(TexturePaths.FONT_MAIN);
        hud = new HUD(font, 960, 540);

        cursor = new Cursor();
        TextureLoader.finishLoading();

        while (!window.shouldClose()) {
            camera.update(window);

            int[] target = camera.getTargetBlockFromMouse(window,
                    chunkManager.getWorldSizeInChunks(),
                    chunkManager);

            double currentTime = glfwGetTime();
            if (glfwGetMouseButton(window.getHandle(), GLFW_MOUSE_BUTTON_1) == GLFW_PRESS) {
                if (currentTime - lastExplosionTime >= explosionCooldown && target != null) {
                    Explosion.createSphere(chunkManager, world, target[0], target[1], target[2]);
                    lastExplosionTime = currentTime;
                }
            }

            window.beginRenderToFBO();
            renderer.setup3D(window);

            glPushMatrix();
            camera.applyTransformations();

            world.render(chunkManager, camera);
            world.renderChunkBounds(chunkManager, camera);

            if (target != null) {
                world.renderSelection(target, chunkManager.getWorldSizeInChunks());
            }

            glPopMatrix();

            renderer.setup2D(window);
            if (hud != null) hud.render();

            float mouseX = window.getMouseRelX(window.getHandle());
            float mouseY = window.getMouseRelY(window.getHandle());
            cursor.updatePosition(mouseX, mouseY);
            cursor.render();

            window.endRenderToFBO();

            window.drawFBO();
            window.update();
        }
        cleanup();
    }

    private void cleanup() {
        Discord.shutdown();
        if (cursor != null) cursor.destroy();
        if (hud != null) hud.cleanup();
        if (font != null) font.destroy();
        Registry.destroy();
        TextureLoader.finishCleanup();
        window.destroy();
        System.exit(0);
    }
}
