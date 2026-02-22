package SwordsGame.client;

import SwordsGame.client.core.Window;
import SwordsGame.server.ChunkManager;
import SwordsGame.client.graphics.Renderer;
import org.joml.Vector2f;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

public class Camera {
    private static final float ORTHO_WIDTH = 720.0f;
    private static final float ORTHO_HEIGHT = 540.0f;

    private final Vector2f position = new Vector2f(0.0f, 0.0f);
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

    public float getX() { return position.x; }
    public float getZ() { return position.y; }
    public float getZoom() { return zoom; }
    public float getRotation() { return currentRotationY; }
    public float getPitch() { return PITCH; }
    public float getOrthoWidth() { return ORTHO_WIDTH; }
    public float getOrthoHeight() { return ORTHO_HEIGHT; }


    public void update(Window window, ChunkManager chunkManager, Renderer renderer, boolean blockVerticalEdgeScroll) {
        long windowHandle = window.getHandle();

        Vector2f forward = forwardFromAngle(currentRotationY);
        Vector2f right = rightFromAngle(currentRotationY);

        boolean isShiftPressed = glfwGetKey(windowHandle, GLFW_KEY_LEFT_SHIFT) == GLFW_PRESS ||
                glfwGetKey(windowHandle, GLFW_KEY_RIGHT_SHIFT) == GLFW_PRESS;
        float currentMoveSpeed = isShiftPressed ? speed * 3.0f : speed;

        if (glfwGetKey(windowHandle, GLFW_KEY_R) == GLFW_PRESS) {
            position.zero();
            targetRotationY = 45.0f;
            currentRotationY = 45.0f;
            zoom = 0.5f;
        }

        if (glfwGetKey(windowHandle, GLFW_KEY_UP) == GLFW_PRESS) {
            position.fma(currentMoveSpeed, forward);
        }
        if (glfwGetKey(windowHandle, GLFW_KEY_DOWN) == GLFW_PRESS) {
            position.fma(-currentMoveSpeed, forward);
        }
        if (glfwGetKey(windowHandle, GLFW_KEY_LEFT) == GLFW_PRESS) {
            position.fma(currentMoveSpeed, right);
        }
        if (glfwGetKey(windowHandle, GLFW_KEY_RIGHT) == GLFW_PRESS) {
            position.fma(-currentMoveSpeed, right);
        }

        float mouseX = window.getMouseRelX();
        float mouseY = window.getMouseRelY();

        int virtualWidth = window.getVirtualWidth();
        int virtualHeight = window.getVirtualHeight();

        if (mouseX < EDGE_SCROLL_ZONE) {
            position.fma(EDGE_SCROLL_SPEED, right);
        } else if (mouseX > virtualWidth - EDGE_SCROLL_ZONE) {
            position.fma(-EDGE_SCROLL_SPEED, right);
        }

        if (!blockVerticalEdgeScroll) {
            if (mouseY < EDGE_SCROLL_ZONE) {
                position.fma(EDGE_SCROLL_SPEED, forward);
            } else if (mouseY > virtualHeight - EDGE_SCROLL_ZONE) {
                position.fma(-EDGE_SCROLL_SPEED, forward);
            }
        }

        if (glfwGetKey(windowHandle, GLFW_KEY_EQUAL) == GLFW_PRESS) zoom += zoomSpeed;
        if (glfwGetKey(windowHandle, GLFW_KEY_MINUS) == GLFW_PRESS) zoom -= zoomSpeed;
        zoom = clamp(zoom, MIN_ZOOM, MAX_ZOOM);

        double currentTime = glfwGetTime();
        float currentStep = isShiftPressed ? 45.0f : 15.0f;
        double currentDelay = isShiftPressed ? (baseRotationDelay * 2) : baseRotationDelay;

        if (currentTime - lastRotationTime >= currentDelay) {
            if (glfwGetKey(windowHandle, GLFW_KEY_E) == GLFW_PRESS) {
                targetRotationY -= currentStep;
                lastRotationTime = currentTime;
            }
            if (glfwGetKey(windowHandle, GLFW_KEY_Q) == GLFW_PRESS) {
                targetRotationY += currentStep;
                lastRotationTime = currentTime;
            }
        }

        double scrollY = window.getScrollY();
        if (scrollY != 0) {
            targetRotationY += (float) scrollY * (isShiftPressed ? 45.0f : 15.0f);
            window.resetScroll();
        }

        currentRotationY += (targetRotationY - currentRotationY) * lerpSpeed;
        clampPosition(chunkManager, renderer);
    }


    public boolean isInVerticalEdgeZone(float mouseY, int virtualHeight) {
        return mouseY < EDGE_SCROLL_ZONE || mouseY > virtualHeight - EDGE_SCROLL_ZONE;
    }

    public void applyTransformations() {
        glScalef(zoom, zoom, zoom);
        glRotatef(PITCH, 1, 0, 0);
        glRotatef(currentRotationY, 0, 1, 0);
        glTranslatef(position.x, 0, position.y);
    }

    private void clampPosition(ChunkManager chunkManager, Renderer renderer) {
        float blockSizeUnits = World.BLOCK_SIZE * 2.0f;
        float halfWorld = (chunkManager.getWorldSizeInBlocks() / 2.0f) * blockSizeUnits;
        float viewportHalfWidth = renderer.getViewportWidth() / 2.0f;
        float viewportHalfHeight = renderer.getViewportHeight() / 2.0f;
        float zoomFactor = Math.max(MIN_ZOOM, zoom);
        float margin = Math.max(viewportHalfWidth, viewportHalfHeight) / zoomFactor;

        position.x = clamp(position.x, -halfWorld + margin, halfWorld - margin);
        position.y = clamp(position.y, -halfWorld + margin, halfWorld - margin);
    }

    private Vector2f forwardFromAngle(float angleDegrees) {
        float angleRad = (float) Math.toRadians(angleDegrees);
        return new Vector2f(-(float) Math.sin(angleRad), (float) Math.cos(angleRad));
    }

    private Vector2f rightFromAngle(float angleDegrees) {
        float angleRad = (float) Math.toRadians(angleDegrees);
        return new Vector2f((float) Math.cos(angleRad), (float) Math.sin(angleRad));
    }

    private float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
}
