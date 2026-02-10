package SwordsGame.server.environment;

public class DayNightCycle {
    private final Sun sun;
    private float previousYaw;
    private float currentYaw;
    private int tickAccumulator;

    public DayNightCycle(Sun sun) {
        this.sun = sun;
        this.previousYaw = sun.getYaw();
        this.currentYaw = sun.getYaw();
    }

    public void syncFromSun() {
        this.previousYaw = sun.getYaw();
        this.currentYaw = sun.getYaw();
        this.tickAccumulator = 0;
    }

    public void tick() {
        tickAccumulator++;
        if (tickAccumulator >= 12) {
            tickAccumulator = 0;
            previousYaw = currentYaw;
            sun.rotateYaw(1.0f);
            currentYaw = sun.getYaw();
        }
    }

    public float getInterpolatedYaw(float alpha) {
        float delta = normalizeDelta(currentYaw - previousYaw);
        return normalizeAngle(previousYaw + (delta * alpha));
    }

    private float normalizeDelta(float delta) {
        float d = delta;
        if (d > 180.0f) d -= 360.0f;
        if (d < -180.0f) d += 360.0f;
        return d;
    }

    private float normalizeAngle(float angle) {
        float result = angle % 360.0f;
        if (result < 0.0f) {
            result += 360.0f;
        }
        return result;
    }
}
