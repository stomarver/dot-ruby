package SwordsGame.server.gameplay;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;

public final class RtsTemplates {
    private static final EnumMap<FactionType, FactionTechTree> TREES = new EnumMap<>(FactionType.class);

    static {
        TREES.put(FactionType.HUMANS, buildHumans());
        TREES.put(FactionType.ELVES, buildElves());
        TREES.put(FactionType.DWARVES, buildDwarves());
    }

    private RtsTemplates() {
    }

    public static FactionTechTree get(FactionType faction) {
        return TREES.get(faction);
    }

    public static Map<FactionType, FactionTechTree> getAll() {
        return Map.copyOf(TREES);
    }

    private static FactionTechTree buildHumans() {
        FactionTechTree tree = new FactionTechTree(FactionType.HUMANS);

        tree.addUnit(new UnitTemplate(
                "human_worker",
                "Peasant",
                FactionType.HUMANS,
                Age.LEGENDS,
                ResourceBundle.of(ResourceType.FOOD, 65),
                EnumSet.of(UnitRole.WORKER),
                CombatType.NONE,
                new UnitStats(100, 15, 100, 100, 100, gather(100, 100, 100, 100)),
                "Universal worker."));

        tree.addUnit(new UnitTemplate(
                "human_swordsman",
                "Swordsman",
                FactionType.HUMANS,
                Age.LEGENDS,
                ResourceBundle.of(ResourceType.FOOD, 70),
                EnumSet.of(UnitRole.INFANTRY),
                CombatType.MELEE,
                new UnitStats(110, 100, 95, 100, 0, gather(0, 0, 0, 0)),
                "Core infantry of the first age."));

        tree.addUnit(new UnitTemplate(
                "human_archer",
                "Archer",
                FactionType.HUMANS,
                Age.LEGENDS,
                ResourceBundle.of(ResourceType.WOOD, 90, ResourceType.FOOD, 40),
                EnumSet.of(UnitRole.RANGED),
                CombatType.RANGED,
                new UnitStats(90, 105, 100, 100, 0, gather(0, 0, 0, 0)),
                "Ranged support."));

        tree.addUnit(new UnitTemplate(
                "human_rider",
                "Rider",
                FactionType.HUMANS,
                Age.LEGENDS,
                ResourceBundle.of(ResourceType.FOOD, 120, ResourceType.MINERALS, 70),
                EnumSet.of(UnitRole.CAVALRY),
                CombatType.MELEE,
                new UnitStats(125, 115, 120, 95, 0, gather(0, 0, 0, 0)),
                "Mobile melee unit."));

        tree.addUnit(new UnitTemplate(
                "orc_elite",
                "Orc Elite",
                FactionType.HUMANS,
                Age.HISTORIES,
                ResourceBundle.of(ResourceType.MINERALS, 200, ResourceType.FOOD, 100),
                EnumSet.of(UnitRole.ELITE, UnitRole.INFANTRY),
                CombatType.MELEE,
                new UnitStats(150, 140, 80, 90, 0, gather(0, 0, 0, 0)),
                "Allied elite from orc clans."));

        tree.addUnit(new UnitTemplate(
                "orc_leader",
                "Orc Leader",
                FactionType.HUMANS,
                Age.HISTORIES,
                ResourceBundle.of(ResourceType.MINERALS, 250, ResourceType.FOOD, 150),
                EnumSet.of(UnitRole.ELITE, UnitRole.SUPPORT),
                CombatType.AURA,
                new UnitStats(120, 100, 90, 100, 0, gather(0, 0, 0, 0)),
                "Aura: +20% damage and health in radius."));

        tree.addBuilding(new BuildingTemplate(
                "human_town_center",
                "Town Center",
                FactionType.HUMANS,
                Age.LEGENDS,
                new ResourceBundle(),
                EnumSet.of(BuildingRole.TOWN_CENTER),
                0,
                0,
                "Main base and age progression."));

        tree.addBuilding(new BuildingTemplate(
                "human_barn",
                "Barn",
                FactionType.HUMANS,
                Age.LEGENDS,
                ResourceBundle.of(ResourceType.WOOD, 120),
                EnumSet.of(BuildingRole.STORAGE),
                400,
                0,
                "Specialized storage for food."));

        tree.addBuilding(new BuildingTemplate(
                "human_lumberyard",
                "Lumberyard",
                FactionType.HUMANS,
                Age.LEGENDS,
                ResourceBundle.of(ResourceType.WOOD, 110),
                EnumSet.of(BuildingRole.STORAGE),
                400,
                0,
                "Specialized storage for wood."));

        tree.addBuilding(new BuildingTemplate(
                "human_quarry",
                "Quarry",
                FactionType.HUMANS,
                Age.LEGENDS,
                ResourceBundle.of(ResourceType.WOOD, 110, ResourceType.STONE, 50),
                EnumSet.of(BuildingRole.STORAGE),
                400,
                0,
                "Specialized storage for stone."));

        tree.addBuilding(new BuildingTemplate(
                "human_barracks",
                "Barracks",
                FactionType.HUMANS,
                Age.LEGENDS,
                ResourceBundle.of(ResourceType.WOOD, 150, ResourceType.STONE, 80),
                EnumSet.of(BuildingRole.TRAINING),
                0,
                0,
                "Trains infantry."));

        tree.addBuilding(new BuildingTemplate(
                "human_tower",
                "Tower",
                FactionType.HUMANS,
                Age.LEGENDS,
                ResourceBundle.of(ResourceType.STONE, 140),
                EnumSet.of(BuildingRole.DEFENSE, BuildingRole.TRAINING),
                0,
                0,
                "Defensive tower."));

        tree.addTechnology(ageUp(FactionType.HUMANS, Age.LEGENDS, Age.HISTORIES, 400));
        tree.addTechnology(ageUp(FactionType.HUMANS, Age.HISTORIES, Age.CHRONICLES, 800));

        return tree;
    }

    private static FactionTechTree buildElves() {
        FactionTechTree tree = new FactionTechTree(FactionType.ELVES);

        tree.addUnit(new UnitTemplate(
                "elf_worker",
                "Gnoll Worker",
                FactionType.ELVES,
                Age.LEGENDS,
                ResourceBundle.of(ResourceType.FOOD, 50),
                EnumSet.of(UnitRole.WORKER),
                CombatType.NONE,
                new UnitStats(80, 10, 90, 100, 65, gather(70, 50, 60, 55)),
                "Cheap but slower worker."));

        tree.addUnit(new UnitTemplate(
                "gnollid",
                "Gnollid",
                FactionType.ELVES,
                Age.LEGENDS,
                ResourceBundle.of(ResourceType.MINERALS, 75),
                EnumSet.of(UnitRole.INFANTRY),
                CombatType.MELEE,
                new UnitStats(110, 90, 95, 85, 0, gather(0, 0, 0, 0)),
                "Worker transformation into melee defender."));

        tree.addUnit(new UnitTemplate(
                "elf_archer",
                "Elven Archer",
                FactionType.ELVES,
                Age.LEGENDS,
                ResourceBundle.of(ResourceType.WOOD, 100, ResourceType.MINERALS, 50),
                EnumSet.of(UnitRole.RANGED),
                CombatType.RANGED,
                new UnitStats(90, 120, 110, 105, 0, gather(0, 0, 0, 0)),
                "Strong ranged core unit."));

        tree.addUnit(new UnitTemplate(
                "elf_spearman",
                "Spearman",
                FactionType.ELVES,
                Age.LEGENDS,
                ResourceBundle.of(ResourceType.FOOD, 80, ResourceType.WOOD, 40),
                EnumSet.of(UnitRole.INFANTRY),
                CombatType.MELEE,
                new UnitStats(100, 95, 100, 100, 0, gather(0, 0, 0, 0)),
                "Anti-cavalry placeholder."));

        tree.addBuilding(new BuildingTemplate(
                "elf_town_center",
                "Ancient Center",
                FactionType.ELVES,
                Age.LEGENDS,
                new ResourceBundle(),
                EnumSet.of(BuildingRole.TOWN_CENTER),
                0,
                0,
                "Main base and age progression."));

        tree.addBuilding(new BuildingTemplate(
                "elf_storehouse",
                "Storehouse",
                FactionType.ELVES,
                Age.LEGENDS,
                ResourceBundle.of(ResourceType.WOOD, 120),
                EnumSet.of(BuildingRole.STORAGE),
                800,
                0,
                "Universal storage."));

        tree.addBuilding(new BuildingTemplate(
                "elf_archery",
                "Archery",
                FactionType.ELVES,
                Age.LEGENDS,
                ResourceBundle.of(ResourceType.WOOD, 160, ResourceType.STONE, 60),
                EnumSet.of(BuildingRole.TRAINING),
                0,
                0,
                "Ranged unit training."));

        tree.addBuilding(new BuildingTemplate(
                "elf_academy",
                "Academy",
                FactionType.ELVES,
                Age.HISTORIES,
                ResourceBundle.of(ResourceType.MINERALS, 140, ResourceType.WOOD, 110),
                EnumSet.of(BuildingRole.TRAINING, BuildingRole.WORKSHOP),
                0,
                0,
                "Magic/support research building."));

        tree.addTechnology(ageUp(FactionType.ELVES, Age.LEGENDS, Age.HISTORIES, 400));
        tree.addTechnology(ageUp(FactionType.ELVES, Age.HISTORIES, Age.CHRONICLES, 800));

        return tree;
    }

    private static FactionTechTree buildDwarves() {
        FactionTechTree tree = new FactionTechTree(FactionType.DWARVES);

        tree.addUnit(new UnitTemplate(
                "dwarf_worker",
                "Gnome Worker",
                FactionType.DWARVES,
                Age.LEGENDS,
                ResourceBundle.of(ResourceType.FOOD, 75),
                EnumSet.of(UnitRole.WORKER),
                CombatType.NONE,
                new UnitStats(120, 12, 85, 100, 95, gather(80, 140, 80, 90)),
                "Durable worker with mineral focus."));

        tree.addUnit(new UnitTemplate(
                "dwarf_enforcer",
                "Enforcer",
                FactionType.DWARVES,
                Age.LEGENDS,
                ResourceBundle.of(ResourceType.MINERALS, 150, ResourceType.STONE, 75),
                EnumSet.of(UnitRole.INFANTRY),
                CombatType.MELEE,
                new UnitStats(140, 110, 70, 90, 0, gather(0, 0, 0, 0)),
                "Heavy frontline unit."));

        tree.addUnit(new UnitTemplate(
                "dwarf_sapper",
                "Sapper",
                FactionType.DWARVES,
                Age.HISTORIES,
                ResourceBundle.of(ResourceType.STONE, 200, ResourceType.MINERALS, 100),
                EnumSet.of(UnitRole.SIEGE),
                CombatType.SIEGE,
                new UnitStats(130, 130, 60, 85, 0, gather(0, 0, 0, 0)),
                "Siege specialist / demolitions."));

        tree.addBuilding(new BuildingTemplate(
                "dwarf_town_center",
                "Forge Hall",
                FactionType.DWARVES,
                Age.LEGENDS,
                new ResourceBundle(),
                EnumSet.of(BuildingRole.TOWN_CENTER),
                0,
                0,
                "Main base and age progression."));

        tree.addBuilding(new BuildingTemplate(
                "dwarf_wagon",
                "Storage Wagon",
                FactionType.DWARVES,
                Age.HISTORIES,
                ResourceBundle.of(ResourceType.STONE, 150, ResourceType.MINERALS, 100),
                EnumSet.of(BuildingRole.STORAGE, BuildingRole.ECONOMY),
                600,
                2,
                "Mobile storage unit."));

        tree.addBuilding(new BuildingTemplate(
                "dwarf_foundry",
                "Foundry",
                FactionType.DWARVES,
                Age.LEGENDS,
                ResourceBundle.of(ResourceType.MINERALS, 170, ResourceType.STONE, 90),
                EnumSet.of(BuildingRole.TRAINING, BuildingRole.WORKSHOP),
                0,
                0,
                "Military and upgrade workshop."));

        tree.addBuilding(new BuildingTemplate(
                "dwarf_turret",
                "Turret",
                FactionType.DWARVES,
                Age.HISTORIES,
                ResourceBundle.of(ResourceType.STONE, 160, ResourceType.MINERALS, 80),
                EnumSet.of(BuildingRole.DEFENSE),
                0,
                0,
                "Static defensive emplacement."));

        tree.addTechnology(ageUp(FactionType.DWARVES, Age.LEGENDS, Age.HISTORIES, 400));
        tree.addTechnology(ageUp(FactionType.DWARVES, Age.HISTORIES, Age.CHRONICLES, 800));

        return tree;
    }

    private static TechnologyTemplate ageUp(FactionType faction, Age from, Age target, int costPerResource) {
        return new TechnologyTemplate(
                faction.name().toLowerCase() + "_age_" + target.name().toLowerCase(),
                "Advance to " + target.name(),
                faction,
                from,
                target,
                ResourceBundle.of(ResourceType.WOOD, costPerResource,
                        ResourceType.MINERALS, costPerResource).add(ResourceBundle.of(ResourceType.FOOD, costPerResource,
                        ResourceType.STONE, costPerResource)),
                "Age progression research at Town Center.");
    }

    private static ResourceBundle gather(int wood, int minerals, int food, int stone) {
        ResourceBundle bundle = new ResourceBundle();
        bundle.set(ResourceType.WOOD, wood);
        bundle.set(ResourceType.MINERALS, minerals);
        bundle.set(ResourceType.FOOD, food);
        bundle.set(ResourceType.STONE, stone);
        return bundle;
    }
}
