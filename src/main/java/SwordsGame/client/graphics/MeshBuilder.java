package SwordsGame.client.graphics;

import SwordsGame.client.Smth;
import SwordsGame.client.World;
import SwordsGame.client.blocks.BlockRegistry;

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
        Block block = BlockRegistry.get(typeId);
        if (block == null) return;

        BlockProps props = block.getProperties();
        if (!props.isSolid()) return;

        float[] baseTint = useVertexColor ? BlockColorPipeline.resolveTint(props, seed, wx, wy, wz) : new float[] {1.0f, 1.0f, 1.0f};
        int rot = props.hasRandomRotation() ? (Math.abs(seed) % 4) : 0;

        float baseX = (wx - totalOffset) * scale;
        float baseY = wy * scale;
        float baseZ = (wz - totalOffset) * scale;

        for (int face = 0; face < 6; face++) {
            if (topOnly && face != 2) continue;
            if (props.isSurfaceOnly() && face != 2) continue;
            if (props.hasSmoothing() && face != 2) continue;
            if (!faces[face]) continue;
            int textureId = block.getTextureId(face);
            if (textureId == 0) continue;

            Map<Integer, FloatCollector> target = props.isTransparent() ? transparent : (props.hasEmission() ? emissive : opaque);
            FloatCollector collector = target.computeIfAbsent(textureId, id -> new FloatCollector(2048));
            float shade = getFaceShade(face);
            float[] shadedColor = new float[] {baseTint[0] * shade, baseTint[1] * shade, baseTint[2] * shade};
            if (props.hasSmoothing() && face == 2) {
                appendSmoothedTopFace(collector, block, rot, baseX, baseY, baseZ, shadedColor, faces);
            } else {
                appendFace(collector, face, block, rot, baseX, baseY, baseZ, shadedColor);
            }
        }
    }


    private float getFaceShade(int face) {
        return switch (face) {
            case 2 -> 1.0f;
            case 3 -> 0.45f;
            case 0 -> 0.78f;
            case 1 -> 0.62f;
            case 4 -> 0.86f;
            case 5 -> 0.56f;
            default -> 1.0f;
        };
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
                            float baseX, float baseY, float baseZ, float[] color) {
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

    private void appendSmoothedTopFace(FloatCollector collector, Block block, int rot,
                                       float baseX, float baseY, float baseZ, float[] color, boolean[] faces) {
        float[] uv = block.getUv(rot);
        float[] normal = FACE_NORMALS[2];
        float[][] verts = FACE_VERTS[2];
        Smth smoothing = new Smth(faces);

        float[] yOffsets = new float[4];
        for (int i = 0; i < 4; i++) {
            yOffsets[i] = smoothing.shouldLowerTopVertex(i) ? -World.BLOCK_SCALE : 0.0f;
        }

        float centerX = (verts[0][0] + verts[1][0] + verts[2][0] + verts[3][0]) * 0.25f;
        float centerZ = (verts[0][2] + verts[1][2] + verts[2][2] + verts[3][2]) * 0.25f;
        float centerYOffset = (yOffsets[0] + yOffsets[1] + yOffsets[2] + yOffsets[3]) * 0.25f;

        float centerU = (uv[0] + uv[2] + uv[4] + uv[6]) * 0.25f;
        float centerV = (uv[1] + uv[3] + uv[5] + uv[7]) * 0.25f;

        addSmoothedTriangleWithCenter(collector, verts[0], yOffsets[0], uv[0], uv[1],
                verts[1], yOffsets[1], uv[2], uv[3],
                centerX, centerYOffset, centerZ, centerU, centerV,
                baseX, baseY, baseZ, normal, color);

        addSmoothedTriangleWithCenter(collector, verts[1], yOffsets[1], uv[2], uv[3],
                verts[2], yOffsets[2], uv[4], uv[5],
                centerX, centerYOffset, centerZ, centerU, centerV,
                baseX, baseY, baseZ, normal, color);

        addSmoothedTriangleWithCenter(collector, verts[2], yOffsets[2], uv[4], uv[5],
                verts[3], yOffsets[3], uv[6], uv[7],
                centerX, centerYOffset, centerZ, centerU, centerV,
                baseX, baseY, baseZ, normal, color);

        addSmoothedTriangleWithCenter(collector, verts[3], yOffsets[3], uv[6], uv[7],
                verts[0], yOffsets[0], uv[0], uv[1],
                centerX, centerYOffset, centerZ, centerU, centerV,
                baseX, baseY, baseZ, normal, color);
    }

    private void addSmoothedTriangleWithCenter(FloatCollector collector,
                                               float[] a, float ayOffset, float au, float av,
                                               float[] b, float byOffset, float bu, float bv,
                                               float centerX, float centerYOffset, float centerZ, float centerU, float centerV,
                                               float baseX, float baseY, float baseZ,
                                               float[] normal, float[] color) {
        addSmoothedVertexWithYOffset(collector, a, ayOffset, baseX, baseY, baseZ, normal, au, av, color);
        addSmoothedVertexWithYOffset(collector, b, byOffset, baseX, baseY, baseZ, normal, bu, bv, color);
        addSmoothedVertexWithYOffset(collector,
                new float[] {centerX, 1.0f, centerZ},
                centerYOffset,
                baseX, baseY, baseZ, normal, centerU, centerV, color);
    }

    private void addVertex(FloatCollector collector, float[] v, float baseX, float baseY, float baseZ,
                           float[] normal, float u, float vTex, float[] color) {
        float x = baseX + (v[0] * World.BLOCK_SIZE);
        float y = baseY + (v[1] * World.BLOCK_SIZE);
        float z = baseZ + (v[2] * World.BLOCK_SIZE);
        collector.add(
                x, y, z,
                normal[0], normal[1], normal[2],
                u, vTex,
                color[0], color[1], color[2]
        );
    }

    private void addSmoothedVertexWithYOffset(FloatCollector collector, float[] v, float yOffset,
                                              float baseX, float baseY, float baseZ,
                                              float[] normal, float u, float vTex, float[] color) {
        float x = baseX + (v[0] * World.BLOCK_SIZE);
        float y = baseY + (v[1] * World.BLOCK_SIZE) + yOffset;
        float z = baseZ + (v[2] * World.BLOCK_SIZE);
        collector.add(
                x, y, z,
                normal[0], normal[1], normal[2],
                u, vTex,
                color[0], color[1], color[2]
        );
    }
}
