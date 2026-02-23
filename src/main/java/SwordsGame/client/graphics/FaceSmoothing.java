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
        float step = Math.abs(amount);
        float loweredY = -step;
        float raisedY = step;

        int[] influence = new int[4];

        // Forward slope axis
        if (frontExposed && !backExposed) {
            // front side changed: front vertices down, opposite up
            influence[1] -= 1;
            influence[2] -= 1;
            influence[0] += 1;
            influence[3] += 1;
        }
        if (backExposed && !frontExposed) {
            influence[0] -= 1;
            influence[3] -= 1;
            influence[1] += 1;
            influence[2] += 1;
        }

        // Lateral slope axis
        if (leftExposed && !rightExposed) {
            influence[0] -= 1;
            influence[1] -= 1;
            influence[2] += 1;
            influence[3] += 1;
        }
        if (rightExposed && !leftExposed) {
            influence[2] -= 1;
            influence[3] -= 1;
            influence[0] += 1;
            influence[1] += 1;
        }

        // Corner bias: reinforce corners touched by two changed adjacent sides.
        if (backExposed && leftExposed) influence[0] -= 1;
        if (frontExposed && leftExposed) influence[1] -= 1;
        if (frontExposed && rightExposed) influence[2] -= 1;
        if (backExposed && rightExposed) influence[3] -= 1;

        float[] offsets = new float[4];
        for (int i = 0; i < 4; i++) {
            if (influence[i] < 0) offsets[i] = loweredY;
            else if (influence[i] > 0) offsets[i] = raisedY;
            else offsets[i] = 0.0f;
        }

        // Magnetic pass (+/- with epsilon): edge vertices should share level on exposed edge.
        propagateEdge(frontExposed, offsets, 1, 2, loweredY, raisedY);
        propagateEdge(backExposed, offsets, 0, 3, loweredY, raisedY);
        propagateEdge(leftExposed, offsets, 0, 1, loweredY, raisedY);
        propagateEdge(rightExposed, offsets, 2, 3, loweredY, raisedY);

        return offsets;
    }

    private void propagateEdge(boolean edgeExposed,
                               float[] offsets,
                               int a,
                               int b,
                               float loweredY,
                               float raisedY) {
        if (!edgeExposed) {
            return;
        }
        boolean aLower = isNear(offsets[a], loweredY);
        boolean bLower = isNear(offsets[b], loweredY);
        boolean aRaise = isNear(offsets[a], raisedY);
        boolean bRaise = isNear(offsets[b], raisedY);

        if (aLower || bLower) {
            offsets[a] = loweredY;
            offsets[b] = loweredY;
            return;
        }
        if (aRaise || bRaise) {
            offsets[a] = raisedY;
            offsets[b] = raisedY;
        }
    }

    private boolean isNear(float value, float target) {
        return Math.abs(value - target) <= CHECK_EPSILON;
    }
}
