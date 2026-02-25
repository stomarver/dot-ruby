package SwordsGame.client.graphics;

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
    private final Map<Long, Float> reconciledTopOffsets = new HashMap<>();
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

        float[] baseTint = useVertexColor ? BlockColorPipeline.resolveTint(props, seed) : new float[] {1.0f, 1.0f, 1.0f};
        int rot = props.hasRandomRotation() ? (Math.abs(seed) % 4) : 0;

        float baseX = (wx - totalOffset) * scale;
        float baseY = wy * scale;
        float baseZ = (wz - totalOffset) * scale;

        if (props.hasSmoothing()) {
            appendSmoothedBlock(props, block, rot, baseX, baseY, baseZ, baseTint, faces, wx, wy, wz);
            return;
        }

        for (int face = 0; face < 6; face++) {
            if (topOnly && face != 2) continue;
            if (props.isSurfaceOnly() && face != 2) continue;
            if (!faces[face]) continue;
            int textureId = block.getTextureId(face);
            if (textureId == 0) continue;

            Map<Integer, FloatCollector> target = props.isTransparent() ? transparent : (props.hasEmission() ? emissive : opaque);
            FloatCollector collector = target.computeIfAbsent(textureId, id -> new FloatCollector(2048));
            float shade = getFaceShade(face);
            float[] shadedColor = new float[] {baseTint[0] * shade, baseTint[1] * shade, baseTint[2] * shade};
            appendFace(collector, face, block, rot, baseX, baseY, baseZ, shadedColor);
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

    private void appendSmoothedBlock(BlockProps props, Block block, int rot,
                                     float baseX, float baseY, float baseZ, float[] baseTint, boolean[] faces,
                                     int wx, int wy, int wz) {
        Map<Integer, FloatCollector> target = props.isTransparent() ? transparent : (props.hasEmission() ? emissive : opaque);

        FaceSmoothing smoothing = new FaceSmoothing(faces);
        FaceSmoothing.Topology topology = smoothing.buildTopology(World.BLOCK_SCALE, wx, wy, wz);

        float[][] topVerts = FACE_VERTS[2];
        float[] yOffsets = new float[4];
        float[] proposed = topology.proposedOffsets();
        for (int i = 0; i < 4; i++) {
            yOffsets[i] = reconcileTopCornerOffset(wx, wy, wz, i, topVerts[i], proposed[i]);
        }

        if (!topOnly) {
            appendSmoothedVerticalFace(target, block, rot, baseX, baseY, baseZ, baseTint, 0, faces[0],
                    new float[] {0.0f, 0.0f, yOffsets[2], yOffsets[1]});
            appendSmoothedVerticalFace(target, block, rot, baseX, baseY, baseZ, baseTint, 1, faces[1],
                    new float[] {0.0f, yOffsets[0], yOffsets[3], 0.0f});
            appendSmoothedVerticalFace(target, block, rot, baseX, baseY, baseZ, baseTint, 4, faces[4],
                    new float[] {0.0f, yOffsets[3], yOffsets[2], 0.0f});
            appendSmoothedVerticalFace(target, block, rot, baseX, baseY, baseZ, baseTint, 5, faces[5],
                    new float[] {0.0f, 0.0f, yOffsets[1], yOffsets[0]});
        }

        if (!faces[2]) {
            return;
        }

        int topTexture = block.getTextureId(2);
        if (topTexture == 0) {
            return;
        }
        FloatCollector topCollector = target.computeIfAbsent(topTexture, id -> new FloatCollector(2048));
        float shade = getFaceShade(2);
        float[] topColor = new float[] {baseTint[0] * shade, baseTint[1] * shade, baseTint[2] * shade};

        float[] uv = block.getUv(rot);
        float[] normal = FACE_NORMALS[2];

        float centerX = (topVerts[0][0] + topVerts[1][0] + topVerts[2][0] + topVerts[3][0]) * 0.25f;
        float centerZ = (topVerts[0][2] + topVerts[1][2] + topVerts[2][2] + topVerts[3][2]) * 0.25f;
        float centerYOffset = (yOffsets[0] + yOffsets[1] + yOffsets[2] + yOffsets[3]) * 0.25f;

        float centerU = (uv[0] + uv[2] + uv[4] + uv[6]) * 0.25f;
        float centerV = (uv[1] + uv[3] + uv[5] + uv[7]) * 0.25f;

        addSmoothedTriangleWithCenter(topCollector, topVerts[0], yOffsets[0], uv[0], uv[1], topColor,
                topVerts[1], yOffsets[1], uv[2], uv[3], topColor,
                centerX, centerYOffset, centerZ, centerU, centerV, topColor,
                baseX, baseY, baseZ, normal);

        addSmoothedTriangleWithCenter(topCollector, topVerts[1], yOffsets[1], uv[2], uv[3], topColor,
                topVerts[2], yOffsets[2], uv[4], uv[5], topColor,
                centerX, centerYOffset, centerZ, centerU, centerV, topColor,
                baseX, baseY, baseZ, normal);

        addSmoothedTriangleWithCenter(topCollector, topVerts[2], yOffsets[2], uv[4], uv[5], topColor,
                topVerts[3], yOffsets[3], uv[6], uv[7], topColor,
                centerX, centerYOffset, centerZ, centerU, centerV, topColor,
                baseX, baseY, baseZ, normal);

        addSmoothedTriangleWithCenter(topCollector, topVerts[3], yOffsets[3], uv[6], uv[7], topColor,
                topVerts[0], yOffsets[0], uv[0], uv[1], topColor,
                centerX, centerYOffset, centerZ, centerU, centerV, topColor,
                baseX, baseY, baseZ, normal);
    }

    private void appendSmoothedVerticalFace(Map<Integer, FloatCollector> target, Block block, int rot,
                                            float baseX, float baseY, float baseZ, float[] baseTint,
                                            int face, boolean exposed, float[] vertexYOffsets) {
        if (!exposed) {
            return;
        }
        int textureId = block.getTextureId(face);
        if (textureId == 0) {
            return;
        }

        FloatCollector collector = target.computeIfAbsent(textureId, id -> new FloatCollector(2048));
        float shade = getFaceShade(face);
        float[] color = new float[] {baseTint[0] * shade, baseTint[1] * shade, baseTint[2] * shade};

        float[] uv = block.getUv(rot);
        float[] normal = FACE_NORMALS[face];
        float[][] verts = FACE_VERTS[face];

        addSmoothedVertexWithYOffset(collector, verts[0], vertexYOffsets[0], baseX, baseY, baseZ, normal, uv[0], uv[1], color);
        addSmoothedVertexWithYOffset(collector, verts[1], vertexYOffsets[1], baseX, baseY, baseZ, normal, uv[2], uv[3], color);
        addSmoothedVertexWithYOffset(collector, verts[2], vertexYOffsets[2], baseX, baseY, baseZ, normal, uv[4], uv[5], color);

        addSmoothedVertexWithYOffset(collector, verts[2], vertexYOffsets[2], baseX, baseY, baseZ, normal, uv[4], uv[5], color);
        addSmoothedVertexWithYOffset(collector, verts[3], vertexYOffsets[3], baseX, baseY, baseZ, normal, uv[6], uv[7], color);
        addSmoothedVertexWithYOffset(collector, verts[0], vertexYOffsets[0], baseX, baseY, baseZ, normal, uv[0], uv[1], color);
    }

    private void addSmoothedTriangleWithCenter(FloatCollector collector,
                                               float[] a, float ayOffset, float au, float av, float[] colorA,
                                               float[] b, float byOffset, float bu, float bv, float[] colorB,
                                               float centerX, float centerYOffset, float centerZ, float centerU, float centerV, float[] colorCenter,
                                               float baseX, float baseY, float baseZ,
                                               float[] normal) {
        addSmoothedVertexWithYOffset(collector, a, ayOffset, baseX, baseY, baseZ, normal, au, av, colorA);
        addSmoothedVertexWithYOffset(collector, b, byOffset, baseX, baseY, baseZ, normal, bu, bv, colorB);
        addSmoothedVertexWithYOffset(collector,
                new float[] {centerX, 1.0f, centerZ},
                centerYOffset,
                baseX, baseY, baseZ, normal, centerU, centerV, colorCenter);
    }

    private float reconcileTopCornerOffset(int wx, int wy, int wz, int cornerIndex, float[] corner, float proposedOffset) {
        int vx = wx + (corner[0] > 0f ? 1 : 0);
        int vz = wz + (corner[2] > 0f ? 1 : 0);
        long key = topVertexKey(vx, wy, vz);

        Float existing = reconciledTopOffsets.get(key);
        if (existing == null) {
            reconciledTopOffsets.put(key, proposedOffset);
            return proposedOffset;
        }

        float reconciled = Math.min(existing, proposedOffset);
        if (reconciled != existing) {
            reconciledTopOffsets.put(key, reconciled);
        }
        return reconciled;
    }

    private long topVertexKey(int vx, int vy, int vz) {
        long x = ((long) vx & 0x1FFFFFL);
        long y = ((long) vy & 0x1FFFL);
        long z = ((long) vz & 0x1FFFFFL);
        return (x << 34) | (y << 21) | z;
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
