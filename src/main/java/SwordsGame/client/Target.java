package SwordsGame.client;

import SwordsGame.client.graphics.Renderer;
import SwordsGame.core.Window;
import SwordsGame.server.Chunk;
import SwordsGame.server.ChunkManager;

public class Target {
    private static final float MAX_RAYCAST_MULTIPLIER = 2.0f;
    private final int x;
    private final int y;
    private final int z;
    private final int faceX;
    private final int faceY;
    private final int faceZ;
    private final boolean hit;

    private Target(int x, int y, int z, int faceX, int faceY, int faceZ, boolean hit) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.faceX = faceX;
        this.faceY = faceY;
        this.faceZ = faceZ;
        this.hit = hit;
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public int getZ() { return z; }
    public int getFaceX() { return faceX; }
    public int getFaceY() { return faceY; }
    public int getFaceZ() { return faceZ; }
    public boolean hasHit() { return hit; }

    public static Target fromMouse(Window window, Camera camera, Renderer renderer, ChunkManager chunkManager) {
        float centerX = renderer.getViewportX() + renderer.getViewportWidth() / 2.0f;
        float centerY = renderer.getViewportY() + renderer.getViewportHeight() / 2.0f;
        float mouseX = (float) window.getMouseRelX() - centerX;
        float mouseY = centerY - (float) window.getMouseRelY();

        float viewX = (mouseX / renderer.getViewportWidth()) * camera.getOrthoWidth();
        float viewY = (mouseY / renderer.getViewportHeight()) * camera.getOrthoHeight();
        return raycast(camera, chunkManager, viewX, viewY, Target::isSolidBlock);
    }

    public static Target focus(Camera camera, ChunkManager chunkManager) {
        return raycast(camera, chunkManager, 0.0f, 0.0f, ChunkManager::isTopSurface);
    }

    private static boolean isSolidBlock(ChunkManager chunkManager, int x, int y, int z) {
        return chunkManager.getBlockAtWorld(x, y, z) != 0;
    }

    private static Target raycast(Camera camera, ChunkManager chunkManager, float viewX, float viewY, HitPredicate predicate) {
        Ray ray = Ray.fromView(camera, viewX, viewY);
        float blockScale = World.BLOCK_SCALE;
        float totalOffsetBlocks = chunkManager.getWorldSizeInBlocks() / 2.0f;

        double ox = (ray.originX / blockScale) + totalOffsetBlocks;
        double oy = ray.originY / blockScale;
        double oz = (ray.originZ / blockScale) + totalOffsetBlocks;

        double dx = ray.dirX;
        double dy = ray.dirY;
        double dz = ray.dirZ;

        int x = (int) Math.floor(ox);
        int y = (int) Math.floor(oy);
        int z = (int) Math.floor(oz);

        int worldSizeBlocks = chunkManager.getWorldSizeInBlocks();
        double maxDistance = worldSizeBlocks * MAX_RAYCAST_MULTIPLIER;

        if (isWithinWorld(x, y, z, worldSizeBlocks) && predicate.hit(chunkManager, x, y, z)) {
            return new Target(x, y, z, 0, 0, 0, true);
        }

        int stepX = dx > 0 ? 1 : -1;
        int stepY = dy > 0 ? 1 : -1;
        int stepZ = dz > 0 ? 1 : -1;

        double tMaxX = intBound(ox, dx);
        double tMaxY = intBound(oy, dy);
        double tMaxZ = intBound(oz, dz);

        double tDeltaX = dx == 0 ? Double.POSITIVE_INFINITY : Math.abs(1.0 / dx);
        double tDeltaY = dy == 0 ? Double.POSITIVE_INFINITY : Math.abs(1.0 / dy);
        double tDeltaZ = dz == 0 ? Double.POSITIVE_INFINITY : Math.abs(1.0 / dz);

        int faceX = 0;
        int faceY = 0;
        int faceZ = 0;
        double distanceTravelled = 0.0;

        while (distanceTravelled <= maxDistance) {
            if (tMaxX < tMaxY) {
                if (tMaxX < tMaxZ) {
                    x += stepX;
                    distanceTravelled = tMaxX;
                    tMaxX += tDeltaX;
                    faceX = -stepX;
                    faceY = 0;
                    faceZ = 0;
                } else {
                    z += stepZ;
                    distanceTravelled = tMaxZ;
                    tMaxZ += tDeltaZ;
                    faceX = 0;
                    faceY = 0;
                    faceZ = -stepZ;
                }
            } else {
                if (tMaxY < tMaxZ) {
                    y += stepY;
                    distanceTravelled = tMaxY;
                    tMaxY += tDeltaY;
                    faceX = 0;
                    faceY = -stepY;
                    faceZ = 0;
                } else {
                    z += stepZ;
                    distanceTravelled = tMaxZ;
                    tMaxZ += tDeltaZ;
                    faceX = 0;
                    faceY = 0;
                    faceZ = -stepZ;
                }
            }

            if (!isWithinWorld(x, y, z, worldSizeBlocks)) {
                break;
            }

            if (predicate.hit(chunkManager, x, y, z)) {
                return new Target(x, y, z, faceX, faceY, faceZ, true);
            }
        }

        return new Target(0, 0, 0, 0, 0, 0, false);
    }

    private static boolean isWithinWorld(int x, int y, int z, int worldSizeBlocks) {
        return x >= 0 && z >= 0 && x < worldSizeBlocks && z < worldSizeBlocks && y >= 0 && y < Chunk.HEIGHT;
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

    private static class Ray {
        private final float originX;
        private final float originY;
        private final float originZ;
        private final float dirX;
        private final float dirY;
        private final float dirZ;

        private Ray(float originX, float originY, float originZ, float dirX, float dirY, float dirZ) {
            this.originX = originX;
            this.originY = originY;
            this.originZ = originZ;
            this.dirX = dirX;
            this.dirY = dirY;
            this.dirZ = dirZ;
        }

        private static Ray fromView(Camera camera, float viewX, float viewY) {
            float[] origin = unprojectViewToWorld(camera, viewX, viewY, 0.0f);
            float[] far = unprojectViewToWorld(camera, viewX, viewY, 1.0f);
            float dirX = far[0] - origin[0];
            float dirY = far[1] - origin[1];
            float dirZ = far[2] - origin[2];
            float length = (float) Math.sqrt(dirX * dirX + dirY * dirY + dirZ * dirZ);
            if (length == 0.0f) {
                return new Ray(origin[0], origin[1], origin[2], 0.0f, 0.0f, 0.0f);
            }
            return new Ray(origin[0], origin[1], origin[2], dirX / length, dirY / length, dirZ / length);
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
    }
}
