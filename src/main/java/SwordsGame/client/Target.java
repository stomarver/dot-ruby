package SwordsGame.client;

import SwordsGame.client.graphics.Renderer;
import SwordsGame.core.Window;
import SwordsGame.server.Chunk;
import SwordsGame.server.ChunkManager;

public class Target {
    private final int x;
    private final int y;
    private final int z;
    private final boolean hit;

    private Target(int x, int y, int z, boolean hit) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.hit = hit;
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public int getZ() { return z; }
    public boolean hasHit() { return hit; }

    public static Target fromMouse(Window window, Camera camera, Renderer renderer, ChunkManager chunkManager) {
        float centerX = renderer.getViewportX() + renderer.getViewportWidth() / 2.0f;
        float centerY = renderer.getViewportY() + renderer.getViewportHeight() / 2.0f;
        float mouseX = (float) window.getMouseRelX() - centerX;
        float mouseY = centerY - (float) window.getMouseRelY();

        float viewX = (mouseX / renderer.getViewportWidth()) * camera.getOrthoWidth();
        float viewY = (mouseY / renderer.getViewportHeight()) * camera.getOrthoHeight();

        return raycastSolid(camera, chunkManager, viewX, viewY);
    }

    public static Target focus(Camera camera, ChunkManager chunkManager) {
        return raycastTopSurface(camera, chunkManager, 0.0f, 0.0f);
    }

    private static Target raycastSolid(Camera camera, ChunkManager chunkManager, float viewX, float viewY) {
        return raycast(camera, chunkManager, viewX, viewY, (cm, x, y, z) -> cm.getBlockAtWorld(x, y, z) != 0);
    }

    private static Target raycastTopSurface(Camera camera, ChunkManager chunkManager, float viewX, float viewY) {
        return raycast(camera, chunkManager, viewX, viewY, ChunkManager::isTopSurface);
    }

    private static Target raycast(Camera camera, ChunkManager chunkManager, float viewX, float viewY, HitPredicate predicate) {
        float[] rayOrigin = unprojectViewToWorld(camera, viewX, viewY, 0.0f);
        float[] rayFar = unprojectViewToWorld(camera, viewX, viewY, 1.0f);
        float[] rayDir = normalize(rayFar[0] - rayOrigin[0], rayFar[1] - rayOrigin[1], rayFar[2] - rayOrigin[2]);

        float blockScale = World.BLOCK_SCALE;
        float totalOffsetBlocks = chunkManager.getWorldSizeInBlocks() / 2.0f;

        double ox = (rayOrigin[0] / blockScale) + totalOffsetBlocks;
        double oy = rayOrigin[1] / blockScale;
        double oz = (rayOrigin[2] / blockScale) + totalOffsetBlocks;

        double dx = rayDir[0];
        double dy = rayDir[1];
        double dz = rayDir[2];

        int x = (int) Math.floor(ox);
        int y = (int) Math.floor(oy);
        int z = (int) Math.floor(oz);

        int stepX = dx > 0 ? 1 : -1;
        int stepY = dy > 0 ? 1 : -1;
        int stepZ = dz > 0 ? 1 : -1;

        double tMaxX = intBound(ox, dx);
        double tMaxY = intBound(oy, dy);
        double tMaxZ = intBound(oz, dz);

        double tDeltaX = dx == 0 ? Double.POSITIVE_INFINITY : Math.abs(1.0 / dx);
        double tDeltaY = dy == 0 ? Double.POSITIVE_INFINITY : Math.abs(1.0 / dy);
        double tDeltaZ = dz == 0 ? Double.POSITIVE_INFINITY : Math.abs(1.0 / dz);

        int maxSteps = chunkManager.getWorldSizeInBlocks() * 2;

        int worldSizeBlocks = chunkManager.getWorldSizeInBlocks();
        for (int i = 0; i < maxSteps; i++) {
            if (x < 0 || z < 0 || x >= worldSizeBlocks || z >= worldSizeBlocks) {
                break;
            }
            if (y >= 0 && y < Chunk.HEIGHT && predicate.hit(chunkManager, x, y, z)) {
                return new Target(x, y, z, true);
            }

            if (tMaxX < tMaxY) {
                if (tMaxX < tMaxZ) {
                    x += stepX;
                    tMaxX += tDeltaX;
                } else {
                    z += stepZ;
                    tMaxZ += tDeltaZ;
                }
            } else {
                if (tMaxY < tMaxZ) {
                    y += stepY;
                    tMaxY += tDeltaY;
                } else {
                    z += stepZ;
                    tMaxZ += tDeltaZ;
                }
            }

            if (x < 0 || z < 0 || x >= worldSizeBlocks || z >= worldSizeBlocks) {
                break;
            }
        }

        return new Target(0, 0, 0, false);
    }

    private static float[] unprojectViewToWorld(Camera camera, float viewX, float viewY, float viewZ) {
        float scaledX = viewX / camera.getZoom();
        float scaledY = viewY / camera.getZoom();
        float scaledZ = viewZ / camera.getZoom();

        float[] afterPitch = rotateX(scaledX, scaledY, scaledZ, -camera.getPitch());
        float[] afterYaw = rotateY(afterPitch[0], afterPitch[1], afterPitch[2], -camera.getRotation());

        afterYaw[0] -= camera.getX();
        afterYaw[2] -= camera.getZ();

        return afterYaw;
    }

    private static float[] normalize(float x, float y, float z) {
        float length = (float) Math.sqrt(x * x + y * y + z * z);
        if (length == 0) return new float[]{0.0f, 0.0f, 0.0f};
        return new float[]{x / length, y / length, z / length};
    }

    private static float[] rotateX(float x, float y, float z, float degrees) {
        float rad = (float) Math.toRadians(degrees);
        float cos = (float) Math.cos(rad);
        float sin = (float) Math.sin(rad);
        return new float[]{x, y * cos - z * sin, y * sin + z * cos};
    }

    private static float[] rotateY(float x, float y, float z, float degrees) {
        float rad = (float) Math.toRadians(degrees);
        float cos = (float) Math.cos(rad);
        float sin = (float) Math.sin(rad);
        return new float[]{x * cos + z * sin, y, -x * sin + z * cos};
    }

    private static double intBound(double s, double ds) {
        if (ds == 0) return Double.POSITIVE_INFINITY;
        double sIsInteger = Math.floor(s);
        if (ds > 0) {
            return (sIsInteger + 1 - s) / ds;
        }
        return (s - sIsInteger) / -ds;
    }

    private interface HitPredicate {
        boolean hit(ChunkManager chunkManager, int x, int y, int z);
    }
}
