package SwordsGame.client.core;

import SwordsGame.client.ui.Hud;
import SwordsGame.client.ui.SelectionBox;

public final class ScenarioRenderUi {
    private ScenarioRenderUi() {
    }

    public static void render(Window window, Hud hud, SelectionBox selection, boolean renderHudBase) {
        if (renderHudBase && hud != null) {
            hud.renderBaseInterface();
        }

        float selectionThickness = window.getVirtualUnitsForPhysicalPixels(2f);
        selection.render(selectionThickness);

        if (hud != null) {
            hud.renderDialogOverlay();
        }
    }
}
