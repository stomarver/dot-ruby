package SwordsGame.server.structures;

import SwordsGame.client.blocks.BlockType;
import SwordsGame.server.Chunk;

public class TerrainGenerator {
    private static double rawNoise(int x, int z) {
        int n = x + z * 1337;
        n = (n << 13) ^ n;
        return (1.0 - ((n * (n * n * 15731 + 789221) + 1376312589) & 0x7fffffff) / 1073741824.0);
    }

    private static double getNoise(double x, double z) {
        int ix = (int) Math.floor(x);
        int iz = (int) Math.floor(z);
        double fx = x - ix;
        double fz = z - iz;

        double ft = fx * Math.PI;
        double f = (1 - Math.cos(ft)) * 0.5;

        double v1 = rawNoise(ix, iz);
        double v2 = rawNoise(ix + 1, iz);
        double v3 = rawNoise(ix, iz + 1);
        double v4 = rawNoise(ix + 1, iz + 1);

        double i1 = v1 + (v2 - v1) * f;
        double i2 = v3 + (v4 - v3) * f;

        double ftz = fz * Math.PI;
        double fz_l = (1 - Math.cos(ftz)) * 0.5;

        return i1 + (i2 - i1) * fz_l;
    }

    public static void generate(Chunk chunk) {
        for (int x = 0; x < Chunk.SIZE; x++) {
            for (int z = 0; z < Chunk.SIZE; z++) {
                int wx = chunk.x * Chunk.SIZE + x;
                int wz = chunk.z * Chunk.SIZE + z;

                double groundNoise = getNoise(wx * 0.06, wz * 0.06);
                int groundY = (int) ((groundNoise + 1.0) * 2.5);

                double islandReliefNoise = getNoise((wx + 1000) * 0.06, (wz + 1000) * 0.06);
                int islandReliefY = (int) ((islandReliefNoise + 1.0) * 2.5);
                int pillarTopY = 24 + islandReliefY;

                double body = getNoise(wx * 0.025, wz * 0.025);
                double edges = getNoise(wx * 0.1, wz * 0.1) * 0.15;
                double finalMask = body + edges;
                boolean isPillar = finalMask > 0.6;

                for (int y = 0; y < Chunk.HEIGHT; y++) {
                    if (isPillar) {
                        if (y == pillarTopY) {
                            chunk.setBlock(x, y, z, BlockType.GRASS.id);
                        } else if (y < pillarTopY && y > groundY) {
                            chunk.setBlock(x, y, z, BlockType.COBBLE.id);
                        } else if (y <= groundY) {
                            chunk.setBlock(x, y, z, BlockType.STONE.id);
                        } else {
                            chunk.setBlock(x, y, z, BlockType.AIR.id);
                        }
                    } else {
                        if (y == groundY) {
                            chunk.setBlock(x, y, z, BlockType.GRASS.id);
                        } else if (y < groundY) {
                            chunk.setBlock(x, y, z, BlockType.STONE.id);
                        } else {
                            chunk.setBlock(x, y, z, BlockType.AIR.id);
                        }
                    }
                }
            }
        }
    }
}
