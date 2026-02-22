package SwordsGame.server.ui;

import SwordsGame.server.gameplay.GameplayRegistry;
import SwordsGame.server.gameplay.MythicCorePack;
import SwordsGame.server.gameplay.MythicFactionPack;
import SwordsGame.server.gameplay.TemplateBase;
import SwordsGame.shared.protocol.ui.UiFrameState;
import SwordsGame.shared.protocol.ui.UiPanelState;

import java.util.Collection;

public class ServerUiComposer {
    public UiFrameState compose(SwordsGame.server.ChunkManager chunkManager, SwordsGame.server.gameplay.FactionType faction) {
        MythicCorePack.init();
        MythicFactionPack.init();

        UiFrameState frame = new UiFrameState();

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
