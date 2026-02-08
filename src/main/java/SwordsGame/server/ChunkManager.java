package SwordsGame.server;

import SwordsGame.server.Terrain;

public class ChunkManager {
    private static final int WORLD_SIZE_BLOCKS = 2048;
    private static final int WORLD_SIZE_CHUNKS = WORLD_SIZE_BLOCKS / Chunk.SIZE;
    private static final int WORLD_RADIUS_BLOCKS = WORLD_SIZE_BLOCKS / 2;
    private final int worldSizeInChunks = WORLD_SIZE_CHUNKS;
    private final Chunk[][] chunks;

    public ChunkManager() {
        this.chunks = new Chunk[worldSizeInChunks][worldSizeInChunks];
        generateWorld();
    }

    private void generateWorld() {
        System.out.println("[Server] Generating world " + WORLD_SIZE_BLOCKS + "x" + WORLD_SIZE_BLOCKS + "...");
        for (int cx = 0; cx < worldSizeInChunks; cx++) {
            for (int cz = 0; cz < worldSizeInChunks; cz++) {
                chunks[cx][cz] = new Chunk(cx, cz);
                Terrain.generate(chunks[cx][cz]);
            }
        }
        System.out.println("[Server] World generation finished.");
    }

    public Chunk[][] getChunks() { return chunks; }
    public int getWorldSizeInChunks() { return worldSizeInChunks; }
    public int getWorldSizeInBlocks() { return WORLD_SIZE_BLOCKS; }
    public int getWorldRadiusBlocks() { return WORLD_RADIUS_BLOCKS; }
}
