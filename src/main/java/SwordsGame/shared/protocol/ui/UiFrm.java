package SwordsGame.shared.protocol.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UiFrm {
    private final List<UiPan> panels = new ArrayList<>();

    public void addPanel(UiPan panel) {
        if (panel != null) {
            panels.add(panel);
        }
    }

    public List<UiPan> getPanels() {
        return Collections.unmodifiableList(panels);
    }
}
