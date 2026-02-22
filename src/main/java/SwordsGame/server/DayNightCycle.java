package SwordsGame.server;

import static org.lwjgl.glfw.GLFW.*;

public class DayNightCycle {
    private static final float CYCLE_SECONDS = 38f * 60f;
    private static final float FAST_SKIP_SECONDS = 60f;
    private static final float BLUE_FOG_R = 0.40f;
    private static final float BLUE_FOG_G = 0.60f;
    private static final float BLUE_FOG_B = 0.85f;
    private static final float DAY_FOG_DISTANCE_MULTIPLIER = 1.18f;

    private float timeSeconds = 0f;
    private int day = 0;
    private boolean uHeld = false;
    private boolean yHeld = false;

    public void update(long windowHandle, float deltaSeconds) {
        boolean uPressed = glfwGetKey(windowHandle, GLFW_KEY_U) == GLFW_PRESS;
        boolean yPressed = glfwGetKey(windowHandle, GLFW_KEY_Y) == GLFW_PRESS;

        if (uPressed && !uHeld) {
            addSeconds(FAST_SKIP_SECONDS);
        }
        if (yPressed && !yHeld) {
            addSeconds(-FAST_SKIP_SECONDS);
        }

        uHeld = uPressed;
        yHeld = yPressed;

        addSeconds(Math.max(0f, deltaSeconds));
    }

    public int getDay() {
        return day;
    }

    public float getTimeSeconds() {
        return timeSeconds;
    }

    public float getFogR() {
        return isBlackFogWindow() ? 0f : BLUE_FOG_R;
    }

    public float getFogG() {
        return isBlackFogWindow() ? 0f : BLUE_FOG_G;
    }

    public float getFogB() {
        return isBlackFogWindow() ? 0f : BLUE_FOG_B;
    }

    public float getFogDistanceMultiplier() {
        return isBlackFogWindow() ? 1.0f : DAY_FOG_DISTANCE_MULTIPLIER;
    }

    public String getTimeLabel() {
        int total = (int) Math.floor(timeSeconds);
        int minutes = (total / 60) % 60;
        int seconds = total % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    private boolean isBlackFogWindow() {
        float minutes = timeSeconds / 60f;
        return minutes >= 24f && minutes < 32f;
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
