package SwordsGame.core;

import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import java.nio.DoubleBuffer;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.system.MemoryUtil.*;

public class Window {
    private long windowHandle;
    private int physicalX, physicalY, physicalWidth, physicalHeight;
    private int fboId, textureId, depthId;
    private boolean isFullscreen = false;
    private static final int VIRTUAL_WIDTH = 960;
    private static final int VIRTUAL_HEIGHT = 540;

    private double scrollX = 0;
    private double scrollY = 0;

    public long getHandle() { return windowHandle; }

    public Window(String title) {}

    public float getMouseRelX(long windowHandle) {
        DoubleBuffer px = BufferUtils.createDoubleBuffer(1);
        glfwGetCursorPos(windowHandle, px, null);
        float relX = ((float) px.get(0) - physicalX) * VIRTUAL_WIDTH / physicalWidth;

        if (relX < 0) relX = 0;
        if (relX > VIRTUAL_WIDTH) relX = VIRTUAL_WIDTH;

        return relX;
    }

    public float getMouseRelY(long windowHandle) {
        DoubleBuffer py = BufferUtils.createDoubleBuffer(1);
        glfwGetCursorPos(windowHandle, null, py);
        float relY = ((float) py.get(0) - physicalY) * VIRTUAL_HEIGHT / physicalHeight;

        if (relY < 0) relY = 0;
        if (relY > VIRTUAL_HEIGHT) relY = VIRTUAL_HEIGHT;

        return relY;
    }

    public double getScrollX() { return scrollX; }
    public double getScrollY() { return scrollY; }
    public void resetScroll() { scrollX = 0; scrollY = 0; }

    private void updatePhysicalDimensions(int fbW, int fbH) {
        float targetAspect = (float) VIRTUAL_WIDTH / VIRTUAL_HEIGHT;
        float windowAspect = (float) fbW / fbH;

        if (windowAspect > targetAspect) {
            physicalHeight = fbH;
            physicalWidth = (int) (fbH * targetAspect);
            physicalX = (fbW - physicalWidth) / 2;
            physicalY = 0;
        } else {
            physicalWidth = fbW;
            physicalHeight = (int) (fbW / targetAspect);
            physicalX = 0;
            physicalY = (fbH - physicalHeight) / 2;
        }
    }

    public void create() {
        if (!glfwInit()) throw new IllegalStateException("GLFW init failed");
        System.out.println("[Sys] GLFW initialized");

        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE);

        windowHandle = glfwCreateWindow(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, "SwordsGame", NULL, NULL);
        if (windowHandle == NULL) throw new RuntimeException("Window creation failed");

        glfwSetWindowSizeLimits(windowHandle, VIRTUAL_WIDTH, VIRTUAL_HEIGHT, VIRTUAL_WIDTH, VIRTUAL_HEIGHT);

        System.out.println("[Vid] Window created: 960x540");

        glfwMakeContextCurrent(windowHandle);
        GL.createCapabilities();

        initFBO();
        setupInput();

        glfwSwapInterval(1);
        System.out.println("[Vid] VSync enabled");

        int[] fw = new int[1], fh = new int[1];
        glfwGetFramebufferSize(windowHandle, fw, fh);
        updatePhysicalDimensions(fw[0], fh[0]);

        glfwSetInputMode(windowHandle, GLFW_CURSOR, GLFW_CURSOR_HIDDEN);
        System.out.println("[Input] System cursor hidden, using virtual cursor");

        glfwShowWindow(windowHandle);
    }

    private void initFBO() {
        fboId = glGenFramebuffers();
        glBindFramebuffer(GL_FRAMEBUFFER, fboId);

        textureId = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, textureId);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, VIRTUAL_WIDTH, VIRTUAL_HEIGHT, 0, GL_RGB, GL_UNSIGNED_BYTE, (java.nio.ByteBuffer)null);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, textureId, 0);

        depthId = glGenRenderbuffers();
        glBindRenderbuffer(GL_RENDERBUFFER, depthId);
        glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH_COMPONENT, VIRTUAL_WIDTH, VIRTUAL_HEIGHT);
        glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, depthId);

        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }

    public void setupInput() {
        glfwSetKeyCallback(windowHandle, (window, key, scancode, action, mods) -> {
            if (action == GLFW_PRESS) {
                boolean isAltEnter = (key == GLFW_KEY_ENTER && (mods & GLFW_MOD_ALT) != 0);
                if (key == GLFW_KEY_F4 || isAltEnter) {
                    toggleFullscreen();
                }
                if (key == GLFW_KEY_ESCAPE) glfwSetWindowShouldClose(window, true);

                boolean isCtrlP = (key == GLFW_KEY_P && (mods & GLFW_MOD_CONTROL) != 0);
                if (key == GLFW_KEY_F12 || isCtrlP) {
                    SwordsGame.utils.Screenshot.takeScreenshot(fboId, VIRTUAL_WIDTH, VIRTUAL_HEIGHT);
                }
            }
        });

        glfwSetFramebufferSizeCallback(windowHandle, (window, w, h) -> {
            updatePhysicalDimensions(w, h);
        });

        glfwSetScrollCallback(windowHandle, (window, xoffset, yoffset) -> {
            scrollX = xoffset;
            scrollY = yoffset;
        });
    }

    public void toggleFullscreen() {
        isFullscreen = !isFullscreen;
        long monitor = glfwGetPrimaryMonitor();
        GLFWVidMode mode = glfwGetVideoMode(monitor);

        if (isFullscreen) {
            glfwSetWindowMonitor(windowHandle, monitor, 0, 0, mode.width(), mode.height(), mode.refreshRate());
        } else {
            glfwSetWindowMonitor(windowHandle, NULL, (mode.width() - VIRTUAL_WIDTH) / 2, (mode.height() - VIRTUAL_HEIGHT) / 2, VIRTUAL_WIDTH, VIRTUAL_HEIGHT, 0);
        }

        int[] fw = new int[1], fh = new int[1];
        glfwGetFramebufferSize(windowHandle, fw, fh);
        updatePhysicalDimensions(fw[0], fh[0]);

        glfwSetInputMode(windowHandle, GLFW_CURSOR, GLFW_CURSOR_HIDDEN);
    }

    public void beginRenderToFBO() {
        glBindFramebuffer(GL_FRAMEBUFFER, fboId);
        glViewport(0, 0, VIRTUAL_WIDTH, VIRTUAL_HEIGHT);
        glClearColor(0.2f, 0.2f, 0.2f, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    }

    public void endRenderToFBO() {
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glViewport(physicalX, physicalY, physicalWidth, physicalHeight);
    }

    public void drawFBO() {
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glViewport(0, 0, physicalWidth + (physicalX * 2), physicalHeight + (physicalY * 2));
        glClearColor(0, 0, 0, 1);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

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

    public void update() {
        glfwSwapBuffers(windowHandle);
        glfwPollEvents();
    }

    public boolean shouldClose() { return glfwWindowShouldClose(windowHandle); }
    public void destroy() { glfwDestroyWindow(windowHandle); glfwTerminate(); }
    public int getVirtualWidth() { return VIRTUAL_WIDTH; }
    public int getVirtualHeight() { return VIRTUAL_HEIGHT; }
}
