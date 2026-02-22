package SwordsGame.server;

import SwordsGame.server.Ter;

public class ChMgr {
    private static final int WORLD_SIZE_BLOCKS = 2048;
    private static final int WORLD_SIZE_CHUNKS = WORLD_SIZE_BLOCKS / Chk.SIZE;
    private static final int WORLD_RADIUS_BLOCKS = WORLD_SIZE_BLOCKS / 2;
    private final int worldSizeInChunks = WORLD_SIZE_CHUNKS;
    private final Chk[] chunks;

    public ChMgr() {
        this.chunks = new Chk[worldSizeInChunks * worldSizeInChunks];
        generateWorld();
    }

    private void generateWorld() {
        System.out.println("[Server] Generating world " + WORLD_SIZE_BLOCKS + "x" + WORLD_SIZE_BLOCKS + "...");
        for (int cx = 0; cx < worldSizeInChunks; cx++) {
            for (int cz = 0; cz < worldSizeInChunks; cz++) {
                Chk chunk = new Chk(cx, cz);
                chunks[toChunkIndex(cx, cz)] = chunk;
                Ter.generate(chunk);
            }
        }
        System.out.println("[Server] Wld generation finished.");
    }

    public Chk getChunk(int cx, int cz) {
        if (cx < 0 || cz < 0 || cx >= worldSizeInChunks || cz >= worldSizeInChunks) return null;
        return chunks[toChunkIndex(cx, cz)];
    }

    public Chk getChunkAtWorld(int wx, int wz) {
        if (!isInsideWorld(wx, wz)) return null;
        int cx = wx / Chk.SIZE;
        int cz = wz / Chk.SIZE;
        return getChunk(cx, cz);
    }

    public byte getBlockAtWorld(int wx, int wy, int wz) {
        if (!isInsideWorld(wx, wz) || wy < 0 || wy >= Chk.HEIGHT) return 0;
        Chk chunk = getChunkAtWorld(wx, wz);
        if (chunk == null) return 0;
        return chunk.getBlock(wx % Chk.SIZE, wy, wz % Chk.SIZE);
    }

    public void setBlockAtWorld(int wx, int wy, int wz, byte type) {
        if (!isInsideWorld(wx, wz) || wy < 0 || wy >= Chk.HEIGHT) return;
        Chk chunk = getChunkAtWorld(wx, wz);
        if (chunk == null) return;
        chunk.setBlock(wx % Chk.SIZE, wy, wz % Chk.SIZE, type);
    }

    public boolean isTopSurface(int wx, int wy, int wz) {
        if (wy < 0 || wy >= Chk.HEIGHT) return false;
        byte current = getBlockAtWorld(wx, wy, wz);
        if (current == 0) return false;
        if (wy == Chk.HEIGHT - 1) return true;
        return getBlockAtWorld(wx, wy + 1, wz) == 0;
    }

    public boolean isInsideWorld(int wx, int wz) {
        return wx >= 0 && wz >= 0 && wx < WORLD_SIZE_BLOCKS && wz < WORLD_SIZE_BLOCKS;
    }

    public int getWorldSizeInChunks() { return worldSizeInChunks; }
    public int getWorldSizeInBlocks() { return WORLD_SIZE_BLOCKS; }
    public int getWorldRadiusBlocks() { return WORLD_RADIUS_BLOCKS; }

    private int toChunkIndex(int cx, int cz) {
        return (cz * worldSizeInChunks) + cx;
    }
}
