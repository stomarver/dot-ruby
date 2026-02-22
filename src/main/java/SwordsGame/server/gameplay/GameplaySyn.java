package SwordsGame.server.gameplay;

import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.Set;

public final class GameplaySyn {
    private GameplaySyn() {}

    public static UnitDef unit(String id, String name, FactionType faction) { return new UnitDef(id, name, faction); }
    public static BuildingDef building(String id, String name, FactionType faction) { return new BuildingDef(id, name, faction); }
    public static TechDef tech(String id, String name, FactionType faction) { return new TechDef(id, name, faction); }
    public static ResourceDef resource(String id, String name, ResourceType type) { return new ResourceDef(id, name, type); }
    public static ObjectDef object(String id, String name) { return new ObjectDef(id, name); }

    public abstract static class DefBase<T extends DefBase<T>> {
        protected final String id;
        protected final String name;
        protected String modelId = "";
        protected final Set<String> tags = new LinkedHashSet<>();

        protected DefBase(String id, String name) {
            this.id = id;
            this.name = name;
        }

        @SuppressWarnings("unchecked")
        public T model(String value) { this.modelId = value == null ? "" : value; return (T) this; }

        @SuppressWarnings("unchecked")
        public T tag(String value) { if (value != null && !value.isBlank()) tags.add(value); return (T) this; }

        protected Set<String> tagsOrEmpty() { return tags.isEmpty() ? Collections.emptySet() : tags; }
    }

    public static final class UnitDef extends DefBase<UnitDef> {
        private final FactionType faction;
        private Age age = Age.LEGENDS;
        private ResourceBundle cost = new ResourceBundle();
        private Set<UnitRole> roles = EnumSet.noneOf(UnitRole.class);
        private CombatType combat = CombatType.NONE;
        private UnitStats stats = new UnitStats(100, 100, 100, 100, 0, new ResourceBundle());
        private String note = "";

        private UnitDef(String id, String name, FactionType faction) { super(id, name); this.faction = faction; }
        public UnitDef age(Age value) { this.age = value; return this; }
        public UnitDef cost(ResourceBundle value) { this.cost = value == null ? new ResourceBundle() : value; return this; }
        public UnitDef roles(Set<UnitRole> value) { this.roles = value == null || value.isEmpty() ? EnumSet.noneOf(UnitRole.class) : EnumSet.copyOf(value); return this; }
        public UnitDef combat(CombatType value) { this.combat = value == null ? CombatType.NONE : value; return this; }
        public UnitDef stats(UnitStats value) { if (value != null) this.stats = value; return this; }
        public UnitDef note(String value) { this.note = value == null ? "" : value; return this; }
        public UnitTemplate build() { return new UnitTemplate(id, name, faction, age, cost, roles, combat, stats, note, modelId, tagsOrEmpty()); }
        public UnitTemplate register() { UnitTemplate t = build(); GameplayRegistry.reg(t); return t; }
    }

    public static final class BuildingDef extends DefBase<BuildingDef> {
        private final FactionType faction;
        private Age age = Age.LEGENDS;
        private ResourceBundle cost = new ResourceBundle();
        private Set<BuildingRole> roles = EnumSet.noneOf(BuildingRole.class);
        private int storage;
        private int pop;
        private String note = "";

        private BuildingDef(String id, String name, FactionType faction) { super(id, name); this.faction = faction; }
        public BuildingDef age(Age value) { this.age = value; return this; }
        public BuildingDef cost(ResourceBundle value) { this.cost = value == null ? new ResourceBundle() : value; return this; }
        public BuildingDef roles(Set<BuildingRole> value) { this.roles = value == null || value.isEmpty() ? EnumSet.noneOf(BuildingRole.class) : EnumSet.copyOf(value); return this; }
        public BuildingDef storage(int value) { this.storage = Math.max(0, value); return this; }
        public BuildingDef pop(int value) { this.pop = Math.max(0, value); return this; }
        public BuildingDef note(String value) { this.note = value == null ? "" : value; return this; }
        public BuildingTemplate build() { return new BuildingTemplate(id, name, faction, age, cost, roles, storage, pop, note, modelId, tagsOrEmpty()); }
        public BuildingTemplate register() { BuildingTemplate t = build(); GameplayRegistry.reg(t); return t; }
    }

    public static final class TechDef extends DefBase<TechDef> {
        private final FactionType faction;
        private Age age = Age.LEGENDS;
        private Age target = Age.HISTORIES;
        private ResourceBundle cost = new ResourceBundle();
        private String description = "";

        private TechDef(String id, String name, FactionType faction) { super(id, name); this.faction = faction; }
        public TechDef age(Age value) { this.age = value; return this; }
        public TechDef target(Age value) { this.target = value; return this; }
        public TechDef cost(ResourceBundle value) { this.cost = value == null ? new ResourceBundle() : value; return this; }
        public TechDef note(String value) { this.description = value == null ? "" : value; return this; }
        public TechnologyTemplate build() { return new TechnologyTemplate(id, name, faction, age, target, cost, description, modelId, tagsOrEmpty()); }
        public TechnologyTemplate register() { TechnologyTemplate t = build(); GameplayRegistry.reg(t); return t; }
    }

    public static final class ResourceDef extends DefBase<ResourceDef> {
        private final ResourceType type;
        private int amount = 1000;
        private int difficulty = 100;

        private ResourceDef(String id, String name, ResourceType type) { super(id, name); this.type = type; }
        public ResourceDef amount(int value) { this.amount = Math.max(1, value); return this; }
        public ResourceDef difficulty(int value) { this.difficulty = Math.max(1, value); return this; }
        public ResourceNodeTemplate build() { return new ResourceNodeTemplate(id, name, type, amount, difficulty, modelId, tagsOrEmpty()); }
        public ResourceNodeTemplate register() { ResourceNodeTemplate t = build(); GameplayRegistry.reg(t); return t; }
    }

    public static final class ObjectDef extends DefBase<ObjectDef> {
        private Set<ObjectRole> roles = EnumSet.noneOf(ObjectRole.class);
        private int hp = 300;
        private boolean interactable;
        private String note = "";

        private ObjectDef(String id, String name) { super(id, name); }
        public ObjectDef roles(Set<ObjectRole> value) { this.roles = value == null || value.isEmpty() ? EnumSet.noneOf(ObjectRole.class) : EnumSet.copyOf(value); return this; }
        public ObjectDef hp(int value) { this.hp = Math.max(1, value); return this; }
        public ObjectDef interactable(boolean value) { this.interactable = value; return this; }
        public ObjectDef note(String value) { this.note = value == null ? "" : value; return this; }
        public WorldObjectTemplate build() { return new WorldObjectTemplate(id, name, roles, hp, interactable, note, modelId, tagsOrEmpty()); }
        public WorldObjectTemplate register() { WorldObjectTemplate t = build(); GameplayRegistry.reg(t); return t; }
    }
}
