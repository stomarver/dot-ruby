package SwordsGame.server;

import SwordsGame.server.structures.TerrainGenerator;

public class ChunkManager {
    private final int worldSizeInChunks = 64;
    private final Chunk[][] chunks;

    public ChunkManager() {
        this.chunks = new Chunk[worldSizeInChunks][worldSizeInChunks];
        generateWorld();
    }

    private void generateWorld() {
        System.out.println("[Server] Generating world 1024x1024...");
        for (int cx = 0; cx < worldSizeInChunks; cx++) {
            for (int cz = 0; cz < worldSizeInChunks; cz++) {
                chunks[cx][cz] = new Chunk(cx, cz);
                TerrainGenerator.generate(chunks[cx][cz]);
            }
        }
        System.out.println("[Server] World generation finished.");
    }

    public Chunk[][] getChunks() { return chunks; }
    public int getWorldSizeInChunks() { return worldSizeInChunks; }
}
