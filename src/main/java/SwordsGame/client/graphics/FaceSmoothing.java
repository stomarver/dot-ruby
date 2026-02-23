package SwordsGame.client.graphics;

public class FaceSmoothing {
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

        // Base lowering: corners fall to the lower vertex when both adjacent sides are exposed.
        lowered[0] = backExposed && leftExposed;   // back-left
        lowered[1] = frontExposed && leftExposed;  // front-left
        lowered[2] = frontExposed && rightExposed; // front-right
        lowered[3] = backExposed && rightExposed;  // back-right

        // Magnetic pass: if one corner on an exposed edge is lowered,
        // the second corner on the same edge follows to the same lower level.
        if (frontExposed && (lowered[1] || lowered[2])) {
            lowered[1] = true;
            lowered[2] = true;
        }
        if (backExposed && (lowered[0] || lowered[3])) {
            lowered[0] = true;
            lowered[3] = true;
        }
        if (leftExposed && (lowered[0] || lowered[1])) {
            lowered[0] = true;
            lowered[1] = true;
        }
        if (rightExposed && (lowered[2] || lowered[3])) {
            lowered[2] = true;
            lowered[3] = true;
        }

        float[] offsets = new float[4];
        float loweredY = -Math.abs(dropAmount);
        for (int i = 0; i < 4; i++) {
            offsets[i] = lowered[i] ? loweredY : 0.0f;
        }
        return offsets;
    }
}
