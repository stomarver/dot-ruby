package SwordsGame.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.joml.Vector2f;
import SwordsGame.client.blocks.Reg;
import SwordsGame.client.graphics.Blk;
import SwordsGame.client.graphics.BlkRdr;
import SwordsGame.client.graphics.ChkMsh;
import SwordsGame.client.graphics.MshBld;
import SwordsGame.server.Chk;
import SwordsGame.server.ChMgr;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.glfw.GLFW.glfwGetTime;

public class Wld {
    public static final float BLOCK_SIZE = 12.5f;
    public static final float BLOCK_SCALE = BLOCK_SIZE * 2.0f;
    private static final float RENDER_RADIUS_PADDING = 6.0f;
    private static final float LOD_DISTANCE_PADDING = 5.0f;
    private final Map<Chk, ChunkRenderData> chunkCache = new HashMap<>();
    private final ArrayList<FallBlk> fallingBlocks = new ArrayList<>();

    public void render(ChMgr chunkManager, Cam camera) {
        ViewCulling culling = buildCulling(chunkManager, camera);
        ArrayList<ChunkRenderEntry> visibleChunks = collectVisibleChunks(chunkManager, culling);

        cleanupCache();

        renderVisibleChunks(chunkManager, culling, visibleChunks);
        updateAndRenderFallingBlocks(culling.worldSize);
    }

    public void renderChunkBounds(ChMgr chunkManager, Cam camera) {
        ViewCulling culling = buildCulling(chunkManager, camera);
        ArrayList<ChunkRenderEntry> visibleChunks = collectVisibleChunks(chunkManager, culling);
        float offset = BLOCK_SCALE;
        float totalOffset = (culling.worldSize * Chk.SIZE) / 2f;

        glDisable(GL_TEXTURE_2D);
        glDisable(GL_LIGHTING);
        glDisable(GL_FOG);
        glDisable(GL_DEPTH_TEST);
        glLineWidth(1.5f);

        renderVisibleChunkBounds(visibleChunks, culling, totalOffset, offset);

        glLineWidth(1.0f);
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_FOG);
        glEnable(GL_LIGHTING);
        glEnable(GL_TEXTURE_2D);
        glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
    }

    private ViewCulling buildCulling(ChMgr chunkManager, Cam camera) {
        int worldSize = chunkManager.getWorldSizeInChunks();
        float chunkSizeInUnits = Chk.SIZE * BLOCK_SCALE;

        float totalOffsetBlocks = chunkManager.getWorldSizeInBlocks() / 2.0f;
        Vector2f cameraBlocks = new Vector2f(
                (-camera.getX() / BLOCK_SCALE) + totalOffsetBlocks,
                (-camera.getZ() / BLOCK_SCALE) + totalOffsetBlocks);

        int focusChunkX = clamp((int) Math.floor(cameraBlocks.x / Chk.SIZE), 0, worldSize - 1);
        int focusChunkZ = clamp((int) Math.floor(cameraBlocks.y / Chk.SIZE), 0, worldSize - 1);

        float halfWidthUnits = (camera.getOrthoWidth() / 2.0f) / camera.getZoom();
        float halfHeightUnits = (camera.getOrthoHeight() / 2.0f) / camera.getZoom();
        float pitchCos = (float) Math.cos(Math.toRadians(camera.getPitch()));
        float depthUnits = pitchCos == 0.0f ? halfHeightUnits : (halfHeightUnits / pitchCos);

        float baseRadius = Math.max(halfWidthUnits, depthUnits) / chunkSizeInUnits;
        float radius = baseRadius + RENDER_RADIUS_PADDING;
        int maxLoopDist = (int) Math.ceil(radius);

        return new ViewCulling(worldSize, focusChunkX, focusChunkZ, radius, maxLoopDist);
    }

    private ArrayList<ChunkRenderEntry> collectVisibleChunks(ChMgr chunkManager, ViewCulling culling) {
        ArrayList<ChunkRenderEntry> entries = new ArrayList<>();
        float radiusSquared = culling.radius * culling.radius;
        for (int dx = -culling.maxLoopDist; dx <= culling.maxLoopDist; dx++) {
            for (int dz = -culling.maxLoopDist; dz <= culling.maxLoopDist; dz++) {
                int cx = culling.centerChunkX + dx;
                int cz = culling.centerChunkZ + dz;
                if (cx >= 0 && cx < culling.worldSize && cz >= 0 && cz < culling.worldSize) {
                    int distanceSq = (dx * dx) + (dz * dz);
                    if (distanceSq <= radiusSquared) {
                        Chk chunk = chunkManager.getChunk(cx, cz);
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

    private void renderVisibleChunks(ChMgr chunkManager, ViewCulling culling, ArrayList<ChunkRenderEntry> entries) {
        for (ChunkRenderEntry entry : entries) {
            renderChunkCached(chunkManager, entry.chunk, culling.worldSize, entry.lod);
        }
    }

    private void renderVisibleChunkBounds(ArrayList<ChunkRenderEntry> entries, ViewCulling culling, float totalOffset, float offset) {
        for (ChunkRenderEntry entry : entries) {
            if (entry.chunk.x == culling.centerChunkX && entry.chunk.z == culling.centerChunkZ) {
                glColor4f(1.0f, 0.85f, 0.25f, 0.95f);
                glLineWidth(2.8f);
            } else {
                float t = clamp((float) entry.distanceSq / Math.max(1.0f, culling.radius * culling.radius), 0.0f, 1.0f);
                float r = 0.2f + (0.25f * (1.0f - t));
                float g = 0.45f + (0.5f * (1.0f - t));
                float b = 0.85f + (0.15f * (1.0f - t));
                float a = 0.35f + (0.45f * (1.0f - t));
                glColor4f(r, g, b, a);
                glLineWidth(1.2f + (0.9f * (1.0f - t)));
            }
            drawChunkBounds(entry.chunk, totalOffset, offset);
        }
    }

    private void updateAndRenderFallingBlocks(int worldSize) {
        double currentTime = glfwGetTime();
        float deltaTime = 1.0f / 60.0f;
        float totalOffset = (worldSize * Chk.SIZE) / 2f;
        float offset = BLOCK_SCALE;

        Iterator<FallBlk> iterator = fallingBlocks.iterator();
        while (iterator.hasNext()) {
            FallBlk block = iterator.next();

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
            BlkRdr.renderBlock(block.type, block.seed, allFaces, alpha);

            glDisable(GL_BLEND);
            glColor3f(1.0f, 1.0f, 1.0f);
            glPopMatrix();
        }
    }

    public void addFallingBlock(int wx, int wy, int wz, byte type) {
        int seed = (wx * 73856093) ^ (wy * 19349663) ^ (wz * 83492791);
        fallingBlocks.add(new FallBlk(wx, wy, wz, type, seed, glfwGetTime()));
    }


    private void renderChunkCached(ChMgr cm, Chk chunk, int worldSize, int lod) {
        if (lod >= 2) {
            return;
        }
        ChunkRenderData data = chunkCache.computeIfAbsent(chunk, key -> new ChunkRenderData());
        ChkMsh mesh = data.getMesh(cm, chunk, worldSize, lod);
        if (mesh != null) {
            mesh.render();
        }
    }

    private void drawChunkBounds(Chk chunk, float totalOffset, float offset) {
        float half = BLOCK_SIZE;
        float x0 = ((chunk.x * Chk.SIZE - totalOffset) * offset) - half;
        float z0 = ((chunk.z * Chk.SIZE - totalOffset) * offset) - half;
        float x1 = (((chunk.x * Chk.SIZE + Chk.SIZE - 1) - totalOffset) * offset) + half;
        float z1 = (((chunk.z * Chk.SIZE + Chk.SIZE - 1) - totalOffset) * offset) + half;
        float y0 = -half;
        float y1 = ((Chk.HEIGHT - 1) * offset) + half;

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


    private boolean isTransparent(ChMgr cm, Chk currentChunk, int x, int y, int z, byte currentType) {
        if (y < 0) return false;
        if (y >= Chk.HEIGHT) return true;
        int worldX = currentChunk.x * Chk.SIZE + x;
        int worldZ = currentChunk.z * Chk.SIZE + z;
        byte neighborType = cm.getBlockAtWorld(worldX, y, worldZ);
        return neighborType == 0 || neighborType != currentType;
    }

    public void markChunkDirty(Chk chunk) {
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
        float distance = new Vector2f(dx, dz).length();
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
        private final Chk chunk;
        private final int lod;
        private final int distanceSq;

        private ChunkRenderEntry(Chk chunk, int lod, int distanceSq) {
            this.chunk = chunk;
            this.lod = lod;
            this.distanceSq = distanceSq;
        }
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    private class ChunkRenderData {
        private final ChkMsh[] meshes = new ChkMsh[2];

        ChkMsh getMesh(ChMgr cm, Chk chunk, int worldSize, int lod) {
            if (lod >= meshes.length) return null;
            if (meshes[lod] == null) {
                meshes[lod] = buildMesh(cm, chunk, worldSize, lod == 1);
            }
            return meshes[lod];
        }

        void destroy() {
            for (ChkMsh mesh : meshes) {
                if (mesh != null) mesh.destroy();
            }
        }
    }

    private ChkMsh buildMesh(ChMgr cm, Chk chunk, int worldSize, boolean topOnly) {
        MshBld builder = new MshBld(topOnly, true);
        float totalOffset = (worldSize * Chk.SIZE) / 2f;

        for (int x = 0; x < Chk.SIZE; x++) {
            for (int z = 0; z < Chk.SIZE; z++) {
                for (int y = 0; y < Chk.HEIGHT; y++) {
                    byte type = chunk.getBlock(x, y, z);
                    if (type == 0) continue;

                    Blk block = Reg.get(type);
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

                    int wx = chunk.x * Chk.SIZE + x;
                    int wz = chunk.z * Chk.SIZE + z;
                    int seed = (wx * 73856093) ^ (y * 19349663) ^ (wz * 83492791);
                    builder.addBlock(type, seed, faces, wx, y, wz, totalOffset, BLOCK_SCALE);
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

    private boolean isSmoothingOpen(ChMgr cm, Chk currentChunk, int x, int y, int z) {
        if (y < 0) return false;
        if (y >= Chk.HEIGHT) return true;
        int worldX = currentChunk.x * Chk.SIZE + x;
        int worldZ = currentChunk.z * Chk.SIZE + z;
        byte neighborType = cm.getBlockAtWorld(worldX, y, worldZ);
        if (neighborType == 0) {
            return !hasSmoothingLimiter(cm, worldX, y, worldZ);
        }
        Blk neighbor = Reg.get(neighborType);
        return neighbor == null || !neighbor.getProperties().isSolid();
    }

    private boolean hasSmoothingLimiter(ChMgr cm, int worldX, int y, int worldZ) {
        int aboveY = y + 1;
        if (aboveY >= Chk.HEIGHT) return false;
        byte aboveType = cm.getBlockAtWorld(worldX, aboveY, worldZ);
        Blk above = Reg.get(aboveType);
        return above != null && above.getProperties().hasSmoothing();
    }
}
