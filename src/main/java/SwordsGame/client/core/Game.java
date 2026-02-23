package SwordsGame.client.core;

import SwordsGame.client.Camera;
import SwordsGame.client.World;
import SwordsGame.client.assets.Paths;
import SwordsGame.client.blocks.BlockRegistry;
import SwordsGame.client.graphics.Font;
import SwordsGame.client.graphics.Renderer;
import SwordsGame.client.graphics.TexLoad;
import SwordsGame.client.ui.Hud;
import SwordsGame.client.ui.Cursor;
import SwordsGame.client.ui.SelectionBox;
import SwordsGame.client.ui.SelectionArea;
import SwordsGame.client.ui.Anchor;
import SwordsGame.client.ui.Dialog;
import SwordsGame.client.utils.Discord;
import SwordsGame.server.ChunkManager;
import SwordsGame.server.DayNightCycle;
import SwordsGame.server.gameplay.MythicCorePack;
import SwordsGame.server.gameplay.MythicFactionPack;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

public class Game {
    private Window window;
    private Renderer renderer;
    private Font font;
    private Hud hud;
    private Cursor cursor;
    private World world;
    private Camera camera;
    private SelectionBox selectionRectangle;
    private final SelectionArea selArea = new SelectionArea();
    private ChunkManager chunkManager;
    private boolean toggleVirtualResHeld = false;
    private DayNightCycle dayNightCycle;
    private float lastCycleTickSeconds = 0f;

    public static void main(String[] args) {
        new Game().start();
    }

    public void start() {
        window = new Window("SwordsGame");
        window.create();
        renderer = new Renderer();

        chunkManager = new ChunkManager();
        world = new World();
        camera = new Camera();
        dayNightCycle = new DayNightCycle();
        lastCycleTickSeconds = (float) glfwGetTime();

        Discord.init();
        BlockRegistry.init();
        MythicCorePack.init();
        MythicFactionPack.init();

        font = new Font(Paths.FONT_MAIN);
        hud = new Hud(font, 960, 540);
        hud.setPrimaryButtonText("butt...on");

        cursor = new Cursor();
        selectionRectangle = new SelectionBox();
        TexLoad.finishLoading();

        while (!window.shouldClose()) {
            float mouseX = window.getMouseRelX();
            float mouseY = window.getMouseRelY();
            boolean leftMouseHeld = glfwGetMouseButton(window.getHandle(), GLFW_MOUSE_BUTTON_LEFT) == GLFW_PRESS;

            selArea.update(window.getVirtualWidth(), window.getVirtualHeight());
            boolean selectionBlockedByDialog = hud != null && hud.blocksSelectionAtCursor();
            selectionRectangle.update(mouseX, mouseY, leftMouseHeld, selectionBlockedByDialog, selArea);

            boolean blockVerticalEdgeScroll = leftMouseHeld && selectionRectangle.isActive() && camera.isInVerticalEdgeZone(mouseY, window.getVirtualHeight());

            float nowCycleTickSeconds = (float) glfwGetTime();
            float deltaCycleSeconds = nowCycleTickSeconds - lastCycleTickSeconds;
            lastCycleTickSeconds = nowCycleTickSeconds;
            dayNightCycle.update(window.getHandle(), deltaCycleSeconds);

            camera.update(window, chunkManager, renderer, blockVerticalEdgeScroll);
            renderer.setSunDirectionFromAngles(30.0f, 15.0f);
            renderer.setCycleTint(dayNightCycle.getNightBlend(), dayNightCycle.getOrangeBlend());
            renderer.setFogColor(dayNightCycle.getFogR(), dayNightCycle.getFogG(), dayNightCycle.getFogB());
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
                if (hud.consumePrimaryButtonClick(leftMouseHeld)) {
                    hud.toggleDialog("^3dialog\ntech wiki overlay", Anchor.CENTER, Anchor.CENTER_Y, 0, 0, 420, 220,
                            Dialog.SelectionBlockMode.DIALOG_AREA);
                }
                hud.renderBaseInterface();
            }

            float selectionThickness = window.getVirtualUnitsForPhysicalPixels(2f);
            selectionRectangle.render(selectionThickness);

            if (hud != null) {
                hud.renderDialogOverlay();
            }

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
        BlockRegistry.destroy();
        TexLoad.finishCleanup();
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
        hud.setTimeInfo("");
        hud.setServerInfo("");
    }
}
