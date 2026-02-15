package SwordsGame.server.gameplay;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

public class UnitTemplate extends TemplateBase {
    private final Set<UnitRole> roles;
    private final CombatType combatType;
    private final UnitStats stats;
    private final String note;

    public UnitTemplate(String id,
                        String name,
                        FactionType faction,
                        Age minAge,
                        ResourceBundle cost,
                        Set<UnitRole> roles,
                        CombatType combatType,
                        UnitStats stats,
                        String note) {
        super(id, name, faction, minAge, cost);
        this.roles = roles == null || roles.isEmpty() ? EnumSet.noneOf(UnitRole.class) : EnumSet.copyOf(roles);
        this.combatType = combatType == null ? CombatType.NONE : combatType;
        this.stats = stats;
        this.note = note == null ? "" : note;
    }

    public Set<UnitRole> getRoles() {
        return Collections.unmodifiableSet(roles);
    }

    public CombatType getCombatType() {
        return combatType;
    }

    public UnitStats getStats() {
        return stats;
    }

    public String getNote() {
        return note;
    }
}
