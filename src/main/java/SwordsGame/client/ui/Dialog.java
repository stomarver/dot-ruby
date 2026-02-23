package SwordsGame.client.ui;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;

public class Dialog {
    private static final float BORDER_THICKNESS = 2.0f;
    private static final float BORDER_R = 0.5f;
    private static final float BORDER_G = 0.5f;
    private static final float BORDER_B = 1.0f;
    private static final float BORDER_A = 0.75f;
    private static final float FILL_R = 0.1f;
    private static final float FILL_G = 0.1f;
    private static final float FILL_B = 0.5f;
    private static final float FILL_A = 0.5f;

    private float fillAlpha = FILL_A;
    private float borderAlpha = BORDER_A;

    public enum SelectionBlockMode {
        NONE,
        DIALOG_AREA,
        FULL_SCREEN
    }

    private final int screenW;
    private final int screenH;

    private boolean visible;
    private Anchor.TypeX anchorX = Anchor.LEFT;
    private Anchor.TypeY anchorY = Anchor.TOP;
    private float offsetX;
    private float offsetY;
    private float width = 260;
    private float height = 120;
    private String text = "";
    private SelectionBlockMode selectionBlockMode = SelectionBlockMode.DIALOG_AREA;

    private final List<TextSlot> textSlots = new ArrayList<>();
    private final List<ButtonSlot> buttonSlots = new ArrayList<>();

    public Dialog(int screenW, int screenH) {
        this.screenW = screenW;
        this.screenH = screenH;
    }

    public void show(Anchor.TypeX ax, Anchor.TypeY ay, float x, float y, float width, float height) {
        show(text, ax, ay, x, y, width, height, selectionBlockMode);
    }

    public void show(String text,
                     Anchor.TypeX ax,
                     Anchor.TypeY ay,
                     float x,
                     float y,
                     float width,
                     float height,
                     SelectionBlockMode blockMode) {
        this.anchorX = ax == null ? Anchor.LEFT : ax;
        this.anchorY = ay == null ? Anchor.TOP : ay;
        this.offsetX = x;
        this.offsetY = y;
        this.width = Math.max(1f, width);
        this.height = Math.max(1f, height);
        this.text = text == null ? "" : text;
        this.selectionBlockMode = blockMode == null ? SelectionBlockMode.DIALOG_AREA : blockMode;
        this.visible = true;
    }

    public void toggle(String text,
                       Anchor.TypeX ax,
                       Anchor.TypeY ay,
                       float x,
                       float y,
                       float width,
                       float height,
                       SelectionBlockMode blockMode) {
        if (visible) {
            visible = false;
        } else {
            show(text, ax, ay, x, y, width, height, blockMode);
        }
    }

    public void hide() {
        this.visible = false;
    }

    public boolean isVisible() {
        return visible;
    }

    public boolean blocksSelection(float cursorX, float cursorY) {
        if (!visible) {
            return false;
        }
        return switch (selectionBlockMode) {
            case NONE -> false;
            case FULL_SCREEN -> true;
            case DIALOG_AREA -> resolveRect(anchorX, anchorY, offsetX, offsetY, width, height).contains(cursorX, cursorY);
        };
    }

    public Anchor resolveLocalAnchor(Anchor.TypeX ax, Anchor.TypeY ay, float x, float y) {
        Rect rect = resolveRect(anchorX, anchorY, offsetX, offsetY, width, height);
        float baseX = switch (ax == null ? Anchor.LEFT : ax) {
            case LEFT -> rect.x;
            case CENTER -> rect.x + rect.w / 2f;
            case RIGHT -> rect.x + rect.w;
        };
        float baseY = switch (ay == null ? Anchor.TOP : ay) {
            case TOP -> rect.y;
            case CENTER -> rect.y + rect.h / 2f;
            case BOTTOM -> rect.y + rect.h;
        };
        return new Anchor(ax == null ? Anchor.LEFT : ax, ay == null ? Anchor.TOP : ay, baseX + x, baseY + y);
    }

    public void setLayout(List<TextSlot> textSlots, List<ButtonSlot> buttonSlots) {
        this.textSlots.clear();
        this.buttonSlots.clear();
        if (textSlots != null) {
            this.textSlots.addAll(textSlots);
        }
        if (buttonSlots != null) {
            this.buttonSlots.addAll(buttonSlots);
        }
    }


    public void setOpacity(float fillAlpha, float borderAlpha) {
        this.fillAlpha = Math.max(0f, Math.min(1f, fillAlpha));
        this.borderAlpha = Math.max(0f, Math.min(1f, borderAlpha));
    }

    public void resetOpacity() {
        this.fillAlpha = FILL_A;
        this.borderAlpha = BORDER_A;
    }

    public String getHoveredButtonId(Button buttonRenderer, float cursorX, float cursorY) {
        if (!visible || buttonRenderer == null) {
            return null;
        }
        for (ButtonSlot slot : buttonSlots) {
            Rect rect = resolveButtonRect(slot);
            if (buttonRenderer.containsAbsolute(rect.x, rect.y, rect.w, rect.h, cursorX, cursorY)) {
                return slot.id;
            }
        }
        return null;
    }


    public void renderBackground() {
        if (!visible) return;

        Rect r = resolveRect(anchorX, anchorY, offsetX, offsetY, width, height);

        glDisable(GL_TEXTURE_2D);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        glColor4f(FILL_R, FILL_G, FILL_B, fillAlpha);
        glBegin(GL_QUADS);
        glVertex2f(r.x, r.y);
        glVertex2f(r.x + r.w, r.y);
        glVertex2f(r.x + r.w, r.y + r.h);
        glVertex2f(r.x, r.y + r.h);
        glEnd();

        glColor4f(BORDER_R, BORDER_G, BORDER_B, borderAlpha);
        drawBorderInside(r.x, r.y, r.w, r.h, BORDER_THICKNESS);

        glColor4f(1f, 1f, 1f, 1f);
    }

    public void renderContent(Text textRenderer, Button buttonRenderer, float cursorX, float cursorY) {
        if (!visible) {
            return;
        }
        if (textRenderer != null && !text.isEmpty()) {
            textRenderer.draw(text, resolveLocalAnchor(Anchor.LEFT, Anchor.TOP, 10, 10), 0f, 0f, 1.0f);
        }

        if (textRenderer != null) {
            for (TextSlot slot : textSlots) {
                Anchor anchor = resolveLocalAnchor(slot.anchorX, slot.anchorY, slot.offsetX, slot.offsetY);
                textRenderer.draw(slot.value, anchor, 0f, 0f, slot.scale);
            }
        }

        if (buttonRenderer != null) {
            for (ButtonSlot slot : buttonSlots) {
                Rect rect = resolveButtonRect(slot);
                buttonRenderer.drawAbsolute(slot.label, rect.x, rect.y, rect.w, rect.h, slot.scale, cursorX, cursorY, !slot.active);
            }
        }
    }

    private Rect resolveButtonRect(ButtonSlot slot) {
        Anchor anchor = resolveLocalAnchor(slot.anchorX, slot.anchorY, slot.offsetX, slot.offsetY);
        float buttonX = anchor.x;
        float buttonY = anchor.y;

        if (anchor.tx == Anchor.TypeX.CENTER) buttonX -= slot.width / 2f;
        else if (anchor.tx == Anchor.TypeX.RIGHT) buttonX -= slot.width;

        if (anchor.ty == Anchor.TypeY.CENTER) buttonY -= slot.height / 2f;
        else if (anchor.ty == Anchor.TypeY.BOTTOM) buttonY -= slot.height;

        return new Rect(buttonX, buttonY, slot.width, slot.height);
    }

    private Rect resolveRect(Anchor.TypeX ax, Anchor.TypeY ay, float x, float y, float w, float h) {
        float baseX = (ax == Anchor.TypeX.LEFT) ? 0 : (ax == Anchor.TypeX.CENTER ? screenW / 2f : screenW);
        float baseY = (ay == Anchor.TypeY.TOP) ? 0 : (ay == Anchor.TypeY.CENTER ? screenH / 2f : screenH);

        float left = baseX + x;
        float top = baseY + y;

        if (ax == Anchor.TypeX.CENTER) left -= w / 2f;
        else if (ax == Anchor.TypeX.RIGHT) left -= w;

        if (ay == Anchor.TypeY.CENTER) top -= h / 2f;
        else if (ay == Anchor.TypeY.BOTTOM) top -= h;

        return new Rect(left, top, w, h);
    }

    private void drawBorderInside(float x, float y, float w, float h, float t) {
        float innerW = Math.max(0f, w - t * 2f);
        float innerH = Math.max(0f, h - t * 2f);

        glBegin(GL_QUADS);
        glVertex2f(x, y); glVertex2f(x + w, y); glVertex2f(x + w, y + t); glVertex2f(x, y + t);
        glVertex2f(x, y + h - t); glVertex2f(x + w, y + h - t); glVertex2f(x + w, y + h); glVertex2f(x, y + h);
        glVertex2f(x, y + t); glVertex2f(x + t, y + t); glVertex2f(x + t, y + t + innerH); glVertex2f(x, y + t + innerH);
        glVertex2f(x + w - t, y + t); glVertex2f(x + w, y + t); glVertex2f(x + w, y + t + innerH); glVertex2f(x + w - t, y + t + innerH);
        glEnd();
    }


    public static TextSlot text(String value, Anchor.TypeX anchorX, Anchor.TypeY anchorY, float offsetX, float offsetY) {
        return new TextSlot(value, anchorX, anchorY, offsetX, offsetY, 1.0f);
    }

    public static ButtonSlot button(String id, String label, Anchor.TypeX anchorX, Anchor.TypeY anchorY,
                                    float offsetX, float offsetY, float width, float height) {
        return new ButtonSlot(id, label, anchorX, anchorY, offsetX, offsetY, width, height, 1.0f, true);
    }

    public static ButtonSlot button(String id, String label, Anchor.TypeX anchorX, Anchor.TypeY anchorY,
                                    float offsetX, float offsetY, float width, float height, boolean active) {
        return new ButtonSlot(id, label, anchorX, anchorY, offsetX, offsetY, width, height, 1.0f, active);
    }

    public record TextSlot(String value,
                           Anchor.TypeX anchorX,
                           Anchor.TypeY anchorY,
                           float offsetX,
                           float offsetY,
                           float scale) {
    }

    public record ButtonSlot(String id,
                             String label,
                             Anchor.TypeX anchorX,
                             Anchor.TypeY anchorY,
                             float offsetX,
                             float offsetY,
                             float width,
                             float height,
                             float scale,
                             boolean active) {
    }

    private record Rect(float x, float y, float w, float h) {
        private boolean contains(float px, float py) {
            return px >= x && px <= (x + w) && py >= y && py <= (y + h);
        }
    }
}
