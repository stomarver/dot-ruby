package SwordsGame.client.graphics;

import SwordsGame.client.World;
import SwordsGame.client.blocks.Registry;
import java.util.HashMap;
import java.util.Map;

public class MeshBuilder {
    static final float[][][] FACE_VERTS = new float[][][] {
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

    public void addBlock(byte typeId, int seed, boolean[] faces, boolean[] sideAir, boolean[] sideSloped,
                         int wx, int wy, int wz, float totalOffset, float scale) {
        Block block = Registry.get(typeId);
        if (block == null) return;

        BlockProperties props = block.getProperties();
        if (!props.isSolid()) return;

        float colorMod = useVertexColor && props.hasRandomColor() ? 0.9f + (Math.abs(seed % 10) / 100f) : 1.0f;
        int rot = props.hasRandomRotation() ? (Math.abs(seed) % 4) : 0;

        float baseX = (wx - totalOffset) * scale;
        float baseY = wy * scale;
        float baseZ = (wz - totalOffset) * scale;
        boolean slopeTop = props.isSloped()
                && faces[FACE_TOP]
                && (isSideFaceOpen(sideAir) || isInnerCorner(sideAir, sideSloped));

        for (int face = 0; face < 6; face++) {
            if (topOnly && face != FACE_TOP) continue;
            if (!faces[face]) continue;
            if (slopeTop && face != FACE_TOP) continue;
            int textureId = block.getTextureId(face);
            if (textureId == 0) continue;

            Map<Integer, FloatCollector> target = props.isTransparent() ? transparent : (props.hasEmission() ? emissive : opaque);
            FloatCollector collector = target.computeIfAbsent(textureId, id -> new FloatCollector(2048));
            if (slopeTop && face == FACE_TOP) {
                Slopes.appendSlopedTopFace(
                        collector,
                        block,
                        rot,
                        baseX,
                        baseY,
                        baseZ,
                        colorMod,
                        sideAir,
                        sideSloped
                );
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

    private boolean isSideFaceOpen(boolean[] sideAir) {
        if (sideAir == null) {
            return false;
        }
        return sideAir[FACE_FRONT] || sideAir[FACE_BACK] || sideAir[FACE_RIGHT] || sideAir[FACE_LEFT];
    }

    private boolean isInnerCorner(boolean[] sideAir, boolean[] sideSloped) {
        if (sideAir == null || sideSloped == null) {
            return false;
        }
        if (isSideFaceOpen(sideAir)) {
            return false;
        }
        return isCornerPair(sideSloped);
    }

    private boolean isCornerPair(boolean[] sideFlags) {
        boolean front = sideFlags[FACE_FRONT];
        boolean back = sideFlags[FACE_BACK];
        boolean right = sideFlags[FACE_RIGHT];
        boolean left = sideFlags[FACE_LEFT];
        int count = 0;
        if (front) count++;
        if (back) count++;
        if (right) count++;
        if (left) count++;
        if (count != 2) {
            return false;
        }
        return (front && right) || (right && back) || (back && left) || (left && front);
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
