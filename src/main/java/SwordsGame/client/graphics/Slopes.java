package SwordsGame.client.graphics;

import SwordsGame.client.World;

class Slopes {
    private static final int FACE_FRONT = 0;
    private static final int FACE_BACK = 1;
    private static final int FACE_TOP = 2;
    private static final int FACE_RIGHT = 4;
    private static final int FACE_LEFT = 5;
    private static final float LOWER_TO_BLOCK_BELOW = 2.0f;

    static void appendSlopedTopFace(FloatCollector collector, Block block, int rot,
                                    float baseX, float baseY, float baseZ, float color,
                                    boolean[] sideAir, boolean[] sideSame) {
        float[] uv = block.getUv(rot);
        float[][] verts = copyFaceVerts(MeshBuilder.FACE_VERTS[FACE_TOP]);
        int cornerRotationSteps = getCornerRotationSteps(sideSame);
        for (int i = 0; i < cornerRotationSteps; i++) {
            verts = rotateTopVerts90(verts);
        }

        boolean[] lower = new boolean[4];
        if (sideAir != null && sideAir[FACE_FRONT]) {
            lower[1] = true;
            lower[2] = true;
        }
        if (sideAir != null && sideAir[FACE_BACK]) {
            lower[0] = true;
            lower[3] = true;
        }
        if (sideAir != null && sideAir[FACE_RIGHT]) {
            lower[2] = true;
            lower[3] = true;
        }
        if (sideAir != null && sideAir[FACE_LEFT]) {
            lower[0] = true;
            lower[1] = true;
        }

        for (int i = 0; i < lower.length; i++) {
            if (lower[i]) {
                verts[i][1] -= LOWER_TO_BLOCK_BELOW;
            }
        }

        float[] center = averageVerts(verts[0], verts[2]);
        float[] uvCenter = averageUvs(uv[0], uv[1], uv[4], uv[5]);

        appendTriangleWithNormalUp(collector, verts[0], verts[1], center, baseX, baseY, baseZ,
                uv[0], uv[1], uv[2], uv[3], uvCenter[0], uvCenter[1], color);
        appendTriangleWithNormalUp(collector, verts[1], verts[2], center, baseX, baseY, baseZ,
                uv[2], uv[3], uv[4], uv[5], uvCenter[0], uvCenter[1], color);
        appendTriangleWithNormalUp(collector, verts[2], verts[3], center, baseX, baseY, baseZ,
                uv[4], uv[5], uv[6], uv[7], uvCenter[0], uvCenter[1], color);
        appendTriangleWithNormalUp(collector, verts[3], verts[0], center, baseX, baseY, baseZ,
                uv[6], uv[7], uv[0], uv[1], uvCenter[0], uvCenter[1], color);
    }

    private static float[][] copyFaceVerts(float[][] source) {
        float[][] copy = new float[source.length][3];
        for (int i = 0; i < source.length; i++) {
            copy[i][0] = source[i][0];
            copy[i][1] = source[i][1];
            copy[i][2] = source[i][2];
        }
        return copy;
    }

    private static float[][] rotateTopVerts90(float[][] source) {
        float[][] rotated = new float[source.length][3];
        for (int i = 0; i < source.length; i++) {
            rotated[i][0] = -source[i][2];
            rotated[i][1] = source[i][1];
            rotated[i][2] = source[i][0];
        }
        return rotated;
    }

    private static int getCornerRotationSteps(boolean[] sideSame) {
        if (sideSame == null) {
            return 0;
        }
        boolean front = sideSame[FACE_FRONT];
        boolean back = sideSame[FACE_BACK];
        boolean right = sideSame[FACE_RIGHT];
        boolean left = sideSame[FACE_LEFT];
        int openSides = countOpenSides(front, back, right, left);
        if (openSides != 2) {
            return 0;
        }
        if (front && right) {
            return 0;
        }
        if (right && back) {
            return 1;
        }
        if (back && left) {
            return 2;
        }
        if (left && front) {
            return 3;
        }
        return 0;
    }

    private static int countOpenSides(boolean front, boolean back, boolean right, boolean left) {
        int count = 0;
        if (front) count++;
        if (back) count++;
        if (right) count++;
        if (left) count++;
        return count;
    }

    private static void appendTriangleWithNormalUp(FloatCollector collector,
                                                   float[] v0, float[] v1, float[] v2,
                                                   float baseX, float baseY, float baseZ,
                                                   float u0, float v0Tex,
                                                   float u1, float v1Tex,
                                                   float u2, float v2Tex,
                                                   float color) {
        float[] normal = computeNormal(v0, v1, v2);
        if (normal[1] < 0.0f) {
            float[] temp = v1;
            v1 = v2;
            v2 = temp;
            float tempU = u1;
            float tempV = v1Tex;
            u1 = u2;
            v1Tex = v2Tex;
            u2 = tempU;
            v2Tex = tempV;
            normal = computeNormal(v0, v1, v2);
        }
        addVertex(collector, v0, baseX, baseY, baseZ, normal, u0, v0Tex, color);
        addVertex(collector, v1, baseX, baseY, baseZ, normal, u1, v1Tex, color);
        addVertex(collector, v2, baseX, baseY, baseZ, normal, u2, v2Tex, color);
    }

    private static float[] computeNormal(float[] v0, float[] v1, float[] v2) {
        float ax = v1[0] - v0[0];
        float ay = v1[1] - v0[1];
        float az = v1[2] - v0[2];
        float bx = v2[0] - v0[0];
        float by = v2[1] - v0[1];
        float bz = v2[2] - v0[2];
        float nx = (ay * bz) - (az * by);
        float ny = (az * bx) - (ax * bz);
        float nz = (ax * by) - (ay * bx);
        float length = (float) Math.sqrt((nx * nx) + (ny * ny) + (nz * nz));
        if (length == 0.0f) {
            return new float[] {0.0f, 1.0f, 0.0f};
        }
        return new float[] {nx / length, ny / length, nz / length};
    }

    private static void addVertex(FloatCollector collector, float[] v,
                                  float baseX, float baseY, float baseZ,
                                  float[] normal, float u, float vTex, float color) {
        float x = baseX + (v[0] * World.BLOCK_SIZE);
        float y = baseY + (v[1] * World.BLOCK_SIZE);
        float z = baseZ + (v[2] * World.BLOCK_SIZE);
        collector.add(
                x, y, z,
                normal[0], normal[1], normal[2],
                u, vTex,
                color, color, color
        );
    }

    private static float[] averageVerts(float[] a, float[] b) {
        return new float[] {
                (a[0] + b[0]) * 0.5f,
                (a[1] + b[1]) * 0.5f,
                (a[2] + b[2]) * 0.5f
        };
    }

    private static float[] averageUvs(float u0, float v0, float u1, float v1) {
        return new float[] { (u0 + u1) * 0.5f, (v0 + v1) * 0.5f };
    }
}
