package SwordsGame.server.gameplay;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

public class BTemp extends TemplateBase {
    private final Set<BRole> roles;
    private final int storageBonus;
    private final int populationUsage;
    private final String note;

    public BTemp(String id,
                            String name,
                            Fac faction,
                            Age minAge,
                            ResPack cost,
                            Set<BRole> roles,
                            int storageBonus,
                            int populationUsage,
                            String note) {
        super(id, name, faction, minAge, cost);
        this.roles = roles == null || roles.isEmpty() ? EnumSet.noneOf(BRole.class) : EnumSet.copyOf(roles);
        this.storageBonus = Math.max(0, storageBonus);
        this.populationUsage = Math.max(0, populationUsage);
        this.note = note == null ? "" : note;
    }

    public Set<BRole> getRoles() {
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
