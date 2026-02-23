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

        boolean n = frontExposed;
        boolean s = backExposed;
        boolean e = rightExposed;
        boolean w = leftExposed;

        float[] proposed = new float[] {0.0f, 0.0f, 0.0f, 0.0f};

        // Priority 1: outer corners (convex silhouettes).
        if (s && w) proposed[0] = loweredY; // back-left
        if (n && w) proposed[1] = loweredY; // front-left
        if (n && e) proposed[2] = loweredY; // front-right
        if (s && e) proposed[3] = loweredY; // back-right

        // Priority 2: inner corners (concave closure between already settled corners).
        settleInnerCorners(proposed, loweredY);

        // Priority 3: side ramps (whole exposed edge must share level).
        propagateLoweredEdge(n, proposed, 1, 2, loweredY);
        propagateLoweredEdge(s, proposed, 0, 3, loweredY);
        propagateLoweredEdge(w, proposed, 0, 1, loweredY);
        propagateLoweredEdge(e, proposed, 2, 3, loweredY);

        // Priority 4: single-face cases (one exposed side still forms a clean ramp).
        settleSingleFaceCases(n, s, e, w, proposed, loweredY);

        // Priority 5: seal pass - any corner touching an exposed side cannot stay "floating".
        sealExposedCorners(n, s, e, w, proposed, loweredY);

        // Final closure for any remaining lone gap.
        settleInnerCorners(proposed, loweredY);

        float center = (proposed[0] + proposed[1] + proposed[2] + proposed[3]) * 0.25f;
        return new Topology(proposed, center);
    }

    private void settleSingleFaceCases(boolean n, boolean s, boolean e, boolean w,
                                       float[] offsets, float loweredY) {
        int exposedCount = (n ? 1 : 0) + (s ? 1 : 0) + (e ? 1 : 0) + (w ? 1 : 0);
        if (exposedCount != 1) {
            return;
        }

        if (n) {
            offsets[1] = loweredY;
            offsets[2] = loweredY;
        } else if (s) {
            offsets[0] = loweredY;
            offsets[3] = loweredY;
        } else if (w) {
            offsets[0] = loweredY;
            offsets[1] = loweredY;
        } else {
            offsets[2] = loweredY;
            offsets[3] = loweredY;
        }
    }

    private void sealExposedCorners(boolean n, boolean s, boolean e, boolean w,
                                    float[] offsets, float loweredY) {
        if (s || w) {
            offsets[0] = loweredY;
        }
        if (n || w) {
            offsets[1] = loweredY;
        }
        if (n || e) {
            offsets[2] = loweredY;
        }
        if (s || e) {
            offsets[3] = loweredY;
        }
    }

    private void settleInnerCorners(float[] offsets, float loweredY) {
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
