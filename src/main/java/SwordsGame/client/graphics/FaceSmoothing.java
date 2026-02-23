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

    public float[] buildTopVertexOffsets(float amount) {
        float loweredY = -Math.abs(amount);
        float[] offsets = new float[] {0.0f, 0.0f, 0.0f, 0.0f};

        // Vertex-to-lower-vertex magnetic rule:
        // each corner snaps down if at least one adjacent side is exposed.
        if (backExposed || leftExposed) offsets[0] = loweredY;   // back-left
        if (frontExposed || leftExposed) offsets[1] = loweredY;  // front-left
        if (frontExposed || rightExposed) offsets[2] = loweredY; // front-right
        if (backExposed || rightExposed) offsets[3] = loweredY;  // back-right

        // Edge consistency: if one corner of an exposed edge is lowered,
        // the second corner on that same edge is forced to the same lowered level.
        propagateLoweredEdge(frontExposed, offsets, 1, 2, loweredY);
        propagateLoweredEdge(backExposed, offsets, 0, 3, loweredY);
        propagateLoweredEdge(leftExposed, offsets, 0, 1, loweredY);
        propagateLoweredEdge(rightExposed, offsets, 2, 3, loweredY);

        return offsets;
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
}
