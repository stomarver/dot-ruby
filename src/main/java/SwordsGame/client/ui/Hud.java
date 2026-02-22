package SwordsGame.client.ui;

import static org.lwjgl.opengl.GL11.*;

import SwordsGame.client.assets.Paths;
import SwordsGame.client.ui.Txt.*;
import SwordsGame.client.graphics.Font;
import SwordsGame.client.graphics.Spr;
import SwordsGame.client.graphics.TexLd;

import java.util.ArrayList;
import java.util.List;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class Hud {
    private final int virtualWidth, virtualHeight, frameWidth;
    private final Txt text;
    private final Spr sprite;
    private final Msg messageSystem;
    private final List<TexLd.Texture> textures = new ArrayList<>();
    private final Inf info;
    private final Btn primaryButton;
    private String primaryButtonText = "Butt...on";
    private float virtualCursorX = -1f;
    private float virtualCursorY = -1f;
    private boolean primaryButtonHeld = false;

    private final TexLd.Texture charFrameTex;
    private final TexLd.Texture separatorTex;

    public Hud(Font font, int w, int h) {
        this.virtualWidth = w;
        this.virtualHeight = h;
        this.frameWidth = (w - 720) / 2;

        this.text = new Txt(font, w, h);
        this.sprite = new Spr(w, h);
        this.messageSystem = new Msg();
        this.info = new Inf(text);
        this.primaryButton = new Btn(text, w, h);

        this.charFrameTex = load(Paths.UI_CHAR_FRAME);
        this.separatorTex = load(Paths.UI_SEPARATOR);

        startTerminalThread();
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

    private TexLd.Texture load(String path) {
        TexLd.Texture t = TexLd.loadTexture(path, true);
        textures.add(t);
        return t;
    }

    public void render() {
        drawBorders();
        drawInterface();
    }

    private void drawBorders() {
        glDisable(GL_TEXTURE_2D);
        glColor3f(0, 0, 0);
        glBegin(GL_QUADS);
        glVertex2f(0, 0); glVertex2f(frameWidth, 0);
        glVertex2f(frameWidth, virtualHeight); glVertex2f(0, virtualHeight);
        glVertex2f(virtualWidth - frameWidth, 0); glVertex2f(virtualWidth, 0);
        glVertex2f(virtualWidth, virtualHeight); glVertex2f(virtualWidth - frameWidth, virtualHeight);
        glEnd();
        glColor4f(1, 1, 1, 1);
    }

    private void drawInterface() {
        sprite.draw(charFrameTex, Anc.LEFT, Anc.TOP, 0, 18, 2.0f);
        sprite.draw(separatorTex, Anc.LEFT, Anc.BOTTOM, 0, -28, 2.0f);

        text.draw("unit.name", Anc.LEFT, Anc.TOP, 10, 2, 1);
        info.renderDebug(1.0f);

        primaryButton.draw(primaryButtonText, Anc.LEFT, Anc.TOP, 10, 170, 100, 28, 1.0f, virtualCursorX, virtualCursorY);

        messageSystem.draw(text);
    }


    public void setCameraInfo(String info) {
        this.info.setCameraInfo(info);
    }

    public void setServerInfo(String info) {
        this.info.setServerInfo(info);
    }

    public void setPrimaryButtonText(String text) {
        this.primaryButtonText = text == null ? "" : text;
    }

    public void setVirtualCursor(float x, float y) {
        this.virtualCursorX = x;
        this.virtualCursorY = y;
    }


    public boolean consumePrimaryButtonClick(boolean mouseDown) {
        boolean hovered = primaryButton.contains(Anc.LEFT, Anc.TOP, 10, 170, 100, 28, virtualCursorX, virtualCursorY);
        boolean clicked = hovered && mouseDown && !primaryButtonHeld;
        primaryButtonHeld = mouseDown;
        return clicked;
    }

    public void cleanup() {
        for (TexLd.Texture t : textures) {
            TexLd.deleteTexture(t.id);
        }
        textures.clear();
    }
}
