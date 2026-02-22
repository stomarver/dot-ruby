package SwordsGame.client.graphics;

import org.joml.Vector3f;

public class FogEnvironmentCalculator {
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

    private final Vector3f sunDirection = new Vector3f(0.0f, 1.0f, 0.0f);
    private final Vector3f ambient = new Vector3f(DAY_AMBIENT_R, DAY_AMBIENT_G, DAY_AMBIENT_B);
    private final Vector3f diffuse = new Vector3f(DAY_DIFFUSE_R, DAY_DIFFUSE_G, DAY_DIFFUSE_B);

    public void setSunDirection(float x, float y, float z) {
        sunDirection.set(x, y, z);
        if (sunDirection.lengthSquared() < 1.0e-8f) {
            sunDirection.set(0.0f, 1.0f, 0.0f);
        } else {
            sunDirection.normalize();
        }
        updateEnvironmentFromSun();
    }

    public Vector3f getSunDirection(Vector3f out) {
        return out.set(sunDirection);
    }

    public Vector3f getAmbient(Vector3f out) {
        return out.set(ambient);
    }

    public Vector3f getDiffuse(Vector3f out) {
        return out.set(diffuse);
    }

    private void updateEnvironmentFromSun() {
        float elevation = sunDirection.y;
        if (elevation >= 0.0f) {
            float warm = clamp01(1.0f - (elevation / SUN_TRANSITION_ELEVATION));
            ambient.set(
                    lerp(DAY_AMBIENT_R, WARM_AMBIENT_R, warm),
                    lerp(DAY_AMBIENT_G, WARM_AMBIENT_G, warm),
                    lerp(DAY_AMBIENT_B, WARM_AMBIENT_B, warm)
            );
            diffuse.set(
                    lerp(DAY_DIFFUSE_R, WARM_DIFFUSE_R, warm),
                    lerp(DAY_DIFFUSE_G, WARM_DIFFUSE_G, warm),
                    lerp(DAY_DIFFUSE_B, WARM_DIFFUSE_B, warm)
            );
            return;
        }

        float night = clamp01((-elevation) / SUN_TRANSITION_ELEVATION);
        ambient.set(
                lerp(DAY_AMBIENT_R, NIGHT_AMBIENT_R, night),
                lerp(DAY_AMBIENT_G, NIGHT_AMBIENT_G, night),
                lerp(DAY_AMBIENT_B, NIGHT_AMBIENT_B, night)
        );
        diffuse.set(
                lerp(DAY_DIFFUSE_R, NIGHT_DIFFUSE_R, night),
                lerp(DAY_DIFFUSE_G, NIGHT_DIFFUSE_G, night),
                lerp(DAY_DIFFUSE_B, NIGHT_DIFFUSE_B, night)
        );
    }

    private float lerp(float from, float to, float t) {
        return from + (to - from) * t;
    }

    private float clamp01(float value) {
        return Math.max(0.0f, Math.min(1.0f, value));
    }
}
