package SwordsGame.server.gameplay;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

public class WorldObjectTemplate extends TemplateBase {
    private final Set<ObjectRole> roles;
    private final int hitPoints;
    private final boolean interactable;
    private final String note;

    public WorldObjectTemplate(String id,
                               String name,
                               Set<ObjectRole> roles,
                               int hitPoints,
                               boolean interactable,
                               String note,
                               String modelId,
                               Set<String> tags) {
        super(id, name, null, Age.LEGENDS, new ResourceBundle(), modelId, tags);
        this.roles = roles == null || roles.isEmpty() ? EnumSet.noneOf(ObjectRole.class) : EnumSet.copyOf(roles);
        this.hitPoints = Math.max(1, hitPoints);
        this.interactable = interactable;
        this.note = note == null ? "" : note;
    }

    public Set<ObjectRole> getRoles() {
        return Collections.unmodifiableSet(roles);
    }

    public int getHitPoints() {
        return hitPoints;
    }

    public boolean isInteractable() {
        return interactable;
    }

    public String getNote() {
        return note;
    }
}
