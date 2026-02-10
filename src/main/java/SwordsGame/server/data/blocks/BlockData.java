package SwordsGame.server.data.blocks;

public final class BlockData {
    private final Type type;
    private final boolean solid;
    private final float hardness;

    public BlockData(Type type, boolean solid, float hardness) {
        this.type = type;
        this.solid = solid;
        this.hardness = hardness;
    }

    public Type getType() {
        return type;
    }

    public boolean isSolid() {
        return solid;
    }

    public float getHardness() {
        return hardness;
    }
}
