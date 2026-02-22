package SwordsGame.client.core;

import SwordsGame.client.Camera;
import SwordsGame.client.World;
import SwordsGame.client.assets.Paths;
import SwordsGame.client.blocks.BlockRegistry;
import SwordsGame.client.graphics.Font;
import SwordsGame.client.graphics.Renderer;
import SwordsGame.client.graphics.TexLoad;
import SwordsGame.server.ChunkManager;
import SwordsGame.server.gameplay.FactionType;
import SwordsGame.server.ui.ServerUiComposer;
import SwordsGame.shared.protocol.ui.UiFrameState;
import SwordsGame.shared.protocol.ui.UiPanelState;
import SwordsGame.client.ui.Cursor;
import SwordsGame.client.ui.Hud;
import SwordsGame.client.ui.SelectionBox;
import SwordsGame.client.ui.SelectionArea;
import SwordsGame.client.utils.Discord;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

public class Debug {
    private Window window;
    private Renderer renderer;
    private Font font;
    private Hud hud;
    private Cursor cursor;
    private World world;
    private Camera camera;
    private SelectionBox selection;
    private final SelectionArea selArea = new SelectionArea();
    private ChunkManager chunkManager;
    private boolean showChunkBounds = false;
    private boolean toggleBoundsHeld = false;
    private boolean showDebugInfo = true;
    private boolean toggleDebugHeld = false;
    private boolean toggleVirtualResHeld = false;
    private static final float FOG_DISTANCE_STEP = 0.1f;
    private static final float FOG_DISTANCE_MIN = 0.4f;
    private static final float FOG_DISTANCE_MAX = 2.5f;
    private float fogDistanceMultiplier = 1.0f;
    private ServerUiComposer serverUiComposer;


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
        serverUiComposer = new ServerUiComposer();

        Discord.init();
        BlockRegistry.init();

        font = new Font(Paths.FONT_MAIN);
        hud = new Hud(font, 960, 540);
        hud.setPrimaryButtonText("deb...ug");

        cursor = new Cursor();
        selection = new SelectionBox();
        TexLoad.finishLoading();

        while (!window.shouldClose()) {
            float mouseX = window.getMouseRelX();
            float mouseY = window.getMouseRelY();
            boolean leftMouseHeld = glfwGetMouseButton(window.getHandle(), GLFW_MOUSE_BUTTON_LEFT) == GLFW_PRESS;

            selArea.update(window.getVirtualWidth(), window.getVirtualHeight());
            selection.update(mouseX, mouseY, leftMouseHeld, selArea);

            boolean blockVerticalEdgeScroll = leftMouseHeld && selection.isActive() && camera.isInVerticalEdgeZone(mouseY, window.getVirtualHeight());

            camera.update(window, chunkManager, renderer, blockVerticalEdgeScroll);
            renderer.setSunDirectionFromAngles(30.0f, 15.0f);
            updateFogDistanceControls(window.getHandle());
            renderer.setFogZoom(camera.getZoom() * fogDistanceMultiplier);
            updateBoundsToggle(window.getHandle());
            updateDebugToggle(window.getHandle());
            updateVirtualResolutionToggle(window.getHandle());
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

            renderer.applyScreenSpaceFog(window);
            renderer.setup2D(window);

            window.setVirtualMouseClamp(leftMouseHeld && selection.isActive(), selArea.minX(), selArea.minY(), selArea.maxX(), selArea.maxY());
            if (hud != null) {
                hud.setVirtualCursor(mouseX, mouseY);
                if (hud.consumePrimaryButtonClick(leftMouseHeld)) {
                    showDebugInfo = !showDebugInfo;
                }
                hud.render();
            }

            float selectionThickness = window.getVirtualUnitsForPhysicalPixels(2f);
            selection.render(selectionThickness);

            window.endRenderToFBO();

            window.drawFBO();

            window.setupOverlay2D();
            cursor.updatePosition(window.getMouseScreenX(), window.getMouseScreenY());
            cursor.render(cursor.getBaseSizePixels());

            window.update();
        }
        cleanup();
    }

    private void updateFogDistanceControls(long windowHandle) {
        if (glfwGetKey(windowHandle, GLFW_KEY_KP_ADD) == GLFW_PRESS) {
            fogDistanceMultiplier = clamp(fogDistanceMultiplier + FOG_DISTANCE_STEP, FOG_DISTANCE_MIN, FOG_DISTANCE_MAX);
        }
        if (glfwGetKey(windowHandle, GLFW_KEY_KP_SUBTRACT) == GLFW_PRESS) {
            fogDistanceMultiplier = clamp(fogDistanceMultiplier - FOG_DISTANCE_STEP, FOG_DISTANCE_MIN, FOG_DISTANCE_MAX);
        }
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
        if (!showDebugInfo) {
            hud.setCameraInfo("");
            hud.setServerInfo("");
            return;
        }
        hud.setCameraInfo(buildCameraInfo());

        UiFrameState frame = serverUiComposer.compose(chunkManager, FactionType.HUMANS);
        hud.setServerInfo(extractServerInfo(frame));
    }

    private String extractServerInfo(UiFrameState frame) {
        if (frame == null) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (UiPanelState panel : frame.getPanels()) {
            if ("world".equals(panel.getPanelId()) || "faction".equals(panel.getPanelId())) {
                if (builder.length() > 0) {
                    builder.append("\n\n");
                }
                builder.append(panel.getText());
            }
        }
        return builder.toString();
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
                "^2Camera^0\n^3pos^0 (%.1f, %.1f)\n^4chunk^0 (%d, %d)\n^1local^0 (%d, %d)\n^5fog^0 x%.2f [%.0f..%.0f]",
                camera.getX(), camera.getZ(), chunkX, chunkZ, localX, localZ,
                fogDistanceMultiplier, renderer.getFogStartDistance(), renderer.getFogEndDistance());
    }

    private float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
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
}
