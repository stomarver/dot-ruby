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
    private static final float NIGHT_TINT_STRENGTH = 0.75f;
    private static final float NIGHT_TINT_R = 0.26f;
    private static final float NIGHT_TINT_G = 0.26f;
    private static final float NIGHT_TINT_B = 1.00f;
    private static final float DAY_FOG_START_DISTANCE = -640.0f;
    private static final float DAY_FOG_END_DISTANCE = -280.0f;
    private static final float NIGHT_FOG_START_DISTANCE = -760.0f;
    private static final float NIGHT_FOG_END_DISTANCE = -320.0f;

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
    private float nightBlend = 0.0f;
    private final FogFx fogFx = new FogFx();

    public Renderer() {
        setSunDirectionFromAngles(DEFAULT_SUN_YAW, DEFAULT_SUN_PITCH);
        fogFx.setDistanceRange(DAY_FOG_START_DISTANCE, DAY_FOG_END_DISTANCE);
    }

    public int getViewportX() { return viewportX; }
    public int getViewportY() { return viewportY; }
    public int getViewportWidth() { return viewportWidth; }
    public int getViewportHeight() { return viewportHeight; }

    public float getFogStartDistance() { return fogFx.startDist(); }
    public float getFogEndDistance() { return fogFx.endDist(); }

    public void setup3D(Window win) {
        int renderWidth = win.getRenderWidth();
        int renderHeight = win.getRenderHeight();
        int marginX = win.isForceVirtualResolution() ? VIEWPORT_MARGIN_X : Math.max(1, Math.round(VIEWPORT_MARGIN_X * (renderWidth / (float) win.getVirtualWidth())));
        viewportX = marginX;
        viewportY = 0;
        viewportWidth = Math.max(1, renderWidth - (marginX * 2));
        viewportHeight = renderHeight;

        glClearColor(CLEAR_R * tintR(), CLEAR_G * tintG(), CLEAR_B * tintB(), 1.0f);
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

    public void setFogZoom(float cameraZoom) {
        fogFx.setZoom(cameraZoom);
    }

    public void applyScreenSpaceFog(Window window) {
        fogFx.apply(window, viewportX, viewportY, viewportWidth, viewportHeight);
    }

    public void setFogColor(float r, float g, float b) {
        fogFx.setColor(r, g, b);
    }

    public void setNightTint(float blend) {
        nightBlend = Math.max(0.0f, Math.min(1.0f, blend));
        float fogStart = lerp(DAY_FOG_START_DISTANCE, NIGHT_FOG_START_DISTANCE, nightBlend);
        float fogEnd = lerp(DAY_FOG_END_DISTANCE, NIGHT_FOG_END_DISTANCE, nightBlend);
        fogFx.setDistanceRange(fogStart, fogEnd);
        updateEnvironmentFromSun();
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
        glDisable(GL_FOG);
    }

    private float lerp(float a, float b, float t) {
        return a + ((b - a) * t);
    }

    private void updateEnvironmentFromSun() {
        ambientR = DAY_AMBIENT_R * tintR();
        ambientG = DAY_AMBIENT_G * tintG();
        ambientB = DAY_AMBIENT_B * tintB();
        diffuseR = DAY_DIFFUSE_R * tintR();
        diffuseG = DAY_DIFFUSE_G * tintG();
        diffuseB = DAY_DIFFUSE_B * tintB();
    }

    private float tintR() {
        return 1.0f - (nightBlend * NIGHT_TINT_STRENGTH * (1.0f - NIGHT_TINT_R));
    }

    private float tintG() {
        return 1.0f - (nightBlend * NIGHT_TINT_STRENGTH * (1.0f - NIGHT_TINT_G));
    }

    private float tintB() {
        return 1.0f - (nightBlend * NIGHT_TINT_STRENGTH * (1.0f - NIGHT_TINT_B));
    }
}
