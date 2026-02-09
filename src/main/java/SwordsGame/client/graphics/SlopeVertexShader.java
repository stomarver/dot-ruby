package SwordsGame.client.graphics;

public class SlopeVertexShader {
    private static final float BOTTOM = -1.0f;
    private static final float DIAGONAL_DROP = 0.75f;
    private static final float EPSILON = 0.001f;

    public float adjustY(float x, float y, float z, boolean[] faces) {
        if (y < 1.0f - EPSILON) {
            return y;
        }
        if (!faces[2]) {
            return y;
        }
        boolean xPos = x > 0.5f && faces[4];
        boolean xNeg = x < -0.5f && faces[5];
        boolean zPos = z > 0.5f && faces[0];
        boolean zNeg = z < -0.5f && faces[1];
        int exposed = (xPos ? 1 : 0) + (xNeg ? 1 : 0) + (zPos ? 1 : 0) + (zNeg ? 1 : 0);
        if (exposed == 0) {
            return y;
        }
        float drop = exposed >= 2 ? DIAGONAL_DROP : EDGE_DROP;
        return y - drop;
    }
}
