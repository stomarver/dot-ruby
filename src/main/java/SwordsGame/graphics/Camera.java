package SwordsGame.graphics;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import SwordsGame.core.Window;
import SwordsGame.server.ChunkManager;
import SwordsGame.server.Chunk;

public class Camera {
    private float x = 0, z = 0;
    private float zoom = 0.5f;
    private float targetRotationY = 45.0f;
    private float currentRotationY = 45.0f;
    private final float lerpSpeed = 0.1f;
    private final float speed = 8.0f;
    private final float zoomSpeed = 0.02f;
    private double lastRotationTime = 0;
    private final double baseRotationDelay = 0.1;

    // Настройки edge scrolling
    private static final float EDGE_SCROLL_ZONE = 30.0f; // Пикселей от края
    private static final float EDGE_SCROLL_SPEED = 15.0f; // Скорость прокрутки (было 6.0f, стало 15.0f)

    public float getX() { return x; }
    public float getZ() { return z; }
    public float getZoom() { return zoom; }
    public float getRotation() { return currentRotationY; }

    public void update(Window window) {
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

        // Управление клавишами
        if (glfwGetKey(windowHandle, GLFW_KEY_UP) == GLFW_PRESS)    { x -= currentMoveSpeed * sin; z += currentMoveSpeed * cos; }
        if (glfwGetKey(windowHandle, GLFW_KEY_DOWN) == GLFW_PRESS)  { x += currentMoveSpeed * sin; z -= currentMoveSpeed * cos; }
        if (glfwGetKey(windowHandle, GLFW_KEY_LEFT) == GLFW_PRESS)  { x += currentMoveSpeed * cos; z += currentMoveSpeed * sin; }
        if (glfwGetKey(windowHandle, GLFW_KEY_RIGHT) == GLFW_PRESS) { x -= currentMoveSpeed * cos; z -= currentMoveSpeed * sin; }

        // Edge scrolling (прокрутка мышью у краёв экрана)
        float mouseX = window.getMouseRelX(windowHandle);
        float mouseY = window.getMouseRelY(windowHandle);
        int virtualWidth = window.getVirtualWidth();
        int virtualHeight = window.getVirtualHeight();

        // Проверяем, находится ли курсор в зоне прокрутки
        if (mouseX < EDGE_SCROLL_ZONE) {
            // Левый край
            x += EDGE_SCROLL_SPEED * cos;
            z += EDGE_SCROLL_SPEED * sin;
        } else if (mouseX > virtualWidth - EDGE_SCROLL_ZONE) {
            // Правый край
            x -= EDGE_SCROLL_SPEED * cos;
            z -= EDGE_SCROLL_SPEED * sin;
        }

        if (mouseY < EDGE_SCROLL_ZONE) {
            // Верхний край
            x -= EDGE_SCROLL_SPEED * sin;
            z += EDGE_SCROLL_SPEED * cos;
        } else if (mouseY > virtualHeight - EDGE_SCROLL_ZONE) {
            // Нижний край
            x += EDGE_SCROLL_SPEED * sin;
            z -= EDGE_SCROLL_SPEED * cos;
        }

        if (glfwGetKey(windowHandle, GLFW_KEY_EQUAL) == GLFW_PRESS) zoom += zoomSpeed;
        if (glfwGetKey(windowHandle, GLFW_KEY_MINUS) == GLFW_PRESS) zoom -= zoomSpeed;
        if (zoom < 0.25f) zoom = 0.25f;
        if (zoom > 2.5f) zoom = 2.5f;

        double currentTime = glfwGetTime();
        float currentStep = isShiftPressed ? 45.0f : 15.0f;
        double currentDelay = isShiftPressed ? (baseRotationDelay * 2) : baseRotationDelay;

        // Обработка вращения клавишами Q/E
        if (currentTime - lastRotationTime >= currentDelay) {
            if (glfwGetKey(windowHandle, GLFW_KEY_E) == GLFW_PRESS) { targetRotationY -= currentStep; lastRotationTime = currentTime; }
            if (glfwGetKey(windowHandle, GLFW_KEY_Q) == GLFW_PRESS) { targetRotationY += currentStep; lastRotationTime = currentTime; }
        }

        // Обработка вращения колесом мыши
        double scrollY = window.getScrollY();
        if (scrollY != 0) {
            targetRotationY += (float) scrollY * (isShiftPressed ? 45.0f : 15.0f);
            window.resetScroll();
        }

        currentRotationY += (targetRotationY - currentRotationY) * lerpSpeed;
    }

    public int[] getTargetBlockFromMouse(Window window, int worldSizeInChunks, ChunkManager cm) {
        // Координаты от центра 3D-вьюпорта
        float mouseX = (float) window.getMouseRelX(window.getHandle()) - 120 - 360;
        float mouseY = 270 - (float) window.getMouseRelY(window.getHandle());

        float worldX_Iso = mouseX / zoom;
        float worldY_Iso = mouseY / zoom;

        float angleRad = (float) Math.toRadians(-currentRotationY);
        float cos = (float) Math.cos(angleRad);
        float sin = (float) Math.sin(angleRad);

        float blockSizeUnits = 12.5f * 2.0f;
        float totalOffsetBlocks = (worldSizeInChunks * 16) / 2.0f;

        float cos35 = 0.8165f;
        float sin35 = 0.5773f;

        for (int y = Chunk.HEIGHT - 1; y >= 0; y--) {
            float yPos = (y + 1.0f) * blockSizeUnits;
            float rotZ = (yPos * cos35 - worldY_Iso) / sin35;
            float rotX = worldX_Iso;

            float realX = rotX * cos + rotZ * sin;
            float realZ = rotZ * cos - rotX * sin;

            float worldX = realX - x;
            float worldZ = realZ - z;

            int blockX = (int) Math.floor((worldX / blockSizeUnits) + totalOffsetBlocks);
            int blockZ = (int) Math.floor((worldZ / blockSizeUnits) + totalOffsetBlocks);

            if (getBlockAt(cm, blockX, y, blockZ, worldSizeInChunks) != 0) {
                return new int[]{blockX, y, blockZ};
            }
        }
        return null;
    }

    private byte getBlockAt(ChunkManager cm, int wx, int wy, int wz, int worldSizeInChunks) {
        int limit = worldSizeInChunks * 16;
        if (wx < 0 || wx >= limit || wz < 0 || wz >= limit || wy < 0 || wy >= 32) return 0;
        return cm.getChunks()[wx / 16][wz / 16].getBlock(wx % 16, wy, wz % 16);
    }

    public void applyTransformations() {
        glScalef(zoom, zoom, zoom);
        glRotatef(35.264f, 1, 0, 0);
        glRotatef(currentRotationY, 0, 1, 0);
        glTranslatef(x, 0, z);
    }
}