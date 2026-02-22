package SwordsGame.server.gameplay;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FTech {
    private final Fac faction;
    private final List<UTemp> units = new ArrayList<>();
    private final List<BTemp> buildings = new ArrayList<>();
    private final List<TTemp> technologies = new ArrayList<>();

    public FTech(Fac faction) {
        this.faction = faction;
    }

    public Fac getFaction() {
        return faction;
    }

    public void addUnit(UTemp unit) {
        units.add(unit);
    }

    public void addBuilding(BTemp building) {
        buildings.add(building);
    }

    public void addTechnology(TTemp technology) {
        technologies.add(technology);
    }

    public List<UTemp> getUnits() {
        return Collections.unmodifiableList(units);
    }

    public List<BTemp> getBuildings() {
        return Collections.unmodifiableList(buildings);
    }

    public List<TTemp> getTechnologies() {
        return Collections.unmodifiableList(technologies);
    }
}
