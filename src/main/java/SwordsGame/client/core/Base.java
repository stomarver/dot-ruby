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
import SwordsGame.client.utils.Discord;
import SwordsGame.server.ChunkManager;
import SwordsGame.server.environment.Sun;
import SwordsGame.server.gameplay.FactionType;
import SwordsGame.server.ui.ServerUiComposer;
import SwordsGame.shared.protocol.ui.UiFrameState;
import SwordsGame.shared.protocol.ui.UiPanelState;
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
    private ServerUiComposer serverUiComposer;


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
        serverUiComposer = new ServerUiComposer();

        Discord.init();
        Registry.init();

        font = new Font(Paths.FONT_MAIN);
        hud = new HUD(font, 960, 540);
        hud.setPrimaryButtonText("Играть");

        cursor = new Cursor();
        TextureLoader.finishLoading();

        while (!window.shouldClose()) {
            camera.update(window, chunkManager, renderer);
            updateSunState();
            updateHudInfo();

            window.beginRenderToFBO();
            renderer.setup3D(window);

            glPushMatrix();
            camera.applyTransformations();
            renderer.applySunLight();

            world.render(chunkManager, camera);


            glPopMatrix();

            renderer.setup2D(window);

            float mouseX = window.getMouseRelX();
            float mouseY = window.getMouseRelY();
            if (hud != null) {
                hud.setVirtualCursor(mouseX, mouseY);
                hud.render();
            }

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

    private void updateSunState() {
        float[] sunDirection = sun.getDirection();
        renderer.setSunDirection(sunDirection[0], sunDirection[1], sunDirection[2]);
    }

    private void updateHudInfo() {
        if (hud == null || camera == null || chunkManager == null) {
            return;
        }
        UiFrameState frame = serverUiComposer.compose(sun, chunkManager, FactionType.HUMANS);
        hud.setSunInfo("");
        hud.setCameraInfo("");
        hud.setServerInfo(extractServerInfo(frame));
    }

    private String extractServerInfo(UiFrameState frame) {
        if (frame == null) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (UiPanelState panel : frame.getPanels()) {
            if ("sun".equals(panel.getPanelId()) || "world".equals(panel.getPanelId()) || "faction".equals(panel.getPanelId())) {
                if (builder.length() > 0) {
                    builder.append("\n\n");
                }
                builder.append(panel.getText());
            }
        }
        return builder.toString();
    }
}
