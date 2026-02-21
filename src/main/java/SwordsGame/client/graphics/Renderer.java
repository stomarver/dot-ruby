package SwordsGame.client.graphics;

import SwordsGame.client.core.Window;
import org.joml.Vector3f;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;

public class Renderer {
    private static final int VIEWPORT_MARGIN_X = 120;
    private static final float CLEAR_R = 0.5f;
    private static final float CLEAR_G = 0.8f;
    private static final float CLEAR_B = 1.0f;
    private static final float DEFAULT_SUN_YAW = 30.0f;
    private static final float DEFAULT_SUN_PITCH = 15.0f;
    private static final float DAY_AMBIENT_R = 0.35f;
    private static final float DAY_AMBIENT_G = 0.35f;
    private static final float DAY_AMBIENT_B = 0.35f;
    private static final float DAY_DIFFUSE_R = 0.95f;
    private static final float DAY_DIFFUSE_G = 0.95f;
    private static final float DAY_DIFFUSE_B = 0.95f;
    private static final float FOG_R = 0.0f;
    private static final float FOG_G = 0.0f;
    private static final float FOG_B = 0.0f;
    private static final float FOG_START = 180.0f;
    private static final float FOG_END = 850.0f;

    private int viewportX = VIEWPORT_MARGIN_X;
    private int viewportY = 0;
    private int viewportWidth = 720;
    private int viewportHeight = 540;
    private final Vector3f sunDirection = new Vector3f(0.0f, 1.0f, 0.0f);
    private float ambientR = DAY_AMBIENT_R;
    private float ambientG = DAY_AMBIENT_G;
    private float ambientB = DAY_AMBIENT_B;
    private float diffuseR = DAY_DIFFUSE_R;
    private float diffuseG = DAY_DIFFUSE_G;
    private float diffuseB = DAY_DIFFUSE_B;

    public Renderer() {
        setSunDirectionFromAngles(DEFAULT_SUN_YAW, DEFAULT_SUN_PITCH);
    }

    public int getViewportX() { return viewportX; }
    public int getViewportY() { return viewportY; }
    public int getViewportWidth() { return viewportWidth; }
    public int getViewportHeight() { return viewportHeight; }

    public void setup3D(Window win) {
        int renderWidth = win.getRenderWidth();
        int renderHeight = win.getRenderHeight();
        int marginX = win.isForceVirtualResolution() ? VIEWPORT_MARGIN_X : Math.max(1, Math.round(VIEWPORT_MARGIN_X * (renderWidth / (float) win.getVirtualWidth())));
        viewportX = marginX;
        viewportY = 0;
        viewportWidth = Math.max(1, renderWidth - (marginX * 2));
        viewportHeight = renderHeight;

        glClearColor(CLEAR_R, CLEAR_G, CLEAR_B, 1.0f);
        glClearDepth(1.0f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glViewport(viewportX, viewportY, viewportWidth, viewportHeight);
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        glOrtho(-360, 360, -270, 270, -5000, 5000);
        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();

        glEnable(GL_DEPTH_TEST);
        glDepthFunc(GL_LEQUAL);
        glDepthMask(true);
        glEnable(GL_CULL_FACE);
        glCullFace(GL_BACK);
        glEnable(GL_TEXTURE_2D);
        glDisable(GL_BLEND);

        setupLighting();
        setupFog();
    }

    public void setup2D(Window win) {
        int viewportW = win.isForceVirtualResolution() ? win.getVirtualWidth() : win.getFramebufferWidth();
        int viewportH = win.isForceVirtualResolution() ? win.getVirtualHeight() : win.getFramebufferHeight();
        glViewport(0, 0, viewportW, viewportH);
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        glOrtho(0, win.getVirtualWidth(), win.getVirtualHeight(), 0, -1, 1);
        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();

        glDisable(GL_DEPTH_TEST);
        glDisable(GL_FOG);
        glDisable(GL_LIGHTING);
        glDisable(GL_CULL_FACE);
        glDisable(GL_COLOR_MATERIAL);
        glDisable(GL_NORMALIZE);
        glEnable(GL_TEXTURE_2D);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glDisableClientState(GL_COLOR_ARRAY);
        glDisableClientState(GL_NORMAL_ARRAY);
    }

    public void setSunDirection(float x, float y, float z) {
        if (x == 0.0f && y == 0.0f && z == 0.0f) {
            sunDirection.set(0.0f, 1.0f, 0.0f);
            updateEnvironmentFromSun();
            return;
        }
        sunDirection.set(x, y, z).normalize();
        updateEnvironmentFromSun();
    }

    public void setSunDirectionFromAngles(float yawDegrees, float pitchDegrees) {
        float yaw = (float) Math.toRadians(yawDegrees);
        float pitch = (float) Math.toRadians(pitchDegrees);
        float cosPitch = (float) Math.cos(pitch);
        float x = (float) (Math.cos(yaw) * cosPitch);
        float y = (float) Math.sin(pitch);
        float z = (float) (Math.sin(yaw) * cosPitch);
        setSunDirection(x, y, z);
    }

    public void applySunLight() {
        float[] lightPosition = { sunDirection.x, sunDirection.y, sunDirection.z, 0.0f };
        glLightfv(GL_LIGHT0, GL_POSITION, lightPosition);
    }

    private void setupLighting() {
        glEnable(GL_LIGHTING);
        glEnable(GL_LIGHT0);
        glEnable(GL_COLOR_MATERIAL);
        glColorMaterial(GL_FRONT_AND_BACK, GL_AMBIENT_AND_DIFFUSE);
        glEnable(GL_NORMALIZE);
        float[] ambientLight = { ambientR, ambientG, ambientB, 1.0f };
        float[] diffuseLight = { diffuseR, diffuseG, diffuseB, 1.0f };

        glLightfv(GL_LIGHT0, GL_AMBIENT, ambientLight);
        glLightfv(GL_LIGHT0, GL_DIFFUSE, diffuseLight);
    }

    private void setupFog() {
        glEnable(GL_FOG);
        glFogi(GL_FOG_MODE, GL_LINEAR);
        glFogf(GL_FOG_START, FOG_START);
        glFogf(GL_FOG_END, FOG_END);
        glFogf(GL_FOG_DENSITY, 1.0f);
        glHint(GL_FOG_HINT, GL_NICEST);
        float[] fogColor = { FOG_R, FOG_G, FOG_B, 1.0f };
        glFogfv(GL_FOG_COLOR, fogColor);
    }

    private void updateEnvironmentFromSun() {
        ambientR = DAY_AMBIENT_R;
        ambientG = DAY_AMBIENT_G;
        ambientB = DAY_AMBIENT_B;
        diffuseR = DAY_DIFFUSE_R;
        diffuseG = DAY_DIFFUSE_G;
        diffuseB = DAY_DIFFUSE_B;
    }
}
