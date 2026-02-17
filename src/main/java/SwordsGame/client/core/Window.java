package SwordsGame.client.core;

import org.lwjgl.glfw.*;
import org.lwjgl.opengl.GL;
import org.lwjgl.BufferUtils;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Window {

    private final String title;
    private long windowHandle;


    private int fboId;
    private int textureId;
    private int depthId;
    private int screenVaoId;
    private int screenVboId;
    private boolean useVao;


    private static final int VIRTUAL_WIDTH = 960;
    private static final int VIRTUAL_HEIGHT = 540;


    private int physicalX, physicalY;
    private int physicalWidth, physicalHeight;
    private int framebufferWidth = VIRTUAL_WIDTH;
    private int framebufferHeight = VIRTUAL_HEIGHT;

    private boolean fullscreen = false;


    private double scrollX = 0;
    private double scrollY = 0;


    private float virtualMouseX = VIRTUAL_WIDTH / 2f;
    private float virtualMouseY = VIRTUAL_HEIGHT / 2f;


    private double lastMouseX = 0;
    private double lastMouseY = 0;
    private boolean mouseInitialized = false;


    private float windowedSensitivity = 1.0f;
    private float fullscreenSensitivity = 0.6f;


    private float mouseSensitivity = windowedSensitivity;
    private boolean forceVirtualResolution = true;

    public Window(String title) {
        this.title = title == null || title.isEmpty() ? "SwordsGame" : title;
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

    public int getRenderWidth() {
        return forceVirtualResolution ? VIRTUAL_WIDTH : Math.max(1, framebufferWidth);
    }

    public int getRenderHeight() {
        return forceVirtualResolution ? VIRTUAL_HEIGHT : Math.max(1, framebufferHeight);
    }

    public int getFramebufferWidth() {
        return Math.max(1, framebufferWidth);
    }

    public int getFramebufferHeight() {
        return Math.max(1, framebufferHeight);
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

    public void toggleVirtualResolution() {
        forceVirtualResolution = !forceVirtualResolution;
        System.out.println("[Vid] Force virtual resolution: " + (forceVirtualResolution ? "ON" : "OFF"));
    }

    public boolean isForceVirtualResolution() {
        return forceVirtualResolution;
    }


    public float getUiScaleToPhysicalPixels() {
        if (forceVirtualResolution) {
            return physicalWidth / (float) VIRTUAL_WIDTH;
        }
        return framebufferWidth / (float) VIRTUAL_WIDTH;
    }

    public float getVirtualUnitsForPhysicalPixels(float pixels) {
        float scale = getUiScaleToPhysicalPixels();
        if (scale <= 0f) {
            return pixels;
        }
        return pixels / scale;
    }





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





    public void create() {
        initGlfw();
        createWindowHandle();
        initOpenGlContext();
        setupCallbacks();

        initFBO();
        updateViewport();

        updateSensitivityMode();
        captureCursor();

        glfwShowWindow(windowHandle);
        System.out.println("[Vid] Window created");
    }

    private void initGlfw() {
        if (!glfwInit()) {
            throw new IllegalStateException("GLFW init failed");
        }

        System.out.println("[Sys] GLFW initialized");

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 2);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 1);
    }

    private void createWindowHandle() {
        windowHandle = glfwCreateWindow(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, title, NULL, NULL);
        if (windowHandle == NULL) {
            throw new IllegalStateException("Failed to create GLFW window");
        }
    }

    private void initOpenGlContext() {
        glfwMakeContextCurrent(windowHandle);
        GL.createCapabilities();
        glfwSwapInterval(1);
        System.out.println("[Vid] VSync enabled");
    }





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
                SwordsGame.client.utils.Screenshot.takeScreenshot(fboId, VIRTUAL_WIDTH, VIRTUAL_HEIGHT);
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





    private void updateViewport() {
        int[] fbW = new int[1];
        int[] fbH = new int[1];
        glfwGetFramebufferSize(windowHandle, fbW, fbH);

        int framebufferW = fbW[0];
        int framebufferH = fbH[0];
        framebufferWidth = framebufferW;
        framebufferHeight = framebufferH;

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


        if (virtualMouseX < 0) virtualMouseX = 0;
        if (virtualMouseY < 0) virtualMouseY = 0;

        float maxMouseX = VIRTUAL_WIDTH - 1f;
        float maxMouseY = VIRTUAL_HEIGHT - 1f;
        if (virtualMouseX > maxMouseX) virtualMouseX = maxMouseX;
        if (virtualMouseY > maxMouseY) virtualMouseY = maxMouseY;
    }





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

        initScreenQuad();

        System.out.println("[Vid] FBO created OK");
    }

    private void initScreenQuad() {
        useVao = GL.getCapabilities().OpenGL30;
        float[] quadVertices = {
                0f, 0f, 0f, 0f,
                1f, 0f, 1f, 0f,
                1f, 1f, 1f, 1f,
                0f, 1f, 0f, 1f
        };

        FloatBuffer quadBuffer = BufferUtils.createFloatBuffer(quadVertices.length);
        quadBuffer.put(quadVertices).flip();

        if (useVao) {
            screenVaoId = glGenVertexArrays();
            glBindVertexArray(screenVaoId);
        }

        screenVboId = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, screenVboId);
        glBufferData(GL_ARRAY_BUFFER, quadBuffer, GL_STATIC_DRAW);

        glEnableClientState(GL_VERTEX_ARRAY);
        glVertexPointer(2, GL_FLOAT, 4 * Float.BYTES, 0L);

        glEnableClientState(GL_TEXTURE_COORD_ARRAY);
        glTexCoordPointer(2, GL_FLOAT, 4 * Float.BYTES, (long) (2 * Float.BYTES));

        if (useVao) {
            glBindVertexArray(0);
        }
        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }





    public void beginRenderToFBO() {
        if (forceVirtualResolution) {
            glBindFramebuffer(GL_FRAMEBUFFER, fboId);
            glViewport(0, 0, VIRTUAL_WIDTH, VIRTUAL_HEIGHT);
        } else {
            glBindFramebuffer(GL_FRAMEBUFFER, 0);
            glViewport(0, 0, framebufferWidth, framebufferHeight);
        }

        glClearColor(0.15f, 0.15f, 0.15f, 1f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    }

    public void endRenderToFBO() {
        if (forceVirtualResolution) {
            glBindFramebuffer(GL_FRAMEBUFFER, 0);
        }
    }

    public void drawFBO() {
        if (!forceVirtualResolution) {
            return;
        }
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

        if (useVao) {
            glBindVertexArray(screenVaoId);
        }
        glBindBuffer(GL_ARRAY_BUFFER, screenVboId);
        glDrawArrays(GL_QUADS, 0, 4);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        if (useVao) {
            glBindVertexArray(0);
        }

        glBindTexture(GL_TEXTURE_2D, 0);
        glDisable(GL_TEXTURE_2D);
    }





    public void update() {
        updateMouse();
        glfwSwapBuffers(windowHandle);
        glfwPollEvents();
    }

    public boolean shouldClose() {
        return glfwWindowShouldClose(windowHandle);
    }





    public void destroy() {

        if (depthId != 0) glDeleteRenderbuffers(depthId);
        if (textureId != 0) glDeleteTextures(textureId);
        if (fboId != 0) glDeleteFramebuffers(fboId);
        if (screenVboId != 0) glDeleteBuffers(screenVboId);
        if (useVao && screenVaoId != 0) glDeleteVertexArrays(screenVaoId);

        glfwDestroyWindow(windowHandle);
        glfwTerminate();

        System.out.println("[Sys] Window destroyed");
    }
}
