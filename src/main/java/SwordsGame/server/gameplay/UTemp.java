package SwordsGame.server.gameplay;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

public class UTemp extends TemplateBase {
    private final Set<URole> roles;
    private final Cbt combatType;
    private final UStat stats;
    private final String note;

    public UTemp(String id,
                        String name,
                        Fac faction,
                        Age minAge,
                        ResPack cost,
                        Set<URole> roles,
                        Cbt combatType,
                        UStat stats,
                        String note) {
        super(id, name, faction, minAge, cost);
        this.roles = roles == null || roles.isEmpty() ? EnumSet.noneOf(URole.class) : EnumSet.copyOf(roles);
        this.combatType = combatType == null ? Cbt.NONE : combatType;
        this.stats = stats;
        this.note = note == null ? "" : note;
    }

    public Set<URole> getRoles() {
        return Collections.unmodifiableSet(roles);
    }

    public Cbt getCombatType() {
        return combatType;
    }

    public UStat getStats() {
        return stats;
    }

    public String getNote() {
        return note;
    }
}
