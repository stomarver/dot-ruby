package SwordsGame.server;

import SwordsGame.server.Terrain;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ChunkManager {
    private static final int WORLD_SIZE_BLOCKS = 2048;
    private static final int WORLD_SIZE_CHUNKS = WORLD_SIZE_BLOCKS / Chunk.SIZE;
    private static final int WORLD_RADIUS_BLOCKS = WORLD_SIZE_BLOCKS / 2;
    private final int worldSizeInChunks = WORLD_SIZE_CHUNKS;
    private final Chunk[] chunks;

    public ChunkManager() {
        this.chunks = new Chunk[worldSizeInChunks * worldSizeInChunks];
        generateWorld();
    }

    private void generateWorld() {
        System.out.println("[Server] Generating world " + WORLD_SIZE_BLOCKS + "x" + WORLD_SIZE_BLOCKS + "...");

        int threads = Math.max(1, Runtime.getRuntime().availableProcessors() - 1);
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        try {
            List<Callable<Void>> tasks = new ArrayList<>();
            for (int cx = 0; cx < worldSizeInChunks; cx++) {
                final int fx = cx;
                tasks.add(() -> {
                    for (int cz = 0; cz < worldSizeInChunks; cz++) {
                        Chunk chunk = new Chunk(fx, cz);
                        Terrain.generate(chunk);
                        chunks[toChunkIndex(fx, cz)] = chunk;
                    }
                    return null;
                });
            }

            List<Future<Void>> futures = pool.invokeAll(tasks);
            for (Future<Void> future : futures) {
                future.get();
            }
        } catch (Exception e) {
            throw new IllegalStateException("World generation failed", e);
        } finally {
            pool.shutdown();
        }

        System.out.println("[Server] World generation finished.");
    }

    public Chunk getChunk(int cx, int cz) {
        if (cx < 0 || cz < 0 || cx >= worldSizeInChunks || cz >= worldSizeInChunks) return null;
        return chunks[toChunkIndex(cx, cz)];
    }

    public Chunk getChunkAtWorld(int wx, int wz) {
        if (!isInsideWorld(wx, wz)) return null;
        int cx = wx / Chunk.SIZE;
        int cz = wz / Chunk.SIZE;
        return getChunk(cx, cz);
    }

    public byte getBlockAtWorld(int wx, int wy, int wz) {
        if (!isInsideWorld(wx, wz) || wy < 0 || wy >= Chunk.HEIGHT) return 0;
        Chunk chunk = getChunkAtWorld(wx, wz);
        if (chunk == null) return 0;
        return chunk.getBlock(wx % Chunk.SIZE, wy, wz % Chunk.SIZE);
    }

    public void setBlockAtWorld(int wx, int wy, int wz, byte type) {
        if (!isInsideWorld(wx, wz) || wy < 0 || wy >= Chunk.HEIGHT) return;
        Chunk chunk = getChunkAtWorld(wx, wz);
        if (chunk == null) return;
        chunk.setBlock(wx % Chunk.SIZE, wy, wz % Chunk.SIZE, type);
    }

    public boolean isTopSurface(int wx, int wy, int wz) {
        if (wy < 0 || wy >= Chunk.HEIGHT) return false;
        byte current = getBlockAtWorld(wx, wy, wz);
        if (current == 0) return false;
        if (wy == Chunk.HEIGHT - 1) return true;
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
