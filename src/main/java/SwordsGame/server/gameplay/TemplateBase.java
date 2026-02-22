package SwordsGame.server.gameplay;

public abstract class TemplateBase {
    private final String id;
    private final String name;
    private final Fac faction;
    private final Age minAge;
    private final ResPack cost;

    protected TemplateBase(String id, String name, Fac faction, Age minAge, ResPack cost) {
        this.id = id;
        this.name = name;
        this.faction = faction;
        this.minAge = minAge;
        this.cost = cost == null ? new ResPack() : cost.copy();
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Fac getFaction() {
        return faction;
    }

    public Age getMinAge() {
        return minAge;
    }

    public ResPack getCost() {
        return cost.copy();
    }
}
