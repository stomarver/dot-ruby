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

    public float[] buildTopVertexOffsets(float dropAmount) {
        boolean[] lowered = new boolean[4];

        // Straight-edge lowering (works for non-corner slopes too).
        if (frontExposed && !backExposed) {
            lowered[1] = true;
            lowered[2] = true;
        }
        if (backExposed && !frontExposed) {
            lowered[0] = true;
            lowered[3] = true;
        }
        if (leftExposed && !rightExposed) {
            lowered[0] = true;
            lowered[1] = true;
        }
        if (rightExposed && !leftExposed) {
            lowered[2] = true;
            lowered[3] = true;
        }

        // Corner lowering: corners fall when both adjacent sides are exposed.
        lowered[0] = lowered[0] || (backExposed && leftExposed);   // back-left
        lowered[1] = lowered[1] || (frontExposed && leftExposed);  // front-left
        lowered[2] = lowered[2] || (frontExposed && rightExposed); // front-right
        lowered[3] = lowered[3] || (backExposed && rightExposed);  // back-right

        float[] offsets = new float[4];
        float loweredY = -Math.abs(dropAmount);
        for (int i = 0; i < 4; i++) {
            offsets[i] = lowered[i] ? loweredY : 0.0f;
        }

        // Magnetic pass with epsilon check (+0.001):
        // if one corner on an exposed edge is already lowered (or near lowered),
        // the second corner on the same edge is snapped to that lower level too.
        if (frontExposed && (isLowered(offsets[1], loweredY) || isLowered(offsets[2], loweredY))) {
            offsets[1] = loweredY;
            offsets[2] = loweredY;
        }
        if (backExposed && (isLowered(offsets[0], loweredY) || isLowered(offsets[3], loweredY))) {
            offsets[0] = loweredY;
            offsets[3] = loweredY;
        }
        if (leftExposed && (isLowered(offsets[0], loweredY) || isLowered(offsets[1], loweredY))) {
            offsets[0] = loweredY;
            offsets[1] = loweredY;
        }
        if (rightExposed && (isLowered(offsets[2], loweredY) || isLowered(offsets[3], loweredY))) {
            offsets[2] = loweredY;
            offsets[3] = loweredY;
        }

        return offsets;
    }

    private boolean isLowered(float value, float loweredY) {
        return Math.abs(value - loweredY) <= CHECK_EPSILON;
    }
}
