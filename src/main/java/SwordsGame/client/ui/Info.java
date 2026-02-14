package SwordsGame.client.ui;

public class Info {
    private static final float TEXT_Y_OFFSET = 10.0f;
    private static final float DEBUG_LINE_GAP = 1.0f;
    private static final float DEBUG_MODULE_GAP = 40.0f;
    private static final float DEBUG_X = 130.0f;
    private static final float DEBUG_Y = 1.0f;

    private final Text text;
    private String sunInfo = "";
    private String cameraInfo = "";
    private String serverInfo = "";

    public Info(Text text) {
        this.text = text;
    }

    public void setSunInfo(String info) {
        this.sunInfo = info == null ? "" : info;
    }

    public void setCameraInfo(String info) {
        this.cameraInfo = info == null ? "" : info;
    }

    public void setServerInfo(String info) {
        this.serverInfo = info == null ? "" : info;
    }

    public void renderDebug(float scale) {
        float currentY = DEBUG_Y + TEXT_Y_OFFSET;
        if (!sunInfo.isEmpty()) {
            currentY = drawDebugLines(sunInfo, DEBUG_X, currentY, scale);
            currentY += DEBUG_MODULE_GAP;
        }
        if (!cameraInfo.isEmpty()) {
            currentY = drawDebugLines(cameraInfo, DEBUG_X, currentY, scale);
            currentY += DEBUG_MODULE_GAP;
        }
        if (!serverInfo.isEmpty()) {
            drawDebugLines(serverInfo, DEBUG_X, currentY, scale);
        }
    }

    public float getTextYOffset() {
        return TEXT_Y_OFFSET;
    }

    private float drawDebugLines(String content, float x, float y, float scale) {
        String[] lines = content.split("\n");
        float step = text.getLineStep(scale) + DEBUG_LINE_GAP;
        float currentY = y;
        for (String line : lines) {
            text.draw(line, Anchor.LEFT, Anchor.TOP, x, currentY, scale);
            currentY += step;
        }
        return currentY - step + DEBUG_LINE_GAP;
    }
}
