package SwordsGame.client;

import SwordsGame.server.Chunk;
import SwordsGame.server.ChunkManager;

public final class Targeting {
    private Targeting() {
    }

    public static int[] pickTopFace(ChunkManager cm, float[] origin, float[] direction) {
        float blockScale = World.BLOCK_SCALE;
        float totalOffsetBlocks = cm.getWorldSizeInBlocks() / 2.0f;

        double ox = (origin[0] / blockScale) + totalOffsetBlocks;
        double oy = origin[1] / blockScale;
        double oz = (origin[2] / blockScale) + totalOffsetBlocks;

        double dx = direction[0];
        double dy = direction[1];
        double dz = direction[2];

        int x = (int) Math.floor(ox);
        int y = (int) Math.floor(oy);
        int z = (int) Math.floor(oz);

        int stepX = dx > 0 ? 1 : -1;
        int stepY = dy > 0 ? 1 : -1;
        int stepZ = dz > 0 ? 1 : -1;

        double tMaxX = intBound(ox, dx);
        double tMaxY = intBound(oy, dy);
        double tMaxZ = intBound(oz, dz);

        double tDeltaX = dx == 0 ? Double.POSITIVE_INFINITY : Math.abs(1.0 / dx);
        double tDeltaY = dy == 0 ? Double.POSITIVE_INFINITY : Math.abs(1.0 / dy);
        double tDeltaZ = dz == 0 ? Double.POSITIVE_INFINITY : Math.abs(1.0 / dz);

        int maxSteps = cm.getWorldSizeInBlocks() * 2;

        for (int i = 0; i < maxSteps; i++) {
            if (y >= 0 && y < Chunk.HEIGHT && cm.isTopSurface(x, y, z)) {
                return new int[]{x, y, z};
            }

            if (tMaxX < tMaxY) {
                if (tMaxX < tMaxZ) {
                    x += stepX;
                    tMaxX += tDeltaX;
                } else {
                    z += stepZ;
                    tMaxZ += tDeltaZ;
                }
            } else {
                if (tMaxY < tMaxZ) {
                    y += stepY;
                    tMaxY += tDeltaY;
                } else {
                    z += stepZ;
                    tMaxZ += tDeltaZ;
                }
            }

            if (x < 0 || z < 0 || x >= cm.getWorldSizeInBlocks() || z >= cm.getWorldSizeInBlocks()) {
                return null;
            }
            if (y < 0 || y >= Chunk.HEIGHT) {
                continue;
            }
        }

        return null;
    }

    private static double intBound(double s, double ds) {
        if (ds == 0) return Double.POSITIVE_INFINITY;
        double sIsInteger = Math.floor(s);
        if (ds > 0) {
            return (sIsInteger + 1 - s) / ds;
        }
        return (s - sIsInteger) / -ds;
    }
}
