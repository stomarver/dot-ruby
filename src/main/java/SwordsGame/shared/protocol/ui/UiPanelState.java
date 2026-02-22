package SwordsGame.shared.protocol.ui;

public class UiPanelState {
    private final String panelId;
    private final String text;

    public UiPanelState(String panelId, String text) {
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
