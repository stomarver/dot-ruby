package SwordsGame.server;

import org.joml.Math;

import static org.lwjgl.glfw.GLFW.*;

public class DayNightCycle {
    private static final float CYCLE_SECONDS = 38f * 60f;
    private static final float FAST_SKIP_PER_SECOND = 15f;
    private static final float SHIFT_SKIP_MULTIPLIER = 8f;

    private static final float BLUE_FOG_R = 0.40f;
    private static final float BLUE_FOG_G = 0.60f;
    private static final float BLUE_FOG_B = 0.85f;
    private static final float ORANGE_FOG_R = 0.95f;
    private static final float ORANGE_FOG_G = 0.48f;
    private static final float ORANGE_FOG_B = 0.16f;

    private static final float DAY_FOG_DISTANCE_MULTIPLIER = 2.50f;
    private static final float NIGHT_FOG_DISTANCE_MULTIPLIER = 1.50f;
    private static final float FOG_TRANSITION_MINUTES = 1.0f;

    private float timeSeconds = 0f;
    private int day = 0;

    public void update(long windowHandle, float deltaSeconds) {
        float dt = Math.max(0f, deltaSeconds);

        boolean uPressed = glfwGetKey(windowHandle, GLFW_KEY_U) == GLFW_PRESS;
        boolean yPressed = glfwGetKey(windowHandle, GLFW_KEY_Y) == GLFW_PRESS;

        boolean shiftPressed = glfwGetKey(windowHandle, GLFW_KEY_LEFT_SHIFT) == GLFW_PRESS
                || glfwGetKey(windowHandle, GLFW_KEY_RIGHT_SHIFT) == GLFW_PRESS;
        float skipSpeed = FAST_SKIP_PER_SECOND * (shiftPressed ? SHIFT_SKIP_MULTIPLIER : 1f);

        if (uPressed && !yPressed) {
            addSeconds(dt * skipSpeed);
        } else if (yPressed && !uPressed) {
            addSeconds(-dt * skipSpeed);
        }

        addSeconds(dt);
    }

    public int getDay() {
        return day;
    }

    public int getUiDay() {
        return day + 1;
    }

    public float getTimeSeconds() {
        return timeSeconds;
    }

    public float getCycleMinutes() {
        return timeSeconds / 60f;
    }

    public float getFogR() {
        return blendFogColor(BLUE_FOG_R, ORANGE_FOG_R, 0f);
    }

    public float getFogG() {
        return blendFogColor(BLUE_FOG_G, ORANGE_FOG_G, 0f);
    }

    public float getFogB() {
        return blendFogColor(BLUE_FOG_B, ORANGE_FOG_B, 0f);
    }

    public float getFogDistanceMultiplier() {
        return Math.lerp(DAY_FOG_DISTANCE_MULTIPLIER, NIGHT_FOG_DISTANCE_MULTIPLIER, getNightBlend());
    }

    public String getTimeLabel() {
        int total = (int) Math.floor(timeSeconds);
        int minutes = (total / 60) % 60;
        int seconds = total % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    public String getPhaseLabel() {
        float m = getCycleMinutes();
        float t = FOG_TRANSITION_MINUTES;
        if (m < 24f - t) return "Day";
        if (m < 24f + t) return "Dusk";
        if (m < 32f - t) return "Night";
        if (m < 32f + t) return "Dawn";
        return "Day";
    }

    private float blendFogColor(float day, float orange, float night) {
        float m = getCycleMinutes();
        float t = FOG_TRANSITION_MINUTES;

        if (m < 24f - t) return day;
        if (m < 24f) return Math.lerp(day, orange, smooth01((m - (24f - t)) / t));
        if (m < 24f + t) return Math.lerp(orange, night, smooth01((m - 24f) / t));

        if (m < 32f - t) return night;
        if (m < 32f) return Math.lerp(night, orange, smooth01((m - (32f - t)) / t));
        if (m < 32f + t) return Math.lerp(orange, day, smooth01((m - 32f) / t));

        return day;
    }


    public float getOrangeBlend() {
        float m = getCycleMinutes();
        float t = FOG_TRANSITION_MINUTES;
        float dusk = transitionBump(m, 24f, t);
        float dawn = transitionBump(m, 32f, t);
        return Math.max(dusk, dawn);
    }

    private float transitionBump(float minutes, float center, float halfWidth) {
        if (minutes < center - halfWidth || minutes > center + halfWidth) return 0f;
        if (minutes <= center) return smooth01((minutes - (center - halfWidth)) / halfWidth);
        return 1f - smooth01((minutes - center) / halfWidth);
    }
    public float getNightBlend() {
        float m = getCycleMinutes();
        float t = FOG_TRANSITION_MINUTES;

        // 0..24 day, 24..32 night, 32..38 dawn -> smooth transitions at 24 and 32.
        if (m < 24f - t) return 0f;
        if (m < 24f + t) return smooth01((m - (24f - t)) / (2f * t));
        if (m < 32f - t) return 1f;
        if (m < 32f + t) return 1f - smooth01((m - (32f - t)) / (2f * t));
        return 0f;
    }

    private float smooth01(float x) {
        float v = Math.clamp(0f, 1f, x);
        return v * v * (3f - 2f * v);
    }

    private void addSeconds(float seconds) {
        timeSeconds += seconds;

        while (timeSeconds >= CYCLE_SECONDS) {
            timeSeconds -= CYCLE_SECONDS;
            day += 1;
        }

        while (timeSeconds < 0f) {
            timeSeconds += CYCLE_SECONDS;
        }
    }
}
