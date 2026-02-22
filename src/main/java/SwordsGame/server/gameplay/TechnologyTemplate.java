package SwordsGame.server.gameplay;

import java.util.Collections;
import java.util.Set;

public class TechnologyTemplate extends TemplateBase {
    private final Age targetAge;
    private final String description;

    public TechnologyTemplate(String id,
                              String name,
                              FactionType faction,
                              Age minAge,
                              Age targetAge,
                              ResourceBundle cost,
                              String description) {
        this(id, name, faction, minAge, targetAge, cost, description, "", Collections.emptySet());
    }

    public TechnologyTemplate(String id,
                              String name,
                              FactionType faction,
                              Age minAge,
                              Age targetAge,
                              ResourceBundle cost,
                              String description,
                              String modelId,
                              Set<String> tags) {
        super(id, name, faction, minAge, cost, modelId, tags);
        this.targetAge = targetAge;
        this.description = description == null ? "" : description;
    }

    public Age getTargetAge() {
        return targetAge;
    }

    public String getDescription() {
        return description;
    }
}
