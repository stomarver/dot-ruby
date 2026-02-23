package SwordsGame.client.ui;

import static org.lwjgl.opengl.GL11.*;

import java.util.ArrayList;
import java.util.List;

public class Info {
    private static final float PANEL_X = 128.0f;
    private static final float PANEL_START_Y = 10.0f;
    private static final float PANEL_WIDTH = 320.0f;
    private static final float PANEL_PADDING_X = 10.0f;
    private static final float PANEL_PADDING_Y = 8.0f;
    private static final float PANEL_GAP = 8.0f;
    private static final float HEADER_EXTRA_GAP = 2.0f;

    private final Text text;
    private String cameraInfo = "";
    private String timeInfo = "";
    private String serverInfo = "";

    public Info(Text text) {
        this.text = text;
    }

    public void setCameraInfo(String info) {
        this.cameraInfo = info == null ? "" : info;
    }

    public void setTimeInfo(String info) {
        this.timeInfo = info == null ? "" : info;
    }

    public void setServerInfo(String info) {
        this.serverInfo = info == null ? "" : info;
    }

    public void renderDebug(float scale) {
        float currentY = PANEL_START_Y;
        currentY = renderPanelIfPresent(cameraInfo, "CAMERA", currentY, scale);
        currentY = renderPanelIfPresent(timeInfo, "TIME", currentY, scale);
        renderPanelIfPresent(serverInfo, "SERVER", currentY, scale);
    }

    private float renderPanelIfPresent(String raw, String fallbackTitle, float y, float scale) {
        if (raw == null || raw.isBlank()) {
            return y;
        }

        String[] lines = raw.split("\\n");
        String title = fallbackTitle;
        int contentStart = 0;
        if (lines.length > 0 && lines[0].startsWith("# ")) {
            title = lines[0].substring(2).trim();
            contentStart = 1;
        }

        List<String> contentLines = new ArrayList<>();
        for (int i = contentStart; i < lines.length; i++) {
            if (!lines[i].isEmpty()) {
                contentLines.add(lines[i]);
            }
        }

        float lineStep = text.getLineStep(scale);
        float contentHeight = contentLines.size() * lineStep;
        float panelHeight = PANEL_PADDING_Y * 2f + lineStep + HEADER_EXTRA_GAP + contentHeight;

        drawPanelBackground(PANEL_X, y, PANEL_WIDTH, panelHeight);

        text.draw("^4" + title.toUpperCase(), Anchor.LEFT, Anchor.TOP, PANEL_X + PANEL_PADDING_X, y + PANEL_PADDING_Y, scale);

        float lineY = y + PANEL_PADDING_Y + lineStep + HEADER_EXTRA_GAP;
        for (String line : contentLines) {
            text.draw(line, Anchor.LEFT, Anchor.TOP, PANEL_X + PANEL_PADDING_X, lineY, scale);
            lineY += lineStep;
        }

        return y + panelHeight + PANEL_GAP;
    }

    private void drawPanelBackground(float x, float y, float w, float h) {
        glDisable(GL_TEXTURE_2D);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        glColor4f(0f, 0f, 0f, 0.35f);
        glBegin(GL_QUADS);
        glVertex2f(x, y);
        glVertex2f(x + w, y);
        glVertex2f(x + w, y + h);
        glVertex2f(x, y + h);
        glEnd();

        glColor4f(0.1f, 0.1f, 0.15f, 0.8f);
        glBegin(GL_QUADS);
        glVertex2f(x, y);
        glVertex2f(x + w, y);
        glVertex2f(x + w, y + 2f);
        glVertex2f(x, y + 2f);
        glEnd();

        glColor4f(1f, 1f, 1f, 1f);
    }
}
