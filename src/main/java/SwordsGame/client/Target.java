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

        int worldSizeBlocks = chunkManager.getWorldSizeInBlocks();
        double maxDistance = worldSizeBlocks * MAX_RAYCAST_MULTIPLIER;

        VoxelTraversal traversal = new VoxelTraversal(ox, oy, oz, dx, dy, dz);
        int faceX = 0;
        int faceY = 0;
        int faceZ = 0;

        while (traversal.distanceTravelled <= maxDistance) {
            if (isWithinWorld(traversal.x, traversal.y, traversal.z, worldSizeBlocks)) {
                if (predicate.hit(chunkManager, traversal.x, traversal.y, traversal.z)) {
                    return new Target(traversal.x, traversal.y, traversal.z, faceX, faceY, faceZ, true);
                }
            }

            AxisStep step = traversal.step();
            faceX = step.faceX;
            faceY = step.faceY;
            faceZ = step.faceZ;
        }

        return new Target(0, 0, 0, 0, 0, 0, false);
    }

    private static boolean isWithinWorld(int x, int y, int z, int worldSizeBlocks) {
        return x >= 0 && z >= 0 && x < worldSizeBlocks && z < worldSizeBlocks && y >= 0 && y < Chunk.HEIGHT;
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
            float[] origin = viewToWorld(camera, viewX, viewY);
            float[] forward = cameraForward(camera);
            return new Ray(origin[0], origin[1], origin[2], forward[0], forward[1], forward[2]);
        }

        private static float[] viewToWorld(Camera camera, float viewX, float viewY) {
            float scaledX = viewX / camera.getZoom();
            float scaledY = viewY / camera.getZoom();

            float[] afterPitch = rotateX(scaledX, scaledY, 0.0f, -camera.getPitch());
            float[] afterYaw = rotateY(afterPitch[0], afterPitch[1], afterPitch[2], -camera.getRotation());

            afterYaw[0] -= camera.getX();
            afterYaw[2] -= camera.getZ();

            return afterYaw;
        }

        private static float[] cameraForward(Camera camera) {
            float[] forward = rotateX(0.0f, 0.0f, -1.0f, -camera.getPitch());
            forward = rotateY(forward[0], forward[1], forward[2], -camera.getRotation());
            float length = (float) Math.sqrt(forward[0] * forward[0] + forward[1] * forward[1] + forward[2] * forward[2]);
            if (length == 0.0f) {
                return new float[]{0.0f, -1.0f, 0.0f};
            }
            return new float[]{forward[0] / length, forward[1] / length, forward[2] / length};
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

    private static class VoxelTraversal {
        private int x;
        private int y;
        private int z;
        private final int stepX;
        private final int stepY;
        private final int stepZ;
        private double tMaxX;
        private double tMaxY;
        private double tMaxZ;
        private final double tDeltaX;
        private final double tDeltaY;
        private final double tDeltaZ;
        private double distanceTravelled;

        private VoxelTraversal(double ox, double oy, double oz, double dx, double dy, double dz) {
            this.x = (int) Math.floor(ox);
            this.y = (int) Math.floor(oy);
            this.z = (int) Math.floor(oz);

            this.stepX = dx > 0 ? 1 : (dx < 0 ? -1 : 0);
            this.stepY = dy > 0 ? 1 : (dy < 0 ? -1 : 0);
            this.stepZ = dz > 0 ? 1 : (dz < 0 ? -1 : 0);

            this.tMaxX = intBound(ox, dx);
            this.tMaxY = intBound(oy, dy);
            this.tMaxZ = intBound(oz, dz);

            this.tDeltaX = dx == 0 ? Double.POSITIVE_INFINITY : Math.abs(1.0 / dx);
            this.tDeltaY = dy == 0 ? Double.POSITIVE_INFINITY : Math.abs(1.0 / dy);
            this.tDeltaZ = dz == 0 ? Double.POSITIVE_INFINITY : Math.abs(1.0 / dz);
            this.distanceTravelled = 0.0;
        }

        private AxisStep step() {
            if (tMaxX < tMaxY) {
                if (tMaxX < tMaxZ) {
                    x += stepX;
                    distanceTravelled = tMaxX;
                    tMaxX += tDeltaX;
                    return AxisStep.fromFace(-stepX, 0, 0);
                }
                z += stepZ;
                distanceTravelled = tMaxZ;
                tMaxZ += tDeltaZ;
                return AxisStep.fromFace(0, 0, -stepZ);
            }
            if (tMaxY < tMaxZ) {
                y += stepY;
                distanceTravelled = tMaxY;
                tMaxY += tDeltaY;
                return AxisStep.fromFace(0, -stepY, 0);
            }
            z += stepZ;
            distanceTravelled = tMaxZ;
            tMaxZ += tDeltaZ;
            return AxisStep.fromFace(0, 0, -stepZ);
        }

        private static double intBound(double s, double ds) {
            if (ds == 0) return Double.POSITIVE_INFINITY;
            double sIsInteger = Math.floor(s);
            if (ds > 0) {
                return (sIsInteger + 1 - s) / ds;
            }
            return (s - sIsInteger) / -ds;
        }
    }

    private static class AxisStep {
        private final int faceX;
        private final int faceY;
        private final int faceZ;

        private AxisStep(int faceX, int faceY, int faceZ) {
            this.faceX = faceX;
            this.faceY = faceY;
            this.faceZ = faceZ;
        }

        private static AxisStep fromFace(int faceX, int faceY, int faceZ) {
            return new AxisStep(faceX, faceY, faceZ);
        }
    }
}
