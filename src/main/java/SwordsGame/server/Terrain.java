package SwordsGame.server;

import SwordsGame.shared.world.BlockId;

public class Terrain {
    private static final int GROUND_BASE_Y = 3;
    private static final int GROUND_HEIGHT_RANGE = 6;
    private static final int PILLAR_BASE_Y = 18;
    private static final int PILLAR_HEIGHT_RANGE = 10;

    private Terrain() {
    }

    private static int noise8(int x, int z, int seed) {
        int n = x * 73428767 ^ z * 912931 ^ seed * 19990303;
        n = (n << 13) ^ n;
        int hash = n * (n * n * 15731 + 789221) + 1376312589;
        return (hash >>> 16) & 0xFF;
    }

    private static int quantizedNoise8(int worldX, int worldZ, int cellSize, int seed) {
        int qx = Math.floorDiv(worldX, cellSize);
        int qz = Math.floorDiv(worldZ, cellSize);
        return noise8(qx, qz, seed);
    }

    public static void generate(Chunk chunk) {
        for (int x = 0; x < Chunk.SIZE; x++) {
            for (int z = 0; z < Chunk.SIZE; z++) {
                int worldX = chunk.x * Chunk.SIZE + x;
                int worldZ = chunk.z * Chunk.SIZE + z;

                int groundByte = quantizedNoise8(worldX, worldZ, 2, 1);
                int groundY = GROUND_BASE_Y + (groundByte * GROUND_HEIGHT_RANGE / 255);

                int pillarMask = quantizedNoise8(worldX, worldZ, 4, 2);
                boolean hasPillar = pillarMask > 192;

                int pillarTopY = groundY;
                if (hasPillar) {
                    int pillarHeightByte = quantizedNoise8(worldX + 1000, worldZ - 1000, 4, 3);
                    pillarTopY = PILLAR_BASE_Y + (pillarHeightByte * PILLAR_HEIGHT_RANGE / 255);
                }

                for (int y = 0; y < Chunk.HEIGHT; y++) {
                    byte block = resolveBlock(y, groundY, pillarTopY, hasPillar);
                    chunk.setBlock(x, y, z, block);
                }
            }
        }
    }

    private static byte resolveBlock(int y, int groundY, int pillarTopY, boolean hasPillar) {
        if (y < groundY) {
            return BlockId.STONE;
        }
        if (y == groundY) {
            return BlockId.GRASS;
        }

        if (!hasPillar) {
            return BlockId.AIR;
        }

        if (y < pillarTopY) {
            return BlockId.COBBLE;
        }
        if (y == pillarTopY) {
            return BlockId.GRASS;
        }

        return BlockId.AIR;
    }
}
