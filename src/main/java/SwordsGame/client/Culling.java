package SwordsGame.client;

public final class Culling {
    private Culling() {
    }

    public static int maxLoopDist(float horizDist, float vertDist) {
        return (int) Math.ceil(Math.max(horizDist, vertDist)) + 2;
    }

    public static boolean isChunkVisible(float relX, float relZ, float sinTheta, float cosTheta,
                                         float horizDist, float vertDist) {
        float depth = relX * (-sinTheta) + relZ * cosTheta;
        float lateral = relX * cosTheta + relZ * sinTheta;
        return Math.abs(depth) <= vertDist + 0.5f && Math.abs(lateral) <= horizDist + 0.5f;
    }

    public static float relX(float camChunkX, int chunkX) {
        return (chunkX + 0.5f) - camChunkX;
    }

    public static float relZ(float camChunkZ, int chunkZ) {
        return (chunkZ + 0.5f) - camChunkZ;
    }
}
