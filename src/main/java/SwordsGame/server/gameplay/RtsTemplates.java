package SwordsGame.server.gameplay;

import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;

public final class RtsTemplates {
    private static final EnumMap<Fac, FTech> TREES = new EnumMap<>(Fac.class);

    static {
        TREES.put(Fac.HUMANS, buildHumans());
        TREES.put(Fac.ELVES, buildElves());
        TREES.put(Fac.DWARVES, buildDwarves());
    }

    private RtsTemplates() {
    }

    public static FTech get(Fac faction) {
        return TREES.get(faction);
    }

    public static Map<Fac, FTech> getAll() {
        return Collections.unmodifiableMap(new EnumMap<>(TREES));
    }

    private static FTech buildHumans() {
        FTech tree = new FTech(Fac.HUMANS);

        tree.addUnit(new UTemp(
                "human_worker",
                "Peasant",
                Fac.HUMANS,
                Age.LEGENDS,
                ResPack.of(Res.FOOD, 65),
                EnumSet.of(URole.WORKER),
                Cbt.NONE,
                new UStat(100, 15, 100, 100, 100, gather(100, 100, 100, 100)),
                "Universal worker."));

        tree.addUnit(new UTemp(
                "human_swordsman",
                "Swordsman",
                Fac.HUMANS,
                Age.LEGENDS,
                ResPack.of(Res.FOOD, 70),
                EnumSet.of(URole.INFANTRY),
                Cbt.MELEE,
                new UStat(110, 100, 95, 100, 0, gather(0, 0, 0, 0)),
                "Core infantry of the first age."));

        tree.addUnit(new UTemp(
                "human_archer",
                "Archer",
                Fac.HUMANS,
                Age.LEGENDS,
                ResPack.of(Res.WOOD, 90, Res.FOOD, 40),
                EnumSet.of(URole.RANGED),
                Cbt.RANGED,
                new UStat(90, 105, 100, 100, 0, gather(0, 0, 0, 0)),
                "Ranged support."));

        tree.addUnit(new UTemp(
                "human_rider",
                "Rider",
                Fac.HUMANS,
                Age.LEGENDS,
                ResPack.of(Res.FOOD, 120, Res.MINERALS, 70),
                EnumSet.of(URole.CAVALRY),
                Cbt.MELEE,
                new UStat(125, 115, 120, 95, 0, gather(0, 0, 0, 0)),
                "Mobile melee unit."));

        tree.addUnit(new UTemp(
                "orc_elite",
                "Orc Elite",
                Fac.HUMANS,
                Age.HISTORIES,
                ResPack.of(Res.MINERALS, 200, Res.FOOD, 100),
                EnumSet.of(URole.ELITE, URole.INFANTRY),
                Cbt.MELEE,
                new UStat(150, 140, 80, 90, 0, gather(0, 0, 0, 0)),
                "Allied elite from orc clans."));

        tree.addUnit(new UTemp(
                "orc_leader",
                "Orc Leader",
                Fac.HUMANS,
                Age.HISTORIES,
                ResPack.of(Res.MINERALS, 250, Res.FOOD, 150),
                EnumSet.of(URole.ELITE, URole.SUPPORT),
                Cbt.AURA,
                new UStat(120, 100, 90, 100, 0, gather(0, 0, 0, 0)),
                "Aura: +20% damage and health in radius."));

        tree.addBuilding(new BTemp(
                "human_town_center",
                "Town Center",
                Fac.HUMANS,
                Age.LEGENDS,
                new ResPack(),
                EnumSet.of(BRole.TOWN_CENTER),
                0,
                0,
                "Main base and age progression."));

        tree.addBuilding(new BTemp(
                "human_barn",
                "Barn",
                Fac.HUMANS,
                Age.LEGENDS,
                ResPack.of(Res.WOOD, 120),
                EnumSet.of(BRole.STORAGE),
                400,
                0,
                "Specialized storage for food."));

        tree.addBuilding(new BTemp(
                "human_lumberyard",
                "Lumberyard",
                Fac.HUMANS,
                Age.LEGENDS,
                ResPack.of(Res.WOOD, 110),
                EnumSet.of(BRole.STORAGE),
                400,
                0,
                "Specialized storage for wood."));

        tree.addBuilding(new BTemp(
                "human_quarry",
                "Quarry",
                Fac.HUMANS,
                Age.LEGENDS,
                ResPack.of(Res.WOOD, 110, Res.STONE, 50),
                EnumSet.of(BRole.STORAGE),
                400,
                0,
                "Specialized storage for stone."));

        tree.addBuilding(new BTemp(
                "human_barracks",
                "Barracks",
                Fac.HUMANS,
                Age.LEGENDS,
                ResPack.of(Res.WOOD, 150, Res.STONE, 80),
                EnumSet.of(BRole.TRAINING),
                0,
                0,
                "Trains infantry."));

        tree.addBuilding(new BTemp(
                "human_tower",
                "Tower",
                Fac.HUMANS,
                Age.LEGENDS,
                ResPack.of(Res.STONE, 140),
                EnumSet.of(BRole.DEFENSE, BRole.TRAINING),
                0,
                0,
                "Defensive tower."));

        tree.addTechnology(ageUp(Fac.HUMANS, Age.LEGENDS, Age.HISTORIES, 400));
        tree.addTechnology(ageUp(Fac.HUMANS, Age.HISTORIES, Age.CHRONICLES, 800));

        return tree;
    }

    private static FTech buildElves() {
        FTech tree = new FTech(Fac.ELVES);

        tree.addUnit(new UTemp(
                "elf_worker",
                "Gnoll Worker",
                Fac.ELVES,
                Age.LEGENDS,
                ResPack.of(Res.FOOD, 50),
                EnumSet.of(URole.WORKER),
                Cbt.NONE,
                new UStat(80, 10, 90, 100, 65, gather(70, 50, 60, 55)),
                "Cheap but slower worker."));

        tree.addUnit(new UTemp(
                "gnollid",
                "Gnollid",
                Fac.ELVES,
                Age.LEGENDS,
                ResPack.of(Res.MINERALS, 75),
                EnumSet.of(URole.INFANTRY),
                Cbt.MELEE,
                new UStat(110, 90, 95, 85, 0, gather(0, 0, 0, 0)),
                "Worker transformation into melee defender."));

        tree.addUnit(new UTemp(
                "elf_archer",
                "Elven Archer",
                Fac.ELVES,
                Age.LEGENDS,
                ResPack.of(Res.WOOD, 100, Res.MINERALS, 50),
                EnumSet.of(URole.RANGED),
                Cbt.RANGED,
                new UStat(90, 120, 110, 105, 0, gather(0, 0, 0, 0)),
                "Strong ranged core unit."));

        tree.addUnit(new UTemp(
                "elf_spearman",
                "Spearman",
                Fac.ELVES,
                Age.LEGENDS,
                ResPack.of(Res.FOOD, 80, Res.WOOD, 40),
                EnumSet.of(URole.INFANTRY),
                Cbt.MELEE,
                new UStat(100, 95, 100, 100, 0, gather(0, 0, 0, 0)),
                "Anti-cavalry placeholder."));

        tree.addBuilding(new BTemp(
                "elf_town_center",
                "Ancient Center",
                Fac.ELVES,
                Age.LEGENDS,
                new ResPack(),
                EnumSet.of(BRole.TOWN_CENTER),
                0,
                0,
                "Main base and age progression."));

        tree.addBuilding(new BTemp(
                "elf_storehouse",
                "Storehouse",
                Fac.ELVES,
                Age.LEGENDS,
                ResPack.of(Res.WOOD, 120),
                EnumSet.of(BRole.STORAGE),
                800,
                0,
                "Universal storage."));

        tree.addBuilding(new BTemp(
                "elf_archery",
                "Archery",
                Fac.ELVES,
                Age.LEGENDS,
                ResPack.of(Res.WOOD, 160, Res.STONE, 60),
                EnumSet.of(BRole.TRAINING),
                0,
                0,
                "Ranged unit training."));

        tree.addBuilding(new BTemp(
                "elf_academy",
                "Academy",
                Fac.ELVES,
                Age.HISTORIES,
                ResPack.of(Res.MINERALS, 140, Res.WOOD, 110),
                EnumSet.of(BRole.TRAINING, BRole.WORKSHOP),
                0,
                0,
                "Magic/support research building."));

        tree.addTechnology(ageUp(Fac.ELVES, Age.LEGENDS, Age.HISTORIES, 400));
        tree.addTechnology(ageUp(Fac.ELVES, Age.HISTORIES, Age.CHRONICLES, 800));

        return tree;
    }

    private static FTech buildDwarves() {
        FTech tree = new FTech(Fac.DWARVES);

        tree.addUnit(new UTemp(
                "dwarf_worker",
                "Gnome Worker",
                Fac.DWARVES,
                Age.LEGENDS,
                ResPack.of(Res.FOOD, 75),
                EnumSet.of(URole.WORKER),
                Cbt.NONE,
                new UStat(120, 12, 85, 100, 95, gather(80, 140, 80, 90)),
                "Durable worker with mineral focus."));

        tree.addUnit(new UTemp(
                "dwarf_enforcer",
                "Enforcer",
                Fac.DWARVES,
                Age.LEGENDS,
                ResPack.of(Res.MINERALS, 150, Res.STONE, 75),
                EnumSet.of(URole.INFANTRY),
                Cbt.MELEE,
                new UStat(140, 110, 70, 90, 0, gather(0, 0, 0, 0)),
                "Heavy frontline unit."));

        tree.addUnit(new UTemp(
                "dwarf_sapper",
                "Sapper",
                Fac.DWARVES,
                Age.HISTORIES,
                ResPack.of(Res.STONE, 200, Res.MINERALS, 100),
                EnumSet.of(URole.SIEGE),
                Cbt.SIEGE,
                new UStat(130, 130, 60, 85, 0, gather(0, 0, 0, 0)),
                "Siege specialist / demolitions."));

        tree.addBuilding(new BTemp(
                "dwarf_town_center",
                "Forge Hall",
                Fac.DWARVES,
                Age.LEGENDS,
                new ResPack(),
                EnumSet.of(BRole.TOWN_CENTER),
                0,
                0,
                "Main base and age progression."));

        tree.addBuilding(new BTemp(
                "dwarf_wagon",
                "Storage Wagon",
                Fac.DWARVES,
                Age.HISTORIES,
                ResPack.of(Res.STONE, 150, Res.MINERALS, 100),
                EnumSet.of(BRole.STORAGE, BRole.ECONOMY),
                600,
                2,
                "Mobile storage unit."));

        tree.addBuilding(new BTemp(
                "dwarf_foundry",
                "Foundry",
                Fac.DWARVES,
                Age.LEGENDS,
                ResPack.of(Res.MINERALS, 170, Res.STONE, 90),
                EnumSet.of(BRole.TRAINING, BRole.WORKSHOP),
                0,
                0,
                "Military and upgrade workshop."));

        tree.addBuilding(new BTemp(
                "dwarf_turret",
                "Turret",
                Fac.DWARVES,
                Age.HISTORIES,
                ResPack.of(Res.STONE, 160, Res.MINERALS, 80),
                EnumSet.of(BRole.DEFENSE),
                0,
                0,
                "Static defensive emplacement."));

        tree.addTechnology(ageUp(Fac.DWARVES, Age.LEGENDS, Age.HISTORIES, 400));
        tree.addTechnology(ageUp(Fac.DWARVES, Age.HISTORIES, Age.CHRONICLES, 800));

        return tree;
    }

    private static TTemp ageUp(Fac faction, Age from, Age target, int costPerResource) {
        return new TTemp(
                faction.name().toLowerCase() + "_age_" + target.name().toLowerCase(),
                "Advance to " + target.name(),
                faction,
                from,
                target,
                ResPack.of(Res.WOOD, costPerResource,
                        Res.MINERALS, costPerResource).add(ResPack.of(Res.FOOD, costPerResource,
                        Res.STONE, costPerResource)),
                "Age progression research at Town Center.");
    }

    private static ResPack gather(int wood, int minerals, int food, int stone) {
        ResPack bundle = new ResPack();
        bundle.set(Res.WOOD, wood);
        bundle.set(Res.MINERALS, minerals);
        bundle.set(Res.FOOD, food);
        bundle.set(Res.STONE, stone);
        return bundle;
    }
}
