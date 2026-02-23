package SwordsGame.client.core;

import SwordsGame.client.assets.Paths;
import SwordsGame.client.graphics.Font;
import SwordsGame.client.graphics.Renderer;
import SwordsGame.client.graphics.TexLoad;
import SwordsGame.client.ui.Anchor;
import SwordsGame.client.ui.Cursor;
import SwordsGame.client.ui.Dialog;
import SwordsGame.client.ui.Hud;
import SwordsGame.client.ui.HudLayoutRegistry;

import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;
import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.glfwGetMouseButton;

public class MainMenuState implements SessionState {
    private SessionContext context;
    private Window window;

    private Renderer renderer;
    private Font font;
    private Hud hud;
    private Cursor cursor;

    @Override
    public void onEnter(SessionContext context) {
        this.context = context;
        this.window = context.getWindow();
        window.setVirtualMouseClamp(false, 0, 0, window.getVirtualWidth() - 1f, window.getVirtualHeight());

        renderer = new Renderer();
        font = new Font(Paths.FONT_MAIN);
        hud = new Hud(font, 960, 540);
        hud.setPrimaryButtonText("menu");
        HudLayoutRegistry.registerDefaultPivots(hud);

        cursor = new Cursor();
        TexLoad.finishLoading();

        hud.setGlobalLoadingVisible(false);
        hud.setGlobalLoadingText("loading");
        hud.applyDialogLayout(HudLayoutRegistry.DIALOG_MAIN_MENU);
        hud.setDialogOpacity(1.0f, 1.0f);
        hud.toggleDialogAtPivot("", "menu.dialog", Anchor.CENTER, Anchor.CENTER_Y, 0, 0, 360, 220,
                Dialog.SelectionBlockMode.NONE);
    }

    @Override
    public void onExit(SessionState nextState) {
        if (cursor != null) cursor.destroy();
        if (nextState == null) {
            if (hud != null) hud.cleanup();
            if (font != null) font.destroy();
            TexLoad.finishCleanup();
        }
    }

    @Override
    public void update() {
        float mouseX = window.getMouseRelX();
        float mouseY = window.getMouseRelY();
        boolean leftMouseHeld = glfwGetMouseButton(window.getHandle(), GLFW_MOUSE_BUTTON_LEFT) == GLFW_PRESS;

        window.setVirtualMouseClamp(false, 0, 0, window.getVirtualWidth() - 1f, window.getVirtualHeight());
        hud.setVirtualCursor(mouseX, mouseY);
        String action = hud.pollDialogButtonClick(leftMouseHeld);
        if (action == null) {
            return;
        }
        switch (action) {
            case "start-session" -> {
                hud.setGlobalLoadingText("loading");
                hud.setGlobalLoadingVisible(true);
                context.getCommands().startScenario(context.isDebugProfile());
            }
            case "open-showcase" -> {
                hud.setGlobalLoadingText("loading");
                hud.setGlobalLoadingVisible(true);
                context.getCommands().openShowcase();
            }
            case "exit-app" -> {
                hud.setGlobalLoadingText("loading");
                hud.setGlobalLoadingVisible(true);
                context.getCommands().exitApplication();
            }
            default -> {
            }
        }
    }

    @Override
    public void render() {
        window.beginRenderToFBO();
        renderer.setup2D(window);

        hud.renderDialogOverlay();

        window.endRenderToFBO();
        window.drawFBO();

        window.setupOverlay2D();
        cursor.updatePosition(window.getMouseScreenX(), window.getMouseScreenY());
        cursor.render(cursor.getBaseSizePixels());
    }
}
