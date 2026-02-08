package SwordsGame.core;

import SwordsGame.client.Camera;
import SwordsGame.client.World;
import SwordsGame.client.assets.Paths;
import SwordsGame.client.blocks.Registry;
import SwordsGame.client.graphics.Font;
import SwordsGame.client.graphics.Renderer;
import SwordsGame.client.graphics.TextureLoader;
import SwordsGame.server.ChunkManager;
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
    private boolean showChunkBounds = true;
    private boolean toggleBoundsHeld = false;
    private float sunYaw = 45.0f;
    private float sunPitch = 50.0f;


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

        font = new Font(Paths.FONT_MAIN);
        hud = new HUD(font, 960, 540);

        cursor = new Cursor();
        TextureLoader.finishLoading();

        while (!window.shouldClose()) {
            camera.update(window, chunkManager, renderer);
            updateSunControls(window.getHandle());
            updateBoundsToggle(window.getHandle());

            window.beginRenderToFBO();
            renderer.setup3D(window);

            glPushMatrix();
            camera.applyTransformations();
            renderer.applySunLight();

            world.render(chunkManager, camera);
            if (showChunkBounds) {
                world.renderChunkBounds(chunkManager, camera);
            }


            glPopMatrix();

            renderer.setup2D(window);
            if (hud != null) hud.render();

            float mouseX = window.getMouseRelX();
            float mouseY = window.getMouseRelY();

            cursor.updatePosition(mouseX, mouseY);
            cursor.render();

            window.endRenderToFBO();

            window.drawFBO();
            window.update();
        }
        cleanup();
    }

    private void updateSunControls(long windowHandle) {
        float step = 1.5f;
        if (glfwGetKey(windowHandle, GLFW_KEY_Y) == GLFW_PRESS) {
            sunYaw -= step;
        }
        if (glfwGetKey(windowHandle, GLFW_KEY_U) == GLFW_PRESS) {
            sunYaw += step;
        }
        sunYaw = normalizeAngle(sunYaw);
        renderer.setSunDirectionFromAngles(sunYaw, sunPitch);
    }

    private void updateBoundsToggle(long windowHandle) {
        boolean togglePressed = glfwGetKey(windowHandle, GLFW_KEY_B) == GLFW_PRESS;
        if (togglePressed && !toggleBoundsHeld) {
            showChunkBounds = !showChunkBounds;
        }
        toggleBoundsHeld = togglePressed;
    }

    private float normalizeAngle(float angle) {
        float result = angle % 360.0f;
        if (result < 0) {
            result += 360.0f;
        }
        return result;
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
