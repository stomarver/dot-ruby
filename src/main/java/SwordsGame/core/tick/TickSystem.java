package SwordsGame.core.tick;

public class TickSystem {
    private final int maxTicksPerSecond;
    private final float tickDelta;
    private double accumulator;
    private double lastTime;

    public TickSystem(int maxTicksPerSecond) {
        this.maxTicksPerSecond = Math.max(1, maxTicksPerSecond);
        this.tickDelta = 1.0f / this.maxTicksPerSecond;
    }

    public void start(double currentTime) {
        this.lastTime = currentTime;
        this.accumulator = 0.0;
    }

    public void update(double currentTime, TickHandler handler) {
        double frame = currentTime - lastTime;
        if (frame < 0.0) frame = 0.0;
        if (frame > 0.25) frame = 0.25;
        lastTime = currentTime;
        accumulator += frame;

        int safety = 0;
        while (accumulator >= tickDelta && safety < maxTicksPerSecond * 2) {
            handler.onTick();
            accumulator -= tickDelta;
            safety++;
        }
    }

    public float getInterpolationAlpha() {
        return (float) Math.max(0.0, Math.min(1.0, accumulator / tickDelta));
    }

    public int getMaxTicksPerSecond() {
        return maxTicksPerSecond;
    }

    public interface TickHandler {
        void onTick();
    }
}
