package SwordsGame.shared.protocol.ui;

public class UiPan {
    private final String panelId;
    private final String text;

    public UiPan(String panelId, String text) {
        this.panelId = panelId == null ? "" : panelId;
        this.text = text == null ? "" : text;
    }

    public String getPanelId() {
        return panelId;
    }

    public String getText() {
        return text;
    }
}
