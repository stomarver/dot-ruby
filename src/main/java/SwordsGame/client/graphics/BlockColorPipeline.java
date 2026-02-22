package SwordsGame.client.graphics;

public final class BlockColorPipeline {
    private static final float COLOR_MIN = 0.8f;

    private BlockColorPipeline() {}

    public static float[] resolveTint(BlockProps props, int seed) {
        int wx = seed;
        int wy = seed * 31;
        int wz = seed * 17;
        return resolveTint(props, seed, wx, wy, wz);
    }

    public static float[] resolveTint(BlockProps props, int seed, int wx, int wy, int wz) {
        if (props == null || !props.hasRandomColor()) {
            return new float[] {1.0f, 1.0f, 1.0f};
        }

        int dominant = Math.floorMod(wx + wy + wz, 3);

        float r = randomChannel(seed, wx, wy, wz, 0);
        float g = randomChannel(seed, wx, wy, wz, 1);
        float b = randomChannel(seed, wx, wy, wz, 2);

        if (dominant == 0) r = 1.0f;
        else if (dominant == 1) g = 1.0f;
        else b = 1.0f;

        return new float[] {r, g, b};
    }

    private static float randomChannel(int seed, int wx, int wy, int wz, int channel) {
        int h = seed;
        h ^= wx * 73856093;
        h ^= wy * 19349663;
        h ^= wz * 83492791;
        h ^= channel * 29791;
        h = Math.abs(h % 19);
        return COLOR_MIN + (h / 100f);
    }
}
