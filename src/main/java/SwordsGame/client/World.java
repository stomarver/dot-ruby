package SwordsGame.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import SwordsGame.client.data.blocks.RenderRegistry;
import SwordsGame.client.graphics.Block;
import SwordsGame.client.graphics.BlockRenderer;
import SwordsGame.client.graphics.ChunkMesh;
import SwordsGame.client.graphics.MeshBuilder;
import SwordsGame.server.Chunk;
import SwordsGame.server.ChunkManager;
import SwordsGame.server.data.blocks.Type;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.glfw.GLFW.glfwGetTime;

public class World {
    public static final float BLOCK_SIZE = 12.5f;
    public static final float BLOCK_SCALE = BLOCK_SIZE * 2.0f;
    private static final float RENDER_RADIUS_PADDING = 6.0f;
    private static final float LOD_DISTANCE_PADDING = 5.0f;
    private final Map<Chunk, ChunkRenderData> chunkCache = new HashMap<>();
    private final ArrayList<FallingBlock> fallingBlocks = new ArrayList<>();

    public void render(ChunkManager chunkManager, Camera camera) {
        ViewCulling culling = buildCulling(chunkManager, camera);
        ArrayList<ChunkRenderEntry> visibleChunks = collectVisibleChunks(chunkManager, culling);

        cleanupCache();

        renderVisibleChunks(chunkManager, culling, visibleChunks);
        updateAndRenderFallingBlocks(culling.worldSize);
    }

    public void renderChunkBounds(ChunkManager chunkManager, Camera camera) {
        ViewCulling culling = buildCulling(chunkManager, camera);
        ArrayList<ChunkRenderEntry> visibleChunks = collectVisibleChunks(chunkManager, culling);
        float offset = BLOCK_SCALE;
        float totalOffset = (culling.worldSize * Chunk.SIZE) / 2f;

        glDisable(GL_TEXTURE_2D);
        glDisable(GL_LIGHTING);
        glColor4f(0.2f, 0.9f, 0.9f, 0.7f);
        glLineWidth(2.0f);

        renderVisibleChunkBounds(visibleChunks, totalOffset, offset);

        glLineWidth(1.0f);
        glEnable(GL_LIGHTING);
        glEnable(GL_TEXTURE_2D);
        glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
    }

    private ViewCulling buildCulling(ChunkManager chunkManager, Camera camera) {
        int worldSize = chunkManager.getWorldSizeInChunks();
        float chunkSizeInUnits = Chunk.SIZE * BLOCK_SCALE;

        float totalOffsetBlocks = chunkManager.getWorldSizeInBlocks() / 2.0f;
        int focusBlockX = (int) Math.floor((-camera.getX() / BLOCK_SCALE) + totalOffsetBlocks);
        int focusBlockZ = (int) Math.floor((-camera.getZ() / BLOCK_SCALE) + totalOffsetBlocks);
        int focusChunkX = clamp(focusBlockX / Chunk.SIZE, 0, worldSize - 1);
        int focusChunkZ = clamp(focusBlockZ / Chunk.SIZE, 0, worldSize - 1);

        float halfWidthUnits = (camera.getOrthoWidth() / 2.0f) / camera.getZoom();
        float halfHeightUnits = (camera.getOrthoHeight() / 2.0f) / camera.getZoom();
        float pitchCos = (float) Math.cos(Math.toRadians(camera.getPitch()));
        float depthUnits = pitchCos == 0.0f ? halfHeightUnits : (halfHeightUnits / pitchCos);

        float baseRadius = Math.max(halfWidthUnits, depthUnits) / chunkSizeInUnits;
        float radius = baseRadius + RENDER_RADIUS_PADDING;
        int maxLoopDist = (int) Math.ceil(radius);

        return new ViewCulling(worldSize, focusChunkX, focusChunkZ, radius, maxLoopDist);
    }

    private ArrayList<ChunkRenderEntry> collectVisibleChunks(ChunkManager chunkManager, ViewCulling culling) {
        ArrayList<ChunkRenderEntry> entries = new ArrayList<>();
        float radiusSquared = culling.radius * culling.radius;
        for (int dx = -culling.maxLoopDist; dx <= culling.maxLoopDist; dx++) {
            for (int dz = -culling.maxLoopDist; dz <= culling.maxLoopDist; dz++) {
                int cx = culling.centerChunkX + dx;
                int cz = culling.centerChunkZ + dz;
                if (cx >= 0 && cx < culling.worldSize && cz >= 0 && cz < culling.worldSize) {
                    int distanceSq = (dx * dx) + (dz * dz);
                    if (distanceSq <= radiusSquared) {
                        Chunk chunk = chunkManager.getChunk(cx, cz);
                        if (chunk != null) {
                            int lod = selectLod(dx, dz, culling.radius);
                            entries.add(new ChunkRenderEntry(chunk, lod, distanceSq));
                        }
                    }
                }
            }
        }
        entries.sort((a, b) -> Integer.compare(a.distanceSq, b.distanceSq));
        return entries;
    }

    private void renderVisibleChunks(ChunkManager chunkManager, ViewCulling culling, ArrayList<ChunkRenderEntry> entries) {
        for (ChunkRenderEntry entry : entries) {
            renderChunkCached(chunkManager, entry.chunk, culling.worldSize, entry.lod);
        }
    }

    private void renderVisibleChunkBounds(ArrayList<ChunkRenderEntry> entries, float totalOffset, float offset) {
        for (ChunkRenderEntry entry : entries) {
            drawChunkBounds(entry.chunk, totalOffset, offset);
        }
    }

    private void updateAndRenderFallingBlocks(int worldSize) {
        double currentTime = glfwGetTime();
        float deltaTime = 1.0f / 60.0f;
        float totalOffset = (worldSize * Chunk.SIZE) / 2f;
        float offset = BLOCK_SCALE;

        Iterator<FallingBlock> iterator = fallingBlocks.iterator();
        while (iterator.hasNext()) {
            FallingBlock block = iterator.next();

            if (currentTime - block.creationTime > 1.0) {
                iterator.remove();
                continue;
            }

            block.update(deltaTime);

            float lifetime = (float) (currentTime - block.creationTime);
            float alpha = 1.0f;
            if (lifetime > 0.7f) {
                alpha = 1.0f - ((lifetime - 0.7f) / 0.3f);
            }

            glPushMatrix();
            glTranslatef((block.x - totalOffset) * offset, block.y * offset, (block.z - totalOffset) * offset);

            glEnable(GL_BLEND);
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

            boolean[] allFaces = {true, true, true, true, true, true};
            BlockRenderer.renderBlock(block.type, block.seed, allFaces, alpha);

            glDisable(GL_BLEND);
            glColor3f(1.0f, 1.0f, 1.0f);
            glPopMatrix();
        }
    }

    public void addFallingBlock(int wx, int wy, int wz, byte type) {
        int seed = (wx * 73856093) ^ (wy * 19349663) ^ (wz * 83492791);
        fallingBlocks.add(new FallingBlock(wx, wy, wz, type, seed, glfwGetTime()));
    }


    private void renderChunkCached(ChunkManager cm, Chunk chunk, int worldSize, int lod) {
        if (lod >= 2) {
            return;
        }
        ChunkRenderData data = chunkCache.computeIfAbsent(chunk, key -> new ChunkRenderData());
        ChunkMesh mesh = data.getMesh(cm, chunk, worldSize, lod);
        if (mesh != null) {
            mesh.render();
        }
    }

    private void drawChunkBounds(Chunk chunk, float totalOffset, float offset) {
        float half = BLOCK_SIZE;
        float x0 = ((chunk.x * Chunk.SIZE - totalOffset) * offset) - half;
        float z0 = ((chunk.z * Chunk.SIZE - totalOffset) * offset) - half;
        float x1 = (((chunk.x * Chunk.SIZE + Chunk.SIZE - 1) - totalOffset) * offset) + half;
        float z1 = (((chunk.z * Chunk.SIZE + Chunk.SIZE - 1) - totalOffset) * offset) + half;
        float y0 = -half;
        float y1 = ((Chunk.HEIGHT - 1) * offset) + half;

        glBegin(GL_LINE_LOOP);
        glVertex3f(x0, y0, z0);
        glVertex3f(x1, y0, z0);
        glVertex3f(x1, y0, z1);
        glVertex3f(x0, y0, z1);
        glEnd();

        glBegin(GL_LINE_LOOP);
        glVertex3f(x0, y1, z0);
        glVertex3f(x1, y1, z0);
        glVertex3f(x1, y1, z1);
        glVertex3f(x0, y1, z1);
        glEnd();

        glBegin(GL_LINES);
        glVertex3f(x0, y0, z0); glVertex3f(x0, y1, z0);
        glVertex3f(x1, y0, z0); glVertex3f(x1, y1, z0);
        glVertex3f(x1, y0, z1); glVertex3f(x1, y1, z1);
        glVertex3f(x0, y0, z1); glVertex3f(x0, y1, z1);
        glEnd();
    }


    private boolean isTransparent(ChunkManager cm, Chunk currentChunk, int x, int y, int z, byte currentType) {
        if (y < 0) return false;
        if (y >= Chunk.HEIGHT) return true;
        int worldX = currentChunk.x * Chunk.SIZE + x;
        int worldZ = currentChunk.z * Chunk.SIZE + z;
        byte neighborType = cm.getBlockAtWorld(worldX, y, worldZ);
        if (neighborType == 0) return true;
        if (currentType == Type.STONE.id && neighborType == Type.COBBLE.id) return false;
        return neighborType != currentType;
    }

    public void markChunkDirty(Chunk chunk) {
        ChunkRenderData data = chunkCache.remove(chunk);
        if (data != null) {
            data.destroy();
        }
    }

    public void cleanupCache() {
        if (chunkCache.size() > 512) {
            for (ChunkRenderData data : chunkCache.values()) {
                data.destroy();
            }
            chunkCache.clear();
        }
    }

    private int selectLod(int dx, int dz, float radius) {
        float distance = (float) Math.sqrt((dx * dx) + (dz * dz));
        float nearThreshold = Math.max(2.0f, radius * 0.35f) + LOD_DISTANCE_PADDING;
        float midThreshold = Math.max(4.0f, radius * 0.7f) + LOD_DISTANCE_PADDING;
        if (distance <= nearThreshold) return 0;
        if (distance <= midThreshold) return 1;
        return 2;
    }

    private static class ViewCulling {
        private final int worldSize;
        private final int centerChunkX;
        private final int centerChunkZ;
        private final float radius;
        private final int maxLoopDist;

        private ViewCulling(int worldSize, int centerChunkX, int centerChunkZ, float radius, int maxLoopDist) {
            this.worldSize = worldSize;
            this.centerChunkX = centerChunkX;
            this.centerChunkZ = centerChunkZ;
            this.radius = radius;
            this.maxLoopDist = maxLoopDist;
        }
    }

    private static class ChunkRenderEntry {
        private final Chunk chunk;
        private final int lod;
        private final int distanceSq;

        private ChunkRenderEntry(Chunk chunk, int lod, int distanceSq) {
            this.chunk = chunk;
            this.lod = lod;
            this.distanceSq = distanceSq;
        }
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private class ChunkRenderData {
        private final ChunkMesh[] meshes = new ChunkMesh[2];

        ChunkMesh getMesh(ChunkManager cm, Chunk chunk, int worldSize, int lod) {
            if (lod >= meshes.length) return null;
            if (meshes[lod] == null) {
                meshes[lod] = buildMesh(cm, chunk, worldSize, lod == 1);
            }
            return meshes[lod];
        }

        void destroy() {
            for (ChunkMesh mesh : meshes) {
                if (mesh != null) mesh.destroy();
            }
        }
    }

    private ChunkMesh buildMesh(ChunkManager cm, Chunk chunk, int worldSize, boolean topOnly) {
        MeshBuilder builder = new MeshBuilder(topOnly, true);
        float totalOffset = (worldSize * Chunk.SIZE) / 2f;

        for (int x = 0; x < Chunk.SIZE; x++) {
            for (int z = 0; z < Chunk.SIZE; z++) {
                for (int y = 0; y < Chunk.HEIGHT; y++) {
                    byte type = chunk.getBlock(x, y, z);
                    if (type == 0) continue;

                    Block block = RenderRegistry.get(type);
                    boolean hasSmoothing = block != null && block.getProperties().hasSmoothing();
                    boolean[] faces = new boolean[6];
                    if (hasSmoothing) {
                        faces[0] = isSmoothingOpen(cm, chunk, x, y, z + 1);
                        faces[1] = isSmoothingOpen(cm, chunk, x, y, z - 1);
                        faces[2] = isTransparent(cm, chunk, x, y + 1, z, type);
                        faces[3] = isTransparent(cm, chunk, x, y - 1, z, type);
                        faces[4] = isSmoothingOpen(cm, chunk, x + 1, y, z);
                        faces[5] = isSmoothingOpen(cm, chunk, x - 1, y, z);
                    } else {
                        faces[0] = isTransparent(cm, chunk, x, y, z + 1, type);
                        faces[1] = isTransparent(cm, chunk, x, y, z - 1, type);
                        faces[2] = isTransparent(cm, chunk, x, y + 1, z, type);
                        faces[3] = isTransparent(cm, chunk, x, y - 1, z, type);
                        faces[4] = isTransparent(cm, chunk, x + 1, y, z, type);
                        faces[5] = isTransparent(cm, chunk, x - 1, y, z, type);
                    }

                    if (hasSmoothing && !faces[2]) continue;
                    if (!isAnyFaceVisible(faces)) continue;

                    int wx = chunk.x * Chunk.SIZE + x;
                    int wz = chunk.z * Chunk.SIZE + z;
                    int seed = (wx * 73856093) ^ (y * 19349663) ^ (wz * 83492791);
                    float[][] ao = computeAmbientOcclusion(cm, wx, y, wz, faces, type);
                    builder.addBlock(type, seed, faces, ao, wx, y, wz, totalOffset, BLOCK_SCALE);
                }
            }
        }

        return builder.build();
    }

    private boolean isAnyFaceVisible(boolean[] faces) {
        for (boolean face : faces) {
            if (face) {
                return true;
            }
        }
        return false;
    }

    private boolean isSmoothingOpen(ChunkManager cm, Chunk currentChunk, int x, int y, int z) {
        if (y < 0) return false;
        if (y >= Chunk.HEIGHT) return true;
        int worldX = currentChunk.x * Chunk.SIZE + x;
        int worldZ = currentChunk.z * Chunk.SIZE + z;
        byte neighborType = cm.getBlockAtWorld(worldX, y, worldZ);
        if (neighborType == 0) {
            return !hasSmoothingLimiter(cm, worldX, y, worldZ);
        }
        Block neighbor = RenderRegistry.get(neighborType);
        return neighbor == null || !neighbor.getProperties().isSolid();
    }

    private boolean hasSmoothingLimiter(ChunkManager cm, int worldX, int y, int worldZ) {
        int aboveY = y + 1;
        if (aboveY >= Chunk.HEIGHT) return false;
        byte aboveType = cm.getBlockAtWorld(worldX, aboveY, worldZ);
        Block above = RenderRegistry.get(aboveType);
        return above != null && above.getProperties().hasSmoothing();
    }

    private float[][] computeAmbientOcclusion(ChunkManager cm, int wx, int wy, int wz, boolean[] visibleFaces, byte currentType) {
        float[][] ao = new float[6][4];
        for (int face = 0; face < 6; face++) {
            if (!visibleFaces[face]) {
                for (int i = 0; i < 4; i++) ao[face][i] = 1.0f;
                continue;
            }
            for (int vertex = 0; vertex < 4; vertex++) {
                ao[face][vertex] = faceVertexAo(cm, wx, wy, wz, face, vertex, currentType);
            }
        }
        return ao;
    }

    private float faceVertexAo(ChunkManager cm, int wx, int wy, int wz, int face, int vertex, byte currentType) {
        int[] sideA = sampleOffset(face, vertex, true);
        int[] sideB = sampleOffset(face, vertex, false);
        boolean a = isOccluder(cm, wx + sideA[0], wy + sideA[1], wz + sideA[2], currentType);
        boolean b = isOccluder(cm, wx + sideB[0], wy + sideB[1], wz + sideB[2], currentType);
        boolean corner = isOccluder(cm, wx + sideA[0] + sideB[0], wy + sideA[1] + sideB[1], wz + sideA[2] + sideB[2], currentType);
        int value = (a && b) ? 0 : 3 - ((a ? 1 : 0) + (b ? 1 : 0) + (corner ? 1 : 0));
        return 0.55f + (value * 0.15f);
    }

    private boolean isOccluder(ChunkManager cm, int wx, int wy, int wz, byte currentType) {
        byte type = cm.getBlockAtWorld(wx, wy, wz);
        if (type == 0 || type == currentType) {
            return false;
        }
        Block block = RenderRegistry.get(type);
        return block != null && block.getProperties().isSolid() && !block.getProperties().isTransparent();
    }

    private int[] sampleOffset(int face, int vertex, boolean first) {
        switch (face) {
            case 2: return topOffset(vertex, first);
            case 3: return bottomOffset(vertex, first);
            case 0: return frontOffset(vertex, first);
            case 1: return backOffset(vertex, first);
            case 4: return rightOffset(vertex, first);
            default: return leftOffset(vertex, first);
        }
    }

    private int[] topOffset(int vertex, boolean first) {
        int[][] sideX = {{-1,0,0},{-1,0,0},{1,0,0},{1,0,0}};
        int[][] sideZ = {{0,0,-1},{0,0,1},{0,0,1},{0,0,-1}};
        return first ? sideX[vertex] : sideZ[vertex];
    }

    private int[] bottomOffset(int vertex, boolean first) { return topOffset(vertex, first); }

    private int[] frontOffset(int vertex, boolean first) {
        int[][] sideX = {{-1,0,0},{-1,0,0},{1,0,0},{1,0,0}};
        int[][] sideY = {{0,-1,0},{0,1,0},{0,1,0},{0,-1,0}};
        return first ? sideX[vertex] : sideY[vertex];
    }

    private int[] backOffset(int vertex, boolean first) { return frontOffset(vertex, first); }

    private int[] rightOffset(int vertex, boolean first) {
        int[][] sideZ = {{0,0,-1},{0,0,-1},{0,0,1},{0,0,1}};
        int[][] sideY = {{0,-1,0},{0,1,0},{0,1,0},{0,-1,0}};
        return first ? sideZ[vertex] : sideY[vertex];
    }

    private int[] leftOffset(int vertex, boolean first) { return rightOffset(vertex, first); }
}
