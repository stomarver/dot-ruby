package SwordsGame.client.graphics;

import SwordsGame.client.Smoothing;
import SwordsGame.client.World;
import SwordsGame.client.data.blocks.RenderRegistry;
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

    public MeshBuilder(boolean topOnly, boolean useVertexColor) {
        this.topOnly = topOnly;
        this.useVertexColor = useVertexColor;
    }

    public void addBlock(byte typeId, int seed, boolean[] faces, int wx, int wy, int wz, float totalOffset, float scale) {
        addBlock(typeId, seed, faces, null, wx, wy, wz, totalOffset, scale);
    }

    public void addBlock(byte typeId, int seed, boolean[] faces, float[][] ao, int wx, int wy, int wz, float totalOffset, float scale) {
        Block block = RenderRegistry.get(typeId);
        if (block == null) return;

        BlockProperties props = block.getProperties();
        if (!props.isSolid()) return;

        float colorMod = useVertexColor && props.hasRandomColor() ? 0.9f + (Math.abs(seed % 10) / 100f) : 1.0f;
        int rot = props.hasRandomRotation() ? (Math.abs(seed) % 4) : 0;

        float baseX = (wx - totalOffset) * scale;
        float baseY = wy * scale;
        float baseZ = (wz - totalOffset) * scale;

        for (int face = 0; face < 6; face++) {
            if (topOnly && face != 2) continue;
            if (props.hasSmoothing() && face != 2) continue;
            if (!faces[face]) continue;
            int textureId = block.getTextureId(face);
            if (textureId == 0) continue;

            Map<Integer, FloatCollector> target = props.isTransparent() ? transparent : (props.hasEmission() ? emissive : opaque);
            FloatCollector collector = target.computeIfAbsent(textureId, id -> new FloatCollector(2048));
            if (props.hasSmoothing() && face == 2) {
                appendSmoothedTopFace(collector, block, rot, baseX, baseY, baseZ, colorMod, faces, ao);
            } else {
                appendFace(collector, face, block, rot, baseX, baseY, baseZ, colorMod, ao);
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
                            float baseX, float baseY, float baseZ, float color, float[][] ao) {
        float[] uv = block.getUv(rot);
        float[] normal = FACE_NORMALS[face];
        float[][] verts = FACE_VERTS[face];

        addVertex(collector, verts[0], baseX, baseY, baseZ, normal, uv[0], uv[1], color * getAo(ao, face, 0));
        addVertex(collector, verts[1], baseX, baseY, baseZ, normal, uv[2], uv[3], color * getAo(ao, face, 1));
        addVertex(collector, verts[2], baseX, baseY, baseZ, normal, uv[4], uv[5], color * getAo(ao, face, 2));

        addVertex(collector, verts[2], baseX, baseY, baseZ, normal, uv[4], uv[5], color * getAo(ao, face, 2));
        addVertex(collector, verts[3], baseX, baseY, baseZ, normal, uv[6], uv[7], color * getAo(ao, face, 3));
        addVertex(collector, verts[0], baseX, baseY, baseZ, normal, uv[0], uv[1], color * getAo(ao, face, 0));
    }

    private void appendSmoothedTopFace(FloatCollector collector, Block block, int rot,
                                       float baseX, float baseY, float baseZ, float color, boolean[] faces, float[][] ao) {
        float[] uv = block.getUv(rot);
        float[] normal = FACE_NORMALS[2];
        float[][] verts = FACE_VERTS[2];
        Smoothing smoothing = new Smoothing(faces);

        addSmoothedVertex(collector, verts[0], baseX, baseY, baseZ, normal, uv[0], uv[1], color * getAo(ao, 2, 0), smoothing, 0);
        addSmoothedVertex(collector, verts[1], baseX, baseY, baseZ, normal, uv[2], uv[3], color * getAo(ao, 2, 1), smoothing, 1);
        addSmoothedVertex(collector, verts[2], baseX, baseY, baseZ, normal, uv[4], uv[5], color * getAo(ao, 2, 2), smoothing, 2);

        addSmoothedVertex(collector, verts[2], baseX, baseY, baseZ, normal, uv[4], uv[5], color * getAo(ao, 2, 2), smoothing, 2);
        addSmoothedVertex(collector, verts[3], baseX, baseY, baseZ, normal, uv[6], uv[7], color * getAo(ao, 2, 3), smoothing, 3);
        addSmoothedVertex(collector, verts[0], baseX, baseY, baseZ, normal, uv[0], uv[1], color * getAo(ao, 2, 0), smoothing, 0);
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

    private void addSmoothedVertex(FloatCollector collector, float[] v, float baseX, float baseY, float baseZ,
                                   float[] normal, float u, float vTex, float color, Smoothing smoothing, int index) {
        float yOffset = smoothing.shouldLowerTopVertex(index) ? -World.BLOCK_SCALE : 0.0f;
        float slopeOcclusion = yOffset < 0.0f ? 0.85f : 1.0f;
        float x = baseX + (v[0] * World.BLOCK_SIZE);
        float y = baseY + (v[1] * World.BLOCK_SIZE) + yOffset;
        float z = baseZ + (v[2] * World.BLOCK_SIZE);
        collector.add(
                x, y, z,
                normal[0], normal[1], normal[2],
                u, vTex,
                color * slopeOcclusion, color * slopeOcclusion, color * slopeOcclusion
        );
    }

    private float getAo(float[][] ao, int face, int vertex) {
        if (ao == null || face < 0 || face >= ao.length || ao[face] == null || vertex < 0 || vertex >= ao[face].length) {
            return 1.0f;
        }
        return ao[face][vertex];
    }
}
