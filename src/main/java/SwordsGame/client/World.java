package SwordsGame.client;

import SwordsGame.client.blocks.BlockRegistry;
import SwordsGame.server.Chunk;
import SwordsGame.server.ChunkManager;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.HashMap;
import java.util.Map;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.glfw.GLFW.glfwGetTime;

public class World {
    public static final float BLOCK_SIZE = 12.5f;
    private final Map<Chunk, Integer> chunkCache = new HashMap<>();
    private final ArrayList<FallingBlock> fallingBlocks = new ArrayList<>();

    public void render(ChunkManager chunkManager, Camera camera) {
        Chunk[][] chunks = chunkManager.getChunks();
        int worldSize = chunkManager.getWorldSizeInChunks();
        float chunkSizeInUnits = Chunk.SIZE * (BLOCK_SIZE * 2);

        int camChunkX = (int) Math.floor((-camera.getX()) / chunkSizeInUnits) + (worldSize / 2);
        int camChunkZ = (int) Math.floor((-camera.getZ()) / chunkSizeInUnits) + (worldSize / 2);

        float sinTheta = (float) Math.sin(Math.toRadians(camera.getRotation()));
        float cosTheta = (float) Math.cos(Math.toRadians(camera.getRotation()));

        float baseDist = 4.0f / camera.getZoom();
        float steppedDist = (float) (Math.floor(baseDist / 2 - 0.5f) + 0.5f);
        float horizDist = Math.max(1.0f, Math.min(5.0f, steppedDist));
        float vertDist = Math.max(1.0f, Math.min(5.0f, steppedDist));

        int maxLoopDist = 12;

        cleanupCache();

        for (int dx = -maxLoopDist; dx <= maxLoopDist; dx++) {
            for (int dz = -maxLoopDist; dz <= maxLoopDist; dz++) {
                int cx = camChunkX + dx;
                int cz = camChunkZ + dz;
                if (cx >= 0 && cx < worldSize && cz >= 0 && cz < worldSize) {
                    float depth = dx * (-sinTheta) + dz * cosTheta;
                    float lateral = dx * cosTheta + dz * sinTheta;

                    if (Math.abs(depth) <= vertDist + 0.5f && Math.abs(lateral) <= horizDist + 0.5f) {
                        renderChunkCached(chunkManager, chunks[cx][cz], worldSize);
                    }
                }
            }
        }

        updateAndRenderFallingBlocks(worldSize);
    }

    public void renderChunkBounds(ChunkManager chunkManager, Camera camera) {
        Chunk[][] chunks = chunkManager.getChunks();
        int worldSize = chunkManager.getWorldSizeInChunks();
        float chunkSizeInUnits = Chunk.SIZE * (BLOCK_SIZE * 2);

        int camChunkX = (int) Math.floor((-camera.getX()) / chunkSizeInUnits) + (worldSize / 2);
        int camChunkZ = (int) Math.floor((-camera.getZ()) / chunkSizeInUnits) + (worldSize / 2);

        float sinTheta = (float) Math.sin(Math.toRadians(camera.getRotation()));
        float cosTheta = (float) Math.cos(Math.toRadians(camera.getRotation()));

        float baseDist = 4.0f / camera.getZoom();
        float steppedDist = (float) (Math.floor(baseDist / 2 - 0.5f) + 0.5f);
        float horizDist = Math.max(1.0f, Math.min(5.0f, steppedDist));
        float vertDist = Math.max(1.0f, Math.min(5.0f, steppedDist));

        int maxLoopDist = 12;

        float offset = BLOCK_SIZE * 2;
        float totalOffset = (worldSize * Chunk.SIZE) / 2f;

        glDisable(GL_TEXTURE_2D);
        glDisable(GL_LIGHTING);
        glColor4f(0.2f, 0.9f, 0.9f, 0.7f);
        glLineWidth(2.0f);

        for (int dx = -maxLoopDist; dx <= maxLoopDist; dx++) {
            for (int dz = -maxLoopDist; dz <= maxLoopDist; dz++) {
                int cx = camChunkX + dx;
                int cz = camChunkZ + dz;
                if (cx >= 0 && cx < worldSize && cz >= 0 && cz < worldSize) {
                    float depth = dx * (-sinTheta) + dz * cosTheta;
                    float lateral = dx * cosTheta + dz * sinTheta;

                    if (Math.abs(depth) <= vertDist + 0.5f && Math.abs(lateral) <= horizDist + 0.5f) {
                        drawChunkBounds(chunks[cx][cz], totalOffset, offset);
                    }
                }
            }
        }

        glLineWidth(1.0f);
        glEnable(GL_LIGHTING);
        glEnable(GL_TEXTURE_2D);
        glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
    }

    private void updateAndRenderFallingBlocks(int worldSize) {
        double currentTime = glfwGetTime();
        float deltaTime = 1.0f / 60.0f;
        float totalOffset = (worldSize * Chunk.SIZE) / 2f;
        float offset = BLOCK_SIZE * 2;

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
            glScalef(BLOCK_SIZE, BLOCK_SIZE, BLOCK_SIZE);

            glEnable(GL_BLEND);
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
            glColor4f(1.0f, 1.0f, 1.0f, alpha);

            boolean[] allFaces = {true, true, true, true, true, true};
            BlockRegistry.draw(block.type, block.seed, allFaces);

            glDisable(GL_BLEND);
            glColor3f(1.0f, 1.0f, 1.0f);
            glPopMatrix();
        }
    }

    public void addFallingBlock(int wx, int wy, int wz, byte type) {
        int seed = (wx * 73856093) ^ (wy * 19349663) ^ (wz * 83492791);
        fallingBlocks.add(new FallingBlock(wx, wy, wz, type, seed, glfwGetTime()));
    }

    public void renderSelection(int[] target, int worldSize) {
        if (target == null) return;

        float offset = BLOCK_SIZE * 2;
        float totalOffset = (worldSize * Chunk.SIZE) / 2f;

        glPushMatrix();
        float tx = (target[0] - totalOffset) * offset;
        float ty = target[1] * offset;
        float tz = (target[2] - totalOffset) * offset;

        glTranslatef(tx, ty, tz);
        glScalef(BLOCK_SIZE, BLOCK_SIZE, BLOCK_SIZE);

        glDisable(GL_TEXTURE_2D);
        glDisable(GL_LIGHTING);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        glLineWidth(4.0f);
        glColor4f(1.0f, 1.0f, 1.0f, 0.8f);

        glBegin(GL_LINE_LOOP);
        float s = 1.01f;
        float h = 1.02f;
        glVertex3f(-s, h, -s);
        glVertex3f( s, h, -s);
        glVertex3f( s, h,  s);
        glVertex3f(-s, h,  s);
        glEnd();

        glDisable(GL_BLEND);
        glEnable(GL_LIGHTING);
        glEnable(GL_TEXTURE_2D);
        glPopMatrix();
    }

    private void renderChunkCached(ChunkManager cm, Chunk chunk, int worldSize) {
        if (!chunkCache.containsKey(chunk)) {
            int listId = glGenLists(1);
            glNewList(listId, GL_COMPILE);
            float offset = BLOCK_SIZE * 2;
            float totalOffset = (worldSize * Chunk.SIZE) / 2f;

            for (int x = 0; x < Chunk.SIZE; x++) {
                for (int z = 0; z < Chunk.SIZE; z++) {
                    for (int y = 0; y < Chunk.HEIGHT; y++) {
                        byte type = chunk.getBlock(x, y, z);
                        if (type == 0) continue;

                        boolean[] faces = new boolean[6];
                        faces[0] = isTransparent(cm, chunk, x, y, z + 1, type);
                        faces[1] = isTransparent(cm, chunk, x, y, z - 1, type);
                        faces[2] = isTransparent(cm, chunk, x, y + 1, z, type);
                        faces[3] = isTransparent(cm, chunk, x, y - 1, z, type);
                        faces[4] = isTransparent(cm, chunk, x + 1, y, z, type);
                        faces[5] = isTransparent(cm, chunk, x - 1, y, z, type);

                        if (isAnyFaceVisible(faces)) {
                            int wx = chunk.x * Chunk.SIZE + x;
                            int wz = chunk.z * Chunk.SIZE + z;
                            drawOptimizedBlock(wx, y, wz, totalOffset, offset, type, faces);
                        }
                    }
                }
            }
            glEndList();
            chunkCache.put(chunk, listId);
        }
        glCallList(chunkCache.get(chunk));
    }

    private void drawChunkBounds(Chunk chunk, float totalOffset, float offset) {
        float x0 = (chunk.x * Chunk.SIZE - totalOffset) * offset;
        float z0 = (chunk.z * Chunk.SIZE - totalOffset) * offset;
        float x1 = (chunk.x * Chunk.SIZE + Chunk.SIZE - totalOffset) * offset;
        float z1 = (chunk.z * Chunk.SIZE + Chunk.SIZE - totalOffset) * offset;
        float y0 = 0;
        float y1 = Chunk.HEIGHT * offset;

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

    private boolean isAnyFaceVisible(boolean[] faces) {
        for (boolean face : faces) {
            if (face) {
                return true;
            }
        }
        return false;
    }

    private boolean isTransparent(ChunkManager cm, Chunk currentChunk, int x, int y, int z, byte currentType) {
        if (y < 0) return false;
        if (y >= Chunk.HEIGHT) return true;
        int worldX = currentChunk.x * Chunk.SIZE + x;
        int worldZ = currentChunk.z * Chunk.SIZE + z;
        int maxBlocks = cm.getWorldSizeInChunks() * Chunk.SIZE;
        if (worldX < 0 || worldX >= maxBlocks || worldZ < 0 || worldZ >= maxBlocks) return true;
        int targetChunkX = worldX / Chunk.SIZE;
        int targetChunkZ = worldZ / Chunk.SIZE;
        byte neighborType = cm.getChunks()[targetChunkX][targetChunkZ].getBlock(worldX % Chunk.SIZE, y, worldZ % Chunk.SIZE);
        return neighborType == 0 || neighborType != currentType;
    }

    public void markChunkDirty(Chunk chunk) {
        if (chunkCache.containsKey(chunk)) {
            glDeleteLists(chunkCache.get(chunk), 1);
            chunkCache.remove(chunk);
        }
    }

    private void drawOptimizedBlock(int x, int y, int z, float totalOffset, float scale, byte type, boolean[] faces) {
        glPushMatrix();
        glTranslatef((x - totalOffset) * scale, y * scale, (z - totalOffset) * scale);
        glScalef(BLOCK_SIZE, BLOCK_SIZE, BLOCK_SIZE);

        int seed = (x * 73856093) ^ (y * 19349663) ^ (z * 83492791);
        BlockRegistry.draw(type, seed, faces);

        glPopMatrix();
    }

    public void cleanupCache() {
        if (chunkCache.size() > 512) {
            for (Integer id : chunkCache.values()) glDeleteLists(id, 1);
            chunkCache.clear();
        }
    }
}
