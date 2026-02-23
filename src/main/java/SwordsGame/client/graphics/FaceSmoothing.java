package SwordsGame.client.graphics;

public class FaceSmoothing {
    private static final float CHECK_EPSILON = 0.001f;

    private final boolean frontExposed;
    private final boolean backExposed;
    private final boolean rightExposed;
    private final boolean leftExposed;

    public FaceSmoothing(boolean[] faces) {
        this.frontExposed = faces[0];
        this.backExposed = faces[1];
        this.rightExposed = faces[4];
        this.leftExposed = faces[5];
    }

    public Topology buildTopology(float amount, int wx, int wy, int wz) {
        float loweredY = -Math.abs(amount);

        // 2) Neighbor query pass (8-neighborhood around top face).
        boolean n = frontExposed;
        boolean s = backExposed;
        boolean e = rightExposed;
        boolean w = leftExposed;
        boolean ne = n || e;
        boolean nw = n || w;
        boolean se = s || e;
        boolean sw = s || w;

        // 3) Vertex proposal pass.
        float[] proposed = new float[] {0.0f, 0.0f, 0.0f, 0.0f};
        if (s || w || sw) proposed[0] = loweredY; // back-left
        if (n || w || nw) proposed[1] = loweredY; // front-left
        if (n || e || ne) proposed[2] = loweredY; // front-right
        if (s || e || se) proposed[3] = loweredY; // back-right

        // Keep exposed edges coherent.
        propagateLoweredEdge(n, proposed, 1, 2, loweredY);
        propagateLoweredEdge(s, proposed, 0, 3, loweredY);
        propagateLoweredEdge(w, proposed, 0, 1, loweredY);
        propagateLoweredEdge(e, proposed, 2, 3, loweredY);

        closeLoneCorner(proposed, loweredY);

        // 6) Displacement pass is intentionally disabled for stable DK-style ramps.
        float center = (proposed[0] + proposed[1] + proposed[2] + proposed[3]) * 0.25f;
        return new Topology(proposed, center);
    }

    private void closeLoneCorner(float[] offsets, float loweredY) {
        boolean[] lowered = new boolean[4];
        for (int i = 0; i < 4; i++) {
            lowered[i] = isNear(offsets[i], loweredY);
        }
        for (int i = 0; i < 4; i++) {
            if (lowered[i]) {
                continue;
            }
            int prev = (i + 3) & 3;
            int next = (i + 1) & 3;
            if (lowered[prev] && lowered[next]) {
                offsets[i] = loweredY;
            }
        }
    }

    private void propagateLoweredEdge(boolean edgeExposed,
                                      float[] offsets,
                                      int a,
                                      int b,
                                      float loweredY) {
        if (!edgeExposed) {
            return;
        }
        if (isNear(offsets[a], loweredY) || isNear(offsets[b], loweredY)) {
            offsets[a] = loweredY;
            offsets[b] = loweredY;
        }
    }

    private boolean isNear(float value, float target) {
        return Math.abs(value - target) <= CHECK_EPSILON;
    }

    public record Topology(float[] proposedOffsets, float centerOffset) {
    }
}
