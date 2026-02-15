package SwordsGame.server.gameplay;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

public class BuildingTemplate extends TemplateBase {
    private final Set<BuildingRole> roles;
    private final int storageBonus;
    private final int populationUsage;
    private final String note;

    public BuildingTemplate(String id,
                            String name,
                            FactionType faction,
                            Age minAge,
                            ResourceBundle cost,
                            Set<BuildingRole> roles,
                            int storageBonus,
                            int populationUsage,
                            String note) {
        super(id, name, faction, minAge, cost);
        this.roles = roles == null || roles.isEmpty() ? EnumSet.noneOf(BuildingRole.class) : EnumSet.copyOf(roles);
        this.storageBonus = Math.max(0, storageBonus);
        this.populationUsage = Math.max(0, populationUsage);
        this.note = note == null ? "" : note;
    }

    public Set<BuildingRole> getRoles() {
        return Collections.unmodifiableSet(roles);
    }

    public int getStorageBonus() {
        return storageBonus;
    }

    public int getPopulationUsage() {
        return populationUsage;
    }

    public String getNote() {
        return note;
    }
}
