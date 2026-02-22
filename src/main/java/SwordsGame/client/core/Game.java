package SwordsGame.client.core;

import SwordsGame.client.Cam;
import SwordsGame.client.Wld;
import SwordsGame.client.assets.Paths;
import SwordsGame.client.blocks.Reg;
import SwordsGame.client.graphics.Font;
import SwordsGame.client.graphics.Rdr;
import SwordsGame.client.graphics.TexLd;
import SwordsGame.client.ui.Hud;
import SwordsGame.client.ui.Cur;
import SwordsGame.client.ui.SelBox;
import SwordsGame.client.ui.SelArea;
import SwordsGame.client.utils.Disc;
import SwordsGame.server.ChMgr;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

public class Game {
    private Win window;
    private Rdr renderer;
    private Font font;
    private Hud hud;
    private Cur cursor;
    private Wld world;
    private Cam camera;
    private SelBox selectionRectangle;
    private final SelArea selArea = new SelArea();
    private ChMgr chunkManager;
    private boolean toggleVirtualResHeld = false;

    public static void main(String[] args) {
        new Game().start();
    }

    public void start() {
        window = new Win("SwordsGame");
        window.create();
        renderer = new Rdr();

        chunkManager = new ChMgr();
        world = new Wld();
        camera = new Cam();

        Disc.init();
        Reg.init();

        font = new Font(Paths.FONT_MAIN);
        hud = new Hud(font, 960, 540);
        hud.setPrimaryButtonText("butt...on");

        cursor = new Cur();
        selectionRectangle = new SelBox();
        TexLd.finishLoading();

        while (!window.shouldClose()) {
            float mouseX = window.getMouseRelX();
            float mouseY = window.getMouseRelY();
            boolean leftMouseHeld = glfwGetMouseButton(window.getHandle(), GLFW_MOUSE_BUTTON_LEFT) == GLFW_PRESS;

            selArea.update(window.getVirtualWidth(), window.getVirtualHeight());
            selectionRectangle.update(mouseX, mouseY, leftMouseHeld, selArea);

            boolean blockVerticalEdgeScroll = leftMouseHeld && selectionRectangle.isActive() && camera.isInVerticalEdgeZone(mouseY, window.getVirtualHeight());

            camera.update(window, chunkManager, renderer, blockVerticalEdgeScroll);
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
        Disc.shutdown();
        if (cursor != null) cursor.destroy();
        if (hud != null) hud.cleanup();
        if (font != null) font.destroy();
        Reg.destroy();
        TexLd.finishCleanup();
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
