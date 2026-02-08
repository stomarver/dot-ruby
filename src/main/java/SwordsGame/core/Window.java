package SwordsGame.core;

import org.lwjgl.glfw.*;
import org.lwjgl.opengl.GL;

import java.nio.ByteBuffer;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Window {

    private long windowHandle;

    // FBO
    private int fboId;
    private int textureId;
    private int depthId;

    // virtual resolution
    private static final int VIRTUAL_WIDTH = 960;
    private static final int VIRTUAL_HEIGHT = 540;

    // real framebuffer letterbox viewport
    private int physicalX, physicalY;
    private int physicalWidth, physicalHeight;

    private boolean fullscreen = false;

    // scroll input
    private double scrollX = 0;
    private double scrollY = 0;

    // virtual mouse position
    private float virtualMouseX = VIRTUAL_WIDTH / 2f;
    private float virtualMouseY = VIRTUAL_HEIGHT / 2f;

    // delta mouse
    private double lastMouseX = 0;
    private double lastMouseY = 0;
    private boolean mouseInitialized = false;

    // sensitivity settings
    private float windowedSensitivity = 1.0f;
    private float fullscreenSensitivity = 0.6f;

    // current sensitivity (auto switches)
    private float mouseSensitivity = windowedSensitivity;

    public Window(String title) {
        // title пока не используем
    }

    public long getHandle() {
        return windowHandle;
    }

    public int getVirtualWidth() {
        return VIRTUAL_WIDTH;
    }

    public int getVirtualHeight() {
        return VIRTUAL_HEIGHT;
    }

    public float getMouseRelX() {
        return virtualMouseX;
    }

    public float getMouseRelY() {
        return virtualMouseY;
    }

    public double getScrollX() {
        return scrollX;
    }

    public double getScrollY() {
        return scrollY;
    }

    public void resetScroll() {
        scrollX = 0;
        scrollY = 0;
    }

    // ===========================================
    // SENSITIVITY API
    // ===========================================

    public void setWindowedSensitivity(float value) {
        windowedSensitivity = value;
        updateSensitivityMode();
    }

    public void setFullscreenSensitivity(float value) {
        fullscreenSensitivity = value;
        updateSensitivityMode();
    }

    private void updateSensitivityMode() {
        if (fullscreen) {
            mouseSensitivity = fullscreenSensitivity;
        } else {
            mouseSensitivity = windowedSensitivity;
        }

        System.out.println("[Input] Mouse sensitivity = " + mouseSensitivity);
    }

    // ===========================================
    // CREATE WINDOW
    // ===========================================

    public void create() {
        if (!glfwInit()) {
            throw new IllegalStateException("GLFW init failed");
        }

        System.out.println("[Sys] GLFW initialized");

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);

        // OpenGL 2.1 (как у тебя)
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 2);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 1);

        windowHandle = glfwCreateWindow(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, "SwordsGame", NULL, NULL);
        if (windowHandle == NULL) {
            throw new RuntimeException("Failed to create GLFW window");
        }

        glfwMakeContextCurrent(windowHandle);
        GL.createCapabilities();

        glfwSwapInterval(1);
        System.out.println("[Vid] VSync enabled");

        setupCallbacks();

        initFBO();
        updateViewport();

        updateSensitivityMode();
        captureCursor();

        glfwShowWindow(windowHandle);

        System.out.println("[Vid] Window created");
    }

    // ===========================================
    // CALLBACKS
    // ===========================================

    private void setupCallbacks() {

        glfwSetKeyCallback(windowHandle, (window, key, scancode, action, mods) -> {
            if (action != GLFW_PRESS) return;

            boolean altEnter = (key == GLFW_KEY_ENTER && (mods & GLFW_MOD_ALT) != 0);
            boolean ctrlP = (key == GLFW_KEY_P && (mods & GLFW_MOD_CONTROL) != 0);

            if (key == GLFW_KEY_ESCAPE) {
                glfwSetWindowShouldClose(window, true);
            }

            if (key == GLFW_KEY_F4 || altEnter) {
                toggleFullscreen();
            }

            if (key == GLFW_KEY_F12 || ctrlP) {
                SwordsGame.utils.Screenshot.takeScreenshot(fboId, VIRTUAL_WIDTH, VIRTUAL_HEIGHT);
            }
        });

        glfwSetScrollCallback(windowHandle, (window, xoffset, yoffset) -> {
            scrollX += xoffset;
            scrollY += yoffset;
        });

        glfwSetFramebufferSizeCallback(windowHandle, (window, width, height) -> {
            updateViewport();
        });

        glfwSetWindowFocusCallback(windowHandle, (window, focused) -> {
            if (focused) {
                captureCursor();
            } else {
                mouseInitialized = false;
            }
        });
    }

    // ===========================================
    // VIEWPORT (LETTERBOX)
    // ===========================================

    private void updateViewport() {
        int[] fbW = new int[1];
        int[] fbH = new int[1];
        glfwGetFramebufferSize(windowHandle, fbW, fbH);

        int framebufferW = fbW[0];
        int framebufferH = fbH[0];

        float targetAspect = (float) VIRTUAL_WIDTH / (float) VIRTUAL_HEIGHT;
        float windowAspect = (float) framebufferW / (float) framebufferH;

        if (windowAspect > targetAspect) {
            physicalHeight = framebufferH;
            physicalWidth = (int) (framebufferH * targetAspect);
            physicalX = (framebufferW - physicalWidth) / 2;
            physicalY = 0;
        } else {
            physicalWidth = framebufferW;
            physicalHeight = (int) (framebufferW / targetAspect);
            physicalX = 0;
            physicalY = (framebufferH - physicalHeight) / 2;
        }

        System.out.println("[Vid] Viewport updated: " + physicalWidth + "x" + physicalHeight);
    }

    // ===========================================
    // CURSOR / MOUSE
    // ===========================================

    private void captureCursor() {
        glfwSetInputMode(windowHandle, GLFW_CURSOR, GLFW_CURSOR_DISABLED);

        if (glfwRawMouseMotionSupported()) {
            glfwSetInputMode(windowHandle, GLFW_RAW_MOUSE_MOTION, GLFW_TRUE);
            System.out.println("[Input] Raw mouse motion enabled");
        }

        mouseInitialized = false;
    }

    private void updateMouse() {
        double[] x = new double[1];
        double[] y = new double[1];

        glfwGetCursorPos(windowHandle, x, y);

        if (!mouseInitialized) {
            lastMouseX = x[0];
            lastMouseY = y[0];
            mouseInitialized = true;
            return;
        }

        double dx = x[0] - lastMouseX;
        double dy = y[0] - lastMouseY;

        lastMouseX = x[0];
        lastMouseY = y[0];

        virtualMouseX += (float) (dx * mouseSensitivity);
        virtualMouseY += (float) (dy * mouseSensitivity);

        // clamp to screen
        if (virtualMouseX < 0) virtualMouseX = 0;
        if (virtualMouseY < 0) virtualMouseY = 0;

        if (virtualMouseX > VIRTUAL_WIDTH - 1) virtualMouseX = VIRTUAL_WIDTH - 1;
        if (virtualMouseY > VIRTUAL_HEIGHT - 1) virtualMouseY = VIRTUAL_HEIGHT - 1;
    }

    // ===========================================
    // FULLSCREEN
    // ===========================================

    public void toggleFullscreen() {
        fullscreen = !fullscreen;

        long monitor = glfwGetPrimaryMonitor();
        GLFWVidMode mode = glfwGetVideoMode(monitor);
        if (mode == null) return;

        if (fullscreen) {
            glfwSetWindowMonitor(
                    windowHandle,
                    monitor,
                    0, 0,
                    mode.width(),
                    mode.height(),
                    mode.refreshRate()
            );
        } else {
            glfwSetWindowMonitor(
                    windowHandle,
                    NULL,
                    (mode.width() - VIRTUAL_WIDTH) / 2,
                    (mode.height() - VIRTUAL_HEIGHT) / 2,
                    VIRTUAL_WIDTH,
                    VIRTUAL_HEIGHT,
                    0
            );
        }

        updateViewport();
        updateSensitivityMode();
        captureCursor();
    }

    // ===========================================
    // FBO
    // ===========================================

    private void initFBO() {
        fboId = glGenFramebuffers();
        glBindFramebuffer(GL_FRAMEBUFFER, fboId);

        textureId = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, textureId);

        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB,
                VIRTUAL_WIDTH, VIRTUAL_HEIGHT,
                0, GL_RGB, GL_UNSIGNED_BYTE,
                (ByteBuffer) null);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, textureId, 0);

        depthId = glGenRenderbuffers();
        glBindRenderbuffer(GL_RENDERBUFFER, depthId);
        glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH_COMPONENT24, VIRTUAL_WIDTH, VIRTUAL_HEIGHT);
        glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, depthId);

        int status = glCheckFramebufferStatus(GL_FRAMEBUFFER);
        if (status != GL_FRAMEBUFFER_COMPLETE) {
            throw new RuntimeException("FBO incomplete: status = " + status);
        }

        glBindFramebuffer(GL_FRAMEBUFFER, 0);

        System.out.println("[Vid] FBO created OK");
    }

    // ===========================================
    // RENDER PIPELINE
    // ===========================================

    public void beginRenderToFBO() {
        glBindFramebuffer(GL_FRAMEBUFFER, fboId);
        glViewport(0, 0, VIRTUAL_WIDTH, VIRTUAL_HEIGHT);

        glClearColor(0.15f, 0.15f, 0.15f, 1f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    }

    public void endRenderToFBO() {
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }

    public void drawFBO() {
        int[] fbW = new int[1];
        int[] fbH = new int[1];
        glfwGetFramebufferSize(windowHandle, fbW, fbH);

        glViewport(0, 0, fbW[0], fbH[0]);

        glClearColor(0, 0, 0, 1);
        glClear(GL_COLOR_BUFFER_BIT);

        glViewport(physicalX, physicalY, physicalWidth, physicalHeight);

        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        glOrtho(0, 1, 0, 1, -1, 1);

        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();

        glEnable(GL_TEXTURE_2D);
        glBindTexture(GL_TEXTURE_2D, textureId);

        glColor4f(1, 1, 1, 1);

        glBegin(GL_QUADS);
        glTexCoord2f(0, 0); glVertex2f(0, 0);
        glTexCoord2f(1, 0); glVertex2f(1, 0);
        glTexCoord2f(1, 1); glVertex2f(1, 1);
        glTexCoord2f(0, 1); glVertex2f(0, 1);
        glEnd();

        glBindTexture(GL_TEXTURE_2D, 0);
        glDisable(GL_TEXTURE_2D);
    }

    // ===========================================
    // MAIN LOOP STEP
    // ===========================================

    public void update() {
        updateMouse();
        glfwSwapBuffers(windowHandle);
        glfwPollEvents();
    }

    public boolean shouldClose() {
        return glfwWindowShouldClose(windowHandle);
    }

    // ===========================================
    // CLEANUP
    // ===========================================

    public void destroy() {

        if (depthId != 0) glDeleteRenderbuffers(depthId);
        if (textureId != 0) glDeleteTextures(textureId);
        if (fboId != 0) glDeleteFramebuffers(fboId);

        glfwDestroyWindow(windowHandle);
        glfwTerminate();

        System.out.println("[Sys] Window destroyed");
    }
}
