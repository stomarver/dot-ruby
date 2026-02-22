package SwordsGame.server.gameplay;

public class TTemp extends TemplateBase {
    private final Age targetAge;
    private final String description;

    public TTemp(String id,
                              String name,
                              Fac faction,
                              Age minAge,
                              Age targetAge,
                              ResPack cost,
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
