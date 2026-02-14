package SwordsGame.rts;

public abstract class TemplateBase {
    private final String id;
    private final String name;
    private final FactionType faction;
    private final Age minAge;
    private final ResourceBundle cost;

    protected TemplateBase(String id, String name, FactionType faction, Age minAge, ResourceBundle cost) {
        this.id = id;
        this.name = name;
        this.faction = faction;
        this.minAge = minAge;
        this.cost = cost == null ? new ResourceBundle() : cost.copy();
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
}
