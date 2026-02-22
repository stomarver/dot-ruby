package SwordsGame.server.ui;

import SwordsGame.server.ChMgr;
import SwordsGame.server.gameplay.FTech;
import SwordsGame.server.gameplay.Fac;
import SwordsGame.server.gameplay.RtsTemplates;
import SwordsGame.shared.protocol.ui.UiFrm;
import SwordsGame.shared.protocol.ui.UiPan;

public class UiComp {
    public UiFrm compose(ChMgr chunkManager, Fac faction) {
        UiFrm frame = new UiFrm();


        if (chunkManager != null) {
            frame.addPanel(new UiPan("world", String.format("^2World^0\\nsize^0 %d blocks", chunkManager.getWorldSizeInBlocks())));
        }

        if (faction != null) {
            FTech tree = RtsTemplates.get(faction);
            if (tree != null) {
                frame.addPanel(new UiPan(
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
