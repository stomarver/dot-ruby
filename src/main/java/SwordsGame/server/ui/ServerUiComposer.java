package SwordsGame.server.ui;

import SwordsGame.server.ChunkManager;
import SwordsGame.server.gameplay.FactionTechTree;
import SwordsGame.server.gameplay.FactionType;
import SwordsGame.server.gameplay.GameplayRegistry;
import SwordsGame.server.gameplay.MythicCorePack;
import SwordsGame.server.gameplay.MythicFactionPack;
import SwordsGame.server.gameplay.RtsTemplates;
import SwordsGame.server.gameplay.TemplateBase;
import SwordsGame.shared.protocol.ui.UiFrameState;
import SwordsGame.shared.protocol.ui.UiPanelState;

import java.util.Collection;

public class ServerUiComposer {
    public UiFrameState compose(ChunkManager chunkManager, FactionType faction) {
        MythicCorePack.init();
        MythicFactionPack.init();

        UiFrameState frame = new UiFrameState();

        if (chunkManager != null) {
            int worldSize = chunkManager.getWorldSizeInBlocks();
            int radius = chunkManager.getWorldRadiusBlocks();
            frame.addPanel(new UiPanelState("world", String.format(
                    "^2World^0\nsize^0 %d blocks\nradius^0 %d blocks\nchunks^0 %dx%d",
                    worldSize,
                    radius,
                    worldSize / SwordsGame.server.Chunk.SIZE,
                    worldSize / SwordsGame.server.Chunk.SIZE)));
        }

        if (faction != null) {
            FactionTechTree tree = RtsTemplates.get(faction);
            if (tree != null) {
                frame.addPanel(new UiPanelState(
                        "faction",
                        String.format("^2Faction^0\n%s\nUnits^0 %d\nBuildings^0 %d\nTechs^0 %d",
                                faction.name(),
                                tree.getUnits().size(),
                                tree.getBuildings().size(),
                                tree.getTechnologies().size())));
            }
        }

        frame.addPanel(new UiPanelState(
                "content",
                String.format("^2Content^0\nreg.units^0 %d\nreg.buildings^0 %d\nreg.techs^0 %d\nreg.resources^0 %d\nreg.objects^0 %d",
                        GameplayRegistry.units().size(),
                        GameplayRegistry.buildings().size(),
                        GameplayRegistry.technologies().size(),
                        GameplayRegistry.resources().size(),
                        GameplayRegistry.objects().size())));

        frame.addPanel(new UiPanelState(
                "models",
                String.format("^2ModelRefs^0\nunits^0 %d/%d\nbuildings^0 %d/%d\ntechs^0 %d/%d\nresources^0 %d/%d\nobjects^0 %d/%d",
                        countWithModel(GameplayRegistry.units()), GameplayRegistry.units().size(),
                        countWithModel(GameplayRegistry.buildings()), GameplayRegistry.buildings().size(),
                        countWithModel(GameplayRegistry.technologies()), GameplayRegistry.technologies().size(),
                        countWithModel(GameplayRegistry.resources()), GameplayRegistry.resources().size(),
                        countWithModel(GameplayRegistry.objects()), GameplayRegistry.objects().size())));

        return frame;
    }

    private int countWithModel(Collection<? extends TemplateBase> templates) {
        int count = 0;
        for (TemplateBase template : templates) {
            if (template.getModelId() != null && !template.getModelId().isBlank()) {
                count++;
            }
        }
        return count;
    }
}
