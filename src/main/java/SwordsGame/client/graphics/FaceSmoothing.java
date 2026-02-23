package SwordsGame.client.graphics;

import org.joml.Vector3f;

public class FaceSmoothing {
    private static final Vector3f SUN_DIR = new Vector3f(-0.55f, 1.0f, 0.35f).normalize();

    private final boolean front;
    private final boolean back;
    private final boolean right;
    private final boolean left;

    public FaceSmoothing(boolean[] faces) {
        this.front = faces[0];
        this.back = faces[1];
        this.right = faces[4];
        this.left = faces[5];
    }

    public float[] buildTopVertexOffsets(float dropAmount) {
        float[] offsets = new float[4];
        for (int i = 0; i < 4; i++) {
            offsets[i] = shouldLowerTopVertex(i) ? -Math.abs(dropAmount) : 0.0f;
        }
        return offsets;
    }

    public Vector3f[] buildTopVertexNormals(float[] yOffsets) {
        Vector3f[] normals = new Vector3f[4];
        for (int i = 0; i < 4; i++) {
            int prev = (i + 3) & 3;
            int next = (i + 1) & 3;

            Vector3f toPrev = edgeVector(i, prev, yOffsets);
            Vector3f toNext = edgeVector(i, next, yOffsets);

            Vector3f normal = new Vector3f(toNext).cross(toPrev);
            if (normal.lengthSquared() < 1e-6f) {
                normal.set(0f, 1f, 0f);
            } else {
                normal.normalize();
            }
            if (normal.y < 0f) {
                normal.negate();
            }
            normals[i] = normal;
        }
        return normals;
    }

    public float shadeFromNormal(Vector3f normal) {
        float diffuse = Math.max(0f, normal.dot(SUN_DIR));
        float rawShade = 0.35f + (0.65f * diffuse);

        // Keep transitions crisp between light levels (no smooth gradient bands).
        float levels = 4.0f;
        return Math.round(rawShade * levels) / levels;
    }

    private Vector3f edgeVector(int from, int to, float[] yOffsets) {
        float fx = cornerX(from);
        float fz = cornerZ(from);
        float fy = yOffsets[from];

        float tx = cornerX(to);
        float tz = cornerZ(to);
        float ty = yOffsets[to];

        return new Vector3f(tx - fx, ty - fy, tz - fz);
    }

    private float cornerX(int index) {
        return switch (index) {
            case 0, 3 -> -1f;
            default -> 1f;
        };
    }

    private float cornerZ(int index) {
        return switch (index) {
            case 0, 1 -> 1f;
            default -> -1f;
        };
    }

    private boolean shouldLowerTopVertex(int index) {
        boolean[] lowered = new boolean[4];
        lowered[0] = !front && !right && (back || left);
        lowered[1] = !back && !right && (front || left);
        lowered[2] = !back && !left && (front || right);
        lowered[3] = !front && !left && (back || right);

        // Special case for grass corner shaping:
        // if two adjacent sides are changed and the opposite two stay unchanged,
        // the corner between changed sides must be lowered one full block.
        boolean changedFront = !front;
        boolean changedBack = !back;
        boolean changedRight = !right;
        boolean changedLeft = !left;

        if (changedFront && changedRight && !changedBack && !changedLeft) {
            lowered[0] = true;
        }
        if (changedBack && changedRight && !changedFront && !changedLeft) {
            lowered[1] = true;
        }
        if (changedBack && changedLeft && !changedFront && !changedRight) {
            lowered[2] = true;
        }
        if (changedFront && changedLeft && !changedBack && !changedRight) {
            lowered[3] = true;
        }

        int loweredCount = 0;
        int loweredIndex = -1;
        for (int i = 0; i < lowered.length; i++) {
            if (lowered[i]) {
                loweredCount++;
                loweredIndex = i;
            }
        }

        if (loweredCount == 1) {
            return index == loweredIndex
                    || index == ((loweredIndex + 1) & 3)
                    || index == ((loweredIndex + 3) & 3);
        }

        return lowered[index];
    }
}
