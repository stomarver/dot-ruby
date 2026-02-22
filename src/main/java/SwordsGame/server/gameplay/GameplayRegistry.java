package SwordsGame.server.gameplay;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class GameplayRegistry {
    private static final Map<String, UnitTemplate> UNITS = new LinkedHashMap<>();
    private static final Map<String, BuildingTemplate> BUILDINGS = new LinkedHashMap<>();
    private static final Map<String, TechnologyTemplate> TECHNOLOGIES = new LinkedHashMap<>();
    private static final Map<String, ResourceNodeTemplate> RESOURCES = new LinkedHashMap<>();
    private static final Map<String, WorldObjectTemplate> OBJECTS = new LinkedHashMap<>();

    private GameplayRegistry() {}

    public static void clear() {
        UNITS.clear();
        BUILDINGS.clear();
        TECHNOLOGIES.clear();
        RESOURCES.clear();
        OBJECTS.clear();
    }

    public static void reg(UnitTemplate template) { reg(UNITS, template.getId(), template); }
    public static void reg(BuildingTemplate template) { reg(BUILDINGS, template.getId(), template); }
    public static void reg(TechnologyTemplate template) { reg(TECHNOLOGIES, template.getId(), template); }
    public static void reg(ResourceNodeTemplate template) { reg(RESOURCES, template.getId(), template); }
    public static void reg(WorldObjectTemplate template) { reg(OBJECTS, template.getId(), template); }

    private static <T> void reg(Map<String, T> map, String id, T template) {
        if (template == null) return;
        String key = id == null ? "" : id.trim();
        if (key.isEmpty()) throw new IllegalArgumentException("Template id is empty");
        if (map.containsKey(key)) throw new IllegalStateException("Duplicate template id: " + key);
        map.put(key, template);
    }

    public static Collection<UnitTemplate> units() { return Collections.unmodifiableCollection(UNITS.values()); }
    public static Collection<BuildingTemplate> buildings() { return Collections.unmodifiableCollection(BUILDINGS.values()); }
    public static Collection<TechnologyTemplate> technologies() { return Collections.unmodifiableCollection(TECHNOLOGIES.values()); }
    public static Collection<ResourceNodeTemplate> resources() { return Collections.unmodifiableCollection(RESOURCES.values()); }
    public static Collection<WorldObjectTemplate> objects() { return Collections.unmodifiableCollection(OBJECTS.values()); }
}
