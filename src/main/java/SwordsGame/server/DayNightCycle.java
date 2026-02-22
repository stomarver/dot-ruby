package SwordsGame.server;

import org.joml.Math;

import static org.lwjgl.glfw.GLFW.*;

public class DayNightCycle {
    private static final float CYCLE_SECONDS = 38f * 60f;
    private static final float FAST_SKIP_PER_SECOND = 60f;

    private static final float BLUE_FOG_R = 0.40f;
    private static final float BLUE_FOG_G = 0.60f;
    private static final float BLUE_FOG_B = 0.85f;

    private static final float DAY_FOG_DISTANCE_MULTIPLIER = 1.18f;
    private static final float NIGHT_FOG_DISTANCE_MULTIPLIER = 1.00f;
    private static final float FOG_TRANSITION_MINUTES = 1.0f;

    private float timeSeconds = 0f;
    private int day = 0;

    public void update(long windowHandle, float deltaSeconds) {
        float dt = Math.max(0f, deltaSeconds);

        boolean uPressed = glfwGetKey(windowHandle, GLFW_KEY_U) == GLFW_PRESS;
        boolean yPressed = glfwGetKey(windowHandle, GLFW_KEY_Y) == GLFW_PRESS;

        if (uPressed && !yPressed) {
            addSeconds(dt * FAST_SKIP_PER_SECOND);
        } else if (yPressed && !uPressed) {
            addSeconds(-dt * FAST_SKIP_PER_SECOND);
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
        return Math.lerp(BLUE_FOG_R, 0f, getNightBlend());
    }

    public float getFogG() {
        return Math.lerp(BLUE_FOG_G, 0f, getNightBlend());
    }

    public float getFogB() {
        return Math.lerp(BLUE_FOG_B, 0f, getNightBlend());
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
        if (m < 24f) return "Day";
        if (m < 32f) return "Night";
        return "Dawn";
    }

    private float getNightBlend() {
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
