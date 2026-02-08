package SwordsGame.client;

public final class Culling {
    private Culling() {
    }

    public static int maxLoopDist(float horizDist, float vertDist) {
        return (int) Math.ceil(Math.max(horizDist, vertDist)) + 2;
    }

    public static boolean isChunkVisible(int dx, int dz, float sinTheta, float cosTheta,
                                         float horizDist, float vertDist) {
        float depth = dx * (-sinTheta) + dz * cosTheta;
        float lateral = dx * cosTheta + dz * sinTheta;
        return Math.abs(depth) <= vertDist + 0.5f && Math.abs(lateral) <= horizDist + 0.5f;
    }
}
