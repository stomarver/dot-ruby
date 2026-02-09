package SwordsGame.ui;

public class Info {
    private static final float TEXT_Y_OFFSET = -20.0f;
    private static final float DEBUG_LINE_GAP = 10.0f;

    private final Text text;
    private String sunInfo = "";
    private String cameraInfo = "";

    public Info(Text text) {
        this.text = text;
    }

    public void setSunInfo(String info) {
        this.sunInfo = info == null ? "" : info;
    }

    public void setCameraInfo(String info) {
        this.cameraInfo = info == null ? "" : info;
    }

    public void renderDebug(float x, float sunY, float cameraBaseY, float scale) {
        float nextDebugY = sunY + TEXT_Y_OFFSET;
        if (!sunInfo.isEmpty()) {
            nextDebugY = drawDebugLines(sunInfo, x, sunY + TEXT_Y_OFFSET, scale);
        }
        if (!cameraInfo.isEmpty()) {
            float cameraY = Math.max(cameraBaseY + TEXT_Y_OFFSET, nextDebugY + DEBUG_LINE_GAP);
            drawDebugLines(cameraInfo, x, cameraY, scale);
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
        return currentY - step;
    }
}
