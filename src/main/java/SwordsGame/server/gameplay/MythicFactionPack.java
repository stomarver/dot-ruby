package SwordsGame.server.gameplay;

import java.util.EnumSet;

public final class MythicFactionPack {
    private static boolean initialized;

    private MythicFactionPack() {}

    public static void init() {
        if (initialized) return;
        initialized = true;

        GameplaySyn.unit("myth_villager", "Villager", FactionType.HUMANS)
                .age(Age.LEGENDS)
                .cost(ResourceBundle.of(ResourceType.FOOD, 50))
                .roles(EnumSet.of(UnitRole.WORKER))
                .combat(CombatType.NONE)
                .stats(new UnitStats(95, 20, 100, 100, 100, gather(100, 100, 100, 100)))
                .model("assets/models/units/villager.gltf")
                .tag("unit").tag("economy")
                .register();

        GameplaySyn.unit("myth_hoplite", "Hoplite", FactionType.HUMANS)
                .age(Age.LEGENDS)
                .cost(ResourceBundle.of(ResourceType.FOOD, 60, ResourceType.MINERALS, 30))
                .roles(EnumSet.of(UnitRole.INFANTRY))
                .combat(CombatType.MELEE)
                .stats(new UnitStats(120, 100, 95, 100, 0, gather(0, 0, 0, 0)))
                .model("assets/models/units/hoplite.gltf")
                .tag("unit").tag("military")
                .register();

        GameplaySyn.building("myth_town_center", "Town Center", FactionType.HUMANS)
                .age(Age.LEGENDS)
                .roles(EnumSet.of(BuildingRole.TOWN_CENTER, BuildingRole.ECONOMY))
                .storage(300)
                .note("Age-up anchor and villager production.")
                .model("assets/models/buildings/town_center.gltf")
                .tag("building").tag("core")
                .register();

        GameplaySyn.building("myth_temple", "Temple", FactionType.HUMANS)
                .age(Age.HISTORIES)
                .cost(ResourceBundle.of(ResourceType.WOOD, 150, ResourceType.STONE, 100))
                .roles(EnumSet.of(BuildingRole.WORKSHOP, BuildingRole.TRAINING))
                .note("Myth unit and divine upgrade hub.")
                .model("assets/models/buildings/temple.gltf")
                .tag("building").tag("mythic")
                .register();

        GameplaySyn.tech("myth_age_up_legends_histories", "Advance to Histories", FactionType.HUMANS)
                .age(Age.LEGENDS)
                .target(Age.HISTORIES)
                .cost(ResourceBundle.of(ResourceType.FOOD, 400, ResourceType.WOOD, 300))
                .note("Unlocks heroic-tier units and temple upgrades.")
                .tag("tech").tag("ageup")
                .register();
    }

    private static ResourceBundle gather(int wood, int minerals, int food, int stone) {
        ResourceBundle rates = new ResourceBundle();
        rates.set(ResourceType.WOOD, wood);
        rates.set(ResourceType.MINERALS, minerals);
        rates.set(ResourceType.FOOD, food);
        rates.set(ResourceType.STONE, stone);
        return rates;
    }
}
