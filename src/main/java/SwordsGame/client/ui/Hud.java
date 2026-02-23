package SwordsGame.client.ui;

import static org.lwjgl.opengl.GL11.*;

import SwordsGame.client.assets.Paths;
import SwordsGame.client.graphics.Font;
import SwordsGame.client.graphics.ImgReg;
import SwordsGame.client.graphics.Sprite;
import SwordsGame.client.graphics.TexLoad;
import SwordsGame.client.assets.Syn;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Hud {
    private final int virtualWidth, virtualHeight, frameWidth;
    private final Text text;
    private final Sprite sprite;
    private final Message messageSystem;
    private final List<TexLoad.Texture> textures = new ArrayList<>();
    private final Map<String, TexLoad.Texture> texturesByAlias = new HashMap<>();
    private final Info info;
    private final Button button;
    private final Dialog dialog;
    private String primaryButtonText = "Butt...on";
    private float virtualCursorX = -1f;
    private float virtualCursorY = -1f;
    private boolean primaryButtonHeld = false;
    private boolean dialogButtonHeld = false;
    private final Map<String, Anchor> pivots = new HashMap<>();
    private final Map<String, Object> uiState = new HashMap<>();
    private final List<UiEventListener> eventListeners = new ArrayList<>();
    private final Map<String, UiButtonBounds> baseButtons = new HashMap<>();

    private final HudScriptRunner uiScript;

    public Hud(Font font, int w, int h) {
        this.virtualWidth = w;
        this.virtualHeight = h;
        this.frameWidth = (w - 720) / 2;

        this.text = new Text(font, w, h);
        this.sprite = new Sprite(w, h);
        this.messageSystem = new Message();
        this.info = new Info(text);
        this.button = new Button(text, w, h);
        this.dialog = new Dialog(w, h);
        this.uiScript = new HudScriptRunner();

        loadAliased("char-frame", Paths.UI_CHAR_FRAME);
        loadAliased("separator", Paths.UI_SEPARATOR);

        startTerminalThread();
        setPivot("screen.left.top", Anchor.LEFT, Anchor.TOP, 0, 0);
        setPivot("screen.center", Anchor.CENTER, Anchor.CENTER_Y, 0, 0);
        setPivot("screen.right.center", Anchor.RIGHT, Anchor.CENTER_Y, 0, 0);
    }

    private void startTerminalThread() {
        Thread term = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(System.in, StandardCharsets.UTF_8))) {

                String line;
                while ((line = reader.readLine()) != null) {
                    if (!line.isEmpty()) {
                        messageSystem.add(line);
                    }
                }
            } catch (Exception e) {
            }
        });
        term.setDaemon(true);
        term.start();
    }

    private TexLoad.Texture loadAliased(String alias, String path) {
        TexLoad.Texture texture = ImgReg.reg(Syn.img(path).alphaKey());
        textures.add(texture);
        if (alias != null && !alias.isBlank()) {
            texturesByAlias.put(alias, texture);
        }
        return texture;
    }

    public void render() {
        renderBaseInterface();
        renderDialogOverlay();
    }

    public void renderBaseInterface() {
        drawBorders();
        drawInterface();
    }

    public void renderDialogOverlay() {
        dialog.renderBackground();
        dialog.renderContent(text, button, virtualCursorX, virtualCursorY);
    }

    private void drawBorders() {
        glDisable(GL_TEXTURE_2D);
        glColor3f(0, 0, 0);
        glBegin(GL_QUADS);
        glVertex2f(0, 0);
        glVertex2f(frameWidth, 0);
        glVertex2f(frameWidth, virtualHeight);
        glVertex2f(0, virtualHeight);
        glVertex2f(virtualWidth - frameWidth, 0);
        glVertex2f(virtualWidth, 0);
        glVertex2f(virtualWidth, virtualHeight);
        glVertex2f(virtualWidth - frameWidth, virtualHeight);
        glEnd();
        glColor4f(1, 1, 1, 1);
    }

    private void drawInterface() {
        Map<String, Object> context = uiContext();
        HudScriptRunner.BaseFrame frame = uiScript.evaluateBase(context);
        baseButtons.clear();

        for (HudScriptRunner.SpriteDef spriteDef : frame.sprites()) {
            TexLoad.Texture texture = texturesByAlias.get(spriteDef.texture());
            if (texture != null) {
                drawSpriteAtPivot(texture, spriteDef.pivot(), spriteDef.alignX(), spriteDef.alignY(), spriteDef.x(), spriteDef.y(), spriteDef.scale());
            }
        }

        for (HudScriptRunner.TextDef textDef : frame.texts()) {
            drawTextAtPivot(textDef.text(), textDef.pivot(), textDef.alignX(), textDef.alignY(), textDef.x(), textDef.y(), textDef.scale());
        }

        for (HudScriptRunner.ButtonDef buttonDef : frame.buttons()) {
            Anchor anchor = anchorAtPivot(buttonDef.pivot(), buttonDef.alignX(), buttonDef.alignY(), buttonDef.x(), buttonDef.y());
            float bx = anchor.x;
            float by = anchor.y;

            if (anchor.tx == Anchor.TypeX.CENTER) {
                bx -= buttonDef.width() / 2f;
            } else if (anchor.tx == Anchor.TypeX.RIGHT) {
                bx -= buttonDef.width();
            }
            if (anchor.ty == Anchor.TypeY.CENTER) {
                by -= buttonDef.height() / 2f;
            } else if (anchor.ty == Anchor.TypeY.BOTTOM) {
                by -= buttonDef.height();
            }

            button.drawAbsolute(buttonDef.label(), bx, by, buttonDef.width(), buttonDef.height(), buttonDef.scale(), virtualCursorX, virtualCursorY, !buttonDef.active());
            baseButtons.put(buttonDef.id(), new UiButtonBounds(buttonDef.id(), bx, by, buttonDef.width(), buttonDef.height(), buttonDef.active()));
        }

        info.renderDebug(1.0f);
        messageSystem.draw(text);
    }

    private Map<String, Object> uiContext() {
        Map<String, Object> context = new HashMap<>();
        context.put("state", new HashMap<>(uiState));
        context.put("primaryButtonText", primaryButtonText);
        return context;
    }

    public void setCameraInfo(String info) {
        this.info.setCameraInfo(info);
    }

    public void setTimeInfo(String info) {
        this.info.setTimeInfo(info);
    }

    public void setServerInfo(String info) {
        this.info.setServerInfo(info);
    }

    public void setPrimaryButtonText(String text) {
        this.primaryButtonText = text == null ? "" : text;
    }

    public void putUiState(String key, Object value) {
        if (key == null || key.isBlank()) {
            return;
        }
        uiState.put(key, value);
    }

    public boolean getUiStateBool(String key, boolean fallback) {
        Object value = uiState.get(key);
        if (value instanceof Boolean b) {
            return b;
        }
        if (value == null) {
            return fallback;
        }
        return Boolean.parseBoolean(String.valueOf(value));
    }

    public void setVirtualCursor(float x, float y) {
        this.virtualCursorX = x;
        this.virtualCursorY = y;
    }

    public boolean consumePrimaryButtonClick(boolean mouseDown) {
        UiButtonBounds primary = baseButtons.get("primary-button");
        if (primary == null || !primary.active) {
            primaryButtonHeld = mouseDown;
            return false;
        }
        boolean hovered = button.containsAbsolute(primary.x, primary.y, primary.width, primary.height, virtualCursorX, virtualCursorY);
        boolean clicked = hovered && mouseDown && !primaryButtonHeld;
        primaryButtonHeld = mouseDown;
        if (clicked) {
            dispatchUiEvent(primary.id);
        }
        return clicked;
    }

    public String pollDialogButtonClick(boolean mouseDown) {
        String hoveredId = dialog.getHoveredButtonId(button, virtualCursorX, virtualCursorY);
        boolean clicked = hoveredId != null && mouseDown && !dialogButtonHeld;
        dialogButtonHeld = mouseDown;
        if (clicked) {
            dispatchUiEvent(hoveredId);
        }
        return clicked ? hoveredId : null;
    }

    public void addUiEventListener(UiEventListener listener) {
        if (listener != null) {
            eventListeners.add(listener);
        }
    }

    private void dispatchUiEvent(String eventId) {
        if (eventId == null || eventId.isBlank()) {
            return;
        }
        for (UiEventListener listener : eventListeners) {
            listener.onUiEvent(eventId);
        }
    }

    public boolean isSelectionBlockedByDialog() {
        return dialog.blocksSelection(virtualCursorX, virtualCursorY);
    }

    public boolean isDialogVisible() {
        return dialog.isVisible();
    }

    public void hideDialog() {
        dialog.hide();
    }

    public void setDialogOpacity(float fillAlpha, float borderAlpha) {
        dialog.setOpacity(fillAlpha, borderAlpha);
    }

    public void resetDialogOpacity() {
        dialog.resetOpacity();
    }

    public void toggleDialog(String body,
                             Anchor.TypeX ax,
                             Anchor.TypeY ay,
                             float x,
                             float y,
                             float width,
                             float height,
                             Dialog.SelectionBlockMode blockMode) {
        dialog.toggle(body, ax, ay, x, y, width, height, blockMode);
    }

    public void setDialogContent(List<Dialog.TextSlot> textSlots, List<Dialog.ButtonSlot> buttonSlots) {
        dialog.setLayout(textSlots, buttonSlots);
    }

    public void applyDialogLayout(String dialogId) {
        HudScriptRunner.DialogFrame frame = uiScript.evaluateDialog(dialogId, uiContext());
        List<Dialog.TextSlot> textSlots = new ArrayList<>();
        for (HudScriptRunner.TextDef textDef : frame.texts()) {
            textSlots.add(new Dialog.TextSlot(
                    textDef.text(),
                    textDef.alignX(),
                    textDef.alignY(),
                    textDef.x(),
                    textDef.y(),
                    textDef.scale()
            ));
        }

        List<Dialog.ButtonSlot> buttonSlots = new ArrayList<>();
        for (HudScriptRunner.ButtonDef buttonDef : frame.buttons()) {
            buttonSlots.add(new Dialog.ButtonSlot(
                    buttonDef.id(),
                    buttonDef.label(),
                    buttonDef.alignX(),
                    buttonDef.alignY(),
                    buttonDef.x(),
                    buttonDef.y(),
                    buttonDef.width(),
                    buttonDef.height(),
                    buttonDef.scale(),
                    buttonDef.active()
            ));
        }
        dialog.setLayout(textSlots, buttonSlots);
    }

    public void setPivot(String id, Anchor pivot) {
        if (id == null || id.isBlank() || pivot == null) {
            return;
        }
        pivots.put(id, pivot);
    }

    public void setPivot(String id, Anchor.TypeX ax, Anchor.TypeY ay, float offsetX, float offsetY) {
        if (id == null || id.isBlank()) {
            return;
        }
        pivots.put(id, Anchor.screenPoint(virtualWidth, virtualHeight, ax, ay, offsetX, offsetY));
    }

    public Anchor getPivot(String id) {
        return pivots.get(id);
    }

    public Anchor anchorAtPivot(String pivotId, Anchor.TypeX ax, Anchor.TypeY ay, float offsetX, float offsetY) {
        return Anchor.pivotPoint(getPivot(pivotId), ax, ay, offsetX, offsetY);
    }

    public void drawTextAtPivot(String value, String pivotId, Anchor.TypeX ax, Anchor.TypeY ay, float offsetX, float offsetY, float scale) {
        text.draw(value == null ? "" : value, anchorAtPivot(pivotId, ax, ay, offsetX, offsetY), 0f, 0f, scale);
    }

    public void drawSpriteAtPivot(TexLoad.Texture texture, String pivotId, Anchor.TypeX ax, Anchor.TypeY ay, float offsetX, float offsetY, float scale) {
        if (texture == null) {
            return;
        }
        sprite.draw(texture, anchorAtPivot(pivotId, ax, ay, offsetX, offsetY), 0f, 0f, scale);
    }

    public void toggleDialogAtPivot(String body,
                                    String pivotId,
                                    Anchor.TypeX alignX,
                                    Anchor.TypeY alignY,
                                    float x,
                                    float y,
                                    float width,
                                    float height,
                                    Dialog.SelectionBlockMode blockMode) {
        dialog.toggle(body, getPivot(pivotId), alignX, alignY, x, y, width, height, blockMode);
    }

    public void cleanup() {
        for (TexLoad.Texture t : textures) {
            TexLoad.deleteTexture(t.id);
        }
        textures.clear();
    }

    private record UiButtonBounds(String id, float x, float y, float width, float height, boolean active) {
    }

    public interface UiEventListener {
        void onUiEvent(String eventId);
    }
}
