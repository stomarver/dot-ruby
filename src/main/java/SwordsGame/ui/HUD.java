package SwordsGame.ui;

import static org.lwjgl.opengl.GL11.*;

import SwordsGame.client.assets.Paths;
import SwordsGame.ui.Text.*;
import SwordsGame.client.graphics.Font;
import SwordsGame.client.graphics.Sprite;
import SwordsGame.client.graphics.TextureLoader;

import java.util.ArrayList;
import java.util.List;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class HUD {
    private final int virtualWidth, virtualHeight, frameWidth;
    private final Text text;
    private final Sprite sprite;
    private final Message messageSystem;
    private final List<TextureLoader.Texture> textures = new ArrayList<>();
    private String sunInfo = "";
    private String cameraInfo = "";
    private final float debugLineGap = 10.0f;

    private final TextureLoader.Texture charFrameTex;
    private final TextureLoader.Texture separatorTex;

    public HUD(Font font, int w, int h) {
        this.virtualWidth = w;
        this.virtualHeight = h;
        this.frameWidth = (w - 720) / 2;

        this.text = new Text(font, w, h);
        this.sprite = new Sprite(w, h);
        this.messageSystem = new Message();

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

    private TextureLoader.Texture load(String path) {
        TextureLoader.Texture t = TextureLoader.loadTexture(path, true);
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
        sprite.draw(charFrameTex, Anchor.LEFT, Anchor.TOP, 0, 18, 2.0f);
        sprite.draw(separatorTex, Anchor.LEFT, Anchor.BOTTOM, 0, -28, 2.0f);

        float textYOffset = -20.0f;
        float debugX = 135.0f;
        float sunY = 28.0f + textYOffset;
        float cameraBaseY = 44.0f + textYOffset;

        text.draw("Грунт", Anchor.LEFT, Anchor.TOP, 10, 2 + textYOffset, 1);
        float nextDebugY = sunY;
        if (!sunInfo.isEmpty()) {
            nextDebugY = drawDebugLines(sunInfo, debugX, sunY, 1.0f);
        }
        if (!cameraInfo.isEmpty()) {
            float cameraY = Math.max(cameraBaseY, nextDebugY + debugLineGap);
            drawDebugLines(cameraInfo, debugX, cameraY, 1.0f);
        }

        messageSystem.draw(text);
    }

    public void setSunInfo(String info) {
        this.sunInfo = info == null ? "" : info;
    }

    public void setCameraInfo(String info) {
        this.cameraInfo = info == null ? "" : info;
    }

    public void cleanup() {
        for (TextureLoader.Texture t : textures) {
            TextureLoader.deleteTexture(t.id);
        }
        textures.clear();
    }

    private float drawDebugLines(String content, float x, float y, float scale) {
        String[] lines = content.split("\n");
        float step = text.getLineStep(scale) + debugLineGap;
        float currentY = y;
        for (String line : lines) {
            text.draw(line, Anchor.LEFT, Anchor.TOP, x, currentY, scale);
            currentY += step;
        }
        return currentY - step;
    }
}
