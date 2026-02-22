package SwordsGame.client.graphics;

public final class BlockColorPipeline {
    private static final float COLOR_MIN = 0.65f;

    private BlockColorPipeline() {}

    public static float[] resolveTint(BlockProps props, int seed) {
        if (props == null || !props.hasRandomColor()) {
            return new float[] {1.0f, 1.0f, 1.0f};
        }

        int mixed = mix(seed);
        int dominant = Math.floorMod(mixed, 3);

        float r = randomChannel(mixed, 0);
        float g = randomChannel(mixed, 1);
        float b = randomChannel(mixed, 2);

        if (dominant == 0) r = 1.0f;
        else if (dominant == 1) g = 1.0f;
        else b = 1.0f;

        return new float[] {r, g, b};
    }

    private static int mix(int x) {
        x ^= (x << 13);
        x ^= (x >>> 17);
        x ^= (x << 5);
        return x;
    }

    private static float randomChannel(int mixed, int channel) {
        int h = mix(mixed ^ (channel * 0x9E3779B9));
        h = Math.abs(h % 36);
        return COLOR_MIN + (h / 100f);
    }
}
