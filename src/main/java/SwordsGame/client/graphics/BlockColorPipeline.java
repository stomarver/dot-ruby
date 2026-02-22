package SwordsGame.client.graphics;

public final class BlockColorPipeline {
    private BlockColorPipeline() {}

    public static float[] resolveTint(BlockProps props, int seed) {
        if (props == null || !props.hasRandomColor()) {
            return new float[] {1.0f, 1.0f, 1.0f};
        }

        float shift = props.getRandomColorShift();
        float low = 1.0f - shift;
        int dominantChannel = Math.abs(seed) % 3;

        float r = low;
        float g = low;
        float b = low;
        if (dominantChannel == 0) r = 1.0f;
        else if (dominantChannel == 1) g = 1.0f;
        else b = 1.0f;

        return new float[] {r, g, b};
    }
}
