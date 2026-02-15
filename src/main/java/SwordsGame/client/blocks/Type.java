package SwordsGame.client.blocks;

import SwordsGame.shared.world.BlockId;

public enum Type {
    AIR(BlockId.AIR, "Air"),
    COBBLE(BlockId.COBBLE, "Cobblestone"),
    GRASS(BlockId.GRASS, "Grass"),
    STONE(BlockId.STONE, "Stone");

    public final byte id;
    public final String name;

    Type(int id, String name) {
        this.id = (byte) id;
        this.name = name;
    }

    public static Type fromId(byte id) {
        for (Type type : values()) {
            if (type.id == id) return type;
        }
        return AIR;
    }
}
