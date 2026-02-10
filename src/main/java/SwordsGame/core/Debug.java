package SwordsGame.core;

import SwordsGame.client.Camera;
import SwordsGame.client.World;
import SwordsGame.client.assets.Paths;
import SwordsGame.server.data.blocks.Registry;
import SwordsGame.client.data.blocks.RenderRegistry;
import SwordsGame.client.graphics.Font;
import SwordsGame.client.graphics.Renderer;
import SwordsGame.client.graphics.TextureLoader;
import SwordsGame.server.ChunkManager;
import SwordsGame.server.environment.DayNightCycle;
import SwordsGame.server.environment.Sun;
import SwordsGame.server.tick.TickSystem;
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
    private Sun sun;
    private DayNightCycle dayNightCycle;
    private TickSystem tickSystem;
    private boolean showChunkBounds = false;
    private boolean toggleBoundsHeld = false;
    private boolean resetSunHeld = false;
    private boolean showDebugInfo = true;
    private boolean toggleDebugHeld = false;


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
        sun = new Sun();
        dayNightCycle = new DayNightCycle(sun);
        tickSystem = new TickSystem(40);

        Discord.init();
        Registry.init();
        RenderRegistry.initFromServerDsl();

        font = new Font(Paths.FONT_MAIN);
        hud = new HUD(font, 960, 540);

        cursor = new Cursor();
        TextureLoader.finishLoading();
        tickSystem.start(glfwGetTime());

        while (!window.shouldClose()) {
            camera.update(window, chunkManager, renderer);
            tickSystem.advance(glfwGetTime(), () -> dayNightCycle.tick());
            updateSunControls(window.getHandle());
            updateBoundsToggle(window.getHandle());
            updateDebugToggle(window.getHandle());
            updateHudInfo();

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
            sun.rotateYaw(-step);
            dayNightCycle.syncFromSun();
        }
        if (glfwGetKey(windowHandle, GLFW_KEY_U) == GLFW_PRESS) {
            sun.rotateYaw(step);
            dayNightCycle.syncFromSun();
        }
        handleSunReset(windowHandle);
        sun.setYaw(dayNightCycle.getInterpolatedYaw(tickSystem.getInterpolationAlpha()));
        float[] sunDirection = sun.getDirection();
        renderer.setSunDirection(sunDirection[0], sunDirection[1], sunDirection[2]);
    }

    private void updateBoundsToggle(long windowHandle) {
        boolean togglePressed = glfwGetKey(windowHandle, GLFW_KEY_B) == GLFW_PRESS;
        if (togglePressed && !toggleBoundsHeld) {
            showChunkBounds = !showChunkBounds;
        }
        toggleBoundsHeld = togglePressed;
    }

    private void updateDebugToggle(long windowHandle) {
        boolean togglePressed = glfwGetKey(windowHandle, GLFW_KEY_F8) == GLFW_PRESS;
        if (togglePressed && !toggleDebugHeld) {
            showDebugInfo = !showDebugInfo;
        }
        toggleDebugHeld = togglePressed;
    }

    private void handleSunReset(long windowHandle) {
        boolean resetPressed = glfwGetKey(windowHandle, GLFW_KEY_R) == GLFW_PRESS;
        if (resetPressed && !resetSunHeld) {
            sun.reset();
            dayNightCycle.syncFromSun();
        }
        resetSunHeld = resetPressed;
    }

    private void updateHudInfo() {
        if (hud == null || camera == null || chunkManager == null) {
            return;
        }
        if (!showDebugInfo) {
            hud.setSunInfo("");
            hud.setCameraInfo("");
            return;
        }
        hud.setSunInfo(String.format("^2Sun^0\n^3yaw^0 %.1f\n^4pitch^0 %.1f", sun.getYaw(), sun.getPitch()));
        hud.setCameraInfo(buildCameraInfo());
    }

    private String buildCameraInfo() {
        float totalOffsetBlocks = chunkManager.getWorldSizeInBlocks() / 2.0f;
        int worldBlockX = (int) Math.floor((-camera.getX() / World.BLOCK_SCALE) + totalOffsetBlocks);
        int worldBlockZ = (int) Math.floor((-camera.getZ() / World.BLOCK_SCALE) + totalOffsetBlocks);
        int maxBlock = chunkManager.getWorldSizeInBlocks() - 1;
        worldBlockX = clamp(worldBlockX, 0, maxBlock);
        worldBlockZ = clamp(worldBlockZ, 0, maxBlock);
        int chunkX = worldBlockX / SwordsGame.server.Chunk.SIZE;
        int chunkZ = worldBlockZ / SwordsGame.server.Chunk.SIZE;
        int localX = worldBlockX % SwordsGame.server.Chunk.SIZE;
        int localZ = worldBlockZ % SwordsGame.server.Chunk.SIZE;
        return String.format(
                "^2Camera^0\n^3pos^0 (%.1f, %.1f)\n^4chunk^0 (%d, %d)\n^1local^0 (%d, %d)",
                camera.getX(), camera.getZ(), chunkX, chunkZ, localX, localZ);
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private void cleanup() {
        Discord.shutdown();
        if (cursor != null) cursor.destroy();
        if (hud != null) hud.cleanup();
        if (font != null) font.destroy();
        RenderRegistry.destroy();
        TextureLoader.finishCleanup();
        window.destroy();
        System.exit(0);
    }
}
