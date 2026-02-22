package SwordsGame.server.gameplay;

import java.util.Set;

public class ResourceNodeTemplate extends TemplateBase {
    private final ResourceType resourceType;
    private final int amount;
    private final int gatherDifficulty;

    public ResourceNodeTemplate(String id,
                                String name,
                                ResourceType resourceType,
                                int amount,
                                int gatherDifficulty,
                                String modelId,
                                Set<String> tags) {
        super(id, name, null, Age.LEGENDS, new ResourceBundle(), modelId, tags);
        this.resourceType = resourceType;
        this.amount = Math.max(1, amount);
        this.gatherDifficulty = Math.max(1, gatherDifficulty);
    }

    public ResourceType getResourceType() {
        return resourceType;
    }

    public int getAmount() {
        return amount;
    }

    public int getGatherDifficulty() {
        return gatherDifficulty;
    }
}
