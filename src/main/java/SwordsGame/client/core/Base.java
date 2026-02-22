package SwordsGame.client.core;

import SwordsGame.client.Camera;
import SwordsGame.client.World;
import SwordsGame.client.assets.Paths;
import SwordsGame.client.blocks.Registry;
import SwordsGame.client.graphics.Font;
import SwordsGame.client.graphics.Renderer;
import SwordsGame.client.graphics.TextureLoader;
import SwordsGame.client.ui.HUD;
import SwordsGame.client.ui.Cursor;
import SwordsGame.client.ui.BoxSel;
import SwordsGame.client.ui.BoxSelArea;
import SwordsGame.client.utils.Discord;
import SwordsGame.server.ChunkManager;
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
    private BoxSel selectionRectangle;
    private final BoxSelArea selArea = new BoxSelArea();
    private ChunkManager chunkManager;
    private boolean toggleVirtualResHeld = false;

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

        Discord.init();
        Registry.init();

        font = new Font(Paths.FONT_MAIN);
        hud = new HUD(font, 960, 540);
        hud.setPrimaryButtonText("butt...on");

        cursor = new Cursor();
        selectionRectangle = new BoxSel();
        TextureLoader.finishLoading();

        while (!window.shouldClose()) {
            camera.update(window, chunkManager, renderer);
            renderer.setSunDirectionFromAngles(30.0f, 15.0f);
            renderer.setFogZoom(camera.getZoom());
            updateVirtualResolutionToggle(window.getHandle());
            updateHudInfo();

            window.beginRenderToFBO();
            renderer.setup3D(window);

            glPushMatrix();
            camera.applyTransformations();
            renderer.applySunLight();

            world.render(chunkManager, camera);


            glPopMatrix();

            renderer.applyScreenSpaceFog(window);
            renderer.setup2D(window);

            float mouseX = window.getMouseRelX();
            float mouseY = window.getMouseRelY();
            boolean leftMouseHeld = glfwGetMouseButton(window.getHandle(), GLFW_MOUSE_BUTTON_LEFT) == GLFW_PRESS;

            selArea.update(window.getVirtualWidth(), window.getVirtualHeight());

            selectionRectangle.update(mouseX, mouseY, leftMouseHeld, selArea);
            window.setVirtualMouseClamp(leftMouseHeld && selectionRectangle.isActive(), selArea.minX(), selArea.minY(), selArea.maxX(), selArea.maxY());

            if (hud != null) {
                hud.setVirtualCursor(mouseX, mouseY);
                hud.render();
            }

            float selectionThickness = window.getVirtualUnitsForPhysicalPixels(2f);
            selectionRectangle.render(selectionThickness);

            window.endRenderToFBO();

            window.drawFBO();

            window.setupOverlay2D();
            cursor.updatePosition(window.getMouseScreenX(), window.getMouseScreenY());
            cursor.render(cursor.getBaseSizePixels());

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


    private void updateVirtualResolutionToggle(long windowHandle) {
        boolean togglePressed = glfwGetKey(windowHandle, GLFW_KEY_F7) == GLFW_PRESS;
        if (togglePressed && !toggleVirtualResHeld) {
            window.toggleVirtualResolution();
        }
        toggleVirtualResHeld = togglePressed;
    }

    private void updateHudInfo() {
        if (hud == null || camera == null || chunkManager == null) {
            return;
        }
        hud.setCameraInfo("");
        hud.setServerInfo("");
    }
}
