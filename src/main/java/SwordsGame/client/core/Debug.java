package SwordsGame.client.core;

import SwordsGame.client.Camera;
import SwordsGame.client.World;
import SwordsGame.client.assets.Paths;
import SwordsGame.client.blocks.BlockRegistry;
import SwordsGame.client.graphics.Font;
import SwordsGame.client.graphics.Renderer;
import SwordsGame.client.graphics.TexLoad;
import SwordsGame.server.ChunkManager;
import SwordsGame.server.DayNightCycle;
import SwordsGame.server.gameplay.MythicCorePack;
import SwordsGame.server.gameplay.MythicFactionPack;
import SwordsGame.client.ui.Cursor;
import SwordsGame.client.ui.Hud;
import SwordsGame.client.ui.Dialog;
import SwordsGame.client.ui.SelectionBox;
import SwordsGame.client.ui.SelectionArea;
import SwordsGame.client.ui.Anchor;
import SwordsGame.client.utils.Discord;
import java.util.ArrayList;
import java.util.List;

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
    private boolean showCameraBlock = true;
    private boolean showTimeBlock = true;
    private boolean showRenderingBlock = true;
    private boolean showClientBlock = true;
    private boolean toggleDebugHeld = false;
    private boolean toggleVirtualResHeld = false;
    private DayNightCycle dayNightCycle;
    private float lastCycleTickSeconds = 0f;
    private static final float FOG_DISTANCE_STEP = 0.1f;
    private static final float FOG_DISTANCE_MIN = 0.4f;
    private static final float FOG_DISTANCE_MAX = 2.5f;
    private float fogDistanceMultiplier = 1.0f;
    private int fps = 0;
    private int fpsCounter = 0;
    private double fpsLastSampleSec = 0.0;


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
        dayNightCycle = new DayNightCycle();
        lastCycleTickSeconds = (float) glfwGetTime();
        fpsLastSampleSec = glfwGetTime();

        Discord.init();
        BlockRegistry.init();
        MythicCorePack.init();
        MythicFactionPack.init();

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
            boolean selectionBlockedByDialog = hud != null && hud.blocksSelectionAtCursor();
            selection.update(mouseX, mouseY, leftMouseHeld, selectionBlockedByDialog, selArea);

            boolean blockVerticalEdgeScroll = leftMouseHeld && selection.isActive() && camera.isInVerticalEdgeZone(mouseY, window.getVirtualHeight());

            float nowCycleTickSeconds = (float) glfwGetTime();
            float deltaCycleSeconds = nowCycleTickSeconds - lastCycleTickSeconds;
            lastCycleTickSeconds = nowCycleTickSeconds;
            dayNightCycle.update(window.getHandle(), deltaCycleSeconds);

            camera.update(window, chunkManager, renderer, blockVerticalEdgeScroll);
            renderer.setSunDirectionFromAngles(30.0f, 15.0f);
            renderer.setCycleTint(dayNightCycle.getNightBlend(), dayNightCycle.getOrangeBlend());
            renderer.setFogColor(dayNightCycle.getFogR(), dayNightCycle.getFogG(), dayNightCycle.getFogB());
            updateFogDistanceControls(window.getHandle());
            renderer.setFogZoom(camera.getZoom() * fogDistanceMultiplier);
            updateBoundsToggle(window.getHandle());
            updateDebugToggle(window.getHandle());
            updateVirtualResolutionToggle(window.getHandle());
            updateFpsCounter(glfwGetTime());
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
                    rebuildDebugDialogContent();
                    hud.setDialogOpacity(1.0f, 1.0f);
                    hud.toggleDialog("", Anchor.CENTER, Anchor.CENTER_Y, 0, 0, 620, 330,
                            Dialog.SelectionBlockMode.DIALOG_AREA);
                }

                String dialogButton = hud.consumeDialogButtonClick(leftMouseHeld);
                handleDialogButton(dialogButton);
                hud.renderBaseInterface();
            }

            float selectionThickness = window.getVirtualUnitsForPhysicalPixels(2f);
            selection.render(selectionThickness);

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
            hud.setTimeInfo("");
            hud.setServerInfo("");
            return;
        }
        String cameraAndRendering = (showRenderingBlock ? buildRenderingInfo() + "\n" : "") + (showCameraBlock ? buildCameraInfo() : "");
        String timeBlock = showTimeBlock ? buildTimeInfo() : "";
        String clientBlock = showClientBlock ? buildClientInfo() : "";

        hud.setCameraInfo(cameraAndRendering.trim());
        hud.setTimeInfo(timeBlock);
        hud.setServerInfo(clientBlock.trim());
    }

    private String buildTimeInfo() {
        return String.format(
                "# Time\n^3Day:^0 %d\n^2Clock:^0 %s / 38:00\n^5Phase:^0 %s",
                dayNightCycle.getUiDay(),
                dayNightCycle.getTimeLabel(),
                dayNightCycle.getPhaseLabel());
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
                "# Camera\n^3Position:^0 (%.1f, %.1f)\n^4Chunk:^0 (%d, %d)\n^1Local:^0 (%d, %d)",
                camera.getX(), camera.getZ(), chunkX, chunkZ, localX, localZ);
    }

    private String buildRenderingInfo() {
        return String.format(
                "# Rendering\n^5Fog:^0 x%.2f [%.0f..%.0f]\n^6FPS:^0 %d",
                fogDistanceMultiplier, renderer.getFogStartDistance(), renderer.getFogEndDistance(), fps);
    }

    private String buildClientInfo() {
        Runtime rt = Runtime.getRuntime();
        long usedMb = (rt.totalMemory() - rt.freeMemory()) / (1024L * 1024L);
        long totalMb = rt.totalMemory() / (1024L * 1024L);
        long maxMb = rt.maxMemory() / (1024L * 1024L);

        return String.format(
                "# Client\n^2Memory:^0 %dMB used / %dMB alloc\n^2Max Memory:^0 %dMB",
                usedMb, totalMb, maxMb);
    }

    private void handleDialogButton(String buttonId) {
        if (buttonId == null) {
            return;
        }
        switch (buttonId) {
            case "toggle-camera" -> showCameraBlock = !showCameraBlock;
            case "toggle-time" -> showTimeBlock = !showTimeBlock;
            case "toggle-rendering" -> showRenderingBlock = !showRenderingBlock;
            case "toggle-client" -> showClientBlock = !showClientBlock;
            case "toggle-all" -> {
                boolean enableAll = !(showCameraBlock && showTimeBlock && showRenderingBlock && showClientBlock);
                showCameraBlock = enableAll;
                showTimeBlock = enableAll;
                showRenderingBlock = enableAll;
                showClientBlock = enableAll;
            }
            case "close" -> { hud.hideDialog(); hud.resetDialogOpacity(); }
            default -> {
                return;
            }
        }
        rebuildDebugDialogContent();
    }

    private void rebuildDebugDialogContent() {
        if (hud == null) {
            return;
        }

        List<Dialog.TextSlot> textSlots = new ArrayList<>();

        List<Dialog.ButtonSlot> buttons = new ArrayList<>();
        buttons.add(Dialog.button("toggle-rendering", "rendering", Anchor.LEFT, Anchor.TOP, 18, 34, 210, 30, showRenderingBlock));
        buttons.add(Dialog.button("toggle-camera", "camera", Anchor.LEFT, Anchor.TOP, 18, 70, 210, 30, showCameraBlock));
        buttons.add(Dialog.button("toggle-time", "time", Anchor.LEFT, Anchor.TOP, 18, 106, 210, 30, showTimeBlock));
        buttons.add(Dialog.button("toggle-client", "client", Anchor.LEFT, Anchor.TOP, 18, 142, 210, 30, showClientBlock));

        boolean allEnabled = showRenderingBlock && showCameraBlock && showTimeBlock && showClientBlock;
        buttons.add(Dialog.button("toggle-all", "all", Anchor.LEFT, Anchor.BOTTOM, 18, -16, 110, 30, allEnabled));
        buttons.add(Dialog.button("close", "close", Anchor.RIGHT, Anchor.BOTTOM, -18, -16, 140, 30));

        hud.setDialogContent(textSlots, buttons);
    }

    private void updateFpsCounter(double nowSeconds) {
        fpsCounter++;
        if (nowSeconds - fpsLastSampleSec >= 1.0) {
            fps = fpsCounter;
            fpsCounter = 0;
            fpsLastSampleSec = nowSeconds;
        }
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
