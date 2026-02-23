package SwordsGame.client.core;

public final class WindowMouseMath {
    private WindowMouseMath() {
    }

    public static float clampMouseX(float value, float minClamp, float maxClamp, boolean useClamp, int virtualWidth) {
        float x = Math.max(0f, Math.min(value, virtualWidth - 1f));
        if (useClamp) {
            x = Math.max(minClamp, Math.min(x, maxClamp));
        }
        return x;
    }

    public static float clampMouseY(float value, float minClamp, float maxClamp, boolean useClamp, int virtualHeight) {
        float y = Math.max(0f, Math.min(value, virtualHeight));
        if (useClamp) {
            y = Math.max(minClamp, Math.min(y, maxClamp));
        }
        return y;
    }
}
