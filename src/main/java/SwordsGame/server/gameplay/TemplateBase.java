package SwordsGame.server.gameplay;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public abstract class TemplateBase {
    private final String id;
    private final String name;
    private final FactionType faction;
    private final Age minAge;
    private final ResourceBundle cost;
    private final String modelId;
    private final Set<String> tags;

    protected TemplateBase(String id, String name, FactionType faction, Age minAge, ResourceBundle cost) {
        this(id, name, faction, minAge, cost, "", Collections.emptySet());
    }

    protected TemplateBase(String id,
                           String name,
                           FactionType faction,
                           Age minAge,
                           ResourceBundle cost,
                           String modelId,
                           Set<String> tags) {
        this.id = id == null ? "" : id;
        this.name = name == null ? "" : name;
        this.faction = faction;
        this.minAge = minAge;
        this.cost = cost == null ? new ResourceBundle() : cost.copy();
        this.modelId = modelId == null ? "" : modelId;
        this.tags = tags == null || tags.isEmpty() ? Collections.emptySet() : Collections.unmodifiableSet(new LinkedHashSet<>(tags));
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public FactionType getFaction() {
        return faction;
    }

    public Age getMinAge() {
        return minAge;
    }

    public ResourceBundle getCost() {
        return cost.copy();
    }

    public String getModelId() {
        return modelId;
    }

    public Set<String> getTags() {
        return tags;
    }
}
