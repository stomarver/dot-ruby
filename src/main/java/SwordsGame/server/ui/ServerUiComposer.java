package SwordsGame.server.ui;

import SwordsGame.server.ChunkManager;
import SwordsGame.server.environment.Sun;
import SwordsGame.server.gameplay.FactionTechTree;
import SwordsGame.server.gameplay.FactionType;
import SwordsGame.server.gameplay.RtsTemplates;
import SwordsGame.shared.protocol.ui.UiFrameState;
import SwordsGame.shared.protocol.ui.UiPanelState;

public class ServerUiComposer {
    public UiFrameState compose(Sun sun, ChunkManager chunkManager, FactionType faction) {
        UiFrameState frame = new UiFrameState();

        if (sun != null) {
            frame.addPanel(new UiPanelState("sun", String.format("^2Sun^0\\n^3yaw^0 %.1f\\n^4pitch^0 %.1f", sun.getYaw(), sun.getPitch())));
        }

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
