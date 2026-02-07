package SwordsGame.client.blocks;

public enum BlockType {
    AIR(0, "Air"),
    COBBLE(1, "Cobblestone"),
    GRASS(2, "Grass"),
    STONE(3, "Stone");

    public final byte id;
    public final String name;

    BlockType(int id, String name) {
        this.id = (byte) id;
        this.name = name;
    }

    public static BlockType fromId(byte id) {
        for (BlockType type : values()) {
            if (type.id == id) return type;
        }
        return AIR;
    }
}
