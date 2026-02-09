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
        boolean slopedGrassSides = block.getType() == Type.GRASS && faces[FACE_TOP];

        for (int face = 0; face < 6; face++) {
            if (topOnly && face != FACE_TOP) continue;
            if (!faces[face]) continue;
            int textureId = block.getTextureId(face);
            if (textureId == 0) continue;

            Map<Integer, FloatCollector> target = props.isTransparent() ? transparent : (props.hasEmission() ? emissive : opaque);
            FloatCollector collector = target.computeIfAbsent(textureId, id -> new FloatCollector(2048));
            if (slopedGrassSides && isSideFace(face)) {
                appendSlopedFace(collector, face, block, rot, baseX, baseY, baseZ, colorMod);
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

    private void appendSlopedFace(FloatCollector collector, int face, Block block, int rot,
                                  float baseX, float baseY, float baseZ, float color) {
        float[] uv = block.getUv(rot);
        float[] normal = FACE_NORMALS[face];
        float[][] verts = FACE_VERTS[face];
        float[] v0;
        float[] v1;
        float[] topMid;
        float[] uv0;
        float[] uv1;
        float[] uvTop;

        if (face == FACE_FRONT) {
            v0 = verts[0];
            v1 = verts[1];
            topMid = averageVerts(verts[2], verts[3]);
            uv0 = new float[] { uv[0], uv[1] };
            uv1 = new float[] { uv[2], uv[3] };
            uvTop = averageUvs(uv[4], uv[5], uv[6], uv[7]);
            appendTriangle(collector, v0, v1, topMid, baseX, baseY, baseZ, normal, uv0, uv1, uvTop, color);
            return;
        }

        if (face == FACE_BACK) {
            v0 = verts[0];
            v1 = verts[3];
            topMid = averageVerts(verts[1], verts[2]);
            uv0 = new float[] { uv[0], uv[1] };
            uv1 = new float[] { uv[6], uv[7] };
            uvTop = averageUvs(uv[2], uv[3], uv[4], uv[5]);
            appendTriangle(collector, v0, v1, topMid, baseX, baseY, baseZ, normal, uv0, uv1, uvTop, color);
            return;
        }

        if (face == FACE_RIGHT) {
            v0 = verts[3];
            v1 = verts[0];
            topMid = averageVerts(verts[1], verts[2]);
            uv0 = new float[] { uv[6], uv[7] };
            uv1 = new float[] { uv[0], uv[1] };
            uvTop = averageUvs(uv[2], uv[3], uv[4], uv[5]);
            appendTriangle(collector, v0, v1, topMid, baseX, baseY, baseZ, normal, uv0, uv1, uvTop, color);
            return;
        }

        if (face == FACE_LEFT) {
            v0 = verts[0];
            v1 = verts[1];
            topMid = averageVerts(verts[2], verts[3]);
            uv0 = new float[] { uv[0], uv[1] };
            uv1 = new float[] { uv[2], uv[3] };
            uvTop = averageUvs(uv[4], uv[5], uv[6], uv[7]);
            appendTriangle(collector, v0, v1, topMid, baseX, baseY, baseZ, normal, uv0, uv1, uvTop, color);
        }
    }

    private void appendTriangle(FloatCollector collector, float[] v0, float[] v1, float[] v2,
                                float baseX, float baseY, float baseZ, float[] normal,
                                float[] uv0, float[] uv1, float[] uv2, float color) {
        addVertex(collector, v0, baseX, baseY, baseZ, normal, uv0[0], uv0[1], color);
        addVertex(collector, v1, baseX, baseY, baseZ, normal, uv1[0], uv1[1], color);
        addVertex(collector, v2, baseX, baseY, baseZ, normal, uv2[0], uv2[1], color);
    }

    private float[] averageVerts(float[] a, float[] b) {
        return new float[] {
                (a[0] + b[0]) * 0.5f,
                (a[1] + b[1]) * 0.5f,
                (a[2] + b[2]) * 0.5f
        };
    }

    private float[] averageUvs(float u0, float v0, float u1, float v1) {
        return new float[] { (u0 + u1) * 0.5f, (v0 + v1) * 0.5f };
    }

    private boolean isSideFace(int face) {
        return face == FACE_FRONT || face == FACE_BACK || face == FACE_RIGHT || face == FACE_LEFT;
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
