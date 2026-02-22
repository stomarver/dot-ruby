package SwordsGame.client.ui;

import org.joml.Vector2f;

public class BoxSelectionMath {
    private final Vector2f start = new Vector2f();
    private final Vector2f end = new Vector2f();

    public void begin(float x, float y) {
        start.set(x, y);
        end.set(x, y);
    }

    public void moveEnd(float x, float y) {
        end.set(x, y);
    }

    public float minX() {
        return snapToVirtualPixel(Math.min(start.x, end.x));
    }

    public float minY() {
        return snapToVirtualPixel(Math.min(start.y, end.y));
    }

    public float maxX() {
        return snapToVirtualPixel(Math.max(start.x, end.x));
    }

    public float maxY() {
        return snapToVirtualPixel(Math.max(start.y, end.y));
    }

    public boolean hasVisibleArea() {
        return (maxX() - minX()) >= 1f && (maxY() - minY()) >= 1f;
    }

    public float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    public boolean isInside(float x, float y, float minX, float minY, float maxX, float maxY) {
        return x >= minX && x <= maxX && y >= minY && y <= maxY;
    }

    private float snapToVirtualPixel(float value) {
        return (float) Math.floor(value);
    }
}
