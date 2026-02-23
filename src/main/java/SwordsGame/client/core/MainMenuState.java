package SwordsGame.client.core;

import SwordsGame.client.assets.Paths;
import SwordsGame.client.graphics.Font;
import SwordsGame.client.graphics.Renderer;
import SwordsGame.client.graphics.TexLoad;
import SwordsGame.client.ui.Anchor;
import SwordsGame.client.ui.Cursor;
import SwordsGame.client.ui.Dialog;
import SwordsGame.client.ui.Hud;

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

        renderer = new Renderer();
        font = new Font(Paths.FONT_MAIN);
        hud = new Hud(font, 960, 540);
        hud.setPrimaryButtonText("menu");
        hud.setPivot("menu.dialog", Anchor.CENTER, Anchor.CENTER_Y, 0, 0);

        cursor = new Cursor();
        TexLoad.finishLoading();

        hud.setGlobalLoadingVisible(true);
        hud.setGlobalLoadingText("loading menu...");
        hud.applyDialogLayout("main.menu");
        hud.setDialogOpacity(1.0f, 1.0f);
        hud.toggleDialogAtPivot("", "menu.dialog", Anchor.CENTER, Anchor.CENTER_Y, 0, 0, 360, 220,
                Dialog.SelectionBlockMode.NONE);
        hud.setGlobalLoadingText("menu ready");
    }

    @Override
    public void onExit() {
        if (cursor != null) cursor.destroy();
        if (hud != null) hud.cleanup();
        if (font != null) font.destroy();
        TexLoad.finishCleanup();
    }

    @Override
    public void update() {
        float mouseX = window.getMouseRelX();
        float mouseY = window.getMouseRelY();
        boolean leftMouseHeld = glfwGetMouseButton(window.getHandle(), GLFW_MOUSE_BUTTON_LEFT) == GLFW_PRESS;

        hud.setVirtualCursor(mouseX, mouseY);
        String action = hud.pollDialogButtonClick(leftMouseHeld);
        if (action == null) {
            return;
        }
        switch (action) {
            case "start-session" -> context.getCommands().startScenario(false);
            case "exit-app" -> context.getCommands().exitApplication();
            default -> {
            }
        }
    }

    @Override
    public void render() {
        window.beginRenderToFBO();
        renderer.setup2D(window);

        hud.renderBaseInterface();
        hud.renderDialogOverlay();

        window.endRenderToFBO();
        window.drawFBO();

        window.setupOverlay2D();
        cursor.updatePosition(window.getMouseScreenX(), window.getMouseScreenY());
        cursor.render(cursor.getBaseSizePixels());
    }
}
