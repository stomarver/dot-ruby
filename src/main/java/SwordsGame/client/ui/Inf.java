package SwordsGame.client.ui;

public class Inf {
    private static final float TEXT_Y_OFFSET = 10.0f;
    private static final float DEBUG_LINE_GAP = 1.0f;
    private static final float DEBUG_MODULE_GAP = 40.0f;
    private static final float DEBUG_X = 130.0f;
    private static final float DEBUG_Y = 1.0f;

    private final Txt text;
    private String cameraInfo = "";
    private String serverInfo = "";

    public Inf(Txt text) {
        this.text = text;
    }


    public void setCameraInfo(String info) {
        this.cameraInfo = info == null ? "" : info;
    }

    public void setServerInfo(String info) {
        if (info == null || info.isEmpty()) {
            this.serverInfo = "";
            return;
        }

        String[] blocks = info.split("\n\n");
        StringBuilder filtered = new StringBuilder();

        for (String block : blocks) {
            if (block.contains("Wld") || block.contains("Faction")) {
                continue;
            }
            String trimmed = block.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            if (filtered.length() > 0) {
                filtered.append("\n\n");
            }
            filtered.append(trimmed);
        }

        this.serverInfo = filtered.toString();
    }

    public void renderDebug(float scale) {
        float currentY = DEBUG_Y + TEXT_Y_OFFSET;
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
            text.draw(line, Anc.LEFT, Anc.TOP, x, currentY, scale);
            currentY += step;
        }
        return currentY - step + DEBUG_LINE_GAP;
    }
}
