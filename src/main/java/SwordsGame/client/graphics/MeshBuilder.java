package SwordsGame.client.graphics;

import SwordsGame.client.World;
import SwordsGame.client.blocks.Registry;
import SwordsGame.client.blocks.Type;
import java.util.HashMap;
import java.util.Map;

public class MeshBuilder {
    private static final float[][][] FACE_VERTS = new float[][][] {
            { {-1, -1,  1}, { 1, -1,  1}, { 1,  1,  1}, {-1,  1,  1} },
            { {-1, -1, -1}, {-1,  1, -1}, { 1,  1, -1}, { 1, -1, -1} },
            { {-1,  1, -1}, {-1,  1,  1}, { 1,  1,  1}, { 1,  1, -1} },
            { {-1, -1, -1}, { 1, -1, -1}, { 1, -1,  1}, {-1, -1,  1} },
            { { 1, -1, -1}, { 1,  1, -1}, { 1,  1,  1}, { 1, -1,  1} },
            { {-1, -1, -1}, {-1, -1,  1}, {-1,  1,  1}, {-1,  1, -1} }
    };

    private static final float[][] FACE_NORMALS = new float[][] {
            {0, 0, 1},
            {0, 0, -1},
            {0, 1, 0},
            {0, -1, 0},
            {1, 0, 0},
            {-1, 0, 0}
    };

    private final Map<Integer, FloatCollector> opaque = new HashMap<>();
    private final Map<Integer, FloatCollector> transparent = new HashMap<>();
    private final Map<Integer, FloatCollector> emissive = new HashMap<>();
    private final boolean topOnly;
    private final boolean useVertexColor;
    private static final int FACE_FRONT = 0;
    private static final int FACE_BACK = 1;
    private static final int FACE_TOP = 2;
    private static final int FACE_BOTTOM = 3;
    private static final int FACE_RIGHT = 4;
    private static final int FACE_LEFT = 5;
    private static final float LOWER_TO_BLOCK_BELOW = 2.0f;

    public MeshBuilder(boolean topOnly, boolean useVertexColor) {
        this.topOnly = topOnly;
        this.useVertexColor = useVertexColor;
    }

    public void addBlock(byte typeId, int seed, boolean[] faces, int wx, int wy, int wz, float totalOffset, float scale) {
        Block block = Registry.get(typeId);
        if (block == null) return;

        BlockProperties props = block.getProperties();
        if (!props.isSolid()) return;

        float colorMod = useVertexColor && props.hasRandomColor() ? 0.9f + (Math.abs(seed % 10) / 100f) : 1.0f;
        int rot = props.hasRandomRotation() ? (Math.abs(seed) % 4) : 0;

        float baseX = (wx - totalOffset) * scale;
        float baseY = wy * scale;
        float baseZ = (wz - totalOffset) * scale;
        boolean hasSideAir = isSideFaceOpen(faces);
        boolean slopeGrassTop = block.getType() == Type.GRASS && faces[FACE_TOP] && hasSideAir;

        for (int face = 0; face < 6; face++) {
            if (topOnly && face != FACE_TOP) continue;
            if (!faces[face]) continue;
            if (slopeGrassTop && face != FACE_TOP) continue;
            int textureId = block.getTextureId(face);
            if (textureId == 0) continue;

            Map<Integer, FloatCollector> target = props.isTransparent() ? transparent : (props.hasEmission() ? emissive : opaque);
            FloatCollector collector = target.computeIfAbsent(textureId, id -> new FloatCollector(2048));
            if (slopeGrassTop && face == FACE_TOP) {
                appendSlopedTopFace(collector, block, rot, baseX, baseY, baseZ, colorMod, faces);
            } else {
                appendFace(collector, face, block, rot, baseX, baseY, baseZ, colorMod);
            }
        }
    }

    public ChunkMesh build() {
        return new ChunkMesh(
                buildBuffers(opaque),
                buildBuffers(transparent),
                buildBuffers(emissive)
        );
    }

    private Map<Integer, MeshBuffer> buildBuffers(Map<Integer, FloatCollector> source) {
        Map<Integer, MeshBuffer> buffers = new HashMap<>();
        for (Map.Entry<Integer, FloatCollector> entry : source.entrySet()) {
            MeshBuffer buffer = MeshBuffer.build(entry.getValue());
            if (buffer != null) {
                buffers.put(entry.getKey(), buffer);
            }
        }
        return buffers;
    }

    private void appendFace(FloatCollector collector, int face, Block block, int rot,
                            float baseX, float baseY, float baseZ, float color) {
        float[] uv = block.getUv(rot);
        float[] normal = FACE_NORMALS[face];
        float[][] verts = FACE_VERTS[face];

        addVertex(collector, verts[0], baseX, baseY, baseZ, normal, uv[0], uv[1], color);
        addVertex(collector, verts[1], baseX, baseY, baseZ, normal, uv[2], uv[3], color);
        addVertex(collector, verts[2], baseX, baseY, baseZ, normal, uv[4], uv[5], color);

        addVertex(collector, verts[2], baseX, baseY, baseZ, normal, uv[4], uv[5], color);
        addVertex(collector, verts[3], baseX, baseY, baseZ, normal, uv[6], uv[7], color);
        addVertex(collector, verts[0], baseX, baseY, baseZ, normal, uv[0], uv[1], color);
    }

    private void appendSlopedTopFace(FloatCollector collector, Block block, int rot,
                                     float baseX, float baseY, float baseZ, float color, boolean[] faces) {
        float[] uv = block.getUv(rot);
        float[][] verts = copyFaceVerts(FACE_VERTS[FACE_TOP]);
        boolean[] lower = new boolean[4];

        if (faces[FACE_FRONT]) {
            lower[1] = true;
            lower[2] = true;
        }
        if (faces[FACE_BACK]) {
            lower[0] = true;
            lower[3] = true;
        }
        if (faces[FACE_RIGHT]) {
            lower[2] = true;
            lower[3] = true;
        }
        if (faces[FACE_LEFT]) {
            lower[0] = true;
            lower[1] = true;
        }

        for (int i = 0; i < lower.length; i++) {
            if (lower[i]) {
                verts[i][1] -= LOWER_TO_BLOCK_BELOW;
            }
        }

        appendTriangleWithNormal(collector, verts[0], verts[1], verts[2], baseX, baseY, baseZ,
                uv[0], uv[1], uv[2], uv[3], uv[4], uv[5], color);
        appendTriangleWithNormal(collector, verts[2], verts[3], verts[0], baseX, baseY, baseZ,
                uv[4], uv[5], uv[6], uv[7], uv[0], uv[1], color);
    }

    private float[][] copyFaceVerts(float[][] source) {
        float[][] copy = new float[source.length][3];
        for (int i = 0; i < source.length; i++) {
            copy[i][0] = source[i][0];
            copy[i][1] = source[i][1];
            copy[i][2] = source[i][2];
        }
        return copy;
    }

    private void appendTriangleWithNormal(FloatCollector collector,
                                          float[] v0, float[] v1, float[] v2,
                                          float baseX, float baseY, float baseZ,
                                          float u0, float v0Tex,
                                          float u1, float v1Tex,
                                          float u2, float v2Tex,
                                          float color) {
        float[] normal = computeNormal(v0, v1, v2);
        addVertex(collector, v0, baseX, baseY, baseZ, normal, u0, v0Tex, color);
        addVertex(collector, v1, baseX, baseY, baseZ, normal, u1, v1Tex, color);
        addVertex(collector, v2, baseX, baseY, baseZ, normal, u2, v2Tex, color);
    }

    private float[] computeNormal(float[] v0, float[] v1, float[] v2) {
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

    private boolean isSideFaceOpen(boolean[] faces) {
        return faces[FACE_FRONT] || faces[FACE_BACK] || faces[FACE_RIGHT] || faces[FACE_LEFT];
    }

    private void addVertex(FloatCollector collector, float[] v, float baseX, float baseY, float baseZ,
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
}
