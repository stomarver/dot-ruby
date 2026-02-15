package SwordsGame.server.gameplay;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FactionTechTree {
    private final FactionType faction;
    private final List<UnitTemplate> units = new ArrayList<>();
    private final List<BuildingTemplate> buildings = new ArrayList<>();
    private final List<TechnologyTemplate> technologies = new ArrayList<>();

    public FactionTechTree(FactionType faction) {
        this.faction = faction;
    }

    public FactionType getFaction() {
        return faction;
    }

    public void addUnit(UnitTemplate unit) {
        units.add(unit);
    }

    public void addBuilding(BuildingTemplate building) {
        buildings.add(building);
    }

    public void addTechnology(TechnologyTemplate technology) {
        technologies.add(technology);
    }

    public List<UnitTemplate> getUnits() {
        return Collections.unmodifiableList(units);
    }

    public List<BuildingTemplate> getBuildings() {
        return Collections.unmodifiableList(buildings);
    }

    public List<TechnologyTemplate> getTechnologies() {
        return Collections.unmodifiableList(technologies);
    }
}
