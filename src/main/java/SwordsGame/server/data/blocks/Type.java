package SwordsGame.server.data.blocks;

public enum Type {
    AIR(0, "Air"),
    COBBLE(1, "Cobblestone"),
    GRASS(2, "Grass"),
    STONE(3, "Stone"),
    GLASS(4, "Glass");

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
