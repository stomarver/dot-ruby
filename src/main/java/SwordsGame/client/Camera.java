package SwordsGame.client;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import SwordsGame.client.core.Window;
import SwordsGame.server.ChunkManager;
import SwordsGame.server.Chunk;
import SwordsGame.client.World;

public class Camera {
    private static final float ORTHO_WIDTH = 720.0f;
    private static final float ORTHO_HEIGHT = 540.0f;
    private float x = 0, z = 0;
    private float zoom = 0.5f;
    private float targetRotationY = 45.0f;
    private float currentRotationY = 45.0f;
    private final float lerpSpeed = 0.1f;
    private final float speed = 8.0f;
    private final float zoomSpeed = 0.02f;
    private double lastRotationTime = 0;
    private final double baseRotationDelay = 0.1;

    private static final float EDGE_SCROLL_ZONE = 30.0f;
    private static final float EDGE_SCROLL_SPEED = 15.0f;
    private static final float MIN_ZOOM = 0.25f;
    private static final float MAX_ZOOM = 2.5f;
    private static final float PITCH = 35.264f;

    public float getX() { return x; }
    public float getZ() { return z; }
    public float getZoom() { return zoom; }
    public float getRotation() { return currentRotationY; }

    public void update(Window window, ChunkManager chunkManager, Renderer renderer) {
        long windowHandle = window.getHandle();

        float angleRad = (float) Math.toRadians(currentRotationY);
        float sin = (float) Math.sin(angleRad);
        float cos = (float) Math.cos(angleRad);
        boolean isShiftPressed = glfwGetKey(windowHandle, GLFW_KEY_LEFT_SHIFT) == GLFW_PRESS ||
                glfwGetKey(windowHandle, GLFW_KEY_RIGHT_SHIFT) == GLFW_PRESS;
        float currentMoveSpeed = isShiftPressed ? speed * 3.0f : speed;

        if (glfwGetKey(windowHandle, GLFW_KEY_R) == GLFW_PRESS) {
            x = 0; z = 0; targetRotationY = 45.0f; currentRotationY = 45.0f; zoom = 0.5f;
        }

        if (glfwGetKey(windowHandle, GLFW_KEY_UP) == GLFW_PRESS)    { x -= currentMoveSpeed * sin; z += currentMoveSpeed * cos; }
        if (glfwGetKey(windowHandle, GLFW_KEY_DOWN) == GLFW_PRESS)  { x += currentMoveSpeed * sin; z -= currentMoveSpeed * cos; }
        if (glfwGetKey(windowHandle, GLFW_KEY_LEFT) == GLFW_PRESS)  { x += currentMoveSpeed * cos; z += currentMoveSpeed * sin; }
        if (glfwGetKey(windowHandle, GLFW_KEY_RIGHT) == GLFW_PRESS) { x -= currentMoveSpeed * cos; z -= currentMoveSpeed * sin; }

        float mouseX = window.getMouseRelX();
        float mouseY = window.getMouseRelY();

        int virtualWidth = window.getVirtualWidth();
        int virtualHeight = window.getVirtualHeight();

        if (mouseX < EDGE_SCROLL_ZONE) {
            x += EDGE_SCROLL_SPEED * cos;
            z += EDGE_SCROLL_SPEED * sin;
        } else if (mouseX > virtualWidth - EDGE_SCROLL_ZONE) {
            x -= EDGE_SCROLL_SPEED * cos;
            z -= EDGE_SCROLL_SPEED * sin;
        }

        if (mouseY < EDGE_SCROLL_ZONE) {
            x -= EDGE_SCROLL_SPEED * sin;
            z += EDGE_SCROLL_SPEED * cos;
        } else if (mouseY > virtualHeight - EDGE_SCROLL_ZONE) {
            x += EDGE_SCROLL_SPEED * sin;
            z -= EDGE_SCROLL_SPEED * cos;
        }

        if (glfwGetKey(windowHandle, GLFW_KEY_EQUAL) == GLFW_PRESS) zoom += zoomSpeed;
        if (glfwGetKey(windowHandle, GLFW_KEY_MINUS) == GLFW_PRESS) zoom -= zoomSpeed;
        zoom = clamp(zoom, MIN_ZOOM, MAX_ZOOM);

        double currentTime = glfwGetTime();
        float currentStep = isShiftPressed ? 45.0f : 15.0f;
        double currentDelay = isShiftPressed ? (baseRotationDelay * 2) : baseRotationDelay;

        if (currentTime - lastRotationTime >= currentDelay) {
            if (glfwGetKey(windowHandle, GLFW_KEY_E) == GLFW_PRESS) { targetRotationY -= currentStep; lastRotationTime = currentTime; }
            if (glfwGetKey(windowHandle, GLFW_KEY_Q) == GLFW_PRESS) { targetRotationY += currentStep; lastRotationTime = currentTime; }
        }

        double scrollY = window.getScrollY();
        if (scrollY != 0) {
            targetRotationY += (float) scrollY * (isShiftPressed ? 45.0f : 15.0f);
            window.resetScroll();
        }

        currentRotationY += (targetRotationY - currentRotationY) * lerpSpeed;
        clampPosition(chunkManager, renderer);
    }

    public int[] getTargetBlockFromMouse(Window window, int worldSizeInChunks, ChunkManager cm, Renderer renderer) {
        float centerX = renderer.getViewportX() + renderer.getViewportWidth() / 2.0f;
        float centerY = renderer.getViewportY() + renderer.getViewportHeight() / 2.0f;
        float mouseX = (float) window.getMouseRelX() - centerX;
        float mouseY = centerY - (float) window.getMouseRelY();

        float viewX = (mouseX / renderer.getViewportWidth()) * ORTHO_WIDTH;
        float viewY = (mouseY / renderer.getViewportHeight()) * ORTHO_HEIGHT;

        float[] rayOrigin = unprojectViewToWorld(viewX, viewY, 0.0f);
        float[] rayFar = unprojectViewToWorld(viewX, viewY, 1.0f);
        float[] rayDir = normalize(rayFar[0] - rayOrigin[0], rayFar[1] - rayOrigin[1], rayFar[2] - rayOrigin[2]);

        return raycastTopFace(cm, rayOrigin, rayDir);
    }

    public void applyTransformations() {
        glScalef(zoom, zoom, zoom);
        glRotatef(PITCH, 1, 0, 0);
        glRotatef(currentRotationY, 0, 1, 0);
        glTranslatef(x, 0, z);
    }

    private void clampPosition(ChunkManager chunkManager, Renderer renderer) {
        float blockSizeUnits = World.BLOCK_SIZE * 2.0f;
        float halfWorld = (chunkManager.getWorldSizeInBlocks() / 2.0f) * blockSizeUnits;
        float viewportHalfWidth = renderer.getViewportWidth() / 2.0f;
        float viewportHalfHeight = renderer.getViewportHeight() / 2.0f;
        float zoomFactor = Math.max(MIN_ZOOM, zoom);
        float margin = Math.max(viewportHalfWidth, viewportHalfHeight) / zoomFactor;

        x = clamp(x, -halfWorld + margin, halfWorld - margin);
        z = clamp(z, -halfWorld + margin, halfWorld - margin);
    }

    private float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    private float[] unprojectViewToWorld(float viewX, float viewY, float viewZ) {
        float scaledX = viewX / zoom;
        float scaledY = viewY / zoom;
        float scaledZ = viewZ / zoom;

        float[] afterPitch = rotateX(scaledX, scaledY, scaledZ, -PITCH);
        float[] afterYaw = rotateY(afterPitch[0], afterPitch[1], afterPitch[2], -currentRotationY);

        afterYaw[0] -= x;
        afterYaw[2] -= z;

        return afterYaw;
    }

    private float[] normalize(float x, float y, float z) {
        float length = (float) Math.sqrt(x * x + y * y + z * z);
        if (length == 0) return new float[]{0.0f, 0.0f, 0.0f};
        return new float[]{x / length, y / length, z / length};
    }

    private float[] rotateX(float x, float y, float z, float degrees) {
        float rad = (float) Math.toRadians(degrees);
        float cos = (float) Math.cos(rad);
        float sin = (float) Math.sin(rad);
        return new float[]{x, y * cos - z * sin, y * sin + z * cos};
    }

    private float[] rotateY(float x, float y, float z, float degrees) {
        float rad = (float) Math.toRadians(degrees);
        float cos = (float) Math.cos(rad);
        float sin = (float) Math.sin(rad);
        return new float[]{x * cos + z * sin, y, -x * sin + z * cos};
    }

    private int[] raycastTopFace(ChunkManager cm, float[] origin, float[] direction) {
        if (direction[1] >= 0.0f) {
            return null;
        }
        float blockScale = World.BLOCK_SCALE;
        float totalOffsetBlocks = cm.getWorldSizeInBlocks() / 2.0f;
        float dirY = direction[1];
        float dirX = direction[0];
        float dirZ = direction[2];

        for (int y = Chunk.HEIGHT - 1; y >= 0; y--) {
            float planeY = (y + 1) * blockScale;
            float t = (planeY - origin[1]) / dirY;
            if (t < 0) {
                continue;
            }
            float hitX = origin[0] + (dirX * t);
            float hitZ = origin[2] + (dirZ * t);
            int bx = (int) Math.floor((hitX / blockScale) + totalOffsetBlocks);
            int bz = (int) Math.floor((hitZ / blockScale) + totalOffsetBlocks);
            if (!cm.isInsideWorld(bx, bz)) {
                continue;
            }
            if (cm.isTopSurface(bx, y, bz)) {
                return new int[]{bx, y, bz};
            }
        }

        return null;
    }
}
