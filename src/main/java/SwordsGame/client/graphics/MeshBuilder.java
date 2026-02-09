package SwordsGame.client.graphics;

import SwordsGame.client.World;
import SwordsGame.client.blocks.Registry;
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
    private final SlopeVertexShader slopeShader = new SlopeVertexShader();

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

        boolean slopeActive = props.hasVertexSmoothing() && faces[2] && hasSlopeSide(faces);
        for (int face = 0; face < 6; face++) {
            if (slopeActive && face != 2) continue;
            if (topOnly && face != 2) continue;
            if (!faces[face]) continue;
            int textureId = block.getTextureId(face);
            if (textureId == 0) continue;

            Map<Integer, FloatCollector> target = props.isTransparent() ? transparent : (props.hasEmission() ? emissive : opaque);
            FloatCollector collector = target.computeIfAbsent(textureId, id -> new FloatCollector(2048));
            SlopeVertexShader shader = slopeActive ? slopeShader : null;
            appendFace(collector, face, block, rot, baseX, baseY, baseZ, colorMod, faces, shader);
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
                            boolean[] faces, SlopeVertexShader shader) {
        float[] uv = block.getUv(rot);
        float[] normal = FACE_NORMALS[face];
        float[][] verts = FACE_VERTS[face];

        addVertex(collector, verts[0], baseX, baseY, baseZ, normal, uv[0], uv[1], color, faces, shader);
        addVertex(collector, verts[1], baseX, baseY, baseZ, normal, uv[2], uv[3], color, faces, shader);
        addVertex(collector, verts[2], baseX, baseY, baseZ, normal, uv[4], uv[5], color, faces, shader);

        addVertex(collector, verts[2], baseX, baseY, baseZ, normal, uv[4], uv[5], color, faces, shader);
        addVertex(collector, verts[3], baseX, baseY, baseZ, normal, uv[6], uv[7], color, faces, shader);
        addVertex(collector, verts[0], baseX, baseY, baseZ, normal, uv[0], uv[1], color, faces, shader);
    }

    private void addVertex(FloatCollector collector, float[] v, float baseX, float baseY, float baseZ,
                           float[] normal, float u, float vTex, float color,
                           boolean[] faces, SlopeVertexShader shader) {
        float localX = v[0];
        float localY = v[1];
        float localZ = v[2];
        if (shader != null) {
            localY = shader.adjustY(localX, localY, localZ, faces);
        }
        float x = baseX + (localX * World.BLOCK_SIZE);
        float y = baseY + (localY * World.BLOCK_SIZE);
        float z = baseZ + (localZ * World.BLOCK_SIZE);
        collector.add(
                x, y, z,
                normal[0], normal[1], normal[2],
                u, vTex,
                color, color, color
        );
    }

    private boolean hasSlopeSide(boolean[] faces) {
        return faces[0] || faces[1] || faces[4] || faces[5];
    }
}
