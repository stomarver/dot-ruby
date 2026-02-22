package SwordsGame.server.gameplay;

import java.util.EnumSet;

public final class MythicCorePack {
    private static boolean initialized;

    private MythicCorePack() {}

    public static void init() {
        if (initialized) return;
        initialized = true;

        GameplaySyn.resource("res_tree_oak", "Oak Tree", ResourceType.WOOD)
                .amount(1800).difficulty(85).model("assets/models/resources/oak_tree.gltf").tag("resource").tag("nature").register();
        GameplaySyn.resource("res_gold_mine", "Gold Mine", ResourceType.MINERALS)
                .amount(2200).difficulty(120).model("assets/models/resources/gold_mine.gltf").tag("resource").tag("mine").register();
        GameplaySyn.resource("res_stone_outcrop", "Stone Outcrop", ResourceType.STONE)
                .amount(2000).difficulty(110).model("assets/models/resources/stone_outcrop.gltf").tag("resource").tag("stone").register();

        GameplaySyn.object("obj_ruins_small", "Ancient Ruins")
                .roles(EnumSet.of(ObjectRole.DECOR, ObjectRole.OBSTACLE)).hp(900)
                .model("assets/models/objects/ruins_small.gltf").tag("mythic").register();
        GameplaySyn.object("obj_relic_sun", "Relic of the Sun")
                .roles(EnumSet.of(ObjectRole.RELIC, ObjectRole.INTERACTIVE, ObjectRole.QUEST)).hp(250).interactable(true)
                .note("Capture grants global favor trickle.")
                .model("assets/models/objects/relic_sun.gltf").tag("mythic").tag("relic").register();
    }
}
