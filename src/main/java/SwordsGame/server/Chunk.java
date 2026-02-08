package SwordsGame.server;

public class Chunk {
    public static final int SIZE = 16;
    public static final int HEIGHT = 32;

    private final byte[][][] blocks;
    public final int x, z;

    public Chunk(int x, int z) {
        this.x = x;
        this.z = z;
        this.blocks = new byte[SIZE][HEIGHT][SIZE];
    }

    public void setBlock(int lx, int ly, int lz, byte type) {
        if (lx >= 0 && lx < SIZE && ly >= 0 && ly < HEIGHT && lz >= 0 && lz < SIZE) {
            blocks[lx][ly][lz] = type;
        }
    }

    public byte getBlock(int lx, int ly, int lz) {
        if (lx < 0 || lx >= SIZE || lz < 0 || lz >= SIZE || ly < 0 || ly >= HEIGHT) return 0;
        return blocks[lx][ly][lz];
    }
}
