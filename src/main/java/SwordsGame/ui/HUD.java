package SwordsGame.ui;

import static org.lwjgl.opengl.GL11.*;

import SwordsGame.client.graphics.Font;
import SwordsGame.client.graphics.Sprite;
import SwordsGame.client.graphics.TextureLoader;
import SwordsGame.ui.data.text.TextRegistry;

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
    private final Info info;
    private final ExampleTextOverlay exampleTextOverlay;

    private final TextureLoader.Texture charFrameTex;
    private final TextureLoader.Texture separatorTex;

    public HUD(Font font, int w, int h) {
        this.virtualWidth = w;
        this.virtualHeight = h;
        this.frameWidth = (w - 720) / 2;

        this.text = new Text(font, w, h);
        this.sprite = new Sprite(w, h);
        this.messageSystem = new Message();
        this.info = new Info(text);
        this.exampleTextOverlay = new ExampleTextOverlay(text);

        this.charFrameTex = load("textures/ui/char-frame.png");
        this.separatorTex = load("textures/ui/separator.png");

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
        TextureLoader.Texture t = TextureLoader.loadTexture(path);
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

        text.draw(d -> d.text(TextRegistry.get("hud.title", "Грунт")).leftTop().at(10, 2).size(1.0f));
        info.renderDebug(1.0f);
        exampleTextOverlay.renderDemo();

        messageSystem.draw(text);
    }

    public void setSunInfo(String info) {
        this.info.setSunInfo(info);
    }

    public void setCameraInfo(String info) {
        this.info.setCameraInfo(info);
    }

    public void cleanup() {
        for (TextureLoader.Texture t : textures) {
            TextureLoader.deleteTexture(t.id);
        }
        textures.clear();
    }
}
