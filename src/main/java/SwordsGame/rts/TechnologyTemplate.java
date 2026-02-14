package SwordsGame.rts;

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
        super(id, name, faction, minAge, cost);
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
