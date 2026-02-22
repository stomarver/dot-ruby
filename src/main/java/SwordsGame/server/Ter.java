package SwordsGame.server;

import SwordsGame.shared.world.BlockId;

public class Ter {
    private static final int GROUND_BASE_Y = 4;
    private static final int GROUND_HEIGHT_RANGE = 5;

    private static final int PLATEAU_RISE_BASE = 18;
    private static final int PLATEAU_RISE_RANGE = 4;

    private static final int FEATURE_CELL = 40;

    private Ter() {
    }

    private static int noise8(int x, int z, int seed) {
        int n = x * 73428767 ^ z * 912931 ^ seed * 19990303;
        n = (n << 13) ^ n;
        int hash = n * (n * n * 15731 + 789221) + 1376312589;
        return (hash >>> 16) & 0xFF;
    }

    private static int lerpInt(int a, int b, int t) {
        return a + ((b - a) * t) / 255;
    }

    private static int smoothStep8(int t) {
        return (t * t * (765 - (t << 1))) >> 16;
    }

    private static int sampleSmoothNoise8(int worldX, int worldZ, int scale, int seed) {
        int cellX = Math.floorDiv(worldX, scale);
        int cellZ = Math.floorDiv(worldZ, scale);

        int localX = Math.floorMod(worldX, scale);
        int localZ = Math.floorMod(worldZ, scale);

        int tx = (localX * 255) / (scale - 1);
        int tz = (localZ * 255) / (scale - 1);

        int sx = smoothStep8(tx);
        int sz = smoothStep8(tz);

        int n00 = noise8(cellX, cellZ, seed);
        int n10 = noise8(cellX + 1, cellZ, seed);
        int n01 = noise8(cellX, cellZ + 1, seed);
        int n11 = noise8(cellX + 1, cellZ + 1, seed);

        int nx0 = lerpInt(n00, n10, sx);
        int nx1 = lerpInt(n01, n11, sx);
        return lerpInt(nx0, nx1, sz);
    }

    private static int clamp255(int value) {
        return Math.max(0, Math.min(255, value));
    }

    private static int sampleGroundByte(int worldX, int worldZ) {
        int broad = sampleSmoothNoise8(worldX, worldZ, 72, 11);
        int mid = sampleSmoothNoise8(worldX, worldZ, 30, 12);
        int fine = sampleSmoothNoise8(worldX, worldZ, 14, 13);
        return clamp255((broad * 5 + mid * 3 + fine * 2) / 10);
    }

    private static int computePlateauMask(int worldX, int worldZ) {
        int warpX = sampleSmoothNoise8(worldX + 1200, worldZ - 1200, 22, 21) - 128;
        int warpZ = sampleSmoothNoise8(worldX - 900, worldZ + 900, 22, 22) - 128;

        int px = worldX + warpX / 6;
        int pz = worldZ + warpZ / 6;

        int cellX = Math.floorDiv(px, FEATURE_CELL);
        int cellZ = Math.floorDiv(pz, FEATURE_CELL);

        int strongest = 0;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                int fx = cellX + dx;
                int fz = cellZ + dz;

                int density = noise8(fx, fz, 30);
                if (density < 168) {
                    continue;
                }

                int jitterX = (noise8(fx, fz, 31) * FEATURE_CELL) / 255;
                int jitterZ = (noise8(fx, fz, 32) * FEATURE_CELL) / 255;

                int centerX = fx * FEATURE_CELL + jitterX;
                int centerZ = fz * FEATURE_CELL + jitterZ;

                int radius = 10 + (noise8(fx, fz, 33) * 8 / 255);
                int rx = px - centerX;
                int rz = pz - centerZ;

                int distSq = rx * rx + rz * rz;
                int radiusSq = radius * radius;

                if (distSq >= radiusSq) {
                    continue;
                }

                int mask = ((radiusSq - distSq) * 255) / radiusSq;
                strongest = Math.max(strongest, smoothStep8(mask));
            }
        }

        return strongest;
    }

    public static void generate(Chk chunk) {
        for (int x = 0; x < Chk.SIZE; x++) {
            for (int z = 0; z < Chk.SIZE; z++) {
                int worldX = chunk.x * Chk.SIZE + x;
                int worldZ = chunk.z * Chk.SIZE + z;

                int groundByte = sampleGroundByte(worldX, worldZ);
                int groundY = GROUND_BASE_Y + (groundByte * GROUND_HEIGHT_RANGE / 255);

                int plateauMask = computePlateauMask(worldX, worldZ);
                boolean hasPlateau = plateauMask > 172;

                int plateauTopY = groundY;
                if (hasPlateau) {
                    int plateauByte = sampleSmoothNoise8(worldX + 700, worldZ - 700, 26, 40);
                    int rise = PLATEAU_RISE_BASE + (plateauByte * PLATEAU_RISE_RANGE / 255);
                    plateauTopY = Math.min(Chk.HEIGHT - 2, groundY + rise);
                }

                for (int y = 0; y < Chk.HEIGHT; y++) {
                    chunk.setBlock(x, y, z, resolveBlock(y, groundY, plateauTopY, hasPlateau));
                }
            }
        }
    }

    private static byte resolveBlock(int y, int groundY, int plateauTopY, boolean hasPlateau) {
        if (!hasPlateau) {
            if (y <= groundY) {
                return BlockId.GRASS;
            }
            return BlockId.AIR;
        }

        if (y < groundY) {
            return BlockId.GRASS;
        }

        if (y == groundY) {
            return BlockId.STONE;
        }

        if (y < plateauTopY) {
            return BlockId.COBBLE;
        }

        if (y == plateauTopY) {
            return BlockId.GRASS;
        }

        return BlockId.AIR;
    }
}
