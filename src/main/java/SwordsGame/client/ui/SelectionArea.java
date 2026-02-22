package SwordsGame.client.ui;

import org.joml.Math;
import org.joml.Vector4f;

public class SelectionArea {
    private final Vector4f bounds = new Vector4f();

    public void update(float virtualWidth, float virtualHeight) {
        float minX = (virtualWidth - (virtualHeight * 4f / 3f)) * 0.5f;
        bounds.set(minX, 0f, virtualWidth - minX, virtualHeight);
    }

    public float minX() { return bounds.x; }
    public float minY() { return bounds.y; }
    public float maxX() { return bounds.z; }
    public float maxY() { return bounds.w; }

    public float clampX(float x) { return Math.clamp(bounds.x, bounds.z, x); }
    public float clampY(float y) { return Math.clamp(bounds.y, bounds.w, y); }

    public boolean contains(float x, float y) {
        return x >= bounds.x && x <= bounds.z && y >= bounds.y && y <= bounds.w;
    }
}
