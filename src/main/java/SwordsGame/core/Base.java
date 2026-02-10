package SwordsGame.core;

import SwordsGame.client.Camera;
import SwordsGame.client.World;
import SwordsGame.client.assets.Paths;
import SwordsGame.client.blocks.Registry;
import SwordsGame.client.graphics.Font;
import SwordsGame.client.graphics.Renderer;
import SwordsGame.client.graphics.TextureLoader;
import SwordsGame.ui.HUD;
import SwordsGame.ui.Cursor;
import SwordsGame.utils.Discord;
import SwordsGame.server.ChunkManager;
import SwordsGame.server.environment.DayNightCycle;
import SwordsGame.server.environment.Sun;
import SwordsGame.server.tick.TickSystem;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

public class Base {
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


    public static void main(String[] args) {
        new Base().start();
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

        font = new Font(Paths.FONT_MAIN);
        hud = new HUD(font, 960, 540);

        cursor = new Cursor();
        TextureLoader.finishLoading();
        tickSystem.start(glfwGetTime());

        while (!window.shouldClose()) {
            camera.update(window, chunkManager, renderer);
            tickSystem.advance(glfwGetTime(), () -> dayNightCycle.tick());
            updateSunState(tickSystem.getInterpolationAlpha());
            updateHudInfo();

            window.beginRenderToFBO();
            renderer.setup3D(window);

            glPushMatrix();
            camera.applyTransformations();
            renderer.applySunLight();

            world.render(chunkManager, camera);


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

    private void updateSunState(float alpha) {
        sun.setYaw(dayNightCycle.getInterpolatedYaw(alpha));
        float[] sunDirection = sun.getDirection();
        renderer.setSunDirection(sunDirection[0], sunDirection[1], sunDirection[2]);
    }

    private void updateHudInfo() {
        if (hud == null || camera == null || chunkManager == null) {
            return;
        }
        hud.setSunInfo("");
        hud.setCameraInfo("");
    }
}
