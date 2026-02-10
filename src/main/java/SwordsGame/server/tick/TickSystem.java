package SwordsGame.server.tick;

public class TickSystem {
    private final int maxTicksPerSecond;
    private final double tickStepSeconds;
    private double accumulatorSeconds;
    private double lastTimeSeconds;

    public TickSystem(int maxTicksPerSecond) {
        this.maxTicksPerSecond = Math.max(1, maxTicksPerSecond);
        this.tickStepSeconds = 1.0d / this.maxTicksPerSecond;
    }

    public void start(double nowSeconds) {
        this.lastTimeSeconds = nowSeconds;
        this.accumulatorSeconds = 0.0d;
    }

    public void advance(double nowSeconds, TickHandler handler) {
        double frameSeconds = nowSeconds - lastTimeSeconds;
        if (frameSeconds < 0.0d) frameSeconds = 0.0d;
        if (frameSeconds > 0.25d) frameSeconds = 0.25d;
        lastTimeSeconds = nowSeconds;
        accumulatorSeconds += frameSeconds;

        int maxTicksThisFrame = maxTicksPerSecond;
        int ticks = 0;
        while (accumulatorSeconds >= tickStepSeconds && ticks < maxTicksThisFrame) {
            handler.onTick();
            accumulatorSeconds -= tickStepSeconds;
            ticks++;
        }
    }

    public float getInterpolationAlpha() {
        return (float) Math.max(0.0d, Math.min(1.0d, accumulatorSeconds / tickStepSeconds));
    }

    public int getMaxTicksPerSecond() {
        return maxTicksPerSecond;
    }

    public interface TickHandler {
        void onTick();
    }
}
