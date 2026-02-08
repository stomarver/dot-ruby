package SwordsGame.client;

import SwordsGame.server.Chunk;
import SwordsGame.server.ChunkManager;
import SwordsGame.client.blocks.Registry;
import SwordsGame.client.blocks.Type;
import SwordsGame.client.Block;
import SwordsGame.client.BlockProperties;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.HashMap;
import java.util.Map;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.glfw.GLFW.glfwGetTime;

public class World {
    public static final float BLOCK_SIZE = 12.5f;
    public static final float BLOCK_SCALE = BLOCK_SIZE * 2.0f;
    private final Map<Chunk, ChunkRenderData> chunkCache = new HashMap<>();
    private final ArrayList<FallingBlock> fallingBlocks = new ArrayList<>();

    public void render(ChunkManager chunkManager, Camera camera) {
        int worldSize = chunkManager.getWorldSizeInChunks();
        float chunkSizeInUnits = Chunk.SIZE * BLOCK_SCALE;

        int camChunkX = (int) Math.floor((-camera.getX()) / chunkSizeInUnits) + (worldSize / 2);
        int camChunkZ = (int) Math.floor((-camera.getZ()) / chunkSizeInUnits) + (worldSize / 2);

        float sinTheta = (float) Math.sin(Math.toRadians(camera.getRotation()));
        float cosTheta = (float) Math.cos(Math.toRadians(camera.getRotation()));

        float baseDist = 4.0f / camera.getZoom();
        float steppedDist = (float) (Math.floor(baseDist / 2 - 0.5f) + 0.5f);
        float horizDist = Math.max(4.0f, Math.min(8.0f, steppedDist));
        float vertDist = Math.max(4.0f, Math.min(8.0f, steppedDist));

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
                        Chunk chunk = chunkManager.getChunk(cx, cz);
                        if (chunk != null) {
                            int lod = selectLod(dx, dz);
                            renderChunkCached(chunkManager, chunk, worldSize, lod);
                        }
                    }
                }
            }
        }

        updateAndRenderFallingBlocks(worldSize);
    }

    public void renderChunkBounds(ChunkManager chunkManager, Camera camera) {
        int worldSize = chunkManager.getWorldSizeInChunks();
        float chunkSizeInUnits = Chunk.SIZE * BLOCK_SCALE;

        int camChunkX = (int) Math.floor((-camera.getX()) / chunkSizeInUnits) + (worldSize / 2);
        int camChunkZ = (int) Math.floor((-camera.getZ()) / chunkSizeInUnits) + (worldSize / 2);

        float sinTheta = (float) Math.sin(Math.toRadians(camera.getRotation()));
        float cosTheta = (float) Math.cos(Math.toRadians(camera.getRotation()));

        float baseDist = 4.0f / camera.getZoom();
        float steppedDist = (float) (Math.floor(baseDist / 2 - 0.5f) + 0.5f);
        float horizDist = Math.max(4.0f, Math.min(8.0f, steppedDist));
        float vertDist = Math.max(4.0f, Math.min(8.0f, steppedDist));

        int maxLoopDist = 12;

        float offset = BLOCK_SCALE;
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
                        Chunk chunk = chunkManager.getChunk(cx, cz);
                        if (chunk != null) {
                            drawChunkBounds(chunk, totalOffset, offset);
                        }
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

    public void renderSelection(int[] target, int worldSize) {
        if (target == null) return;

        float offset = BLOCK_SCALE;
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

        float pulse = (float) (0.6f + 0.4f * Math.sin(glfwGetTime() * 6.0));
        drawSelectionOutline(pulse);
        drawSelectionFill(pulse);

        glDisable(GL_BLEND);
        glEnable(GL_LIGHTING);
        glEnable(GL_TEXTURE_2D);
        glPopMatrix();
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

    private void drawSelectionOutline(float pulse) {
        float s = 1.03f;
        float h = 1.03f;
        glLineWidth(2.5f);
        glColor4f(0.2f, 0.9f, 1.0f, 0.5f + (0.3f * pulse));

        glBegin(GL_LINE_LOOP);
        glVertex3f(-s, -s, -s);
        glVertex3f(s, -s, -s);
        glVertex3f(s, -s, s);
        glVertex3f(-s, -s, s);
        glEnd();

        glBegin(GL_LINE_LOOP);
        glVertex3f(-s, h, -s);
        glVertex3f(s, h, -s);
        glVertex3f(s, h, s);
        glVertex3f(-s, h, s);
        glEnd();

        glBegin(GL_LINES);
        glVertex3f(-s, -s, -s); glVertex3f(-s, h, -s);
        glVertex3f(s, -s, -s); glVertex3f(s, h, -s);
        glVertex3f(s, -s, s); glVertex3f(s, h, s);
        glVertex3f(-s, -s, s); glVertex3f(-s, h, s);
        glEnd();

        glLineWidth(1.0f);
    }

    private void drawSelectionFill(float pulse) {
        float s = 1.02f;
        float h = 1.02f;
        glColor4f(0.1f, 0.6f, 0.9f, 0.15f + (0.2f * pulse));

        glBegin(GL_QUADS);
        glVertex3f(-s, h, -s);
        glVertex3f(s, h, -s);
        glVertex3f(s, h, s);
        glVertex3f(-s, h, s);
        glEnd();
    }

    private boolean isTransparent(ChunkManager cm, Chunk currentChunk, int x, int y, int z, byte currentType) {
        if (y < 0) return false;
        if (y >= Chunk.HEIGHT) return true;
        int worldX = currentChunk.x * Chunk.SIZE + x;
        int worldZ = currentChunk.z * Chunk.SIZE + z;
        byte neighborType = cm.getBlockAtWorld(worldX, y, worldZ);
        return neighborType == 0 || neighborType != currentType;
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

    private int selectLod(int dx, int dz) {
        int dist = Math.max(Math.abs(dx), Math.abs(dz));
        if (dist <= 2) return 0;
        if (dist <= 4) return 1;
        return 2;
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

                    boolean[] faces = new boolean[6];
                    faces[0] = isTransparent(cm, chunk, x, y, z + 1, type);
                    faces[1] = isTransparent(cm, chunk, x, y, z - 1, type);
                    faces[2] = isTransparent(cm, chunk, x, y + 1, z, type);
                    faces[3] = isTransparent(cm, chunk, x, y - 1, z, type);
                    faces[4] = isTransparent(cm, chunk, x + 1, y, z, type);
                    faces[5] = isTransparent(cm, chunk, x - 1, y, z, type);

                    if (!isAnyFaceVisible(faces)) continue;

                    int wx = chunk.x * Chunk.SIZE + x;
                    int wz = chunk.z * Chunk.SIZE + z;
                    if (type == Type.STONE.id) {
                        byte above = getBlockAtWorld(cm, wx, y + 1, wz);
                        if (above == Type.COBBLE.id) {
                            continue;
                        }
                    }
                    int seed = (wx * 73856093) ^ (y * 19349663) ^ (wz * 83492791);
                    float[][] faceVertexColors = buildFaceVertexColors(cm, wx, y, wz, faces);
                    float tintR = 1.0f;
                    float tintG = 1.0f;
                    float tintB = 1.0f;
                    float alpha = 1.0f;
                    boolean forceTransparent = false;

                    if (type == Type.STONE.id) {
                        byte above = getBlockAtWorld(cm, wx, y + 1, wz);
                        if (above == Type.AIR.id) {
                            tintR = 0.6f;
                            tintG = 0.8f;
                            tintB = 1.0f;
                            alpha = 0.55f;
                            forceTransparent = true;
                        }
                    }

                    builder.addBlock(type, seed, faces, wx, y, wz, totalOffset, BLOCK_SCALE,
                            faceVertexColors, tintR, tintG, tintB, alpha, forceTransparent);
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

    private float[][] buildFaceVertexColors(ChunkManager cm, int wx, int wy, int wz, boolean[] faces) {
        float[][] colors = new float[6][4];
        for (int face = 0; face < 6; face++) {
            if (!faces[face]) {
                colors[face] = new float[] {1.0f, 1.0f, 1.0f, 1.0f};
                continue;
            }
            colors[face] = computeFaceVertexColors(cm, wx, wy, wz, face);
        }
        return colors;
    }

    private float[] computeFaceVertexColors(ChunkManager cm, int wx, int wy, int wz, int face) {
        switch (face) {
            case 0:
                return new float[] {
                        ao(cm, wx, wy, wz, -1, 0, 1, 0, -1, 1, -1, -1, 1),
                        ao(cm, wx, wy, wz, 1, 0, 1, 0, -1, 1, 1, -1, 1),
                        ao(cm, wx, wy, wz, 1, 0, 1, 0, 1, 1, 1, 1, 1),
                        ao(cm, wx, wy, wz, -1, 0, 1, 0, 1, 1, -1, 1, 1)
                };
            case 1:
                return new float[] {
                        ao(cm, wx, wy, wz, -1, 0, -1, 0, -1, -1, -1, -1, -1),
                        ao(cm, wx, wy, wz, 1, 0, -1, 0, -1, -1, 1, -1, -1),
                        ao(cm, wx, wy, wz, 1, 0, -1, 0, 1, -1, 1, 1, -1),
                        ao(cm, wx, wy, wz, -1, 0, -1, 0, 1, -1, -1, 1, -1)
                };
            case 2:
                return new float[] {
                        ao(cm, wx, wy, wz, -1, 1, 0, 0, 1, -1, -1, 1, -1),
                        ao(cm, wx, wy, wz, -1, 1, 0, 0, 1, 1, -1, 1, 1),
                        ao(cm, wx, wy, wz, 1, 1, 0, 0, 1, 1, 1, 1, 1),
                        ao(cm, wx, wy, wz, 1, 1, 0, 0, 1, -1, 1, 1, -1)
                };
            case 3:
                return new float[] {
                        ao(cm, wx, wy, wz, -1, -1, 0, 0, -1, -1, -1, -1, -1),
                        ao(cm, wx, wy, wz, -1, -1, 0, 0, -1, 1, -1, -1, 1),
                        ao(cm, wx, wy, wz, 1, -1, 0, 0, -1, 1, 1, -1, 1),
                        ao(cm, wx, wy, wz, 1, -1, 0, 0, -1, -1, 1, -1, -1)
                };
            case 4:
                return new float[] {
                        ao(cm, wx, wy, wz, 1, -1, 0, 1, 0, -1, 1, -1, -1),
                        ao(cm, wx, wy, wz, 1, 1, 0, 1, 0, -1, 1, 1, -1),
                        ao(cm, wx, wy, wz, 1, 1, 0, 1, 0, 1, 1, 1, 1),
                        ao(cm, wx, wy, wz, 1, -1, 0, 1, 0, 1, 1, -1, 1)
                };
            case 5:
                return new float[] {
                        ao(cm, wx, wy, wz, -1, -1, 0, -1, 0, -1, -1, -1, -1),
                        ao(cm, wx, wy, wz, -1, 1, 0, -1, 0, -1, -1, 1, -1),
                        ao(cm, wx, wy, wz, -1, 1, 0, -1, 0, 1, -1, 1, 1),
                        ao(cm, wx, wy, wz, -1, -1, 0, -1, 0, 1, -1, -1, 1)
                };
            default:
                return new float[] {1.0f, 1.0f, 1.0f, 1.0f};
        }
    }

    private float ao(ChunkManager cm, int wx, int wy, int wz,
                     int sx1, int sy1, int sz1,
                     int sx2, int sy2, int sz2,
                     int cx, int cy, int cz) {
        int side1 = isOccluding(cm, wx + sx1, wy + sy1, wz + sz1) ? 1 : 0;
        int side2 = isOccluding(cm, wx + sx2, wy + sy2, wz + sz2) ? 1 : 0;
        int corner = isOccluding(cm, wx + cx, wy + cy, wz + cz) ? 1 : 0;
        int occlusion = (side1 == 1 && side2 == 1) ? 3 : (side1 + side2 + corner);
        switch (occlusion) {
            case 0:
                return 1.0f;
            case 1:
                return 0.8f;
            case 2:
                return 0.6f;
            default:
                return 0.5f;
        }
    }

    private boolean isOccluding(ChunkManager cm, int wx, int wy, int wz) {
        if (wy < 0 || wy >= Chunk.HEIGHT) return false;
        byte type = getBlockAtWorld(cm, wx, wy, wz);
        if (type == Type.AIR.id) return false;
        Block block = Registry.get(type);
        if (block == null) return false;
        BlockProperties props = block.getProperties();
        return props.isSolid() && !props.isTransparent();
    }

    private byte getBlockAtWorld(ChunkManager cm, int wx, int wy, int wz) {
        if (wy < 0 || wy >= Chunk.HEIGHT) return Type.AIR.id;
        return cm.getBlockAtWorld(wx, wy, wz);
    }
}
