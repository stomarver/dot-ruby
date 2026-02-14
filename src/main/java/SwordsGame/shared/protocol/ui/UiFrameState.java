package SwordsGame.shared.protocol.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UiFrameState {
    private final List<UiPanelState> panels = new ArrayList<>();

    public void addPanel(UiPanelState panel) {
        if (panel != null) {
            panels.add(panel);
        }
    }

    public List<UiPanelState> getPanels() {
        return Collections.unmodifiableList(panels);
    }
}
