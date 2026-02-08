package SwordsGame.client;

import SwordsGame.client.blocks.Registry;

import java.util.HashMap;
import java.util.Map;

public class MeshBuilder {
    private static final float[][][] FACE_VERTS = new float[][][] {
            { {-1, -1,  1}, { 1, -1,  1}, { 1,  1,  1}, {-1,  1,  1} }, // +Z
            { {-1, -1, -1}, {-1,  1, -1}, { 1,  1, -1}, { 1, -1, -1} }, // -Z
            { {-1,  1, -1}, {-1,  1,  1}, { 1,  1,  1}, { 1,  1, -1} }, // +Y
            { {-1, -1, -1}, { 1, -1, -1}, { 1, -1,  1}, {-1, -1,  1} }, // -Y
            { { 1, -1, -1}, { 1,  1, -1}, { 1,  1,  1}, { 1, -1,  1} }, // +X
            { {-1, -1, -1}, {-1, -1,  1}, {-1,  1,  1}, {-1,  1, -1} }  // -X
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
        addBlock(typeId, seed, faces, wx, wy, wz, totalOffset, scale, null, 1.0f, 1.0f, 1.0f, 1.0f, false);
    }

    public void addBlock(byte typeId, int seed, boolean[] faces, int wx, int wy, int wz, float totalOffset, float scale,
                         float[][] faceVertexColors, float tintR, float tintG, float tintB, float alpha, boolean forceTransparent) {
        Block block = Registry.get(typeId);
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
            if (!faces[face]) continue;
            int textureId = block.getTextureId(face);
            if (textureId == 0) continue;

            boolean shouldBeTransparent = props.isTransparent() || forceTransparent;
            Map<Integer, FloatCollector> target = shouldBeTransparent ? transparent : (props.hasEmission() ? emissive : opaque);
            FloatCollector collector = target.computeIfAbsent(textureId, id -> new FloatCollector(2048));
            float[] vertexColors = faceVertexColors != null ? faceVertexColors[face] : null;
            appendFace(collector, face, block, rot, baseX, baseY, baseZ, colorMod, vertexColors, tintR, tintG, tintB, alpha);
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
                            float baseX, float baseY, float baseZ, float color,
                            float[] vertexColors, float tintR, float tintG, float tintB, float alpha) {
        float[] uv = block.getUv(rot);
        float[] normal = FACE_NORMALS[face];
        float[][] verts = FACE_VERTS[face];

        addVertex(collector, verts[0], baseX, baseY, baseZ, normal, uv[0], uv[1],
                applyVertexColor(color, vertexColors, 0, tintR, tintG, tintB), alpha);
        addVertex(collector, verts[1], baseX, baseY, baseZ, normal, uv[2], uv[3],
                applyVertexColor(color, vertexColors, 1, tintR, tintG, tintB), alpha);
        addVertex(collector, verts[2], baseX, baseY, baseZ, normal, uv[4], uv[5],
                applyVertexColor(color, vertexColors, 2, tintR, tintG, tintB), alpha);

        addVertex(collector, verts[2], baseX, baseY, baseZ, normal, uv[4], uv[5],
                applyVertexColor(color, vertexColors, 2, tintR, tintG, tintB), alpha);
        addVertex(collector, verts[3], baseX, baseY, baseZ, normal, uv[6], uv[7],
                applyVertexColor(color, vertexColors, 3, tintR, tintG, tintB), alpha);
        addVertex(collector, verts[0], baseX, baseY, baseZ, normal, uv[0], uv[1],
                applyVertexColor(color, vertexColors, 0, tintR, tintG, tintB), alpha);
    }

    private void addVertex(FloatCollector collector, float[] v, float baseX, float baseY, float baseZ,
                           float[] normal, float u, float vTex, float[] color, float alpha) {
        float x = baseX + (v[0] * World.BLOCK_SIZE);
        float y = baseY + (v[1] * World.BLOCK_SIZE);
        float z = baseZ + (v[2] * World.BLOCK_SIZE);
        collector.add(
                x, y, z,
                normal[0], normal[1], normal[2],
                u, vTex,
                color[0], color[1], color[2], alpha
        );
    }

    private float[] applyVertexColor(float baseColor, float[] vertexColors, int vertexIndex,
                                     float tintR, float tintG, float tintB) {
        float shade = vertexColors != null ? vertexColors[vertexIndex] : 1.0f;
        float color = baseColor * shade;
        return new float[] {color * tintR, color * tintG, color * tintB};
    }
}
