package SwordsGame.server.functions;

import SwordsGame.client.blocks.Type;
import SwordsGame.client.World;
import SwordsGame.server.Chunk;
import SwordsGame.server.ChunkManager;

public class Explosion {
    public static void createSphere(ChunkManager cm, World world, int centerX, int centerY, int centerZ) {
        if (!canExplode(cm, centerX, centerY, centerZ)) return;

        float radius = 7.0f;
        int r = (int) Math.ceil(radius);

        for (int dx = -r; dx <= r; dx++) {
            for (int dy = -r; dy <= r; dy++) {
                for (int dz = -r; dz <= r; dz++) {
                    if (dx * dx + dy * dy + dz * dz <= radius * radius) {
                        removeBlock(cm, world, centerX + dx, centerY + dy, centerZ + dz);
                    }
                }
            }
        }
    }

    private static boolean canExplode(ChunkManager cm, int wx, int wy, int wz) {
        byte blockAtPos = getBlockAtWorldPos(cm, wx, wy, wz);
        byte blockBelow = getBlockAtWorldPos(cm, wx, wy - 1, wz);

        boolean isCorrectTarget = (blockAtPos == Type.GRASS.id || blockAtPos == Type.STONE.id);
        boolean isNotBlocked = (blockBelow != Type.COBBLE.id);

        return isCorrectTarget && isNotBlocked;
    }

    private static byte getBlockAtWorldPos(ChunkManager cm, int wx, int wy, int wz) {
        if (wy < 0 || wy >= Chunk.HEIGHT) return Type.AIR.id;
        int worldSize = cm.getWorldSizeInChunks() * Chunk.SIZE;
        if (wx < 0 || wx >= worldSize || wz < 0 || wz >= worldSize) return Type.AIR.id;
        int cx = wx / Chunk.SIZE;
        int cz = wz / Chunk.SIZE;
        return cm.getChunks()[cx][cz].getBlock(wx % Chunk.SIZE, wy, wz % Chunk.SIZE);
    }

    private static void removeBlock(ChunkManager cm, World world, int wx, int wy, int wz) {
        if (wy < 0 || wy >= Chunk.HEIGHT) return;
        int worldSize = cm.getWorldSizeInChunks() * Chunk.SIZE;
        if (wx < 0 || wx >= worldSize || wz < 0 || wz >= worldSize) return;

        Chunk chunk = cm.getChunks()[wx / Chunk.SIZE][wz / Chunk.SIZE];
        byte blockType = chunk.getBlock(wx % Chunk.SIZE, wy, wz % Chunk.SIZE);

        if (blockType == Type.COBBLE.id) {
            world.addFallingBlock(wx, wy, wz, blockType);
            chunk.setBlock(wx % Chunk.SIZE, wy, wz % Chunk.SIZE, Type.AIR.id);
            world.markChunkDirty(chunk);
        }
    }
}
