# Gameplay Syntax (Units / Buildings / Resources / Objects)

New fluent DSL lives in `SwordsGame.server.gameplay.GameplaySyn`.

## Units
```java
GameplaySyn.unit("myth_hoplite", "Hoplite", FactionType.HUMANS)
        .age(Age.LEGENDS)
        .cost(ResourceBundle.of(ResourceType.FOOD, 60, ResourceType.MINERALS, 30))
        .roles(EnumSet.of(UnitRole.INFANTRY))
        .combat(CombatType.MELEE)
        .stats(new UnitStats(...))
        .model("assets/models/units/hoplite.gltf")
        .tag("unit")
        .register();
```

## Buildings
```java
GameplaySyn.building("myth_temple", "Temple", FactionType.HUMANS)
        .age(Age.HISTORIES)
        .cost(ResourceBundle.of(ResourceType.WOOD, 150, ResourceType.STONE, 100))
        .roles(EnumSet.of(BuildingRole.WORKSHOP, BuildingRole.TRAINING))
        .model("assets/models/buildings/temple.gltf")
        .register();
```

## Resource nodes
```java
GameplaySyn.resource("res_gold_mine", "Gold Mine", ResourceType.MINERALS)
        .amount(2200)
        .difficulty(120)
        .model("assets/models/resources/gold_mine.gltf")
        .register();
```

## World objects
```java
GameplaySyn.object("obj_relic_sun", "Relic of the Sun")
        .roles(EnumSet.of(ObjectRole.RELIC, ObjectRole.INTERACTIVE))
        .hp(250)
        .interactable(true)
        .model("assets/models/objects/relic_sun.gltf")
        .register();
```

## Registry
All definitions are stored in `GameplayRegistry` with duplicate id protection.
