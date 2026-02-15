package SwordsGame.client.graphics;

import SwordsGame.client.core.Window;
import static org.lwjgl.opengl.GL11.*;

public class Renderer {
    private static final int VIEWPORT_MARGIN_X = 120;
    private static final float CLEAR_R = 0.5f;
    private static final float CLEAR_G = 0.8f;
    private static final float CLEAR_B = 1.0f;
    private static final float DEFAULT_SUN_YAW = 45.0f;
    private static final float DEFAULT_SUN_PITCH = 50.0f;
    private static final float DAY_AMBIENT_R = 0.35f;
    private static final float DAY_AMBIENT_G = 0.35f;
    private static final float DAY_AMBIENT_B = 0.35f;
    private static final float DAY_DIFFUSE_R = 0.95f;
    private static final float DAY_DIFFUSE_G = 0.95f;
    private static final float DAY_DIFFUSE_B = 0.95f;
    private static final float WARM_AMBIENT_R = 0.45f;
    private static final float WARM_AMBIENT_G = 0.32f;
    private static final float WARM_AMBIENT_B = 0.22f;
    private static final float WARM_DIFFUSE_R = 1.0f;
    private static final float WARM_DIFFUSE_G = 0.8f;
    private static final float WARM_DIFFUSE_B = 0.65f;
    private static final float NIGHT_AMBIENT_R = 0.12f;
    private static final float NIGHT_AMBIENT_G = 0.14f;
    private static final float NIGHT_AMBIENT_B = 0.2f;
    private static final float NIGHT_DIFFUSE_R = 0.35f;
    private static final float NIGHT_DIFFUSE_G = 0.4f;
    private static final float NIGHT_DIFFUSE_B = 0.55f;
    private static final float SUN_TRANSITION_ELEVATION = 0.6f;

    private int viewportX = VIEWPORT_MARGIN_X;
    private int viewportY = 0;
    private int viewportWidth = 720;
    private int viewportHeight = 540;
    private float sunDirX = 0.0f;
    private float sunDirY = 1.0f;
    private float sunDirZ = 0.0f;
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
        int virtualWidth = win.getVirtualWidth();
        int virtualHeight = win.getVirtualHeight();
        viewportX = VIEWPORT_MARGIN_X;
        viewportY = 0;
        viewportWidth = Math.max(1, virtualWidth - (VIEWPORT_MARGIN_X * 2));
        viewportHeight = virtualHeight;

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
    }

    public void setup2D(Window win) {
        glViewport(0, 0, win.getVirtualWidth(), win.getVirtualHeight());
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        glOrtho(0, win.getVirtualWidth(), win.getVirtualHeight(), 0, -1, 1);
        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();

        glDisable(GL_DEPTH_TEST);
        glDisable(GL_LIGHTING);
        glDisable(GL_CULL_FACE);
        glEnable(GL_TEXTURE_2D);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
    }

    public void setSunDirection(float x, float y, float z) {
        float length = (float) Math.sqrt(x * x + y * y + z * z);
        if (length == 0.0f) {
            sunDirX = 0.0f;
            sunDirY = 1.0f;
            sunDirZ = 0.0f;
            updateEnvironmentFromSun();
            return;
        }
        sunDirX = x / length;
        sunDirY = y / length;
        sunDirZ = z / length;
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
        float[] lightPosition = { sunDirX, sunDirY, sunDirZ, 0.0f };
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

    private void updateEnvironmentFromSun() {
        float elevation = sunDirY;
        if (elevation >= 0.0f) {
            float warm = clamp(1.0f - (elevation / SUN_TRANSITION_ELEVATION), 0.0f, 1.0f);
            ambientR = lerp(DAY_AMBIENT_R, WARM_AMBIENT_R, warm);
            ambientG = lerp(DAY_AMBIENT_G, WARM_AMBIENT_G, warm);
            ambientB = lerp(DAY_AMBIENT_B, WARM_AMBIENT_B, warm);
            diffuseR = lerp(DAY_DIFFUSE_R, WARM_DIFFUSE_R, warm);
            diffuseG = lerp(DAY_DIFFUSE_G, WARM_DIFFUSE_G, warm);
            diffuseB = lerp(DAY_DIFFUSE_B, WARM_DIFFUSE_B, warm);
            return;
        }

        float night = clamp((-elevation) / SUN_TRANSITION_ELEVATION, 0.0f, 1.0f);
        ambientR = lerp(DAY_AMBIENT_R, NIGHT_AMBIENT_R, night);
        ambientG = lerp(DAY_AMBIENT_G, NIGHT_AMBIENT_G, night);
        ambientB = lerp(DAY_AMBIENT_B, NIGHT_AMBIENT_B, night);
        diffuseR = lerp(DAY_DIFFUSE_R, NIGHT_DIFFUSE_R, night);
        diffuseG = lerp(DAY_DIFFUSE_G, NIGHT_DIFFUSE_G, night);
        diffuseB = lerp(DAY_DIFFUSE_B, NIGHT_DIFFUSE_B, night);
    }

    private float lerp(float from, float to, float t) {
        return from + (to - from) * t;
    }

    private float clamp(float value, float min, float max) {
        if (value < min) {
            return min;
        }
        if (value > max) {
            return max;
        }
        return value;
    }
}
