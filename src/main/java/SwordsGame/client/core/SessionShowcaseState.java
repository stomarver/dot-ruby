package SwordsGame.client.core;

import SwordsGame.client.Camera;
import SwordsGame.client.World;
import SwordsGame.client.assets.Paths;
import SwordsGame.client.blocks.BlockRegistry;
import SwordsGame.client.graphics.Font;
import SwordsGame.client.graphics.Renderer;
import SwordsGame.client.graphics.TexLoad;
import SwordsGame.client.ui.Anchor;
import SwordsGame.client.ui.Cursor;
import SwordsGame.client.ui.Dialog;
import SwordsGame.client.ui.Hud;
import SwordsGame.client.ui.HudLayoutRegistry;
import SwordsGame.client.ui.SelectionArea;
import SwordsGame.client.ui.SelectionBox;
import SwordsGame.shared.world.BlockId;
import SwordsGame.server.ChunkManager;

import java.util.List;
import java.util.Set;

import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;
import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.glfwGetMouseButton;

public class SessionShowcaseState implements SessionState {
    private final boolean debugProfile;

    private SessionContext context;
    private Window window;

    private Renderer renderer;
    private Font font;
    private Hud hud;
    private Cursor cursor;
    private World world;
    private Camera camera;
    private ChunkManager chunkManager;
    private SelectionBox selection;
    private final SelectionArea selArea = new SelectionArea();

    public SessionShowcaseState(boolean debugProfile) {
        this.debugProfile = debugProfile;
    }

    @Override
    public void onEnter(SessionContext context) {
        this.context = context;
        this.window = context.getWindow();

        renderer = new Renderer();
        chunkManager = new ChunkManager();
        world = new World();
        camera = new Camera();
        selection = new SelectionBox();

        BlockRegistry.init();
        font = new Font(Paths.FONT_MAIN);
        hud = new Hud(font, 960, 540);
        HudLayoutRegistry.registerDefaultPivots(hud);
        hud.setPrimaryButtonText("menu");
        hud.putUiState("debugMode", debugProfile);

        cursor = new Cursor();
        TexLoad.finishLoading();

        injectShowcaseBlocks();
        applyShowcaseLayout();
    }

    @Override
    public void onExit(SessionState nextState) {
        if (cursor != null) cursor.destroy();
        BlockRegistry.destroy();
        TexLoad.evictCacheByPrefix("blocks/");

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

        selArea.update(window.getVirtualWidth(), window.getVirtualHeight());
        selection.update(mouseX, mouseY, leftMouseHeld, hud != null && hud.isSelectionBlockedByDialog(), selArea);

        boolean blockVerticalEdgeScroll = leftMouseHeld && selection.isActive() && camera.isInVerticalEdgeZone(mouseY, window.getVirtualHeight());
        boolean blockEdgeScroll = hud != null && hud.isEdgeScrollBlockedByDialog();
        camera.update(window, chunkManager, renderer, blockVerticalEdgeScroll, blockEdgeScroll);

        hud.setVirtualCursor(mouseX, mouseY);

        if (hud.consumeBaseButtonClick("primary-button", leftMouseHeld)) {
            context.getCommands().openMainMenu();
            return;
        }

        String action = hud.pollDialogButtonClick(leftMouseHeld);
        if (action != null) {
            handleDialogAction(action);
        }

        if (hud != null && hud.isCursorLockedByDialog()) {
            float[] dialogBounds = hud.getDialogBounds();
            if (dialogBounds != null && dialogBounds.length == 4) {
                float minX = Math.max(0f, dialogBounds[0]);
                float minY = Math.max(0f, dialogBounds[1]);
                float maxX = Math.min(window.getVirtualWidth() - 1f, dialogBounds[0] + dialogBounds[2]);
                float maxY = Math.min(window.getVirtualHeight(), dialogBounds[1] + dialogBounds[3]);
                window.setVirtualMouseClamp(true, minX, minY, maxX, maxY);
                return;
            }
        }

        if (leftMouseHeld && selection.isActive()) {
            window.setVirtualMouseClamp(true, selArea.minX(), selArea.minY(), selArea.maxX(), selArea.maxY());
        } else {
            window.setVirtualMouseClamp(false, 0, 0, window.getVirtualWidth() - 1f, window.getVirtualHeight());
        }
    }

    @Override
    public void render() {
        window.beginRenderToFBO();

        ScenarioRender3D.render(window, renderer, camera, world, chunkManager, false);
        ScenarioRenderUi.render(window, hud, selection, true);

        hud.drawTextAtPivot("^2Showcase^0 session", "screen.left.top", Anchor.LEFT, Anchor.TOP, 130, 10, 1.0f);
        hud.drawTextAtPivot("Text scale 1.0", "screen.left.top", Anchor.LEFT, Anchor.TOP, 130, 30, 1.0f);
        hud.drawTextAtPivot("Text scale 2.0", "screen.left.top", Anchor.LEFT, Anchor.TOP, 130, 52, 2.0f);
        hud.drawTextAtPivot("Multiline:\n^3TOP^0 / ^2CENTER^0 / ^1BOTTOM", "screen.left.top", Anchor.LEFT, Anchor.TOP, 130, 88, 1.0f);

        window.endRenderToFBO();
        window.drawFBO();

        window.setupOverlay2D();
        cursor.updatePosition(window.getMouseScreenX(), window.getMouseScreenY());
        cursor.render(cursor.getBaseSizePixels());
    }

    private void applyShowcaseLayout() {
        hud.setGlobalLoadingVisible(false);
        hud.setDialogOpacity(0.72f, 1.0f);
        hud.applyDialogLayout(HudLayoutRegistry.DIALOG_SHOWCASE_MENU);
        hud.toggleDialogAtPivot("", "screen.center", Anchor.CENTER, Anchor.CENTER_Y, 0, 0, 380, 250,
                Dialog.SelectionBlockMode.NONE, Set.of(Dialog.DialogFlag.BLOCK_EDGE_SCROLL));
    }

    private void handleDialogAction(String action) {
        switch (action) {
            case "showcase-open-none" -> openDialogVariant("No flags", Set.of(), Dialog.SelectionBlockMode.NONE,
                    "showcase.dialog.left", 300, 170, 0.35f);
            case "showcase-open-cursor-lock" -> openDialogVariant("Lock cursor", Set.of(Dialog.DialogFlag.LOCK_CURSOR_TO_DIALOG),
                    Dialog.SelectionBlockMode.NONE, "showcase.dialog.left", 300, 170, 0.55f);
            case "showcase-open-edge-block" -> openDialogVariant("Block edge scroll", Set.of(Dialog.DialogFlag.BLOCK_EDGE_SCROLL),
                    Dialog.SelectionBlockMode.NONE, "showcase.dialog.right", 300, 170, 0.55f);
            case "showcase-open-all" -> openDialogVariant("Cursor + edge + fullscreen block",
                    Set.of(Dialog.DialogFlag.BLOCK_EDGE_SCROLL, Dialog.DialogFlag.LOCK_CURSOR_TO_DIALOG),
                    Dialog.SelectionBlockMode.FULL_SCREEN, "screen.center", 420, 220, 0.75f);
            case "showcase-main-dialog" -> applyShowcaseLayout();
            case "showcase-main-menu" -> context.getCommands().openMainMenu();
            case "close" -> applyShowcaseLayout();
            default -> {
            }
        }
    }

    private void openDialogVariant(String title,
                                   Set<Dialog.DialogFlag> flags,
                                   Dialog.SelectionBlockMode blockMode,
                                   String pivot,
                                   float width,
                                   float height,
                                   float fillAlpha) {
        hud.setDialogContent(
                List.of(
                        new Dialog.TextSlot("^2" + title, Anchor.CENTER, Anchor.TOP, 0, 16, 1.0f),
                        new Dialog.TextSlot("Flags: " + flags, Anchor.LEFT, Anchor.TOP, 12, 48, 1.0f),
                        new Dialog.TextSlot("Selection mode: " + blockMode, Anchor.LEFT, Anchor.TOP, 12, 68, 1.0f),
                        new Dialog.TextSlot("Dialog params demo", Anchor.CENTER, Anchor.BOTTOM, 0, -12, 1.0f)
                ),
                List.of(
                        new Dialog.ButtonSlot("showcase-main-dialog", "back to showcase", Anchor.CENTER, Anchor.BOTTOM, 0, -10, 200, 26, 1.0f, true)
                )
        );
        hud.setDialogOpacity(fillAlpha, 1.0f);
        hud.toggleDialogAtPivot("", pivot, Anchor.CENTER, Anchor.CENTER_Y, 0, 0, width, height, blockMode, flags);
    }

    private void injectShowcaseBlocks() {
        int c = chunkManager.getWorldRadiusBlocks();
        int y = 8;

        for (int x = c - 2; x <= c + 2; x++) {
            for (int z = c - 2; z <= c + 2; z++) {
                chunkManager.setBlockAtWorld(x, y - 1, z, BlockId.COBBLE);
            }
        }

        chunkManager.setBlockAtWorld(c - 2, y, c, BlockId.COBBLE);
        chunkManager.setBlockAtWorld(c, y, c, BlockId.GRASS);
        chunkManager.setBlockAtWorld(c + 2, y, c, BlockId.STONE);
    }
}
