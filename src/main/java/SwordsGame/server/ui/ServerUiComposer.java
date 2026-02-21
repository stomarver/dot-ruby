package SwordsGame.server.ui;

import SwordsGame.server.ChunkManager;
import SwordsGame.server.gameplay.FactionTechTree;
import SwordsGame.server.gameplay.FactionType;
import SwordsGame.server.gameplay.RtsTemplates;
import SwordsGame.shared.protocol.ui.UiFrameState;
import SwordsGame.shared.protocol.ui.UiPanelState;

public class ServerUiComposer {
    public UiFrameState compose(ChunkManager chunkManager, FactionType faction) {
        UiFrameState frame = new UiFrameState();


        if (chunkManager != null) {
            frame.addPanel(new UiPanelState("world", String.format("^2World^0\\nsize^0 %d blocks", chunkManager.getWorldSizeInBlocks())));
        }

        if (faction != null) {
            FactionTechTree tree = RtsTemplates.get(faction);
            if (tree != null) {
                frame.addPanel(new UiPanelState(
                        "faction",
                        String.format("^2Faction^0\\n%s\\nUnits: %d\\nBuildings: %d\\nTechs: %d",
                                faction.name(),
                                tree.getUnits().size(),
                                tree.getBuildings().size(),
                                tree.getTechnologies().size())));
            }
        }

        return frame;
    }
}
